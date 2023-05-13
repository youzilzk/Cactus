package com.gyf.cactus.sample.client.handlers;

import com.alibaba.fastjson.JSONObject;
import com.gyf.cactus.sample.client.entity.Client;
import com.gyf.cactus.sample.client.entity.Tunnel;
import com.gyf.cactus.sample.client.manager.ChannelManager;
import com.gyf.cactus.sample.client.manager.ClientManager;
import com.gyf.cactus.sample.client.manager.FutureManager;
import com.gyf.cactus.sample.client.manager.TunnelManager;
import com.gyf.cactus.sample.client.work.UserClientBootstrap;
import com.gyf.cactus.sample.client.work.UserServerBootstrap;
import com.gyf.cactus.sample.common.protocol.Constants;
import com.gyf.cactus.sample.common.protocol.ProxyMessage;
import com.gyf.cactus.sample.common.utils.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.net.InetSocketAddress;

/**
 * 客户端和服务端连接处理
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {
    private static LoggerFactory log = LoggerFactory.getLogger();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage proxyMessage) throws Exception {
        switch (proxyMessage.getType()) {
            case LINK:
                handleLinkMessage(ctx, proxyMessage);
                break;
            case UNLINK:
                handleUnlinkMessage(ctx, proxyMessage);
                break;
            case TUNNEL_MESSAGE:
                handleReceiveTunnelMessage(ctx, proxyMessage);
                break;
            case OPEN_PORT:
                handleOpenPortMessage(ctx, proxyMessage);
                break;
            case CLOSE_PORT:
                handleClosePortMessage(ctx, proxyMessage);
                break;
            case OPEN_CONNECT:
                handleOpenConnectMessage(ctx, proxyMessage);
                break;
            case CLOSE_CONNECT:
                handleCloseConnectMessage(ctx, proxyMessage);
                break;
            case RELAY:
                handleTransferMessage(ctx, proxyMessage);
                break;
            case HEARTBEAT:
                handleHeartbeatMessage(ctx, proxyMessage);
                break;
            default:
                break;
        }

    }

    private void handleOpenPortMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        Channel channel = ctx.channel();
        Tunnel tunnel = JSONObject.parseObject(new String(proxyMessage.getData()), Tunnel.class);

        Integer toPort = tunnel.getToPort();
        //需打开端口
        try {
            ChannelFuture future = UserServerBootstrap.start(toPort);
            FutureManager.updateChannelFuture(tunnel.getId(), future);

            log.info("隧道在本地开启端口[{}]成功!", toPort);

            //proxyMessage的其他信息不变
            proxyMessage.setContent(Constants.STATE.SUCCESS.value);

            //回复客户端
            channel.writeAndFlush(proxyMessage);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("隧道在本地开启端口[{}]失败, message: {}", toPort, e.getMessage());

            //proxyMessage的其他信息不变
            proxyMessage.setContent(Constants.STATE.FAILED.value);

            //回复客户端
            channel.writeAndFlush(proxyMessage);
        }


    }

    private void handleClosePortMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) throws InterruptedException {
        Tunnel tunnel = JSONObject.parseObject(new String(proxyMessage.getData()), Tunnel.class);
        log.info("客户端停止监听端口{}", tunnel.getToPort());
        FutureManager.close(tunnel.getId());
    }

    private void handleTransferMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        Channel toChannel = ctx.channel().attr(Constants.TOWARD_CHANNEL).get();
        assert toChannel != null;

        byte[] data = proxyMessage.getData();

        log.info("收到服务器响应信息, 数据长度: {} [byte]", data.length);
        ByteBuf buf = ctx.alloc().buffer(data.length);
        buf.writeBytes(data);

        //转发消息
        toChannel.writeAndFlush(buf);
    }

    private void handleHeartbeatMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        log.info("收到心跳回复[{}]", ctx.channel().id());
    }

    private void handleOpenConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) throws InterruptedException {
        Channel channel = ctx.channel();
        Tunnel tunnel = JSONObject.parseObject(new String(proxyMessage.getData()), Tunnel.class);

        if (proxyMessage.getContent().equals(Constants.STATE.SUCCESS.value)) {
            Channel towardChannel = channel.attr(Constants.TOWARD_CHANNEL).get();
            //远程链路打开成功, 开启可读
            towardChannel.config().setOption(ChannelOption.AUTO_READ, true);

        } else if (proxyMessage.getContent().equals(Constants.STATE.FAILED.value)) {
            Channel towardChannel = channel.attr(Constants.TOWARD_CHANNEL).get();

            //如果失败, 关闭两个链路, 丢弃数据
            towardChannel.close().sync();
            channel.close().sync();

        } else if (proxyMessage.getContent().equals(Constants.STATE.REQUEST.value)) {
            try {
                //连接已有端口
                ChannelFuture future = UserClientBootstrap.getUserBootstrap().connect(new InetSocketAddress(tunnel.getFromHost(), tunnel.getFromPort())).sync();
                // 连接成功
                if (future.isSuccess()) {

                    //连接服务端
                    UserClientBootstrap.getBootstrap().connect(UserClientBootstrap.getInetSocketAddress()).addListener((ChannelFutureListener) future1 -> {
                        if (future1.isSuccess()) {
                            //绑定双向Channel
                            future.channel().attr(Constants.TOWARD_CHANNEL).set(future1.channel());
                            future1.channel().attr(Constants.TOWARD_CHANNEL).set(future.channel());

                            log.info("源[{}:{}]请求连接目标[{}:{}]成功, 完成绑定连接目标到服务器的双向链路!", tunnel.getToClient(), tunnel.getToPort(), tunnel.getFromHost(), tunnel.getFromPort());

                            proxyMessage.setContent(Constants.STATE.SUCCESS.value);
                            //回复客户端成功消息, 使用新链路回复, 以便服务端绑定链路
                            future1.channel().writeAndFlush(proxyMessage);
                        } else {
                            log.info("源[{}:{}]请求连接目标[{}:{}]失败!", tunnel.getToClient(), tunnel.getToPort(), tunnel.getFromHost(), tunnel.getFromPort());

                            future.channel().close().sync();
                            future1.channel().close().sync();

                            proxyMessage.setContent(Constants.STATE.FAILED.value);
                            //回复客户端
                            channel.writeAndFlush(proxyMessage);
                        }

                    });

                } else {
                    log.info("源[{}:{}]请求连接服务器失败!", tunnel.getToClient(), tunnel.getToPort());

                    future.channel().close().sync();

                    proxyMessage.setContent(Constants.STATE.FAILED.value);
                    //回复客户端
                    channel.writeAndFlush(proxyMessage);
                }
            } catch (Exception e) {
                log.info("客户端连接目标时异常!");
                e.printStackTrace();
            }
        }
    }


    private void handleCloseConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) throws InterruptedException {
        Channel channel = ctx.channel();
        Channel towardChannel = channel.attr(Constants.TOWARD_CHANNEL).get();
        log.info("关闭链路[({})<->({})]", channel.id(), towardChannel.id());
        channel.close().sync();
        towardChannel.close().sync();
    }

    private void handleUnlinkMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) throws InterruptedException {
        Channel channel = ctx.channel();
        InetSocketAddress address = (InetSocketAddress) (ctx.channel().remoteAddress());
        ProxyMessage.Content content = JSONObject.parseObject(proxyMessage.getContent(), ProxyMessage.Content.class);

        log.info("释放连接, {}:{}", address.getHostString(), address.getPort());
        log.info("释放原因, {}:{}", content.getCode(), content.getMessage());

        channel.close().sync();
        ClientManager.removeClient();
        System.exit(2);
    }

    private void handleLinkMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        ProxyMessage.Content content = JSONObject.parseObject(proxyMessage.getContent(), ProxyMessage.Content.class);
        log.info("code={},message={}", content.getCode(), content.getMessage());

        String data = new String(proxyMessage.getData());
        Client client = JSONObject.parseObject(data, Client.class);

        //更新客户端信息
        client.setChannel(ctx.channel());
        ClientManager.updateClient(client);
    }

    private void handleReceiveTunnelMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        Channel channel = ctx.channel();
        String data = new String(proxyMessage.getData());
        Tunnel tunnel = JSONObject.parseObject(data, Tunnel.class);

        log.info("接收隧道信息[{}]", tunnel.toString());
        TunnelManager.updateTunnelMap(tunnel);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        String clientId = channel.attr(Constants.CLIENT_ID).get();

        InetSocketAddress address = (InetSocketAddress) (ctx.channel().remoteAddress());
        if (clientId != null) {
            //如果不空则为客户端链路
            log.info("客户端异常[{}:{}, clientId={},channelId={}], message: {}", address.getHostString(), address.getPort(), clientId, channel.id(), cause.getMessage());
            ChannelManager.closeAll();
            FutureManager.closeAll();

            channel.close().sync();
        } else {
            //否则为隧道链路
            log.info("链路关闭[{}:{}, channelId={}], message: {}", address.getHostString(), address.getPort(), channel.id(), cause.getMessage());
            Channel towardChannel = channel.attr(Constants.TOWARD_CHANNEL).get();
            towardChannel.close().sync();
            channel.close().sync();
        }
        super.exceptionCaught(ctx, cause);
    }

}
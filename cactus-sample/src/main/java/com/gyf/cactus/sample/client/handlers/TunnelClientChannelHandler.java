package com.gyf.cactus.sample.client.handlers;

import com.gyf.cactus.sample.common.protocol.Constants;
import com.gyf.cactus.sample.common.protocol.ProxyMessage;
import com.gyf.cactus.sample.common.utils.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

/**
 * 隧道客户端连接端口一方处理
 */
public class TunnelClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static LoggerFactory log = LoggerFactory.getLogger();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        //收到来自连接的信息
        Channel channel = ctx.channel();
        InetSocketAddress address = (InetSocketAddress) (ctx.channel().remoteAddress());

        //监听端口获取到信息, 尝试获取目标Channel, 为空则请求开启链路
        Channel toChannel = channel.attr(Constants.TOWARD_CHANNEL).get();

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        log.info("收到主机[{}:{}]请求消息, 将转发到服务器, 数据长度: {} [byte]", address.getHostString(), address.getPort(), bytes.length);

        //转发信息
        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TYPE.RELAY);
        proxyMessage.setData(bytes);

        toChannel.writeAndFlush(proxyMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress address = (InetSocketAddress) (ctx.channel().remoteAddress());
        log.info("连接隧道断开连接[{}:{}, channelId={}], message: {}", address.getHostString(), address.getPort(), channel.id(), cause.getMessage());
        Channel towardChannel = channel.attr(Constants.TOWARD_CHANNEL).get();

        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TYPE.CLOSE_CONNECT);

        towardChannel.writeAndFlush(proxyMessage);
        //下发关闭对向连接链路
        towardChannel.writeAndFlush(proxyMessage);
        towardChannel.close().sync();
        channel.close().sync();
        super.exceptionCaught(ctx, cause);
    }
}

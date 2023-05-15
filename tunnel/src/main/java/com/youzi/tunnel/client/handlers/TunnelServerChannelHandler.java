package com.youzi.tunnel.client.handlers;


import com.youzi.tunnel.common.protocol.Constants;
import com.youzi.tunnel.common.protocol.ProxyMessage;
import com.youzi.tunnel.common.utils.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

/**
 * 隧道客户端监听端口一方处理
 */
public class TunnelServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static LoggerFactory log = LoggerFactory.getLogger();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress address = (InetSocketAddress) (ctx.channel().remoteAddress());

        //监听端口获取到信息, 尝试获取目标Channel
        Channel toChannel = channel.attr(Constants.TOWARD_CHANNEL).get();

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        log.info("服务器[{}:{}]请求转发消息,将转发到目标主机, 数据长度: {} [byte]", address.getHostString(), address.getPort(), bytes.length);

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
        log.info("远程主机断开连接[{}:{},channelId={}], message: {}", address.getHostString(), address.getPort(), channel.id(), cause.getMessage());
        Channel towardChannel = channel.attr(Constants.TOWARD_CHANNEL).get();
        towardChannel.close().sync();
        channel.close().sync();
        super.exceptionCaught(ctx, cause);
    }
}
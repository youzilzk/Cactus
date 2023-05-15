package com.youzi.tunnel.client.handlers;


import com.youzi.tunnel.common.protocol.Constants;
import com.youzi.tunnel.common.protocol.ProxyMessage;
import com.youzi.tunnel.common.utils.LoggerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;


public class IdleCheckHandler extends IdleStateHandler {
    private static final LoggerFactory log = LoggerFactory.getLogger();

    public static final int READ_IDLE_TIME = 50;
    public static final int WRITE_IDLE_TIME = 45;

    public IdleCheckHandler() {
        super(READ_IDLE_TIME, WRITE_IDLE_TIME, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        Channel channel = ctx.channel();
        String clientId = channel.attr(Constants.CLIENT_ID).get();

        InetSocketAddress address = (InetSocketAddress) (ctx.channel().remoteAddress());
        if (clientId != null) {
            if (IdleState.WRITER_IDLE == evt.state()) {
                log.info("客户端写闲置, 发送心跳[{}]", channel.id());
                ProxyMessage proxyMessage = new ProxyMessage();
                proxyMessage.setType(ProxyMessage.TYPE.HEARTBEAT);
                ctx.channel().writeAndFlush(proxyMessage);
            } else {
                log.info("客户端读闲置, 关闭链路[{}]", channel.id());
                channel.close().sync();
            }
        } else {
            //否则为隧道链路
            Channel towardChannel = channel.attr(Constants.TOWARD_CHANNEL).get();
            log.info("隧道链路闲置,将关闭[{}:{}, {}<->{}]", address.getHostString(), address.getPort(), channel.id(), towardChannel.id());

            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setType(ProxyMessage.TYPE.CLOSE_CONNECT);
            //通知服务器关闭对向连接链路
            channel.writeAndFlush(proxyMessage);

            towardChannel.close().sync();
            channel.close().sync();
        }
    }
}

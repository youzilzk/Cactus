package com.youzi.tunnel.client.work;


import com.youzi.tunnel.client.config.ClientProperties;
import com.youzi.tunnel.client.handlers.ClientChannelHandler;
import com.youzi.tunnel.client.handlers.IdleCheckHandler;
import com.youzi.tunnel.client.handlers.TunnelClientChannelHandler;
import com.youzi.tunnel.client.work.SslContextCreator;
import com.youzi.tunnel.common.protocol.MessageDecoder;
import com.youzi.tunnel.common.protocol.MessageEncoder;
import com.youzi.tunnel.common.utils.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;

/**
 * 连接启动器
 */
public class UserClientBootstrap {
    private static LoggerFactory log = LoggerFactory.getLogger();

    private static Bootstrap bootstrap;
    private static Bootstrap userBootstrap;
    private static InetSocketAddress inetAddress;


    public static InetSocketAddress getInetSocketAddress() {
        if (inetAddress == null) {
            initInetSocketAddress();
        }
        return inetAddress;
    }

    private static void initInetSocketAddress() {
        inetAddress = new InetSocketAddress(ClientProperties.getInstance().getServerHost(), ClientProperties.getInstance().getServerPort());
    }

    public static Bootstrap getBootstrap() {
        if (bootstrap == null) {
            initBootstrap();
        }
        return bootstrap;
    }

    private static void initBootstrap() {
        bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        if (ClientProperties.getInstance().isSslEnable()) {
                            SSLContext sslContext = SslContextCreator.getSSLContext();
                            SSLEngine sslEngine = sslContext.createSSLEngine();
                            sslEngine.setUseClientMode(true);

                            ch.pipeline().addLast(new SslHandler(sslEngine));
                        }
                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new MessageEncoder());
                        ch.pipeline().addLast(new IdleCheckHandler());
                        ch.pipeline().addLast(new ClientChannelHandler());

                    }
                });
    }

    public static Bootstrap getUserBootstrap() {
        if (userBootstrap == null) {
            initUserBootstrap();
        }
        return userBootstrap;
    }

    private static void initUserBootstrap() {
        userBootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TunnelClientChannelHandler());

                    }
                });
    }
}

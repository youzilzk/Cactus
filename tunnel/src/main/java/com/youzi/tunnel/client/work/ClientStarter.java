package com.youzi.tunnel.client.work;

import android.content.Context;
import com.alibaba.fastjson.JSONObject;
import com.youzi.tunnel.client.config.ClientProperties;
import com.youzi.tunnel.client.entity.Client;
import com.youzi.tunnel.client.handlers.ClientChannelHandler;
import com.youzi.tunnel.client.handlers.IdleCheckHandler;
import com.youzi.tunnel.client.manager.Manager;
import com.youzi.tunnel.client.work.SslContextCreator;
import com.youzi.tunnel.common.protocol.Constants;
import com.youzi.tunnel.common.protocol.MessageDecoder;
import com.youzi.tunnel.common.protocol.MessageEncoder;
import com.youzi.tunnel.common.protocol.ProxyMessage;
import com.youzi.tunnel.common.utils.AndroidUtil;
import com.youzi.tunnel.common.utils.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * 客户端启动器
 */
public class ClientStarter {
    private static LoggerFactory log = LoggerFactory.getLogger();


    private static Channel clientChannel;

    public static void start() {
        String clientId = ClientProperties.getInstance().getClientId();
        InetSocketAddress inetAddress = new InetSocketAddress(ClientProperties.getInstance().getServerHost(), ClientProperties.getInstance().getServerPort());

        Bootstrap bootstrap = new Bootstrap()
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
        //连接服务器
        bootstrap.connect(inetAddress).addListener((ChannelFutureListener) future -> {
            Channel channel = future.channel();
            clientChannel = channel;
            //记录客户端id, 链路关闭时用, 获取时为空则说明是隧道链路, 否则为客户端链路
            channel.attr(Constants.CLIENT_ID).set(clientId);

            // 连接后端服务器成功
            if (future.isSuccess()) {
                //认证
                ProxyMessage proxyMessage = new ProxyMessage();
                proxyMessage.setType(ProxyMessage.TYPE.LINK);

                Context context = Manager.getContext();
                String macAddress = AndroidUtil.getMacAddress(context);

                proxyMessage.setData(JSONObject.toJSONString(new Client(clientId, macAddress)).getBytes(Charset.forName("UTF-8")));
                channel.writeAndFlush(proxyMessage);
            } else {
                log.info("连接失败");
                channel.close().sync();
                System.exit(2);
            }
        });
    }

    public static void stop() {
        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TYPE.UNLINK);
        clientChannel.writeAndFlush(proxyMessage);
    }
}

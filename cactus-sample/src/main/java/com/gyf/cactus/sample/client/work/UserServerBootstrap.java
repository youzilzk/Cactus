package com.gyf.cactus.sample.client.work;


import com.alibaba.fastjson.JSONObject;
import com.gyf.cactus.sample.client.entity.Tunnel;
import com.gyf.cactus.sample.client.handlers.TunnelServerChannelHandler;
import com.gyf.cactus.sample.client.manager.ChannelManager;
import com.gyf.cactus.sample.client.manager.TunnelManager;
import com.gyf.cactus.sample.common.protocol.Constants;
import com.gyf.cactus.sample.common.protocol.ProxyMessage;
import com.gyf.cactus.sample.common.utils.LoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

/**
 * 监听端口启动器
 */
public class UserServerBootstrap {
    private static LoggerFactory log = LoggerFactory.getLogger();

    private static ConcurrentLinkedQueue<NioEventLoopGroup> bossList = new ConcurrentLinkedQueue();

    private static ServerBootstrap serverBootstrap;

    /*
     * 开启端口
     * */
    public static ChannelFuture start(Integer port) throws InterruptedException, ExecutionException {
        initServerBootstrap();
        return serverBootstrap.bind(port).sync();
    }


    private static void initServerBootstrap() {
        if (serverBootstrap == null) {
            serverBootstrap = new ServerBootstrap();
            NioEventLoopGroup serverBossGroup = new NioEventLoopGroup(8);
            NioEventLoopGroup serverWorkerGroup = new NioEventLoopGroup(8);

            bossList.add(serverBossGroup);
            bossList.add(serverWorkerGroup);

            serverBootstrap.group(serverBossGroup, serverWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) {
                            ChannelManager.updateChannel(channel);
                            try {
                                //请求开启链路, 绑定端口到服务端的链路
                                ChannelFuture future = UserClientBootstrap.getBootstrap().connect(UserClientBootstrap.getInetSocketAddress()).sync();
                                // 连接后端服务器成功
                                if (future.isSuccess()) {
                                    //绑定链路
                                    Channel futureChannel = future.channel();
                                    channel.attr(Constants.TOWARD_CHANNEL).set(futureChannel);
                                    futureChannel.attr(Constants.TOWARD_CHANNEL).set(channel);

                                    log.info("收到连接消息, 开启并绑定链路, [channelId={}, futureChannelId={}]", channel.id(), futureChannel.id());


                                    InetSocketAddress address = (InetSocketAddress) (channel.localAddress());
                                    //通过端口获取到链路信息
                                    Tunnel tunnel = TunnelManager.getChannel(address.getPort());

                                    //设置不可读, 远程链路打开后才可读
                                    channel.config().setOption(ChannelOption.AUTO_READ, false);

                                    //发送开启链路请求信息
                                    ProxyMessage proxyMessage = new ProxyMessage();
                                    proxyMessage.setContent(Constants.STATE.REQUEST.value);
                                    proxyMessage.setType(ProxyMessage.TYPE.OPEN_CONNECT);

                                    proxyMessage.setData(JSONObject.toJSONString(tunnel).getBytes(Charset.forName("UTF-8")));

                                    //使用新开启的链路发送这条消息, 以便服务端绑定链路
                                    futureChannel.writeAndFlush(proxyMessage);

                                    ChannelManager.updateChannel(futureChannel);
                                } else {
                                    //把两个连接都关闭
                                    channel.close().sync();
                                    future.channel().close().sync();
                                    throw new Exception("请求开启链路失败, 连接后端服务器时失败, 本连接也将关闭");
                                }

                                channel.pipeline().addLast(new TunnelServerChannelHandler());

                            } catch (Exception e) {
                                log.info("请求开启链路异常!");
                            }
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            cause.printStackTrace();
                            super.exceptionCaught(ctx, cause);
                        }
                    });
        }

    }

    public static void shutdown() {
        for (NioEventLoopGroup eventExecutors : bossList) {
            eventExecutors.shutdownGracefully();
            bossList.remove(eventExecutors);
        }
    }
}

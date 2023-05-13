package com.gyf.cactus.sample.client.manager;


import io.netty.channel.ChannelFuture;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Future管理器,停止监听端口需要用到
 */
public class FutureManager {
    //tunnelId:ChannelFuture
    private static final Map<String, ChannelFuture> channelFutureMap = new ConcurrentHashMap<>();


    /**
     * 获取ChannelFuture
     *
     * @param tunnelId
     */
    public static ChannelFuture getChannelFuture(String tunnelId) {
        return channelFutureMap.get(tunnelId);
    }

    /**
     * 获取ChannelFuture
     */
    public static Collection<ChannelFuture> getChannelFuture() {
        return channelFutureMap.values();
    }


    /**
     * 更新ChannelFuture
     *
     * @param tunnelId
     * @param future
     */
    public static void updateChannelFuture(String tunnelId, ChannelFuture future) {
        channelFutureMap.put(tunnelId, future);
    }

    /**
     * 删除ChannelFuture
     *
     * @param tunnelId
     */
    public static void removeChannelFuture(String tunnelId) {
        channelFutureMap.remove(tunnelId);
    }

    /**
     * 关闭端口
     *
     * @param tunnelId
     */
    public static void close(String tunnelId) throws InterruptedException {
        channelFutureMap.get(tunnelId).channel().close().sync();
        channelFutureMap.remove(tunnelId);
    }

    /**
     * 关闭隧道
     */
    public static void closeAll() {
        channelFutureMap.keySet().forEach(e -> {
            try {
                channelFutureMap.get(e).channel().close().sync();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            channelFutureMap.remove(e);
        });

    }
}

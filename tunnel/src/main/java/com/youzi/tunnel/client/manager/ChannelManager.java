package com.youzi.tunnel.client.manager;

import io.netty.channel.Channel;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *Channel管理器
 */
public class ChannelManager {


    //隧道关系表在内存的映射<channelId:Channel>
    private static final Map<String, Channel> channelMap = new ConcurrentHashMap<>();


    /**
     * 获取隧道
     *
     * @param channelId
     */
    public static Channel getChannel(String channelId) {
        return channelMap.get(channelId);
    }

    /**
     * 获取隧道
     */
    public static Collection<Channel> getChannel() {
        return channelMap.values();
    }


    /**
     * 更新隧道
     *
     * @param channel
     */
    public static void updateChannel(Channel channel) {
        channelMap.put(channel.id().toString(), channel);
    }

    /**
     * 删除隧道
     *
     * @param channelId
     */
    public static void removeChannel(String channelId) {
        channelMap.remove(channelId);
    }


    /**
     * 关闭隧道
     *
     * @param channelId
     */
    public static void close(String channelId) throws InterruptedException {
        channelMap.get(channelId).close().sync();
        channelMap.remove(channelId);
    }

    /**
     * 关闭隧道
     */
    public static void closeAll() {
        channelMap.keySet().forEach(e -> {
            try {
                channelMap.get(e).close().sync();
                channelMap.remove(e);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        });
    }


}

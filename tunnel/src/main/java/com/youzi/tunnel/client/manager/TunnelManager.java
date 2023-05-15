package com.youzi.tunnel.client.manager;

import com.youzi.tunnel.client.entity.Tunnel;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * Tunnel管理器
 */
public class TunnelManager {


    //隧道关系表在内存的映射<tunnelId:Tunnel>
    private static final Map<String, Tunnel> tunnelMap = new ConcurrentHashMap<>();


    /**
     * 获取隧道
     *
     * @param tunnelId
     */
    public static Tunnel getTunnel(String tunnelId) {
        return tunnelMap.get(tunnelId);
    }

    /**
     * 获取隧道
     *
     * @param
     */
    public static Collection<Tunnel> getByFromClient(String clientId) {
        Collection<Tunnel> tunnels = tunnelMap.values();
        return tunnels.stream().filter(tunnel -> tunnel.getFromClient().equals(clientId)).collect(Collectors.toList());
    }

    /**
     * 获取隧道
     *
     * @param
     */
    public static Collection<Tunnel> getByToClient(String clientId) {
        Collection<Tunnel> tunnels = tunnelMap.values();
        return tunnels.stream().filter(tunnel -> tunnel.getToClient().equals(clientId)).collect(Collectors.toList());
    }

    /**
     * 获取隧道
     *
     * @param
     */
    public static Collection<Tunnel> getTunnels() {
        return tunnelMap.values();
    }

    /**
     * 获取链路
     *
     * @param
     */
    public static Tunnel getChannel(Integer toPort) {
        Collection<Tunnel> tunnels = tunnelMap.values();
        for (Tunnel tunnel : tunnels) {
            if (tunnel.getToPort().equals(toPort)) {
                return tunnel;
            }
        }
        return null;
    }


    /**
     * 更新隧道
     *
     * @param tunnel
     */
    public static void updateTunnelMap(Tunnel tunnel) {
        tunnelMap.put(tunnel.getId(), tunnel);
    }

    /**
     * 删除隧道
     *
     * @param tunnelId
     */
    public static void removeTunnelMap(String tunnelId) {
        tunnelMap.remove(tunnelId);
    }


}

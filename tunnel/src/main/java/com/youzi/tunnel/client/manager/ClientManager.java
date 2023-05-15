package com.youzi.tunnel.client.manager;


import com.youzi.tunnel.client.entity.Client;

/**
 *Client管理器
 */
public class ClientManager {

    //客户端表在内存的映射<clientId:Client>
    private static Client client;


    /**
     * 获取客户端
     *
     * @param
     */
    public static Client getClient() {
        return client;
    }


    /**
     * 更新客户端
     *
     * @param client
     */
    public static void updateClient(Client client) {
        client = client;
    }

    /**
     * 移除客户端
     */
    public static void removeClient() {
        client = null;
    }


}

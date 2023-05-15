package com.youzi.tunnel.client;


import com.youzi.tunnel.client.config.ClientProperties;
import com.youzi.tunnel.client.manager.ChannelManager;
import com.youzi.tunnel.client.manager.FutureManager;
import com.youzi.tunnel.client.work.ClientStarter;
import com.youzi.tunnel.client.work.UserServerBootstrap;
import com.youzi.tunnel.common.Config;

import java.io.IOException;

public class ClientApplication {

    public static void start(String host, Integer port) {
        try {
            Config.getInstance().init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (host != null) {
            ClientProperties.getInstance().setServerHost(host);
        }
        if (port != null) {
            ClientProperties.getInstance().setServerPort(port);
        }
        ClientStarter.start();
    }

    public static void stop() {
        ClientStarter.stop();
        ChannelManager.closeAll();
        FutureManager.closeAll();
        UserServerBootstrap.shutdown();
    }

}

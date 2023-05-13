package com.gyf.cactus.sample.client;


import com.gyf.cactus.sample.client.config.ClientProperties;
import com.gyf.cactus.sample.client.manager.ChannelManager;
import com.gyf.cactus.sample.client.manager.FutureManager;
import com.gyf.cactus.sample.client.work.ClientStarter;
import com.gyf.cactus.sample.client.work.UserServerBootstrap;
import com.gyf.cactus.sample.common.Config;

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

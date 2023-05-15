package com.youzi.tunnel.client;

import com.gyf.cactus.callback.CactusCallback;
import com.youzi.tunnel.client.ClientApplication;

public class TunnelServiceEnter implements CactusCallback {
    @Override
    public void doWork(int times) {
        com.youzi.tunnel.client.ClientApplication.start(null, null);
    }

    @Override
    public void onStop() {
        ClientApplication.stop();
    }
}

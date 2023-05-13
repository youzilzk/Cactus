package com.gyf.cactus.sample.client;

import com.gyf.cactus.callback.CactusCallback;

public class TunnelCallback implements CactusCallback {
    @Override
    public void doWork(int times) {
        ClientApplication.start(null, null);
    }

    @Override
    public void onStop() {
        ClientApplication.stop();
    }
}

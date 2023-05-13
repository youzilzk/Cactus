package com.gyf.cactus.sample.client.manager;

import android.content.res.AssetManager;

public class Manager {
    private static AssetManager assetManager;

    public static AssetManager getAssetManager() {
        return assetManager;
    }

    public static void initAssetManager(AssetManager manager) {
        assetManager = manager;
    }
}

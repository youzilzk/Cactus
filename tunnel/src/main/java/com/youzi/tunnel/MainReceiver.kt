package com.youzi.tunnel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gyf.cactus.Cactus


class MainReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.action?.apply {
            when (this) {
                Cactus.CACTUS_WORK -> {
                    Log.d(
                        App.TAG,
                        this + "--" + intent.getIntExtra(Cactus.CACTUS_TIMES, 0)
                    )
                }
                Cactus.CACTUS_STOP -> {
                    Log.d(App.TAG, this)
                }
                Cactus.CACTUS_BACKGROUND -> {
                    Log.d(App.TAG, this)
                }
                Cactus.CACTUS_FOREGROUND -> {
                    Log.d(App.TAG, this)
                }
            }
        }
    }
}
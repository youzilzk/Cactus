package com.youzi.tunnel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppManager.INSTANCE.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.INSTANCE.removeActivity(this)
    }
}
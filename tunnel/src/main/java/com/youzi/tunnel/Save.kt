package com.youzi.tunnel

import android.annotation.SuppressLint


@SuppressLint("StaticFieldLeak")
object Save {
    var timer by preference(0L)
    var lastTimer by preference(0L)
    var date by preference("0000-01-01 00:00:00")
    var endDate by preference("0000-01-01 00:00:00")
}
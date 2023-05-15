package com.youzi.tunnel

import kotlin.reflect.jvm.jvmName



inline fun <reified R, T> R.preference(defaultValue: T) =
    Preference(App.context, "", defaultValue, R::class.jvmName)
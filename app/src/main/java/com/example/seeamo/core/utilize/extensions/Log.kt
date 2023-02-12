package com.example.seeamo.core.utilize.extensions

import android.util.Log
import com.example.seeamo.BuildConfig

inline fun Any.logInfo(crossinline message: () -> String) {
    if (BuildConfig.DEBUG)
        Log.i(this::class.java.simpleName, message())
}

inline fun Any.logError(crossinline message: () -> String) {
    if (BuildConfig.DEBUG)
        Log.e(this::class.java.simpleName, message())
}

inline fun Any.logDebug(crossinline message: () -> String) {
    if (BuildConfig.DEBUG)
        Log.d(this::class.java.simpleName, message())
}
package com.example.seeamo.utilize.extensions

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun Activity.changeDecorFitsSystemWindows(isDecorFits: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(window, isDecorFits)
}

fun Activity.setStatusBarAppearance(isLight: Boolean) {
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = isLight
}

inline fun AppCompatActivity.launchScope(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch(context, start) {
        block()
    }
}
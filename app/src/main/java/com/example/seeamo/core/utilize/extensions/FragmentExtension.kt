package com.example.seeamo.core.utilize.extensions

import android.content.res.Configuration
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.*

inline fun Fragment.repeatViewLifecycle(
    dispatchers: CoroutineDispatcher = Dispatchers.Main,
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
): Job =
    viewLifecycleOwner.lifecycleScope.launch(dispatchers) {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(activeState) {
            block()
        }
    }


inline fun Fragment.launchScope(
    dispatchers: CoroutineDispatcher = Dispatchers.Main,
    crossinline block: suspend CoroutineScope.() -> Unit
): Job =
    viewLifecycleOwner.lifecycleScope.launch(dispatchers) {
        block()
    }

fun Fragment.setStatusBarBackgroundColor(color: Int) {
    if (activity == null)
        return

    activity!!.window.statusBarColor = color
}

fun Fragment.setStatusBarAppearance(isLight: Boolean? = null, backToDefault: Boolean = false) {
    if (activity == null)
        return

    if (isLight != null && !backToDefault)
        WindowCompat.getInsetsController(
            activity!!.window,
            activity!!.window.decorView
        ).isAppearanceLightStatusBars = isLight
    else
        when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> setStatusBarAppearance(isLight = false)
            Configuration.UI_MODE_NIGHT_NO -> setStatusBarAppearance(isLight = true)
        }
}
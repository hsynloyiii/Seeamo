package com.example.seeamo.core.utilize.extensions

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

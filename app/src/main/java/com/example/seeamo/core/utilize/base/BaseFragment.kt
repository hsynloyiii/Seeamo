package com.example.seeamo.core.utilize.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.seeamo.core.utilize.helper.LayoutHelper

abstract class BaseFragment(
    private val hasBackGestureCallBack: Boolean
): Fragment() {

    lateinit var root: View

    lateinit var layoutHelper: LayoutHelper
        private set

    lateinit var baseColor: BaseColor
        private set

    abstract fun createViews(savedInstanceState: Bundle?)

    abstract fun setup(savedInstanceState: Bundle?)

    open fun onBackGesture() {}

    open fun onLifecycleCreate(owner: LifecycleOwner) {}
    open fun onLifecycleStart(owner: LifecycleOwner) {}
    open fun onLifecycleStop(owner: LifecycleOwner) {}
    open fun onLifecycleResume(owner: LifecycleOwner) {}
    open fun onLifecyclePause(owner: LifecycleOwner) {}
    open fun onLifecycleDestroy(owner: LifecycleOwner) {}

    private val onBackPressCallback = object : OnBackPressedCallback(hasBackGestureCallBack) {
        override fun handleOnBackPressed() {
            onBackGesture()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutHelper = LayoutHelper(context)
        baseColor = BaseColor(context ?: requireContext())
        createViews(savedInstanceState)
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressCallback)
//        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
//            override fun onStart(owner: LifecycleOwner) {
//                onLifecycleStart(owner)
//            }
//            override fun onStop(owner: LifecycleOwner) {
//                onLifecycleStop(owner)
//            }
//            override fun onResume(owner: LifecycleOwner) {
//                onLifecycleResume(owner)
//            }
//            override fun onPause(owner: LifecycleOwner) {
//                onLifecyclePause(owner)
//            }
//            override fun onDestroy(owner: LifecycleOwner) {
//                onLifecycleDestroy(owner)
//            }
//            override fun onCreate(owner: LifecycleOwner) {
//                onLifecycleCreate(owner)
//            }
//        })
        setup(savedInstanceState)
    }

}
package com.example.seeamo.utilize.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.seeamo.utilize.helper.LayoutHelper

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

    private val onBackPressCallback = object : OnBackPressedCallback(hasBackGestureCallBack) {
        override fun handleOnBackPressed() {
            onBackGesture()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setup(savedInstanceState)
    }

}
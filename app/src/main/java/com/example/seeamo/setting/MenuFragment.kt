package com.example.seeamo.setting

import android.os.Bundle
import android.widget.RelativeLayout
import com.example.seeamo.core.utilize.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment : BaseFragment(false) {

    private lateinit var mainLayout: RelativeLayout
    override fun createViews(savedInstanceState: Bundle?) {
        root = RelativeLayout(context).apply {
            setBackgroundColor(baseColor.red)
        }
    }

    override fun setup(savedInstanceState: Bundle?) {

    }
}
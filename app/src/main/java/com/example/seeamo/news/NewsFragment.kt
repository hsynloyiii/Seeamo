package com.example.seeamo.news

import android.os.Bundle
import android.widget.RelativeLayout
import com.example.seeamo.core.utilize.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsFragment : BaseFragment(false) {

    private lateinit var mainLayout: RelativeLayout
    override fun createViews(savedInstanceState: Bundle?) {
        root = RelativeLayout(context).apply {
            setBackgroundColor(baseColor.white)
        }
    }

    override fun setup(savedInstanceState: Bundle?) {

    }
}
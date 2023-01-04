package com.example.seeamo.ui.movie

import android.os.Bundle
import android.widget.RelativeLayout
import com.example.seeamo.utilize.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieFragment : BaseFragment(false) {

    private lateinit var mainLayout: RelativeLayout
    override fun createViews(savedInstanceState: Bundle?) {
        root = RelativeLayout(context).apply {
            setBackgroundColor(baseColor.gray)
        }
    }

    override fun setup(savedInstanceState: Bundle?) {

    }
}
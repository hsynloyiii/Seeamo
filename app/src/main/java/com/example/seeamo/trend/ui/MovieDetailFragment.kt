package com.example.seeamo.trend.ui

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.navigation.fragment.findNavController
import com.example.seeamo.core.ui.MainActivity
import com.example.seeamo.core.ui.NavRoutes
import com.example.seeamo.core.utilize.base.BaseFragment
import com.example.seeamo.core.utilize.extensions.defaultAppearance
import com.example.seeamo.core.utilize.extensions.logDebug
import com.example.seeamo.core.utilize.extensions.logInfo

class MovieDetailFragment : BaseFragment(false) {

    private lateinit var mainLayout: RelativeLayout

    override fun createViews(savedInstanceState: Bundle?) {
        root = RelativeLayout(requireContext())
        mainLayout = (root as RelativeLayout).apply {
//            defaultAppearance(baseColor)
            setBackgroundColor(baseColor.red)
        }
    }

    override fun setup(savedInstanceState: Bundle?) {
//        if (findNavController().previousBackStackEntry?.destination?.route == NavRoutes.Main.TREND_FRAGMENT) {
//            val trendFragment =
//                (activity as MainActivity).navHostFragment.childFragmentManager.fragments
//            logDebug { "The list is  $trendFragment" }
//        }
    }
}
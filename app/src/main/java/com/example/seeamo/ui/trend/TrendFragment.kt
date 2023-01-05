package com.example.seeamo.ui.trend

import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seeamo.R
import com.example.seeamo.utilize.base.BaseFragment
import com.example.seeamo.utilize.extensions.defaultAppearance
import com.example.seeamo.utilize.extensions.repeatViewLifecycle
import com.example.seeamo.utilize.extensions.toDp
import com.example.seeamo.utilize.extensions.withAlpha
import com.example.seeamo.utilize.helper.LayoutHelper
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TrendFragment: BaseFragment(false) {

    private lateinit var trendRecyclerView: RecyclerView

    private val trendViewModel by viewModels<TrendViewModel>()
    private lateinit var trendAdapter: TrendAdapter

    override fun createViews(savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                Log.i("TrendFragment", "onStart:")
            }
        })
        root = RecyclerView(requireContext())

        trendRecyclerView = (root as RecyclerView).apply {
            defaultAppearance(baseColor)

            id = R.id.trend_fragment_recycler_view
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)

            val divider = MaterialDividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                dividerInsetStart = 8.toDp(context)
                dividerInsetEnd = 8.toDp(context)
                isLastItemDecorated = false
                dividerColor = baseColor.outline.withAlpha(0.72)
            }
            addItemDecoration(divider)
        }


    }

    override fun setup(savedInstanceState: Bundle?) {
        bindTrendData()
    }

    private fun bindTrendData() {
        trendAdapter = TrendAdapter(baseColor, layoutHelper)
        trendRecyclerView.adapter = trendAdapter.withLoadStateFooter(
            TrendLoadStateAdapter(baseColor, layoutHelper) { trendAdapter.retry() }
        )

        repeatViewLifecycle {
            trendViewModel.trendResult.collect {
                Log.i("TrendFragment", "$it")
                trendAdapter.submitData(it)
            }
        }
    }
}
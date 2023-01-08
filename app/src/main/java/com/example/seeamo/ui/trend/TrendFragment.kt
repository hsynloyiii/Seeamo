package com.example.seeamo.ui.trend

import android.animation.LayoutTransition
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.seeamo.R
import com.example.seeamo.data.model.UIState
import com.example.seeamo.utilize.base.BaseFragment
import com.example.seeamo.utilize.extensions.*
import com.example.seeamo.utilize.helper.LayoutHelper
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrendFragment : BaseFragment(false) {

    companion object {
        const val TAG = "TrendFragment"
    }

    private lateinit var mainLayout: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var trendRecyclerView: RecyclerView
    private lateinit var errorTextView: TextView

    private val trendViewModel by viewModels<TrendViewModel>()
    private lateinit var trendAdapter: TrendAdapter

    override fun createViews(savedInstanceState: Bundle?) {
        root = RelativeLayout(requireContext())

        mainLayout = (root as RelativeLayout).apply {
            defaultAppearance(baseColor)
            layoutTransition = LayoutTransition()
        }

        swipeRefreshLayout = SwipeRefreshLayout(requireContext()).apply {
            id = R.id.trend_fragment_swipe_refresh_layout

            setOnRefreshListener {
                if (this@TrendFragment::trendAdapter.isInitialized) {
                    if (this@TrendFragment::errorTextView.isInitialized && errorTextView.isVisible)
                        trendAdapter.retry()
                    else
                        trendAdapter.refresh()
                }
            }

            mainLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
            )
        }

        trendRecyclerView = RecyclerView(requireContext()).apply {
            id = R.id.trend_fragment_recycler_view
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)

            val divider =
                MaterialDividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                    dividerInsetStart = 8.toDp(context)
                    dividerInsetEnd = 8.toDp(context)
                    isLastItemDecorated = false
                    dividerColor = baseColor.outline.withAlpha(0.72)
                }
            addItemDecoration(divider)

            swipeRefreshLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
            )
        }

        errorTextView = TextView(context).apply {
            id = R.id.trend_fragment_error_text_view
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(baseColor.onBackground)
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true

            setOnClickListener {
                if (this@TrendFragment::trendAdapter.isInitialized)
                    trendAdapter.retry()
            }

            mainLayout.addView(
                this,
                layoutHelper.createRelative(
                    LayoutHelper.MATCH_PARENT,
                    LayoutHelper.WRAP_CONTENT,
                    alignParent = RelativeLayout.CENTER_IN_PARENT,
                    startMargin = 16.toDp(context),
                    endMargin = 16.toDp(context)
                )
            )
        }
    }

    override fun setup(savedInstanceState: Bundle?) {
        bindTrendData()
    }

    private fun bindTrendData() {
        trendAdapter = TrendAdapter(baseColor, layoutHelper) {
            Log.i(TAG, "bindTrendData: ${it.id}")
            launchScope {
                trendViewModel.getTrendTrailer(requireContext(), it.id).collect { trendTrailerUIState ->
                    when(trendTrailerUIState.uiState) {
                        UIState.LOADING -> {
                            context?.toast("Loading ...")
                        }
                        UIState.SUCCEED -> {
                            context?.toast(trendTrailerUIState.trailerUrl)
                            Log.i(TAG, "bindTrendData: ${trendTrailerUIState.trailerUrl}")
                        }
                        UIState.FAILED -> {
                            context?.toast(trendTrailerUIState.failure_message)
                        }
                        else -> return@collect
                    }
                }
            }
        }
        trendRecyclerView.adapter = trendAdapter.withLoadStateFooter(
            TrendAdapter.TrendLoadStateAdapter(baseColor, layoutHelper) { trendAdapter.retry() }
        )

        repeatViewLifecycle {
            trendViewModel.trendResult.collect {
                trendAdapter.submitData(it)
            }
        }

        trendAdapter.addLoadStateListener { combinedLoadState ->
            swipeRefreshLayout.isRefreshing = combinedLoadState.refresh is LoadState.Loading

            val errorState = when {
                combinedLoadState.refresh is LoadState.Error -> combinedLoadState.refresh as LoadState.Error
                combinedLoadState.append is LoadState.Error -> null
                combinedLoadState.prepend is LoadState.Error -> null
                else -> null
            }

            errorState?.error?.message?.let {
                if (trendAdapter.itemCount < 1) {
                    errorTextView.isVisible = true
                    errorTextView.text = it
                } else {
                    errorTextView.isVisible = false
                    mainLayout.snack(it)
                }
            }
        }
    }


    override fun onLifecycleStart(owner: LifecycleOwner) {
        Log.i(TAG, "onStart:")
    }

    override fun onLifecycleCreate(owner: LifecycleOwner) {
        Log.i(TAG, "onLifecycleCreate: ")
    }
}
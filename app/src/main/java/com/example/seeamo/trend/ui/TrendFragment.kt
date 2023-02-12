package com.example.seeamo.trend.ui

import android.animation.LayoutTransition
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.seeamo.R
import com.example.seeamo.core.utilize.base.BaseFragment
import com.example.seeamo.core.utilize.extensions.*
import com.example.seeamo.core.utilize.helper.LayoutHelper
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TrendFragment : BaseFragment(false) {

    companion object {
        const val TAG = "TrendFragment"
        const val IS_FRAGMENT_BEING_DESTROYED_KEY = "isFragmentBeingDestroyed"
    }

    private lateinit var mainLayout: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var trendRecyclerView: RecyclerView
    private lateinit var errorTextView: TextView

    private val trendViewModel: TrendViewModel by viewModels()
    private var trendAdapter: TrendAdapter? = null

//    private lateinit var mediaSession: MediaSessionCompat
//    private lateinit var mediaSessionConnector: MediaSessionConnector
//    private lateinit var mediaController: MediaControllerCompat

    private val syncObject = Any()

    private var isFragmentBeingDestroyed = false
    private var savedInstanceState: Bundle? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_FRAGMENT_BEING_DESTROYED_KEY, isFragmentBeingDestroyed)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        if (savedInstanceState != null)
            isFragmentBeingDestroyed =
                savedInstanceState.getBoolean(IS_FRAGMENT_BEING_DESTROYED_KEY)
    }

    override fun createViews(savedInstanceState: Bundle?) {
        root = RelativeLayout(requireContext())

        mainLayout = (root as RelativeLayout).apply {
            defaultAppearance(baseColor)
            layoutTransition = LayoutTransition()
        }

        swipeRefreshLayout = SwipeRefreshLayout(requireContext()).apply {
            id = R.id.trend_fragment_swipe_refresh_layout

            setOnRefreshListener {
                trendAdapter?.apply {
                    if (errorTextView.isVisible)
                        retry()
                    else
                        refresh()
                }
            }

            mainLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
            )
        }

        trendRecyclerView =
            RecyclerView(requireContext()).apply {
                id = R.id.trend_fragment_recycler_view
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)

                val divider =
                    MaterialDividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                        dividerInsetStart = 8.toDp(context)
                        isLastItemDecorated = false
                        dividerColor = baseColor.outline.withAlpha(0.16)
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
            isVisible = false

            setOnClickListener {
                trendAdapter?.retry()
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
        repeatViewLifecycle {
            Log.i(TAG, "bindTrendData: ")
            trendViewModel.trendResult.collect {
                trendAdapter?.submitData(it)
            }
        }
        bindTrendData()
    }

    private fun bindTrendData() {
        if (trendAdapter != null)
            return

        trendAdapter =
            TrendAdapter(
                requireContext(),
                baseColor,
                layoutHelper,
                trendViewModel
            )

        trendAdapter!!.run {
            trendRecyclerView.adapter = withLoadStateFooter(
                TrendAdapter.TrendLoadStateAdapter(baseColor, layoutHelper) { retry() }
            )

            addLoadStateListener { combinedLoadState ->
                swipeRefreshLayout.isRefreshing = combinedLoadState.refresh is LoadState.Loading

                val errorState = when {
                    combinedLoadState.refresh is LoadState.Error -> combinedLoadState.refresh as LoadState.Error
                    combinedLoadState.append is LoadState.Error -> null
                    combinedLoadState.prepend is LoadState.Error -> null
                    else -> null
                }

                errorState?.error?.message?.let {
                    if (itemCount < 1) {
                        errorTextView.isVisible = true
                        errorTextView.text = it
                    } else {
                        errorTextView.isVisible = false
                        mainLayout.snack(it)
                    }
                }
            }

            trendRecyclerView.addOnChildAttachStateChangeListener(object :
                RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    val lm = trendRecyclerView.layoutManager as LinearLayoutManager
                    if (lastPlayedItemView != null &&
                        lastPlayedItemView == view &&
                        trendViewModel.lastPlayedItemListPosition in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
                    ) {
                        lastPlayedThumbnailLayout?.visibility = View.GONE
                        lastPlayedPlayerView?.visibility = View.VISIBLE
                        (lastPlayedItemView as FrameLayout).layoutTransition =
                            LayoutTransition()
//                    if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
//                        mediaController.transportControls.play()
//                        setPlayPauseButtonIcon(true)
//                    }
                        if (trendViewModel.isPlayerPlaying()) {
                            trendViewModel.playPlayer()
                            setPlayPauseButtonIcon(isPlaying = true)
                        }
                    }
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                    if (lastPlayedItemView != null && lastPlayedItemView == view) {
                        (lastPlayedItemView as FrameLayout).layoutTransition = null
                        lastPlayedThumbnailLayout?.visibility = View.VISIBLE
                        lastPlayedPlayerView?.visibility = View.GONE
//                    if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
//                        mediaController.transportControls.pause()
//                        setPlayPauseButtonIcon(false)
                        if (trendViewModel.isPlayerPlaying()) {
                            trendViewModel.pausePlayer()
                            setPlayPauseButtonIcon(isPlaying = false)
                        }
                    }
                }

            })

        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: ")
//        if (trendViewModel.exoPlayer != null) {
//            mediaSessionConnector.setPlayer(trendViewModel.exoPlayer)
//            mediaSession.isActive = true
//            if (this::trendRecyclerView.isInitialized) {
//                val lm = trendRecyclerView.layoutManager as LinearLayoutManager
//                if (trendViewModel.lastPlayedItemListPosition in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()) {
//                    trendViewModel.playPlayer()
//                    trendAdapter?.setPlayPauseButtonIcon(isPlaying = true)
//                }
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
        trendViewModel.savePlayerCurrentPosition()
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
        trendViewModel.pausePlayer()
        trendAdapter?.setPlayPauseButtonIcon(isPlaying = false)

//            mediaSessionConnector.setPlayer(null)
//            mediaSession.isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isFragmentBeingDestroyed && !isStateSaved)
        // The fragment destroyed by other reason than configuration changes
            trendViewModel.releasePlayer()
        else {
            val adapter = trendAdapter ?: return
            trendViewModel.removePlayerListener(adapter)
        }

        isFragmentBeingDestroyed = true
    }
}
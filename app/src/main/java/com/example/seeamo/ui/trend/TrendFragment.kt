package com.example.seeamo.ui.trend

import android.animation.LayoutTransition
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore.Video
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media.AudioAttributesCompat
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.seeamo.R
import com.example.seeamo.data.model.TrendTrailerUIState
import com.example.seeamo.data.model.UIState
import com.example.seeamo.utilize.base.BaseFragment
import com.example.seeamo.utilize.extensions.*
import com.example.seeamo.utilize.helper.ExoAudioFocusHelper
import com.example.seeamo.utilize.helper.LayoutHelper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TrendFragment : BaseFragment(false), PlayerHolderEventListener {

    companion object {
        const val TAG = "TrendFragment"
    }

//    private val LIST_STATE_KEY = "recycler_state"
//    private var recyclerViewState : Parcelable? = null
//    private lateinit var recyclerView: RecyclerView

    private lateinit var mainLayout: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var trendRecyclerView: RecyclerView
    private lateinit var errorTextView: TextView

    private val trendViewModel by viewModels<TrendViewModel>()
    private lateinit var trendAdapter: TrendAdapter

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var mediaController: MediaControllerCompat

    private var videoPosition = 0L

    private lateinit var audioManager: AudioManager

    private var previousItemView: View? = null
    private var previousItemPosition: Int = 0
    private var previousThumbnailLayout: ConstraintLayout? = null
    private var previousPlayerView: StyledPlayerView? = null

    private val syncObject = Any()

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
//                    if (this@TrendFragment::errorTextView.isInitialized && errorTextView.isVisible)
//                        trendAdapter.retry()
//                    else
                    trendAdapter.refresh()
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

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putParcelable(LIST_STATE_KEY, recyclerView.layoutManager?.onSaveInstanceState())
//    }

    override fun setup(savedInstanceState: Bundle?) {

//        if (savedInstanceState != null)
//            recyclerViewState = savedInstanceState.getParcelable(LIST_STATE_KEY)
        bindTrendData()
    }

    private fun bindTrendData() {
        trendAdapter =
            TrendAdapter(
                baseColor,
                layoutHelper,
                trendViewModel,
                this
            )

        trendRecyclerView.adapter = trendAdapter.withLoadStateFooter(
            TrendAdapter.TrendLoadStateAdapter(baseColor, layoutHelper) { trendAdapter.retry() }
        )

        repeatViewLifecycle {
            trendViewModel.trendResult.collectLatest {
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

//        trendRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                synchronized(syncObject) {
//
//                    if (previousItemView != null) {
//
//                        val lm = recyclerView.layoutManager as LinearLayoutManager
//                        val firstVisiblePosition = lm.findFirstVisibleItemPosition()
//                        val lastVisiblePosition = lm.findLastVisibleItemPosition()
//
//                        if (previousItemPosition in firstVisiblePosition..lastVisiblePosition) {
//                            playerActionOnThumbnailVisibleInBounds(false) {
//                                if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PAUSED)
//                                    mediaController.transportControls.play()
//                            }
//                        } else {
//                            playerActionOnThumbnailVisibleInBounds(true) {
//                                if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING)
//                                    mediaController.transportControls.pause()
//                            }
//                        }
//                    }
//                }
//            }
//        })

        trendRecyclerView.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val lm = trendRecyclerView.layoutManager as LinearLayoutManager
                if (previousItemView != null &&
                    previousItemView == view &&
                    previousItemPosition in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
                ) {
                    if (previousPlayerView?.isVisible == false) {
                        previousThumbnailLayout?.visibility = View.GONE
                        previousPlayerView?.visibility = View.VISIBLE
                        if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PAUSED)
                            mediaController.transportControls.play()
                    }
                }
//                if (this@TrendFragment::exoPlayer.isInitialized) {
//                    val lm = trendRecyclerView.layoutManager as LinearLayoutManager
//                    val child = lm.getChildAt(1) ?: return
//                    val holder = child.tag as TrendViewHolder
//                    previousItemView = holder.itemView
//                    previousThumbnailLayout = holder.thumbnailLayout
//                    previousPlayerView = holder.playerView
//
//                    previousThumbnailLayout?.visibility = View.GONE
//                    previousPlayerView?.visibility = View.VISIBLE
//                }
                Log.i(TAG, "onChildViewAttachedToWindow: $view")
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (previousItemView != null && previousItemView == view) {
                    if (previousPlayerView?.isVisible == true) {
                        previousThumbnailLayout?.visibility = View.VISIBLE
                        previousPlayerView?.visibility = View.GONE
                        if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING)
                            mediaController.transportControls.pause()
                    }
                }
                Log.i(TAG, "onChildViewDetachedFromWindow: ")
            }

        })
    }

    private fun playerActionOnThumbnailVisibleInBounds(
        isThumbVisible: Boolean,
        onDone: () -> Unit
    ) = launchScope {
        delay(200)
        previousThumbnailLayout?.visibility = if (isThumbVisible) View.VISIBLE else View.GONE
        previousPlayerView?.visibility = if (isThumbVisible) View.GONE else View.VISIBLE
        onDone()
    }

    override fun onPlayVideo(
        itemView: View,
        playerView: StyledPlayerView,
        thumbnailLayout: ConstraintLayout,
        uiState: TrendTrailerUIState
    ) {
        if (!this::exoPlayer.isInitialized)
            setupExoPlayer()

        previousThumbnailLayout?.run {
            visibility = View.VISIBLE
        }
        previousPlayerView?.run {
            player = null
            visibility = View.GONE
        }
        playerView.visibility = View.VISIBLE
        thumbnailLayout.visibility = View.GONE

        with(exoPlayer) {
            clearMediaItems()
            setMediaItem(MediaItem.fromUri(uiState.trailerUrl))
            playerView.player = this
            prepare()
            playWhenReady = true
        }
        setupSessionAndController()

        previousPlayerView = playerView
        previousThumbnailLayout = thumbnailLayout
        previousItemView = itemView
        previousItemPosition = trendRecyclerView.getChildAdapterPosition(previousItemView!!)
    }

    private fun setupExoPlayer() {
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributesCompat.Builder()
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .build()
        exoPlayer =
            ExoAudioFocusHelper(
                ExoPlayer.Builder(requireContext()).build(),
                audioManager,
                audioAttributes
            ).apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {}
                            Player.STATE_BUFFERING -> {}
                            Player.STATE_ENDED -> {}
                            Player.STATE_IDLE -> {}
                        }
                    }
                })
            }
    }

    private fun setupSessionAndController() {
        if (this::mediaSession.isInitialized ||
            this::mediaSessionConnector.isInitialized ||
            this::mediaController.isInitialized
        )
            return

        mediaSession =
            MediaSessionCompat(requireContext(), TAG).apply { setMediaButtonReceiver(null) }
        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
        }

        mediaController =
            MediaControllerCompat(context, mediaSession.sessionToken).also { mediaController ->
                MediaControllerCompat.setMediaController(requireActivity(), mediaController)
            }
    }

    override fun onBindViewHolderToWindow(holder: TrendViewHolder, position: Int) {
        if (previousItemView != null && previousItemPosition == position) {
            previousItemView = holder.itemView
            previousThumbnailLayout = holder.thumbnailLayout
            previousPlayerView = holder.playerView
            previousPlayerView!!.player = exoPlayer

            previousThumbnailLayout?.visibility = View.GONE
            previousPlayerView?.visibility = View.VISIBLE

            exoPlayer.play()
        }
        Log.i(TAG, "onPauseVideo: ${holder.itemView}")
    }

    override fun onStart() {
        super.onStart()
//        if (recyclerViewState != null)
//            recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)

        if (this::exoPlayer.isInitialized) {
//            exoPlayer.prepare()
//            exoPlayer.seekTo(videoPosition)
            if (this::trendRecyclerView.isInitialized) {
                val lm = trendRecyclerView.layoutManager as LinearLayoutManager
                if (previousItemPosition in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition())
                    exoPlayer.play()
            }
            mediaSessionConnector.setPlayer(exoPlayer)
            mediaSession.isActive = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (this::exoPlayer.isInitialized) {
//            with(exoPlayer) {
//                videoPosition = currentPosition
//                stop()
//            }
            if (exoPlayer.isPlaying)
                exoPlayer.pause()
            mediaSessionConnector.setPlayer(null)
            mediaSession.isActive = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        mediaSession.release()
    }
}
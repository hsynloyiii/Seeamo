package com.example.seeamo.trend.ui

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.media.AudioAttributesCompat
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.seeamo.R
import com.example.seeamo.data.model.TrendTrailerUIState
import com.example.seeamo.core.utilize.base.BaseFragment
import com.example.seeamo.core.utilize.extensions.*
import com.example.seeamo.core.utilize.helper.ExoAudioFocusHelper
import com.example.seeamo.core.utilize.helper.LayoutHelper
import com.example.seeamo.data.model.UIState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

@AndroidEntryPoint
class TrendFragment : BaseFragment(false), PlayerHolderEventListener {

    companion object {
        const val TAG = "TrendFragment"
    }

    private lateinit var mainLayout: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var trendRecyclerView: RecyclerView
    private lateinit var errorTextView: TextView

    private val trendViewModel: TrendViewModel by viewModels()
    private lateinit var trendAdapter: TrendAdapter
    private var savedTrendTrailerUIState: TrendTrailerUIState? = null

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var audioManager: AudioManager

    private var previousVideoVolume: Float? = null

    private var previousItemView: View? = null

    private var previousThumbnailLayout: ConstraintLayout? = null
    private var previousPlayerView: StyledPlayerView? = null

    private var currentPlayPauseButton: MaterialButton? = null

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
                    if (errorTextView.isVisible)
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
        Log.i(TAG, "setup: ")

        repeatViewLifecycle(activeState = Lifecycle.State.STARTED) {
            trendViewModel.getSavedTrendTrailerUIState.collect {
                savedTrendTrailerUIState = it
            }
        }

        bindTrendData()
    }

    private fun bindTrendData() {
        trendAdapter =
            TrendAdapter(
                baseColor,
                layoutHelper,
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

        trendRecyclerView.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val lm = trendRecyclerView.layoutManager as LinearLayoutManager
                if (previousItemView != null &&
                    previousItemView == view &&
                    trendViewModel.lastPlayedItemListPosition in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
                ) {
                    previousThumbnailLayout?.visibility = View.GONE
                    previousPlayerView?.visibility = View.VISIBLE
                    (previousItemView as FrameLayout).layoutTransition = LayoutTransition()
                    if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                        mediaController.transportControls.play()
                        setPlayPauseButtonIcon(true)
                    }
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (previousItemView != null && previousItemView == view) {
                    (previousItemView as FrameLayout).layoutTransition = null
                    previousThumbnailLayout?.visibility = View.VISIBLE
                    previousPlayerView?.visibility = View.GONE
                    if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                        mediaController.transportControls.pause()
                        setPlayPauseButtonIcon(false)
                    }
                }
            }

        })
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
                        currentPlayPauseButton?.showProgress(
                            showProgress = playbackState == Player.STATE_BUFFERING,
                            progressSize = CircularProgressDrawable.LARGE,
                            progressColor = baseColor.white,
                            initialIcon =
                            if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PAUSED)
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.animated_play_to_pause
                                )
                            else
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.animated_pause_to_play
                                )
                        )
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

        while (activity == null)
            return

        mediaController =
            MediaControllerCompat(context, mediaSession.sessionToken).also { mediaController ->
                MediaControllerCompat.setMediaController(requireActivity(), mediaController)
            }
    }

    override fun startPlayerButtonOnClick(
        trendResultId: Int,
        button: MaterialButton,
        itemView: View,
        playerView: StyledPlayerView,
        thumbnailLayout: ConstraintLayout
    ) {
        repeatViewLifecycle {
            trendViewModel.getTrendTrailer(trendResultId)
                .collect { trendTrailerUIState ->
                    button.showProgress(
                        showProgress = trendTrailerUIState.uiState == UIState.LOADING,
                        initialIcon = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.animated_play_to_pause
                        )
                    )
                    when (trendTrailerUIState.uiState) {
                        UIState.LOADING -> {}
                        UIState.SUCCEED -> {
                            trendViewModel.lastPlayedItemListPosition =
                                trendRecyclerView.getChildAdapterPosition(itemView)

                            if (!this@TrendFragment::exoPlayer.isInitialized)
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
                                setMediaItem(MediaItem.fromUri(trendTrailerUIState.trailerUrl))
                                playerView.player = this
                                prepare()
                                playWhenReady = true
                            }
                            setupSessionAndController()

                            previousPlayerView = playerView
                            previousThumbnailLayout = thumbnailLayout
                            previousItemView = itemView
                            Log.i(TAG, "onPlayVideo: ${trendViewModel.lastPlayedItemListPosition}, $previousItemView")
                        }
                        UIState.FAILED -> {
                            context?.toast(trendTrailerUIState.failure_message)
                        }
                        else -> return@collect
                    }
                }
        }
    }

    override fun onBindViewHolderToWindow(holder: TrendViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolderToWindow: ${holder.itemView}")
        if (trendViewModel.lastPlayedItemListPosition?.equals(position) == true) {
            if (!this::exoPlayer.isInitialized && savedTrendTrailerUIState != null) {
                setupExoPlayer()
                with(exoPlayer) {
                    clearMediaItems()
                    setMediaItem(MediaItem.fromUri(savedTrendTrailerUIState!!.trailerUrl))
                    seekTo(trendViewModel.lastItemVideoPosition ?: 0)
                    prepare()
                    playWhenReady = true
                }
            }

            previousItemView = holder.itemView
            previousThumbnailLayout = holder.thumbnailLayout
            previousPlayerView = holder.playerView

            previousPlayerView!!.player = exoPlayer
            setupSessionAndController()

            previousThumbnailLayout?.visibility = View.GONE
            previousPlayerView?.visibility = View.VISIBLE

            exoPlayer.play()
            setPlayPauseButtonIcon(true)
        }
    }

    override fun playPauseButtonOnClick(button: MaterialButton) {
        val pauseToPlayAnimatedIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.animated_pause_to_play)
        val playToPauseAnimatedIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.animated_play_to_pause)

        when (mediaController.playbackState.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                mediaController.transportControls.pause()
                with(button) {
                    icon = pauseToPlayAnimatedIcon
                    (icon as AnimatedVectorDrawable).start()
                }
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                mediaController.transportControls.play()
                with(button) {
                    icon = playToPauseAnimatedIcon
                    (icon as AnimatedVectorDrawable).start()
                }
            }
            else -> return
        }
    }

    override fun soundToggleButtonOnClick(button: MaterialButton) {
        val currentVideoVolume = exoPlayer.volume
        if (previousVideoVolume == null)
            previousVideoVolume = currentVideoVolume

        if (currentVideoVolume == 0f) {
            exoPlayer.volume = previousVideoVolume as Float
            button.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_sound_24)
        } else {
            exoPlayer.volume = 0f
            button.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_mute_24)
        }
    }

    override fun remainingPlayerTimeListener(view: TextView) {
        currentPlayPauseButton = (view.parent as ConstraintLayout).getChildAt(0) as MaterialButton
        repeatViewLifecycle {
            while (isActive) {
                if (exoPlayer.currentPosition > 100 && exoPlayer.isPlaying) {
                    val currentPlayerPosition = exoPlayer.currentPosition
                    val fullDuration = exoPlayer.duration
                    val remainingMillis = fullDuration - currentPlayerPosition

                    view.text = remainingMillis.convertMillisToCountDownFormat(false)
                }
                delay(1000)
            }
        }
    }

    private fun setPlayPauseButtonIcon(isPlaying: Boolean) {
        currentPlayPauseButton?.icon =
            if (isPlaying)
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.animated_pause_to_play
                )
            else
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.animated_play_to_pause
                )
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: ")
        if (this::exoPlayer.isInitialized) {
            mediaSessionConnector.setPlayer(exoPlayer)
            mediaSession.isActive = true
            if (this::trendRecyclerView.isInitialized) {
                val lm = trendRecyclerView.layoutManager as LinearLayoutManager
                if (trendViewModel.lastPlayedItemListPosition in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()) {
                    exoPlayer.play()
                    setPlayPauseButtonIcon(true)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ${trendViewModel.lastPlayedItemListPosition ?: -1}")
//        trendViewModel.lastPlayedItemListPosition = lastPlayedItemListPosition
        if (this::exoPlayer.isInitialized)
            trendViewModel.lastItemVideoPosition = exoPlayer.currentPosition
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
        if (this::exoPlayer.isInitialized) {
            if (exoPlayer.isPlaying || exoPlayer.playbackState == Player.STATE_BUFFERING)
                exoPlayer.pause()
            mediaSessionConnector.setPlayer(null)
            mediaSession.isActive = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::exoPlayer.isInitialized) {
            exoPlayer.release()
            mediaSession.release()
        }
    }
}
package com.example.seeamo.trend.ui

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.seeamo.R
import com.example.seeamo.data.model.TrendResult
import com.example.seeamo.core.utilize.base.BaseColor
import com.example.seeamo.core.utilize.extensions.*
import com.example.seeamo.core.utilize.helper.LayoutHelper
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.button.MaterialButton

class TrendAdapter(
    private val context: Context,
    private val baseColor: BaseColor,
    private val layoutHelper: LayoutHelper,
    private val trendViewModel: TrendViewModel,
    private val onPlayerExpandButtonClick: (StyledPlayerView, TrendResult) -> Unit
) : PagingDataAdapter<TrendResult, TrendViewHolder>(diffCallback = TrendResult.DIFF_CALLBACK),
    Player.Listener {

    var lastPlayedViewHolder: TrendViewHolder? = null
    private var playerPlayPauseButton: MaterialButton? = null

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        val trendResult = getItem(position) ?: return
        if (position == trendViewModel.lastPlayedItemListPosition) {
            trendViewModel.setupPlayer(
                context = context,
                listener = this@TrendAdapter,
                shouldStartPlayer = true,
                mediaUrl = trendViewModel.savedTrendTrailerUIState?.trailerUrl
            )
            playerPlayPauseButton =
                holder.controlsLayout.getChildAt(0) as MaterialButton

            trendViewModel.removePlayerListener(listener = this)
            trendViewModel.addPlayerListener(listener = this)

            lastPlayedViewHolder = holder

            holder.thumbnailLayout.visibility = View.GONE
            holder.playerView.visibility = View.VISIBLE

            holder.playerView.player = trendViewModel.player
            trendViewModel.playPlayer()

            startUpdatingRemainingPlayerTimeJob(holder = holder)
        }

        holder.bind(
            trendResult,
            startPlayer = { trendTrailerUIState ->
                playerPlayPauseButton = holder.controlsLayout.getChildAt(0) as MaterialButton

                trendViewModel.setupPlayer(context = context, listener = this)

                lastPlayedViewHolder?.run {
                    thumbnailLayout.visibility = View.VISIBLE
                    playerView.apply {
                        player = null
                        visibility = View.GONE
                    }
                    removeWatchAgainButton()
                }

                holder.run {
                    playerView.visibility = View.VISIBLE
                    thumbnailLayout.visibility = View.GONE

                    playerView.player = trendViewModel.player
                    removeWatchAgainButton()

                    trendViewModel.startPlayer(trendTrailerUIState.trailerUrl)

                    lastPlayedViewHolder = this
                }
            },
            onPlayPauseButtonClick = {
                val pauseToPlayAnimatedIcon =
                    ContextCompat.getDrawable(context, R.drawable.animated_pause_to_play)
                val playToPauseAnimatedIcon =
                    ContextCompat.getDrawable(context, R.drawable.animated_play_to_pause)

                val exoPlayer = trendViewModel.player ?: return@bind

                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    with(it) {
                        icon = pauseToPlayAnimatedIcon
                        (icon as AnimatedVectorDrawable).start()
                    }
                } else {
                    exoPlayer.play()
                    with(it) {
                        icon = playToPauseAnimatedIcon
                        (icon as AnimatedVectorDrawable).start()
                    }
                }
            },
            onMuteButtonClick = {
                if (trendViewModel.isPlayerMuted) {
                    trendViewModel.unMutePlayer()
                    it.icon = ContextCompat.getDrawable(context, R.drawable.ic_round_sound_24)
                } else {
                    trendViewModel.mutePlayer()
                    it.icon = ContextCompat.getDrawable(context, R.drawable.ic_round_mute_24)
                }
            },
            onPlayerExpandButtonClick = onPlayerExpandButtonClick
        )
    }

    private fun startUpdatingRemainingPlayerTimeJob(holder: TrendViewHolder) {
        trendViewModel.startUpdatingCurrentPositionJob { currentPosition, duration ->
            val remainingMillis = duration - currentPosition

            val remainingPlayerTime = holder.controlsLayout.getChildAt(2) as TextView
            remainingPlayerTime.text =
                remainingMillis.convertMillisToCountDownFormat(false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val context = parent.context
        val constraintLayout = FrameLayout(context).apply {
            layoutParams =
                layoutHelper.createRecycler(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT)
            setPadding(8.toDp(context), 4.toDp(context), 8.toDp(context), 4.toDp(context))
//            setBackgroundResource(context.toThemeResourceId(android.R.attr.selectableItemBackground))
            layoutTransition = LayoutTransition()
        }

        return TrendViewHolder(
            constraintLayout,
            layoutHelper,
            baseColor,
            trendViewModel
        )
    }

    private fun setPlayPauseButtonIcon(isPlaying: Boolean) {
        playerPlayPauseButton?.icon =
            if (isPlaying)
                ContextCompat.getDrawable(
                    context,
                    R.drawable.animated_pause_to_play
                )
            else
                ContextCompat.getDrawable(
                    context,
                    R.drawable.animated_play_to_pause
                )
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        playerPlayPauseButton?.showProgress(
            showProgress = playbackState == Player.STATE_BUFFERING,
            progressSize = CircularProgressDrawable.LARGE,
            progressColor = baseColor.white,
            initialIcon =
            if (trendViewModel.isPlayerPaused())
                ContextCompat.getDrawable(
                    context,
                    R.drawable.animated_play_to_pause
                )
            else
                ContextCompat.getDrawable(
                    context,
                    R.drawable.animated_pause_to_play
                )
        )
        if (playbackState == Player.STATE_ENDED)
            lastPlayedViewHolder?.addWatchAgainButton()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (!trendViewModel.isPlayerBuffering() && !trendViewModel.isPlayerEnded())
            setPlayPauseButtonIcon(isPlaying = isPlaying)
        if (!isPlaying) {
            trendViewModel.stopUpdatingCurrentPositionJob()
        } else {
            val holder = lastPlayedViewHolder ?: return
            startUpdatingRemainingPlayerTimeJob(holder)
        }
    }


    // Loading State Adapter
    class TrendLoadStateAdapter(
        private val baseColor: BaseColor,
        private val layoutHelper: LayoutHelper,
        private val retry: () -> Unit
    ) :
        LoadStateAdapter<TrendLoadStateAdapter.ViewHolder>() {

        inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent) {
            private val context: Context
            private val mainLayout: RelativeLayout
            private val progressBar: ProgressBar
            private val errorTextView: TextView

            init {
                context = parent.context
                mainLayout = (parent as RelativeLayout)

                progressBar = ProgressBar(context).apply {
                    indeterminateTintList = baseColor.baseColorStateList(baseColor.onBackground)
                    isFocusable = true
                    isClickable = true

                    mainLayout.addView(
                        this,
                        layoutHelper.createRelative(
                            12.toDp(context),
                            12.toDp(context),
                            alignParent = RelativeLayout.CENTER_IN_PARENT
                        )
                    )
                }

                errorTextView = TextView(context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(baseColor.onBackground)
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true

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

            fun bindState(loadState: LoadState) {
                progressBar.isVisible = loadState is LoadState.Loading

                errorTextView.isVisible = loadState is LoadState.Error
                if (loadState is LoadState.Error)
                    errorTextView.apply {
                        text = loadState.error.message

                        setOnClickListener { retry.invoke() }
                    }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
            holder.bindState(loadState)
        }

        override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
            val context = parent.context
            val relativeLayout = RelativeLayout(context).apply {
                layoutParams = layoutHelper.createRecycler(
                    LayoutHelper.MATCH_PARENT,
                    LayoutHelper.WRAP_CONTENT,
                    margin = 8.toDp(context)
                )
                isClickable = true
                isFocusable = true
                setBackgroundResource(context.toThemeResourceId(android.R.attr.selectableItemBackground))
            }

            return ViewHolder(relativeLayout)
        }
    }
}


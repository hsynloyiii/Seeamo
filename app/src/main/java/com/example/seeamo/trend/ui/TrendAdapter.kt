package com.example.seeamo.trend.ui

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrendAdapter(
    private val context: Context,
    private val baseColor: BaseColor,
    private val layoutHelper: LayoutHelper,
    private val trendViewModel: TrendViewModel
//    private val playerHolderEventListener: PlayerHolderEventListener
) : PagingDataAdapter<TrendResult, TrendViewHolder>(diffCallback = TrendResult.DIFF_CALLBACK),
    Player.Listener {

    var lastPlayedItemView: View? = null
    var lastPlayedThumbnailLayout: ConstraintLayout? = null
    var lastPlayedPlayerView: StyledPlayerView? = null
    private var defaultVideoVolume: Float? = null
    private var playerPlayPauseButton: MaterialButton? = null

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        val trendResult = getItem(position) ?: return
        if (position == trendViewModel.lastPlayedItemListPosition) {
            Log.i(TrendFragment.TAG, "onBindViewHolder: ")
            trendViewModel.setupPlayer(
                context = context,
                listener = this@TrendAdapter,
                shouldStartPlayer = true,
                mediaUrl = trendViewModel.savedTrendTrailerUIState?.trailerUrl
            )
            playerPlayPauseButton =
                holder.controlsLayout.getChildAt(0) as MaterialButton

            trendViewModel.addPlayerListener(listener = this)

            lastPlayedItemView = holder.itemView
            lastPlayedThumbnailLayout = holder.thumbnailLayout
            lastPlayedPlayerView = holder.playerView
            Log.i(TrendFragment.TAG, "onBindViewHolder: $playerPlayPauseButton")

//            setupSessionAndController()

            holder.thumbnailLayout.visibility = View.GONE
            holder.playerView.visibility = View.VISIBLE

            holder.playerView.player = trendViewModel.exoPlayer
            trendViewModel.playPlayer()

            launchRemainingPlayerTimeScope(holder = holder)

            setPlayPauseButtonIcon(isPlaying = true)
        }

        holder.bind(
            trendResult,
            startPlayer = { trendTrailerUIState ->
                playerPlayPauseButton = holder.controlsLayout.getChildAt(0) as MaterialButton

                trendViewModel.setupPlayer(context = context, listener = this)

                Log.i(TrendFragment.TAG, "onBindViewHolder: $playerPlayPauseButton")

                lastPlayedThumbnailLayout?.visibility = View.VISIBLE
                lastPlayedPlayerView?.run {
                    player = null
                    visibility = View.GONE
                }
                holder.playerView.visibility = View.VISIBLE
                holder.thumbnailLayout.visibility = View.GONE

                holder.playerView.player = trendViewModel.exoPlayer
                trendViewModel.startPlayer(trendTrailerUIState.trailerUrl)

                launchRemainingPlayerTimeScope(holder = holder)
//                setupSessionAndController()

                lastPlayedPlayerView = holder.playerView
                lastPlayedThumbnailLayout = holder.thumbnailLayout
                lastPlayedItemView = holder.itemView
            },
            onPlayPauseButtonClick = {
                val pauseToPlayAnimatedIcon =
                    ContextCompat.getDrawable(context, R.drawable.animated_pause_to_play)
                val playToPauseAnimatedIcon =
                    ContextCompat.getDrawable(context, R.drawable.animated_play_to_pause)

                val exoPlayer = trendViewModel.exoPlayer ?: return@bind

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
                val exoPlayer = trendViewModel.exoPlayer ?: return@bind
                val currentVideoVolume = exoPlayer.volume
                if (defaultVideoVolume == null)
                    defaultVideoVolume = currentVideoVolume

                if (currentVideoVolume == 0f) {
                    trendViewModel.exoPlayer!!.volume = defaultVideoVolume as Float
                    it.icon = ContextCompat.getDrawable(context, R.drawable.ic_round_sound_24)
                } else {
                    trendViewModel.exoPlayer!!.volume = 0f
                    it.icon = ContextCompat.getDrawable(context, R.drawable.ic_round_mute_24)
                }
            }
        )
    }

    private fun launchRemainingPlayerTimeScope(holder: TrendViewHolder) {
        trendViewModel.viewModelScope.launch {
            while (isActive) {
                if (trendViewModel.exoPlayer!!.currentPosition > 100 && trendViewModel.exoPlayer!!.isPlaying) {
                    val currentPlayerPosition = trendViewModel.exoPlayer!!.currentPosition
                    val fullDuration = trendViewModel.exoPlayer!!.duration
                    val remainingMillis = fullDuration - currentPlayerPosition

                    val remainingPlayerTime = holder.controlsLayout.getChildAt(2) as TextView
                    remainingPlayerTime.text =
                        remainingMillis.convertMillisToCountDownFormat(false)
                }
                delay(1000)
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING)
            Log.i(TrendFragment.TAG, "onPlaybackStateChanged: $playerPlayPauseButton")
        playerPlayPauseButton!!.showProgress(
            showProgress = playbackState == Player.STATE_BUFFERING,
            progressSize = CircularProgressDrawable.LARGE,
            progressColor = baseColor.white,
            initialIcon =
            if (trendViewModel.exoPlayer?.isPlaying == false)
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

    fun setPlayPauseButtonIcon(isPlaying: Boolean) {
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


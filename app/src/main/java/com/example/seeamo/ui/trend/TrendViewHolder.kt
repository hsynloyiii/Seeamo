package com.example.seeamo.ui.trend

import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.setPadding
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.example.seeamo.R
import com.example.seeamo.data.model.TrendResult
import com.example.seeamo.data.model.TrendTrailerUIState
import com.example.seeamo.data.model.UIState
import com.example.seeamo.utilize.base.BaseColor
import com.example.seeamo.utilize.extensions.*
import com.example.seeamo.utilize.helper.DrawableHelper
import com.example.seeamo.utilize.helper.ImageHelper
import com.example.seeamo.utilize.helper.LayoutHelper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

interface PlayerHolderEventListener {
    fun onPlay(
        playerView: StyledPlayerView,
        thumbnailLayout: ConstraintLayout,
        uiState: TrendTrailerUIState
    )

    fun onPause()
}

class TrendViewHolder(
    parent: View,
    layoutHelper: LayoutHelper,
    baseColor: BaseColor,
    private val trendViewModel: TrendViewModel,
    private val playerHolderEventListener: PlayerHolderEventListener
) : RecyclerView.ViewHolder(parent) {
    private val context = parent.context

    private val mainLayout: FrameLayout = parent as FrameLayout
    private val thumbnailLayout: ConstraintLayout = ConstraintLayout(context).apply {
        mainLayout.addView(
            this,
            LayoutHelper.MATCH_PARENT,
            LayoutHelper.WRAP_CONTENT
        )
        minHeight = 200.toDp(context)
        maxHeight = 300.toDp(context)
    }

    private val imageView: ImageView by lazy {
        ImageView(context).apply {
            id = R.id.trend_fragment_recycler_view_poster
            thumbnailLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT
            )
        }
    }
    private val titleTextView: TextView by lazy {
        TextView(context).apply {
            id = R.id.trend_fragment_recycler_view_title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(baseColor.white)
            background = DrawableHelper.Round(
                context,
                4.toDp(context).toFloat(),
                baseColor.onBackground.withAlpha(0.32),
                0
            )
            setPadding(8.toDp(context))
            gravity = Gravity.START
            thumbnailLayout.addView(
                this,
                layoutHelper.createConstraints(
                    0, LayoutHelper.WRAP_CONTENT,
                    startToStart = imageView.id,
                    endToEnd = imageView.id,
                    bottomToBottom = imageView.id
                )
            )
        }
    }

    private val playPauseButton: MaterialButton by lazy {
        MaterialButton(context).apply {
            thumbnailLayout.addView(
                this,
                layoutHelper.createConstraints(
                    LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                    bottomToBottom = 0,
                    endToEnd = 0,
                    endMargin = 8.toDp(context),
                    bottomMargin = 16.toDp(context)
                )
            )

            iconButton(
                icon = ContextCompat.getDrawable(context, R.drawable.animated_play_to_pause),
                bcColor = baseColor.baseColorStateList(baseColor.primaryContainer),
                iconTint = baseColor.baseColorStateList(baseColor.onBackground),
                isCircular = true
            )

//                val pauseToPlayAnimatedIcon =
//                    ContextCompat.getDrawable(context, R.drawable.animated_pause_to_play)
//                val playToPauseAnimatedIcon =
//                    ContextCompat.getDrawable(context, R.drawable.animated_play_to_pause)
//
//                setOnClickListener {
//                    if (isPlaying) {
//                        icon = pauseToPlayAnimatedIcon
//                        (icon as AnimatedVectorDrawable).start()
//                    } else {
//                        icon = playToPauseAnimatedIcon
//                        (icon as AnimatedVectorDrawable).start()
//                    }
//
//                    isPlaying = !isPlaying
//                }
        }
    }

    private val playerView: StyledPlayerView by lazy {
        StyledPlayerView(context).apply {
            visibility = View.GONE
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

            mainLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                200.toDp(context),
            )
        }
    }

    fun bind(trendResult: TrendResult) {
        ImageHelper.loadUriTo(
            imageView,
            trendResult.fullBackdropPath.toUri(),
            cornerSize = 8.toDp(context),
            transformation = FitCenter()
        )
        titleTextView.text = trendResult.original_title

        mainLayout.setOnClickListener {
            Log.i(TrendFragment.TAG, "main = $mainLayout || ${trendResult.id}")
        }

        playPauseOnClick(trendResult)
    }

    private fun playPauseOnClick(trendResult: TrendResult) = with(playPauseButton) {
        setOnClickListener {
            trendViewModel.viewModelScope.launch {
                trendViewModel.getTrendTrailer(context, trendResult.id)
                    .collect { trendTrailerUIState ->
                        showProgress(
                            showProgress = trendTrailerUIState.uiState == UIState.LOADING,
                            initialIcon = ContextCompat.getDrawable(
                                context,
                                R.drawable.animated_play_to_pause
                            )
                        )
                        when (trendTrailerUIState.uiState) {
                            UIState.LOADING -> {}
                            UIState.SUCCEED -> {
                                context?.toast(trendTrailerUIState.trailerUrl)
                                playTrailer(trendTrailerUIState)
                            }
                            UIState.FAILED -> {
                                context?.toast(trendTrailerUIState.failure_message)
                            }
                            else -> return@collect
                        }
                    }
            }
        }
    }

    //    private val mediaItems = mutableListOf<MediaItem>()
    private fun playTrailer(uiState: TrendTrailerUIState) {
        playerHolderEventListener.onPlay(playerView, thumbnailLayout, uiState)
    }
}
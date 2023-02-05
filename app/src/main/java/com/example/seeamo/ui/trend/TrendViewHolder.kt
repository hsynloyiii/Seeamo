package com.example.seeamo.ui.trend

import android.annotation.SuppressLint
import android.graphics.Outline
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.example.seeamo.R
import com.example.seeamo.data.model.TrendResult
import com.example.seeamo.data.model.TrendTrailerUIState
import com.example.seeamo.data.model.UIState
import com.example.seeamo.utilize.base.BaseAnimation
import com.example.seeamo.utilize.base.BaseColor
import com.example.seeamo.utilize.extensions.*
import com.example.seeamo.utilize.helper.DrawableHelper
import com.example.seeamo.utilize.helper.ImageHelper
import com.example.seeamo.utilize.helper.LayoutHelper
import com.example.seeamo.utilize.helper.ViewHelper
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Local

interface PlayerHolderEventListener {
    fun onPlayVideo(
        itemView: View,
        playerView: StyledPlayerView,
        thumbnailLayout: ConstraintLayout,
        uiState: TrendTrailerUIState
    )

    fun onBindViewHolderToWindow(holder: TrendViewHolder, position: Int)

    fun playPauseButtonOnClick(button: MaterialButton)
    fun soundToggleButtonOnClick(button: MaterialButton)

    fun remainingPlayerTimeListener(view: TextView)
}

@SuppressLint("RtlHardcoded")
class TrendViewHolder(
    parent: View,
    layoutHelper: LayoutHelper,
    baseColor: BaseColor,
    private val trendViewModel: TrendViewModel,
    private val playerHolderEventListener: PlayerHolderEventListener
) : RecyclerView.ViewHolder(parent) {
    private val context = parent.context

    private val mainLayout: FrameLayout = parent as FrameLayout
    val thumbnailLayout: ConstraintLayout = ConstraintLayout(context).apply {
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
            gravity = Gravity.LEFT
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

    private val startPlayerButton: MaterialButton by lazy {
        MaterialButton(context).apply {
            iconButton(
                icon = ContextCompat.getDrawable(context, R.drawable.animated_play_to_pause),
                bcColor = baseColor.baseColorStateList(baseColor.primaryContainer),
                iconTint = baseColor.baseColorStateList(baseColor.onBackground),
                isCircular = true
            )

            thumbnailLayout.addView(
                this,
                layoutHelper.createConstraints(
                    ViewHelper.Button.ICON_BUTTON_WIDTH,
                    ViewHelper.Button.ICON_BUTTON_HEIGHT,
                    bottomToBottom = 0,
                    endToEnd = 0,
                    endMargin = 8.toDp(context),
                    bottomMargin = 16.toDp(context)
                )
            )
        }
    }

    val playerView: StyledPlayerView by lazy {
        StyledPlayerView(context).apply {
            visibility = View.GONE
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

            useController = false

            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, 8.toDp(context).toFloat())
                }
            }
            clipToOutline = true

            isClickable = true
            isFocusable = true

            val hideControlsLayoutRunnable = Runnable {
                hideControlsLayoutAnimation()
            }

            setOnClickListener {
                if (controlsLayout.isVisible) {
                    playerView.removeCallbacks(hideControlsLayoutRunnable)
                    hideControlsLayoutAnimation()
                } else controlsLayout.apply {
                    alpha = 0f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(1f)
                        .setDuration(BaseAnimation.DURATION_MEDIUM_1)
                        .withEndAction {
                            playerView.postDelayed(hideControlsLayoutRunnable, 5000)
                        }
                }
            }

            mainLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                200.toDp(context),
            )


        }
    }

    private val controlsLayout: ConstraintLayout by lazy {
        ConstraintLayout(context).apply {
            alpha = 0f
            visibility = View.GONE
            setBackgroundColor(baseColor.black.withAlpha(0.52))

            playerView.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
            )

            // PlayPause button
            val playPauseButton = MaterialButton(context).apply {
                iconButton(
                    icon = ContextCompat.getDrawable(context, R.drawable.animated_pause_to_play),
                    bcColor = baseColor.baseColorStateList(baseColor.transparent),
                    iconTint = baseColor.baseColorStateList(baseColor.white),
                    iconSize = 42.toDp(context),
                    colorRipple = baseColor.baseRippleColorStateList(baseColor.white),
                    isCircular = true
                )

                setOnClickListener {
                    playerHolderEventListener.playPauseButtonOnClick(it as MaterialButton)
                }
            }
            addView(
                playPauseButton,
                layoutHelper.createConstraints(
                    64,
                    64,
                    startToStart = 0,
                    endToEnd = 0,
                    bottomToBottom = 0,
                    topToTop = 0
                )
            )

            // Toggle mute button
            val muteButton = MaterialButton(context).apply {
                iconButton(
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_round_sound_24),
                    bcColor = baseColor.baseColorStateList(baseColor.transparent),
                    iconTint = baseColor.baseColorStateList(baseColor.gray),
                    colorRipple = baseColor.withoutRippleColor(),
                    isCircular = true
                )

                setOnClickListener {
                    playerHolderEventListener.soundToggleButtonOnClick(it as MaterialButton)
                }
            }
            addView(
                muteButton,
                layoutHelper.createConstraints(
                    ViewHelper.Button.ICON_BUTTON_WIDTH,
                    ViewHelper.Button.ICON_BUTTON_HEIGHT,
                    endToEnd = 0,
                    bottomToBottom = 0
                )
            )

            // Remaining video time text
            val remainingTimeTextView = TextView(context).apply {
                setTextColor(baseColor.gray)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.CENTER

                val millis = 0L
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                text = String.format("%02d:%02d", minutes, seconds)
            }
            addView(
                remainingTimeTextView,
                layoutHelper.createConstraints(
                    LayoutHelper.WRAP_CONTENT,
                    LayoutHelper.WRAP_CONTENT,
                    startToStart = 0,
                    bottomToBottom = 0,
                    startMargin = 12.toDp(context),
                    bottomMargin = 12.toDp(context)
                )
            )
            playerHolderEventListener.remainingPlayerTimeListener(remainingTimeTextView)
        }
    }

    private fun convertMillisToDate(millis: Long): String {
        val date = Date(millis)
//        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
//        calendar.timeInMillis = millis
        return formatter.format(date)
    }

    private fun hideControlsLayoutAnimation() {
        controlsLayout.animate()
            .alpha(0f)
            .setDuration(BaseAnimation.DURATION_MEDIUM_1)
            .withEndAction {
                controlsLayout.visibility = View.GONE
            }
    }

    fun bind(trendResult: TrendResult) {
        ImageHelper.loadUriTo(
            imageView,
            trendResult.fullBackdropPath.toUri(),
            cornerSize = 8.toDp(context),
            transformation = CenterInside()
        )

        titleTextView.text = trendResult.original_title

        mainLayout.setOnClickListener {
            Log.i(TrendFragment.TAG, "$itemView")
        }

        playPauseOnClick(trendResult)
    }

    private fun playPauseOnClick(trendResult: TrendResult) = with(startPlayerButton) {
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

    private fun playTrailer(uiState: TrendTrailerUIState) {
        playerHolderEventListener.onPlayVideo(itemView, playerView, thumbnailLayout, uiState)
    }
}
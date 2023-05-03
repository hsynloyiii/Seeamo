 package com.example.seeamo.trend.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.seeamo.R
import com.example.seeamo.core.ui.MainActivity
import com.example.seeamo.core.utilize.navigation.NavRoutes
import com.example.seeamo.core.utilize.base.BaseAnimation
import com.example.seeamo.core.utilize.base.BaseFragment
import com.example.seeamo.core.utilize.extensions.*
import com.example.seeamo.core.utilize.helper.LayoutHelper
import com.example.seeamo.core.utilize.helper.ViewHelper
import com.example.seeamo.trend.data.TrendResult
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

 @AndroidEntryPoint
class TrendFragment : BaseFragment(false), Player.Listener {

    companion object {
        const val IS_FRAGMENT_BEING_DESTROYED_KEY = "isFragmentBeingDestroyed"
    }

    private lateinit var mainLayout: FrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var trendRecyclerView: RecyclerView
    private lateinit var errorTextView: TextView
    private lateinit var expandedViewBackground: View
    private lateinit var expandedPlayerView: StyledPlayerView
    private val expandedControlLayout: ConstraintLayout by lazy {
        ConstraintLayout(requireContext()).apply {
            id = R.id.trend_fragment_expanded_control_layout
            alpha = 0f
            visibility = View.GONE
            setBackgroundColor(baseColor.transparent)

            expandedPlayerView.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
            )

            applyPaddingWindowInsets(applyTop = true, applyBottom = true)

            // PlayPause button
            val playPauseButton = MaterialButton(context).apply {
                id = R.id.trend_fragment_expanded_play_pause_button
                iconButton(
                    icon = ContextCompat.getDrawable(context, R.drawable.animated_pause_to_play),
                    bcColor = baseColor.baseColorStateList(baseColor.transparent),
                    iconTint = baseColor.baseColorStateList(baseColor.white),
                    iconSize = 42.toDp(context),
                    colorRipple = baseColor.baseRippleColorStateList(baseColor.white),
                    isCircular = true
                )
            }
            addView(
                playPauseButton,
                layoutHelper.createConstraints(
                    64,
                    64,
                    startToStart = 0,
                    endToEnd = 0,
                    bottomToBottom = 0,
                    topToTop = 0,
                    bottomMargin = layoutHelper.smallMargin2
                )
            )

            // Back button
            val backButton = MaterialButton(context).apply {
                id = R.id.trend_fragment_expanded_play_pause_button
                iconButton(
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_round_arrow_back_24),
                    bcColor = baseColor.baseColorStateList(baseColor.transparent),
                    iconTint = baseColor.baseColorStateList(baseColor.white),
                    colorRipple = baseColor.baseRippleColorStateList(baseColor.white),
                    isCircular = true
                )
            }
            addView(
                backButton,
                layoutHelper.createConstraints(
                    ViewHelper.Button.ICON_BUTTON_WIDTH,
                    ViewHelper.Button.ICON_BUTTON_HEIGHT,
                    topToTop = 0,
                    startToStart = 0,
                    startMargin = layoutHelper.smallMargin1,
                    topMargin = layoutHelper.smallMargin1
                )
            )

            // Title
            val title = TextView(context).apply {
                id = R.id.trend_fragment_expanded_title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, ViewHelper.Text.MEDIUM_TEXT_SIZE_1)
                setTextColor(baseColor.white)

                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END

                gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
            addView(
                title,
                layoutHelper.createConstraints(
                    0, 0,
                    startToEnd = backButton.id,
                    topToTop = backButton.id,
                    bottomToBottom = backButton.id,
                    endToEnd = 0,
                    endMargin = layoutHelper.smallMargin1,
                    startMargin = layoutHelper.smallMargin2
                )
            )

            // Position Text
            val positionTextView = TextView(context).apply {
                id = R.id.trend_fragment_expanded_position_text
                setTextColor(baseColor.white)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                gravity = Gravity.CENTER

                text = resources.getString(
                    R.string.expanded_position_text,
                    "00:00", "00:00"
                )
            }

            // Timer
            val playerTimerSlider = Slider(context).apply {
                id = R.id.trend_fragment_expanded_timer_slider
                trackActiveTintList = baseColor.baseColorStateList(baseColor.white)
                trackInactiveTintList = baseColor.baseColorStateList(baseColor.gray)
//                trackHeight = 2.toDp(context)
//                setBackgroundColor(baseColor.red)

                thumbTintList = baseColor.baseColorStateList(baseColor.white)
                thumbRadius = 7.toDp(context)

                labelBehavior = LabelFormatter.LABEL_GONE
            }
            addView(
                positionTextView,
                layoutHelper.createConstraints(
                    width = LayoutHelper.WRAP_CONTENT,
                    height = 0,
                    endToEnd = 0,
                    bottomToBottom = playerTimerSlider.id,
                    topToTop = playerTimerSlider.id,
                    bottomMargin = layoutHelper.smallMargin1,
                    endMargin = layoutHelper.smallMargin2
                )
            )
            addView(
                playerTimerSlider,
                layoutHelper.createConstraints(
                    width = 0,
                    height = LayoutHelper.WRAP_CONTENT,
                    bottomToBottom = 0,
//                    topToTop = positionTextView.id,
                    startToStart = 0,
                    endToStart = positionTextView.id,
                    bottomMargin = layoutHelper.smallMargin1
                )
            )

            // Go to detail button
            val detailButton = MaterialButton(context).apply {
                id = R.id.trend_fragment_expanded_detail_button
                iconButton(
                    icon = ContextCompat.getDrawable(
                        context,
                        R.drawable.round_subdirectory_arrow_right_24
                    ),
                    bcColor = baseColor.baseColorStateList(baseColor.transparent),
                    iconTint = baseColor.baseColorStateList(baseColor.white),
                    colorRipple = baseColor.baseRippleColorStateList(baseColor.white),
                    isCircular = true
                )
                contentDescription = resources.getString(R.string.detail_button_desc)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    tooltipText = resources.getString(R.string.detail_button_desc)
            }
            addView(
                detailButton,
                layoutHelper.createConstraints(
                    ViewHelper.Button.ICON_BUTTON_WIDTH,
                    ViewHelper.Button.ICON_BUTTON_HEIGHT,
                    topToTop = 0,
                    endToEnd = 0,
                    endMargin = layoutHelper.smallMargin1,
                    topMargin = layoutHelper.smallMargin1
                )
            )
        }
    }

    private val trendViewModel: TrendViewModel by viewModels()
    private var trendAdapter: TrendAdapter? = null

    private val syncObject = Any()

    private var isFragmentBeingDestroyed = false
    private var savedInstanceState: Bundle? = null

    private var expandingPlayerViewAnimator: Animator? = null
    private var startExpandingBounds = RectF()
    private var finalExpandingBounds = RectF()
    private var startExpandingScale = 0f
    private val startExpandingAlpha = 0f
    private val finalExpandingAlpha = 1f

    private var thumbPlayerView: StyledPlayerView? = null
    private var onPlayerExpandButtonClick: (StyledPlayerView, TrendResult) -> Unit =
        { playerView, trendResult ->
            thumbPlayerView = playerView
            zoomPlayerViewFromThumb(trendResult)
        }

    private var isEnableToGesturing = true
    private var expandedPlayerViewGestureDetector: GestureDetectorCompat? = null
    private var expandedPlayerViewScaleGestureDetector: ScaleGestureDetector? = null

    private val hideExpandedControlLayoutRunnable = Runnable {
        hideExpandedControlsLayout()
    }

    private var currentTimerSliderJob: Job? = null
    private var timerSliderAnimator: ObjectAnimator? = null

    private var currentPositionJob: Job? = null

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
        root = FrameLayout(requireContext())

        mainLayout = (root as FrameLayout).apply {
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
                layoutHelper.createFrame(
                    LayoutHelper.MATCH_PARENT,
                    LayoutHelper.WRAP_CONTENT,
                    gravity = Gravity.CENTER,
                    startMargin = 16.toDp(context),
                    endMargin = 16.toDp(context)
                )
            )
        }

        expandedViewBackground = View(requireContext()).apply {
            id = R.id.trend_fragment_expanded_background_view
            alpha = startExpandingAlpha
            setBackgroundColor(baseColor.black)
            visibility = View.GONE

            mainLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
            )
        }

        expandedPlayerView = StyledPlayerView(requireContext()).apply {
            id = R.id.trend_fragment_expanded_player_view
            visibility = View.INVISIBLE
//            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

            useController = false

            isClickable = true
            isFocusable = true

            mainLayout.addView(
                this,
                layoutHelper.createFrame(
                    LayoutHelper.MATCH_PARENT,
                    LayoutHelper.MATCH_PARENT,
                    gravity = Gravity.CENTER
                )
            )
        }
    }

    override fun setup(savedInstanceState: Bundle?) {
        bindTrendData()
        collectPlayerViewState()
    }

    private fun bindTrendData() {
        repeatViewLifecycle {
            trendViewModel.trendResult.collect {
                trendAdapter?.submitData(it)
            }
        }

        if (trendAdapter != null)
            return

        trendAdapter =
            TrendAdapter(
                requireContext(),
                baseColor,
                layoutHelper,
                trendViewModel,
                onPlayerExpandButtonClick
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
                    lastPlayedViewHolder?.run {
                        if (itemView == view &&
                            trendViewModel.lastPlayedItemListPosition in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
                        ) {
                            thumbnailLayout.visibility = View.GONE
                            playerView.visibility = View.VISIBLE
                            (itemView as FrameLayout).layoutTransition = LayoutTransition()
                            if (trendViewModel.isPlayerPaused())
                                trendViewModel.playPlayer()
                        }
                    }
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                    lastPlayedViewHolder?.run {
                        if (itemView == view) {
                            (itemView as FrameLayout).layoutTransition = null
                            thumbnailLayout.visibility = View.VISIBLE
                            playerView.visibility = View.GONE
                            if (trendViewModel.isPlayerPlaying())
                                trendViewModel.pausePlayer()
                        }
                    }

                }

            })

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun zoomPlayerViewFromThumb(trendResult: TrendResult) {
        if (thumbPlayerView == null) return
        expandingPlayerViewAnimator?.cancel()

        expandedPlayerView.player = trendViewModel.player

        val startBoundInt = Rect()
        val finalBoundInt = Rect()
        val globalOffset = Point()

        thumbPlayerView!!.getGlobalVisibleRect(startBoundInt)
        mainLayout.getGlobalVisibleRect(finalBoundInt, globalOffset)
        startBoundInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundInt.offset(-globalOffset.x, -globalOffset.y)

        startExpandingBounds = RectF(startBoundInt)
        finalExpandingBounds = RectF(finalBoundInt)

        if (finalExpandingBounds.width() / finalExpandingBounds.height() > startExpandingBounds.width() / startExpandingBounds.height()) {
            // Extend horizontally
            startExpandingScale = startExpandingBounds.height() / finalExpandingBounds.height()
            val startWidth: Float = startExpandingScale * finalExpandingBounds.width()
            val deltaWidth: Float = (startWidth - startExpandingBounds.width()) / 2
            startExpandingBounds.left -= deltaWidth.toInt()
            startExpandingBounds.right += deltaWidth.toInt()
        } else {
            // Extend vertically
            startExpandingScale = startExpandingBounds.width() / finalExpandingBounds.width()
            val startHeight: Float = startExpandingScale * finalExpandingBounds.height()
            val deltaHeight: Float = (startHeight - startExpandingBounds.height()) / 2
            startExpandingBounds.top -= deltaHeight.toInt()
            startExpandingBounds.bottom += deltaHeight.toInt()
        }


        trendViewModel.setPlayerViewState(playerViewState = TrendViewModel.PlayerViewState.IS_IN_ZOOM_MODE)

        expandingPlayerViewAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(
                    expandedPlayerView,
                    View.X,
                    startExpandingBounds.left,
                    finalExpandingBounds.left
                ),
                ObjectAnimator.ofFloat(
                    expandedPlayerView,
                    View.Y,
                    startExpandingBounds.top,
                    finalExpandingBounds.top
                ),
                ObjectAnimator.ofFloat(
                    expandedPlayerView,
                    View.SCALE_X,
                    startExpandingScale,
                    1f
                ),
                ObjectAnimator.ofFloat(
                    expandedPlayerView,
                    View.SCALE_Y,
                    startExpandingScale,
                    1f
                ),
                ObjectAnimator.ofFloat(
                    expandedViewBackground,
                    View.ALPHA,
                    startExpandingAlpha,
                    finalExpandingAlpha
                )
            )
            duration = BaseAnimation.DURATION_MEDIUM_1
            interpolator = DecelerateInterpolator()
            doOnEnd {
                expandingPlayerViewAnimator = null
                expandedControlLayoutActionInitializer(trendResult)
            }
            doOnCancel {
                expandingPlayerViewAnimator = null
            }
            start()
        }

        var mLastTouchY = 0f
        var mLastTouchX = 0f

        var pointerID = 1000
        expandedPlayerView.apply {
            setOnTouchListener { _, event ->
                if (isEnableToGesturing) {
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            mLastTouchY = event.rawY
                            pointerID = event.getPointerId(0)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val value = event.rawY - mLastTouchY
                            translationY = value

                            if (expandedControlLayout.isVisible) {
                                if (value in 1f..100f)
                                    expandedControlLayout.alpha = 1f - (value / 100)
                                else if (value <= -1f && value >= -100f)
                                    expandedControlLayout.alpha = 1f - (value / -100)

                                if (value >= 100f || value <= -100f) {
                                    expandedPlayerView.removeCallbacks(
                                        hideExpandedControlLayoutRunnable
                                    )
                                    expandedControlLayout.visibility = View.GONE
                                }
                            }

                            if (value in 1f..200f)
                                expandedViewBackground.alpha = finalExpandingAlpha - (value / 200)
                            else if (value <= -1f && value >= -200f)
                                expandedViewBackground.alpha = finalExpandingAlpha - (value / -200)
                            else if (value > 200f || value < -200f)
                                expandedViewBackground.alpha = startExpandingAlpha
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            pointerID = 1000
                            val objectTranslationY = event.rawY - mLastTouchY
                            if (objectTranslationY > 400 || objectTranslationY < -400)
                                zoomOutToThumb()
                            else {
                                animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .translationY(0f)
                                    .setDuration(BaseAnimation.DURATION_SHORT_2)
                                    .start()
                                expandedViewBackground.animate()
                                    .alpha(finalExpandingAlpha)
                                    .setDuration(BaseAnimation.DURATION_SHORT_2)
                                    .start()
                            }
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            event.getPointerId(event.actionIndex).takeIf { it == pointerID }?.run {
                                val newPointerIndex = if (event.actionIndex == 0) 1 else 0
                                mLastTouchY = event.getY(newPointerIndex)
                                pointerID = event.getPointerId(newPointerIndex)
                            }
                        }
                    }
                } else {
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            mLastTouchX = event.rawX - translationX
                            pointerID = event.getPointerId(0)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            translationX = event.rawX - mLastTouchX
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            pointerID = 1000
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            event.getPointerId(event.actionIndex).takeIf { it == pointerID }?.run {
                                val newPointerIndex = if (event.actionIndex == 0) 1 else 0
                                mLastTouchX = event.getX(newPointerIndex)
                                pointerID = event.getPointerId(newPointerIndex)
                            }
                        }
                    }
                }
                expandedPlayerViewGestureDetector?.onTouchEvent(event)
                expandedPlayerViewScaleGestureDetector?.onTouchEvent(event)
                true
            }
        }

        expandedPlayerViewGestureInitializer()
    }

    private fun expandedPlayerViewGestureInitializer() {
        var isScaled = false
        expandedPlayerViewGestureDetector = GestureDetectorCompat(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    isScaled = !isScaled
                    expandedPlayerView.apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            resetPivot()
                        if (isScaled)
                            animate()
                                .scaleX(2f)
                                .scaleY(2f)
                                .setDuration(BaseAnimation.DURATION_SHORT_2)
                                .start()
                        else {
                            animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .translationX(0f)
                                .setDuration(BaseAnimation.DURATION_SHORT_2)
                                .start()
                            isEnableToGesturing = true
                        }
                    }
                    return true
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    expandedPlayerSingleTapAction()
                    return true
                }
            }
        )

        var scaleFactor = 1f
        expandedPlayerViewScaleGestureDetector = ScaleGestureDetector(
            requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    expandedPlayerView.apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            resetPivot()
                        scaleFactor *= detector.scaleFactor
                        scaleX *= scaleFactor
                        scaleY *= scaleFactor
                    }
                    return true
                }
            }
        )
    }

    private fun zoomOutToThumb() {
        if (thumbPlayerView == null) return
        expandingPlayerViewAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(expandedPlayerView, View.X, startExpandingBounds.left),
                ObjectAnimator.ofFloat(expandedPlayerView, View.Y, startExpandingBounds.top),
                ObjectAnimator.ofFloat(expandedPlayerView, View.SCALE_X, startExpandingScale),
                ObjectAnimator.ofFloat(expandedPlayerView, View.SCALE_Y, startExpandingScale),
                ObjectAnimator.ofFloat(expandedViewBackground, View.ALPHA, startExpandingAlpha)
            )
            duration = BaseAnimation.DURATION_MEDIUM_1
            interpolator = DecelerateInterpolator()
            doOnEnd {
                trendViewModel.setPlayerViewState(playerViewState = TrendViewModel.PlayerViewState.IS_IN_THUMB_MODE)
            }
            doOnCancel {
                (activity as MainActivity).run {
                    toggleAppBarLayout(shouldShow = true)
                    toggleBottomNavigation(shouldShow = true)
                }
                expandedPlayerView.apply {
                    visibility = View.GONE
                    player = null
                }
                expandedViewBackground.visibility = View.GONE
                setStatusBarAppearance(backToDefault = true)
                thumbPlayerView!!.apply {
                    alpha = 1f
                    player = trendViewModel.player
                }
            }
            start()
        }
    }

    private fun collectPlayerViewState() = repeatViewLifecycle {
        trendViewModel.playerViewState.collectNotNull { state ->
            when (state) {
                TrendViewModel.PlayerViewState.IS_IN_ZOOM_MODE -> {
                    setStatusBarAppearance(isLight = false)
                    (activity as MainActivity).run {
                        toggleAppBarLayout(shouldShow = false)
                        toggleBottomNavigation(shouldShow = false)
                    }
                    thumbPlayerView!!.alpha = 0f
                    thumbPlayerView!!.player = null
                    expandedPlayerView.apply {
                        visibility = View.VISIBLE
                        pivotX = 0f
                        pivotY = 0f
                    }
                    expandedViewBackground.visibility = View.VISIBLE

                    trendViewModel.addPlayerListener(this@TrendFragment)
                }
                TrendViewModel.PlayerViewState.IS_IN_THUMB_MODE -> {
                    (activity as MainActivity).run {
                        toggleAppBarLayout(shouldShow = true)
                        toggleBottomNavigation(shouldShow = true)
                    }
                    expandedPlayerView.apply {
                        visibility = View.GONE
                        player = null
                    }
                    expandedViewBackground.visibility = View.GONE
                    setStatusBarAppearance(backToDefault = true)
                    thumbPlayerView!!.apply {
                        alpha = 1f
                        player = trendViewModel.player
                    }

                    trendViewModel.removePlayerListener(this@TrendFragment)
                }
            }
        }
    }

    private fun expandedControlLayoutActionInitializer(trendResult: TrendResult) {
        val player = trendViewModel.player ?: return

        expandedControlLayout.run {
            // PlayPause Button
            (getChildAt(0) as MaterialButton).apply {
                setOnClickListener {
                    val pauseToPlayAnimatedIcon =
                        ContextCompat.getDrawable(context, R.drawable.animated_pause_to_play)
                    val playToPauseAnimatedIcon =
                        ContextCompat.getDrawable(context, R.drawable.animated_play_to_pause)

                    if (trendViewModel.isPlayerPlaying()) {
                        trendViewModel.pausePlayer()
                        icon = pauseToPlayAnimatedIcon
                        (icon as AnimatedVectorDrawable).start()
                    } else {
                        trendViewModel.playPlayer()
                        icon = playToPauseAnimatedIcon
                        (icon as AnimatedVectorDrawable).start()
                    }
                }
            }

            // Back Button
            (getChildAt(1) as MaterialButton).setOnClickListener {
                zoomOutToThumb()
            }

            // Title
            (getChildAt(2) as TextView).text = trendResult.original_title

            // Position Text
            val positionText = (getChildAt(3) as TextView)
            startUpdatingCurrentPositionJob()

            // Timer
            val slider = getChildAt(4) as Slider
            slider.apply {
                valueFrom = 0f
                valueTo = (player.duration / 1000).toFloat()

                addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                        expandedPlayerView.removeCallbacks(hideExpandedControlLayoutRunnable)
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        expandedPlayerView.postDelayed(hideExpandedControlLayoutRunnable, 5000)
                    }
                })

                addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        positionText.text = resources.getString(
                            R.string.expanded_position_text,
                            (value.toLong() * 1000).convertMillisToCountDownFormat(false),
                            player.duration.convertMillisToCountDownFormat(false)
                        )
                        player.seekTo(value.toLong() * 1000)
                    }
                }
            }
            startUpdatingTimerSlideValueJob()

            // Detail Button
            val detailButton = getChildAt(5) as MaterialButton
            detailButton.setOnClickListener {
                val id = trendResult.id.toString()
                val title = trendResult.original_title
                val trendResultBackgroundPath = Uri.encode(trendResult.fullBackdropPath)
                findNavController().navigateSlide(
                    route = "${NavRoutes.Main.MOVIE_DETAIL_FRAGMENT}/$id|$title|$trendResultBackgroundPath",
                )
                zoomOutToThumb()
            }
        }
    }

    private fun startUpdatingTimerSlideValueJob() {
        val player = trendViewModel.player ?: return
        val slider = expandedControlLayout.getChildAt(4) as Slider
        currentTimerSliderJob = repeatViewLifecycle {
            while (isActive) {
                logDebug {
                    "startUpdatingTimerSlideValueJob"
                }
                startTimerSliderValueAnimation(
                    slider = slider,
                    value = (player.contentPosition / 1000).toFloat()
                )
                delay(100)
            }
        }
    }

    private fun stopUpdatingTimerSliderValueJob() {
        currentTimerSliderJob?.cancel()
        currentTimerSliderJob = null
    }

    private fun startUpdatingCurrentPositionJob() {
        val player = trendViewModel.player
        if (player == null && currentPositionJob != null)
            return
        val positionText = expandedControlLayout.getChildAt(3) as TextView
        currentPositionJob = repeatViewLifecycle {
            while (isActive) {
                val currentPosition = player!!.currentPosition
                val duration = player.duration
                if (currentPosition > 0) {
                    positionText.text = resources.getString(
                        R.string.expanded_position_text,
                        currentPosition.convertMillisToCountDownFormat(false),
                        duration.convertMillisToCountDownFormat(false)
                    )
                }
                delay(1000)
            }
        }
    }

    private fun stopUpdatingCurrentPositionJob() {
        currentPositionJob?.cancel()
        currentPositionJob = null
    }

    private fun startTimerSliderValueAnimation(slider: Slider, value: Float) {
        timerSliderAnimator = ObjectAnimator.ofFloat(slider, "value", value).apply {
            interpolator = LinearInterpolator()
            duration = BaseAnimation.DURATION_LONG_3

            if (!isStarted)
                start()
        }
    }

    private fun stopTimerSliderValueAnimation() {
        timerSliderAnimator?.cancel()
        timerSliderAnimator = null
    }

    private fun expandedPlayerSingleTapAction() {
        if (expandedControlLayout.isVisible) {
            expandedPlayerView.removeCallbacks(hideExpandedControlLayoutRunnable)
            hideExpandedControlsLayout()
        } else showExpandedControlLayout {
            expandedPlayerView.postDelayed(hideExpandedControlLayoutRunnable, 5000)
        }
    }

    private fun hideExpandedControlsLayout(duration: Long = BaseAnimation.DURATION_MEDIUM_1) {
        expandedControlLayout.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                expandedControlLayout.visibility = View.GONE
            }
    }

    private fun showExpandedControlLayout(endAction: (() -> Unit)? = null) {
        expandedControlLayout.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(BaseAnimation.DURATION_MEDIUM_1)
                .withEndAction {
                    endAction?.invoke()
                }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        val playPauseButton = expandedControlLayout.getChildAt(0) as MaterialButton
        playPauseButton.showProgress(
            showProgress = playbackState == Player.STATE_BUFFERING,
            progressSize = CircularProgressDrawable.LARGE,
            progressColor = baseColor.white,
            initialIcon =
            if (trendViewModel.isPlayerPaused())
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
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            startUpdatingTimerSlideValueJob()
            startUpdatingCurrentPositionJob()
        } else {
            stopUpdatingTimerSliderValueJob()
            stopTimerSliderValueAnimation()
            stopUpdatingCurrentPositionJob()
        }
    }

    override fun onStart() {
        super.onStart()
        logDebug { "onStart" }
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
//        if (!trendViewModel.isPlayerBuffering() && !trendViewModel.isPlayerEnded() && trendViewModel.isPlayerPaused())
//            trendAdapter?.setPlayPauseButtonIcon(isPlaying = false)
    }

    override fun onPause() {
        super.onPause()
        logDebug { "onPause" }
//        trendViewModel.savePlayerCurrentPosition()
        if (findNavController().currentBackStackEntry?.destination?.route == NavRoutes.Main.MOVIE_DETAIL_FRAGMENT)
            zoomOutToThumb()
    }

    override fun onStop() {
        super.onStop()
        logDebug { "onStop" }
        trendViewModel.pausePlayer()
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
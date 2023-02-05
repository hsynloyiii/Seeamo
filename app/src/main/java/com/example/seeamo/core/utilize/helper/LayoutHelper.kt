package com.example.seeamo.core.utilize.helper

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.GravityInt
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.example.seeamo.core.utilize.extensions.toDp
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.Behavior
import com.google.android.material.appbar.AppBarLayout.ChildScrollEffect
import com.google.android.material.appbar.AppBarLayout.LayoutParams.ScrollFlags

class LayoutHelper(private val context: Context?) {

    @IntDef(View.VISIBLE, View.GONE, View.INVISIBLE)
    @Retention
    annotation class Visibility


    private fun getSize(size: Int): Int = if (size < 0) size else size.toDp(context)

    companion object {
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
    }

    // RelativeLayout
    fun createRelative(
        width: Int,
        height: Int,
        endMargin: Int = 0,
        topMargin: Int = 0,
        startMargin: Int = 0,
        bottomMargin: Int = 0,
        alignParent: Int = -1,
        alignRelative: Int = -1,
        anchorRelative: Int = -1
    ): RelativeLayout.LayoutParams =
        RelativeLayout.LayoutParams(getSize(width), getSize(height)).apply {
            if (alignParent >= 0)
                addRule(alignParent)
            if (alignRelative >= 0 && anchorRelative >= 0)
                addRule(alignRelative, anchorRelative)

            this.marginEnd = endMargin
            this.topMargin = topMargin
            this.marginStart = startMargin
            this.bottomMargin = bottomMargin
        }


    // RelativeLayout
    fun createRecycler(
        width: Int,
        height: Int,
        margin: Int = 0,
        endMargin: Int = 0,
        topMargin: Int = 0,
        startMargin: Int = 0,
        bottomMargin: Int = 0
    ): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(getSize(width), getSize(height)).apply {
            if (margin != 0) {
                this.marginEnd = margin
                this.topMargin = margin
                this.marginStart = margin
                this.bottomMargin = margin
            } else {
                this.marginEnd = endMargin
                this.topMargin = topMargin
                this.marginStart = startMargin
                this.bottomMargin = bottomMargin
            }
        }


    // ConstraintLayout
    fun createConstraints(
        width: Int,
        height: Int,
        endMargin: Int = 0,
        topMargin: Int = 0,
        startMargin: Int = 0,
        bottomMargin: Int = 0,
        startToStart: Int = -1,
        topToTop: Int = -1,
        endToEnd: Int = -1,
        bottomToBottom: Int = -1,
        startToEnd: Int = -1,
        topToBottom: Int = -1,
        endToStart: Int = -1,
        bottomToTop: Int = -1,
        verticalChainStyle: Int = ConstraintLayout.LayoutParams.CHAIN_SPREAD,
        horizontalChainStyle: Int = ConstraintLayout.LayoutParams.CHAIN_SPREAD,
    ): ConstraintLayout.LayoutParams =
        ConstraintLayout.LayoutParams(getSize(width), getSize(height)).apply {
            if (width != MATCH_PARENT) {
                this.startToStart = startToStart
                this.endToEnd = endToEnd
                this.startToEnd = startToEnd
                this.endToStart = endToStart
                this.horizontalChainStyle = horizontalChainStyle
            }
            if (height != MATCH_PARENT) {
                this.topToTop = topToTop
                this.bottomToBottom = bottomToBottom
                this.topToBottom = topToBottom
                this.bottomToTop = bottomToTop
                this.verticalChainStyle = verticalChainStyle
            }

            this.marginEnd = endMargin
            this.topMargin = topMargin
            this.marginStart = startMargin
            this.bottomMargin = bottomMargin
        }

    fun updateConstraint(
        view: View,
        startToStart: Int? = null,
        topToTop: Int? = null,
        endToEnd: Int? = null,
        bottomToBottom: Int? = null,
        startToEnd: Int? = null,
        topToBottom: Int? = null,
        endToStart: Int? = null,
        bottomToTop: Int? = null
    ) {
        view.updateLayoutParams<ConstraintLayout.LayoutParams> {
            if (startToStart != null)
                this.startToStart = startToStart

            if (topToTop != null)
                this.topToTop = topToTop

            if (endToEnd != null)
                this.endToEnd = endToEnd

            if (bottomToBottom != null)
                this.bottomToBottom = bottomToBottom

            if (startToEnd != null)
                this.startToEnd = startToEnd

            if (topToBottom != null)
                this.topToBottom = topToBottom

            if (endToStart != null)
                this.endToStart = endToStart

            if (bottomToTop != null)
                this.bottomToTop = bottomToTop
        }
    }

    // CoordinatorLayout
    fun createCoordinator(
        width: Int,
        height: Int,
        behavior: AppBarLayout.ScrollingViewBehavior? = null,
        @GravityInt gravity: Int = Gravity.NO_GRAVITY,
        @GravityInt anchorGravity: Int = Gravity.NO_GRAVITY,
        anchorId: Int = View.NO_ID,
        @GravityInt insetEdge: Int = Gravity.NO_GRAVITY,
        @GravityInt dodgeInsetEdges: Int = Gravity.NO_GRAVITY,
        endMargin: Int = 0,
        topMargin: Int = 0,
        startMargin: Int = 0,
        bottomMargin: Int = 0,
    ): CoordinatorLayout.LayoutParams =
        CoordinatorLayout.LayoutParams(getSize(width), getSize(height)).apply {
            this.gravity = gravity
            this.anchorGravity = anchorGravity
            this.anchorId = anchorId
            this.insetEdge = insetEdge
            this.dodgeInsetEdges = dodgeInsetEdges
            this.behavior = behavior

            this.marginEnd = endMargin
            this.topMargin = topMargin
            this.marginStart = startMargin
            this.bottomMargin = bottomMargin
        }

    // AppBarLayout
    fun createAppBarLayout(
        width: Int,
        height: Int,
        @ScrollFlags scrollFlags: Int,
        scrollEffect: ChildScrollEffect? = null,
        scrollInterpolator: Interpolator? = null,
        @GravityInt gravity: Int = Gravity.NO_GRAVITY,
        endMargin: Int = 0,
        topMargin: Int = 0,
        startMargin: Int = 0,
        bottomMargin: Int = 0,
    ): AppBarLayout.LayoutParams =
        AppBarLayout.LayoutParams(getSize(width), getSize(height)).apply {
            this.gravity = gravity
            this.scrollFlags = scrollFlags
            this.scrollEffect = scrollEffect
            this.scrollInterpolator = scrollInterpolator

            this.marginEnd = endMargin
            this.topMargin = topMargin
            this.marginStart = startMargin
            this.bottomMargin = bottomMargin
        }

    // FrameLayout
    fun createFrame(
        width: Int,
        height: Int,
        gravity: Int = -1,
        startMargin: Int = 0,
        topMargin: Int = 0,
        endMargin: Int = 0,
        bottomMargin: Int = 0,
    ): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(getSize(width), getSize(height), gravity).apply {
            this.marginStart = startMargin
            this.topMargin = topMargin
            this.marginEnd = endMargin
            this.bottomMargin = bottomMargin
        }

    fun updateFrame(
        view: View,
        width: Int? = null,
        height: Int? = null,
        gravity: Int? = null,
        startMargin: Int? = null,
        topMargin: Int? = null,
        endMargin: Int? = null,
        bottomMargin: Int? = null,
    ) {
        view.updateLayoutParams<FrameLayout.LayoutParams> {
            if (width != null)
                this.width = width
            if (height != null)
                this.height = height

            if (gravity != null)
                this.gravity = gravity

            if (startMargin != null)
                this.marginStart = startMargin
            if (topMargin != null)
                this.topMargin = topMargin
            if (endMargin != null)
                this.marginEnd = endMargin
            if (bottomMargin != null)
                this.bottomMargin = bottomMargin
        }
    }

    // LinearLayout
    fun createLinear(
        width: Int,
        height: Int,
        weight: Int = 0,
        gravity: Int = -1,
        startMargin: Int = 0,
        topMargin: Int = 0,
        endMargin: Int = 0,
        bottomMargin: Int = 0
    ): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(getSize(width), getSize(height), weight.toFloat()).apply {
            this.gravity = gravity
            this.marginStart = startMargin
            this.topMargin = topMargin
            this.marginEnd = endMargin
            this.bottomMargin = bottomMargin
        }

    val smallMargin1 = 4.toDp(context)
    val smallMargin2 = 8.toDp(context)
    val mediumMargin1 = 12.toDp(context)
    val mediumMargin2 = 16.toDp(context)
    val largeMargin1 = 24.toDp(context)
    val largeMargin2 = 32.toDp(context)
    val extraLargeMargin = 48.toDp(context)

}
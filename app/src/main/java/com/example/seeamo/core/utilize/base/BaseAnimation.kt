package com.example.seeamo.core.utilize.base

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.content.Context
import com.example.seeamo.core.utilize.extensions.toDp

class BaseAnimation {

    @SuppressLint("ObjectAnimatorBinding")
    class Button {
        companion object {
            fun defaultStateListAnimator(context: Context) = StateListAnimator().apply {
                    addState(
                        intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled),
                        createDefaultAnimatorSet(
                            valuesTo = listOf(6.toDp(context), 2.toDp(context)),
                            durations = listOf(100, 0),
                            propertyNames = arrayOf("translationZ", "elevation")
                        )
                    )
                    addState(
                        intArrayOf(android.R.attr.state_hovered, android.R.attr.state_enabled),
                        createDefaultAnimatorSet(
                            valuesTo = listOf(2.toDp(context), 2.toDp(context)),
                            durations = listOf(100, 0),
                            propertyNames = arrayOf("translationZ", "elevation")
                        )
                    )
                    addState(
                        intArrayOf(android.R.attr.state_focused, android.R.attr.state_enabled),
                        createDefaultAnimatorSet(
                            valuesTo = listOf(2.toDp(context), 2.toDp(context)),
                            durations = listOf(100, 0),
                            propertyNames = arrayOf("translationZ", "elevation")
                        )
                    )
                    addState(
                        intArrayOf(android.R.attr.state_enabled),
                        createDefaultAnimatorSet(
                            valuesTo = listOf(0.toDp(context), 2.toDp(context)),
                            durations = listOf(100, 0),
                            delayStart = 100,
                            "translationZ", "elevation"
                        )
                    )
                    addState(
                        intArrayOf(0),
                        createDefaultAnimatorSet(
                            valuesTo = listOf(0.toDp(context), 0.toDp(context)),
                            durations = listOf(0, 0),
                            propertyNames = arrayOf("translationZ", "elevation")
                        )
                    )
                }

            fun scaleStateListAnimator(toScaleX: Float, toScaleY: Float = toScaleX) =
                StateListAnimator().apply {
                    addState(
                        intArrayOf(android.R.attr.state_pressed),
                        ObjectAnimator.ofPropertyValuesHolder(
                            this,
                            PropertyValuesHolder.ofFloat("scaleX", toScaleX),
                            PropertyValuesHolder.ofFloat("scaleY", toScaleY)
                        )
                    )
                    addState(
                        intArrayOf(),
                        ObjectAnimator.ofPropertyValuesHolder(
                            this,
                            PropertyValuesHolder.ofFloat("scaleX", 1f),
                            PropertyValuesHolder.ofFloat("scaleY", 1f)
                        )
                    )
                }

            private fun createDefaultAnimatorSet(
                valuesTo: List<Int>,
                durations: List<Int>,
                delayStart: Int = 0,
                vararg propertyNames: String
            ): AnimatorSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(this, propertyNames[0], valuesTo[0].toFloat())
                        .apply {
                            duration = durations[0].toLong()
                            startDelay = delayStart.toLong()
                        },
                    ObjectAnimator.ofFloat(this, propertyNames[1], valuesTo[1].toFloat())
                        .apply { duration = durations[1].toLong() }
                )
            }
        }
    }

    companion object {
        const val DURATION_SHORT_1 = 75L
        const val DURATION_SHORT_2 = 150L
        const val DURATION_MEDIUM_1 = 200L
        const val DURATION_MEDIUM_2 = 250L
        const val DURATION_LONG_1 = 300L
        const val DURATION_LONG_2 = 350L
    }
}
package com.example.seeamo.utilize.extensions

import android.content.Context
import android.util.TypedValue
import androidx.core.graphics.ColorUtils

fun Number.toDp(context: Context?): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context?.resources?.displayMetrics
    ).toInt()

fun Int.withAlpha(alpha: Double) : Int = ColorUtils.setAlphaComponent(this, (alpha * 255).toInt())

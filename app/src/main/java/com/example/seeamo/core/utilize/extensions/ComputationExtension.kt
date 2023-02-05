package com.example.seeamo.core.utilize.extensions

import android.content.Context
import android.util.TypedValue
import androidx.core.graphics.ColorUtils
import java.util.concurrent.TimeUnit

fun Number.toDp(context: Context?): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context?.resources?.displayMetrics
    ).toInt()

fun Int.withAlpha(alpha: Double) : Int = ColorUtils.setAlphaComponent(this, (alpha * 255).toInt())

fun Long.convertMillisToCountDownFormat(includeHours: Boolean): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) %
            TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) %
            TimeUnit.MINUTES.toSeconds(1)

    if (includeHours) {
        val hours = TimeUnit.MILLISECONDS.toHours(this)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else
        return String.format("%02d:%02d", minutes, seconds)
}

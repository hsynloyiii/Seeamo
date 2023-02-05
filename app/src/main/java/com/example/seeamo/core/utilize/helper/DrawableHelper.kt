package com.example.seeamo.core.utilize.helper

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import com.example.seeamo.core.utilize.base.BaseColor
import com.example.seeamo.core.utilize.extensions.toDp
import com.example.seeamo.core.utilize.extensions.withAlpha

class DrawableHelper {

    class BottomSheetDivider(
        private val context: Context,
        private val cornerRadius: Float = 16.toDp(context).toFloat(),
        private val backgroundColor: Int = BaseColor(context).gray.withAlpha(0.55)
    ) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun draw(canvas: Canvas) {
            paint.color = backgroundColor
            canvas.drawRoundRect(
                bounds.left.toFloat(),
                bounds.top.toFloat(),
                bounds.width().toFloat(),
                bounds.height().toFloat(),
                cornerRadius,
                cornerRadius,
                paint
            )
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int = PixelFormat.OPAQUE
    }

    class Round(
        private val context: Context,
        private val cornerRadius: Float? = null,
        private val backgroundColor: Int? = null,
        inset: Int? = null
    ) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val inset = inset ?: 8

        override fun draw(canvas: Canvas) {
            paint.color = backgroundColor ?: BaseColor(context).surfaceVariant
            val cornerSize = (bounds.width() / 12).toFloat()
            canvas.drawRoundRect(
                bounds.left.toFloat() + inset.toDp(context),
                bounds.top.toFloat() + inset.toDp(context),
                bounds.width().toFloat() - inset.toDp(context),
                bounds.height().toFloat() - inset.toDp(context),
                cornerRadius?.toDp(context)?.toFloat() ?: cornerSize,
                cornerRadius?.toDp(context)?.toFloat() ?: cornerSize,
                paint
            )
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int = PixelFormat.OPAQUE
    }

}
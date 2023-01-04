package com.example.seeamo.utilize.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.annotation.FontRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import com.example.seeamo.utilize.base.BaseColor
import com.example.seeamo.utilize.extensions.toDp
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.ShapeAppearanceModel

// This include basic implementation of most button's background color with text appearance
class ViewHelper {

    class Button(
        private val mtButton: MaterialButton
    ) {
        fun baseAppearance(
            bcColor: ColorStateList? = null,
            txtColors: ColorStateList? = null,
            txtSize: Float? = null,
            font: Typeface? = null,
            isCircular: Boolean = false,
            cornerRadius: Float? = null,
            onClick: ((View) -> Unit)? = null
        ) = mtButton.apply {
            val baseColor = BaseColor(context)
            setTextColor(
                txtColors
                    ?: baseColor.baseColorStateList(baseColor.onPrimary)
            )
            setPadding(
                Large_PADDING_1.toDp(context),
                MEDIUM_PADDING_1.toDp(context),
                Large_PADDING_1.toDp(context),
                MEDIUM_PADDING_1.toDp(context)
            )
            backgroundTintList = bcColor ?: baseColor.checkedColorStateList(
                baseColor.primary
            )
            maxWidth = 320.toDp(context)
            if (!isCircular) {
                insetBottom = 4.toDp(context)
                insetTop = 4.toDp(context)
                shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(
                    cornerRadius ?: BASE_CORNER_RADIUS.toDp(context).toFloat()
                )
            } else {
                insetBottom = 0
                insetTop = 0
                shapeAppearanceModel = ShapeAppearanceModel().withCornerSize {
                    it.height() / 2
                }
            }
            boldButtonTextAppearance(txtSize, font)
            if (onClick != null)
                setOnClickListener { onClick(it) }
        }


        private fun MaterialButton.boldButtonTextAppearance(
            txtSize: Float? = null,
            font: Typeface? = null
        ) {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize ?: Text.SMALL_TEXT_SIZE_3)
//            typeface = font
//                ?: ResourcesCompat.getFont(mtButton.context, R.font.iran_yekan_bold)
        }
    }

    class Text {

        companion object {
            const val SMALL_TEXT_SIZE_1 = 12f
            const val SMALL_TEXT_SIZE_2 = 14f
            const val SMALL_TEXT_SIZE_3 = 16f
            const val MEDIUM_TEXT_SIZE_1 = 18f
            const val MEDIUM_TEXT_SIZE_2 = 22f
            const val MEDIUM_TEXT_SIZE_3 = 24f
            const val LARGE_TEXT_SIZE_1 = 28f
            const val LARGE_TEXT_SIZE_2 = 32f
            const val LARGE_TEXT_SIZE_3 = 36f
            const val EXTRA_LARGE_TEXT_SIZE_1 = 45f
            const val EXTRA_LARGE_TEXT_SIZE_2 = 57f

            const val SMALL_LINE_SPACE = 2
            const val MEDIUM_LINE_SPACE = 4
            const val LARGE_LINE_SPACE = 8
        }

        class FontSizeSpan(
            private val context: Context,
            @FontRes private val fontResId: Int? = null,
            private val txtSize: Float? = null,
            style: Int = 0
        ) : StyleSpan(style) {
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.apply {
                    if (fontResId != null)
                        typeface = ResourcesCompat.getFont(context, fontResId)
                    if (txtSize != null)
                        textSize = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            txtSize,
                            context.resources?.displayMetrics
                        )
                }
            }

            override fun updateMeasureState(paint: TextPaint) {
                super.updateMeasureState(paint)
                paint.apply {
                    if (txtSize != null)
                        textSize = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            txtSize,
                            context.resources?.displayMetrics
                        )
                }
            }
        }

        class GravitySpan(
            private val gravity: Paint.Align,
            style: Int = 0
        ) : StyleSpan(style) {
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.apply {
                    textAlign = gravity
                }
            }
        }
    }

    class Menu {
        companion object {
            fun createPopup(
                context: Context,
                view: View,
                items: Array<out String>,
                gravity: Int = Gravity.START,
                onMenuClick: ((MenuItem) -> Unit)? = null,
                onDismiss: ((PopupMenu) -> Unit)? = null
            ) =
                PopupMenu(context, view, gravity).apply {
                    items.forEachIndexed { index, item ->
                        menu.add(0, index, index, item)
                    }
                    setOnMenuItemClickListener {
                        if (onMenuClick != null) {
                            onMenuClick(it)
                            true
                        } else false
                    }
                    setOnDismissListener {
                        onDismiss?.invoke(it)
                    }
                }
        }
    }

    companion object {
        const val BASE_CORNER_RADIUS = 12
        const val BASE_ICON_SIZE = 24

        const val SMALL_PADDING_1 = 4
        const val SMALL_PADDING_2 = 8
        const val MEDIUM_PADDING_1 = 12
        const val MEDIUM_PADDING_2 = 16
        const val Large_PADDING_1 = 24
        const val Large_PADDING_2 = 32
        const val EXTRA_LARGE_PADDING = 48
    }

}
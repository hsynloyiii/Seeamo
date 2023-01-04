package com.example.seeamo.utilize.base

import android.content.Context
import android.content.res.ColorStateList
import com.example.seeamo.utilize.extensions.toThemeAttr
import com.example.seeamo.utilize.extensions.withAlpha
import com.google.android.material.R

class BaseColor(context: Context) {

    fun baseRippleColorStateList(color: Int) = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_hovered),
            intArrayOf(0)
        ),
        intArrayOf(
            color.withAlpha(0.2),
            color.withAlpha(0.2),
            color.withAlpha(0.16),
            color.withAlpha(0.2)
        )
    )

    fun customTransparencyRippleColorStateList(color: Int, transparency: Double) =
        ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_hovered),
                intArrayOf(0)
            ),
            intArrayOf(
                color.withAlpha(transparency),
                color.withAlpha(transparency),
                color.withAlpha(transparency),
                color.withAlpha(transparency)
            )
        )

    fun noAlphaRippleColor(color: Int) = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_hovered),
            intArrayOf(0)
        ),
        intArrayOf(
            color,
            color,
            color,
            color
        )
    )

    fun withoutRippleColor() =
        ColorStateList(arrayOf(intArrayOf()), intArrayOf(android.R.color.transparent))

    fun baseColorStateList(
        enabledColor: Int,
        disabledColor: Int = enabledColor
    ) = ColorStateList(
        arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(0)),
        intArrayOf(
            disabledColor,
            enabledColor
        )
    )

    fun checkedColorStateList(
        unCheckedColor: Int,
        checkedColor: Int = unCheckedColor
    ) = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        ),
        intArrayOf(checkedColor, unCheckedColor)
    )

    fun errorColorStateList(context: Context) = ColorStateList(
        arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
        intArrayOf(
            BaseColor(context).onError,
            BaseColor(context).error
        )
    )

    fun changeColorByPress(color: Int, pressedColor: Int) = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(0)),
        intArrayOf(pressedColor, color)
    )

    fun checkWithPressColorStateList(
        baseColor: Int,
        checkedColor: Int,
        pressedColor: Int
    ) = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_checked, -android.R.attr.state_pressed)
        ),
        intArrayOf(checkedColor, pressedColor, baseColor)
    )

    // primary
    val primary = context.toThemeAttr(R.attr.colorPrimary)
    val onPrimary = context.toThemeAttr(R.attr.colorOnPrimary)
    val primaryContainer = context.toThemeAttr(R.attr.colorPrimaryContainer)
    val onPrimaryContainer = context.toThemeAttr(R.attr.colorOnPrimaryContainer)

    // secondary
    val secondary = context.toThemeAttr(R.attr.colorSecondary)
    val onSecondary = context.toThemeAttr(R.attr.colorOnSecondary)
    val secondaryContainer = context.toThemeAttr(R.attr.colorSecondaryContainer)
    val onSecondaryContainer = context.toThemeAttr(R.attr.colorOnSecondaryContainer)

    // tertiary
    val tertiary = context.toThemeAttr(R.attr.colorTertiary)
    val onTertiary = context.toThemeAttr(R.attr.colorOnTertiary)
    val tertiaryContainer = context.toThemeAttr(R.attr.colorTertiaryContainer)
    val onTertiaryContainer = context.toThemeAttr(R.attr.colorOnTertiaryContainer)

    // error
    val error = context.toThemeAttr(R.attr.colorError)
    val onError = context.toThemeAttr(R.attr.colorOnError)
    val errorContainer = context.toThemeAttr(R.attr.colorErrorContainer)
    val onErrorContainer = context.toThemeAttr(R.attr.colorOnErrorContainer)

    // natural
    val background = context.toThemeAttr(R.attr.backgroundColor)
    val onBackground = context.toThemeAttr(R.attr.colorOnBackground)
    val surface = context.toThemeAttr(R.attr.colorSurface)
    val onSurface = context.toThemeAttr(R.attr.colorOnSurface)

    // neutral variant
    val surfaceVariant = context.toThemeAttr(R.attr.colorSurfaceVariant)
    val onSurfaceVariant = context.toThemeAttr(R.attr.colorOnSurfaceVariant)
    val outline = context.toThemeAttr(R.attr.colorOutline)

    val black = context.toThemeAttr(com.example.seeamo.R.attr.black)
    val white = context.toThemeAttr(com.example.seeamo.R.attr.white)
    val blue = context.toThemeAttr(com.example.seeamo.R.attr.blue)
    val darkBlue = context.toThemeAttr(com.example.seeamo.R.attr.dark_blue)
    val gray = context.toThemeAttr(com.example.seeamo.R.attr.gray)
    val red = context.toThemeAttr(com.example.seeamo.R.attr.red)
    val transparent = context.getColor(android.R.color.transparent)
}
package com.example.seeamo.utilize.extensions

import android.animation.*
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.view.*
import androidx.core.widget.doOnTextChanged
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.seeamo.utilize.helper.LayoutHelper
import com.example.seeamo.data.model.InitialMargin
import com.example.seeamo.data.model.InitialPadding
import com.example.seeamo.utilize.base.BaseColor
import com.example.seeamo.utilize.helper.ViewHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.button.MaterialButtonToggleGroup

fun View.snack(msg: String) = Snackbar.make(
    this,
    msg,
    Snackbar.LENGTH_SHORT
).show()

fun View.customDurationSnack(msg: String, duration: Int) = Snackbar.make(
    this,
    msg,
    Snackbar.LENGTH_INDEFINITE
).setDuration(duration).show()

// Insets ---
fun View.applyMarginWindowInsets(
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false,
    margin: InitialMargin = InitialMargin()
) {
    doOnApplyWindowInsets { view, windowInsetsCompat, _ ->
        val insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
        val left = if (applyLeft) insets.left else 0
        val top = if (applyTop) insets.top else 0
        val right = if (applyRight) insets.right else 0
        val bottom = if (applyBottom) insets.bottom else 0

        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = margin.left + left
            topMargin = margin.top + top
            rightMargin = margin.right + right
            bottomMargin = margin.bottom + bottom
        }
    }
}

fun View.applyPaddingWindowInsets(
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false
) {
    doOnApplyWindowInsets { view, windowInsetsCompat, initialPadding ->
        val insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
        val left = if (applyLeft) insets.left else 0
        val top = if (applyTop) insets.top else 0
        val right = if (applyRight) insets.right else 0
        val bottom = if (applyBottom) insets.bottom else 0

        view.updatePadding(
            left,
            top,
            right,
             bottom
        )
    }
}

fun View.doOnApplyWindowInsets(
    block: (View, WindowInsetsCompat, InitialPadding) -> Unit
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        block(
            view,
            windowInsets,
            InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        )

        WindowInsetsCompat.CONSUMED
    }

    doOnAttach {
        requestApplyInsets()
    }
}

// Buttons ---

fun MaterialButton.filled(
    bcColor: ColorStateList? = null,
    txtColor: ColorStateList? = null,
    txtSize: Float? = null,
    font: Typeface? = null,
    rippleColor: ColorStateList? = null,
    isCircular: Boolean = false,
    cornerRadius: Float? = null,
    stateAnimator: StateListAnimator? = null,
    onClick: ((View) -> Unit)? = null
) {
    val baseColor = BaseColor(context)
    ViewHelper.Button(this).baseAppearance(
        bcColor, txtColor, txtSize, font, isCircular, cornerRadius, onClick
    )
    this.rippleColor = rippleColor ?: baseColor.baseRippleColorStateList(
        baseColor.primaryContainer
    )
    stateListAnimator = stateAnimator
    textAlignment = MaterialButton.TEXT_ALIGNMENT_CENTER
}

fun MaterialButton.outlined(
    bcColor: ColorStateList? = null,
    txtColor: ColorStateList? = null,
    txtSize: Float? = null,
    font: Typeface? = null,
    strokeWidth: Int? = null,
    colorStroke: Int? = null,
    rippleColor: Int? = null,
    isCircular: Boolean = false,
    sizeCorner: Float? = null,
    onClick: ((View) -> Unit)? = null
) {
    val baseColor = BaseColor(context)
    ViewHelper.Button(this)
        .baseAppearance(
            bcColor ?: baseColor.baseColorStateList(baseColor.background),
            txtColor ?: baseColor.changeColorByPress(
                color = baseColor.onBackground,
                pressedColor = baseColor.onPrimary
            ),
            txtSize,
            font,
            isCircular,
            sizeCorner,
            onClick
        )

    strokeColor = baseColor.baseColorStateList(
        colorStroke ?: baseColor.primary
    )
    this.strokeWidth = strokeWidth ?: 2.toDp(context)
    this.rippleColor =
        baseColor.noAlphaRippleColor(rippleColor ?: baseColor.primary)
    elevation = 2.toDp(context).toFloat()
}

fun MaterialButton.textButton(
    txtColor: ColorStateList? = null,
    bcColor: ColorStateList? = null,
    txtSize: Float? = null,
    font: Typeface? = null,
    rippleColor: ColorStateList? = null,
    isCircular: Boolean = false,
    cornerRadius: Float? = null,
    stateListAnimator: StateListAnimator? = null,
    onClick: ((View) -> Unit)? = null
) {
    val baseColor = BaseColor(context)
    ViewHelper.Button(this).baseAppearance(
        bcColor ?: baseColor.checkedColorStateList(baseColor.transparent),
        txtColor ?: baseColor.baseColorStateList(
            baseColor.onBackground.withAlpha(0.75)
        ),
        txtSize,
        font,
        isCircular,
        cornerRadius,
        onClick
    )
    this.rippleColor =
        rippleColor ?: baseColor.baseRippleColorStateList(baseColor.primary)
    this.stateListAnimator = stateListAnimator
}

// For index `0` should set default and index `1` should set pressed color
fun MaterialButton.changeBackgroundByPressed(
    bcColors: List<Int>,
    txtColors: List<Int>,
    txtSize: Float? = null,
    font: Typeface? = null,
    colorRipple: ColorStateList? = null,
    isCircular: Boolean = false,
    sizeCorner: Float? = null,
    onClick: ((View) -> Unit)? = null
) {
    val baseColor = BaseColor(context)
    ViewHelper.Button(this).baseAppearance(
        baseColor.changeColorByPress(
            color = bcColors[0],
            pressedColor = if (bcColors.size > 1)
                bcColors[1] else bcColors[0]
        ),
        baseColor.changeColorByPress(
            color = txtColors[0],
            pressedColor = if (txtColors.size > 1)
                txtColors[1] else txtColors[0]

        ),
        txtSize,
        font,
        isCircular,
        sizeCorner,
        onClick
    )
    rippleColor = colorRipple ?: baseColor.withoutRippleColor()
}

fun MaterialButton.iconButton(
    icon: Drawable?,
    bcColor: ColorStateList? = null,
    iconTint: ColorStateList? = null,
    colorRipple: ColorStateList? = null,
    isCircular: Boolean = false,
    sizeCorner: Float? = null,
    onClick: ((View) -> Unit)? = null
) {
    val baseColor = BaseColor(context)
    updateLayoutParams<ViewGroup.LayoutParams> {
        width = 48.toDp(context)
        height = 48.toDp(context)
    }
    ViewHelper.Button(this).baseAppearance(
        bcColor = bcColor ?: baseColor.checkedColorStateList(baseColor.background),
        isCircular = isCircular,
        cornerRadius = sizeCorner ?: (width / 2).toFloat(),
        onClick = onClick
    )
    shapeAppearanceModel = ShapeAppearanceModel().withCornerSize {
        it.width() / 2
    }
    iconButtonPadding()
    text = null
    this.icon = icon
    iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
    this.iconTint = iconTint ?: baseColor.baseColorStateList(baseColor.onBackground)
    iconSize = ViewHelper.BASE_ICON_SIZE.toDp(context)
    rippleColor = colorRipple
        ?: baseColor.customTransparencyRippleColorStateList(
            baseColor.onSurfaceVariant,
            0.08
        )
    iconPadding = 0
}

fun MaterialButton.backButton(
    colorRipple: ColorStateList? = null,
    sizeCorner: Float? = null,
    onClick: ((View) -> Unit)? = null
) {
    val baseColor = BaseColor(context)
    updateLayoutParams<ViewGroup.LayoutParams> {
        width = 48.toDp(context)
        height = 48.toDp(context)
    }
    ViewHelper.Button(this).baseAppearance(
        bcColor = baseColor.checkedColorStateList(baseColor.background),
        cornerRadius = sizeCorner ?: (width / 2).toFloat(),
        onClick = onClick
    )
    shapeAppearanceModel = ShapeAppearanceModel().withCornerSize {
        it.width() / 2
    }
    iconButtonPadding()
    text = null
    icon = ContextCompat.getDrawable(
        context,
        com.example.seeamo.R.drawable.ic_arrow_back_24
    )
    iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
    iconTint = baseColor.baseColorStateList(baseColor.onBackground)
    iconSize = ViewHelper.BASE_ICON_SIZE.toDp(context)
    rippleColor = colorRipple
        ?: baseColor.customTransparencyRippleColorStateList(
            baseColor.onSurfaceVariant,
            0.08
        )
    iconPadding = 0
}

fun MaterialButton.enableBtn() {
    isClickable = true
    isFocusable = true
    isEnabled = true
}

fun MaterialButton.disableBtn() {
    isClickable = false
    isFocusable = false
    isEnabled = false
}

fun MaterialButton.iconButtonPadding() {
    setPadding(
        ViewHelper.SMALL_PADDING_2.toDp(context),
        ViewHelper.SMALL_PADDING_2.toDp(context),
        ViewHelper.SMALL_PADDING_2.toDp(context),
        ViewHelper.SMALL_PADDING_2.toDp(context)
    )
}

fun MaterialButton.validateWithInputs(
    inputs: List<EditText?>,
    validate: ((List<EditText?>) -> Boolean)? = null,
    trueCondition: ((List<EditText?>) -> Unit)? = null,
    falseCondition: ((List<EditText?>) -> Unit)? = null
) {
    if (validate != null)
        if (validate(inputs)) {
            enableBtn()
        } else {
            disableBtn()
        }
    else
        if (inputs.any { it?.text.isNullOrEmpty() }) {
            disableBtn()
        } else {
            enableBtn()
        }
    inputs.forEach { editText ->
        editText?.doOnTextChanged { _, _, _, _ ->
            if (validate != null)
                if (validate(inputs)) {
                    enableBtn()
                } else {
                    disableBtn()
                }
            else
                if (inputs.any { it?.text.isNullOrEmpty() }) {
                    disableBtn()
                } else {
                    enableBtn()
                }
            trueCondition?.invoke(inputs)
            falseCondition?.invoke(inputs)
        }
    }
}

fun MaterialButton.validate(
    validate: () -> Boolean
) {
    if (validate())
        enableBtn()
    else
        disableBtn()
}

fun MaterialButton.showProgress(
    showProgress: Boolean?,
    initialText: String? = null,
    progressColor: Int = BaseColor(context).onBackground,
    initialIcon: Drawable? = null
) {
    when (showProgress) {
        true -> {
            icon = CircularProgressDrawable(context!!).apply {
                setStyle(CircularProgressDrawable.DEFAULT)
                setColorSchemeColors(progressColor)
                iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
                strokeWidth = 2.toDp(context).toFloat()
                start()
            }
            iconPadding = 0
            disableBtn()
            text = null
        }
        null -> {
            disableBtn()
            text = initialText
        }
        else -> {
            enableBtn()
            icon = initialIcon
            text = initialText
        }
    }
}

fun MaterialButtonToggleGroup.set(
    buttons: Array<MaterialButton>,
    isSingleSelection: Boolean = true,
    onButtonChecked: ((MaterialButtonToggleGroup, checkedId: Int, isChecked: Boolean) -> Unit)? = null
) {
    val baseColor = BaseColor(context)
    val buttonBackgroundColor =
        baseColor.checkedColorStateList(baseColor.background, baseColor.primaryContainer)
    val buttonTextColor = baseColor.checkWithPressColorStateList(
        baseColor.onBackground,
        baseColor.onPrimaryContainer,
        baseColor.onPrimaryContainer.withAlpha(0.5)
    )

    val rippleColor = baseColor.primaryContainer

    buttons.forEachIndexed { index, btn ->
        btn.id = index
        btn.outlined(
            bcColor = buttonBackgroundColor,
            txtColor = buttonTextColor,
            strokeWidth = 1,
            rippleColor = rippleColor
        )
        addView(
            btn,
            LayoutHelper(context).createLinear(0, LayoutHelper.WRAP_CONTENT, 1)
        )
    }
    this.isSingleSelection = isSingleSelection
    if (onButtonChecked != null)
        addOnButtonCheckedListener { group, checkedId, isChecked ->
            onButtonChecked(
                group,
                checkedId,
                isChecked
            )
        }
}


fun View.screenSize(): Point {
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
        point.set(metrics.bounds.width(), metrics.bounds.height())
        return point
    } else {
        val display = (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).displays[0]
        @Suppress("DEPRECATION")
        display.getSize(point)
        return point
    }
}






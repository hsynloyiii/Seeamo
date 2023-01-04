package com.example.seeamo.utilize.extensions

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.SpannableString
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.set
import com.example.seeamo.utilize.base.BaseColor
import com.example.seeamo.utilize.helper.DrawableHelper
import com.example.seeamo.utilize.helper.ViewHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.ShapeAppearanceModel

fun Context.toast(
    msg: String
) = Toast.makeText(
    this,
    msg,
    Toast.LENGTH_SHORT
).show()


fun Context.toThemeAttr(attr: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun Context.toThemeResourceId(attr: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.resourceId
}

fun Context.alert(
    title: String,
    message: String? = null,
    negativeButtonText: String,
    positiveButtonText: String,
    positiveButtonColor: Int? = null,
    positiveButtonAction: (DialogInterface) -> Unit
): AlertDialog {
    val titleMsg = SpannableString(title).apply {
        set(
            0, length, ViewHelper.Text.FontSizeSpan(
                this@alert,
                txtSize = ViewHelper.Text.MEDIUM_TEXT_SIZE_2
            )
        )
    }
    val builder = MaterialAlertDialogBuilder(this)
        .setTitle(titleMsg)
        .setMessage(message)
        .setNegativeButton(negativeButtonText) { dialog, _ ->
            dialog.cancel()
        }
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            positiveButtonAction(dialog)
        }
        .create()

    builder.apply {
        show()
        window?.setBackgroundDrawable(
            DrawableHelper.Round(
                this@alert,
                12f,
                BaseColor(this@alert).surface,
                6
            )
        )

        findViewById<TextView>(android.R.id.message)?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, ViewHelper.Text.SMALL_TEXT_SIZE_2)
        }
        findViewById<TextView>(android.R.id.message)?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, ViewHelper.Text.SMALL_TEXT_SIZE_2)
        }

        val btnShape = ShapeAppearanceModel().withCornerSize(12.toDp(this@alert).toFloat())
        (getButton(Dialog.BUTTON_NEGATIVE) as MaterialButton).also { btn ->
            btn.shapeAppearanceModel = btnShape
        }
        (getButton(Dialog.BUTTON_POSITIVE) as MaterialButton).also { btn ->
            btn.shapeAppearanceModel = btnShape
            if (positiveButtonColor != null) {
                btn.rippleColor = BaseColor(context).baseRippleColorStateList(positiveButtonColor)
                btn.setTextColor(positiveButtonColor)
            }
        }
    }

    return builder
}
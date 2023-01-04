package com.example.seeamo.utilize.extensions

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seeamo.utilize.base.BaseColor

fun ViewGroup.defaultAppearance(fitSystem: Boolean = false) {
    val baseColor = BaseColor(context)
    setBackgroundColor(baseColor.background)
    fitsSystemWindows = fitSystem
}

// Runs [action] on every visible [RecyclerView.ViewHolder] in this [RecyclerView]
inline fun <reified T : RecyclerView.ViewHolder> RecyclerView.forEachVisibleHolder(
    action: (T) -> Unit
) {
    for (i in 0 until childCount) {
        action(getChildViewHolder(getChildAt(i)) as T)
    }
}
package com.example.seeamo.core.utilize.extensions

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seeamo.core.utilize.base.BaseColor

fun ViewGroup.defaultAppearance(baseColor: BaseColor, fitSystem: Boolean = false) {
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
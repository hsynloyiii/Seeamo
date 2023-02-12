package com.example.seeamo.core.ui

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class ScrollingViewWithBottomNavigationBehavior : AppBarLayout.ScrollingViewBehavior() {
    private var bottomMargin = 0

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return super.layoutDependsOn(parent, child, dependency) || dependency is BottomNavigationView
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val result = super.onDependentViewChanged(parent, child, dependency)

        return if(dependency is BottomNavigationView && dependency.height != bottomMargin) {
            bottomMargin = dependency.height
            val layout = child.layoutParams as CoordinatorLayout.LayoutParams
            layout.bottomMargin = bottomMargin
            child.requestLayout()
            true
        } else {
            result
        }
    }
}
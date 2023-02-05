package com.example.seeamo.core.utilize.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.seeamo.core.utilize.helper.LayoutHelper
import com.example.seeamo.core.utilize.extensions.changeDecorFitsSystemWindows

abstract class BaseActivity: AppCompatActivity() {

    var root: View? = null
    lateinit var baseColor: BaseColor
        private set

    abstract fun createViews(savedInstanceState: Bundle?)
    abstract fun setup(savedInstanceState: Bundle?)

    lateinit var layoutHelper: LayoutHelper
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        changeDecorFitsSystemWindows(false)
        super.onCreate(savedInstanceState)

        layoutHelper = LayoutHelper(this)
        baseColor = BaseColor(this)
        createViews(savedInstanceState)
        setContentView(root ?: View(this))
        setup(savedInstanceState)
    }

}

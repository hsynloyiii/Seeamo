package com.example.seeamo.utilize.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.seeamo.utilize.helper.LayoutHelper
import com.example.seeamo.utilize.extensions.changeDecorFitsSystemWindows

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
        createViews(savedInstanceState)
        setContentView(root ?: View(this))
        baseColor = BaseColor(this)
        setup(savedInstanceState)
    }

}

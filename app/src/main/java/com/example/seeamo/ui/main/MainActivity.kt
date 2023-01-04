package com.example.seeamo.ui.main

import android.os.Bundle
import android.view.Menu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.fragment
import androidx.navigation.get
import androidx.navigation.ui.setupWithNavController
import com.example.seeamo.R
import com.example.seeamo.ui.menu.MenuFragment
import com.example.seeamo.ui.movie.MovieFragment
import com.example.seeamo.ui.news.NewsFragment
import com.example.seeamo.ui.series.SeriesFragment
import com.example.seeamo.ui.trend.TrendFragment
import com.example.seeamo.utilize.base.BaseActivity
import com.example.seeamo.utilize.extensions.applyMarginWindowInsets
import com.example.seeamo.utilize.extensions.defaultAppearance
import com.example.seeamo.utilize.extensions.toDp
import com.example.seeamo.utilize.helper.LayoutHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private lateinit var mainLayout: ConstraintLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fragmentContainerView: FragmentContainerView

    override fun createViews(savedInstanceState: Bundle?) {
        root = ConstraintLayout(this)
        mainLayout = (root as ConstraintLayout).apply {
            defaultAppearance()
        }
        fragmentContainerView = FragmentContainerView(this).apply {
            id = R.id.main_nav
        }
        bottomNavigationView = BottomNavigationView(this).apply {
            id = R.id.main_bottom_navigation

            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
            isItemActiveIndicatorEnabled = false
            setBackgroundColor(baseColor.background)
            itemRippleColor = baseColor.baseRippleColorStateList(baseColor.primary)
            itemTextColor = baseColor.checkedColorStateList(
                checkedColor = baseColor.darkBlue,
                unCheckedColor = baseColor.onBackground
            )
            itemIconTintList = null
            itemPaddingTop = 8.toDp(this@MainActivity)
            itemPaddingBottom = 8.toDp(this@MainActivity)
            elevation = 0f
        }
        mainLayout.apply {
            addView(
                fragmentContainerView,
                layoutHelper.createConstraints(
                    LayoutHelper.MATCH_PARENT,
                    0,
                    topToTop = 0,
                    bottomToTop = bottomNavigationView.id
                )
            )
            addView(
                bottomNavigationView,
                layoutHelper.createConstraints(
                    LayoutHelper.MATCH_PARENT,
                    25.toDp(this@MainActivity),
                    bottomToBottom = 0
                )
            )
        }
        bottomNavigationView.applyMarginWindowInsets(applyBottom = true)
    }

    override fun setup(savedInstanceState: Bundle?) {
        setupNavGraph()
    }

    private fun setupNavGraph() {
        val containerFragment: Fragment =
            supportFragmentManager.fragmentFactory.instantiate(
                fragmentContainerView.context.classLoader,
                NavHostFragment().javaClass.name
            )

        supportFragmentManager.commitNow(allowStateLoss = true) {
            setReorderingAllowed(true)
            add(fragmentContainerView.id, containerFragment)
            setPrimaryNavigationFragment(containerFragment)
        }

        navHostFragment =
            supportFragmentManager.findFragmentById(fragmentContainerView.id) as NavHostFragment
        navController = navHostFragment.navController

        navController.graph = navController.createGraph(
            startDestination = NavRoutes.Main.START_DESTINATION
        ) {
            fragment<TrendFragment>(NavRoutes.Main.TREND_FRAGMENT).apply {
                label = resources.getString(R.string.trend_fragment)
            }
            fragment<MovieFragment>(NavRoutes.Main.MOVIE_FRAGMENT).apply {
                label = resources.getString(R.string.movie_fragment)
            }
            fragment<SeriesFragment>(NavRoutes.Main.SERIES_FRAGMENT).apply {
                label = resources.getString(R.string.series_fragment)
            }
            fragment<NewsFragment>(NavRoutes.Main.NEWS_FRAGMENT).apply {
                label = resources.getString(R.string.news_fragment)
            }
            fragment<MenuFragment>(NavRoutes.Main.MENU_FRAGMENT).apply {
                label = resources.getString(R.string.menu_fragment)
            }
        }

        setupBottomNav()
    }

    private fun setupBottomNav() {
        val menuIds = listOf(
            navController.graph[NavRoutes.Main.TREND_FRAGMENT].id,
            navController.graph[NavRoutes.Main.MOVIE_FRAGMENT].id,
            navController.graph[NavRoutes.Main.SERIES_FRAGMENT].id,
            navController.graph[NavRoutes.Main.NEWS_FRAGMENT].id,
            navController.graph[NavRoutes.Main.MENU_FRAGMENT].id
        )
        val menuTitles =
            listOf(R.string.trend, R.string.movie, R.string.series, R.string.news, R.string.menu)
        val menuIcons = listOf(
            R.drawable.animated_trend,
            R.drawable.animated_movie,
            R.drawable.animated_series,
            R.drawable.animated_news,
            R.drawable.animated_menu
        )


        menuIds.forEachIndexed { index, id ->
            bottomNavigationView.menu.add(Menu.NONE, id, index, menuTitles[index])
                .setIcon(menuIcons[index])
        }

        bottomNavigationView.apply {
            setupWithNavController(navController)

            selectedItemId = menu[0].itemId
        }
    }

}
package com.example.seeamo.ui.main

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentContainerView
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
import com.example.seeamo.utilize.extensions.*
import com.example.seeamo.utilize.helper.LayoutHelper
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private lateinit var mainLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: MaterialToolbar

    private lateinit var appBarConstraintLayout: ConstraintLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fragmentContainerView: FragmentContainerView

    @SuppressLint("InflateParams")
    override fun createViews(savedInstanceState: Bundle?) {
        root = CoordinatorLayout(this)


        mainLayout = (root as CoordinatorLayout).apply {
            defaultAppearance(baseColor, false)
        }

        appBarLayout = AppBarLayout(this).apply {
            fitsSystemWindows = true
            isLiftOnScroll = false

            statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(
                context
            )
            setStatusBarForegroundColor(baseColor.background)
            setBackgroundColor(baseColor.background)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                outlineAmbientShadowColor = baseColor.onBackground.withAlpha(0.52)
                outlineSpotShadowColor = baseColor.onBackground.withAlpha(0.52)
            }

            minimumHeight = 36.toDp(this@MainActivity)
            mainLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT
            )
        }

        toolbar = MaterialToolbar(this).apply {
            id = R.id.main_toolbar

            setTitleTextAppearance(
                this@MainActivity,
                toThemeResourceId(com.google.android.material.R.attr.textAppearanceHeadline6)
            )


            menu.add(Menu.NONE, R.id.main_toolbar_search, 0, R.string.main_toolbar_search).apply {
                setIcon(R.drawable.ic_round_search_24)
                setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
                actionView = SearchView(this@MainActivity).apply {
                    maxWidth = Int.MAX_VALUE

                    setOnSearchClickListener {
                        startAnimation(
                            AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in).apply {
                                setAnimationListener(null)
                            })
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    contentDescription = resources.getString(R.string.main_toolbar_search)
            }

            appBarLayout.addView(
                this,
                layoutHelper.createAppBarLayout(
                    LayoutHelper.MATCH_PARENT,
                    22.toDp(context),
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                )
            )
        }

        appBarConstraintLayout = ConstraintLayout(this).apply {
            setBackgroundColor(baseColor.transparent)
            mainLayout.addView(
                this,
                layoutHelper.createCoordinator(
                    LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,
                    behavior = AppBarLayout.ScrollingViewBehavior()
                )
            )
            applyMarginWindowInsets(applyBottom = true)
        }

        fragmentContainerView = layoutInflater.inflate(
            R.layout.activity_container_fragment,
            null
        ) as FragmentContainerView

        bottomNavigationView = BottomNavigationView(this).apply {
            id = R.id.main_bottom_navigation

            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_UNLABELED
            isItemActiveIndicatorEnabled = false
            setBackgroundColor(baseColor.background)
            itemRippleColor = baseColor.baseColorStateList(baseColor.surfaceVariant.withAlpha(0.32))
            itemTextColor = baseColor.checkedColorStateList(
                checkedColor = baseColor.darkBlue,
                unCheckedColor = baseColor.onBackground
            )
            itemIconTintList = null
//            itemPaddingTop = 8.toDp(this@MainActivity)
//            itemPaddingBottom = 8.toDp(this@MainActivity)
            elevation = 0f
            setPadding(0)

            itemIconSize = 26.toDp(context)


            mainLayout.addView(
                this,
                layoutHelper.createCoordinator(
                    LayoutHelper.MATCH_PARENT,
                    22.toDp(this@MainActivity),
                    gravity = Gravity.BOTTOM
                )
            )
            applyMarginWindowInsets(applyBottom = true)
        }

        appBarConstraintLayout.addView(
            fragmentContainerView,
            layoutHelper.createConstraints(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT,
                bottomMargin = 68.toDp(this@MainActivity)
            )
        )

    }

    override fun setup(savedInstanceState: Bundle?) {
        setupNavGraph()
    }

    private fun setupNavGraph() {
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
        setupNavControllerDestinationListener()
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

            setOnItemReselectedListener {
                when (it.itemId) {
                    menuIds[0] -> {
                        (navHostFragment.childFragmentManager.fragments[0] as TrendFragment)
                            .trendRecyclerView.scrollToPosition(0)
                        appBarLayout.setExpanded(true, true)
                    }
                }
            }
        }
    }

    private fun setupNavControllerDestinationListener() =
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.route) {
                NavRoutes.Main.TREND_FRAGMENT -> {
                    setupToolbarWithNavController(
                        title = resources.getString(R.string.app_name),
                        searchQueryHint = "Search all movie/series ..."
                    ) {}
                }
                NavRoutes.Main.MOVIE_FRAGMENT -> {
                    setupToolbarWithNavController(
                        title = resources.getString(R.string.movie),
                        searchQueryHint = "Search all movie/series ..."
                    ) {}
                }
                NavRoutes.Main.SERIES_FRAGMENT -> {
                    setupToolbarWithNavController(
                        title = resources.getString(R.string.series),
                        searchQueryHint = "Search all movie/series ..."
                    ) {}
                }
                NavRoutes.Main.NEWS_FRAGMENT -> {
                    setupToolbarWithNavController(
                        title = resources.getString(R.string.news),
                        searchQueryHint = "Search any news/journal ..."
                    ) {}
                }
                NavRoutes.Main.MENU_FRAGMENT -> {
                    setupToolbarWithNavController(
                        title = resources.getString(R.string.menu),
                        isSearchMenuVisible = false
                    ) {}
                }
            }
        }

    private fun setupToolbarWithNavController(
        title: String,
        appBarExpand: Boolean = true,
        appBarExpandWithAnimation: Boolean = true,
        isSearchMenuVisible: Boolean = true,
        searchQueryHint: String = "",
        onQueryTextChanged: ((String) -> Unit)? = null
    ) {
        toolbar.title = title
        appBarLayout.setExpanded(appBarExpand, appBarExpandWithAnimation)

        val menuItem = toolbar.menu[0]
        menuItem.isVisible = isSearchMenuVisible
        menuItem.collapseActionView()

        (menuItem.actionView as SearchView).apply {
            this.queryHint = searchQueryHint

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText != null && onQueryTextChanged != null) {
                        onQueryTextChanged(newText)
                    }
                    return true
                }
            })
        }
    }

}
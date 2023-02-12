package com.example.seeamo.core.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.get
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.fragment
import androidx.navigation.get
import androidx.navigation.ui.setupWithNavController
import com.example.seeamo.R
import com.example.seeamo.setting.MenuFragment
import com.example.seeamo.content.MovieFragment
import com.example.seeamo.news.NewsFragment
import com.example.seeamo.search.SearchFragment
import com.example.seeamo.trend.ui.TrendFragment
import com.example.seeamo.core.utilize.base.BaseActivity
import com.example.seeamo.core.utilize.extensions.*
import com.example.seeamo.core.utilize.helper.LayoutHelper
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

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fragmentContainerView: FragmentContainerView

    @SuppressLint("InflateParams")
    override fun createViews(savedInstanceState: Bundle?) {
        root = CoordinatorLayout(this)

        mainLayout = (root as CoordinatorLayout).apply {
            defaultAppearance(baseColor, false)
        }

        appBarLayout = AppBarLayout(this).apply {
            id = R.id.main_app_bar
            fitsSystemWindows = true
            isLiftOnScroll = false

            statusBarForeground =
                MaterialShapeDrawable.createWithElevationOverlay(context)

            setBackgroundColor(baseColor.background)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                outlineAmbientShadowColor = baseColor.onBackground.withAlpha(0.52)
                outlineSpotShadowColor = baseColor.onBackground.withAlpha(0.52)
            }

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
                    56,
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                )
            )
        }

        fragmentContainerView = (layoutInflater.inflate(
            R.layout.activity_container_fragment,
            null
        ) as FragmentContainerView).apply {
            mainLayout.addView(
                this,
                layoutHelper.createCoordinator(
                    LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,
                    behavior = ScrollingViewWithBottomNavigationBehavior()
                )
            )
            applyPaddingWindowInsets(applyBottom = true)
        }

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
            itemPaddingTop = 0
            itemPaddingBottom = 0
            elevation = 0f
            setPadding(0)

            itemIconSize = 26.toDp(context)

            minimumHeight = 56.toDp(context)
            mainLayout.addView(
                this,
                layoutHelper.createCoordinator(
                    LayoutHelper.MATCH_PARENT,
                    LayoutHelper.WRAP_CONTENT,
                    gravity = Gravity.BOTTOM,
                    insetEdge = Gravity.BOTTOM
                )
            )
            applyMarginWindowInsets(applyBottom = true)
        }

    }

    override fun setup(savedInstanceState: Bundle?) {
        Log.i(TrendFragment.TAG, "activity setup: ")
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
            fragment<SearchFragment>(NavRoutes.Main.SEARCH_FRAGMENT).apply {
                label = resources.getString(R.string.search_fragment)
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
            navController.graph[NavRoutes.Main.SEARCH_FRAGMENT].id,
            navController.graph[NavRoutes.Main.NEWS_FRAGMENT].id,
            navController.graph[NavRoutes.Main.MENU_FRAGMENT].id
        )
        val menuTitles =
            listOf(R.string.trend, R.string.movie, R.string.search, R.string.news, R.string.menu)
        val menuIcons = listOf(
            R.drawable.animated_trend,
            R.drawable.animated_movie,
            R.drawable.animated_search,
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
                        isSearchMenuVisible = false
                    ) {}
                }
                NavRoutes.Main.MOVIE_FRAGMENT -> {
                    setupToolbarWithNavController(
                        title = resources.getString(R.string.movie),
                        isSearchMenuVisible = false
                    ) {}
                }
                NavRoutes.Main.SEARCH_FRAGMENT -> {
                    setupToolbarWithNavController(
                        title = resources.getString(R.string.search),
                        searchQueryHint = "Search all movie/series ..."
                    ) {}
                }
                NavRoutes.Main.NEWS_FRAGMENT -> {
                    setupToolbarWithNavController(
                        title = resources.getString(R.string.news),
                        isSearchMenuVisible = false
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
        isSearchMenuVisible: Boolean = true,
        searchQueryHint: String = "",
        onQueryTextChanged: ((String) -> Unit)? = null
    ) {
        toolbar.title = title
//        appBarLayout.setExpanded(appBarExpand, true)

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

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TrendFragment.TAG, "activity onDestroy: ")
    }
}
package com.example.seeamo.core.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.INotificationSideChannel
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.example.seeamo.R
import com.example.seeamo.core.data.ApiService
import com.example.seeamo.core.data.model.MovieDetail
import com.example.seeamo.core.data.model.UIState
import com.example.seeamo.core.utilize.base.BaseFragment
import com.example.seeamo.core.utilize.extensions.collectNotNull
import com.example.seeamo.core.utilize.extensions.defaultAppearance
import com.example.seeamo.core.utilize.extensions.repeatViewLifecycle
import com.example.seeamo.core.utilize.extensions.toDp
import com.example.seeamo.core.utilize.extensions.toast
import com.example.seeamo.core.utilize.extensions.withAlpha
import com.example.seeamo.core.utilize.helper.ImageHelper
import com.example.seeamo.core.utilize.helper.LayoutHelper
import com.example.seeamo.core.utilize.navigation.NavArguments
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class MovieDetailFragment : BaseFragment(false) {

    private lateinit var mainLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var toolbarBackgroundImageView: ImageView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var progressBar: ProgressBar
    private val contentScrollView: NestedScrollView by lazy {
        NestedScrollView(requireContext()).apply {
            id = R.id.movie_detail_fragment_content_scroll_view
            visibility = View.INVISIBLE
            mainLayout.addView(
                this,
                layoutHelper.createCoordinator(
                    LayoutHelper.MATCH_PARENT,
                    LayoutHelper.MATCH_PARENT,
                    behavior = AppBarLayout.ScrollingViewBehavior()
                )
            )


            val descTextView = TextView(context).apply {
                text = resources.getString(R.string.movie_desc_title)
            }
            addView(
                descTextView,
                layoutHelper.createFrame(
                    width = LayoutHelper.MATCH_PARENT,
                    height = LayoutHelper.MATCH_PARENT,
                    startMargin = 8.toDp(context),
                    topMargin = 8.toDp(context)
                )
            )
        }
    }

    private var movieId by Delegates.notNull<Int>()
    private lateinit var title: String
    private lateinit var backgroundPath: String

    private val coreViewModel: CoreViewModel by viewModels()

    override fun createViews(savedInstanceState: Bundle?) {
        root = CoordinatorLayout(requireContext())
        mainLayout = (root as CoordinatorLayout).apply {
            defaultAppearance(baseColor)
        }

        appBarLayout = AppBarLayout(requireContext()).apply {
            id = R.id.movie_detail_fragment_app_bar
            fitsSystemWindows = true
//            isLiftOnScroll = true

            setBackgroundColor(baseColor.background)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                outlineAmbientShadowColor = baseColor.onBackground.withAlpha(0.64)
                outlineSpotShadowColor = baseColor.onBackground.withAlpha(0.64)
            }

            mainLayout.addView(
                this, LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT
            )
//            applyMarginWindowInsets(applyTop = true)
        }

        collapsingToolbarLayout = CollapsingToolbarLayout(requireContext()).apply {
            id = R.id.movie_detail_fragment_collapsing_toolbar
            collapsedTitleGravity = Gravity.START

            setCollapsedTitleTextColor(baseColor.baseColorStateList(baseColor.background))
            setExpandedTitleTextColor(baseColor.baseColorStateList(baseColor.background))

            setContentScrimColor(baseColor.red)

            appBarLayout.addView(
                this, layoutHelper.createAppBarLayout(
                    LayoutHelper.MATCH_PARENT,
                    250,
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                            or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                            or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                )
            )
        }

        toolbarBackgroundImageView = ImageView(requireContext()).apply {
            id = R.id.movie_detail_fragment_collapsing_toolbar_background_image
            fitsSystemWindows = true

            collapsingToolbarLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
            )
        }

        toolbar = MaterialToolbar(requireContext()).apply {
            id = R.id.movie_detail_fragment_toolbar

            navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_round_arrow_back_24)
            setNavigationIconTint(baseColor.background)

            collapsingToolbarLayout.addView(
                this,
                layoutHelper.createCollapsingToolbarLayout(
                    LayoutHelper.MATCH_PARENT,
                    56,
                    collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
                )
            )

        }

        progressBar = ProgressBar(requireContext()).apply {
            id = R.id.movie_detail_fragment_content_progress_bar
            visibility = View.VISIBLE
            indeterminateTintList = baseColor.baseColorStateList(baseColor.onBackground)
            mainLayout.addView(
                this,
                layoutHelper.createCoordinator(
                    64,
                    64,
                    gravity = Gravity.CENTER
                )
            )
        }
    }

    override fun setup(savedInstanceState: Bundle?) {
//        if (findNavController().previousBackStackEntry?.destination?.route == NavRoutes.Main.TREND_FRAGMENT) {
//            val trendFragment =
//                (activity as MainActivity).navHostFragment.childFragmentManager.fragments
//            logDebug { "The list is  $trendFragment" }
//        }
//        setStatusBarAppearance(backToDefault = true)

        val titleAndBackground =
            arguments?.getString(NavArguments.MOVIE_DETAIL_ID_TITLE_BACKGROUND)?.split("|")
        movieId = titleAndBackground?.get(0)?.toInt() ?: 0
        title = titleAndBackground?.get(1) ?: "No title"
        backgroundPath = Uri.decode(titleAndBackground?.get(2))

        setToolbarData()

        coreViewModel.getMovieDetail(id = movieId)
        collectContentData()
    }

    private fun setToolbarData() {
        ImageHelper.loadUriTo(
            toolbarBackgroundImageView,
            backgroundPath.toUri(),
            transformation = CenterCrop()
        )

        toolbar.apply {
            title = this@MovieDetailFragment.title

            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun collectContentData() = repeatViewLifecycle {
        coreViewModel.movieDetail.collectNotNull { uiState ->
            when (uiState.uiState) {
                UIState.NONE -> {
                    progressBar.visibility = View.VISIBLE
                    contentScrollView.visibility = View.INVISIBLE
                }

                UIState.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    contentScrollView.visibility = View.INVISIBLE
                }

                UIState.SUCCEED -> {
                    progressBar.visibility = View.GONE
                    contentScrollView.visibility = View.VISIBLE
                    val detail = uiState.detail ?: return@collectNotNull
                    bindContentData(detail = detail)
                }

                UIState.FAILED -> {
                    progressBar.visibility = View.GONE
                    context?.toast(uiState.failureMessage)
                    contentScrollView.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun bindContentData(detail: MovieDetail) {
        val textViewDescription = contentScrollView.getChildAt(0) as TextView
        textViewDescription.text = detail.overview
    }
}
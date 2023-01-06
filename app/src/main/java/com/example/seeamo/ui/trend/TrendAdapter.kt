package com.example.seeamo.ui.trend

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.example.seeamo.data.model.TrendResult
import com.example.seeamo.utilize.base.BaseColor
import com.example.seeamo.utilize.extensions.toDp
import com.example.seeamo.utilize.extensions.toThemeResourceId
import com.example.seeamo.utilize.helper.ImageHelper
import com.example.seeamo.utilize.helper.LayoutHelper

class TrendAdapter(
    private val baseColor: BaseColor,
    private val layoutHelper: LayoutHelper
) :
    PagingDataAdapter<TrendResult, TrendAdapter.ViewHolder>(diffCallback = TrendResult.DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { trendResult ->
            holder.bind(trendResult)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val constraintLayout = ConstraintLayout(context).apply {
            layoutParams =
                layoutHelper.createRecycler(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT)
            setPadding(8.toDp(context), 4.toDp(context), 8.toDp(context), 4.toDp(context))
            isClickable = true
            isFocusable = true
            setBackgroundResource(context.toThemeResourceId(android.R.attr.selectableItemBackground))
        }

        return ViewHolder(constraintLayout)
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent) {
        private val context: Context
        private val mainLayout: ConstraintLayout
        private val imageView: ImageView

        init {
            context = parent.context
            mainLayout = (parent as ConstraintLayout)

            imageView = ImageView(context).apply {
                mainLayout.addView(
                    this,
                    LayoutHelper.MATCH_PARENT,
                    250.toDp(context)
                )
            }
        }

        fun bind(trendResult: TrendResult) {
            ImageHelper.loadUriTo(
                imageView,
                trendResult.fullBackdropPath.toUri(),
                cornerSize = 8.toDp(context),
                transformation = FitCenter()
            )
        }
    }
}

// Loading State Adapter
class TrendLoadStateAdapter(
    private val baseColor: BaseColor,
    private val layoutHelper: LayoutHelper,
    private val retry: () -> Unit
) :
    LoadStateAdapter<TrendLoadStateAdapter.ViewHolder>() {

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent) {
        private val context: Context
        private val mainLayout: RelativeLayout
        val progressBar: ProgressBar

        init {
            context = parent.context
            mainLayout = (parent as RelativeLayout)

            progressBar = ProgressBar(context).apply {
                indeterminateTintList = baseColor.baseColorStateList(baseColor.onBackground)
                isFocusable = true
                isClickable = true

                mainLayout.addView(
                    this,
                    layoutHelper.createRelative(
                        12.toDp(context),
                        12.toDp(context),
                        alignParent = RelativeLayout.CENTER_IN_PARENT
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.progressBar.apply {
            isVisible = loadState is LoadState.Loading

            setOnClickListener {
                retry.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val context = parent.context
        val relativeLayout = RelativeLayout(context).apply {
            layoutParams = layoutHelper.createRecycler(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT,
                margin = 8.toDp(context)
            )
            isClickable = true
            isFocusable = true
            setBackgroundResource(context.toThemeResourceId(android.R.attr.selectableItemBackground))
        }

        return ViewHolder(relativeLayout)
    }
}
package com.example.seeamo.ui.trend

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.example.seeamo.R
import com.example.seeamo.data.model.TrendResult
import com.example.seeamo.data.model.UIState
import com.example.seeamo.utilize.base.BaseColor
import com.example.seeamo.utilize.extensions.*
import com.example.seeamo.utilize.helper.DrawableHelper
import com.example.seeamo.utilize.helper.ImageHelper
import com.example.seeamo.utilize.helper.LayoutHelper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TrendAdapter(
    private val baseColor: BaseColor,
    private val layoutHelper: LayoutHelper,
    private val trendViewModel: TrendViewModel,
    private val playerHolderEventListener: PlayerHolderEventListener
) : PagingDataAdapter<TrendResult, TrendViewHolder>(diffCallback = TrendResult.DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        val trendResult = getItem(position) ?: return

        playerHolderEventListener.onBindViewHolderToWindow(holder, position)

        holder.bind(trendResult)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val context = parent.context
        val constraintLayout = FrameLayout(context).apply {
            layoutParams =
                layoutHelper.createRecycler(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT)
            setPadding(8.toDp(context), 4.toDp(context), 8.toDp(context), 4.toDp(context))
//            isClickable = true
//            isFocusable = true
//            setBackgroundResource(context.toThemeResourceId(android.R.attr.selectableItemBackground))
        }

        return TrendViewHolder(
            constraintLayout,
            layoutHelper,
            baseColor,
            trendViewModel,
            playerHolderEventListener
        )
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
            private val progressBar: ProgressBar
            private val errorTextView: TextView

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

                errorTextView = TextView(context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(baseColor.onBackground)
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true

                    mainLayout.addView(
                        this,
                        layoutHelper.createRelative(
                            LayoutHelper.MATCH_PARENT,
                            LayoutHelper.WRAP_CONTENT,
                            alignParent = RelativeLayout.CENTER_IN_PARENT,
                            startMargin = 16.toDp(context),
                            endMargin = 16.toDp(context)
                        )
                    )
                }
            }

            fun bindState(loadState: LoadState) {
                progressBar.isVisible = loadState is LoadState.Loading

                errorTextView.isVisible = loadState is LoadState.Error
                if (loadState is LoadState.Error)
                    errorTextView.apply {
                        text = loadState.error.message

                        setOnClickListener { retry.invoke() }
                    }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
            holder.bindState(loadState)
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
}


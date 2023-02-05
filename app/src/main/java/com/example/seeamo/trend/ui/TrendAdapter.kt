package com.example.seeamo.trend.ui

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.seeamo.data.model.TrendResult
import com.example.seeamo.core.utilize.base.BaseColor
import com.example.seeamo.core.utilize.extensions.toDp
import com.example.seeamo.core.utilize.extensions.toThemeResourceId
import com.example.seeamo.core.utilize.helper.LayoutHelper

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


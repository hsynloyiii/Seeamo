package com.example.seeamo.data.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrendResponse(
    val page: Int,
    val results: List<TrendResult>,
    val dates: TrendDate,
    val total_pages: Int,
    val total_results: Int
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "trendResult")
data class TrendResult(
    val id: Int,
    @PrimaryKey(autoGenerate = false)
    val original_title: String,
    val backdrop_path: String
) {
    var full_backdrop_path = "https://image.tmdb.org/t/p/w500$backdrop_path"

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrendResult>() {
            override fun areItemsTheSame(oldItem: TrendResult, newItem: TrendResult): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: TrendResult, newItem: TrendResult): Boolean =
                oldItem == newItem

        }
    }
}

@JsonClass(generateAdapter = true)
data class TrendDate(
    val maximum: String,
    val minimum: String
)

@Entity(tableName = "trendRemoteKey")
data class TrendRemoteKey(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val prev: Int?,
    val next: Int?
)
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
    val poster_path: String?
) {
    var fullBackdropPath: String = if (poster_path != null)
        "https://image.tmdb.org/t/p/w500$poster_path"
    else
        "https://static.vecteezy.com/system/resources/previews/004/141/669/original/no-photo-or-blank-image-icon-loading-images-or-missing-image-mark-image-not-available-or-image-coming-soon-sign-simple-nature-silhouette-in-frame-isolated-illustration-vector.jpg"

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
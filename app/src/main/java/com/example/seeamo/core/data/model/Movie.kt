package com.example.seeamo.core.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieDetail(
    val id: Int,
    val original_title: String,
    val backdrop_path: String,
    val overview: String
)

data class MovieDetailUIState(
    val uiState: UIState,
    val detail: MovieDetail? = null,
    val failureMessage: String = ""
)
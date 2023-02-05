package com.example.seeamo.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseError(
    val errors: List<Error>?
)

@Serializable
data class Error(
    val field: String,
    val message: String
)
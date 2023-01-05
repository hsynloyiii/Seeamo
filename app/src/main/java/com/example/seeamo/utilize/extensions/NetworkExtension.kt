package com.example.seeamo.utilize.extensions

import com.example.seeamo.data.model.ResponseError
import com.example.seeamo.data.source.network.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Response


suspend inline fun <T> requestResource(
    crossinline succeedData: suspend () -> T
): Resource<T> {
    return try {
        Resource.Success(data = succeedData())
    } catch (e: Exception) {
        Resource.Error(e)
    }
}

suspend inline fun <reified T> Flow<Resource<T>>.collectResources(
    crossinline onSuccess: (data: T) -> Unit,
    crossinline onFailure: (exception: Exception) -> Unit
) {
    collect {
        when (it) {
            is Resource.Success -> onSuccess(it.data as T)
            is Resource.Error -> onFailure(it.exception ?: throw IllegalAccessException())
        }
    }
}

suspend inline fun <reified T> Flow<T?>.collectNotNull(
    crossinline body: suspend (T) -> Unit
) {
    collect {
        if (it != null)
            body(it)
    }
}

suspend inline fun <reified T, H> Resource<T>.getByState(
    crossinline onSuccess: suspend (data: T) -> H,
    crossinline onFailure: suspend (exception: Exception) -> H
): H = when (this) {
    is Resource.Success -> onSuccess(this.data as T)
    is Resource.Error -> onFailure(this.exception ?: throw IllegalAccessException())
}

fun <T> ResponseBody?.toErrorResponse(
    errors: (List<com.example.seeamo.data.model.Error>?) -> T
): T {
    return errors(
        Json.decodeFromString<ResponseError>(
            this?.string() ?: ""
        ).errors
    )
}

suspend fun <T, H> Response<T>.getByResponseCode(
    succeed: suspend (T) -> H,
    failed: (List<com.example.seeamo.data.model.Error>?) -> H
): H? {
    return if (isSuccessful)
        body()?.let { succeed(it) }
    else
        errorBody().toErrorResponse {
            failed(it)
        }
}
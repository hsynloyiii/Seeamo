package com.example.seeamo.core.data

sealed class Resource<T>(
    val data: T? = null,
    val exception: Exception? = null
) {
    class Success<T>(data: T) : Resource<T>(data = data)
    class Error<T>(exception: Exception, data: T? = null) : Resource<T>(data = data, exception = exception)
}

//fun <T> handleException(e: Exception): Resource.Error<T> = when (e) {
//    is HttpException -> Resource.Error(message = getErrorMessage(e.code()), null)
//    is SocketTimeoutException -> Resource.Error(message = "timeout", null)
//    is UnknownHostException -> Resource.Error(message = "Unable to resolve host name")
//    else -> Resource.Error(message = getErrorMessage(Int.MAX_VALUE), null)
//}
//
//private fun getErrorMessage(code: Int): String = when (code) {
//    401 -> "Unauthorised"
//    404 -> "Not found"
//    else -> "Something went wrong $code"
//}


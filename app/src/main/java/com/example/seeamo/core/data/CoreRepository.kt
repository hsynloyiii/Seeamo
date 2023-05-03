package com.example.seeamo.core.data

import com.example.seeamo.core.data.model.MovieDetail
import com.example.seeamo.core.utilize.extensions.requestResource
import javax.inject.Inject

class CoreRepository @Inject constructor(
    private val apiService: ApiService.Core
): Repository.Core {
    override suspend fun getMovieDetail(id: Int): Resource<MovieDetail> = requestResource {
        apiService.geMovieDetail(id = id, api_key = "b8dd19559ebbbadf3c31009fa3f093cb")
    }
}
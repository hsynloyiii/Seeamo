package com.example.seeamo.trend.data

import com.example.seeamo.core.data.Repository
import com.example.seeamo.core.data.ApiService
import com.example.seeamo.core.data.Resource
import com.example.seeamo.core.utilize.extensions.requestResource
import javax.inject.Inject

class TrendRepository @Inject constructor(
    private val apiService: ApiService.Trend
): Repository.Trend {

    override suspend fun getTrendMovie(page: Int): Resource<TrendResponse> = requestResource {
        apiService.getTrendMovie("b8dd19559ebbbadf3c31009fa3f093cb", page)
    }

    override suspend fun getTrendTrailer(id: Int): Resource<TrendTrailerResponse> = requestResource {
        apiService.getTrendTrailer(id, "b8dd19559ebbbadf3c31009fa3f093cb")
    }

}
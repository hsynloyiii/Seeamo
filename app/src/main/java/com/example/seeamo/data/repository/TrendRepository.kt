package com.example.seeamo.data.repository

import com.example.seeamo.data.model.TrendResponse
import com.example.seeamo.data.model.TrendTrailerResponse
import com.example.seeamo.data.source.network.ApiService
import com.example.seeamo.data.source.network.Resource
import com.example.seeamo.utilize.extensions.requestResource
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
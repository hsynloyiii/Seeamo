package com.example.seeamo.core.data

import com.example.seeamo.data.model.TrendResponse
import com.example.seeamo.data.model.TrendTrailerResponse

interface Repository {

    interface Trend {
        suspend fun getTrendMovie(page: Int): Resource<TrendResponse>
        suspend fun getTrendTrailer(id: Int): Resource<TrendTrailerResponse>
    }
}
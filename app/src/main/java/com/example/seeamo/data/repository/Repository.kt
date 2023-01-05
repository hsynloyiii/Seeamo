package com.example.seeamo.data.repository

import com.example.seeamo.data.model.TrendResponse
import com.example.seeamo.data.source.network.Resource

interface Repository {

    interface Trend {
        suspend fun getTrendMovie(page: Int): Resource<TrendResponse>
    }
}
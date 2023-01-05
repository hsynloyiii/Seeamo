package com.example.seeamo.data.source.network

import com.example.seeamo.data.model.TrendResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    interface Trend {
        @GET("3/movie/now_playing")
        suspend fun getTrendMovie(
            @Query("api_key") api_key: String,
            @Query("page") page: Int
        ): TrendResponse
    }

}
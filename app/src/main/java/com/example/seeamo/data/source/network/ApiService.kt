package com.example.seeamo.data.source.network

import com.example.seeamo.data.model.TrendResponse
import com.example.seeamo.data.model.TrendTrailerResponse
import retrofit2.http.GET
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    interface Trend {
        @GET("3/movie/now_playing")
        suspend fun getTrendMovie(
            @Query("api_key") api_key: String,
            @Query("page") page: Int
        ): TrendResponse

        @GET("3/movie/{movie_id}/videos")
        suspend fun getTrendTrailer(
            @Path("movie_id") id: Int,
            @Query("api_key") api_key: String
        ): TrendTrailerResponse
    }

}
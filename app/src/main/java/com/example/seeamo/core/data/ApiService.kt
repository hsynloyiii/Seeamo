package com.example.seeamo.core.data

import com.example.seeamo.core.data.model.MovieDetail
import com.example.seeamo.trend.data.TrendResponse
import com.example.seeamo.trend.data.TrendTrailerResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    interface Core {
        @GET("movie/{movie_id}")
        suspend fun geMovieDetail(
            @Path("movie_id") id: Int,
            @Query("api_key") api_key: String
        ): MovieDetail
    }

    interface Trend {
        @GET("movie/now_playing")
        suspend fun getTrendMovie(
            @Query("api_key") api_key: String,
            @Query("page") page: Int
        ): TrendResponse

        @GET("movie/{movie_id}/videos")
        suspend fun getTrendTrailer(
            @Path("movie_id") id: Int,
            @Query("api_key") api_key: String
        ): TrendTrailerResponse
    }

}
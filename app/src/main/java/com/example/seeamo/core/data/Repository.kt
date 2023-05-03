package com.example.seeamo.core.data

import com.example.seeamo.core.data.model.MovieDetail
import com.example.seeamo.trend.data.TrendResponse
import com.example.seeamo.trend.data.TrendTrailerResponse

interface Repository {

    interface Core {
        suspend fun getMovieDetail(id: Int): Resource<MovieDetail>
    }
    interface Trend {
        suspend fun getTrendMovie(page: Int): Resource<TrendResponse>
        suspend fun getTrendTrailer(id: Int): Resource<TrendTrailerResponse>
    }
}
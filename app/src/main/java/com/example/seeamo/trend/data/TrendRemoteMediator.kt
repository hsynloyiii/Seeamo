package com.example.seeamo.trend.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.seeamo.core.data.source.MovieDao
import com.example.seeamo.core.data.source.MovieDatabase
import com.example.seeamo.data.model.TrendRemoteKey
import com.example.seeamo.data.model.TrendResult
import com.example.seeamo.trend.ui.TrendFragment
import com.example.seeamo.core.utilize.extensions.getByState
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class TrendRemoteMediator(
    private val movieDatabase: MovieDatabase,
    private val movieDao: MovieDao,
    private val trendRepository: TrendRepository,
    private val initialPage: Int = 1
) : RemoteMediator<Int, TrendResult>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TrendResult>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKey = getClosestKey(state)
                    remoteKey?.next?.minus(1) ?: initialPage
                }
                LoadType.PREPEND -> {
                    val remoteKey = getFirstKey(state)
                    val prevPage = remoteKey?.prev ?: return MediatorResult.Success(
                        endOfPaginationReached = remoteKey != null
                    )
                    prevPage
                }
                LoadType.APPEND -> {
                    val remoteKey =
                        getLastKey(state)
                    val nextPage = remoteKey?.next ?: return MediatorResult.Success(
                        endOfPaginationReached = remoteKey != null
                    )
                    nextPage
                }
            }

            trendRepository.getTrendMovie(page = page).getByState(
                onSuccess = { trendResponse ->
                    val endOfPagination = trendResponse.results.isEmpty()

                    movieDatabase.withTransaction {
                        val prev = if (page == 1) null else page - 1
                        val next = if (endOfPagination) null else {
                            delay(1500)
                            page + 1
                        }

                        if (loadType == LoadType.REFRESH) {
                            movieDao.clearAllTrends()
                            movieDao.deleteAllTrendRemoteKey()
                        }

                        // create a list of remote keys
                        val trendRemoteKey = trendResponse.results.map { tr ->
                            TrendRemoteKey(
                                id = tr.original_title,
                                prev = prev,
                                next = next
                            )
                        }

                        movieDao.insertAllTrendRemoteKeys(trendRemoteKey)
                        val validatedMovies = trendResponse.results.filter {
                            it.poster_path != null || it.backdrop_path != null
                        }
                        movieDao.insertAllTrend(validatedMovies)
                    }

                    MediatorResult.Success(endOfPaginationReached = endOfPagination)
                },
                onFailure = { e ->
                    Log.i(TrendFragment.TAG, "${e.message}")
                    MediatorResult.Error(e)
                }
            )
        } catch (e: IOException) {
            Log.i("ErrorRemoting", e.message.toString())
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getClosestKey(state: PagingState<Int, TrendResult>): TrendRemoteKey? {
        return state.anchorPosition?.let {
            state.closestItemToPosition(anchorPosition = it)?.let { trendResult ->
                movieDatabase.withTransaction {
                    movieDao.getAllTrendRemoteKey(title = trendResult.original_title)
                }
            }
        }
    }

    private suspend fun getFirstKey(state: PagingState<Int, TrendResult>): TrendRemoteKey? {
        return state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let { trendResult ->
            movieDatabase.withTransaction {
                movieDao.getAllTrendRemoteKey(title = trendResult.original_title)
            }
        }
    }

    private suspend fun getLastKey(state: PagingState<Int, TrendResult>): TrendRemoteKey? {
        return state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { trendResult ->
            movieDatabase.withTransaction {
                movieDao.getAllTrendRemoteKey(title = trendResult.original_title)
            }
        }
    }
}
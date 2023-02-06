package com.example.seeamo.trend.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.example.seeamo.data.model.TrendTrailerUIState
import com.example.seeamo.data.model.UIState
import com.example.seeamo.trend.data.TrendRepository
import com.example.seeamo.trend.data.TrendRemoteMediator
import com.example.seeamo.core.data.source.MovieDao
import com.example.seeamo.core.data.source.MovieDatabase
import com.example.seeamo.core.di.IODispatchers
import com.example.seeamo.core.utilize.extensions.getByState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume


@OptIn(ExperimentalPagingApi::class)
@HiltViewModel
class TrendViewModel @Inject constructor(
    private val saveStateHandle: SavedStateHandle,
    @IODispatchers private val ioDispatchers: CoroutineDispatcher,
    movieDatabase: MovieDatabase,
    private val movieDao: MovieDao,
    private val trendRepository: TrendRepository
) : ViewModel() {

    val trendResult = Pager(
        config = PagingConfig(20, enablePlaceholders = false),
        remoteMediator = TrendRemoteMediator(movieDatabase, movieDao, trendRepository)
    ) {
        movieDao.trends()
    }.flow.cachedIn(viewModelScope)


    fun getTrendTrailer(context: Context, id: Int) = flow {
        val uiState = TrendTrailerUIState(UIState.NONE)
        emit(uiState.copy(uiState = UIState.LOADING))

        trendRepository.getTrendTrailer(id).getByState(
            onSuccess = { response ->
                val trailerResult = response.results.firstOrNull { it.type == "Trailer" }
                if (trailerResult != null) {
                    val youtubeUrl = "https://www.youtube.com/watch?v=${trailerResult.key}"
                    val trailerUrl = extractVideoUrlFromYoutube(context, youtubeUrl)
                    val updatedUIState = uiState.copy(
                        uiState = UIState.SUCCEED,
                        trailerUrl = trailerUrl,
                        published_at = trailerResult.published_at
                    )
                    setTrendTrailerUIState(updatedUIState)
                    emit(updatedUIState)
                } else
                    emit(
                        uiState.copy(
                            uiState = UIState.FAILED,
                            failure_message = "There's no trailer associated with the movie"
                        )
                    )
            },
            onFailure = { e ->
                emit(
                    uiState.copy(
                        uiState = UIState.FAILED,
                        failure_message = e.message ?: "Failed to retrieve exception message"
                    )
                )
            }
        )
    }.flowOn(ioDispatchers)

    val getSavedTrendTrailerUIState: StateFlow<TrendTrailerUIState?> =
        saveStateHandle.getStateFlow(TREND_TRAILER_UI_STATE_KEY, null)

    private fun setTrendTrailerUIState(trendTrailerUIState: TrendTrailerUIState) {
        saveStateHandle[TREND_TRAILER_UI_STATE_KEY] = trendTrailerUIState
    }

    @SuppressLint("StaticFieldLeak")
    private suspend fun extractVideoUrlFromYoutube(context: Context, youtubeUrl: String): String =
        suspendCancellableCoroutine {
            object : YouTubeExtractor(context) {
                override fun onExtractionComplete(
                    ytFiles: SparseArray<YtFile>?,
                    vMeta: VideoMeta?
                ) {
                    if (ytFiles != null) {
                        val tag = 22
                        val downloadUrl = ytFiles[tag].url
                        it.resume(downloadUrl)
                    }
                }
            }.extract(youtubeUrl)
        }

    companion object {
        private const val TREND_TRAILER_UI_STATE_KEY = "trend_trailer_ui_state"
    }

}
package com.example.seeamo.ui.trend

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Contacts.Intents.UI
import android.util.Log
import android.util.SparseArray
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
import com.example.seeamo.data.repository.TrendRepository
import com.example.seeamo.data.source.TrendRemoteMediator
import com.example.seeamo.data.source.db.MovieDao
import com.example.seeamo.data.source.db.MovieDatabase
import com.example.seeamo.di.IODispatchers
import com.example.seeamo.utilize.extensions.getByState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume


@OptIn(ExperimentalPagingApi::class)
@HiltViewModel
class TrendViewModel @Inject constructor(
    @IODispatchers private val ioDispatchers: CoroutineDispatcher,
    movieDatabase: MovieDatabase,
    private val movieDao: MovieDao,
    private val trendRepository: TrendRepository
) : ViewModel() {

    val trendResult = Pager(
        config = PagingConfig(10),
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
                    Log.i(TrendFragment.TAG, "getTrendTrailer: $youtubeUrl")
                    val trailerUrl = extractVideoUrlFromYoutube(context, youtubeUrl)
                    emit(
                        uiState.copy(
                            uiState = UIState.SUCCEED,
                            trailerUrl = trailerUrl,
                            published_at = trailerResult.published_at
                        )
                    )
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

}
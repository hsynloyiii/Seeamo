package com.example.seeamo.trend.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.util.SparseArray
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media.AudioAttributesCompat
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
import com.example.seeamo.core.utilize.helper.ExoAudioFocusHelper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.Listener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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


    fun getTrendTrailer(id: Int, context: Context) = flow {
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
//                    setTrendTrailerUIState(updatedUIState)
                    savedTrendTrailerUIState = updatedUIState
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

    var savedTrendTrailerUIState: TrendTrailerUIState?
        get() = saveStateHandle[TREND_TRAILER_UI_STATE_KEY]
        private set(value) = saveStateHandle.set(TREND_TRAILER_UI_STATE_KEY, value)


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

    // ExoPlayer
    var exoPlayer: ExoPlayer? = null

    private lateinit var audioManager: AudioManager

    var lastPlayedItemListPosition: Int?
        get() = saveStateHandle[LAST_PLAYED_ITEM_LIST_POSITION_STATE_KEY]
        private set(value) = saveStateHandle.set(LAST_PLAYED_ITEM_LIST_POSITION_STATE_KEY, value)

    var lastItemPlayerPosition: Long?
        get() = saveStateHandle[LAST_ITEM_VIDEO_POSITION_STATE_KEY]
        private set(value) = saveStateHandle.set(LAST_ITEM_VIDEO_POSITION_STATE_KEY, value)

    fun setupPlayer(
        context: Context,
        listener: Listener,
        shouldStartPlayer: Boolean = false,
        mediaUrl: String? = null
    ) {
        if (exoPlayer != null)
            return

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributesCompat.Builder()
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .build()
        exoPlayer =
            ExoAudioFocusHelper(
                ExoPlayer.Builder(context).build(),
                audioManager,
                audioAttributes
            ).apply {
                addListener(listener)
            }

        if (shouldStartPlayer)
            startPlayer(mediaUrl = mediaUrl)
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    fun startPlayer(mediaUrl: String?) {
        if (exoPlayer == null && mediaUrl == null)
            return

        with(exoPlayer!!) {
            clearMediaItems()
            setMediaItem(MediaItem.fromUri(mediaUrl!!))
            seekTo(lastItemPlayerPosition ?: 0)
            prepare()
            playWhenReady = true
        }
    }

    fun removePlayerListener(listener: Listener) {
        if (exoPlayer == null)
            return
        exoPlayer!!.removeListener(listener)
    }

    fun addPlayerListener(listener: Listener) {
        if (exoPlayer == null)
            return
        exoPlayer!!.addListener(listener)
    }

    fun playPlayer() {
        if (exoPlayer == null)
            return
        exoPlayer!!.play()
    }

    fun pausePlayer() {
        if (exoPlayer == null)
            return
        exoPlayer!!.pause()
    }

    fun isPlayerPlaying(): Boolean = if (exoPlayer == null) false else exoPlayer!!.isPlaying

    fun setLastPlayedItemListPosition(position: Int) {
        lastPlayedItemListPosition = position
    }

    fun savePlayerCurrentPosition() {
        if (exoPlayer != null)
            lastItemPlayerPosition = exoPlayer?.currentPosition
    }

    fun setupSessionAndController() {
//        if (this::mediaSession.isInitialized ||
//            this::mediaSessionConnector.isInitialized ||
//            this::mediaController.isInitialized
//        )
//            return
//
//        mediaSession =
//            MediaSessionCompat(requireContext(), TrendFragment.TAG).apply { setMediaButtonReceiver(null) }
//        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
//            setPlayer(trendViewModel.exoPlayer)
//        }
//
//        while (activity == null)
//            return
//
//        mediaController =
//            MediaControllerCompat(context, mediaSession.sessionToken).also { mediaController ->
//                MediaControllerCompat.setMediaController(requireActivity(), mediaController)
//            }
    }

    companion object {
        private const val TREND_TRAILER_UI_STATE_KEY = "trend_trailer_ui_state"
        private const val LAST_PLAYED_ITEM_LIST_POSITION_STATE_KEY = "last_played_item_list_state"
        private const val LAST_ITEM_VIDEO_POSITION_STATE_KEY = "last_item_video_position_state"
    }

}
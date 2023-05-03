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
import com.example.seeamo.core.data.model.UIState
import com.example.seeamo.trend.data.TrendTrailerUIState
import com.example.seeamo.trend.data.TrendRepository
import com.example.seeamo.trend.data.TrendRemoteMediator
import com.example.seeamo.core.data.source.MovieDao
import com.example.seeamo.core.data.source.MovieDatabase
import com.example.seeamo.core.di.IODispatchers
import com.example.seeamo.core.utilize.extensions.getByState
import com.example.seeamo.core.utilize.helper.ExoAudioFocusHelper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.Listener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    var player: ExoPlayer? = null
    private lateinit var audioManager: AudioManager
    private var defaultVideoVolume = 1f
    var isPlayerMuted = false

    enum class PlayerViewState {
        IS_IN_THUMB_MODE,
        IS_IN_ZOOM_MODE
    }
    private val _playerViewState: MutableStateFlow<PlayerViewState?> = MutableStateFlow(null)
    val playerViewState: StateFlow<PlayerViewState?>
        get() = _playerViewState.asStateFlow()
    fun setPlayerViewState(playerViewState: PlayerViewState) {
        _playerViewState.update { playerViewState }
    }

    var lastPlayedItemListPosition: Int?
        get() = saveStateHandle[LAST_PLAYED_ITEM_LIST_POSITION_STATE_KEY]
        private set(value) = saveStateHandle.set(LAST_PLAYED_ITEM_LIST_POSITION_STATE_KEY, value)

    fun setupPlayer(
        context: Context,
        listener: Listener,
        shouldStartPlayer: Boolean = false,
        mediaUrl: String? = null
    ) {
        if (player != null)
            return

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributesCompat.Builder()
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .build()
        player =
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
        player?.release()
        player = null
    }

    fun startPlayer(mediaUrl: String?) {
        if (player == null && mediaUrl == null)
            return

        with(player!!) {
            clearMediaItems()
            setMediaItem(MediaItem.fromUri(mediaUrl!!))
            seekTo(0)
            prepare()
            playWhenReady = true
            if (isPlayerMuted) {
                volume = 1f
                isPlayerMuted = false
            }
            defaultVideoVolume = volume
        }
    }

    fun removePlayerListener(listener: Listener) {
        player?.removeListener(listener)
    }

    fun addPlayerListener(listener: Listener) {
        player?.addListener(listener)
    }

    fun playPlayer() {
        player?.play()
    }

    fun pausePlayer() {
        player?.pause()
    }

    fun mutePlayer() {
        player?.volume = 0f
        isPlayerMuted = true
    }

    fun unMutePlayer() {
        player?.volume = defaultVideoVolume
        isPlayerMuted = false
    }

    fun playAgain() {
        player?.seekTo(0)
        playPlayer()
    }

    private var currentPositionJob: Job? = null
    fun startUpdatingCurrentPositionJob(invoke: (currentPosition: Long, duration: Long) -> Unit) {
        if (player == null && currentPositionJob != null)
            return
        currentPositionJob = viewModelScope.launch {
            while (isActive) {
                val currentPosition = player!!.currentPosition
                val duration = player!!.duration
                if (currentPosition > 0)
                    invoke(currentPosition, duration)
                delay(1000)
            }
        }
    }

    fun stopUpdatingCurrentPositionJob() {
        currentPositionJob?.cancel()
        currentPositionJob = null
    }

    fun isPlayerPlaying(): Boolean {
        return if (player == null) false else player!!.isPlaying
    }

    fun isPlayerPaused(): Boolean {
        return if (player == null) true else !player!!.isPlaying
    }

    fun isPlayerEnded(): Boolean {
        return if (player == null) false else player!!.playbackState == Player.STATE_ENDED
    }

    fun isPlayerBuffering(): Boolean {
        return if (player == null) false else player!!.playbackState == Player.STATE_BUFFERING
    }

    fun setLastPlayedItemListPosition(position: Int) {
        lastPlayedItemListPosition = position
    }


    companion object {
        private const val TREND_TRAILER_UI_STATE_KEY = "trend_trailer_ui_state"
        private const val LAST_PLAYED_ITEM_LIST_POSITION_STATE_KEY = "last_played_item_list_state"
    }

}
package com.example.seeamo.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seeamo.core.data.CoreRepository
import com.example.seeamo.core.data.model.MovieDetailUIState
import com.example.seeamo.core.data.model.UIState
import com.example.seeamo.core.di.IODispatchers
import com.example.seeamo.core.utilize.extensions.getByState
import com.example.seeamo.core.utilize.extensions.logDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoreViewModel @Inject constructor(
    @IODispatchers private val ioDispatchers: CoroutineDispatcher,
    private val coreRepository: CoreRepository
): ViewModel() {

    private val _movieDetail: MutableStateFlow<MovieDetailUIState?> = MutableStateFlow(null)
    val movieDetail: StateFlow<MovieDetailUIState?>
        get() = _movieDetail.asStateFlow()

    fun getMovieDetail(id: Int) = viewModelScope.launch {
        val uiState = MovieDetailUIState(UIState.NONE)
        _movieDetail.update { uiState.copy(uiState = UIState.LOADING) }

        coreRepository.getMovieDetail(id).getByState(
            onSuccess = { detail ->
                _movieDetail.update {
                    uiState.copy(
                        uiState = UIState.SUCCEED,
                        detail = detail
                    )
                }
            },
            onFailure = { e ->
                _movieDetail.update {
                    uiState.copy(
                        uiState = UIState.FAILED,
                        failureMessage = e.message ?: "Failed to retrieve exception message"
                    )
                }
            }
        )
    }

}
package com.example.seeamo.ui.trend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.seeamo.data.repository.TrendRepository
import com.example.seeamo.data.source.TrendRemoteMediator
import com.example.seeamo.data.source.db.MovieDao
import com.example.seeamo.data.source.db.MovieDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
@HiltViewModel
class TrendViewModel @Inject constructor(
    private val movieDatabase: MovieDatabase,
    private val movieDao: MovieDao,
    trendRepository: TrendRepository
) : ViewModel() {

    val trendResult = Pager(
        config = PagingConfig(10),
        remoteMediator = TrendRemoteMediator(movieDatabase, movieDao, trendRepository)
    ) {
        movieDao.trends()
    }.flow.cachedIn(viewModelScope)

}
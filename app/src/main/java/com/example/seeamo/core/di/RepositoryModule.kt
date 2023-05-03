package com.example.seeamo.core.di

import com.example.seeamo.core.data.CoreRepository
import com.example.seeamo.core.data.Repository
import com.example.seeamo.trend.data.TrendRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
class RepoModule {

    @Singleton
    @Provides
    fun provideTrendRepo(
        trendRepository: TrendRepository
    ): Repository.Trend = trendRepository

    @Singleton
    @Provides
    fun provideCoreRepo(
        coreRepository: CoreRepository
    ): Repository.Core = coreRepository


}
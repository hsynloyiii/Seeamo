package com.example.seeamo.di

import com.example.seeamo.data.repository.Repository
import com.example.seeamo.data.repository.TrendRepository
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
    fun provideRegistrationRepo(
        trendRepository: TrendRepository
    ): Repository.Trend = trendRepository

}
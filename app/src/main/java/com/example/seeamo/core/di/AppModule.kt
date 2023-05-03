package com.example.seeamo.core.di

import android.content.Context
import com.example.seeamo.BuildConfig
import com.example.seeamo.core.data.source.MovieDao
import com.example.seeamo.core.data.source.MovieDatabase
import com.example.seeamo.core.data.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    // Network
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder().baseUrl(BuildConfig.BASE_URL).addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            )
        ).build()

    @Provides
    @Singleton
    fun provideTrendApi(retrofit: Retrofit): ApiService.Trend =
        retrofit.create(ApiService.Trend::class.java)

    @Provides
    @Singleton
    fun provideCoreApi(retrofit: Retrofit): ApiService.Core =
        retrofit.create(ApiService.Core::class.java)


    // Database
    @Provides
    @Singleton
    fun provideMovieDatabase(@ApplicationContext context: Context): MovieDatabase =
        MovieDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideMovieDao(movieDatabase: MovieDatabase): MovieDao = movieDatabase.movieDao()
}
package com.example.seeamo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Singleton
    @DefaultDispatchers
    @Provides
    fun provideDefaultDispatchers(): CoroutineDispatcher = Dispatchers.Default

    @Singleton
    @IODispatchers
    @Provides
    fun provideIODispatchers(): CoroutineDispatcher = Dispatchers.IO

    @Singleton
    @MainDispatchers
    @Provides
    fun provideMainDispatchers(): CoroutineDispatcher = Dispatchers.Main

    @Singleton
    @MainImmediateDispatchers
    @Provides
    fun provideMainImmediateDispatchers(): CoroutineDispatcher = Dispatchers.Main.immediate

    @Singleton
    @ApplicationScope
    @Provides
    fun provideCoroutineScope(
        @IODispatchers ioDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
}
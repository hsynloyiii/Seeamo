package com.example.seeamo.core.data.source

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.seeamo.data.model.TrendRemoteKey
import com.example.seeamo.data.model.TrendResult

@Dao
interface MovieDao {

    // Trend
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTrend(trendResult: List<TrendResult>)

    @Query("SELECT * FROM trendResult")
    fun trends(): PagingSource<Int, TrendResult>

    @Query("DELETE FROM trendResult")
    suspend fun clearAllTrends()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTrendRemoteKeys(TRENDRemoteKey: List<TrendRemoteKey>)

    @Query("SELECT * FROM trendRemoteKey WHERE id = :title")
    suspend fun getAllTrendRemoteKey(title: String): TrendRemoteKey

    @Query("DELETE FROM trendRemoteKey")
    suspend fun deleteAllTrendRemoteKey()

}
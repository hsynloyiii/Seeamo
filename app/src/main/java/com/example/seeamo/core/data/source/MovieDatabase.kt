package com.example.seeamo.core.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.seeamo.trend.data.TrendRemoteKey
import com.example.seeamo.trend.data.TrendResult

@Database(entities = [TrendResult::class, TrendRemoteKey::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(
            context: Context
        ): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    MovieDatabase::class.java,
                    "movie_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }

}
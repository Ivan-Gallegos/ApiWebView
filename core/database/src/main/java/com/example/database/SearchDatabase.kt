package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SearchEntity::class], version = 1)
abstract class SearchDatabase : RoomDatabase() {
    abstract fun searchDao(): SearchDao

    companion object {
        @Volatile
        private var instance: SearchDatabase? = null

        fun getInstance(appContext: Context): SearchDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                appContext, SearchDatabase::class.java, "search"
            ).build()
        }
    }
}

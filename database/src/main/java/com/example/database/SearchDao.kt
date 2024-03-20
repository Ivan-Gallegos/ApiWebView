package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SearchDao {
    @Query("SELECT * FROM searchEntity WHERE `query` LIKE :query LIMIT 1")
    fun getSearchEntity(query: String): SearchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg searchEntity: SearchEntity)

    fun insertWithTimeStamp(query: String, response: String) =
        insertAll(SearchEntity(query, response, System.currentTimeMillis()))

    @Delete
    fun delete(searchEntity: SearchEntity)
}
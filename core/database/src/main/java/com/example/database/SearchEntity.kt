package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SearchEntity(
    @PrimaryKey val query: String = "",
    val response: String = "",
    val timeStamp: Long = 0
){
    val ageMillis: Long get() = System.currentTimeMillis() - timeStamp
}

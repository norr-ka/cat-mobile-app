package com.example.mobilkiprojekt.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MediaDao {
    @Insert
    suspend fun insert(media: MediaEntity)

    @Query("SELECT * FROM media")
    fun getAllMedia(): kotlinx.coroutines.flow.Flow<List<MediaEntity>>

    @Delete
    suspend fun delete(media: MediaEntity)
}
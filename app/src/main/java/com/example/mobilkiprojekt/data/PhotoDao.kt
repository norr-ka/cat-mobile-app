package com.example.mobilkiprojekt.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: PhotoEntity)

    @Query("SELECT * FROM photo")
    suspend fun getAllPhotos(): List<PhotoEntity>

    @Delete
    suspend fun delete(photo: PhotoEntity)
}


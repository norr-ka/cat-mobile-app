package com.example.mobilkiprojekt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val path: String
)
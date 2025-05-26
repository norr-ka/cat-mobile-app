package com.example.mobilkiprojekt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val path: String,
    val type: MediaType, // Typ pliku - zdjęcie lub wideo
    val thumbnailPath: String? = null // Ścieżka do miniatury dla filmów
)

enum class MediaType {
    PHOTO, VIDEO
}
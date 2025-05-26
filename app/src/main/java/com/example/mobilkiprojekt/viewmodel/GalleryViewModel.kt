package com.example.mobilkiprojekt.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilkiprojekt.data.AppDatabase
import com.example.mobilkiprojekt.data.MediaDao
import com.example.mobilkiprojekt.data.MediaEntity
import com.example.mobilkiprojekt.data.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class GaleriaViewModel(private val mediaDao: MediaDao) : ViewModel() {
    val media = mediaDao.getAllMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun addMediaFromUri(uri: Uri, type: MediaType, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            // Tutaj logika zapisywania pliku i tworzenia miniatur dla filmów
            val file = saveMediaToInternalStorage(uri, context, type)
            if (file != null) {
                val thumbnailPath = if (type == MediaType.VIDEO) {
                    createVideoThumbnail(file.path, context)?.absolutePath
                } else null

                mediaDao.insert(MediaEntity(path = file.path, type = type, thumbnailPath = thumbnailPath))
            }
        }
    }

    private fun saveMediaToInternalStorage(uri: Uri, context: Context, type: MediaType): File? {
        // Implementacja zapisywania pliku
    }

    private fun createVideoThumbnail(videoPath: String, context: Context): File? {
        // Implementacja tworzenia miniatury filmu
    }

    fun deleteMedia(media: MediaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Usuń plik z pamięci
            File(media.path).delete()
            media.thumbnailPath?.let { File(it).delete() }
            mediaDao.delete(media)
        }
    }
}


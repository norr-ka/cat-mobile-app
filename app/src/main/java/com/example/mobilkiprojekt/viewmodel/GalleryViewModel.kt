package com.example.mobilkiprojekt.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilkiprojekt.data.PhotoDatabase
import com.example.mobilkiprojekt.data.PhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class GaleriaViewModel(application: Application) : AndroidViewModel(application) {
    private val photoDao = PhotoDatabase.getDatabase(application).photoDao()

    private val _photos = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val photos = _photos.asStateFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _photos.value = photoDao.getAllPhotos()
        }
    }

    fun addPhotoFromUri(uri: Uri) {
        viewModelScope.launch {
            val savedPath = saveImageToInternalStorage(getApplication(), uri)
            savedPath?.let {
                val photo = PhotoEntity(path = it)
                photoDao.insert(photo)
                loadPhotos()
            }
        }
    }


    private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            val file = File(photo.path)
            if (file.exists()) {
                file.delete()
            }
            photoDao.delete(photo)
            loadPhotos()
        }
    }

}
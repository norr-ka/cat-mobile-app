package com.example.mobilkiprojekt.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilkiprojekt.data.MediaDao
import com.example.mobilkiprojekt.data.MediaEntity
import com.example.mobilkiprojekt.data.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
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
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val extension = if (type == MediaType.VIDEO) "mp4" else "jpg"
            val fileName = "${System.currentTimeMillis()}.$extension"
            val outputDir = context.filesDir
            val outputFile = File(outputDir, fileName)

            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun createVideoThumbnail(videoPath: String, context: Context): File? {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitmap = retriever.frameAtTime // domyślnie pierwsza klatka
            retriever.release()

            if (bitmap != null) {
                val fileName = "thumb_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
                }
                file
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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


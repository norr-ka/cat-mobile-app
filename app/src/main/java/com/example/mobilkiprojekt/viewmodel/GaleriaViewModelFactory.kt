package com.example.mobilkiprojekt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobilkiprojekt.data.AppDatabase

class GaleriaViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val mediaDao = AppDatabase.getDatabase(context).photoDao()
        if (modelClass.isAssignableFrom(GaleriaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GaleriaViewModel(mediaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.mobilkiprojekt

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime

class DataStore(context: Context) {
    private val sharedPref = context.getSharedPreferences("HARMONOGRAM_PREFS", Context.MODE_PRIVATE)

    fun saveTime(key: String, time: LocalTime) {
        with(sharedPref.edit()) {
            putString(key, "${time.hour}:${time.minute}")
            apply()
        }
    }

    fun getTime(key: String, default: LocalTime): LocalTime {
        val timeString = sharedPref.getString(key, null)
        return timeString?.let {
            val parts = it.split(":")
            LocalTime.of(parts[0].toInt(), parts[1].toInt())
        } ?: default
    }
}

@Composable
fun rememberDataStore(): DataStore {
    val context = LocalContext.current
    return remember { DataStore(context) }
}
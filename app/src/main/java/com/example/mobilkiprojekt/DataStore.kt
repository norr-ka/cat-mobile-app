package com.example.mobilkiprojekt

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalTime

class DataStore(context: Context) {
    private val sharedPref = context.getSharedPreferences("HARMONOGRAM_PREFS", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Metody do zapisywania i odczytywania pojedynczych czasów
    fun saveTime(key: String, time: LocalTime) {
        with(sharedPref.edit()) {
            putString(key, "${time.hour}:${time.minute}")
            apply()
        }
    }

    fun getTime(key: String, default: LocalTime): LocalTime {
        val timeString = sharedPref.getString(key, null) ?: return default
        return try {
            val parts = timeString.split(":")
            if (parts.size != 2) default else {
                LocalTime.of(parts[0].toInt(), parts[1].toInt())
            }
        } catch (e: Exception) {
            default
        }
    }

    // Metody do obsługi listy przypomnień
    fun saveReminders(reminders: List<Reminder>) {
        val json = gson.toJson(reminders)
        sharedPref.edit().apply {
            putString("reminders_list", json)
            apply()
        }
    }

    fun getReminders(): List<Reminder> {
        val json = sharedPref.getString("reminders_list", null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Reminder>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteReminder(reminderId: String) {
        val reminders = getReminders().toMutableList()
        reminders.removeIf { it.id == reminderId }
        saveReminders(reminders)
    }

    fun initializeDefaultReminders() {
        if (getReminders().isEmpty()) {
            val defaultReminders = listOf(
                Reminder(
                    id = "1",
                    title = "Śniadanie",
                    time = LocalTime.of(8, 0),
                    notificationTitle = "Przypomnienie",
                    notificationMessage = "Pora śniadania!"
                ),
                Reminder(
                    id = "2",
                    title = "Obiad",
                    time = LocalTime.of(14, 0),
                    notificationTitle = "Przypomnienie",
                    notificationMessage = "Pora obiadu!"
                ),
                Reminder(
                    id = "3",
                    title = "Kolacja",
                    time = LocalTime.of(19, 0),
                    notificationTitle = "Przypomnienie",
                    notificationMessage = "Pora kolacji!"
                )
            )
            saveReminders(defaultReminders)
        }
    }
}

@Composable
fun rememberDataStore(): DataStore {
    val context = LocalContext.current
    return remember { DataStore(context) }
}


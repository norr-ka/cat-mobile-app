// Reminder.kt
package com.example.mobilkiprojekt

import androidx.annotation.Keep
import java.time.LocalTime

@Keep
data class Reminder(
    val id: String,
    var title: String,
    var time: LocalTime,
    var notificationTitle: String,
    var notificationMessage: String
)
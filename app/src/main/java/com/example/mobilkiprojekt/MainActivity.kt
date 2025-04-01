package com.example.mobilkiprojekt

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilkiprojekt.ui.theme.MobilkiProjektTheme
import java.time.LocalTime
import java.util.Calendar

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sprawdź i poproś o uprawnienia
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // Przywróć alarmy przy starcie aplikacji
        restoreAlarms()

        enableEdgeToEdge()
        setContent {
            MobilkiProjektTheme {
                AppNavigation()
            }
        }
    }

    private fun restoreAlarms() {
        val sharedPref = getSharedPreferences("HARMONOGRAM_PREFS", Context.MODE_PRIVATE)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Lista wszystkich możliwych aktywności
        val activities = listOf(
            "sniadanie" to Pair("Śniadanie dla kota", "Czas nakarmić kota!"),
            "obiad" to Pair("Obiad dla kota", "Czas na obiad dla kota!"),
            "kolacja" to Pair("Kolacja dla kota", "Czas na kolację dla kota!"),
            "woda" to Pair("Zmiana wody", "Czas zmienić wodę kotu!"),
            "zabawa" to Pair("Pora zabawy", "Czas pobawić się z kotem!"),
            "kuweta" to Pair("Kuweta", "Czas wyczyścić kuwetę!"),
            "leki" to Pair("Leki", "Czas podać leki kotu!")
        )

        activities.forEach { (key, messages) ->
            sharedPref.getString(key, null)?.let { timeString ->
                val parts = timeString.split(":")
                val time = LocalTime.of(parts[0].toInt(), parts[1].toInt())

                val intent = Intent(this, NotificationReceiver::class.java).apply {
                    putExtra("title", messages.first)
                    putExtra("message", messages.second)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    messages.first.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, time.hour)
                    set(Calendar.MINUTE, time.minute)
                    set(Calendar.SECOND, 0)

                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController)
        }
        composable("harmonogram") {
            HarmonogramScreen(navController)
        }
        composable("wyposazenie") {
            WyposazenieScreen(navController)
        }
        composable("galeria") {
            GaleriaScreen(navController)
        }
        composable("kocia_gra") {
            KociaGraScreen(navController)
        }
        composable("powrot"){
            navController.navigateUp()
        }
    }
}

object SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, soundResId: Int) {
        mediaPlayer?.release() // Zwolnij poprzedni dźwięk jeśli istnieje
        mediaPlayer = MediaPlayer.create(context, soundResId).apply {
            start()
            setOnCompletionListener {
                release()
                mediaPlayer = null
            }
        }
    }
}

val font_cat = FontFamily(
    Font(R.font.font_cat)
)

val font_happy = FontFamily(
    Font(R.font.font_happy)
)
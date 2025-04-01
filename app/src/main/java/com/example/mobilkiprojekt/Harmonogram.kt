package com.example.mobilkiprojekt

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalTime
import java.util.Calendar
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HarmonogramScreen(navController: NavController) {
    val context = LocalContext.current

    fun scheduleNotification(time: LocalTime, title: String, message: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("message", message)
            }

            // Używamy tytułu jako unikalnego requestCode
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                title.hashCode(),
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val dataStore = rememberDataStore()

    // Stan dla każdej z godzin z odczytem z SharedPreferences
    var sniadanie by remember {
        mutableStateOf(dataStore.getTime("sniadanie", LocalTime.of(8, 0)))
    }
    var obiad by remember {
        mutableStateOf(dataStore.getTime("obiad", LocalTime.of(14, 0)))
    }
    var kolacja by remember {
        mutableStateOf(dataStore.getTime("kolacja", LocalTime.of(19, 0)))
    }
    var woda by remember {
        mutableStateOf(dataStore.getTime("woda", LocalTime.of(12, 0)))
    }
    var zabawa by remember {
        mutableStateOf(dataStore.getTime("zabawa", LocalTime.of(18, 0)))
    }
    var kuweta by remember {
        mutableStateOf(dataStore.getTime("kuweta", LocalTime.of(9, 0)))
    }
    var leki by remember {
        mutableStateOf(dataStore.getTime("leki", LocalTime.of(10, 0)))
    }

    // Stan dla widoczności time pickerów
    var showSniadaniePicker by remember { mutableStateOf(false) }
    var showObiadPicker by remember { mutableStateOf(false) }
    var showKolacjaPicker by remember { mutableStateOf(false) }
    var showWodaPicker by remember { mutableStateOf(false) }
    var showZabawaPicker by remember { mutableStateOf(false) }
    var showKuwetaPicker by remember { mutableStateOf(false) }
    var showLekiPicker by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ){
        // Przycisk powrotu w lewym górnym rogu
        IconButton(
            onClick = {
                SoundPlayer.playSound(context, R.raw.miau)
                navController.navigateUp() // Powrót do poprzedniego ekranu
            },
            modifier = Modifier
                .padding(top = 46.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "powrot",
                tint = colorResource(id = R.color.kremowy)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 100.dp)
        ) {
            // Sekcja Jedzenie
            item {
                Text(
                    text = "Przypomnienia",
                    style = TextStyle(
                        fontSize = 32.sp,
                        color = colorResource(id = R.color.kremowy),
                        fontFamily = font_happy
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Śniadanie
            item {
                HarmonogramItem(
                    title = "Pora śniadania",
                    time = sniadanie,
                    onClick = { showSniadaniePicker = true }
                )
                if (showSniadaniePicker) {
                    TimePickerDialog(
                        onCancel = { showSniadaniePicker = false },
                        onConfirm = { hour, minute ->
                            sniadanie = LocalTime.of(hour, minute)
                            dataStore.saveTime("sniadanie", sniadanie)
                            showSniadaniePicker = false
                            scheduleNotification(
                                sniadanie,
                                "Przypomnienie",
                                "Pora śniadania!"
                            )
                        },
                        initialHour = sniadanie.hour,
                        initialMinute = sniadanie.minute
                    )
                }
            }

            // Obiad
            item {
                HarmonogramItem(
                    title = "Pora obiadu",
                    time = obiad,
                    onClick = { showObiadPicker = true }
                )
                if (showObiadPicker) {
                    TimePickerDialog(
                        onCancel = { showObiadPicker = false },
                        onConfirm = { hour, minute ->
                            obiad = LocalTime.of(hour, minute)
                            showObiadPicker = false
                        },
                        initialHour = obiad.hour,
                        initialMinute = obiad.minute
                    )
                }
            }

            // Kolacja
            item {
                HarmonogramItem(
                    title = "Pora kolacji",
                    time = kolacja,
                    onClick = { showKolacjaPicker = true }
                )
                if (showKolacjaPicker) {
                    TimePickerDialog(
                        onCancel = { showKolacjaPicker = false },
                        onConfirm = { hour, minute ->
                            kolacja = LocalTime.of(hour, minute)
                            showKolacjaPicker = false
                        },
                        initialHour = kolacja.hour,
                        initialMinute = kolacja.minute
                    )
                }
            }

            // Woda
            item {
                HarmonogramItem(
                    title = "Zmiana wody",
                    time = woda,
                    onClick = { showWodaPicker = true }
                )
                if (showWodaPicker) {
                    TimePickerDialog(
                        onCancel = { showWodaPicker = false },
                        onConfirm = { hour, minute ->
                            woda = LocalTime.of(hour, minute)
                            showWodaPicker = false
                        },
                        initialHour = woda.hour,
                        initialMinute = woda.minute
                    )
                }
            }

            // Zabawa
            item {
                HarmonogramItem(
                    title = "Pora zabawy",
                    time = zabawa,
                    onClick = { showZabawaPicker = true }
                )
                if (showZabawaPicker) {
                    TimePickerDialog(
                        onCancel = { showZabawaPicker = false },
                        onConfirm = { hour, minute ->
                            zabawa = LocalTime.of(hour, minute)
                            showZabawaPicker = false
                        },
                        initialHour = zabawa.hour,
                        initialMinute = zabawa.minute
                    )
                }
            }

            // Kuweta
            item {
                HarmonogramItem(
                    title = "Zmiana kuwety",
                    time = kuweta,
                    onClick = { showKuwetaPicker = true }
                )
                if (showKuwetaPicker) {
                    TimePickerDialog(
                        onCancel = { showKuwetaPicker = false },
                        onConfirm = { hour, minute ->
                            kuweta = LocalTime.of(hour, minute)
                            showKuwetaPicker = false
                        },
                        initialHour = kuweta.hour,
                        initialMinute = kuweta.minute
                    )
                }
            }

            // Leki
            item {
                HarmonogramItem(
                    title = "Leki",
                    time = leki,
                    onClick = { showLekiPicker = true }
                )
                if (showLekiPicker) {
                    TimePickerDialog(
                        onCancel = { showLekiPicker = false },
                        onConfirm = { hour, minute ->
                            leki = LocalTime.of(hour, minute)
                            showLekiPicker = false
                        },
                        initialHour = leki.hour,
                        initialMinute = leki.minute
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollableNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(fontSize = 24.sp)
) {
    var offsetY by remember { mutableStateOf(0f) }
    var currentValue by remember { mutableStateOf(value) }

    val animatedOffset by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = tween(durationMillis = 100),
        label = "pickerAnimation"
    )

    Box(
        modifier = modifier
            .height(100.dp)
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    //Use the y of the dragAmount
                    offsetY += dragAmount.y
                    if (abs(offsetY) > 60) { // Próg zmiany wartości
                        val change = if (offsetY > 0) -1 else 1
                        currentValue = (currentValue + change).let {
                            when {
                                it < range.first -> range.last
                                it > range.last -> range.first
                                else -> it
                            }
                        }
                        onValueChange(currentValue)
                        offsetY = 0f
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.offset(y = animatedOffset.dp)
        ) {
            // Poprzednia wartość (dla efektu ciągłości)
            Text(
                text = "%02d".format(
                    if (currentValue - 1 < range.first) range.last else currentValue - 1
                ),
                style = textStyle,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = colorResource(id = R.color.kremowy).copy(alpha = 0.5f)
            )

            // Aktualna wartość
            Text(
                text = "%02d".format(currentValue),
                style = textStyle,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Następna wartość (dla efektu ciągłości)
            Text(
                text = "%02d".format(
                    if (currentValue + 1 > range.last) range.first else currentValue + 1
                ),
                style = textStyle,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = colorResource(id = R.color.kremowy).copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun HarmonogramItem(
    title: String,
    time: LocalTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                try {
                    onClick()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.rozowy)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.kremowy)
                )
            )
            Text(
                text = String.format("%02d:%02d", time.hour, time.minute),
                style = TextStyle(
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.kremowy)
                )
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int = 0,
    initialMinute: Int = 0
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(onClick = { onConfirm(hour, minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("Anuluj")
            }
        },
        title = { Text("Wybierz godzinę", style = TextStyle(fontSize = 20.sp)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Picker godzin
                ScrollableNumberPicker(
                    value = hour,
                    onValueChange = { hour = it },
                    range = 0..23,
                    modifier = Modifier.width(60.dp),
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontFamily = font_happy,
                        color = colorResource(id = R.color.kremowy)
                    )
                )

                Text(
                    text = ":",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontFamily = font_happy,
                        color = colorResource(id = R.color.kremowy))
                )

                // Picker minut
                ScrollableNumberPicker(
                    value = minute,
                    onValueChange = { minute = it },
                    range = 0..59,
                    modifier = Modifier.width(60.dp),
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontFamily = font_happy,
                        color = colorResource(id = R.color.kremowy))
                )
            }
        },
        containerColor = colorResource(id = R.color.ciemny),
        titleContentColor = colorResource(id = R.color.kremowy),
        textContentColor = colorResource(id = R.color.kremowy)
    )
}

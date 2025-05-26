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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontWeight
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
    val dataStore = rememberDataStore()

    // Lista przypomnień z możliwością dodawania i usuwania
    val reminders = remember { mutableStateListOf<Reminder>() }



    // Zapisywanie przypomnień przy zmianie
    LaunchedEffect(reminders) {
        if (reminders.isNotEmpty()) {
            dataStore.saveReminders(reminders)
        }
    }

    // Stan dla nowego przypomnienia
    var showAddDialog by remember { mutableStateOf(false) }
    var newReminderTitle by remember { mutableStateOf("") }
    var newNotificationTitle by remember { mutableStateOf("Przypomnienie") }
    var newNotificationMessage by remember { mutableStateOf("") }
    var newReminderTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Funkcja do planowania powiadomienia
    fun scheduleNotification(reminder: Reminder) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", reminder.notificationTitle)
                putExtra("message", reminder.notificationMessage)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminder.time.hour)
                set(Calendar.MINUTE, reminder.time.minute)
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
            dataStore.saveReminders(reminders)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Funkcja do usuwania powiadomienia
    fun cancelNotification(reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // Wczytanie zapisanych przypomnień przy inicjalizacji
    LaunchedEffect(Unit) {
        dataStore.initializeDefaultReminders()
        val savedReminders = dataStore.getReminders()
        if (savedReminders.isNotEmpty()) {
            reminders.addAll(savedReminders)
            savedReminders.forEach { reminder ->
                scheduleNotification(reminder)
            }        }
   }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        // Przycisk powrotu
        IconButton(
            onClick = {
                SoundPlayer.playSound(context, R.raw.miau)
                navController.navigateUp()
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

        Text(
            text = "Harmonogram",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = colorResource(id = R.color.kremowy),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        )

        // Lista przypomnień
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 100.dp)
        ) {

            items(reminders, key = { it.id }) { reminder ->
                var showPicker by remember { mutableStateOf(false) }

                ReminderItem(
                    reminder = reminder,
                    onTimeClick = { showPicker = true },
                    onDelete = {
                        cancelNotification(reminder.id)
                        reminders.remove(reminder)
                        dataStore.deleteReminder(reminder.id)
                    },
                    onTitleChange = { newTitle ->
                        val index = reminders.indexOf(reminder)
                        if (index != -1) {
                            reminders[index] = reminder.copy(title = newTitle)
                        }
                    }
                )

                if (showPicker) {
                    TimePickerDialog(
                        onCancel = { showPicker = false },
                        onConfirm = { hour, minute ->
                            val newTime = LocalTime.of(hour, minute)
                            val index = reminders.indexOf(reminder)
                            if (index != -1) {
                                // Anulujemy stare powiadomienie
                                cancelNotification(reminder.id)
                                // Aktualizujemy czas
                                reminders[index] = reminder.copy(time = newTime)
                                // Planujemy nowe powiadomienie
                                scheduleNotification(reminders[index])
                            }
                            showPicker = false
                        },
                        initialHour = reminder.time.hour,
                        initialMinute = reminder.time.minute
                    )
                }
            }
        }

        // Przycisk dodawania nowego przypomnienia
        FloatingActionButton(
            onClick = {
                // Resetowanie pól przed pokazaniem dialogu
                newReminderTitle = ""
                newNotificationMessage = ""
                newReminderTime = LocalTime.now()
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 16.dp, vertical = 30.dp),
            containerColor = colorResource(id = R.color.rozowy),
            contentColor = colorResource(id = R.color.kremowy)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj przypomnienie")
        }

        // Dialog dodawania nowego przypomnienia
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Dodaj nowe przypomnienie", style = TextStyle(fontSize = 20.sp)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newReminderTitle,
                            onValueChange = { newReminderTitle = it },
                            label = { Text("Nazwa przypomnienia") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newNotificationTitle,
                            onValueChange = { newNotificationTitle = it },
                            label = { Text("Tytuł powiadomienia") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )

                        OutlinedTextField(
                            value = newNotificationMessage,
                            onValueChange = { newNotificationMessage = it },
                            label = { Text("Treść powiadomienia") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )

                        Button(
                            onClick = { showTimePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text("Wybierz godzinę: ${String.format("%02d:%02d", newReminderTime.hour, newReminderTime.minute)}")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newReminderTitle.isNotBlank()) {
                                val newReminder = Reminder(
                                    id = System.currentTimeMillis().toString(),
                                    title = newReminderTitle,
                                    time = newReminderTime,
                                    notificationTitle = newNotificationTitle,
                                    notificationMessage = newNotificationMessage
                                )
                                reminders.add(newReminder)
                                scheduleNotification(newReminder)
                                dataStore.saveReminders(reminders) // Dodaj tę linię
                                showAddDialog = false
                            }
                        },
                        enabled = newReminderTitle.isNotBlank()
                    ) {
                        Text("Dodaj")
                    }
                },
                dismissButton = {
                    Button(onClick = { showAddDialog = false }) {
                        Text("Anuluj")
                    }
                },
                containerColor = colorResource(id = R.color.ciemny),
                titleContentColor = colorResource(id = R.color.kremowy),
                textContentColor = colorResource(id = R.color.kremowy)
            )
        }

        // Time picker dla nowego przypomnienia
        if (showTimePicker) {
            TimePickerDialog(
                onCancel = { showTimePicker = false },
                onConfirm = { hour, minute ->
                    newReminderTime = LocalTime.of(hour, minute)
                    showTimePicker = false
                },
                initialHour = newReminderTime.hour,
                initialMinute = newReminderTime.minute
            )
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onTimeClick: () -> Unit,
    onDelete: () -> Unit,
    onTitleChange: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(reminder.title) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.rozowy)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { it ->
                        editedTitle = it
                    },
                    label = { Text("Nazwa przypomnienia") },  // Wymagany label
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = colorResource(id = R.color.kremowy)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorResource(id = R.color.rozowy),
                        unfocusedContainerColor = colorResource(id = R.color.rozowy),
                        focusedTextColor = colorResource(id = R.color.kremowy),
                        unfocusedTextColor = colorResource(id = R.color.kremowy)
                    )
                )
            } else {
                Text(
                    text = reminder.title,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEditing = true },
                    color = colorResource(id = R.color.kremowy)
                )
            }

            Text(
                text = String.format("%02d:%02d", reminder.time.hour, reminder.time.minute),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable(onClick = onTimeClick),
                color = colorResource(id = R.color.kremowy)
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = colorResource(id = R.color.kremowy)
                )
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

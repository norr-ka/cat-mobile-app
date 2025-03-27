package com.example.mobilkiprojekt

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilkiprojekt.ui.theme.MobilkiProjektTheme
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MobilkiProjektTheme {
                AppNavigation()
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
            HarmonogramScreen()
        }
        composable("wyposazenie") {
            WyposazenieScreen()
        }
        composable("znane_kotki") {
            ZnaneKotkiScreen()
        }
        composable("kocia_gra") {
            KociaGraScreen()
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val opcjeMenu = listOf("Harmonogram", "Wyposazenie", "Znane kotki", "Kocia gra")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny)) // tło
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter) // Wyśrodkuj poziomo i ustaw na górze
                .padding(top = 100.dp) // Odstęp od góry
        ) {
            // Tytuł "KOTKI"
            Text(
                text = "KOTKI",
                style = TextStyle(
                    fontSize = 120.sp, // Duży rozmiar czcionki
                    color = colorResource(id = R.color.kremowy), // Jasny kolor
                    fontFamily = font_cat
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // Wyśrodkuj poziomo
            )

            // Napis "Podstawowe informacje o kotach"
            Text(
                text = "Podstawowe informacje o kotach",
                style = TextStyle(
                    fontSize = 32.sp, // Mniejszy rozmiar czcionki
                    color = colorResource(id = R.color.kremowy), // Jasny kolor
                    fontFamily = font_happy // Użyj czcionki font_happy
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // Wyśrodkuj poziomo
                    .padding(top = 16.dp) // Odstęp od tytułu
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent // Ustaw tło Scaffold na przezroczyste
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(top = 300.dp) // Odstęp od tytułu i podtytułu
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(opcjeMenu) { opcja ->
                    AnimatedButton(
                        tekst = opcja,
                        onClick = {
                            when (opcja) {
                                "Harmonogram" -> navController.navigate("harmonogram")
                                "Wyposazenie" -> navController.navigate("wyposazenie")
                                "Znane kotki" -> navController.navigate("znane_kotki")
                                "Kocia gra" -> navController.navigate("kocia_gra")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedButton(tekst: String, onClick: () -> Unit) {
    var isClicked by remember { mutableStateOf(false) }

    // Animacja wysokości przycisku
    val buttonHeight by animateDpAsState(
        targetValue = if (isClicked) 84.dp else 96.dp,
        label = "buttonHeightAnimation"
    )

    // Animacja koloru tła przycisku
    val buttonColor by animateColorAsState(
        targetValue = if (isClicked) {
            colorResource(id = R.color.rozowy_ciemniejszy)
        } else {
            colorResource(id = R.color.rozowy)
        },
        label = "buttonColorAnimation"
    )

    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(50) // Czas trwania animacji
            isClicked = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Wyłącz domyślny efekt wskazania (np. ripple)
                onClick = {
                    isClicked = !isClicked
                    onClick()
                }
            )
            .background(
                color = buttonColor,
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tekst,
            style = TextStyle(
                fontSize = 28.sp,
                color = colorResource(id = R.color.kremowy),
                fontFamily = font_happy
            )
        )
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HarmonogramScreen() {
    // Stan dla każdej z godzin
    var sniadanie by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var obiad by remember { mutableStateOf(LocalTime.of(14, 0)) }
    var kolacja by remember { mutableStateOf(LocalTime.of(19, 0)) }
    var woda by remember { mutableStateOf(LocalTime.of(12, 0)) }
    var zabawa by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var kuweta by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var leki by remember { mutableStateOf(LocalTime.of(10, 0)) }

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
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Sekcja Jedzenie
            item {
                Text(
                    text = "Jedzenie",
                    style = TextStyle(
                        fontSize = 28.sp,
                        color = colorResource(id = R.color.rozowy),
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
                            showSniadaniePicker = false
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

            // Sekcja Opieka
            item {
                Text(
                    text = "Opieka",
                    style = TextStyle(
                        fontSize = 28.sp,
                        color = colorResource(id = R.color.rozowy),
                        fontFamily = font_happy
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
            .clickable(onClick = onClick),
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

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column {
        Button(onClick = {
            if (value < range.last) onValueChange(value + 1)
        }) {
            Text("↑")
        }
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 24.sp
        )
        Button(onClick = {
            if (value > range.first) onValueChange(value - 1)
        }) {
            Text("↓")
        }
    }
}

@Composable
fun WyposazenieScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        Text(
            text = "Wyposazenie",
            style = TextStyle(
                fontSize = 32.sp,
                color = colorResource(id = R.color.kremowy),
                fontFamily = font_happy
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun ZnaneKotkiScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        Text(
            text = "Znane kotki",
            style = TextStyle(
                fontSize = 32.sp,
                color = colorResource(id = R.color.kremowy),
                fontFamily = font_happy
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun KociaGraScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        Text(
            text = "Kocia gra",
            style = TextStyle(
                fontSize = 32.sp,
                color = colorResource(id = R.color.kremowy),
                fontFamily = font_happy
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

val font_cat = FontFamily(
    Font(R.font.font_cat)
)

val font_happy = FontFamily(
    Font(R.font.font_happy)
)
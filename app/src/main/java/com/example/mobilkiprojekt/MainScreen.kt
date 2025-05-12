package com.example.mobilkiprojekt

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MainScreen(navController: NavController) {
    val opcjeMenu = listOf("Harmonogram", "Karta Zdrowia", "Galeria", "Kocia gra")

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
            // Tytul
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

            // Podtytul
            Text(
                text = "Opieka nad kotami",
                style = TextStyle(
                    fontSize = 32.sp, // Mniejszy rozmiar czcionki
                    color = colorResource(id = R.color.kremowy), // Jasny kolor
                    fontFamily = font_happy
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
                                "Karta Zdrowia" -> navController.navigate("karta_zdrowia")
                                "Galeria" -> navController.navigate("galeria")
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
    var isPressed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Animacja wysokości przycisku
    val buttonHeight by animateDpAsState(
        targetValue = if (isPressed) 84.dp else 96.dp,
        label = "buttonHeightAnimation"
    )

    // Animacja koloru tła przycisku
    val buttonColor by animateColorAsState(
        targetValue = if (isPressed) {
            colorResource(id = R.color.rozowy_ciemniejszy)
        } else {
            colorResource(id = R.color.rozowy)
        },
        label = "buttonColorAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // Rozpocznij animację wciśnięcia
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            // Zakończ animację po zwolnieniu
                            isPressed = false
                        }
                    },
                    onTap = {
                        // Odtwórz dźwięk i wykonaj akcję dopiero po pełnym kliknięciu
                        SoundPlayer.playSound(context, R.raw.miau)
                        onClick()
                    }
                )
            }
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
                color = colorResource(id = R.color.kremowy)
            )
        )
    }
}


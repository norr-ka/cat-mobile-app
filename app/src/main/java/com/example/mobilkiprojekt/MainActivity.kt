package com.example.mobilkiprojekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilkiprojekt.ui.theme.MobilkiProjektTheme

class MainActivity : ComponentActivity() {
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController)
        }
        composable("jedzenie") {
            JedzenieScreen()
        }
        composable("opieka") {
            OpiekaScreen()
        }
        composable("zabawy") {
            ZabawyScreen()
        }
        composable("znane_kotki") {
            ZnaneKotkiScreen()
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val opcjeMenu = listOf("Jedzenie", "Opieka", "Zabawy", "Znane kotki")

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
            ) {
                items(opcjeMenu) { opcja ->
                    PrzyciskMenu(
                        tekst = opcja,
                        onClick = {
                            when (opcja) {
                                "Jedzenie" -> navController.navigate("jedzenie")
                                "Opieka" -> navController.navigate("opieka")
                                "Zabawy" -> navController.navigate("zabawy")
                                "Znane kotki" -> navController.navigate("znane_kotki")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PrzyciskMenu(tekst: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.rozowy), // Kolor tła przycisku (brązowy)
            contentColor = colorResource(id = R.color.ciemny) // Kolor tekstu (kremowy)
        ),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp) // Odstępy między przyciskami
    ) {
        Text(
            text = tekst,
            style = TextStyle(
                fontSize = 32.sp,
                fontFamily = font_happy // Użyj czcionki font_happy
            )
        )
    }
}

@Composable
fun JedzenieScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        Text(
            text = "Ekran Jedzenie",
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
fun OpiekaScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        Text(
            text = "Ekran Opieka",
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
fun ZabawyScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        Text(
            text = "Ekran Zabawy",
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
            text = "Ekran Znane kotki",
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
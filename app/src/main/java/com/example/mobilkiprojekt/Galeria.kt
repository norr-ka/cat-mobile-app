package com.example.mobilkiprojekt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun GaleriaScreen(navController: NavController) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
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
    }
}

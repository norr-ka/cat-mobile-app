package com.example.mobilkiprojekt

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun KociaGraScreen(navController: NavController) {
    val context = LocalContext.current
    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels.toFloat()

    var mouseAlive by remember { mutableStateOf(true) }
    var isDeadVisible by remember { mutableStateOf(false) }

    val position = remember { Animatable(Offset(500f, 500f), Offset.VectorConverter) }
    var direction by remember { mutableStateOf(randomDirection()) }
    val speed = 500f // px/s

    val oscillation = remember { Animatable(0f) }
    val oscillationAmplitude = 10f // maksymalny kąt wychylenia


    // Ruch myszki w pętli
    LaunchedEffect(mouseAlive) {
        while (mouseAlive) {
            val frameTime = 16L // ok. 60fps
            val deltaTime = frameTime / 1000f

            val next = position.value + direction * speed * deltaTime

            // Odbijanie od krawędzi
            val bounded = Offset(
                next.x.coerceIn(0f, screenWidth - 100f),
                next.y.coerceIn(0f, screenHeight - 100f)
            )
            if (next.x < 0 || next.x > screenWidth - 100) direction = direction.copy(x = -direction.x)
            if (next.y < 0 || next.y > screenHeight - 100) direction = direction.copy(y = -direction.y)

            position.snapTo(bounded)
            delay(frameTime)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            oscillation.animateTo(
                targetValue = 2 * Math.PI.toFloat(),
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
            )
            oscillation.snapTo(0f)
        }
    }


    // Co kilka sekund zmiana kierunku
    LaunchedEffect(mouseAlive) {
        while (mouseAlive) {
            delay(Random.nextLong(500, 4000))
            direction = randomDirection()
        }
    }

    // Reset po trafieniu
    LaunchedEffect(mouseAlive) {
        if (!mouseAlive) {
            delay(1000) // 1 sekunda pokazania martwej myszy
            isDeadVisible = false

            // Reset pozycji
            position.snapTo(
                Offset(
                    Random.nextFloat() * (screenWidth - 100f),
                    Random.nextFloat() * (screenHeight - 100f)
                )
            )

            delay(500) // chwila przerwy, zanim nowa się pojawi
            direction = randomDirection()
            mouseAlive = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        // Powrót
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

        val baseAngle = Math.toDegrees(
            kotlin.math.atan2(direction.y.toDouble(), direction.x.toDouble())
        ).toFloat() + 90f

        val oscillationAngle = sin(oscillation.value.toDouble()).toFloat() * oscillationAmplitude
        val angleDegrees = if (mouseAlive) {
            baseAngle + oscillationAngle
        } else {
            baseAngle
        }

        if (mouseAlive || isDeadVisible) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(position.value.x.roundToInt(), position.value.y.roundToInt())
                    }
                    .size(80.dp)
                    .graphicsLayer {
                        rotationZ = angleDegrees + 180
                    }
                    .then(
                        if (mouseAlive) Modifier.clickable {
                            mouseAlive = false
                            isDeadVisible = true
                            SoundPlayer.playSound(context, R.raw.hit_sound)
                        } else Modifier
                    )
            ) {
                Image(
                    painter = painterResource(
                        id = if (mouseAlive) R.drawable.mouse else R.drawable.mouse_dead
                    ),
                    contentDescription = "mouse",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Kierunek w losowym kącie 360°
fun randomDirection(): Offset {
    val angle = Random.nextDouble(0.0, 2 * Math.PI)
    return Offset(cos(angle).toFloat(), sin(angle).toFloat())
}


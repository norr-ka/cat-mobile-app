package com.example.mobilkiprojekt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mobilkiprojekt.data.PhotoEntity
import com.example.mobilkiprojekt.viewmodel.GaleriaViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GaleriaScreen(navController: NavController) {
    val context = LocalContext.current
    val updatedContext by rememberUpdatedState(newValue = context)
    val viewModel: GaleriaViewModel = viewModel()
    val photos by viewModel.photos.collectAsState()

    var selectedPhoto by remember { mutableStateOf<PhotoEntity?>(null) }
    var photoToDelete by remember { mutableStateOf<PhotoEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.addPhotoFromUri(uri)
        }
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
                contentDescription = "powrót",
                tint = colorResource(id = R.color.kremowy)
            )
        }

        // Przyciski dodawania zdjęcia
        FloatingActionButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            containerColor = colorResource(id = R.color.kremowy),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 80.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Dodaj zdjęcie",
                tint = colorResource(id = R.color.ciemny)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(top = 100.dp, bottom = 80.dp)
                .fillMaxSize()
        ) {
            items(photos) { photo ->
                AsyncImage(
                    model = File(photo.path),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .combinedClickable(
                            onClick = { selectedPhoto = photo },
                            onLongClick = {
                                photoToDelete = photo
                                showDeleteDialog = true
                            }
                        )
                )
            }
        }

        // Powiększony widok
        if (selectedPhoto != null) {
            val startIndex = photos.indexOfFirst { it.id == selectedPhoto!!.id }
            val pagerState = rememberPagerState(initialPage = startIndex)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.ciemny).copy(alpha = 0.95f))
                    .clickable { selectedPhoto = null },
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    count = photos.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = File(photos[page].path),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // Aktualizacja selectedPhoto gdy zmieni się strona
            LaunchedEffect(pagerState.currentPage) {
                selectedPhoto = photos.getOrNull(pagerState.currentPage)
            }
        }

        if (showDeleteDialog && photoToDelete != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { androidx.compose.material3.Text("Usuń zdjęcie") },
                text = { androidx.compose.material3.Text("Czy na pewno chcesz usunąć to zdjęcie?") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.deletePhoto(photoToDelete!!)
                            showDeleteDialog = false
                            photoToDelete = null
                        }
                    ) {
                        androidx.compose.material3.Text("Usuń")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showDeleteDialog = false
                            photoToDelete = null
                        }
                    ) {
                        androidx.compose.material3.Text("Anuluj")
                    }
                }
            )
        }
    }
}
package com.example.mobilkiprojekt

import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mobilkiprojekt.data.MediaEntity
import com.example.mobilkiprojekt.viewmodel.GaleriaViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.example.mobilkiprojekt.data.MediaType
import com.example.mobilkiprojekt.viewmodel.GaleriaViewModelFactory
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GaleriaScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: GaleriaViewModel = viewModel(factory = GaleriaViewModelFactory(context))

    val media by viewModel.media.collectAsState(initial = emptyList())

    var selectedMedia by remember { mutableStateOf<MediaEntity?>(null) }
    var mediaToDelete by remember { mutableStateOf<MediaEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val type = getMediaType(context, uri)
            viewModel.addMediaFromUri(uri, type, context)
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

        Text(
            text = "Galeria",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = colorResource(id = R.color.kremowy),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        )

        // Przyciski dodawania zdjęcia
        FloatingActionButton(
            onClick = { mediaPickerLauncher.launch("*/*") },
            containerColor = colorResource(id = R.color.kremowy),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 80.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Dodaj zdjęcie lub film",
                tint = colorResource(id = R.color.ciemny)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(top = 100.dp, bottom = 80.dp)
                .fillMaxSize()
        ) {
            items(media) { mediaItem ->
                when (mediaItem.type) {
                    MediaType.PHOTO -> {
                        AsyncImage(
                            model = File(mediaItem.path),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .combinedClickable(
                                    onClick = { selectedMedia = mediaItem },
                                    onLongClick = {
                                        mediaToDelete = mediaItem
                                        showDeleteDialog = true
                                    }
                                )
                        )
                    }
                    MediaType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .combinedClickable(
                                    onClick = { selectedMedia = mediaItem },
                                    onLongClick = {
                                        mediaToDelete = mediaItem
                                        showDeleteDialog = true
                                    }
                                )
                                .background(Color.Black)
                        ) {
                            mediaItem.thumbnailPath?.let { thumbnailPath ->
                                AsyncImage(
                                    model = File(thumbnailPath),
                                    contentDescription = "Miniatura filmu",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Odtwórz film",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        // Powiększony widok
        if (selectedMedia != null) {
            val startIndex = media.indexOfFirst { it.id == selectedMedia!!.id }
            val pagerState = rememberPagerState(initialPage = startIndex)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.ciemny).copy(alpha = 0.95f))
                    .clickable { selectedMedia = null },
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    count = media.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (media[page].type) {
                        MediaType.PHOTO -> {
                            AsyncImage(
                                model = File(media[page].path),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                        MediaType.VIDEO -> {
                            VideoPlayer(
                                videoUri = Uri.fromFile(File(media[page].path)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                selectedMedia = media.getOrNull(pagerState.currentPage)
            }
        }

        if (showDeleteDialog && mediaToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Usuń plik") },
                text = { Text("Czy na pewno chcesz usunąć ten plik?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteMedia(mediaToDelete!!)
                            showDeleteDialog = false
                            mediaToDelete = null
                        }
                    ) {
                        Text("Usuń")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            mediaToDelete = null
                        }
                    ) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}

private fun getMediaType(context: Context, uri: Uri): MediaType {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri)
    return if (mimeType?.startsWith("video/") == true) MediaType.VIDEO else MediaType.PHOTO
}
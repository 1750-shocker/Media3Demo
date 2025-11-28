package com.wzh.media3demo.music.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wzh.media3demo.music.AlbumsViewModel
import android.util.Log

@Composable
fun AlbumsScreen(navController: NavController) {
    val vm: AlbumsViewModel = viewModel()
    LaunchedEffect(Unit) {
        Log.d("wzhhh", "AlbumsScreen enter, trigger vm.load")
        vm.load()
    }
    val albums by vm.albums.collectAsState()
    val context = LocalContext.current
    Log.d("wzhhh", "AlbumsScreen albums size=" + albums.size)
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
        Text("专辑", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(albums) { album ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Log.d("wzhhh", "AlbumsScreen click album id=" + album.id + " title=" + album.title)
                        navController.navigate("albumDetail/${album.id}")
                    }) {
                    Column {
                        val url = album.artworkUrl
                        val icon = album.iconUri
                        Box(modifier = Modifier.height(160.dp)) {
                            androidx.compose.ui.viewinterop.AndroidView(factory = {
                                android.widget.ImageView(it).apply { scaleType = android.widget.ImageView.ScaleType.CENTER_CROP }
                            }, modifier = Modifier.fillMaxSize()) { iv ->
                                if (url != null) {
                                    Log.d("wzhhh", "AlbumsScreen load artworkUrl=" + url)
                                    com.bumptech.glide.Glide.with(context).load(url).into(iv)
                                } else if (icon != null) {
                                    Log.d("wzhhh", "AlbumsScreen load icon uri via Glide=" + icon)
                                    com.bumptech.glide.Glide.with(context).load(icon).into(iv)
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(album.title, style = MaterialTheme.typography.titleMedium)
                            Text(album.subtitle, style = MaterialTheme.typography.bodyMedium)
                            if (album.year != null) Text(album.year, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

package com.wzh.media3demo.music.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.wzh.media3demo.music.AlbumDetailViewModel
import com.wzh.media3demo.music.MusicViewModel
import android.util.Log
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: AlbumDetailViewModel = viewModel(factory = AlbumDetailViewModel.Factory(app))
    LaunchedEffect(Unit) {
        Log.d("wzhhh", "AlbumDetailScreen enter albumId=" + albumId)
        vm.load(albumId)
    }
    val tracks by vm.tracks.collectAsState()
    val header by vm.header.collectAsState()
    Log.d("wzhhh", "AlbumDetailScreen tracks size=" + tracks.size)
    val musicVm: MusicViewModel = viewModel()
    LaunchedEffect(Unit) {
        Log.d("wzhhh", "AlbumDetailScreen connect controller for playback")
        musicVm.connect()
    }
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        TopAppBar(title = { Text("专辑详情") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Text("<") } })
        androidx.compose.foundation.layout.Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            val art = header?.artworkUrl
            androidx.compose.ui.viewinterop.AndroidView(factory = {
                android.widget.ImageView(it).apply { scaleType = android.widget.ImageView.ScaleType.CENTER_CROP }
            }, modifier = Modifier.size(64.dp)) { iv ->
                if (art != null) {
                    com.bumptech.glide.Glide.with(iv.context).load(art).into(iv)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(header?.title ?: "", style = MaterialTheme.typography.titleLarge)
                Text(header?.artist ?: "", style = MaterialTheme.typography.bodyMedium)
                val count = tracks.size
                Text((header?.year?.let { "$it · " } ?: "") + "$count 首歌曲", style = MaterialTheme.typography.bodySmall)
            }
        }
                    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        items(tracks) { t ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Log.d("wzhhh", "AlbumDetailScreen click track id=" + t.id + " title=" + t.title + " uri=" + t.mediaUri)
                    val startIndex = tracks.indexOfFirst { it.id == t.id }.let { if (it < 0) 0 else it }
                    musicVm.playPlaylist(tracks, startIndex)
                     navController.navigate("nowPlaying")
                }) {
                Row(modifier = Modifier.padding(12.dp).height(68.dp)) {
                    Text(text = t.trackNumber.toString(), modifier = Modifier.padding(end = 12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(t.title, style = MaterialTheme.typography.titleMedium)
                        Text(t.artist, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(if (t.duration > 0) formatTime(t.duration) else "00:00")
                }
            }
        }
        }
    }
}

@android.annotation.SuppressLint("DefaultLocale")
private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format("%02d:%02d", m, s)
}

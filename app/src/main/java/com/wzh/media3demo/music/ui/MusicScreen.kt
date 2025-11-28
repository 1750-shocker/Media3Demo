package com.wzh.media3demo.music.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wzh.media3demo.music.MusicViewModel

@Composable
fun MusicScreen() {
    val vm: MusicViewModel = viewModel()
    LaunchedEffect(Unit) { vm.connect() }
    val isPlaying by vm.isPlaying.collectAsState()
    val title by vm.title.collectAsState()
    val artist by vm.artist.collectAsState()
    val position by vm.positionMs.collectAsState()
    val duration by vm.durationMs.collectAsState()
    val playlist by vm.playlist.collectAsState()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        Text(title)
        Text(artist)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { vm.prev() }) { Text("上一首") }
            Button(onClick = { vm.playPause() }) { Text(if (isPlaying) "暂停" else "播放") }
            Button(onClick = { vm.next() }) { Text("下一首") }
        }
        Slider(
            value = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
            onValueChange = { v -> if (duration > 0) vm.seekTo((v * duration).toLong()) }
        )
        Row {
            Text(formatTime(position))
            Text(" / ")
            Text(formatTime(duration))
        }
        LazyColumn {
            items(playlist) { item ->
                Text(item)
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format("%02d:%02d", m, s)
}


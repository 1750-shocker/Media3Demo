package com.wzh.media3demo.music.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wzh.media3demo.music.MusicViewModel
import android.util.Log
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen() {
    val vm: MusicViewModel = viewModel()
    LaunchedEffect(Unit) {
        Log.d("wzhhh", "NowPlayingScreen enter, connect controller")
        vm.connect()
    }
    val isPlaying by vm.isPlaying.collectAsState()
    val title by vm.title.collectAsState()
    val artist by vm.artist.collectAsState()
    val artwork by vm.artworkUri.collectAsState()
    val position by vm.positionMs.collectAsState()
    val duration by vm.durationMs.collectAsState()
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding(), verticalArrangement = Arrangement.Top) {
        TopAppBar(title = { Text("正在播放") })

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.ui.viewinterop.AndroidView(factory = {
                android.widget.ImageView(it).apply {
                    scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                }
            }, modifier = Modifier.size(280.dp).clip(RoundedCornerShape(16.dp))) { iv ->
                val aw = artwork
                if (aw != null) {
                    Log.d("wzhhh1", "NowPlayingScreen artwork uri=" + aw + " scheme=" + aw.scheme + " authority=" + aw.authority)
                    com.bumptech.glide.Glide.with(iv.context).load(aw).into(iv)
                }
            }
            Text(title)
            Text(artist)
            Slider(
                value = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
                onValueChange = { v -> if (duration > 0) vm.seekTo((v * duration).toLong()) }
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(position))
                Text(formatTime(duration))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { vm.prev() }) { Text("上一首") }
                Button(onClick = { vm.playPause() }) { Text(if (isPlaying) "暂停" else "播放") }
                Button(onClick = { vm.next() }) { Text("下一首") }
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

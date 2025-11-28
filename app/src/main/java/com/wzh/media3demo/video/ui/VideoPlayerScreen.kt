package com.wzh.media3demo.video.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wzh.media3demo.video.VideoViewModel
import android.util.Log

@Composable
fun VideoPlayerScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val exo = remember { ExoPlayer.Builder(context).build() }
    val isFullscreen = remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose { exo.release() }
    }
    val vm: VideoViewModel = viewModel()
    val uriState by vm.uri.collectAsState()
    val u = uriState
    if (u != null) {
        try {
            context.contentResolver.openFileDescriptor(u, "r")?.close()
            Log.d("wzhhh", "preOpenFd success uri=" + u)
        } catch (e: Exception) {
            Log.d("wzhhh", "preOpenFd failed: " + e.message)
        }
        val item = androidx.media3.common.MediaItem.Builder()
            .setUri(u)
            .setMimeType("video/*")
            .build()
        Log.d("wzhhh", "setMediaItem uri=" + u)
        exo.setMediaItem(item)
        exo.prepare()
        Log.d("wzhhh", "player.prepare done")
        exo.playWhenReady = true
        Log.d("wzhhh", "playWhenReady true")
    }
    Box(modifier = Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.BottomCenter) {
        AndroidView(factory = {
            PlayerView(it).apply {
                player = exo
                useController = true
            }
        }, modifier = Modifier.fillMaxSize())
        Button(onClick = { isFullscreen.value = !isFullscreen.value }) { Text(if (isFullscreen.value) "退出全屏" else "全屏") }
    }
}


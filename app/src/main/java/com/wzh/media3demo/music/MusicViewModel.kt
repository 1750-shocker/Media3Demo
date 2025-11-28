package com.wzh.media3demo.music

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.wzh.media3demo.music.service.AudioService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MusicViewModel(app: Application) : AndroidViewModel(app) {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _artist = MutableStateFlow("")
    val artist: StateFlow<String> = _artist

    private val _playlist = MutableStateFlow<List<String>>(emptyList())
    val playlist: StateFlow<List<String>> = _playlist

    private var controller: MediaController? = null

    @OptIn(UnstableApi::class)
    fun connect() {
        if (controller != null) return
        val context = getApplication<Application>()
        val token = SessionToken(context, ComponentName(context, AudioService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get()
            setupListeners()
            refreshMetadata()
        }, java.util.concurrent.Executor { it.run() })
    }

    private fun setupListeners() {
        controller?.addListener(object : Player.Listener {})
        viewModelScope.launch {
            while (true) {
                val c = controller ?: break
                _positionMs.value = c.currentPosition
                _durationMs.value = c.duration.coerceAtLeast(0L)
                _isPlaying.value = c.isPlaying
                _title.value = c.mediaMetadata.title?.toString() ?: ""
                _artist.value = c.mediaMetadata.artist?.toString() ?: ""
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private fun refreshMetadata() {
        val c = controller ?: return
        val list = ArrayList<String>(c.mediaItemCount)
        for (i in 0 until c.mediaItemCount) {
            val m = c.getMediaItemAt(i).mediaMetadata
            list.add(m.title?.toString() ?: "Item $i")
        }
        _playlist.value = list
    }

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun prev() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }
}

package com.wzh.media3demo.music

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wzh.common.library.BrowseTree
import com.wzh.common.library.JsonSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.media.browse.MediaBrowser

data class TrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUri: Uri?,
    val artworkUrl: String?,
    val mediaUri: String,
    val trackNumber: Long,
    val duration: Long
)

data class AlbumHeader(
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val trackCount: Int,
    val year: String?
)

class AlbumDetailViewModel(app: Application) : AndroidViewModel(app) {
    private val _tracks = MutableStateFlow<List<TrackItem>>(emptyList())
    val tracks: StateFlow<List<TrackItem>> = _tracks
    private val _header = MutableStateFlow<AlbumHeader?>(null)
    val header: StateFlow<AlbumHeader?> = _header

    fun load(albumId: String) {
        if (_tracks.value.isNotEmpty()) return
        viewModelScope.launch {
            val url = "https://storage.googleapis.com/uamp/catalog.json"
            Log.d("wzhhh", "AlbumDetailViewModel.load start albumId=" + albumId)
            try {
                val source = JsonSource(Uri.parse(url))
                source.load()
                val tree = BrowseTree(source, recentMediaId = null)
                val normalizedKey = if (albumId.startsWith("album:")) {
                    val raw = albumId.removePrefix("album:")
                    val enc = Uri.encode(raw)
                    "album:" + enc
                } else albumId
                Log.d("wzhhh", "AlbumDetailViewModel.lookup key=" + normalizedKey)
                val list: List<MediaBrowser.MediaItem> = tree[normalizedKey] ?: emptyList()
                Log.d("wzhhh", "AlbumDetailViewModel.load tracks count=" + list.size)
                _tracks.value = list.map { item ->
                    val d = item.description
                    val e: Bundle? = d.extras
                TrackItem(
                    id = d.mediaId ?: "",
                    title = d.title?.toString() ?: "",
                    artist = d.subtitle?.toString() ?: (e?.getString("artist") ?: ""),
                    artworkUri = d.iconUri,
                    artworkUrl = e?.getString("com.example.android.uamp.JSON_ARTWORK_URI"),
                    mediaUri = e?.getString("mediaUri") ?: "",
                    trackNumber = e?.getLong("trackNumber", -1L) ?: -1L,
                    duration = e?.getLong("duration", -1L) ?: -1L
                )
            }
                _header.value = _tracks.value.firstOrNull()?.let { first ->
                    val albumTitle = list.firstOrNull()?.description?.extras?.getString("album")
                        ?: normalizedKey.removePrefix("album:")
                    AlbumHeader(
                        title = albumTitle,
                        artist = first.artist,
                        artworkUrl = first.artworkUrl ?: first.artworkUri?.toString(),
                        trackCount = list.size,
                        year = null
                    ).also { Log.d("wzhhh", "AlbumHeader title=" + it.title + " artist=" + it.artist + " count=" + it.trackCount) }
                }
            } catch (e: Exception) {
                Log.d("wzhhh", "AlbumDetailViewModel.load failed: " + e.message)
            }
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AlbumDetailViewModel(app) as T
        }
    }
}

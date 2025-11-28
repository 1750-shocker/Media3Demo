package com.wzh.media3demo.music

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wzh.common.library.BrowseTree
import com.wzh.common.library.JsonSource
import com.wzh.common.library.UAMP_ALBUMS_ROOT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.media.browse.MediaBrowser

data class AlbumItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconUri: android.net.Uri?,
    val artworkUrl: String?,
    val year: String?
)

class AlbumsViewModel(app: Application) : AndroidViewModel(app) {
    private val _albums = MutableStateFlow<List<AlbumItem>>(emptyList())
    val albums: StateFlow<List<AlbumItem>> = _albums

    fun load() {
        if (_albums.value.isNotEmpty()) return
        viewModelScope.launch {
            val url = "https://storage.googleapis.com/uamp/catalog.json"
            Log.d("wzhhh", "AlbumsViewModel.load start url=" + url)
            try {
                val source = JsonSource(Uri.parse(url))
                source.load()
                Log.d("wzhhh", "AlbumsViewModel.load source loaded state OK")
            val tree = BrowseTree(source, recentMediaId = null)
            val list: List<MediaBrowser.MediaItem> = tree[UAMP_ALBUMS_ROOT] ?: emptyList()
            Log.d("wzhhh", "AlbumsViewModel.load albums count=" + list.size)
            _albums.value = list.map { item ->
                val d = item.description
                val extras: Bundle? = d.extras
                AlbumItem(
                    id = d.mediaId ?: "",
                    title = d.title?.toString() ?: "",
                    subtitle = d.subtitle?.toString() ?: (extras?.getString("artist") ?: ""),
                    iconUri = d.iconUri,
                    artworkUrl = extras?.getString("com.example.android.uamp.JSON_ARTWORK_URI"),
                    year = extras?.getString("year")
                )
            }
            } catch (e: Exception) {
                Log.d("wzhhh", "AlbumsViewModel.load failed: " + e.message)
            }
        }
    }
}

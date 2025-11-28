package com.wzh.media3demo.music.data

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

class UampRepository(private val context: Context) {
    fun load(): List<MediaItem> {
        val json = context.assets.open("uamp_catalog.json").bufferedReader().use { it.readText() }
        val catalog = UampParser.parse(json)
        return catalog.tracks.map { t ->
            MediaItem.Builder()
                .setUri(t.url)
                .setMediaId(t.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .setArtworkUri(android.net.Uri.parse(t.artwork))
                        .build()
                )
                .build()
        }
    }
}


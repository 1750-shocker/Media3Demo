package com.wzh.media3demo.music.data

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.core.net.toUri

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
                        .setArtworkUri(t.artwork.toUri())
                        .build()
                )
                .build()
        }
    }
}


package com.wzh.common.library

import android.media.MediaDescription
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

class JsonSource(private val source: Uri) : AbstractMusicSource() {

    companion object {
        const val ORIGINAL_ARTWORK_URI_KEY = "com.example.android.uamp.JSON_ARTWORK_URI"
    }

    private var catalog: List<MediaBrowser.MediaItem> = emptyList()

    init {
        state = STATE_INITIALIZING
    }

    override fun iterator(): Iterator<MediaBrowser.MediaItem> = catalog.iterator()

    override suspend fun load() {
        updateCatalog(source)?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            catalog = emptyList()
            state = STATE_ERROR
        }
    }

    private suspend fun updateCatalog(catalogUri: Uri): List<MediaBrowser.MediaItem>? {
        return withContext(Dispatchers.IO) {
            val musicCat = try {
                downloadJson(catalogUri)
            } catch (_: IOException) {
                return@withContext null
            }

            val baseUri = catalogUri.toString().removeSuffix(catalogUri.lastPathSegment ?: "")

            musicCat.music.map { song ->
                catalogUri.scheme?.let { scheme ->
                    if (!song.source.startsWith(scheme)) {
                        song.source = baseUri + song.source
                    }
                    if (!song.image.startsWith(scheme)) {
                        song.image = baseUri + song.image
                    }
                }

                val jsonImageUri = song.image.toUri()
                val imageUri = AlbumArtContentProvider.mapUri(jsonImageUri)

                val extras = Bundle().apply {
                    putString(ORIGINAL_ARTWORK_URI_KEY, jsonImageUri.toString())
                    putString("title", song.title)
                    putString("artist", song.artist)
                    putString("album", song.album)
                    putString("genre", song.genre)
                    putString("mediaUri", song.source)
                    putString("albumArtUri", imageUri.toString())
                    putLong("trackNumber", song.trackNumber)
                    putLong("trackCount", song.totalTrackCount)
                    putLong("duration", TimeUnit.SECONDS.toMillis(song.duration))
                }

                val desc = MediaDescription.Builder()
                    .setMediaId(song.id)
                    .setTitle(song.title)
                    .setSubtitle(song.artist)
                    .setDescription(song.album)
                    .setIconUri(imageUri)
                    .setExtras(extras)
                    .build()

                MediaBrowser.MediaItem(desc, MediaBrowser.MediaItem.FLAG_PLAYABLE)
            }.toList()
        }
    }

    @Throws(IOException::class)
    private fun downloadJson(catalogUri: Uri): JsonCatalog {
        val catalogConn = URL(catalogUri.toString())
        val reader = BufferedReader(InputStreamReader(catalogConn.openStream()))
        return Gson().fromJson(reader, JsonCatalog::class.java)
    }
}

class JsonCatalog {
    var music: List<JsonMusic> = ArrayList()
}

@Suppress("unused")
class JsonMusic {
    var id: String = ""
    var title: String = ""
    var album: String = ""
    var artist: String = ""
    var genre: String = ""
    var source: String = ""
    var image: String = ""
    var trackNumber: Long = 0
    var totalTrackCount: Long = 0
    var duration: Long = -1L
    var site: String = ""
}

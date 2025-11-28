package com.wzh.common.library

import android.media.MediaDescription
import android.media.browse.MediaBrowser
import android.net.Uri

class BrowseTree(
    private val musicSource: MusicSource,
    private val recentMediaId: String? = null
) {
    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaBrowser.MediaItem>>()

    init {
        val rootList = mediaIdToChildren[UAMP_BROWSABLE_ROOT] ?: mutableListOf()
        val recommendedDesc = MediaDescription.Builder()
            .setMediaId(UAMP_RECOMMENDED_ROOT)
            .setTitle("为你推荐")
            .build()
        val albumsDesc = MediaDescription.Builder()
            .setMediaId(UAMP_ALBUMS_ROOT)
            .setTitle("专辑")
            .build()
        rootList += MediaBrowser.MediaItem(recommendedDesc, MediaBrowser.MediaItem.FLAG_BROWSABLE)
        rootList += MediaBrowser.MediaItem(albumsDesc, MediaBrowser.MediaItem.FLAG_BROWSABLE)
        mediaIdToChildren[UAMP_BROWSABLE_ROOT] = rootList

        musicSource.forEach { mediaItem ->
            val extras = mediaItem.description.extras
            val albumName = extras?.getString("album") ?: ""
            val albumId = "album:" + Uri.encode(albumName)

            val albumChildren = mediaIdToChildren[albumId] ?: buildAlbumRoot(albumName, mediaItem)
            albumChildren += mediaItem

            val trackNumber = extras?.getLong("trackNumber", -1L) ?: -1L
            if (trackNumber == 1L) {
                val recommendedChildren = mediaIdToChildren[UAMP_RECOMMENDED_ROOT] ?: mutableListOf()
                recommendedChildren += mediaItem
                mediaIdToChildren[UAMP_RECOMMENDED_ROOT] = recommendedChildren
            }

            val id = mediaItem.description.mediaId
            if (id != null && id == recentMediaId) {
                mediaIdToChildren[UAMP_RECENT_ROOT] = mutableListOf(mediaItem)
            }
        }
    }

    operator fun get(mediaId: String) = mediaIdToChildren[mediaId]

    private fun buildAlbumRoot(albumName: String, sampleItem: MediaBrowser.MediaItem): MutableList<MediaBrowser.MediaItem> {
        val extras = sampleItem.description.extras
        val iconUriString = extras?.getString("albumArtUri")
        val iconUri = iconUriString?.let { Uri.parse(it) } ?: sampleItem.description.iconUri

        val albumId = "album:" + Uri.encode(albumName)
        val builder = MediaDescription.Builder()
            .setMediaId(albumId)
            .setTitle(albumName)
            .setSubtitle(extras?.getString("artist") ?: "")
        if (iconUri != null) builder.setIconUri(iconUri)
        // 将原始封面 URL 一并写入专辑节点的 extras，便于 UI 使用 Glide 加载
        val albumExtras = android.os.Bundle().apply {
            putString("album", albumName)
            putString("artist", extras?.getString("artist") ?: "")
            putString("albumArtUri", iconUri?.toString() ?: iconUriString)
            val orig = extras?.getString("com.example.android.uamp.JSON_ARTWORK_URI")
            if (orig != null) putString("com.example.android.uamp.JSON_ARTWORK_URI", orig)
        }
        builder.setExtras(albumExtras)
        val albumDesc = builder.build()

        val rootList = mediaIdToChildren[UAMP_ALBUMS_ROOT] ?: mutableListOf()
        rootList += MediaBrowser.MediaItem(albumDesc, MediaBrowser.MediaItem.FLAG_BROWSABLE)
        mediaIdToChildren[UAMP_ALBUMS_ROOT] = rootList

        return mutableListOf<MediaBrowser.MediaItem>().also {
            mediaIdToChildren[albumId] = it
        }
    }
}

const val UAMP_BROWSABLE_ROOT = "/"
const val UAMP_RECOMMENDED_ROOT = "__RECOMMENDED__"
const val UAMP_ALBUMS_ROOT = "__ALBUMS__"
const val UAMP_RECENT_ROOT = "__RECENT__"

const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

const val RESOURCE_ROOT_URI = "android.resource://com.example.android.uamp.next/drawable/"

package com.wzh.media3demo.music.data

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val artwork: String
)

data class TracksCatalog(
    val tracks: List<Track>
)


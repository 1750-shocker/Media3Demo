package com.wzh.media3demo.music.data

import org.json.JSONObject

object UampParser {
    fun parse(json: String): TracksCatalog {
        val root = JSONObject(json)
        val arr = root.getJSONArray("tracks")
        val list = ArrayList<Track>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Track(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    artist = o.getString("artist"),
                    url = o.getString("url"),
                    artwork = o.getString("artwork")
                )
            )
        }
        return TracksCatalog(list)
    }
}


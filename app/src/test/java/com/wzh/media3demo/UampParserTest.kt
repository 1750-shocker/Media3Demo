package com.wzh.media3demo

import com.wzh.media3demo.music.data.UampParser
import org.junit.Assert.assertEquals
import org.junit.Test

class UampParserTest {
    @Test
    fun parseCatalog() {
        val json = """{
          "tracks": [
            {"id":"1","title":"A","artist":"B","url":"http://example.com/a.mp3","artwork":"http://example.com/a.jpg"},
            {"id":"2","title":"C","artist":"D","url":"http://example.com/c.mp3","artwork":"http://example.com/c.jpg"}
          ]
        }"""
        val catalog = UampParser.parse(json)
        assertEquals(2, catalog.tracks.size)
        assertEquals("A", catalog.tracks[0].title)
        assertEquals("D", catalog.tracks[1].artist)
    }
}


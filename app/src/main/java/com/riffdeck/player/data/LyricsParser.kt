package com.riffdeck.player.data

import com.riffdeck.player.data.model.lyrics.Lyrics
import com.riffdeck.player.data.model.lyrics.LyricsFile
import java.io.Reader

interface LyricsParser {

    fun parse(input: String, trackLength: Long, ignoreBlankLines: Boolean): Lyrics? =
        if (input.isNotBlank()) input.reader().use { parse(it, trackLength, ignoreBlankLines) } else null

    fun parse(reader: Reader, trackLength: Long, ignoreBlankLines: Boolean): Lyrics?

    fun handles(file: LyricsFile): Boolean

    fun handles(input: String): Boolean =
        if (input.isNotBlank()) input.reader().use { handles(it) } else false

    fun handles(reader: Reader): Boolean
}
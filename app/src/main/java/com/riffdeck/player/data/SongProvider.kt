package com.riffdeck.player.data

import com.riffdeck.player.data.model.Song

interface SongProvider {
    val songs: List<Song>
}
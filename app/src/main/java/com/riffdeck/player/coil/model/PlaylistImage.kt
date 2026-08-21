package com.riffdeck.player.coil.model

import com.riffdeck.player.data.local.room.PlaylistEntity
import com.riffdeck.player.data.model.Song

class PlaylistImage(val playlistEntity: PlaylistEntity, val songs: List<Song>) {
    override fun toString(): String {
        return buildString {
            append("PlaylistImage{")
            append("playlistEntity=$playlistEntity,")
            append("songs=$songs")
            append("}")
        }
    }
}
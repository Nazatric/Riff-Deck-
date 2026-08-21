/*
 * Copyright (c) 2024 Christians Martínez Alvarado
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.riffdeck.player.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * RiffDeck: persists artist biography text fetched from Last.fm so it
 * survives app restarts and remains readable offline once fetched at least
 * once, mirroring the same "fetch when online, cache to disk, read from
 * cache when offline" pattern already used for lyrics and (via Coil's disk
 * cache) artist/album images.
 */
@Entity
class ArtistInfoEntity(
    @PrimaryKey
    @ColumnInfo(name = "artist_name")
    val artistName: String,
    @ColumnInfo(name = "biography")
    val biography: String,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long
)

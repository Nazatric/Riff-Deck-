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

package com.riffdeck.player.ui.adapters.song

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.riffdeck.player.core.model.sort.SortKey
import com.riffdeck.player.core.sort.SongSortMode
import com.riffdeck.player.data.model.Song
import com.riffdeck.player.extensions.media.asReadableDuration
import com.riffdeck.player.extensions.media.asReadableTrackNumber
import com.riffdeck.player.extensions.media.displayArtistName
import com.riffdeck.player.extensions.utilities.buildInfoString
import com.riffdeck.player.ui.ISongCallback

class SimpleSongAdapter(
    context: FragmentActivity,
    songs: List<Song>,
    layoutRes: Int,
    sortMode: SongSortMode,
    callback: ISongCallback
) : SongAdapter(context, songs, layoutRes, sortMode, callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(itemLayoutRes, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val fixedTrackNumber = dataSet[position].trackNumber.asReadableTrackNumber()

        holder.imageText?.text = if (fixedTrackNumber > 0) fixedTrackNumber.toString() else "-"
        holder.time?.text = dataSet[position].duration.asReadableDuration()
    }

    override fun getSongText(song: Song): String {
        return when (sortMode?.selectedKey) {
            SortKey.Album -> buildInfoString(getTrackNumberString(song), song.albumName)
            SortKey.Track -> buildInfoString(getTrackNumberString(song), song.displayArtistName())
            SortKey.Year -> if (song.year > 0) {
                buildInfoString(song.year.toString(), song.displayArtistName())
            } else {
                song.displayArtistName()
            }
            else -> song.displayArtistName()
        }
    }

    private fun getTrackNumberString(song: Song) =
        song.trackNumber.takeIf { it > 0 }?.asReadableTrackNumber()?.toString() ?: "-"

    override fun getItemCount(): Int {
        return dataSet.size
    }
}
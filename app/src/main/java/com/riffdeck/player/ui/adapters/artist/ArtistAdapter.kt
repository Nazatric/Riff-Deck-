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

package com.riffdeck.player.ui.adapters.artist

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.FragmentActivity
import com.riffdeck.player.R
import com.riffdeck.player.coil.DEFAULT_ARTIST_IMAGE
import com.riffdeck.player.core.sort.ArtistSortMode
import com.riffdeck.player.core.sort.SongSortMode
import com.riffdeck.player.data.model.Artist
import com.riffdeck.player.extensions.isActivated
import com.riffdeck.player.extensions.loadPaletteImage
import com.riffdeck.player.extensions.media.artistInfo
import com.riffdeck.player.extensions.media.asSectionName
import com.riffdeck.player.extensions.media.displayName
import com.riffdeck.player.ui.IArtistCallback
import com.riffdeck.player.ui.component.base.AbsMultiSelectAdapter
import com.riffdeck.player.ui.component.base.MediaEntryViewHolder
import com.riffdeck.player.ui.component.menu.OnClickMenu
import com.riffdeck.player.ui.screen.player.PlayerViewModel
import com.riffdeck.player.util.Preferences
import me.zhanghai.android.fastscroll.PopupTextProvider
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class ArtistAdapter constructor(
    activity: FragmentActivity,
    dataSet: List<Artist>,
    private val itemLayoutRes: Int,
    private val sortMode: ArtistSortMode? = null,
    private val callback: IArtistCallback? = null,
) : AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist>(activity, R.menu.menu_media_selection),
    PopupTextProvider {

    private var albumArtistsOnly = false

    var dataSet by Delegates.observable(dataSet) { _: KProperty<*>, _: List<Artist>, _: List<Artist> ->
        albumArtistsOnly = Preferences.onlyAlbumArtists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(itemLayoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist: Artist = dataSet[position]
        val isChecked = isChecked(artist)
        holder.isActivated = isChecked
        holder.menu?.isGone = isChecked
        holder.title?.text = getArtistTitle(artist)
        holder.text?.text = getArtistText(holder, artist)
        val transitionName = if (albumArtistsOnly) artist.name else artist.id.toString()
        if (holder.imageContainer != null) {
            holder.imageContainer.transitionName = transitionName
        } else {
            holder.image?.transitionName = transitionName
        }
        holder.loadPaletteImage(artist, DEFAULT_ARTIST_IMAGE)
    }

    private fun getArtistTitle(artist: Artist): String {
        return artist.displayName()
    }

    private fun getArtistText(holder: ViewHolder, artist: Artist): String {
        return artist.artistInfo(holder.itemView.context)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun getIdentifier(position: Int): Artist? {
        return dataSet[position]
    }

    override fun getName(item: Artist): String? {
        return item.displayName()
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<Artist>) {
        callback?.artistsMenuItemClick(selection, menuItem)
    }

    override fun getPopupText(view: View, position: Int): CharSequence {
        return dataSet.getOrNull(position)?.displayName()?.asSectionName(sortMode) ?: ""
    }

    open inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {

        protected val artist: Artist
            get() = dataSet[bindingAdapterPosition]

        protected val sharedElements: Array<Pair<View, String>>?
            get() = if (imageContainer != null) {
                arrayOf(imageContainer to imageContainer.transitionName)
            } else if (image != null) {
                arrayOf(image to image.transitionName)
            } else {
                null
            }

        protected open fun onArtistMenuItemClick(item: MenuItem): Boolean {
            return callback?.artistMenuItemClick(artist, item, sharedElements) ?: false
        }

        override fun onClick(view: View) {
            if (isInQuickSelectMode) {
                toggleChecked(bindingAdapterPosition)
            } else {
                callback?.artistClick(artist, sharedElements)
            }
        }

        override fun onLongClick(view: View): Boolean {
            return toggleChecked(bindingAdapterPosition)
        }

        init {
            menu?.setOnClickListener(object : OnClickMenu() {
                override val popupMenuRes: Int
                    get() = R.menu.menu_item_artist

                override fun onMenuItemClick(item: MenuItem): Boolean {
                    return onArtistMenuItemClick(item)
                }
            })
            play?.setOnClickListener {
                val artistSongs = with(SongSortMode.ArtistSongs) {
                    artist.songs.sorted()
                }
                getViewModel<PlayerViewModel>()?.openQueue(artistSongs)
            }
        }
    }

    init {
        setHasStableIds(true)
    }
}
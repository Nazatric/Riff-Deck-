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

package com.riffdeck.player.ui.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.riffdeck.player.R
import com.riffdeck.player.coil.playlistImage
import com.riffdeck.player.data.local.room.PlaylistWithSongs
import com.riffdeck.player.extensions.media.asNumberOfSongs
import com.riffdeck.player.ui.IPlaylistCallback
import com.riffdeck.player.ui.component.base.AbsMultiSelectAdapter
import com.riffdeck.player.ui.component.base.MediaEntryViewHolder
import com.riffdeck.player.ui.component.menu.OnClickMenu
import com.riffdeck.player.ui.screen.player.PlayerViewModel
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


/**
 * @author Christians M. A. (mardous)
 */
class PlaylistAdapter(
    activity: AppCompatActivity,
    dataSet: List<PlaylistWithSongs>,
    @LayoutRes
    private val itemLayoutRes: Int,
    private val callback: IPlaylistCallback? = null
) : AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, PlaylistWithSongs>(activity, R.menu.menu_playlists_selection) {

    var dataSet by Delegates.observable(dataSet) { _: KProperty<*>, _: List<Any>, _: List<Any> ->
        notifyDataSetChanged()
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return LayoutInflater.from(parent.context).inflate(itemLayoutRes, parent, false).let { itemView ->
            createViewHolder(itemView)
        }
    }

    private fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = dataSet[position]
        val isChecked = isChecked(playlist)
        holder.itemView.isActivated = isChecked
        holder.menu?.isGone = isChecked
        holder.title?.text = playlist.playlistEntity.playlistName
        if (holder.text != null) {
            holder.text.text = playlist.songCount.asNumberOfSongs(holder.itemView.context)
        }
        if (holder.imageContainer != null) {
            holder.imageContainer.transitionName = playlist.playlistEntity.playlistName
        } else {
            holder.image?.transitionName = playlist.playlistEntity.playlistName
        }
        holder.image?.playlistImage(playlist)
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].playlistEntity.playListId
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): PlaylistWithSongs {
        return dataSet[position]
    }

    override fun getName(item: PlaylistWithSongs): String {
        return item.playlistEntity.playlistName
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<PlaylistWithSongs>) {
        callback?.playlistsMenuItemClick(selection, menuItem)
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {

        init {
            menu?.setOnClickListener(object : OnClickMenu() {
                override val popupMenuRes: Int
                    get() = R.menu.menu_item_playlist

                override fun onMenuItemClick(item: MenuItem): Boolean =
                    callback?.playlistMenuItemClick(playlist, item) ?: false
            })
            play?.setOnClickListener {
                getViewModel<PlayerViewModel>()?.openPlaylist(playlist.playlistEntity)
            }
        }

        private val playlist: PlaylistWithSongs
            get() = dataSet[bindingAdapterPosition]

        override fun onClick(view: View) {
            if (isInQuickSelectMode) {
                toggleChecked(bindingAdapterPosition)
            } else {
                callback?.playlistClick(playlist)
            }
        }

        override fun onLongClick(view: View): Boolean {
            return toggleChecked(bindingAdapterPosition)
        }
    }
}
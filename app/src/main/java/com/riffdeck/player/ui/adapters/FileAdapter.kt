/*
 * Copyright (c) 2025 Christians Martínez Alvarado
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

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import coil3.load
import com.riffdeck.player.R
import com.riffdeck.player.coil.DEFAULT_SONG_IMAGE
import com.riffdeck.player.coil.placeholderDrawableRes
import com.riffdeck.player.core.model.action.SongClickBehavior
import com.riffdeck.player.core.model.filesystem.FileSystemItem
import com.riffdeck.player.core.model.filesystem.StorageDevice
import com.riffdeck.player.core.model.sort.SortKey
import com.riffdeck.player.core.sort.FileSortMode
import com.riffdeck.player.data.model.Folder
import com.riffdeck.player.data.model.Song
import com.riffdeck.player.extensions.isActivated
import com.riffdeck.player.extensions.media.asReadableTrackNumber
import com.riffdeck.player.extensions.media.songInfo
import com.riffdeck.player.extensions.plurals
import com.riffdeck.player.extensions.resources.useAsIcon
import com.riffdeck.player.extensions.utilities.buildInfoString
import com.riffdeck.player.ui.IFileCallback
import com.riffdeck.player.ui.component.base.AbsMultiSelectAdapter
import com.riffdeck.player.ui.component.base.MediaEntryViewHolder
import com.riffdeck.player.ui.component.menu.OnClickMenu

class FileAdapter(
    activity: FragmentActivity,
    files: List<FileSystemItem>,
    private val itemLayoutRes: Int,
    private var sortMode: FileSortMode,
    var songClickBehavior: SongClickBehavior,
    var playOptionAlwaysVisible: Boolean,
    private val callback: IFileCallback?,
) : AbsMultiSelectAdapter<FileAdapter.ViewHolder, FileSystemItem>(activity, R.menu.menu_media_selection) {

    var files: List<FileSystemItem> = files
        private set

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = if (viewType == VIEW_TYPE_FOLDER || viewType == VIEW_TYPE_SONG) {
            LayoutInflater.from(parent.context).inflate(itemLayoutRes, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        }
        return ViewHolder(itemView, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        val isChecked = isChecked(file)
        holder.isActivated = isChecked
        holder.menu?.isGone = isChecked || getItemViewType(position) == VIEW_TYPE_OTHER
        holder.title?.text = getFileTitle(file)
        holder.text?.text = getFileText(holder, file)
        if (getItemViewType(position) == VIEW_TYPE_SONG) {
            holder.image?.load(file) {
                placeholderDrawableRes(holder.itemView.context, DEFAULT_SONG_IMAGE)
            }
        } else {
            holder.image?.setImageDrawable(getFileIcon(holder, file))
        }
    }

    private fun getFileTitle(file: FileSystemItem): CharSequence {
        return if (file is Song) {
            if (sortMode.selectedKey == SortKey.FileName) {
                file.fileName
            } else {
                file.title
            }
        } else {
            file.fileName
        }
    }

    private fun getFileText(holder: ViewHolder, file: FileSystemItem): CharSequence? {
        return when (file) {
            is Song -> when (sortMode.selectedKey) {
                SortKey.Track -> {
                    buildInfoString(
                        file.trackNumber
                            .asReadableTrackNumber()
                            .takeIf { it > 0 }?.toString() ?: "-",
                        file.songInfo()
                    )
                }

                else -> file.songInfo()
            }
            is Folder -> holder.itemView.context.plurals(R.plurals.x_items, file.musicFiles.size)
            else -> null
        }
    }

    private fun getFileIcon(holder: ViewHolder, file: FileSystemItem): Drawable? {
        val iconRes = when (file) {
            is StorageDevice -> file.iconRes
            else -> R.drawable.ic_folder_24dp
        }
        if (iconRes != 0) {
            return AppCompatResources.getDrawable(holder.itemView.context, iconRes)
        }
        return null
    }

    override fun getItemCount(): Int = files.size

    override fun getItemId(position: Int): Long = files[position].fileId

    override fun getItemViewType(position: Int): Int {
        return when(files[position]) {
            is Song -> VIEW_TYPE_SONG
            is Folder -> VIEW_TYPE_FOLDER
            else -> VIEW_TYPE_OTHER
        }
    }

    override fun getIdentifier(position: Int): FileSystemItem? {
        if (getItemViewType(position) == VIEW_TYPE_OTHER) {
            return null
        }
        return files[position]
    }

    override fun getName(item: FileSystemItem): String = item.fileName

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<FileSystemItem>) {
        callback?.filesMenuItemClick(selection, menuItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(files: List<FileSystemItem>, sortMode: FileSortMode) {
        this.files = files
        this.sortMode = sortMode
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View, itemViewType: Int) : MediaEntryViewHolder(itemView) {
        private val currentFile: FileSystemItem
            get() = files[bindingAdapterPosition]

        private val filePopupMenuResource: Int
            get() = when (itemViewType) {
                VIEW_TYPE_SONG -> R.menu.menu_item_song
                VIEW_TYPE_FOLDER -> R.menu.menu_item_directory
                else -> 0
            }

        override fun onClick(view: View) {
            if (isInQuickSelectMode) {
                toggleChecked(bindingAdapterPosition)
            } else {
                callback?.fileClick(currentFile)
            }
        }

        override fun onLongClick(view: View): Boolean {
            toggleChecked(bindingAdapterPosition)
            return true
        }

        init {
            text?.isVisible = (itemViewType == VIEW_TYPE_SONG || itemViewType == VIEW_TYPE_FOLDER)
            if (itemViewType != VIEW_TYPE_SONG) {
                image?.useAsIcon()
            }
            if (itemViewType != VIEW_TYPE_OTHER) {
                menu?.setOnClickListener(object : OnClickMenu() {
                    override val popupMenuRes: Int
                        get() = filePopupMenuResource

                    override fun onPreparePopup(menu: Menu) {
                        menu.findItem(R.id.action_play)
                            ?.isVisible = !songClickBehavior.isAbleToPlay || playOptionAlwaysVisible
                    }

                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        return callback?.fileMenuItemClick(currentFile, item) == true
                    }
                })
            }
        }
    }

    companion object {
        const val VIEW_TYPE_OTHER = 0
        const val VIEW_TYPE_FOLDER = 1
        const val VIEW_TYPE_SONG = 2
    }
}
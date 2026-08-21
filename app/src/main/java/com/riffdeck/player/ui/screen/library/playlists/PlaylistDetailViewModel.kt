package com.riffdeck.player.ui.screen.library.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.riffdeck.player.data.local.repository.PlaylistRepository
import com.riffdeck.player.data.local.room.SongEntity

class PlaylistDetailViewModel(
    private val playlistRepository: PlaylistRepository,
    private var playlistId: Long
) : ViewModel() {
    fun getSongs(): LiveData<List<SongEntity>> =
        playlistRepository.playlistSongsObservable(playlistId)

    fun playlistExists(): LiveData<Boolean> =
        playlistRepository.checkPlaylistExists(playlistId)

    fun getPlaylist() = playlistRepository.playlistWithSongsObservable(playlistId)
}
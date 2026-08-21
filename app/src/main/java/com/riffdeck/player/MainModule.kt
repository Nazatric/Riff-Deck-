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

package com.riffdeck.player

import androidx.preference.PreferenceManager
import androidx.room.Room
import com.riffdeck.player.coil.CustomArtistImageManager
import com.riffdeck.player.coil.CustomPlaylistImageManager
import com.riffdeck.player.core.RiffDeckDatabase
import com.riffdeck.player.core.audio.AudioOutputObserver
import com.riffdeck.player.data.local.AlbumCoverSaver
import com.riffdeck.player.data.local.EditTarget
import com.riffdeck.player.data.local.MediaStoreWriter
import com.riffdeck.player.data.local.repository.AlbumRepository
import com.riffdeck.player.data.local.repository.ArtistRepository
import com.riffdeck.player.data.local.repository.GenreRepository
import com.riffdeck.player.data.local.repository.LyricsRepository
import com.riffdeck.player.data.local.repository.NetworkRepository
import com.riffdeck.player.data.local.repository.NetworkRepositoryImpl
import com.riffdeck.player.data.local.repository.PlaylistRepository
import com.riffdeck.player.data.local.repository.RealAlbumRepository
import com.riffdeck.player.data.local.repository.RealArtistRepository
import com.riffdeck.player.data.local.repository.RealGenreRepository
import com.riffdeck.player.data.local.repository.RealLyricsRepository
import com.riffdeck.player.data.local.repository.RealPlaylistRepository
import com.riffdeck.player.data.local.repository.RealRepository
import com.riffdeck.player.data.local.repository.RealSearchRepository
import com.riffdeck.player.data.local.repository.RealSmartRepository
import com.riffdeck.player.data.local.repository.RealSongRepository
import com.riffdeck.player.data.local.repository.RealSpecialRepository
import com.riffdeck.player.data.local.repository.Repository
import com.riffdeck.player.data.local.repository.SearchRepository
import com.riffdeck.player.data.local.repository.SmartRepository
import com.riffdeck.player.data.local.repository.SongRepository
import com.riffdeck.player.data.local.repository.SpecialRepository
import com.riffdeck.player.data.model.Genre
import com.riffdeck.player.data.remote.deezer.DeezerService
import com.riffdeck.player.data.remote.github.GitHubService
import com.riffdeck.player.data.remote.jsonHttpClient
import com.riffdeck.player.data.remote.lastfm.LastFmService
import com.riffdeck.player.data.remote.listenbrainz.ListenBrainzService
import com.riffdeck.player.data.remote.lyrics.LyricsDownloadService
import com.riffdeck.player.data.remote.provideOkHttp
import com.riffdeck.player.playback.SleepTimer
import com.riffdeck.player.playback.equalizer.EqualizerManager
import com.riffdeck.player.playback.processor.BalanceAudioProcessor
import com.riffdeck.player.playback.processor.ReplayGainAudioProcessor
import com.riffdeck.player.ui.screen.about.AboutViewModel
import com.riffdeck.player.ui.screen.equalizer.EqualizerViewModel
import com.riffdeck.player.ui.screen.info.InfoViewModel
import com.riffdeck.player.ui.screen.library.LibraryViewModel
import com.riffdeck.player.ui.screen.library.albums.AlbumDetailViewModel
import com.riffdeck.player.ui.screen.library.artists.ArtistDetailViewModel
import com.riffdeck.player.ui.screen.library.folders.FolderDetailViewModel
import com.riffdeck.player.ui.screen.library.genres.GenreDetailViewModel
import com.riffdeck.player.ui.screen.library.playlists.PlaylistDetailViewModel
import com.riffdeck.player.ui.screen.library.search.SearchViewModel
import com.riffdeck.player.ui.screen.library.years.YearDetailViewModel
import com.riffdeck.player.ui.screen.lyrics.LyricsViewModel
import com.riffdeck.player.ui.screen.player.PlayerViewModel
import com.riffdeck.player.ui.screen.sleeptimer.SleepTimerViewModel
import com.riffdeck.player.ui.screen.tageditor.TagEditorViewModel
import com.riffdeck.player.ui.screen.update.UpdateViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    factory {
        jsonHttpClient(okHttpClient = get())
    }
    factory {
        provideOkHttp(context = get())
    }
    single {
        GitHubService(context = androidContext(), client = get())
    }
    single {
        DeezerService(client = get())
    }
    single {
        LastFmService(client = get())
    }
    single {
        ListenBrainzService(client = get())
    }
    single {
        LyricsDownloadService(context = get(), client = get())
    }
}

private val mainModule = module {
    single {
        androidContext().contentResolver
    }
    single {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    }
    single {
        SleepTimer(context = androidContext())
    }
    single {
        BalanceAudioProcessor()
    }
    single {
        ReplayGainAudioProcessor()
    }
    single {
        EqualizerManager(
            context = androidContext(),
            balanceProcessor = get(),
            replayGainProcessor = get(),
            audioOutputObserver = get()
        )
    }
    single {
        MediaStoreWriter(context = androidContext(), contentResolver = get())
    }
    single {
        AlbumCoverSaver(context = androidContext(), mediaStoreWriter = get())
    }
    single {
        CustomArtistImageManager(context = androidContext())
    }
    single {
        CustomPlaylistImageManager(context = androidContext())
    }
    single {
        AudioOutputObserver(context = androidContext())
    }
}

private val roomModule = module {
    single {
        Room.databaseBuilder(androidContext(), RiffDeckDatabase::class.java, "music_database.db")
            .addMigrations(
                RiffDeckDatabase.MIGRATION_1_2,
                RiffDeckDatabase.MIGRATION_2_3,
                RiffDeckDatabase.MIGRATION_3_4,
                RiffDeckDatabase.MIGRATION_4_5,
                RiffDeckDatabase.MIGRATION_5_6
            )
            .build()
    }

    factory {
        get<RiffDeckDatabase>().playlistDao()
    }

    factory {
        get<RiffDeckDatabase>().playCountDao()
    }

    factory {
        get<RiffDeckDatabase>().historyDao()
    }

    factory {
        get<RiffDeckDatabase>().queueDao()
    }

    factory {
        get<RiffDeckDatabase>().inclExclDao()
    }

    factory {
        get<RiffDeckDatabase>().lyricsDao()
    }

    factory {
        get<RiffDeckDatabase>().artistInfoDao()
    }
}

private val dataModule = module {
    single {
        RealRepository(
            context = androidContext(),
            songRepository = get(),
            albumRepository = get(),
            artistRepository = get(),
            genreRepository = get(),
            smartRepository = get(),
            specialRepository = get(),
            playlistRepository = get(),
            searchRepository = get(),
            networkRepository = get()
        )
    } bind Repository::class

    single {
        RealSongRepository(context = get(), inclExclDao = get())
    } bind SongRepository::class

    single {
        RealAlbumRepository(songRepository = get())
    } bind AlbumRepository::class

    single {
        RealArtistRepository(songRepository = get(), albumRepository = get())
    } bind ArtistRepository::class

    single {
        RealPlaylistRepository(
            context = androidContext(),
            songRepository = get(),
            playlistDao = get()
        )
    } bind PlaylistRepository::class

    single {
        RealGenreRepository(contentResolver = get(), songRepository = get())
    } bind GenreRepository::class

    single {
        RealSearchRepository(
            albumRepository = get(),
            songRepository = get(),
            artistRepository = get(),
            playlistRepository = get(),
            genreRepository = get(),
            specialRepository = get()
        )
    } bind SearchRepository::class

    single {
        RealSmartRepository(
            context = androidContext(),
            songRepository = get(),
            albumRepository = get(),
            artistRepository = get(),
            historyDao = get(),
            playCountDao = get()
        )
    } bind SmartRepository::class

    single {
        RealSpecialRepository(songRepository = get())
    } bind SpecialRepository::class

    single {
        RealLyricsRepository(
            context = androidContext(),
            preferences = get(),
            contentResolver = get(),
            lyricsDownloadService = get(),
            lyricsDao = get()
        )
    } bind LyricsRepository::class

    single {
        NetworkRepositoryImpl(
            context = androidContext(),
            preferences = get(),
            lastFmService = get(),
            listenBrainzService = get(),
            deezerService = get(),
            artistInfoDao = get()
        )
    } bind NetworkRepository::class
}

private val viewModule = module {
    viewModel {
        LibraryViewModel(repository = get(), inclExclDao = get(), customPlaylistImageManager = get())
    }

    viewModel {
        PlayerViewModel(preferences = get(), repository = get(), albumCoverSaver = get())
    }

    viewModel {
        EqualizerViewModel(
            contentResolver = get(),
            equalizerManager = get(),
            audioOutputObserver = get(),
            mediaStoreWriter = get()
        )
    }

    viewModel {
        SleepTimerViewModel(
            application = androidApplication(),
            sleepTimer = get()
        )
    }

    viewModel { (albumId: Long) ->
        AlbumDetailViewModel(
            application = androidApplication(),
            repository = get(),
            albumId = albumId
        )
    }

    viewModel { (artistId: Long, artistName: String?) ->
        ArtistDetailViewModel(
            application = androidApplication(),
            repository = get(),
            artistId = artistId,
            artistName = artistName
        )
    }

    viewModel { (playlistId: Long) ->
        PlaylistDetailViewModel(playlistRepository = get(), playlistId = playlistId)
    }

    viewModel { (genre: Genre) ->
        GenreDetailViewModel(repository = get(), genre = genre)
    }

    viewModel { (year: Int) ->
        YearDetailViewModel(repository = get(), year = year)
    }

    viewModel { (path: String) ->
        FolderDetailViewModel(repository = get(), folderPath = path)
    }

    viewModel {
        SearchViewModel(repository = get())
    }

    viewModel { (target: EditTarget) ->
        TagEditorViewModel(
            repository = get(),
            customArtistImageManager = get(),
            target = target
        )
    }

    viewModel {
        LyricsViewModel(preferences = get(), lyricsRepository = get())
    }

    viewModel {
        InfoViewModel(repository = get())
    }

    viewModel {
        UpdateViewModel(updateService = get())
    }

    viewModel {
        AboutViewModel(repository = get())
    }
}

val appModules = listOf(networkModule, mainModule, roomModule, dataModule, viewModule)
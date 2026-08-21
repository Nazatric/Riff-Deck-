package com.riffdeck.player.coil.fetcher

import android.content.SharedPreferences
import android.util.Log
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.kyant.taglib.TagLib
import com.riffdeck.player.coil.model.AudioCover
import com.riffdeck.player.coil.util.AudioCoverUtils
import com.riffdeck.player.data.local.repository.Repository
import com.riffdeck.player.data.model.network.NetworkFeature
import com.riffdeck.player.extensions.media.asAlbumCoverUri
import com.riffdeck.player.extensions.media.isArtistNameUnknown
import com.riffdeck.player.util.ImageSize
import com.riffdeck.player.util.PREFERRED_IMAGE_SIZE
import com.riffdeck.player.util.Preferences.requireString
import okio.IOException
import okio.buffer
import okio.source

class AudioCoverFetcher(
    private val loader: ImageLoader,
    private val options: Options,
    private val repository: Repository,
    private val cover: AudioCover,
    private val imageSize: String
) : Fetcher {

    private val contentResolver get() = options.context.contentResolver

    override suspend fun fetch(): FetchResult? {
        val stream = try {
            if (cover.isIgnoreMediaStore) {
                AudioCoverUtils.fallback(cover.path, cover.isUseFolderArt)
                    ?: contentResolver.openFileDescriptor(cover.uri, "r")?.use { fd ->
                        TagLib.getFrontCover(fd.dup().detachFd())?.data?.inputStream()
                    }
            } else {
                contentResolver.openInputStream(cover.albumId.asAlbumCoverUri())
            }
        } catch (e: IOException) {
            Log.e("AudioCoverFetcher", "Unable to decode cover image for ${cover.path}", e)
            null
        }

        if (stream == null &&
            !cover.artistName.isArtistNameUnknown() &&
            NetworkFeature.Images.Albums.isAvailable(options.context)) {
            val imageUrl = if (cover.isAlbum) {
                repository.deezerAlbum(cover.artistName, cover.albumName)?.getBestImage(cover.albumName, imageSize)
                    ?: repository.deezerTrack(cover.artistName, cover.title)?.getBestImage(imageSize)
            } else {
                repository.deezerTrack(cover.artistName, cover.title)?.getBestImage(imageSize)
            }
            if (imageUrl != null) {
                val data = loader.components.map(imageUrl, options)
                val output = loader.components.newFetcher(data, options, loader)
                val (fetcher) = checkNotNull(output) { "no supported fetcher for $imageUrl" }
                return fetcher.fetch()
            }
        }

        if (stream == null) return null
        return SourceFetchResult(
            source = ImageSource(
                source = stream.source().buffer(),
                fileSystem = options.fileSystem,
                metadata = null
            ),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    class Factory(
        private val preferences: SharedPreferences,
        private val repository: Repository
    ) : Fetcher.Factory<AudioCover> {
        override fun create(
            data: AudioCover,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return AudioCoverFetcher(
                loader = imageLoader,
                options = options,
                repository = repository,
                cover = data,
                imageSize = preferences.requireString(PREFERRED_IMAGE_SIZE, ImageSize.MEDIUM)
            )
        }
    }
}
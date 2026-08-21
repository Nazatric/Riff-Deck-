package com.riffdeck.player.ui.screen.lyrics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.riffdeck.player.R
import com.riffdeck.player.core.model.LibraryMargin
import com.riffdeck.player.core.model.lyrics.LyricsViewSettings
import com.riffdeck.player.core.model.lyrics.LyricsViewSettings.BackgroundEffect
import com.riffdeck.player.core.model.lyrics.LyricsViewState
import com.riffdeck.player.core.model.player.PlayerColorScheme
import com.riffdeck.player.data.model.lyrics.Lyrics
import com.riffdeck.player.extensions.isPowerSaveMode
import com.riffdeck.player.ui.component.compose.AnimatedEqBars
import com.riffdeck.player.ui.component.compose.color.extractGradientColors
import com.riffdeck.player.ui.component.compose.decoration.FadingEdges
import com.riffdeck.player.ui.component.compose.decoration.animatedGradient
import com.riffdeck.player.ui.theme.riffdeck.components.RiffDeckRetryLyricsButton
import com.riffdeck.player.ui.theme.riffdeck.components.riffDeckLiquidGlass
import com.riffdeck.player.ui.component.compose.decoration.fadingEdges
import com.riffdeck.player.ui.component.compose.lyrics.LyricsView
import com.riffdeck.player.ui.screen.library.LibraryViewModel
import com.riffdeck.player.ui.screen.player.PlayerViewModel
import com.riffdeck.player.ui.theme.PlayerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LyricsScreen(
    libraryViewModel: LibraryViewModel,
    lyricsViewModel: LyricsViewModel,
    playerViewModel: PlayerViewModel,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val isPowerSaveMode = context.isPowerSaveMode()

    val miniPlayerMargin by libraryViewModel.getMiniPlayerMargin().observeAsState(LibraryMargin(0))

    val lyricsViewSettings by lyricsViewModel.fullLyricsViewSettings.collectAsState()
    val lyricsResult by lyricsViewModel.lyricsResult.collectAsState()

    val currentSong by playerViewModel.currentSongFlow.collectAsState()
    val isPlaying by playerViewModel.isPlayingFlow.collectAsState()

    val lyricsViewState = remember(lyricsResult.syncedLyrics) {
        LyricsViewState(lyricsResult.syncedLyrics.content)
    }

    val songProgress by playerViewModel.progressFlow.collectAsState()
    LaunchedEffect(songProgress) {
        lyricsViewState.updatePosition(songProgress)
    }

    val plainScrollState = rememberScrollState()
    LaunchedEffect(lyricsResult.id) {
        plainScrollState.scrollTo(0)
    }

    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    LaunchedEffect(lyricsResult.id) {
        if (isPowerSaveMode)
            return@LaunchedEffect

        if (lyricsViewSettings.backgroundEffect == BackgroundEffect.Gradient) {
            withContext(Dispatchers.Default) {
                val result = SingletonImageLoader.get(context).execute(
                    ImageRequest.Builder(context)
                        .data(playerViewModel.currentSong)
                        .build()
                )
                gradientColors = if (result is SuccessResult) {
                    result.image.toBitmap().extractGradientColors()
                } else {
                    emptyList()
                }
            }
        }
    }

    var hasBackgroundEffects by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets
            .navigationBars
            .add(WindowInsets(bottom = miniPlayerMargin.totalMargin)),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onEditClick,
                containerColor = Color(0xFF3B4048),
                contentColor = Color(0xFFEFF3F8)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit_note_24dp),
                    contentDescription = stringResource(R.string.action_lyrics_editor)
                )
            }
        },
        modifier = Modifier.keepScreenOn()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = Pair(lyricsViewSettings.backgroundEffect, gradientColors),
                transitionSpec = {
                    fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000)))
                }
            ) { (effect, gradientColors) ->
                when {
                    effect.isGradient && gradientColors.size >= 2 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .animatedGradient(gradientColors, isPlaying)
                        )
                        hasBackgroundEffects = true
                    }

                    effect.isBlur -> {
                        val backgroundColor = MaterialTheme.colorScheme.surface

                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = currentSong,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(90.dp)
                                    .drawWithContent {
                                        drawContent()

                                        drawRect(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    backgroundColor.copy(alpha = 0.8f),
                                                    backgroundColor
                                                ),
                                                radius = size.minDimension * 0.9f
                                            )
                                        )
                                    }
                            )
                        }
                        hasBackgroundEffects = true
                    }

                    else -> {
                        hasBackgroundEffects = false
                    }
                }
            }

            LyricsSurface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .riffDeckLiquidGlass(),
                result = lyricsResult,
                state = lyricsViewState,
                settings = lyricsViewSettings,
                fadingEdges = FadingEdges(top = 56.dp, bottom = 32.dp),
                scrollState = plainScrollState,
                textAlign = TextAlign.Start,
                isPlaying = isPlaying,
                isPowerSaveMode = isPowerSaveMode,
                hasBackgroundEffects = hasBackgroundEffects,
                onRetry = { lyricsViewModel.retryLyrics(currentSong) }
            ) { playerViewModel.seekTo(it.startAt) }
        }
    }
}

@Composable
fun CoverLyricsScreen(
    lyricsViewModel: LyricsViewModel,
    playerViewModel: PlayerViewModel,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPowerSaveMode = context.isPowerSaveMode()

    val isPlaying by playerViewModel.isPlayingFlow.collectAsState()
    val currentSong by playerViewModel.currentSongFlow.collectAsState()
    val lyricsViewSettings by lyricsViewModel.playerLyricsViewSettings.collectAsState()

    val lyricsResult by lyricsViewModel.lyricsResult.collectAsState()
    val songProgress by playerViewModel.progressFlow.collectAsStateWithLifecycle(
        initialValue = 0,
        minActiveState = Lifecycle.State.RESUMED
    )
    val lyricsViewState = remember(lyricsResult.syncedLyrics) {
        LyricsViewState(lyricsResult.syncedLyrics.content)
    }

    LaunchedEffect(songProgress) {
        lyricsViewState.updatePosition(songProgress)
    }

    val plainScrollState = rememberScrollState()
    LaunchedEffect(lyricsResult.id) {
        plainScrollState.scrollTo(0)
    }

    val playerColorScheme by playerViewModel.colorSchemeFlow.collectAsState(
        initial = PlayerColorScheme.themeColorScheme(context)
    )
    PlayerTheme(playerColorScheme) {
        Box(modifier = modifier.fillMaxSize()) {
            LyricsSurface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .riffDeckLiquidGlass(),
                result = lyricsResult,
                state = lyricsViewState,
                settings = lyricsViewSettings,
                fadingEdges = FadingEdges(top = 72.dp, bottom = 64.dp),
                scrollState = plainScrollState,
                textAlign = TextAlign.Center,
                isPlaying = isPlaying,
                isPowerSaveMode = isPowerSaveMode,
                hasBackgroundEffects = false,
                onRetry = { lyricsViewModel.retryLyrics(currentSong) }
            ) { playerViewModel.seekTo(it.startAt) }

            FilledIconButton(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF3B4048),
                    contentColor = Color(0xFFEFF3F8)
                ),
                onClick = onExpandClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_open_in_full_24dp),
                    contentDescription = stringResource(R.string.action_lyrics_editor)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LyricsSurface(
    result: LyricsResult,
    state: LyricsViewState,
    settings: LyricsViewSettings,
    fadingEdges: FadingEdges,
    scrollState: ScrollState,
    textAlign: TextAlign?,
    isPlaying: Boolean,
    isPowerSaveMode: Boolean,
    hasBackgroundEffects: Boolean,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onSeekToLine: (Lyrics.Line) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = when {
        hasBackgroundEffects -> Color.White
        else -> when (settings.mode) {
            LyricsViewSettings.Mode.Player -> colorScheme.onSurface
            else -> colorScheme.secondary
        }
    }
    Box(modifier) {
        if (result.loading) {
            CircularWavyProgressIndicator(
                color = contentColor,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            when {
                state.lyrics != null -> {
                    LyricsView(
                        state = state,
                        settings = settings,
                        fadingEdges = fadingEdges,
                        contentColor = contentColor,
                        isPowerSaveMode = isPowerSaveMode,
                        hasBackgroundEffects = hasBackgroundEffects
                    ) { onSeekToLine(it) }
                    RiffDeckRetryLyricsButton(
                        onClick = onRetry,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                !result.plainLyrics.content.isNullOrBlank() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(rememberNestedScrollInteropConnection())
                            .fadingEdges(fadingEdges)
                            .verticalScroll(scrollState)
                            .padding(settings.contentPadding)
                    ) {
                        Text(
                            text = result.plainLyrics.content,
                            color = contentColor,
                            textAlign = textAlign,
                            style = settings.unsyncedStyle,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    RiffDeckRetryLyricsButton(
                        onClick = onRetry,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                result.instrumental -> {
                    AnimatedEqBars(
                        color = contentColor,
                        isPlaying = isPlaying,
                        barCount = 5,
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                    )
                }

                else -> {
                    // RiffDeck: real "Add lyrics" call to action instead of
                    // static text — taps trigger LyricsViewModel.retryLyrics(),
                    // which fetches from LRCLIB, BetterLyrics, and SimpMusic
                    // (see LyricsDownloadService), the same free providers
                    // already used for automatic lookups.
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_lyrics_found),
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5A6E),
                                contentColor = Color(0xFF3A0008)
                            )
                        ) {
                            Text(text = stringResource(R.string.action_add_lyrics))
                        }
                    }
                }
            }
        }
    }
}
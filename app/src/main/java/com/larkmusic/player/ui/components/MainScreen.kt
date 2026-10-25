package com.larkmusic.player.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.larkmusic.player.ui.navigation.Screen
import com.larkmusic.player.ui.theme.DarkBackground
import com.larkmusic.player.ui.theme.DarkSurface
import com.larkmusic.player.ui.theme.NeonCyan
import com.larkmusic.player.ui.theme.TextSecondary
import com.larkmusic.player.ui.viewmodel.EqualizerViewModel
import com.larkmusic.player.ui.viewmodel.MusicViewModel

@Composable
fun MainScreen(
    musicViewModel: MusicViewModel,
    equalizerViewModel: EqualizerViewModel
) {
    val context = LocalContext.current

    val songs by musicViewModel.songs.collectAsState()
    val currentSong by musicViewModel.currentSong.collectAsState()
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val currentPositionMs by musicViewModel.currentPositionMs.collectAsState()
    val durationMs by musicViewModel.durationMs.collectAsState()
    val lyrics by musicViewModel.lyrics.collectAsState()
    val activeLyricIndex by musicViewModel.activeLyricIndex.collectAsState()
    val isShuffle by musicViewModel.isShuffle.collectAsState()
    val repeatMode by musicViewModel.repeatMode.collectAsState()
    val isNowPlayingExpanded by musicViewModel.isNowPlayingExpanded.collectAsState()
    val isFloatingLyricsActive by musicViewModel.isFloatingLyricsActive.collectAsState()

    // Equalizer state
    val isEqEnabled by equalizerViewModel.isEqualizerEnabled.collectAsState()
    val frequencies by equalizerViewModel.bandFrequencies.collectAsState()
    val bandLevels by equalizerViewModel.bandLevels.collectAsState()
    val bassBoost by equalizerViewModel.bassBoost.collectAsState()
    val virtualizer by equalizerViewModel.virtualizer.collectAsState()
    val presets by equalizerViewModel.presets.collectAsState()
    val selectedPreset by equalizerViewModel.selectedPreset.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Songs) }

    val screens = listOf(
        Screen.Songs,
        Screen.Equalizer,
        Screen.Settings
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = Color.White
                ) {
                    screens.forEach { screen ->
                        val selected = currentScreen.route == screen.route
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan.copy(alpha = 0.15f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            onClick = { currentScreen = screen }
                        )
                    }
                }
            },
            containerColor = DarkBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.Songs -> {
                        SongListScreen(
                            songs = songs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongClick = { song -> musicViewModel.playSong(song) }
                        )
                    }
                    Screen.Equalizer -> {
                        EqualizerScreen(
                            isEnabled = isEqEnabled,
                            frequencies = frequencies,
                            bandLevels = bandLevels,
                            bassBoost = bassBoost,
                            virtualizer = virtualizer,
                            presets = presets,
                            selectedPreset = selectedPreset,
                            onToggleEnabled = { equalizerViewModel.toggleEqualizer() },
                            onPresetSelect = { equalizerViewModel.selectPreset(it) },
                            onBandChange = { band, level -> equalizerViewModel.setBandLevel(band, level) },
                            onBassBoostChange = { equalizerViewModel.setBassBoost(it) },
                            onVirtualizerChange = { equalizerViewModel.setVirtualizer(it) }
                        )
                    }
                    Screen.Settings, Screen.Playlists -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Lark Music v1.0.0", color = TextSecondary)
                        }
                    }
                }

                // Mini Player docked right above bottom navigation bar
                MiniPlayer(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onPlayPauseClick = { musicViewModel.togglePlayPause() },
                    onSkipNextClick = { musicViewModel.skipNext() },
                    onBarClick = { musicViewModel.setNowPlayingExpanded(true) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        // Full Screen Now Playing Sheet
        NowPlayingSheet(
            isVisible = isNowPlayingExpanded,
            currentSong = currentSong,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            lyrics = lyrics,
            activeLyricIndex = activeLyricIndex,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            isFloatingLyricsActive = isFloatingLyricsActive,
            onDismiss = { musicViewModel.setNowPlayingExpanded(false) },
            onPlayPauseClick = { musicViewModel.togglePlayPause() },
            onSkipNextClick = { musicViewModel.skipNext() },
            onSkipPrevClick = { musicViewModel.skipPrevious() },
            onSeekTo = { musicViewModel.seekTo(it) },
            onToggleShuffle = { musicViewModel.toggleShuffle() },
            onToggleRepeat = { musicViewModel.toggleRepeat() },
            onToggleFloatingLyrics = {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                } else {
                    musicViewModel.toggleFloatingLyrics()
                }
            }
        )
    }
}

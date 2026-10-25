package com.larkmusic.player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Songs : Screen("songs", "Songs", Icons.Default.MusicNote)
    object Playlists : Screen("playlists", "Playlists", Icons.Default.QueueMusic)
    object Equalizer : Screen("equalizer", "Equalizer", Icons.Default.Equalizer)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

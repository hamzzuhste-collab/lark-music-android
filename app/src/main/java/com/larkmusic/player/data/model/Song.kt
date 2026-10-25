package com.larkmusic.player.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val contentUri: Uri,
    val albumArtUri: Uri?,
    val path: String,
    val size: Long
)

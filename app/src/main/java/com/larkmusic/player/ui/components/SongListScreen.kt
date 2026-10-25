package com.larkmusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.larkmusic.player.data.model.Song
import com.larkmusic.player.ui.theme.DarkSurface
import com.larkmusic.player.ui.theme.NeonCyan
import com.larkmusic.player.ui.theme.TextPrimary
import com.larkmusic.player.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SongListScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = "All Tracks",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${songs.size} songs available",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracks found.\nScan or add audio files to device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(songs, key = { it.id }) { song ->
                    val isSelected = currentSong?.id == song.id
                    SongItem(
                        song = song,
                        isSelected = isSelected,
                        isPlaying = isSelected && isPlaying,
                        onClick = { onSongClick(song) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(88.dp)) // padding for floating mini player
                }
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DarkSurface else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    color = if (isSelected) NeonCyan else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${formatDuration(song.duration)}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Playing",
                tint = NeonCyan,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(20.dp)
            )
        }

        IconButton(onClick = { /* More options */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

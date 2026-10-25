package com.larkmusic.player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.larkmusic.player.data.model.LyricLine
import com.larkmusic.player.data.model.Song
import com.larkmusic.player.ui.theme.AccentPink
import com.larkmusic.player.ui.theme.DarkBackground
import com.larkmusic.player.ui.theme.DarkSurface
import com.larkmusic.player.ui.theme.NeonCyan
import com.larkmusic.player.ui.theme.TextPrimary
import com.larkmusic.player.ui.theme.TextSecondary

@Composable
fun NowPlayingSheet(
    isVisible: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    isShuffle: Boolean,
    repeatMode: Int,
    isFloatingLyricsActive: Boolean,
    onDismiss: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPrevClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFloatingLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible && currentSong != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (currentSong == null) return@AnimatedVisibility

        var showLyricsTab by remember { mutableStateOf(false) }
        var isDraggingSlider by remember { mutableStateOf(false) }
        var sliderPosition by remember { mutableStateOf(0f) }

        val actualPosition = if (isDraggingSlider) {
            sliderPosition
        } else {
            if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
        }

        // Vinyl rotation animation when playing
        val infiniteTransition = rememberInfiniteTransition(label = "VinylRotation")
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "DiscRotation"
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // Top App Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = TextSecondary
                            )
                        )
                    }

                    // Floating Lyrics Capsule Toggle
                    IconButton(onClick = onToggleFloatingLyrics) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Floating Lyrics",
                            tint = if (isFloatingLyricsActive) NeonCyan else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Middle Section: Toggle between Vinyl Record and Synced Lyrics
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (showLyricsTab) {
                        LyricsView(
                            lyrics = lyrics,
                            activeLyricIndex = activeLyricIndex,
                            onLyricClick = { targetTime -> onSeekTo(targetTime) }
                        )
                    } else {
                        // Lark-style Vinyl Record Player Disc
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(DarkSurface, Color(0xFF0A0B0E))
                                    )
                                )
                                .border(8.dp, Color(0xFF1E202B), CircleShape)
                                .clickable { showLyricsTab = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(190.dp)
                                    .clip(CircleShape)
                                    .rotate(if (isPlaying) rotationAngle else 0f)
                            ) {
                                if (currentSong.albumArtUri != null) {
                                    AsyncImage(
                                        model = currentSong.albumArtUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(NeonCyan.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                }
                            }

                            // Center Spindle Hole
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkBackground)
                                    .border(3.dp, NeonCyan, CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Track Title, Artist & Lyrics Tab Toggle Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { showLyricsTab = !showLyricsTab }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Lyrics Mode",
                            tint = if (showLyricsTab) NeonCyan else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seekbar / Slider
                Slider(
                    value = actualPosition,
                    onValueChange = {
                        isDraggingSlider = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        val targetMs = (sliderPosition * durationMs).toLong()
                        onSeekTo(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time indicators
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentDisplay = if (isDraggingSlider) {
                        (sliderPosition * durationMs).toLong()
                    } else {
                        currentPositionMs
                    }
                    Text(
                        text = formatDuration(currentDisplay),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Player Controls (Shuffle, Prev, Play/Pause, Next, Repeat)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp)
                ) {
                    // Shuffle
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) NeonCyan else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Previous
                    IconButton(
                        onClick = onSkipPrevClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Big glowing Play/Pause FAB
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeonCyan, AccentPink)
                                )
                            )
                            .clickable { onPlayPauseClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = DarkBackground,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = onSkipNextClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repeat
                    IconButton(onClick = onToggleRepeat) {
                        val icon = if (repeatMode == Player.REPEAT_MODE_ONE) {
                            Icons.Default.RepeatOne
                        } else {
                            Icons.Default.Repeat
                        }
                        val tint = if (repeatMode != Player.REPEAT_MODE_OFF) NeonCyan else Color.White.copy(alpha = 0.5f)
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repeat",
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

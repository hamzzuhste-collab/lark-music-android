package com.larkmusic.player.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.larkmusic.player.data.model.LyricLine
import com.larkmusic.player.ui.theme.NeonCyan
import com.larkmusic.player.ui.theme.TextSecondary

@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    onLyricClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Smooth scroll to keep active lyric centered
    LaunchedEffect(activeLyricIndex) {
        if (activeLyricIndex in lyrics.indices) {
            val targetScroll = maxOf(0, activeLyricIndex - 2)
            listState.animateScrollToItem(targetScroll)
        }
    }

    if (lyrics.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No lyrics available for this song",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 120.dp, horizontal = 24.dp),
            modifier = modifier.fillMaxSize()
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isActive = index == activeLyricIndex

                val textColor by animateColorAsState(
                    targetValue = if (isActive) NeonCyan else Color.White.copy(alpha = 0.45f),
                    label = "LyricTextColor"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1.08f else 1.0f,
                    label = "LyricScale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .scale(scale)
                        .clickable { onLyricClick(line.timeMs) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = line.text,
                        color = textColor,
                        fontSize = if (isActive) 20.sp else 16.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }
        }
    }
}

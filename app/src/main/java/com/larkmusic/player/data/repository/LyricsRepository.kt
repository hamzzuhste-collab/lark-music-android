package com.larkmusic.player.data.repository

import com.larkmusic.player.data.model.LyricLine
import com.larkmusic.player.data.model.Song
import com.larkmusic.player.lyrics.LrcParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LyricsRepository {

    suspend fun loadLyricsForSong(song: Song): List<LyricLine> = withContext(Dispatchers.IO) {
        // 1. Try finding companion .lrc file in the same folder
        try {
            if (song.path.isNotEmpty()) {
                val audioFile = File(song.path)
                val lrcFile = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
                if (lrcFile.exists() && lrcFile.canRead()) {
                    val content = lrcFile.readText(Charsets.UTF_8)
                    val parsed = LrcParser.parse(content)
                    if (parsed.isNotEmpty()) {
                        return@withContext parsed
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback
        }

        // 2. Default fallback: synthesize time-spaced lyrics for preview/demo
        generateFallbackLyrics(song)
    }

    private fun generateFallbackLyrics(song: Song): List<LyricLine> {
        val totalMs = if (song.duration > 0) song.duration else 180000L
        val interval = 5000L // every 5 seconds
        val lines = mutableListOf<LyricLine>()

        lines.add(LyricLine(0L, "♪ Enjoying ${song.title} ♪"))
        lines.add(LyricLine(4000L, "Artist: ${song.artist}"))
        lines.add(LyricLine(8000L, "Album: ${song.album}"))

        var current = 12000L
        var counter = 1
        while (current < totalMs - 10000L) {
            lines.add(
                LyricLine(
                    timeMs = current,
                    text = "♪ Lark Music synced lyrics verse $counter ♪"
                )
            )
            current += interval
            counter++
        }

        lines.add(LyricLine(maxOf(0L, totalMs - 6000L), "♪ (Music fading out) ♪"))
        return lines
    }
}

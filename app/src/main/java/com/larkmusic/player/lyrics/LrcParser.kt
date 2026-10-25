package com.larkmusic.player.lyrics

import com.larkmusic.player.data.model.LyricLine
import java.util.regex.Pattern

object LrcParser {
    private val TIME_TAG_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:[.:](\\d{2,3}))?\\]")
    private val OFFSET_PATTERN = Pattern.compile("\\[offset:\\s*([+-]?\\d+)\\]")

    fun parse(lrcContent: String): List<LyricLine> {
        val lines = lrcContent.lines()
        val result = mutableListOf<LyricLine>()
        var offsetMs: Long = 0

        // Check for offset tag first
        for (rawLine in lines) {
            val offsetMatcher = OFFSET_PATTERN.matcher(rawLine.trim())
            if (offsetMatcher.find()) {
                offsetMs = offsetMatcher.group(1)?.toLongOrNull() ?: 0
                break
            }
        }

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") ||
                trimmed.startsWith("[al:") || trimmed.startsWith("[by:") || trimmed.startsWith("[offset:")) {
                continue
            }

            val matcher = TIME_TAG_PATTERN.matcher(trimmed)
            val timeList = mutableListOf<Long>()

            while (matcher.find()) {
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0
                val fractionStr = matcher.group(3) ?: "0"
                val millis = if (fractionStr.length == 2) {
                    fractionStr.toLong() * 10
                } else {
                    fractionStr.toLong()
                }

                val totalMs = (minutes * 60 + seconds) * 1000 + millis + offsetMs
                timeList.add(maxOf(0L, totalMs))
            }

            // The remaining text after stripping all timestamp tags
            val text = trimmed.replace(TIME_TAG_PATTERN.toRegex(), "").trim()

            if (text.isNotEmpty() || timeList.isNotEmpty()) {
                for (time in timeList) {
                    result.add(LyricLine(timeMs = time, text = text))
                }
            }
        }

        // Sort chronologically by start time
        return result.sortedBy { it.timeMs }
    }

    /**
     * Finds the index of the currently active lyric line for a given playback position in ms.
     */
    fun findActiveLyricIndex(lyrics: List<LyricLine>, currentPositionMs: Long): Int {
        if (lyrics.isEmpty()) return -1
        if (currentPositionMs < lyrics.first().timeMs) return 0

        for (i in lyrics.indices.reversed()) {
            if (currentPositionMs >= lyrics[i].timeMs) {
                return i
            }
        }
        return 0
    }
}

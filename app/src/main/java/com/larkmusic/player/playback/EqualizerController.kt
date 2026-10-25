package com.larkmusic.player.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.larkmusic.player.data.model.EqualizerPreset

class EqualizerController {

    private val tag = "EqualizerController"
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    var isEnabled: Boolean = false
        private set

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        release()

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
            }
            isEnabled = true
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize AudioEffect suite", e)
            isEnabled = false
        }
    }

    fun getBandCount(): Short = equalizer?.numberOfBands ?: 5

    fun getBandFrequency(band: Short): Int = equalizer?.getCenterFreq(band) ?: 0

    fun getBandLevelRange(): ShortArray = equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)

    fun getBandLevel(band: Short): Short = equalizer?.getBandLevel(band) ?: 0

    fun setBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
        } catch (e: Exception) {
            Log.e(tag, "Failed to set band level", e)
        }
    }

    fun getPresets(): List<EqualizerPreset> {
        val eq = equalizer ?: return emptyList()
        val count = eq.numberOfPresets
        val presets = mutableListOf<EqualizerPreset>()
        val bands = eq.numberOfBands

        for (i in 0 until count) {
            val presetId = i.toShort()
            val name = eq.getPresetName(presetId)
            val levels = ShortArray(bands.toInt())
            presets.add(EqualizerPreset(presetId, name, levels))
        }
        return presets
    }

    fun applyPreset(presetIndex: Short) {
        try {
            equalizer?.usePreset(presetIndex)
        } catch (e: Exception) {
            Log.e(tag, "Failed to apply preset $presetIndex", e)
        }
    }

    fun setBassBoostStrength(strength: Short) {
        try {
            bassBoost?.setStrength(strength)
        } catch (e: Exception) {
            Log.e(tag, "Failed to set bass boost", e)
        }
    }

    fun getBassBoostStrength(): Short = bassBoost?.roundedStrength ?: 0

    fun setVirtualizerStrength(strength: Short) {
        try {
            virtualizer?.setStrength(strength)
        } catch (e: Exception) {
            Log.e(tag, "Failed to set virtualizer strength", e)
        }
    }

    fun getVirtualizerStrength(): Short = virtualizer?.roundedStrength ?: 0

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            Log.e(tag, "Error releasing audio effects", e)
        } finally {
            equalizer = null
            bassBoost = null
            virtualizer = null
            isEnabled = false
        }
    }
}

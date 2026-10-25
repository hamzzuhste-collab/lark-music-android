package com.larkmusic.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.larkmusic.player.data.model.EqualizerPreset
import com.larkmusic.player.playback.EqualizerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EqualizerViewModel : ViewModel() {

    private var equalizerController: EqualizerController? = null

    private val _isEqualizerEnabled = MutableStateFlow(true)
    val isEqualizerEnabled = _isEqualizerEnabled.asStateFlow()

    private val _bandFrequencies = MutableStateFlow<List<Int>>(listOf(60, 230, 910, 3600, 14000))
    val bandFrequencies = _bandFrequencies.asStateFlow()

    private val _bandLevels = MutableStateFlow<List<Short>>(listOf(0, 0, 0, 0, 0))
    val bandLevels = _bandLevels.asStateFlow()

    private val _bassBoost = MutableStateFlow<Short>(350)
    val bassBoost = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow<Short>(400)
    val virtualizer = _virtualizer.asStateFlow()

    private val _presets = MutableStateFlow<List<String>>(
        listOf("Flat", "Rock", "Pop", "Jazz", "Hip Hop", "Dance", "Classical", "Bass Boosted")
    )
    val presets = _presets.asStateFlow()

    private val _selectedPreset = MutableStateFlow("Rock")
    val selectedPreset = _selectedPreset.asStateFlow()

    fun bindController(controller: EqualizerController) {
        this.equalizerController = controller
    }

    fun setBandLevel(bandIndex: Int, level: Short) {
        val current = _bandLevels.value.toMutableList()
        if (bandIndex in current.indices) {
            current[bandIndex] = level
            _bandLevels.value = current
            equalizerController?.setBandLevel(bandIndex.toShort(), level)
        }
    }

    fun setBassBoost(value: Short) {
        _bassBoost.value = value
        equalizerController?.setBassBoostStrength(value)
    }

    fun setVirtualizer(value: Short) {
        _virtualizer.value = value
        equalizerController?.setVirtualizerStrength(value)
    }

    fun selectPreset(name: String) {
        _selectedPreset.value = name
        when (name) {
            "Rock" -> _bandLevels.value = listOf(500, 300, -100, 400, 600)
            "Pop" -> _bandLevels.value = listOf(-100, 200, 500, 300, -200)
            "Jazz" -> _bandLevels.value = listOf(400, 200, -200, 200, 500)
            "Hip Hop" -> _bandLevels.value = listOf(600, 400, 0, 200, 300)
            "Dance" -> _bandLevels.value = listOf(700, 0, 200, 400, 100)
            "Classical" -> _bandLevels.value = listOf(500, 300, -200, 400, 400)
            "Bass Boosted" -> _bandLevels.value = listOf(900, 600, 200, 0, -100)
            else -> _bandLevels.value = listOf(0, 0, 0, 0, 0)
        }
    }

    fun toggleEqualizer() {
        _isEqualizerEnabled.value = !_isEqualizerEnabled.value
    }
}

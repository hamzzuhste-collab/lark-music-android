package com.larkmusic.player.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.larkmusic.player.ui.theme.DarkSurface
import com.larkmusic.player.ui.theme.DarkSurfaceVariant
import com.larkmusic.player.ui.theme.NeonCyan
import com.larkmusic.player.ui.theme.TextPrimary
import com.larkmusic.player.ui.theme.TextSecondary

@Composable
fun EqualizerScreen(
    isEnabled: Boolean,
    frequencies: List<Int>,
    bandLevels: List<Short>,
    bassBoost: Short,
    virtualizer: Short,
    presets: List<String>,
    selectedPreset: String,
    onToggleEnabled: () -> Unit,
    onPresetSelect: (String) -> Unit,
    onBandChange: (Int, Short) -> Unit,
    onBassBoostChange: (Short) -> Unit,
    onVirtualizerChange: (Short) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Equalizer Header & Toggle Switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column {
                Text(
                    text = "Equalizer & Effects",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Lark 3D Surround & Bass Engine",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggleEnabled() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = NeonCyan.copy(alpha = 0.5f)
                )
            )
        }

        // Preset Chips
        Text(
            text = "PRESETS",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = TextSecondary
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { preset ->
                val isSelected = preset == selectedPreset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) NeonCyan else DarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else Color.Transparent,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onPresetSelect(preset) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = preset,
                        color = if (isSelected) Color.Black else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Band Sliders Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "5-BAND EQUALIZER",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                frequencies.forEachIndexed { index, freq ->
                    val level = bandLevels.getOrElse(index) { 0 }
                    val freqLabel = if (freq >= 1000) "${freq / 1000} kHz" else "$freq Hz"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = freqLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.width(60.dp)
                        )

                        Slider(
                            value = level.toFloat(),
                            onValueChange = { onBandChange(index, it.toInt().toShort()) },
                            valueRange = -1500f..1500f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            enabled = isEnabled,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "${level / 100} dB",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            modifier = Modifier
                                .width(50.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Audio Effects: Bass Boost & 3D Surround
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Bass Boost Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Bass Boost",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${bassBoost / 10}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = bassBoost.toFloat(),
                        onValueChange = { onBassBoostChange(it.toInt().toShort()) },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan
                        ),
                        enabled = isEnabled
                    )
                }
            }

            // 3D Virtualizer Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "3D Surround",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${virtualizer / 10}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = virtualizer.toFloat(),
                        onValueChange = { onVirtualizerChange(it.toInt().toShort()) },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan
                        ),
                        enabled = isEnabled
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // padding for mini player
    }
}

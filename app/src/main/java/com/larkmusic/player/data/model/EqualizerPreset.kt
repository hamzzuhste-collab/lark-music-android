package com.larkmusic.player.data.model

data class EqualizerPreset(
    val id: Short,
    val name: String,
    val bandLevels: ShortArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EqualizerPreset

        if (id != other.id) return false
        if (name != other.name) return false
        if (!bandLevels.contentEquals(other.bandLevels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.toInt()
        result = 31 * result + name.hashCode()
        result = 31 * result + bandLevels.contentHashCode()
        return result
    }
}

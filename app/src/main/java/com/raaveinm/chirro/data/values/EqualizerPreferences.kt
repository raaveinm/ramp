package com.raaveinm.chirro.data.values

data class EqualizerPreferences(
    val id: String = "custom",
    val subBass: Float = 0f,
    val bass: Float = 0f,
    val lowMid: Float = 0f,
    val mid: Float = 0f,
    val highMid: Float = 0f,
    val presence: Float = 0f,
    val brilliance: Float = 0f,
    val air: Float = 0f
) {
    companion object {
        val BASS_BOOST = EqualizerPreferences(
            id = "bass_boost",
            subBass = 4f,
            bass = 3f
        )
        val TREBLE_BOOST = EqualizerPreferences(
            id = "treble_boost",
            presence = 2f,
            brilliance = 5f,
            air = 6f
        )
        val NORMAL = EqualizerPreferences(id = "normal")

        val ROCK = EqualizerPreferences(
            id = "rock",
            subBass = 5f,
            bass = 3f,
            lowMid = -2f,
            mid = -4f,
            highMid = 2f,
            presence = 4f,
            brilliance = 5f,
            air = 3f
        )

        val JAZZ = EqualizerPreferences(
            id = "jazz",
            subBass = 3f,
            bass = 4f,
            lowMid = 2f,
            mid = -1f,
            highMid = -1f,
            presence = 2f,
            brilliance = 3f,
            air = 1f
        )

        val CLASSICAL = EqualizerPreferences(
            id = "classical",
            subBass = 2f,
            bass = 1f,
            lowMid = -1f,
            mid = 0f,
            highMid = 1f,
            presence = 2f,
            brilliance = 3f,
            air = 2f
        )
        val CUSTOM = EqualizerPreferences(id = "custom")
    }
}
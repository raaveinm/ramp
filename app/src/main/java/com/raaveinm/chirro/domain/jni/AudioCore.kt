package com.raaveinm.chirro.domain.jni

import java.nio.ByteBuffer

object AudioCore {
    init {
        System.loadLibrary("chirro")
    }

    external fun test(): String

    /**
     * Initializes the Equalizer with sample rate and channels.
     */
    external fun initEqualizer(sampleRate: Int, numChannels: Int)

    /**
     * Process raw PCM audio data for DSP effects (EQ, Bass Boost, etc.)
     * @param buffer Direct ByteBuffer containing PCM data
     * @param length Length of data in bytes
     */
    external fun processAudio(buffer: ByteBuffer, length: Int)

    /**
     * Calculate FFT for visualization
     * @param buffer Direct ByteBuffer containing PCM data
     * @param length Length of data in bytes
     * @return Array of frequency magnitudes
     */
    external fun calculateFFT(buffer: ByteBuffer, length: Int): FloatArray

    /**
     * Set Equalizer band gain
     * @param band Index of the band
     * @param gain Gain in dB
     */
    external fun setEqualizerBand(band: Int, gain: Float)

    /**
     * Enable/Disable specific DSP effects
     */
    external fun setEffectEnabled(effectId: Int, enabled: Boolean)

    const val EFFECT_EQUALIZER = 1
    const val EFFECT_BASS_BOOST = 2
    const val EFFECT_REVERB = 3
    const val EFFECT_SILENCE_TRIMMER = 4
}

package com.raaveinm.chirro.domain.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.nio.ByteBuffer

@RunWith(AndroidJUnit4::class)
class AudioCoreInstrumentedTest {

    @Test
    fun testInitEqualizer() {
        try {
            AudioCore.initEqualizer(44100, 2)
            // No crash means it works
        } catch (e: Exception) {
            fail("initEqualizer should not throw: ${e.message}")
        }
    }

    @Test
    fun testCalculateFFT() {
        val buffer = ByteBuffer.allocateDirect(2048)
        val result = AudioCore.calculateFFT(buffer, 2048)
        assertNotNull(result)
        assertEquals(64, result.size)
    }

    @Test
    fun testProcessAudio() {
        val buffer = ByteBuffer.allocateDirect(1024)
        try {
            AudioCore.processAudio(buffer, 1024)
        } catch (e: Exception) {
            fail("processAudio should not throw: ${e.message}")
        }
    }
}

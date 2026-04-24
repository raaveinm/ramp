package com.raaveinm.chirro.domain.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ShuffleInstrumentedTest {

    @Test
    fun testShuffleTrackList() {
        val array = longArrayOf(1, 2, 3, 4, 5)
        try {
            Shuffle.shuffleTrackList(array)
        } catch (e: Exception) {
            fail("shuffleTrackList should not throw: ${e.message}")
        }
    }
}

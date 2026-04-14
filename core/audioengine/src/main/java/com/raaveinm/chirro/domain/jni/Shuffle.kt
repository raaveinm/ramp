package com.raaveinm.chirro.domain.jni


import kotlin.collections.map

/**
 * require [precompiled Library](https://github.com/raaveinm/shuffle_lib)
 * @param array LongArray of track ids, which will be shuffled
 */

object Shuffle {
    init {
        System.loadLibrary("chirro")
    }
    external fun shuffleTrackList(array: LongArray)
}

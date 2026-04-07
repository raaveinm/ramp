package com.raaveinm.chirro.domain.jni

import com.raaveinm.chirro.data.values.TrackInfo
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
fun List<TrackInfo>.shuffled(): List<TrackInfo> {
    if (this.isEmpty()) return this
    val array = this.map { it.id }.toLongArray()
    Shuffle.shuffleTrackList(array)
    val trackMap = this.associateBy { it.id }
    return array.map { id -> trackMap[id]?: TrackInfo.EMPTY }
}

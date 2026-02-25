package com.raaveinm.chirro.domain

import com.raaveinm.chirro.data.values.TrackInfo
import kotlin.collections.map

object Shuffle {
    init {
        System.loadLibrary("chirro-shuffle")
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

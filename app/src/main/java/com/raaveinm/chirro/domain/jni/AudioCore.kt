package com.raaveinm.chirro.domain.jni

import java.nio.ByteBuffer

object AudioCore {
    init {
        System.loadLibrary("chirro")
    }
    external fun test() : String
}

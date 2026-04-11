package com.raaveinm.chirro.domain

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.domain.jni.AudioCore
import java.nio.ByteBuffer
import java.nio.ByteOrder

@UnstableApi
class ChirroAudioProcessor : AudioProcessor {

    private var outputFormat = AudioProcessor.AudioFormat.NOT_SET
    private var inputFormat = AudioProcessor.AudioFormat.NOT_SET
    private var isActive = false
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var workingBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var inputEnded = false

    var fftResult: FloatArray? = null
        private set

    override fun configure(inputFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        this.inputFormat = inputFormat

        if (inputFormat.encoding != androidx.media3.common.C.ENCODING_PCM_16BIT) {
            isActive = false
            return AudioProcessor.AudioFormat.NOT_SET
        }
        
        this.outputFormat = inputFormat
        isActive = true
        AudioCore.initEqualizer(inputFormat.sampleRate, inputFormat.channelCount)
        return outputFormat
    }

    override fun isActive(): Boolean = isActive

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!inputBuffer.hasRemaining()) return

        val length = inputBuffer.remaining()


        if (workingBuffer.capacity() < length)
            workingBuffer = ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder())

        workingBuffer.clear()
        workingBuffer.put(inputBuffer)
        workingBuffer.flip()

        fftResult = AudioCore.calculateFFT(workingBuffer, length)
        AudioCore.processAudio(workingBuffer, length)
        if (outputBuffer.capacity() < length)
            outputBuffer = ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder())

        outputBuffer.clear()
        outputBuffer.put(workingBuffer)
        outputBuffer.flip()
        inputBuffer.position(inputBuffer.limit())
    }

    override fun queueEndOfStream() { inputEnded = true }

    override fun getOutput(): ByteBuffer {
        val output = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return output
    }

    override fun isEnded(): Boolean = inputEnded && outputBuffer === AudioProcessor.EMPTY_BUFFER

    override fun flush(streamMetadata: AudioProcessor.StreamMetadata) {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false
    }

    override fun reset() {
        flush(AudioProcessor.StreamMetadata.DEFAULT)
        workingBuffer = AudioProcessor.EMPTY_BUFFER
        inputFormat = AudioProcessor.AudioFormat.NOT_SET
        outputFormat = AudioProcessor.AudioFormat.NOT_SET
        isActive = false
    }
}

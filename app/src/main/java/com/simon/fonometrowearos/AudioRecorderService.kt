package com.simon.fonometrowearos

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.math.sqrt

class AudioRecorderService {
    private val _dbFlow = MutableStateFlow(0f)
    val dbFlow: StateFlow<Float> = _dbFlow.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @SuppressLint("MissingPermission")
    suspend fun startRecording() {
        if (isRecording) return
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                // Log error or handle initialization failure
                return
            }

            audioRecord?.startRecording()
            isRecording = true
            
            withContext(Dispatchers.IO) {
                val buffer = ShortArray(bufferSize)
                while (isRecording) {
                    val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (readResult > 0) {
                        val db = calculateDb(buffer, readResult)
                        _dbFlow.emit(db)
                    }
                    // Small delay to reduce CPU usage if needed, though AudioRecord blocking read handles pacing usually
                    // delay(50) 
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
        }
    }

    fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
    }

    private fun calculateDb(buffer: ShortArray, readSize: Int): Float {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i]
        }
        if (readSize > 0 && sum > 0) {
            val amplitude = sqrt(sum / readSize)
            // Reference amplitude is usually 1, but this formula depends on calibration.
            // 20 * log10(amplitude) gives dB relative to digital full scale if normalized,
            // or relative to 1 unit sample value here.
            // Common simple calibration:
            return (20 * log10(amplitude)).toFloat() + 0 // Add calibration offset if needed
        }
        return 0f
    }
}

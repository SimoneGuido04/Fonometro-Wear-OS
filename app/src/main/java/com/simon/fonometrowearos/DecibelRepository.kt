package com.simon.fonometrowearos

import kotlinx.coroutines.flow.StateFlow

class DecibelRepository(private val audioService: AudioRecorderService) {
    val decibelFlow: StateFlow<Float> = audioService.dbFlow

    suspend fun startListening() {
        audioService.startRecording()
    }

    fun stopListening() {
        audioService.stopRecording()
    }
}

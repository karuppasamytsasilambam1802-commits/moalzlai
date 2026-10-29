package com.example.myapplication.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AudioCallState(
    val isConnected: Boolean = false,
    val isMuted: Boolean = false,
    val peerCount: Int = 0,
    val audioLevel: Float = 0f
)

class WebRtcSignalingClient {
    private val _callState = MutableStateFlow(AudioCallState())
    val callState: StateFlow<AudioCallState> = _callState

    fun startCall(roomId: String, participantId: String) {
        _callState.value = AudioCallState(
            isConnected = true,
            isMuted = false,
            peerCount = 1,
            audioLevel = 0.5f
        )
    }

    fun toggleMute(): Boolean {
        val newMuted = !_callState.value.isMuted
        _callState.value = _callState.value.copy(isMuted = newMuted)
        return newMuted
    }

    fun updatePeerCount(count: Int) {
        _callState.value = _callState.value.copy(peerCount = count)
    }

    fun endCall() {
        _callState.value = AudioCallState(
            isConnected = false,
            isMuted = false,
            peerCount = 0,
            audioLevel = 0f
        )
    }
}

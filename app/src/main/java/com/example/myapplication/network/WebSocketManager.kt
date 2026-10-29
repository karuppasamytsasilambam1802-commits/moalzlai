package com.example.myapplication.network

import com.example.myapplication.data.models.Message
import com.example.myapplication.data.models.WebSocketEvent
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED
}

class WebSocketManager {
    private val gson = Gson()
    private val client = OkHttpClient.Builder().build()
    private var webSocket: WebSocket? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _events = MutableSharedFlow<WebSocketEvent>()
    val events: SharedFlow<WebSocketEvent> = _events

    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect(roomId: String, participantId: String, name: String, language: String) {
        if (_connectionState.value == ConnectionState.CONNECTED) return

        _connectionState.value = ConnectionState.CONNECTING

        val wsUrl = NetworkConfig.getWebSocketUrl(roomId, participantId, name, language)
        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val event = gson.fromJson(text, WebSocketEvent::class.java)
                    scope.launch {
                        _events.emit(event)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.FAILED
            }
        })
    }

    fun sendMessage(message: Message) {
        if (_connectionState.value != ConnectionState.CONNECTED) return
        val event = WebSocketEvent(
            type = "MESSAGE",
            roomId = message.roomId,
            senderId = message.senderId,
            senderName = message.senderName,
            messageObj = message
        )
        webSocket?.send(gson.toJson(event))
    }

    fun sendInterimTranscript(roomId: String, senderId: String, senderName: String, text: String) {
        if (_connectionState.value != ConnectionState.CONNECTED) return
        val event = WebSocketEvent(
            type = "INTERIM",
            roomId = roomId,
            senderId = senderId,
            senderName = senderName,
            payload = text
        )
        webSocket?.send(gson.toJson(event))
    }

    fun disconnect() {
        webSocket?.close(1000, "User Left")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}

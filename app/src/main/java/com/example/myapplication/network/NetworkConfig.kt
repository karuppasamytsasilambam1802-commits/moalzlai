package com.example.myapplication.network

object NetworkConfig {
    private var currentServerUrl: String = "http://10.0.2.2:8000"

    fun getServerUrl(): String {
        return currentServerUrl.trimEnd('/')
    }

    fun setServerUrl(url: String) {
        var formatted = url.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = "http://$formatted"
        }
        currentServerUrl = formatted
    }

    fun getWebSocketUrl(roomId: String, participantId: String, name: String, language: String): String {
        val baseUrl = getServerUrl()
        val wsBase = baseUrl.replace("http://", "ws://").replace("https://", "wss://")
        return "$wsBase/ws/rooms/$roomId?participant_id=$participantId&name=${java.net.URLEncoder.encode(name, "UTF-8")}&language=$language"
    }

    fun isEmulator(): Boolean {
        return currentServerUrl.contains("10.0.2.2")
    }
}

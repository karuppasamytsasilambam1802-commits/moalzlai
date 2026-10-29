package com.example.myapplication.data.models

import com.google.gson.annotations.SerializedName

enum class CommunicationMode {
    @SerializedName("VOICE_AND_TEXT")
    VOICE_AND_TEXT,
    @SerializedName("VOICE_ONLY")
    VOICE_ONLY,
    @SerializedName("TEXT_ONLY")
    TEXT_ONLY,
    @SerializedName("CAPTIONS_ONLY")
    CAPTIONS_ONLY
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}

data class Language(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String
) {
    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            Language("en", "English", "English", "🇺🇸"),
            Language("ta", "Tamil", "தமிழ்", "🇮🇳"),
            Language("hi", "Hindi", "हिन्दी", "🇮🇳"),
            Language("ml", "Malayalam", "മലയാളം", "🇮🇳"),
            Language("te", "Telugu", "తెలుగు", "🇮🇳"),
            Language("kn", "Kannada", "கன்னட / Kannada", "🇮🇳"),
            Language("es", "Spanish", "Español", "🇪🇸"),
            Language("fr", "French", "Français", "🇫🇷"),
            Language("de", "German", "Deutsch", "🇩🇪"),
            Language("zh", "Chinese", "中文", "🇨🇳")
        )

        fun getByCode(code: String): Language {
            return SUPPORTED_LANGUAGES.find { it.code.lowercase() == code.lowercase() }
                ?: Language(code, code.uppercase(), code.uppercase(), "🌐")
        }
    }
}

data class Participant(
    val id: String,
    val name: String,
    val preferredLanguage: String,
    val mode: CommunicationMode = CommunicationMode.VOICE_AND_TEXT,
    val isHost: Boolean = false,
    val isMuted: Boolean = false,
    val isConnected: Boolean = true
)

data class Message(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val originalText: String,
    val sourceLanguage: String,
    val translations: Map<String, String> = emptyMap(),
    val sequenceNumber: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val isFinal: Boolean = true,
    val status: MessageStatus = MessageStatus.SENT,
    val isTyped: Boolean = true
) {
    fun getTranslationFor(targetLangCode: String): String {
        return translations[targetLangCode] ?: translations[targetLangCode.lowercase()] ?: originalText
    }
}

data class Room(
    val roomId: String,
    val hostId: String,
    val hostName: String,
    val supportedModes: List<CommunicationMode> = listOf(CommunicationMode.VOICE_AND_TEXT),
    val maxParticipants: Int = 10,
    val participants: List<Participant> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class CreateRoomRequest(
    val hostName: String,
    val preferredLanguage: String,
    val mode: CommunicationMode = CommunicationMode.VOICE_AND_TEXT,
    val maxParticipants: Int = 10
)

data class JoinRoomRequest(
    val roomId: String,
    val participantName: String,
    val preferredLanguage: String,
    val mode: CommunicationMode = CommunicationMode.VOICE_AND_TEXT
)

data class TranslationRequest(
    val text: String,
    val sourceLanguage: String,
    val targetLanguages: List<String>
)

data class TranslationResponse(
    val originalText: String,
    val sourceLanguage: String,
    val translations: Map<String, String>
)

data class GitHubUser(
    val id: Long,
    val login: String,
    val name: String,
    val avatarUrl: String,
    val email: String? = null,
    val bio: String? = null,
    val htmlUrl: String? = null
)

data class GitHubAuthRequest(
    val code: String? = null,
    val username: String? = null
)

data class GitHubAuthResponse(
    val success: Boolean,
    val message: String,
    val user: GitHubUser? = null
)

data class OnlineClass(
    val classId: String,
    val subjectName: String,
    val instructorName: String,
    val scheduledTime: String = "Live Now",
    val attendeesCount: Int = 1,
    val isLectureActive: Boolean = true
)

data class HandRaiseEvent(
    val studentId: String,
    val studentName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "RAISED" // "RAISED", "LOWERED", "ACKNOWLEDGED"
)

data class WebSocketEvent(
    val type: String, // "JOIN", "LEAVE", "MESSAGE", "INTERIM", "WEBRTC_SIGNAL", "STATUS"
    val roomId: String,
    val senderId: String,
    val senderName: String = "",
    val payload: String = "",
    val messageObj: Message? = null,
    val participantObj: Participant? = null,
    val timestamp: Long = System.currentTimeMillis()
)

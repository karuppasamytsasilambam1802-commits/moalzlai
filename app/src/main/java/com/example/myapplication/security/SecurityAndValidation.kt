package com.example.myapplication.security

enum class UserRole {
    HOST,
    MODERATOR,
    PARTICIPANT
}

data class PermissionCheckResult(
    val isAllowed: Boolean,
    val reason: String = ""
)

object SecurityManager {

    fun checkPermission(role: UserRole, action: String): PermissionCheckResult {
        return when (action.uppercase()) {
            "MUTE_OTHERS", "END_ROOM", "KICK_PARTICIPANT" -> {
                if (role == UserRole.HOST || role == UserRole.MODERATOR) {
                    PermissionCheckResult(true)
                } else {
                    PermissionCheckResult(false, "Requires Host or Moderator privileges.")
                }
            }
            "EXPORT_REPORTS", "CLEAR_MESSAGES" -> {
                if (role == UserRole.HOST) {
                    PermissionCheckResult(true)
                } else {
                    PermissionCheckResult(false, "Requires Host privileges.")
                }
            }
            "SEND_MESSAGE", "SPEAK_AUDIO", "VIEW_CAPTIONS" -> {
                PermissionCheckResult(true)
            }
            else -> PermissionCheckResult(true)
        }
    }

    fun obfuscateText(input: String): String {
        return input.replace(Regex("""\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b"""), "[EMAIL REDACTED]")
    }
}

object InputValidator {

    fun validateRoomCode(code: String): Pair<Boolean, String> {
        val trimmed = code.trim().uppercase()
        if (trimmed.isEmpty()) return false to "Room code cannot be empty."
        if (trimmed.length < 4) return false to "Room code must be at least 4 characters."
        if (!trimmed.matches(Regex("^[A-Z0-9-]+$"))) return false to "Room code can only contain letters, numbers, and hyphens."
        return true to "Valid room code."
    }

    fun sanitizeMessageText(text: String): String {
        return text.trim()
            .take(500) // Maximum length safety limit
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    fun validateServerUrl(url: String): Pair<Boolean, String> {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false to "Server URL cannot be empty."
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return false to "Server URL must start with http:// or https://"
        }
        return true to "Valid server URL."
    }
}

package com.example.myapplication.audit

data class AuditLogEntry(
    val id: String,
    val userId: String,
    val userName: String,
    val userRole: String,
    val action: String, // "CREATE_ROOM", "JOIN_ROOM", "SEND_MESSAGE", "DELETE_RECORD", "EXPORT_REPORT"
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

object AuditLogManager {
    private val logs = mutableListOf<AuditLogEntry>()

    fun logAction(
        userId: String,
        userName: String,
        userRole: String,
        action: String,
        details: String
    ) {
        val entry = AuditLogEntry(
            id = "audit_${System.currentTimeMillis()}_${(100..999).random()}",
            userId = userId,
            userName = userName,
            userRole = userRole,
            action = action,
            details = details
        )
        logs.add(0, entry)
        if (logs.size > 200) {
            logs.removeAt(logs.size - 1)
        }
    }

    fun getAuditLogs(): List<AuditLogEntry> = logs.toList()
}

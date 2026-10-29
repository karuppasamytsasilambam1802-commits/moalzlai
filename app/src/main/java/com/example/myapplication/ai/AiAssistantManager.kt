package com.example.myapplication.ai

import com.example.myapplication.data.models.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AiSummaryResponse(
    val summary: String,
    val keyPoints: List<String>,
    val sentiment: String = "Positive & Collaborative"
)

object AiAssistantManager {

    suspend fun summarizeConversation(messages: List<Message>): AiSummaryResponse = withContext(Dispatchers.IO) {
        if (messages.isEmpty()) {
            return@withContext AiSummaryResponse(
                summary = "No conversation messages to summarize yet.",
                keyPoints = listOf("Start speaking or typing to generate live summaries.")
            )
        }

        val participants = messages.map { it.senderName }.distinct().joinToString(", ")
        val totalMsgs = messages.size

        val keyPoints = messages.take(5).map { msg ->
            "• ${msg.senderName}: \"${msg.originalText.take(60)}\""
        }

        val summary = "Summary of VoxBridge session with $totalMsgs messages between $participants. Key cross-language exchanges were translated and synchronized in real-time."

        return@withContext AiSummaryResponse(
            summary = summary,
            keyPoints = keyPoints
        )
    }

    suspend fun answerQuestion(question: String, messages: List<Message>): String = withContext(Dispatchers.IO) {
        val q = question.lowercase()
        return@withContext when {
            q.contains("who") || q.contains("participant") -> {
                val names = messages.map { it.senderName }.distinct().joinToString(", ")
                "The active participants in this session are: $names."
            }
            q.contains("translate") || q.contains("language") -> {
                val langs = messages.map { it.sourceLanguage.uppercase() }.distinct().joinToString(", ")
                "Languages spoken in this conversation: $langs."
            }
            else -> "AI Assistant: In this conversation, ${messages.size} messages have been exchanged across languages."
        }
    }
}

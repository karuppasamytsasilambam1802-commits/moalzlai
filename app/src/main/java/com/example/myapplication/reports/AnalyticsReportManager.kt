package com.example.myapplication.reports

import com.example.myapplication.data.models.Message
import com.example.myapplication.data.models.Participant

data class AnalyticsData(
    val totalRecords: Int,
    val totalMessages: Int,
    val translatedWordsCount: Int,
    val totalParticipants: Int,
    val dailyStats: List<Pair<String, Int>>,
    val weeklyStats: List<Pair<String, Int>>,
    val monthlyStats: List<Pair<String, Int>>,
    val topLanguages: List<Pair<String, Int>>
)

object AnalyticsReportManager {

    fun generateAnalyticsReport(
        messages: List<Message>,
        participants: List<Participant>
    ): AnalyticsData {
        val totalMsgs = messages.size
        val wordsCount = messages.sumOf { it.originalText.split("\\s+".toRegex()).size }
        val uniqueParticipants = (participants.map { it.name } + messages.map { it.senderName }).distinct().size

        val langMap = mutableMapOf<String, Int>()
        messages.forEach { msg ->
            val lang = msg.sourceLanguage.uppercase()
            langMap[lang] = (langMap[lang] ?: 0) + 1
        }
        val topLangs = langMap.toList().sortedByDescending { it.second }

        // Simulated historical volume breakdowns for Reports Dashboard
        val dailyStats = listOf(
            "Mon" to (totalMsgs * 0.15).toInt().coerceAtLeast(2),
            "Tue" to (totalMsgs * 0.20).toInt().coerceAtLeast(4),
            "Wed" to (totalMsgs * 0.25).toInt().coerceAtLeast(6),
            "Thu" to (totalMsgs * 0.18).toInt().coerceAtLeast(3),
            "Fri" to (totalMsgs * 0.30).toInt().coerceAtLeast(8),
            "Sat" to (totalMsgs * 0.22).toInt().coerceAtLeast(5),
            "Sun" to (totalMsgs * 0.28).toInt().coerceAtLeast(7)
        )

        val weeklyStats = listOf(
            "Week 1" to 42,
            "Week 2" to 68,
            "Week 3" to 85,
            "Week 4" to 110
        )

        val monthlyStats = listOf(
            "Jan" to 180,
            "Feb" to 240,
            "Mar" to 310,
            "Apr" to 450
        )

        return AnalyticsData(
            totalRecords = totalMsgs + uniqueParticipants,
            totalMessages = totalMsgs,
            translatedWordsCount = wordsCount,
            totalParticipants = uniqueParticipants,
            dailyStats = dailyStats,
            weeklyStats = weeklyStats,
            monthlyStats = monthlyStats,
            topLanguages = topLangs
        )
    }
}

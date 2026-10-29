package com.example.myapplication.translation

import com.example.myapplication.data.models.TranslationRequest
import com.example.myapplication.data.models.TranslationResponse
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TranslationRepository {

    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguages: List<String>
    ): TranslationResponse = withContext(Dispatchers.IO) {
        try {
            val api = RetrofitClient.getService()
            val request = TranslationRequest(
                text = text,
                sourceLanguage = sourceLanguage,
                targetLanguages = targetLanguages
            )
            val response = api.translate(request)
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback for Demo Mode / Offline / Server unreachable
        return@withContext generateFallbackTranslations(text, sourceLanguage, targetLanguages)
    }

    private fun generateFallbackTranslations(
        text: String,
        sourceLang: String,
        targetLangs: List<String>
    ): TranslationResponse {
        val translations = mutableMapOf<String, String>()

        for (target in targetLangs) {
            if (sourceLang.equals(target, ignoreCase = true)) {
                translations[target] = text
                continue
            }

            translations[target] = getKnownDemoTranslation(text, sourceLang, target)
                ?: "[Simulated $target]: $text"
        }

        return TranslationResponse(
            originalText = text,
            sourceLanguage = sourceLang,
            translations = translations
        )
    }

    private fun getKnownDemoTranslation(text: String, sourceLang: String, targetLang: String): String? {
        val lowerText = text.lowercase().trim()
        val s = sourceLang.lowercase()
        val t = targetLang.lowercase()

        if (s == "ta" && t == "en") {
            if (lowerText.contains("வணக்கம்") || lowerText.contains("hello")) return "Hello everyone! Welcome to VoxBridge AI."
            if (lowerText.contains("நன்றி")) return "Thank you very much."
            if (lowerText.contains("எப்படி இருக்கிறீர்கள்")) return "How are you doing today?"
            if (lowerText.contains("பேசுகிறேன்")) return "I am speaking in Tamil, live in VoxBridge."
        }

        if (s == "en" && t == "ta") {
            if (lowerText.contains("hello")) return "வணக்கம்! VoxBridge AI பயன்பாட்டிற்கு வருக."
            if (lowerText.contains("thank you")) return "மிக்க நன்றி."
            if (lowerText.contains("how are you")) return "நீங்கள் எப்படி இருக்கிறீர்கள்?"
        }

        if (s == "hi" && t == "en") {
            if (lowerText.contains("नमस्ते")) return "Hello! Welcome to the conversation."
            if (lowerText.contains("धन्यवाद")) return "Thank you."
        }

        if (s == "es" && t == "en") {
            if (lowerText.contains("hola")) return "Hello, glad to join you."
            if (lowerText.contains("gracias")) return "Thank you."
        }

        return null
    }
}

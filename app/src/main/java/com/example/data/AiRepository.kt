package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiRepository"

class AiRepository {
    // Mandated: lazy initialization of Gemini 3.8 Flash model
    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
            }
        )
    }

    suspend fun generateCaption(topic: String, tone: String = "আকর্ষণীয়"): String {
        return withTimeout(60_000L) {
            try {
                val prompt = """
                    You are the official AI assistant of YOURPLATFORM social media.
                    Write a creative social media post caption in Bengali based on this topic: "$topic".
                    Tone: $tone.
                    Include relevant emojis and 3-5 trending hashtags at the end (e.g. #YOURPLATFORM, #bangla).
                    Keep it natural, engaging, and ready to post directly.
                """.trimIndent()

                val response = generalModel.generateContent(prompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    text.trim()
                } else {
                    throw IllegalStateException("Model returned empty text.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Caption generation timed out", e)
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Caption generation failed", e)
                throw e
            }
        }
    }

    suspend fun enhancePostText(currentText: String): String {
        return withTimeout(60_000L) {
            try {
                val prompt = """
                    You are an expert social media editor for YOURPLATFORM.
                    Improve and polish the following post text written by a user:
                    "$currentText"
                    Fix any grammar/spelling, make it more engaging and expressive, add fitting emojis, and suggest 2-3 hashtags.
                    Keep the original language (Bengali or English). Return only the improved post text.
                """.trimIndent()

                val response = generalModel.generateContent(prompt)
                response.text?.trim() ?: currentText
            } catch (e: TimeoutCancellationException) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Enhance post failed", e)
                throw e
            }
        }
    }

    suspend fun generateProfileBio(userName: String, style: String = "স্মার্ট ও আকর্ষণীয়"): String {
        return withTimeout(60_000L) {
            try {
                val prompt = """
                    Write an attractive short profile bio for a user named "$userName" on YOURPLATFORM.
                    Style: $style.
                    Include relevant emojis. Keep it under 150 characters, crisp and catchy.
                    Return only the bio text.
                """.trimIndent()

                val response = generalModel.generateContent(prompt)
                response.text?.trim() ?: "YOURPLATFORM কন্টেন্ট ক্রিয়েটর 🚀"
            } catch (e: TimeoutCancellationException) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Bio generation failed", e)
                throw e
            }
        }
    }

    suspend fun generateCommentSuggestions(postText: String): List<String> {
        return withTimeout(30_000L) {
            try {
                val prompt = """
                    Based on this social media post: "$postText",
                    provide exactly 3 short, friendly, natural reply/comment suggestions in Bengali.
                    Return them as 3 bullet points, each on a new line, each under 6 words with an emoji.
                """.trimIndent()

                val response = generalModel.generateContent(prompt)
                val lines = response.text?.lines()
                    ?.map { it.replace(Regex("^[-*•0-9.\\s]+"), "").trim() }
                    ?.filter { it.isNotBlank() }
                    ?.take(3)
                if (!lines.isNullOrEmpty()) lines else listOf("দারুণ পোস্ট! ❤️", "অনেক ভালো লাগলো 👏", "শুভকামনা রইলো 🌟")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                listOf("দারুণ পোস্ট! ❤️", "অনেক ভালো লাগলো 👏", "শুভকামনা রইলো 🌟")
            }
        }
    }

    fun chatStream(userMessage: String): Flow<String> = flow {
        val prompt = """
            You are the friendly, intelligent AI Assistant of YOURPLATFORM social media app.
            You help users create posts, find ideas for Waves reels, write captions, analyze trends, and answer questions.
            Always answer politely, concisely, and creatively in natural Bengali (or English if the user writes in English).
            
            User message: $userMessage
        """.trimIndent()

        val responseStream = generalModel.generateContentStream(prompt)
        responseStream.collect { chunk ->
            val text = chunk.text
            if (!text.isNullOrEmpty()) {
                emit(text)
            }
        }
    }
}

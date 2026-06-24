package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        systemInstruction: String,
        prompt: String,
        imageBase64: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: API Key is not set. Please configure it in the Secrets panel in AI Studio."
        }

        try {
            val rootJson = JSONObject()

            // System Instruction
            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            }
            rootJson.put("systemInstruction", systemInstructionJson)

            // Contents
            val contentsArray = JSONArray()
            val userContent = JSONObject()
            val partsArray = JSONArray()

            // User prompt part
            partsArray.put(JSONObject().apply { put("text", prompt) })

            // Optional Image part (multimodal OCR)
            if (imageBase64 != null) {
                partsArray.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", imageBase64)
                    })
                })
            }

            userContent.put("parts", partsArray)
            userContent.put("role", "user")
            contentsArray.put(userContent)
            rootJson.put("contents", contentsArray)

            // Configuration
            val generationConfig = JSONObject().apply {
                put("temperature", 0.2) // Low temperature for high precision calculations
            }
            rootJson.put("generationConfig", generationConfig)

            val requestBodyJson = rootJson.toString()
            Log.d(TAG, "Request JSON: $requestBodyJson")

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBodyJson.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                Log.d(TAG, "Response JSON: $responseStr")

                if (!response.isSuccessful) {
                    return@withContext "Error Api (Code ${response.code}): ${response.message}\n$responseStr"
                }

                val jsonResponse = JSONObject(responseStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No response text")
                    }
                }
                return@withContext "Error: No se recibió respuesta de PetroCalc AI."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during call", e)
            return@withContext "Error: ${e.message}"
        }
    }
}

package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Mapped Request/Response Classes ---
@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// --- Retrofit API Service ---
interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

// --- Gemini Business Logic Service Helper ---
object GeminiService {

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Common method to call the Gemini API.
     */
    private suspend fun callGemini(
        prompt: String,
        systemPrompt: String? = null,
        history: List<Pair<String, Boolean>> = emptyList() // Pair of <Text, IsUser>
    ): String = withContext(Dispatchers.IO) {
        val key = getApiKey()
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing or invalid. Please check the Secrets panel in AI Studio and configure GEMINI_API_KEY."
        }

        // Build history context content
        val contentsList = mutableListOf<Content>()
        
        for (item in history) {
            val rolePart = if (item.second) "user" else "model"
            // We assemble a simple visual conversational thread if direct chat models don't auto-resolve roles,
            // or pass role-structured prompt turns.
            // For general generation, sending consecutive user/model parts or single combined prompt is safest.
            contentsList.add(
                Content(parts = listOf(Part(text = "${if (item.second) "Student" else "AI Tutor"}: ${item.first}")))
            )
        }

        // Append current prompt
        contentsList.add(Content(parts = listOf(Part(text = prompt))))

        val systemInstruction = systemPrompt?.let { 
            Content(parts = listOf(Part(text = it)))
        }

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = systemInstruction
        )

        try {
            val response = GeminiClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Empty response received from AI. Please try again."
        } catch (e: Exception) {
            e.printStackTrace()
            "AI Service Unreachable: ${e.localizedMessage ?: "Timeout or Network error"}. Please check your internet connection."
        }
    }

    /**
     * AI Personal Tutor Chat with Session Memory & Bilingual Support
     */
    suspend fun chatWithTutor(
        studentName: String,
        studentClass: String,
        message: String,
        chatHistory: List<TutorMessage>,
        tutorMode: String = "Normal"
    ): String {
        val modeInstruction = when (tutorMode.uppercase()) {
            "SOFT" -> """
                - MODE: Extremely Affectionate & Encouraging ('স্নেহভরা মেন্টর'). 
                - Tone: Warm, sweet, full of praise. Use words like "আমার সোনা", "লক্ষ্মী সোনা", "Dear friend".
                - Relate concepts to fun local things: sweets (rasgulla, sandesh), village ponds, ducks, green fields, flying kites ("ঘুড়ি ওড়ানো"). Speak mostly in sweet Bengali mixed with encouraging English phrases.
            """.trimIndent()
            "STEPBYSTEP" -> """
                - MODE: Step-by-Step Hint Guide ('ধাপে ধাপে সমাধানকারী'). 
                - Action: Under NO circumstances should you give the complete final answer or formula directly for math, science or spelling!
                - Clues: Give exactly one simple clue or small step at a time. Then kindly ask the child: "Can you try the next step?" or "বলতো দেখি এরপর কি হবে?".
            """.trimIndent()
            "BILINGUAL" -> """
                - MODE: Language Helper / English Practice ('ইংরেজি-বাংলা প্র্যাকটিস'). 
                - Focus: Building English-Bengali translation habits.
                - Format: When you write an English sentence, always put the Bengali translation right below it in parenthesis or as parallel bullet points. Highlight new vocabulary (English word followed by its Bengali meaning in square brackets, e.g., 'Discover [আবিষ্কার]').
            """.trimIndent()
            else -> """
                - MODE: Balanced Helper (সাধারণ গাইড).
                - Balance English and Bengali explanations step-by-step nicely.
            """.trimIndent()
        }

        val systemPrompt = """
            You are 'KPS AI Tutor', an affectionate, highly supportive virtual teacher built specifically for primary school children of 'Kharuberia Primary School' in rural Bengal.
            The current student is name '$studentName' who studies in '$studentClass'.
            
            Current selected teaching preference:
            $modeInstruction
            
            General Guidelines:
            1. Language Support: Speak in simple Bengali (বাংলা) and clean, basic English. Mix them appropriately (code-switching / Banglish) so the child can easily read and learn! Always write Bengali responses in native Bengali script.
            2. Child-Friendly: Use simple, clean formatting, bullet points, and encouraging phrases like "খুব ভালো!", "চলো একসঙ্গে শিখি!", "Excellent!", "You can do it!".
            3. Step-by-Step explanation: Break everything down step-by-step. Guide the student so they think and solve the final steps themselves!
            4. Encouragement: If a question is hard, console them and explain it by relating to rural Bengali life (like paddy fields, local games, kites, family, or local school environment).
            5. Clear Formatting: Keep responses conversational, concise, and structured with bold highlights where appropriate.
        """.trimIndent()

        // Map database history to service format
        val historyTurns = chatHistory.map { Pair(it.text, it.role == "user") }

        return callGemini(
            prompt = message,
            systemPrompt = systemPrompt,
            history = historyTurns
        )
    }

    /**
     * AI Homework Creator for Teachers
     */
    suspend fun generateHomework(
        className: String,
        subject: String,
        chapter: String
    ): String {
        val systemPrompt = "You are a professional primary school teacher creator assistant for Kharuberia Primary School."
        val prompt = """
            Create highly engaging Homework assignments for primary class '$className' on the subject of '$subject', Chapter / Theme: '$chapter'.
            
            Please generate exactly:
            1. Homework Title & brief background context.
            2. 3 Multiple Choice Questions (MCQs) with options A, B, C, D and marked Answer.
            3. 3 Short Answer Questions suitable for kids.
            4. 2 Long/Creative Questions.
            5. Simple Practice guidelines for parents to assist.
            
            Language: Use Bengali for instructions/questions if subject is Bengali/Math/Science, and English if the subject is English. Keep it simple and relevant to rural Bengal contexts. Format output with bold headings and neat line breaks.
        """.trimIndent()

        return callGemini(prompt = prompt, systemPrompt = systemPrompt)
    }

    /**
     * AI Worksheet Creator
     */
    suspend fun generateWorksheet(
        className: String,
        subject: String,
        chapter: String,
        worksheetType: String // "Practice", "Revision", "Assignment"
    ): String {
        val systemPrompt = "You are an expert worksheet designer for primary schools."
        val prompt = """
            Generate a '$worksheetType' Worksheet for '$className', Subject: '$subject', Chapter: '$chapter'.
            
            Structure the Worksheet elegantly of layout:
            ------------------------------------------------
            Kharuberia Primary School (KPS)
            $worksheetType Worksheet - Session 2026
            Class: $className | Subject: $subject | Chapter: $chapter
            Student Name: _________________ | Roll No: _____
            ------------------------------------------------
            Instructions: Write down answers in the blank spaces.
            
            Section A: Fill in the Blanks (4 Questions)
            Section B: Matching Columns or True/False (4 Questions)
            Section C: Fun/Creative Activity Task
            
            Write the contents in simple English and Bengali translation where appropriate so kids can easily solve it at home. Use clear alignments and divider line characters.
        """.trimIndent()

        return callGemini(prompt = prompt, systemPrompt = systemPrompt)
    }

    /**
     * AI Question Bank
     */
    suspend fun generateQuestionBank(
        className: String,
        subject: String,
        chapter: String
    ): String {
        val systemPrompt = "You are a curriculum planner and question database builder."
        val prompt = """
            Build a comprehensive Question Bank resource for our teachers at Kharuberia Primary School.
            Class: $className | Subject: $subject | Chapter: $chapter
            
            Please output standard sections:
            1. MCQs: 5 unique questions with options and answer keys.
            2. Short Answer Questions: 5 standard questions with 1-sentence answers for teacher grading reference.
            3. Detailed/Long Questions: 3 conceptual questions.
            4. Creative/Thinking Questions: 2 questions that trigger logical thinking.
            5. Project/Home experiment topic: 1 fun project topic that students can do at home with their parents using household items.
            
            Language should be simplified Bengali/English bilingually. Keep layout highly formatted.
        """.trimIndent()

        return callGemini(prompt = prompt, systemPrompt = systemPrompt)
    }

    /**
     * AI Exam Generator
     */
    suspend fun generateExam(
        className: String,
        subject: String,
        chapter: String,
        examType: String // "Class Test", "Unit Test", "Practice Test", "Mock Test"
    ): String {
        val systemPrompt = "You are a professional primary examination developer."
        val prompt = """
            Generate a printable, kids-friendly '$examType' Question Paper for Class '$className', Subject: '$subject', Chapter: '$chapter'.
            
            Requirements:
            - Full Marks: 20 Marks or 50 Marks (depending on level). Mention it clearly on top right.
            - Time Allowed: 45 Minutes.
            - Balanced Structure:
              - Part I: Very easy, single marks questions (e.g. 5 MCQ/True-False, total 5 marks).
              - Part II: Short questions (e.g. 3 questions of 2 marks, total 6 marks).
              - Part III: Broad/Descriptive question (e.g. 3 questions of 3 marks, total 9 marks).
            - Add a professional school header on top:
              ----------------------------------------------------
              KHARUBERIA PRIMARY SCHOOL — UNIT TEST EXAM 2026
              Class: $className | Subject: $subject
              Time: 45 mins   | Full Marks: 20
              ----------------------------------------------------
            
            Write the text with rich clear formatting and appropriate bilingual instructions or vocabulary footnotes if needed.
        """.trimIndent()

        return callGemini(prompt = prompt, systemPrompt = systemPrompt)
    }

    /**
     * AI-powered Extraction of Holidays from images, PDFs or list text (XLSX)
     */
    suspend fun extractHolidaysFromDocText(rawText: String): String {
        val systemPrompt = "You are a data formatting and OCR parsing bot for Kharuberia Primary School."
        val prompt = """
            Extract a clean, organized, and chronologically ordered list of school holidays from the following raw document text, OCR text, or handwritten description.
            
            Raw Text:
            $rawText
            
            Formatting Rules (CRITICAL):
            - Output should consist ONLY of plain lines of text. Do NOT wrap in ``` markdown codeblocks, do NOT add introductory text, and do NOT add empty lines.
            - Format each vacation/holiday line EXACTLY as:
              YYYY-MM-DD|Holiday Name|Description in Bengali or English
            - Example output line:
              2026-06-12|Eid al-Adha|বকরি ঈদ ও কোরবান
              2026-08-15|Independence Day|স্বাধীনতা দিবস উদযাপন করা হবে
            - Ensure all dates are resolved to the year 2026 (or whichever year is implied). If a date doesn't mention a year, assume 2026.
            - Ensure any Sundays are ignored from dynamic lists unless they are part of a multi-day holiday, in which case output them normally. Sundays represent standard weekend closures anyway.
            - Ignore unnecessary conversations or labels. Just output lines matching: YYYY-MM-DD|Holiday Name|Description
        """.trimIndent()
        return callGemini(prompt = prompt, systemPrompt = systemPrompt)
    }
}

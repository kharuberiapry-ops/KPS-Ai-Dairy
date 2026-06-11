package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val diaryDao = database.diaryDao()
    private val tutorDao = database.tutorDao()
    private val generatedItemDao = database.generatedItemDao()
    private val holidayDao = database.holidayDao()

    // --- State Flows ---
    val currentUser = MutableStateFlow<User?>(null)
    val loginError = MutableStateFlow<String?>(null)

    val allUsers: StateFlow<List<User>> = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<User>> = userDao.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<User>> = userDao.getAllTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDiaryEntries: StateFlow<List<DiaryEntry>> = diaryDao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHolidays: StateFlow<List<Holiday>> = holidayDao.getAllHolidays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentChatMessages = MutableStateFlow<List<TutorMessage>>(emptyList())
    val currentChatMessages: StateFlow<List<TutorMessage>> = _currentChatMessages.asStateFlow()

    val generatedItems: StateFlow<List<GeneratedItem>> = generatedItemDao.getAllGeneratedItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isGeneratingAI = MutableStateFlow(false)
    val aiResponseText = MutableStateFlow<String?>(null)

    init {
        // Initialize sample records immediately on startup so the app is populated
        viewModelScope.launch {
            MockInitializer.initializeDatabaseIfEmpty(database)
        }
    }

    // --- Authentication ---
    fun login(usernameInput: String, passwordInput: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            loginError.value = null
            val usernameTrim = usernameInput.trim()
            val user = userDao.getUserByUsername(usernameTrim)
            if (user != null) {
                if (user.password == passwordInput) {
                    currentUser.value = user
                    onResult(true)
                } else {
                    loginError.value = "Incorrect password. Default is 'KPS@123'"
                    onResult(false)
                }
            } else {
                loginError.value = "Username not found. Try 'Rahul', 'amal', or 'Admin'"
                onResult(false)
            }
        }
    }

    fun logout() {
        currentUser.value = null
        loginError.value = null
        _currentChatMessages.value = emptyList()
        aiResponseText.value = null
        isGeneratingAI.value = false
    }

    // --- User Roles CRUD (Admin Dashboard Actions) ---
    fun addTeacher(username: String, fullName: String) {
        viewModelScope.launch {
            val trimUser = username.trim()
            userDao.insertUser(
                User(
                    username = trimUser,
                    fullName = fullName,
                    role = "TEACHER",
                    password = "KPS@123"
                )
            )
        }
    }

    fun resetPassword(username: String, newPw: String) {
        viewModelScope.launch {
            userDao.updatePassword(username, newPw)
        }
    }

    fun deleteUser(username: String) {
        viewModelScope.launch {
            userDao.deleteUser(username)
        }
    }

    // --- Student Profile Generation (Import Actions) ---
    /**
     * Creates a student with automatically resolved unique username.
     */
    fun addStudentAndGenerateUsername(
        firstNameEnglish: String,
        className: String,
        section: String,
        rollNumber: String
    ): String {
        val baseUsername = firstNameEnglish.trim().replace("\\s".toRegex(), "")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        var finalUsername = baseUsername
        var counter = 1

        viewModelScope.launch {
            // Read all usernames to verify uniqueness
            var existingUser = userDao.getUserByUsername(finalUsername)
            while (existingUser != null) {
                counter++
                finalUsername = "$baseUsername$counter"
                existingUser = userDao.getUserByUsername(finalUsername)
            }

            userDao.insertUser(
                User(
                    username = finalUsername,
                    fullName = "$firstNameEnglish ($className-$section Roll $rollNumber)",
                    role = "STUDENT",
                    password = "KPS@123",
                    studentClass = className,
                    section = section,
                    rollNumber = rollNumber
                )
            )
        }
        return finalUsername
    }

    // --- Student CSV/Excel Importer & Exporter ---
    fun importStudentsFromCSV(csvText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var importCount = 0
            val lines = csvText.split("\n")
            for (line in lines) {
                val parts = if (line.contains("\t")) {
                    line.split("\t")
                } else {
                    line.split(",")
                }
                if (parts.size >= 4) {
                    val firstName = parts[0].trim()
                    val clazz = parts[1].trim()
                    val section = parts[2].trim()
                    val roll = parts[3].trim()

                    if (firstName.isNotEmpty() && firstName != "First Name" && clazz.isNotEmpty()) {
                        addStudentAndGenerateUsername(firstName, clazz, section, roll)
                        importCount++
                    }
                }
            }
            withContext(Dispatchers.Main) {
                onComplete(importCount)
            }
        }
    }

    fun exportStudentsToCSV(): String {
        val currentList = allStudents.value
        val sb = java.lang.StringBuilder()
        sb.append("First Name,Class,Section,Roll Number,Username,Default Password\n")
        for (st in currentList) {
            val rawName = st.fullName.split(" (").firstOrNull() ?: st.fullName
            sb.append("${rawName},${st.studentClass ?: ""},${st.section ?: ""},${st.rollNumber ?: ""},${st.username},${st.password}\n")
        }
        return sb.toString()
    }

    // --- Digital Diary CRUD ---
    fun publishDiaryEntry(
        type: String,
        title: String,
        content: String,
        subject: String,
        teacherName: String,
        targetClass: String,
        targetSection: String,
        attachmentUrl: String? = null
    ) {
        viewModelScope.launch {
            val entry = DiaryEntry(
                type = type,
                title = title,
                content = content,
                date = SimpleDateFormatter.getCurrentDate(),
                subject = subject,
                teacherName = teacherName,
                targetClass = targetClass,
                targetSection = targetSection,
                attachmentUrl = attachmentUrl
            )
            diaryDao.insertEntry(entry)
        }
    }

    fun deleteDiaryEntry(id: Int) {
        viewModelScope.launch {
            diaryDao.deleteEntry(id)
        }
    }

    // --- AI Tutor Chat Actions with memory ---
    val selectedTutorMode = kotlinx.coroutines.flow.MutableStateFlow("Normal")

    fun setTutorMode(mode: String) {
        selectedTutorMode.value = mode
    }

    fun loadChatHistory(studentUsername: String) {
        viewModelScope.launch {
            tutorDao.getChatHistory(studentUsername).collect { list ->
                _currentChatMessages.value = list
            }
        }
    }

    fun sendTutorMessage(studentUsername: String, studentName: String, studentClass: String, text: String) {
        val currentMsg = TutorMessage(studentUsername = studentUsername, role = "user", text = text)
        val mode = selectedTutorMode.value
        viewModelScope.launch {
            // Save user message to DB
            tutorDao.insertMessage(currentMsg)

            // Trigger AI call
            isGeneratingAI.value = true
            val history = tutorDao.getChatHistory(studentUsername).first()
            val aiResponse = GeminiService.chatWithTutor(studentName, studentClass, text, history, tutorMode = mode)
            
            // Save model response message to DB
            val replyMsg = TutorMessage(studentUsername = studentUsername, role = "model", text = aiResponse)
            tutorDao.insertMessage(replyMsg)
            isGeneratingAI.value = false
        }
    }

    fun clearChatHistory(studentUsername: String) {
        viewModelScope.launch {
            tutorDao.clearChatHistory(studentUsername)
            _currentChatMessages.value = emptyList()
        }
    }

    // --- AI Learning Generator Actions (Homework, Worksheets, Exam, QBank) ---
    fun generateAIContent(
        creatorType: String, // "HOMEWORK", "WORKSHEET", "QUESTION_BANK", "EXAM"
        className: String,
        subject: String,
        chapter: String,
        extraParam: String = "" // worksheetType or examType
    ) {
        viewModelScope.launch {
            isGeneratingAI.value = true
            aiResponseText.value = null
            
            val response = when (creatorType) {
                "HOMEWORK" -> GeminiService.generateHomework(className, subject, chapter)
                "WORKSHEET" -> GeminiService.generateWorksheet(className, subject, chapter, extraParam)
                "QUESTION_BANK" -> GeminiService.generateQuestionBank(className, subject, chapter)
                "EXAM" -> GeminiService.generateExam(className, subject, chapter, extraParam)
                else -> "Invalid content type"
            }

            aiResponseText.value = response
            isGeneratingAI.value = false
        }
    }

    fun saveContentAsGeneratedItem(
        type: String,
        subject: String,
        className: String,
        chapter: String,
        content: String
    ) {
        viewModelScope.launch {
            val item = GeneratedItem(
                type = type,
                subject = subject,
                className = className,
                chapterName = chapter,
                contentJson = content
            )
            generatedItemDao.insertGeneratedItem(item)
        }
    }

    fun deleteGeneratedResource(id: Int) {
        viewModelScope.launch {
            generatedItemDao.deleteGeneratedItem(id)
        }
    }

    // --- Holidays Management ---
    fun addHoliday(date: String, name: String, description: String? = null) {
        viewModelScope.launch {
            holidayDao.insertHoliday(Holiday(date = date, name = name, description = description))
        }
    }

    fun deleteHoliday(date: String) {
        viewModelScope.launch {
            holidayDao.deleteHolidayByDate(date)
        }
    }

    fun clearAllHolidays() {
        viewModelScope.launch {
            holidayDao.deleteAllHolidays()
        }
    }

    fun importHolidaysFromDocAI(rawDocText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            isGeneratingAI.value = true
            try {
                val aiLinesStr = GeminiService.extractHolidaysFromDocText(rawDocText)
                
                var successCount = 0
                val parsedHolidays = mutableListOf<Holiday>()
                
                val lines = aiLinesStr.split("\n")
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        val parts = trimmed.split("|")
                        if (parts.size >= 2) {
                            val targetDate = parts[0].trim() // YYYY-MM-DD
                            val holidayName = parts[1].trim()
                            val desc = if (parts.size >= 3) parts[2].trim() else null
                            
                            // Basic validation for YYYY-MM-DD format
                            if (targetDate.matches("\\d{4}-\\d{2}-\\d{2}".toRegex()) && holidayName.isNotEmpty()) {
                                parsedHolidays.add(Holiday(date = targetDate, name = holidayName, description = desc))
                                successCount++
                            }
                        }
                    }
                }
                
                if (parsedHolidays.isNotEmpty()) {
                    holidayDao.insertHolidays(parsedHolidays)
                }
                
                withContext(Dispatchers.Main) {
                    onComplete(successCount)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(0)
                }
            } finally {
                isGeneratingAI.value = false
            }
        }
    }
}

// Simple Date formatter helper
object SimpleDateFormatter {
    fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}

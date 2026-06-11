package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val fullName: String,
    val role: String, // "SUPER_ADMIN", "TEACHER", "STUDENT"
    val password: String = "KPS@123",
    val studentClass: String? = null, // e.g. "Class I", "Class II", "Class III", "Class IV", "Class V"
    val section: String? = null,      // e.g. "A", "B", "C"
    val rollNumber: String? = null    // e.g. "12"
) : Serializable

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "HOMEWORK", "CLASSWORK", "ASSIGNMENT", "STUDY_MATERIAL", "ANNOUNCEMENT"
    val title: String,
    val content: String,
    val date: String, // YYYY-MM-DD
    val subject: String,
    val teacherName: String,
    val targetClass: String, // "Class I" - "Class V", or "All"
    val targetSection: String, // "A", "B", "C", or "All"
    val targetStudentUsername: String? = null, // Null for entire class/section, populated if specific student
    val attachmentUrl: String? = null, // Custom links, e.g. "video:https://youtube.com/..." or "pdf:Notes.pdf"
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "tutor_messages")
data class TutorMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentUsername: String,
    val role: String, // "user", "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "generated_items")
data class GeneratedItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "HOMEWORK", "WORKSHEET", "QUESTION_BANK", "EXAM"
    val subject: String,
    val className: String,
    val chapterName: String,
    val contentJson: String, // Markdown or plain text containing generated content
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "holidays")
data class Holiday(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val name: String,             // Holiday name, e.g. "Id-ul-Fitr", "Rabindra Jayanti"
    val description: String? = null // Optional description or day name
) : Serializable


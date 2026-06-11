package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MockInitializer {

    suspend fun initializeDatabaseIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val userDao = database.userDao()
        val diaryDao = database.diaryDao()

        // Check if DB is empty
        val existingAdmins = userDao.getUsersByRole("SUPER_ADMIN")
        if (existingAdmins.isNotEmpty()) {
            return@withContext
        }

        // 1. Create Default Users
        val defaultUsers = listOf(
            // Admin
            User("Admin", "School Headmaster", "SUPER_ADMIN", "KPS@123"),
            
            // Teachers
            User("amal", "Amal Roy (Math & Science Teacher)", "TEACHER", "KPS@123"),
            User("priya_t", "Priya Das (English & Bengali Teacher)", "TEACHER", "KPS@123"),
            
            // Students
            User("Rahul", "Rahul Sen", "STUDENT", "KPS@123", "Class IV", "A", "1"),
            User("Priya", "Priya Halder", "STUDENT", "KPS@123", "Class III", "B", "15"),
            User("Riya", "Riya Santra", "STUDENT", "KPS@123", "Class V", "A", "5"),
            User("Suman", "Suman Middya", "STUDENT", "KPS@123", "Class II", "C", "8")
        )
        userDao.insertUsers(defaultUsers)

        // 2. Create Default Digital Diary Postings (Classworks, Homeworks, Materials, Announcements)
        val defaultDiaries = listOf(
            DiaryEntry(
                type = "ANNOUNCEMENT",
                title = "Welcome to KPS Digital Diary!",
                content = "Welcome to our newly launched Kharuberia Primary School digital platform. Parents and students can now easily access Homework, Classwork, and learning worksheets online. You can also chat with your AI Personal Tutor in Bengali and English for homework guidance!",
                date = "2026-06-10",
                subject = "General Notice",
                teacherName = "Headmaster (Admin)",
                targetClass = "All",
                targetSection = "All"
            ),
            DiaryEntry(
                type = "HOMEWORK",
                title = "Write a Paragraph on Our School",
                content = "Write a short 10-line paragraph about Kharuberia Primary School in English or Bengali in your copy. Use simple sentences. Your AI Tutor can help you with spelling and sentence structure!",
                date = "2026-06-10",
                subject = "English",
                teacherName = "Priya Das",
                targetClass = "Class IV",
                targetSection = "A",
                attachmentUrl = "doc:English_Exercise_1.pdf"
            ),
            DiaryEntry(
                type = "CLASSWORK",
                title = "Multiplication Table of 12 and 13",
                content = "Today in Math class, we worked on learning multiplication tables 12 and 13. Please write them twice in your home-copy and practice verbal recitation.",
                date = "2026-06-10",
                subject = "Mathematics",
                teacherName = "Amal Roy",
                targetClass = "Class III",
                targetSection = "All"
            ),
            DiaryEntry(
                type = "STUDY_MATERIAL",
                title = "Bengali Alphabet Writing & Pronunciation Guide",
                content = "Learn the correct pronunciation and stroke orders for vowels of the Bengali alphabet. Follow along with the video link provided below.",
                date = "2026-06-09",
                subject = "Bengali",
                teacherName = "Priya Das",
                targetClass = "Class II",
                targetSection = "All",
                attachmentUrl = "video:https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            ),
            DiaryEntry(
                type = "ASSIGNMENT",
                title = "Science Model of Solar System",
                content = "Construct a simple solar system card or draw a neat diagram showing planets relative to the sun. Label each planet in Bengali or English. Submit clear sketches on next Monday.",
                date = "2026-06-08",
                subject = "EVS / Science",
                teacherName = "Amal Roy",
                targetClass = "Class V",
                targetSection = "A"
            )
        )

        for (diary in defaultDiaries) {
            diaryDao.insertEntry(diary)
        }

        // 3. Create Default Holidays for 2026 Calendar
        val holidayDao = database.holidayDao()
        val defaultHolidays = listOf(
            Holiday("2026-06-12", "Bakrid / Eid al-Adha", "বকরি ঈদ ও কোরবান"),
            Holiday("2026-06-23", "Ratha Yatra", "শুভ রথযাত্রা উৎসব"),
            Holiday("2026-07-15", "Summer Half-Holiday", "গ্রীষ্মকালীন বিশেষ ছুটি"),
            Holiday("2026-08-15", "Independence Day", "ভারতের স্বাধীনতা দিবস ও জাতীয় উৎসব")
        )
        holidayDao.insertHolidays(defaultHolidays)
    }
}

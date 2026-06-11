package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = 'STUDENT' ORDER BY studentClass ASC, rollNumber ASC")
    fun getAllStudents(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = 'TEACHER' ORDER BY fullName ASC")
    fun getAllTeachers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Query("UPDATE users SET password = :password WHERE username = :username")
    suspend fun updatePassword(username: String, password: String)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String)
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE targetClass = :className OR targetClass = 'All' ORDER BY timestamp DESC")
    fun getEntriesForClass(className: String): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntry(id: Int)
}

@Dao
interface TutorDao {
    @Query("SELECT * FROM tutor_messages WHERE studentUsername = :student ORDER BY timestamp ASC")
    fun getChatHistory(student: String): Flow<List<TutorMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: TutorMessage)

    @Query("DELETE FROM tutor_messages WHERE studentUsername = :student")
    suspend fun clearChatHistory(student: String)
}

@Dao
interface GeneratedItemDao {
    @Query("SELECT * FROM generated_items ORDER BY timestamp DESC")
    fun getAllGeneratedItems(): Flow<List<GeneratedItem>>

    @Query("SELECT * FROM generated_items WHERE type = :type ORDER BY timestamp DESC")
    fun getGeneratedItemsByType(type: String): Flow<List<GeneratedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedItem(item: GeneratedItem)

    @Query("DELETE FROM generated_items WHERE id = :id")
    suspend fun deleteGeneratedItem(id: Int)
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY date ASC")
    fun getAllHolidays(): Flow<List<Holiday>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: Holiday)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<Holiday>)

    @Query("DELETE FROM holidays")
    suspend fun deleteAllHolidays()

    @Query("DELETE FROM holidays WHERE date = :date")
    suspend fun deleteHolidayByDate(date: String)
}

@Database(
    entities = [User::class, DiaryEntry::class, TutorMessage::class, GeneratedItem::class, Holiday::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun diaryDao(): DiaryDao
    abstract fun tutorDao(): TutorDao
    abstract fun generatedItemDao(): GeneratedItemDao
    abstract fun holidayDao(): HolidayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kps_digital_diary_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

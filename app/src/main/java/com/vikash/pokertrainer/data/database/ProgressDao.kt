package com.vikash.pokertrainer.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vikash.pokertrainer.data.model.UserProgress
import com.vikash.pokertrainer.data.model.PlayerTypeProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Insert
    suspend fun insertProgress(progress: UserProgress)

    @Insert
    suspend fun insertPlayerTypeProgress(progress: PlayerTypeProgress)

    @Query("SELECT * FROM user_progress ORDER BY timestamp DESC")
    fun getAllProgress(): Flow<List<UserProgress>>

    @Query("SELECT COUNT(*) FROM user_progress")
    fun getTotalAttempted(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_progress WHERE wasCorrect = 1")
    fun getTotalCorrect(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_progress WHERE street = :street")
    fun getAttemptedByStreet(street: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_progress WHERE street = :street AND wasCorrect = 1")
    fun getCorrectByStreet(street: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM player_type_progress")
    fun getPlayerTypeAttempted(): Flow<Int>

    @Query("SELECT COUNT(*) FROM player_type_progress WHERE wasCorrect = 1")
    fun getPlayerTypeCorrect(): Flow<Int>

    @Query("SELECT category, COUNT(*) as total, SUM(CASE WHEN wasCorrect = 1 THEN 1 ELSE 0 END) as correct FROM user_progress GROUP BY category")
    fun getCategoryStats(): Flow<List<CategoryStat>>

    @Query("DELETE FROM user_progress")
    suspend fun clearAllProgress()

    @Query("DELETE FROM player_type_progress")
    suspend fun clearAllPlayerTypeProgress()
}

data class CategoryStat(
    val category: String,
    val total: Int,
    val correct: Int
)

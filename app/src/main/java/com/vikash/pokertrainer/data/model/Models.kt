package com.vikash.pokertrainer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Scenario(
    val id: String,
    val hand: String,
    val position: String,
    val board: List<String>,
    val street: Street,
    val potSize: String,
    val stackSize: String,
    val villainAction: String,
    val options: List<String>,
    val correct: String,
    val explanation: String,
    val category: String
)

enum class Street {
    PREFLOP, FLOP, TURN, RIVER
}

data class PlayerProfile(
    val name: String,
    val type: PlayerType,
    val vpip: Int,
    val pfr: Int,
    val af: Double,
    val threeBet: Double,
    val foldToThreeBet: Double,
    val cbet: Double,
    val wtsd: Double,
    val description: String,
    val bestStrategy: String,
    val strategyExplanation: String
)

enum class PlayerType {
    CALLING_STATION, NIT, MANIAC, TAG, LAG, FISH
}

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scenarioId: String,
    val street: String,
    val category: String,
    val wasCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_type_progress")
data class PlayerTypeProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileName: String,
    val wasCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GtoEntry(
    val hand: String,
    val action: GtoAction,
    val frequency: Double,
    val explanation: String
)

enum class GtoAction {
    RAISE, CALL, FOLD
}

data class UserStats(
    val totalAttempted: Int,
    val totalCorrect: Int,
    val accuracy: Double,
    val preflopAccuracy: Double,
    val flopAccuracy: Double,
    val turnAccuracy: Double,
    val riverAccuracy: Double,
    val weaknesses: List<String>,
    val strengths: List<String>,
    val playerTypeAccuracy: Double
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }
}

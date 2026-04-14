package com.vikash.pokertrainer.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vikash.pokertrainer.data.database.AppDatabase
import com.vikash.pokertrainer.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PokerRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.progressDao()
    private val gson = Gson()

    private var scenariosCache: List<Scenario>? = null
    private var playerProfilesCache: List<PlayerProfile>? = null

    fun getScenarios(context: Context): List<Scenario> {
        if (scenariosCache == null) {
            val json = context.assets.open("scenarios.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Scenario>>() {}.type
            scenariosCache = gson.fromJson(json, type)
        }
        return scenariosCache!!
    }

    fun getScenariosByStreet(context: Context, street: Street): List<Scenario> {
        return getScenarios(context).filter { it.street == street }
    }

    fun getPlayerProfiles(context: Context): List<PlayerProfile> {
        if (playerProfilesCache == null) {
            val json = context.assets.open("player_profiles.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<PlayerProfile>>() {}.type
            playerProfilesCache = gson.fromJson(json, type)
        }
        return playerProfilesCache!!
    }

    suspend fun saveProgress(progress: UserProgress) {
        dao.insertProgress(progress)
    }

    suspend fun savePlayerTypeProgress(progress: PlayerTypeProgress) {
        dao.insertPlayerTypeProgress(progress)
    }

    fun getTotalAttempted(): Flow<Int> = dao.getTotalAttempted()
    fun getTotalCorrect(): Flow<Int> = dao.getTotalCorrect()

    fun getAccuracyByStreet(street: String): Flow<Double> {
        return dao.getAttemptedByStreet(street).combine(dao.getCorrectByStreet(street)) { total, correct ->
            if (total > 0) (correct.toDouble() / total * 100) else 0.0
        }
    }

    fun getPlayerTypeAccuracy(): Flow<Double> {
        return dao.getPlayerTypeAttempted().combine(dao.getPlayerTypeCorrect()) { total, correct ->
            if (total > 0) (correct.toDouble() / total * 100) else 0.0
        }
    }

    fun getCategoryStats() = dao.getCategoryStats()

    suspend fun clearAllProgress() {
        dao.clearAllProgress()
        dao.clearAllPlayerTypeProgress()
    }
}

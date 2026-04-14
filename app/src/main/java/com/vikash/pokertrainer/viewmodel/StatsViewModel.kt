package com.vikash.pokertrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vikash.pokertrainer.data.database.CategoryStat
import com.vikash.pokertrainer.data.repository.PokerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StatsUiState(
    val totalAttempted: Int = 0,
    val totalCorrect: Int = 0,
    val overallAccuracy: Double = 0.0,
    val preflopAccuracy: Double = 0.0,
    val flopAccuracy: Double = 0.0,
    val turnAccuracy: Double = 0.0,
    val riverAccuracy: Double = 0.0,
    val playerTypeAccuracy: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val strengths: List<String> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PokerRepository(application)
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                repository.getTotalAttempted(),
                repository.getTotalCorrect(),
                repository.getAccuracyByStreet("PREFLOP"),
                repository.getAccuracyByStreet("FLOP"),
                repository.getAccuracyByStreet("TURN"),
                repository.getAccuracyByStreet("RIVER"),
            ) { values ->
                val total = values[0] as Int
                val correct = values[1] as Int
                val preflopAcc = values[2] as Double
                val flopAcc = values[3] as Double
                val turnAcc = values[4] as Double
                val riverAcc = values[5] as Double
                val overall = if (total > 0) (correct.toDouble() / total * 100) else 0.0

                val weaknesses = mutableListOf<String>()
                val strengths = mutableListOf<String>()

                if (total >= 5) {
                    mapOf("Preflop" to preflopAcc, "Flop" to flopAcc, "Turn" to turnAcc, "River" to riverAcc)
                        .forEach { (street, acc) ->
                            when {
                                acc < 40 -> weaknesses.add("Struggling with $street decisions")
                                acc < 60 -> weaknesses.add("$street needs improvement")
                                acc >= 80 -> strengths.add("Strong $street play")
                                acc >= 60 -> strengths.add("Decent $street understanding")
                            }
                        }
                }

                StatsUiState(
                    totalAttempted = total,
                    totalCorrect = correct,
                    overallAccuracy = overall,
                    preflopAccuracy = preflopAcc,
                    flopAccuracy = flopAcc,
                    turnAccuracy = turnAcc,
                    riverAccuracy = riverAcc
                ).copy(weaknesses = weaknesses, strengths = strengths)
            }.collect { state ->
                _uiState.value = state
            }
        }

        viewModelScope.launch {
            repository.getPlayerTypeAccuracy().collect { acc ->
                _uiState.value = _uiState.value.copy(playerTypeAccuracy = acc)
            }
        }

        viewModelScope.launch {
            repository.getCategoryStats().collect { stats ->
                _uiState.value = _uiState.value.copy(categoryStats = stats)
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.clearAllProgress()
        }
    }
}

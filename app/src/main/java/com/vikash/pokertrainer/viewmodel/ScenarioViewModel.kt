package com.vikash.pokertrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vikash.pokertrainer.data.model.*
import com.vikash.pokertrainer.data.repository.PokerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScenarioUiState(
    val currentScenario: Scenario? = null,
    val selectedAnswer: String? = null,
    val showResult: Boolean = false,
    val isCorrect: Boolean = false,
    val currentIndex: Int = 0,
    val totalScenarios: Int = 0,
    val selectedStreet: Street? = null,
    val sessionCorrect: Int = 0,
    val sessionTotal: Int = 0
)

class ScenarioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PokerRepository(application)
    private val _uiState = MutableStateFlow(ScenarioUiState())
    val uiState: StateFlow<ScenarioUiState> = _uiState.asStateFlow()

    private var scenarios: List<Scenario> = emptyList()

    fun loadScenarios(street: Street? = null) {
        val context = getApplication<Application>()
        scenarios = if (street != null) {
            repository.getScenariosByStreet(context, street)
        } else {
            repository.getScenarios(context)
        }.shuffled()

        _uiState.value = ScenarioUiState(
            currentScenario = scenarios.firstOrNull(),
            totalScenarios = scenarios.size,
            selectedStreet = street,
            currentIndex = 0
        )
    }

    fun selectAnswer(answer: String) {
        val current = _uiState.value.currentScenario ?: return
        val isCorrect = answer == current.correct

        _uiState.value = _uiState.value.copy(
            selectedAnswer = answer,
            showResult = true,
            isCorrect = isCorrect,
            sessionCorrect = _uiState.value.sessionCorrect + if (isCorrect) 1 else 0,
            sessionTotal = _uiState.value.sessionTotal + 1
        )

        viewModelScope.launch {
            repository.saveProgress(
                UserProgress(
                    scenarioId = current.id,
                    street = current.street.name,
                    category = current.category,
                    wasCorrect = isCorrect,
                    userAnswer = answer,
                    correctAnswer = current.correct
                )
            )
        }
    }

    fun nextScenario() {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex < scenarios.size) {
            _uiState.value = _uiState.value.copy(
                currentScenario = scenarios[nextIndex],
                selectedAnswer = null,
                showResult = false,
                isCorrect = false,
                currentIndex = nextIndex
            )
        } else {
            // Reshuffle and restart
            scenarios = scenarios.shuffled()
            _uiState.value = _uiState.value.copy(
                currentScenario = scenarios.firstOrNull(),
                selectedAnswer = null,
                showResult = false,
                isCorrect = false,
                currentIndex = 0
            )
        }
    }
}

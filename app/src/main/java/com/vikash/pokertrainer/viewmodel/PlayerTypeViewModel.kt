package com.vikash.pokertrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vikash.pokertrainer.data.model.PlayerProfile
import com.vikash.pokertrainer.data.model.PlayerType
import com.vikash.pokertrainer.data.model.PlayerTypeProgress
import com.vikash.pokertrainer.data.repository.PokerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerTypeUiState(
    val currentProfile: PlayerProfile? = null,
    val selectedType: PlayerType? = null,
    val showResult: Boolean = false,
    val isCorrect: Boolean = false,
    val currentIndex: Int = 0,
    val totalProfiles: Int = 0,
    val sessionCorrect: Int = 0,
    val sessionTotal: Int = 0,
    /** Full list of profiles (stable, unshuffled) for Browse mode. */
    val allProfiles: List<PlayerProfile> = emptyList()
)

class PlayerTypeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PokerRepository(application)
    private val _uiState = MutableStateFlow(PlayerTypeUiState())
    val uiState: StateFlow<PlayerTypeUiState> = _uiState.asStateFlow()

    private var profiles: List<PlayerProfile> = emptyList()

    fun loadProfiles() {
        val context = getApplication<Application>()
        val allProfiles = repository.getPlayerProfiles(context)
        profiles = allProfiles.shuffled()
        _uiState.value = PlayerTypeUiState(
            currentProfile = profiles.firstOrNull(),
            totalProfiles = profiles.size,
            allProfiles = allProfiles
        )
    }

    fun selectType(type: PlayerType) {
        val current = _uiState.value.currentProfile ?: return
        val isCorrect = type == current.type

        _uiState.value = _uiState.value.copy(
            selectedType = type,
            showResult = true,
            isCorrect = isCorrect,
            sessionCorrect = _uiState.value.sessionCorrect + if (isCorrect) 1 else 0,
            sessionTotal = _uiState.value.sessionTotal + 1
        )

        viewModelScope.launch {
            repository.savePlayerTypeProgress(
                PlayerTypeProgress(
                    profileName = current.name,
                    wasCorrect = isCorrect,
                    userAnswer = type.name,
                    correctAnswer = current.type.name
                )
            )
        }
    }

    fun nextProfile() {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex < profiles.size) {
            _uiState.value = _uiState.value.copy(
                currentProfile = profiles[nextIndex],
                selectedType = null,
                showResult = false,
                isCorrect = false,
                currentIndex = nextIndex
            )
        } else {
            profiles = profiles.shuffled()
            _uiState.value = _uiState.value.copy(
                currentProfile = profiles.firstOrNull(),
                selectedType = null,
                showResult = false,
                isCorrect = false,
                currentIndex = 0
            )
        }
    }
}

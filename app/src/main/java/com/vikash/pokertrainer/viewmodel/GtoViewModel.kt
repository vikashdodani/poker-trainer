package com.vikash.pokertrainer.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GtoHandInfo(
    val hand: String,
    val action: String,
    val color: Int, // 0=fold, 1=call, 2=raise, 3=mixed
    val explanation: String
)

data class GtoUiState(
    val selectedPosition: String = "BTN",
    val selectedHand: GtoHandInfo? = null,
    val showHandDetail: Boolean = false,
    val gameType: String = "Cash"
)

class GtoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GtoUiState())
    val uiState: StateFlow<GtoUiState> = _uiState.asStateFlow()

    val positions = listOf("UTG", "MP", "CO", "BTN", "SB", "BB")
    val ranks = listOf("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2")

    // GTO preflop ranges by position - returns action for each hand
    fun getHandAction(row: Int, col: Int, position: String): GtoHandInfo {
        val rank1 = ranks[row]
        val rank2 = ranks[col]
        val hand = if (row == col) {
            "$rank1$rank2" // Pocket pair
        } else if (row < col) {
            "$rank1${rank2}s" // Suited (above diagonal)
        } else {
            "$rank2${rank1}o" // Offsuit (below diagonal)
        }

        val (action, color, explanation) = getGtoAction(hand, position)
        return GtoHandInfo(hand, action, color, explanation)
    }

    private fun getGtoAction(hand: String, position: String): Triple<String, Int, String> {
        val isPair = hand.length == 2
        val isSuited = hand.endsWith("s")
        val rank1 = hand[0]
        val rank2 = hand[1]

        // Simplified GTO ranges
        return when (position) {
            "UTG" -> getUtgAction(hand, isPair, isSuited, rank1, rank2)
            "MP" -> getMpAction(hand, isPair, isSuited, rank1, rank2)
            "CO" -> getCoAction(hand, isPair, isSuited, rank1, rank2)
            "BTN" -> getBtnAction(hand, isPair, isSuited, rank1, rank2)
            "SB" -> getSbAction(hand, isPair, isSuited, rank1, rank2)
            "BB" -> getBbAction(hand, isPair, isSuited, rank1, rank2)
            else -> Triple("Fold", 0, "Default fold")
        }
    }

    private fun highCardValue(c: Char): Int = when(c) {
        'A' -> 14; 'K' -> 13; 'Q' -> 12; 'J' -> 11; 'T' -> 10
        else -> c.digitToInt()
    }

    private fun getUtgAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char): Triple<String, Int, String> {
        if (isPair) {
            val v = highCardValue(r1)
            return when {
                v >= 10 -> Triple("Raise", 2, "Premium pair \u2014 always raise from UTG")
                v >= 7 -> Triple("Raise", 2, "Medium pair \u2014 raise for set mining value")
                v >= 4 -> Triple("Call/Fold", 3, "Small pair \u2014 position dependent, often fold UTG")
                else -> Triple("Fold", 0, "Low pair \u2014 fold from early position")
            }
        }
        if (isSuited) {
            return when {
                r1 == 'A' && highCardValue(hand[1]) >= 10 -> Triple("Raise", 2, "Premium suited Ace \u2014 raise for value")
                r1 == 'A' && highCardValue(hand[1]) >= 5 -> Triple("Raise", 2, "Suited Ace \u2014 raise, good playability postflop")
                r1 == 'A' -> Triple("Call/Fold", 3, "Weak suited Ace \u2014 marginal from UTG")
                r1 == 'K' && highCardValue(hand[1]) >= 10 -> Triple("Raise", 2, "Strong suited King \u2014 raise")
                highCardValue(r1) >= 10 && highCardValue(r1) - highCardValue(hand[1]) <= 2 -> Triple("Raise", 2, "Suited broadway \u2014 raise")
                else -> Triple("Fold", 0, "Too weak for UTG \u2014 fold")
            }
        }
        // Offsuit
        return when {
            r1 == 'A' && highCardValue(hand[1]) >= 11 -> Triple("Raise", 2, "Strong Ace-high offsuit \u2014 raise")
            r1 == 'K' && hand[1] == 'Q' -> Triple("Raise", 2, "KQo \u2014 strong enough for UTG raise")
            else -> Triple("Fold", 0, "Offsuit hand too weak for UTG")
        }
    }

    private fun getMpAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char): Triple<String, Int, String> {
        if (isPair) {
            val v = highCardValue(r1)
            return when {
                v >= 7 -> Triple("Raise", 2, "Raise medium+ pairs from MP")
                v >= 4 -> Triple("Raise", 2, "Small pairs playable from MP")
                else -> Triple("Fold", 0, "Fold lowest pairs from MP")
            }
        }
        if (isSuited) {
            return when {
                r1 == 'A' -> Triple("Raise", 2, "All suited Aces playable from MP")
                r1 == 'K' && highCardValue(hand[1]) >= 9 -> Triple("Raise", 2, "Suited King \u2014 raise from MP")
                highCardValue(r1) >= 10 && highCardValue(hand[1]) >= 9 -> Triple("Raise", 2, "Suited connectors/broadways \u2014 raise")
                else -> Triple("Fold", 0, "Too speculative for MP")
            }
        }
        return when {
            r1 == 'A' && highCardValue(hand[1]) >= 10 -> Triple("Raise", 2, "Strong Ace offsuit \u2014 raise")
            r1 == 'K' && highCardValue(hand[1]) >= 11 -> Triple("Raise", 2, "KQ/KJ offsuit \u2014 raise from MP")
            else -> Triple("Fold", 0, "Fold weak offsuit hands from MP")
        }
    }

    private fun getCoAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char): Triple<String, Int, String> {
        if (isPair) return Triple("Raise", 2, "All pocket pairs playable from CO")
        if (isSuited) {
            return when {
                r1 == 'A' -> Triple("Raise", 2, "All suited Aces \u2014 raise from CO")
                r1 == 'K' -> Triple("Raise", 2, "All suited Kings \u2014 raise from CO")
                highCardValue(r1) >= 9 -> Triple("Raise", 2, "Suited broadways/connectors \u2014 raise from CO")
                highCardValue(r1) - highCardValue(hand[1]) <= 2 -> Triple("Call", 1, "Suited connectors \u2014 can open from CO")
                else -> Triple("Fold", 0, "Too weak even for CO")
            }
        }
        return when {
            r1 == 'A' && highCardValue(hand[1]) >= 8 -> Triple("Raise", 2, "Ace-high offsuit \u2014 raise from CO")
            r1 == 'K' && highCardValue(hand[1]) >= 10 -> Triple("Raise", 2, "King-high offsuit \u2014 raise from CO")
            r1 == 'Q' && highCardValue(hand[1]) >= 10 -> Triple("Raise", 2, "Queen-high broadway \u2014 raise from CO")
            highCardValue(r1) >= 10 && highCardValue(hand[1]) >= 10 -> Triple("Raise", 2, "Broadway offsuit \u2014 raise from CO")
            else -> Triple("Fold", 0, "Offsuit hand not strong enough for CO open")
        }
    }

    private fun getBtnAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char): Triple<String, Int, String> {
        if (isPair) return Triple("Raise", 2, "All pocket pairs \u2014 always raise on BTN")
        if (isSuited) {
            return when {
                r1 == 'A' -> Triple("Raise", 2, "All suited Aces \u2014 raise on BTN")
                r1 == 'K' -> Triple("Raise", 2, "All suited Kings \u2014 raise on BTN")
                highCardValue(r1) >= 7 -> Triple("Raise", 2, "Wide suited range \u2014 raise on BTN")
                else -> Triple("Call", 1, "Weak suited \u2014 can mix raise/fold on BTN")
            }
        }
        return when {
            r1 == 'A' -> Triple("Raise", 2, "All Ace-x offsuit \u2014 raise on BTN")
            r1 == 'K' && highCardValue(hand[1]) >= 5 -> Triple("Raise", 2, "King-high offsuit \u2014 raise on BTN")
            highCardValue(r1) >= 10 && highCardValue(hand[1]) >= 7 -> Triple("Raise", 2, "Broadway+ offsuit \u2014 raise on BTN")
            highCardValue(r1) >= 9 && highCardValue(r1) - highCardValue(hand[1]) <= 2 -> Triple("Raise", 2, "Connected offsuit \u2014 raise on BTN")
            else -> Triple("Fold", 0, "Even BTN has limits \u2014 fold weakest hands")
        }
    }

    private fun getSbAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char): Triple<String, Int, String> {
        if (isPair) {
            val v = highCardValue(r1)
            return when {
                v >= 9 -> Triple("Raise", 2, "Premium/medium pair \u2014 raise from SB")
                else -> Triple("Call", 1, "Small pair \u2014 complete or raise from SB")
            }
        }
        if (isSuited) {
            return when {
                r1 == 'A' -> Triple("Raise", 2, "Suited Ace \u2014 raise from SB")
                r1 == 'K' && highCardValue(hand[1]) >= 8 -> Triple("Raise", 2, "Suited King \u2014 raise from SB")
                highCardValue(r1) >= 10 -> Triple("Call", 1, "Suited broadway \u2014 call/raise from SB")
                else -> Triple("Call", 1, "Suited hand \u2014 can complete from SB")
            }
        }
        return when {
            r1 == 'A' && highCardValue(hand[1]) >= 9 -> Triple("Raise", 2, "Strong Ace offsuit \u2014 raise from SB")
            r1 == 'K' && highCardValue(hand[1]) >= 11 -> Triple("Raise", 2, "KQ/KJ \u2014 raise from SB")
            highCardValue(r1) >= 10 && highCardValue(hand[1]) >= 10 -> Triple("Call", 1, "Broadway offsuit \u2014 can complete from SB")
            else -> Triple("Fold", 0, "Weak hand \u2014 fold from SB despite price")
        }
    }

    private fun getBbAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char): Triple<String, Int, String> {
        if (isPair) return Triple("Raise", 2, "All pocket pairs \u2014 3-bet or call from BB")
        if (isSuited) {
            return when {
                r1 == 'A' -> Triple("Raise", 2, "Suited Ace \u2014 3-bet from BB")
                highCardValue(r1) >= 9 -> Triple("Call", 1, "Suited broadway \u2014 defend BB")
                highCardValue(r1) - highCardValue(hand[1]) <= 3 -> Triple("Call", 1, "Suited connector \u2014 defend BB")
                else -> Triple("Call", 1, "Getting good price \u2014 can defend suited hands in BB")
            }
        }
        return when {
            r1 == 'A' && highCardValue(hand[1]) >= 10 -> Triple("Raise", 2, "Strong Ace \u2014 3-bet from BB")
            r1 == 'A' -> Triple("Call", 1, "Ace-x offsuit \u2014 defend BB")
            r1 == 'K' && highCardValue(hand[1]) >= 10 -> Triple("Call", 1, "King-high broadway \u2014 defend BB")
            highCardValue(r1) >= 10 && highCardValue(hand[1]) >= 8 -> Triple("Call", 1, "Broadway \u2014 defend BB with good price")
            else -> Triple("Fold", 0, "Too weak to defend \u2014 fold from BB")
        }
    }

    fun selectPosition(position: String) {
        _uiState.value = _uiState.value.copy(
            selectedPosition = position,
            selectedHand = null,
            showHandDetail = false
        )
    }

    fun selectHand(handInfo: GtoHandInfo) {
        _uiState.value = _uiState.value.copy(
            selectedHand = handInfo,
            showHandDetail = true
        )
    }

    fun dismissHandDetail() {
        _uiState.value = _uiState.value.copy(showHandDetail = false)
    }

    fun setGameType(type: String) {
        _uiState.value = _uiState.value.copy(gameType = type)
    }
}

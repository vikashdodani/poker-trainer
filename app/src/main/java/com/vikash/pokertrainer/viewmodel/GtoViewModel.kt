package com.vikash.pokertrainer.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GtoHandInfo(
    val hand: String,
    val action: String,
    val color: Int, // 0=fold, 1=call, 2=raise, 3=mixed
    val explanation: String,
    val raiseFreq: Int = 0,
    val callFreq: Int = 0,
    val foldFreq: Int = 100
)

data class RangeStats(
    val raiseCount: Int,
    val callCount: Int,
    val foldCount: Int,
    val mixedCount: Int
) {
    val total = 169
    val raisePercent get() = (raiseCount * 100) / total
    val callPercent get() = (callCount * 100) / total
    val foldPercent get() = (foldCount * 100) / total
    val mixedPercent get() = (mixedCount * 100) / total
    val playedCount get() = raiseCount + callCount + mixedCount
}

data class GtoUiState(
    val selectedPosition: String = "BTN",
    val selectedHand: GtoHandInfo? = null,
    val showHandDetail: Boolean = false,
    val gameType: String = "Cash",
    val positionHandsForSelected: Map<String, GtoHandInfo> = emptyMap()
)

class GtoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GtoUiState())
    val uiState: StateFlow<GtoUiState> = _uiState.asStateFlow()

    val positions = listOf("UTG", "MP", "CO", "BTN", "SB", "BB")
    val ranks = listOf("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2")

    val positionDescriptions = mapOf(
        "UTG" to "Under the Gun · First to act, tightest range",
        "MP" to "Middle Position · Slightly wider than UTG",
        "CO" to "Cut-off · Wide range, strong position",
        "BTN" to "Button · Last to act, widest opening range",
        "SB" to "Small Blind · Act first postflop, tighter defense",
        "BB" to "Big Blind · Getting odds to defend wide"
    )

    // Internal result type carrying action + frequencies
    private data class R(
        val action: String,
        val color: Int,
        val explanation: String,
        val raiseFreq: Int = 0,
        val callFreq: Int = 0,
        val foldFreq: Int = 100
    )

    private fun raise(expl: String, pct: Int = 100) = R("Raise", 2, expl, pct, 0, 100 - pct)
    private fun call(expl: String, pct: Int = 100) = R("Call", 1, expl, 0, pct, 100 - pct)
    private fun fold(expl: String) = R("Fold", 0, expl, 0, 0, 100)
    private fun mixed(label: String, expl: String, rPct: Int = 0, cPct: Int = 0) =
        R(label, 3, expl, rPct, cPct, 100 - rPct - cPct)

    fun getHandAction(row: Int, col: Int, position: String, gameType: String = "Cash"): GtoHandInfo {
        val rank1 = ranks[row]
        val rank2 = ranks[col]
        val hand = when {
            row == col -> "$rank1$rank2"
            row < col -> "$rank1${rank2}s"
            else -> "$rank2${rank1}o"
        }
        val res = getGtoAction(hand, position, gameType == "Tournament")
        return GtoHandInfo(hand, res.action, res.color, res.explanation, res.raiseFreq, res.callFreq, res.foldFreq)
    }

    fun getRangeStats(position: String, gameType: String): RangeStats {
        var raise = 0; var call = 0; var fold = 0; var mixed = 0
        for (row in 0..12) {
            for (col in 0..12) {
                when (getHandAction(row, col, position, gameType).color) {
                    0 -> fold++
                    1 -> call++
                    2 -> raise++
                    3 -> mixed++
                }
            }
        }
        return RangeStats(raise, call, fold, mixed)
    }

    private fun findHandIndices(hand: String): Pair<Int, Int>? {
        ranks.forEachIndexed { row, _ ->
            ranks.forEachIndexed { col, _ ->
                val h = when {
                    row == col -> "${ranks[row]}${ranks[col]}"
                    row < col -> "${ranks[row]}${ranks[col]}s"
                    else -> "${ranks[col]}${ranks[row]}o"
                }
                if (h == hand) return Pair(row, col)
            }
        }
        return null
    }

    private fun computePositionHands(hand: String, gameType: String): Map<String, GtoHandInfo> {
        val indices = findHandIndices(hand) ?: return emptyMap()
        return positions.associateWith { pos ->
            getHandAction(indices.first, indices.second, pos, gameType)
        }
    }

    private fun getGtoAction(hand: String, position: String, isTournament: Boolean): R {
        val isPair = hand.length == 2
        val isSuited = hand.endsWith("s")
        val rank1 = hand[0]
        val rank2 = hand[1]
        return when (position) {
            "UTG" -> getUtgAction(hand, isPair, isSuited, rank1, rank2, isTournament)
            "MP" -> getMpAction(hand, isPair, isSuited, rank1, rank2, isTournament)
            "CO" -> getCoAction(hand, isPair, isSuited, rank1, rank2, isTournament)
            "BTN" -> getBtnAction(hand, isPair, isSuited, rank1, rank2, isTournament)
            "SB" -> getSbAction(hand, isPair, isSuited, rank1, rank2, isTournament)
            "BB" -> getBbAction(hand, isPair, isSuited, rank1, rank2, isTournament)
            else -> fold("Default fold")
        }
    }

    private fun hv(c: Char): Int = when (c) {
        'A' -> 14; 'K' -> 13; 'Q' -> 12; 'J' -> 11; 'T' -> 10
        else -> c.digitToInt()
    }

    private fun getUtgAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char, isTournament: Boolean): R {
        if (isPair) {
            val v = hv(r1)
            return when {
                v >= 10 -> raise("Premium pair — always raise from UTG")
                v >= 8 -> raise(if (isTournament) "Medium pair — raise to build pot" else "Medium pair — raise for set mining value", if (isTournament) 95 else 90)
                v >= 6 && !isTournament -> mixed("Call/Fold", "Small pair — position dependent, often fold UTG", cPct = 40)
                else -> fold(if (isTournament) "Small pair — preserve chips in tournaments" else "Low pair — fold from early position")
            }
        }
        if (isSuited) {
            return when {
                r1 == 'A' && hv(hand[1]) >= 10 -> raise("Premium suited Ace — raise for value")
                r1 == 'A' && hv(hand[1]) >= 5 -> raise("Suited Ace — raise, good playability postflop", 85)
                r1 == 'A' && !isTournament -> mixed("Call/Fold", "Weak suited Ace — marginal from UTG", cPct = 35)
                r1 == 'A' -> fold("Weak suited Ace — fold UTG in tournaments")
                r1 == 'K' && hv(hand[1]) >= 10 -> raise("Strong suited King — raise", 95)
                hv(r1) >= 11 && hv(r1) - hv(hand[1]) <= 2 -> raise("Suited broadway — raise", 90)
                hv(r1) >= 10 && hv(r1) - hv(hand[1]) <= 2 && !isTournament -> raise("Suited broadway — raise", 75)
                else -> fold("Too weak for UTG — fold")
            }
        }
        return when {
            r1 == 'A' && hand[1] == 'K' -> raise("AKo — premium hand, always raise UTG")
            r1 == 'A' && hand[1] == 'Q' -> raise("AQo — strong offsuit Ace, raise UTG", 95)
            r1 == 'A' && hv(hand[1]) >= 11 -> raise("AJo — solid hand, raise UTG", 85)
            r1 == 'K' && hand[1] == 'Q' -> raise("KQo — strong enough for UTG raise", 80)
            else -> fold("Offsuit hand too weak for UTG")
        }
    }

    private fun getMpAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char, isTournament: Boolean): R {
        if (isPair) {
            val v = hv(r1)
            return when {
                v >= 9 -> raise("Raise medium+ pairs from MP")
                v >= 7 -> raise("Medium pair — raise from MP", 90)
                v >= 5 && !isTournament -> raise("Small pairs playable from MP in cash", 70)
                v >= 5 -> mixed("Call/Fold", "Small pairs — mixed strategy in tournaments", cPct = 40)
                else -> fold("Fold lowest pairs from MP")
            }
        }
        if (isSuited) {
            return when {
                r1 == 'A' && hv(hand[1]) >= 10 -> raise("Premium suited Ace — raise from MP")
                r1 == 'A' && hv(hand[1]) >= 5 -> raise("Suited Ace — raise from MP", 90)
                r1 == 'A' -> raise("Weak suited Ace — playable from MP", 75)
                r1 == 'K' && hv(hand[1]) >= 10 -> raise("Suited King — raise from MP", 95)
                r1 == 'K' && hv(hand[1]) >= 9 -> raise("Suited King — raise from MP", 80)
                hv(r1) >= 10 && hv(hand[1]) >= 9 -> raise("Suited connectors/broadways — raise", 85)
                hv(r1) >= 9 && hv(r1) - hv(hand[1]) <= 1 && !isTournament -> raise("Suited connector — playable from MP", 65)
                else -> fold("Too speculative for MP")
            }
        }
        return when {
            r1 == 'A' && hand[1] == 'K' -> raise("AKo — raise from MP")
            r1 == 'A' && hv(hand[1]) >= 10 -> raise("Strong Ace offsuit — raise", 90)
            r1 == 'K' && hv(hand[1]) >= 12 -> raise("KQo — raise from MP", 90)
            r1 == 'K' && hv(hand[1]) >= 11 -> raise("KJo — raise from MP", 80)
            r1 == 'Q' && hand[1] == 'J' && !isTournament -> raise("QJo — borderline from MP in cash", 65)
            else -> fold("Fold weak offsuit hands from MP")
        }
    }

    private fun getCoAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char, isTournament: Boolean): R {
        if (isPair) return raise("All pocket pairs playable from CO")
        if (isSuited) {
            return when {
                r1 == 'A' -> raise("All suited Aces — raise from CO")
                r1 == 'K' -> raise("All suited Kings — raise from CO", 95)
                hv(r1) >= 10 -> raise("Suited broadways/connectors — raise from CO", 90)
                hv(r1) >= 9 -> raise("Suited connectors — raise from CO", 80)
                hv(r1) - hv(hand[1]) <= 2 && !isTournament -> call("Suited connector — can open from CO in cash", 70)
                hv(r1) - hv(hand[1]) <= 1 -> call("Tight suited connector — CO in tournaments", 65)
                else -> fold("Too weak even for CO")
            }
        }
        return when {
            r1 == 'A' && hv(hand[1]) >= 10 -> raise("Ace-high broadway offsuit — raise from CO")
            r1 == 'A' && hv(hand[1]) >= 8 -> raise("Ace-high offsuit — raise from CO", 90)
            r1 == 'A' && !isTournament -> raise("Ace-x offsuit — raise from CO in cash", 75)
            r1 == 'K' && hv(hand[1]) >= 11 -> raise("King-high broadway — raise from CO", 95)
            r1 == 'K' && hv(hand[1]) >= 10 -> raise("King-high offsuit — raise from CO", 85)
            r1 == 'Q' && hv(hand[1]) >= 10 -> raise("Queen-high broadway — raise from CO", 80)
            hv(r1) >= 10 && hv(hand[1]) >= 10 -> raise("Broadway offsuit — raise from CO", 75)
            else -> fold("Offsuit hand not strong enough for CO open")
        }
    }

    private fun getBtnAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char, isTournament: Boolean): R {
        if (isPair) return raise("All pocket pairs — always raise on BTN")
        if (isSuited) {
            return when {
                r1 == 'A' -> raise("All suited Aces — raise on BTN")
                r1 == 'K' -> raise("All suited Kings — raise on BTN", 95)
                hv(r1) >= 9 -> raise("Wide suited range — raise on BTN", 90)
                hv(r1) >= 7 -> raise("Suited connectors — raise on BTN", 80)
                !isTournament -> call("Weak suited — can mix raise/fold on BTN in cash", 70)
                else -> fold("Too weak for BTN in tournaments")
            }
        }
        return when {
            r1 == 'A' && hv(hand[1]) >= 9 -> raise("Ace-high offsuit — raise on BTN")
            r1 == 'A' -> raise("All Ace-x offsuit — raise on BTN", 85)
            r1 == 'K' && hv(hand[1]) >= 8 -> raise("King-high offsuit — raise on BTN", 90)
            r1 == 'K' && hv(hand[1]) >= 5 -> raise("King-x offsuit — raise on BTN", 80)
            r1 == 'K' && !isTournament -> raise("King-x offsuit — raise on BTN in cash", 70)
            hv(r1) >= 10 && hv(hand[1]) >= 8 -> raise("Broadway+ offsuit — raise on BTN", 85)
            hv(r1) >= 10 && hv(hand[1]) >= 7 -> raise("Broadway+ offsuit — raise on BTN", 75)
            hv(r1) >= 9 && hv(r1) - hv(hand[1]) <= 2 -> raise("Connected offsuit — raise on BTN", 70)
            else -> fold("Even BTN has limits — fold weakest hands")
        }
    }

    private fun getSbAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char, isTournament: Boolean): R {
        if (isPair) {
            val v = hv(r1)
            return when {
                v >= 11 -> raise("Premium pair — raise from SB")
                v >= 9 -> raise("Medium pair — raise from SB", 90)
                v >= 7 -> raise("Medium pair — raise or complete from SB", 80)
                !isTournament -> call("Small pair — complete from SB", 70)
                else -> mixed("Call/Fold", "Small pair — mixed in tournaments SB", cPct = 45)
            }
        }
        if (isSuited) {
            return when {
                r1 == 'A' && hv(hand[1]) >= 10 -> raise("Strong suited Ace — raise from SB")
                r1 == 'A' -> raise("Suited Ace — raise from SB", 85)
                r1 == 'K' && hv(hand[1]) >= 10 -> raise("Strong suited King — raise from SB", 90)
                r1 == 'K' && hv(hand[1]) >= 8 -> raise("Suited King — raise from SB", 75)
                hv(r1) >= 11 -> call("Suited broadway — call/raise from SB", 85)
                hv(r1) >= 10 -> call("Suited broadway — call from SB", 80)
                !isTournament -> call("Suited hand — can complete from SB", 70)
                hv(r1) >= 8 -> call("Decent suited — complete from SB", 65)
                else -> fold("Weak suited — fold from SB in tournaments")
            }
        }
        return when {
            r1 == 'A' && hv(hand[1]) >= 11 -> raise("Strong Ace offsuit — raise from SB", 90)
            r1 == 'A' && hv(hand[1]) >= 9 -> raise("Ace-high offsuit — raise from SB", 80)
            r1 == 'K' && hv(hand[1]) >= 12 -> raise("KQo — raise from SB", 85)
            r1 == 'K' && hv(hand[1]) >= 11 -> raise("KJo — raise from SB", 75)
            hv(r1) >= 11 && hv(hand[1]) >= 10 -> call("Broadway offsuit — complete from SB", 80)
            hv(r1) >= 10 && hv(hand[1]) >= 10 -> call("Broadway offsuit — complete from SB", 75)
            !isTournament && hv(r1) >= 9 && hv(hand[1]) >= 8 -> call("Connected — complete from SB in cash", 65)
            else -> fold("Weak hand — fold from SB despite price")
        }
    }

    private fun getBbAction(hand: String, isPair: Boolean, isSuited: Boolean, r1: Char, r2: Char, isTournament: Boolean): R {
        if (isPair) {
            val v = hv(r1)
            return when {
                v >= 10 -> raise("Premium pair — 3-bet from BB", 90)
                v >= 7 -> raise("Medium pair — 3-bet or call from BB", 75)
                else -> call("Small pair — call and set mine from BB", 85)
            }
        }
        if (isSuited) {
            return when {
                r1 == 'A' && hv(hand[1]) >= 10 -> raise("Strong suited Ace — 3-bet from BB", 85)
                r1 == 'A' -> raise("Suited Ace — 3-bet or call from BB", 70)
                hv(r1) >= 10 -> call("Suited broadway — defend BB", 90)
                hv(r1) >= 9 -> call("Suited broadway — defend BB", 85)
                hv(r1) - hv(hand[1]) <= 3 -> call("Suited connector — defend BB", 80)
                !isTournament -> call("Getting good price — defend suited in BB", 75)
                hv(r1) >= 7 -> call("Suited hand — defend BB", 70)
                else -> fold("Too weak to defend even in BB in tournaments")
            }
        }
        return when {
            r1 == 'A' && hv(hand[1]) >= 12 -> raise("Strong Ace — 3-bet from BB", 85)
            r1 == 'A' && hv(hand[1]) >= 10 -> raise("Ace broadway — 3-bet or call from BB", 70)
            r1 == 'A' -> call("Ace-x offsuit — defend BB", 85)
            r1 == 'K' && hv(hand[1]) >= 11 -> call("King broadway — defend BB", 90)
            r1 == 'K' && hv(hand[1]) >= 10 -> call("King-high broadway — defend BB", 85)
            hv(r1) >= 10 && hv(hand[1]) >= 9 -> call("Broadway — defend BB", 80)
            hv(r1) >= 10 && hv(hand[1]) >= 8 -> call("Broadway — defend BB with good price", 75)
            !isTournament && hv(r1) >= 9 && hv(hand[1]) >= 7 -> call("Connected — defend BB in cash", 65)
            else -> fold("Too weak to defend — fold from BB")
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
        val posHands = computePositionHands(handInfo.hand, _uiState.value.gameType)
        _uiState.value = _uiState.value.copy(
            selectedHand = handInfo,
            showHandDetail = true,
            positionHandsForSelected = posHands
        )
    }

    fun dismissHandDetail() {
        _uiState.value = _uiState.value.copy(showHandDetail = false)
    }

    fun setGameType(type: String) {
        _uiState.value = _uiState.value.copy(
            gameType = type,
            selectedHand = null,
            showHandDetail = false
        )
    }
}

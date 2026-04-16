package com.vikash.pokertrainer.scenarios

/**
 * Pure-JVM validator that mirrors `scripts/validate_scenarios.py`.
 *
 * Used by ScenarioJsonValidationTest to assert that every scenario shipped in
 * `app/src/main/assets/scenarios.json` is internally consistent — i.e. the
 * explanation text doesn't contradict the actual hand+board.
 *
 * Keep these checks in lockstep with the Python script. If you add a check
 * in one place, add it in the other.
 */
object ScenarioValidator {

    private val SUIT_GLYPHS = mapOf('♠' to 's', '♥' to 'h', '♦' to 'd', '♣' to 'c')
    private val SUIT_LETTERS = setOf('s', 'h', 'd', 'c', 'S', 'H', 'D', 'C')
    private const val RANK_ORDER = "23456789TJQKA"
    private val RANK_VAL = RANK_ORDER.mapIndexed { i, r -> r to i + 2 }.toMap()
    private val ALL_SUITS = listOf('s', 'h', 'd', 'c')

    data class Card(val rank: Char, val suit: Char) {
        val rankVal: Int get() = RANK_VAL[rank] ?: error("bad rank $rank")
    }

    data class Scenario(
        val id: String,
        val hand: String,
        val position: String,
        val board: List<String>,
        val street: String,
        val explanation: String,
    )

    fun parseCard(card: String): Card? {
        if (card.isBlank()) return null
        val normalised = card.trim().replace("10", "T")
        if (normalised.length < 2) return null
        val suitChar = normalised.last()
        val rankPart = normalised.dropLast(1)
        val suit: Char = when {
            SUIT_GLYPHS.containsKey(suitChar) -> SUIT_GLYPHS.getValue(suitChar)
            suitChar in SUIT_LETTERS -> suitChar.lowercaseChar()
            else -> return null
        }
        val rank = rankPart.uppercase().firstOrNull() ?: return null
        if (!RANK_VAL.containsKey(rank)) return null
        return Card(rank, suit)
    }

    fun parseHand(hand: String): List<Card> {
        if (hand.isBlank()) return emptyList()
        val s = hand.trim().replace("10", "T")
        val out = mutableListOf<Card>()
        var i = 0
        while (i + 1 < s.length) {
            val rank = s[i].uppercaseChar()
            val suitChar = s[i + 1]
            if (RANK_VAL.containsKey(rank) &&
                (SUIT_GLYPHS.containsKey(suitChar) || suitChar in SUIT_LETTERS)
            ) {
                val suit = SUIT_GLYPHS[suitChar] ?: suitChar.lowercaseChar()
                out.add(Card(rank, suit))
                i += 2
            } else {
                i += 1
            }
        }
        return out
    }

    // -----------------------------------------------------------------------
    // Hand evaluation primitives
    // -----------------------------------------------------------------------

    private fun isStraight(rankVals: Collection<Int>): Boolean =
        bestStraightHigh(rankVals) != null

    private fun bestStraightHigh(rankVals: Collection<Int>): Int? {
        val sorted = rankVals.toSet().sorted()
        if (sorted.size < 5) {
            // wheel only
            if (setOf(14, 2, 3, 4, 5).all { it in rankVals }) return 5
            return null
        }
        var best: Int? = null
        for (i in 0..sorted.size - 5) {
            val window = sorted.subList(i, i + 5)
            if (window.last() - window.first() == 4) best = window.last()
        }
        if (best == null && setOf(14, 2, 3, 4, 5).all { it in rankVals.toSet() }) return 5
        return best
    }

    private fun flushSuit(suits: Collection<Char>): Char? {
        val counts = suits.groupingBy { it }.eachCount()
        return counts.entries.firstOrNull { it.value >= 5 }?.key
    }

    private enum class HandClass { HIGH, PAIR, TWO_PAIR, TRIPS, STRAIGHT, FLUSH, FULL_HOUSE, QUADS, STRAIGHT_FLUSH }

    private fun classifySevenCards(cards: List<Card>): HandClass {
        val ranks = cards.map { it.rankVal }
        val suits = cards.map { it.suit }
        val flush = flushSuit(suits)
        val straight = isStraight(ranks)
        val sf = flush?.let { fs ->
            isStraight(cards.filter { it.suit == fs }.map { it.rankVal })
        } ?: false
        val rankCounts = ranks.groupingBy { it }.eachCount()
        val sortedCounts = rankCounts.values.sortedDescending()
        return when {
            sf -> HandClass.STRAIGHT_FLUSH
            sortedCounts[0] == 4 -> HandClass.QUADS
            sortedCounts[0] == 3 && (sortedCounts.getOrNull(1) ?: 0) >= 2 -> HandClass.FULL_HOUSE
            flush != null -> HandClass.FLUSH
            straight -> HandClass.STRAIGHT
            sortedCounts[0] == 3 -> HandClass.TRIPS
            sortedCounts[0] == 2 && (sortedCounts.getOrNull(1) ?: 0) == 2 -> HandClass.TWO_PAIR
            sortedCounts[0] == 2 -> HandClass.PAIR
            else -> HandClass.HIGH
        }
    }

    private fun bestPossibleClassFor(board: List<Card>): HandClass {
        val deck = mutableListOf<Card>()
        for (r in RANK_ORDER) {
            for (s in ALL_SUITS) {
                val c = Card(r, s)
                if (c !in board) deck.add(c)
            }
        }
        var best = HandClass.HIGH
        for (i in 0 until deck.size) {
            for (j in i + 1 until deck.size) {
                val cls = classifySevenCards(board + deck[i] + deck[j])
                if (cls.ordinal > best.ordinal) best = cls
                if (best == HandClass.STRAIGHT_FLUSH) return best
            }
        }
        return best
    }

    // -----------------------------------------------------------------------
    // Pattern matching for explanation claims
    // -----------------------------------------------------------------------

    private val rainbowPatterns = listOf(
        Regex("\\brainbow\\b", RegexOption.IGNORE_CASE),
        Regex("\\bthree[- ]?suit", RegexOption.IGNORE_CASE),
    )
    private val monotonePatterns = listOf(
        Regex("\\bmonotone\\b", RegexOption.IGNORE_CASE)
    )
    private val twoTonePatterns = listOf(
        Regex("\\btwo[- ]?tone\\b", RegexOption.IGNORE_CASE)
    )
    private val pairedBoardPatterns = listOf(
        Regex(
            "\\b(this is a |on (a|this) |the )paired board\\b|\\bboard is paired\\b|board pairs (and|now)",
            RegexOption.IGNORE_CASE,
        )
    )

    private val claimBackdoorFlush = Regex("backdoor[- ]flush", RegexOption.IGNORE_CASE)

    private val claimHeroFlushDraw = Regex(
        "\\b(you have|i have|we have|hero has|having)\\s+(a\\s+)?(nut\\s+|backdoor[- ]?)?flush draw",
        RegexOption.IGNORE_CASE,
    )

    private val claimHeroHasNuts = Regex(
        "(?i)(" +
            "\\bsecond\\s+nuts\\b" +
            "|\\b(you|we|i|hero|your|my|hero's)\\s+" +
            "(have|has|hold|holds|holding|made|got|is\\s+on|are\\s+on)\\s+" +
            "(the\\s+)?(second\\s+)?nuts\\b" +
            "|\\b(your|my|hero's)\\s+\\w+(\\s+\\w+){0,3}\\s+is\\s+" +
            "(the\\s+)?(second\\s+)?nuts\\b" +
            ")"
    )

    private val namedStraightRegexes = mapOf(
        "Broadway" to Regex("\\bbroadway\\b", RegexOption.IGNORE_CASE),
        "wheel" to Regex("\\bwheel\\b", RegexOption.IGNORE_CASE),
    )

    private val namedStraightNeeds = mapOf(
        "Broadway" to setOf(10, 11, 12, 13, 14),
        "wheel" to setOf(14, 2, 3, 4, 5),
    )

    private val negationNear = Regex(
        "(?i)\\b(impossible|not\\s+possible|cannot|can't|isn't|is\\s+not|no\\s+one\\s+can|" +
            "would\\s+require|would\\s+need|requires?\\s+a|needs?\\s+a|missing|without|" +
            "never|no\\s+\\w+\\s+(on\\s+the\\s+)?board)\\b"
    )

    private val expectedBoard = mapOf(
        "PREFLOP" to 0, "FLOP" to 3, "TURN" to 4, "RIVER" to 5
    )
    private val futureCardsByStreet = mapOf("FLOP" to 4, "TURN" to 3, "RIVER" to 2)

    // -----------------------------------------------------------------------
    // The validator
    // -----------------------------------------------------------------------

    fun validate(scenario: Scenario): List<String> {
        val issues = mutableListOf<String>()
        val hand = parseHand(scenario.hand)
        val board = mutableListOf<Card>()
        for (b in scenario.board) {
            val c = parseCard(b)
            if (c == null) issues += "unparseable board card '$b'"
            else board.add(c)
        }

        if (hand.size != 2) {
            issues += "hand did not parse to 2 cards: '${scenario.hand}'"
        }

        val all = hand + board
        val dups = all.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (dups.isNotEmpty()) issues += "duplicate cards: ${dups.sortedBy { it.rankVal }}"

        expectedBoard[scenario.street]?.let { expected ->
            if (board.size != expected) {
                issues += "street=${scenario.street} expects $expected board cards, " +
                    "got ${board.size}"
            }
        }

        val explanation = scenario.explanation
        val isPostflop = scenario.street in setOf("FLOP", "TURN", "RIVER") && board.size >= 3

        // Board structure claims (only meaningful on flop/turn/river).
        if (isPostflop) {
            val flop = board.take(3)
            val flopSuits = flop.map { it.suit }
            val uniqueFlopSuits = flopSuits.toSet().size

            if (rainbowPatterns.any { it.containsMatchIn(explanation) } && uniqueFlopSuits != 3) {
                issues += "explanation says 'rainbow' but flop has $uniqueFlopSuits suits ($flopSuits)"
            }
            if (monotonePatterns.any { it.containsMatchIn(explanation) } && uniqueFlopSuits != 1) {
                issues += "explanation says 'monotone' but flop has $uniqueFlopSuits suits ($flopSuits)"
            }
            if (twoTonePatterns.any { it.containsMatchIn(explanation) } && uniqueFlopSuits != 2) {
                issues += "explanation says 'two-tone' but flop has $uniqueFlopSuits suits ($flopSuits)"
            }
            if (pairedBoardPatterns.any { it.containsMatchIn(explanation) }) {
                val boardRanks = board.map { it.rank }
                if (boardRanks.toSet().size == boardRanks.size) {
                    issues += "explanation says 'paired board' but board has no pair ($boardRanks)"
                }
            }
        }

        // Backdoor flush draw on the turn → expect 4 of one suit across hand+board.
        if (claimBackdoorFlush.containsMatchIn(explanation) &&
            scenario.street == "TURN" && board.size == 4
        ) {
            val maxSuit = ALL_SUITS.maxOf { s -> all.count { it.suit == s } }
            if (maxSuit < 4) {
                issues += "says 'backdoor flush' but turned card doesn't give 4-of-suit (max=$maxSuit)"
            }
        }

        // Hero flush draw claim → 4 cards of one suit across hand+board.
        if (claimHeroFlushDraw.containsMatchIn(explanation) &&
            scenario.street in setOf("FLOP", "TURN") && board.size >= 3
        ) {
            val maxSuit = ALL_SUITS.maxOf { s -> all.count { it.suit == s } }
            if (maxSuit < 4) {
                issues += "says hero has a flush draw but only $maxSuit cards of any one " +
                    "suit across hand+board"
            }
        }

        // Named straight claims (Broadway / wheel) — only meaningful postflop.
        if (scenario.street in setOf("FLOP", "TURN", "RIVER")) {
            val unknowns = futureCardsByStreet[scenario.street] ?: 0
            for ((name, regex) in namedStraightRegexes) {
                val matches = regex.findAll(explanation).toList()
                if (matches.isEmpty()) continue
                val needed = namedStraightNeeds[name] ?: continue
                val boardVals = board.map { it.rankVal }.toSet()
                val missing = needed - boardVals
                if (missing.size <= unknowns) continue  // achievable, skip
                // For each mention, check if it's in a negation context.
                var flagged = false
                for (m in matches) {
                    val start = (m.range.first - 60).coerceAtLeast(0)
                    val end = (m.range.last + 60).coerceAtMost(explanation.length - 1) + 1
                    val window = explanation.substring(start, end)
                    if (negationNear.containsMatchIn(window)) continue
                    flagged = true
                    break
                }
                if (flagged) {
                    issues += "explanation mentions '$name' but it is physically impossible " +
                        "on this ${scenario.street.lowercase()} board (missing " +
                        "${missing.sorted()}; only $unknowns more cards available across " +
                        "hand + future community)"
                }
            }
        }

        // "Nuts" claim — hero must actually hold the best class.
        if (claimHeroHasNuts.containsMatchIn(explanation) &&
            scenario.street == "RIVER" && board.size == 5
        ) {
            val nutClass = bestPossibleClassFor(board)
            val heroClass = classifySevenCards(hand + board)
            if (heroClass.ordinal < nutClass.ordinal) {
                issues += "says 'nuts' but hero has $heroClass while nuts available = $nutClass"
            }
        }

        return issues
    }
}

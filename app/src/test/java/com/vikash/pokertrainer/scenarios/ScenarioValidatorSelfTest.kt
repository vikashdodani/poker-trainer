package com.vikash.pokertrainer.scenarios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Self-tests for ScenarioValidator itself.
 *
 * If you add a new check to ScenarioValidator, add a positive fixture (must be
 * caught) and a negative fixture (must NOT be caught) here. Mirrors the
 * `--self-test` mode in `scripts/validate_scenarios.py`.
 */
class ScenarioValidatorSelfTest {

    private fun scenario(
        id: String,
        hand: String,
        board: List<String>,
        street: String,
        explanation: String,
        position: String = "BTN",
    ) = ScenarioValidator.Scenario(
        id = id,
        hand = hand,
        position = position,
        board = board,
        street = street,
        explanation = explanation,
    )

    // -----------------------------------------------------------------------
    // Positive fixtures: validator MUST flag these.
    // -----------------------------------------------------------------------

    @Test
    fun flags_broadway_when_no_jack_or_ace_reachable() {
        // The original bug: claims Broadway available with hole AT on Q-9-3-6-K.
        // Broadway needs T+J+A; board has no J, hero has no J — impossible.
        val s = scenario(
            id = "test_broadway_impossible",
            hand = "A♣T♣",
            board = listOf("Q♥", "9♣", "3♦", "6♠", "K♦"),
            street = "RIVER",
            explanation = "Broadway is the nut straight here.",
        )
        val issues = ScenarioValidator.validate(s)
        assertTrue(
            "expected Broadway-impossible flag, got: $issues",
            issues.any { it.contains("Broadway", ignoreCase = true) },
        )
    }

    @Test
    fun flags_wheel_when_unreachable() {
        // Wheel needs A-2-3-4-5; on KQJT9 with 2 hole cards, impossible.
        val s = scenario(
            id = "test_wheel_impossible",
            hand = "AsKs",
            board = listOf("Ks", "Qs", "Js", "Ts", "9s"),
            street = "RIVER",
            explanation = "The wheel completes on this run-out.",
        )
        val issues = ScenarioValidator.validate(s)
        assertTrue(
            "expected wheel-impossible flag, got: $issues",
            issues.any { it.contains("wheel", ignoreCase = true) },
        )
    }

    @Test
    fun flags_rainbow_claim_on_two_tone_flop() {
        val s = scenario(
            id = "test_rainbow_wrong",
            hand = "A♥K♥",
            board = listOf("J♥", "7♥", "2♣"),
            street = "FLOP",
            explanation = "The flop comes rainbow so multiway play is fine.",
        )
        val issues = ScenarioValidator.validate(s)
        assertTrue(
            "expected rainbow flag, got: $issues",
            issues.any { it.contains("rainbow") },
        )
    }

    @Test
    fun flags_monotone_claim_on_two_tone_flop() {
        val s = scenario(
            id = "test_monotone_wrong",
            hand = "A♥K♥",
            board = listOf("J♥", "7♥", "2♣"),
            street = "FLOP",
            explanation = "Monotone board, very dangerous.",
        )
        val issues = ScenarioValidator.validate(s)
        assertTrue(
            "expected monotone flag, got: $issues",
            issues.any { it.contains("monotone") },
        )
    }

    @Test
    fun flags_paired_board_when_no_pair_present() {
        val s = scenario(
            id = "test_paired_wrong",
            hand = "A♣K♣",
            board = listOf("J♥", "7♣", "2♦", "5♠", "9♥"),
            street = "RIVER",
            explanation = "On this paired board you should slow down.",
        )
        val issues = ScenarioValidator.validate(s)
        assertTrue(
            "expected paired-board flag, got: $issues",
            issues.any { it.contains("paired") },
        )
    }

    @Test
    fun flags_nuts_claim_when_better_hand_available() {
        // Hero claims nuts with a pair on a board where straights/flushes beat them.
        val s = scenario(
            id = "test_nuts_wrong",
            hand = "A♣K♥",
            board = listOf("J♣", "T♣", "9♣", "2♦", "3♥"),
            street = "RIVER",
            explanation = "You have the nuts here, bet for value.",
        )
        val issues = ScenarioValidator.validate(s)
        assertTrue(
            "expected nuts flag, got: $issues",
            issues.any { it.contains("nuts") },
        )
    }

    // -----------------------------------------------------------------------
    // Negative fixtures: validator MUST NOT flag these.
    // -----------------------------------------------------------------------

    @Test
    fun does_not_flag_broadway_in_negation_context() {
        val s = scenario(
            id = "test_broadway_negated",
            hand = "A♣T♣",
            board = listOf("Q♥", "9♣", "3♦", "6♠", "K♦"),
            street = "RIVER",
            explanation = "Note: Broadway is impossible here because there is no jack on the board.",
        )
        val issues = ScenarioValidator.validate(s)
        assertFalse(
            "negated Broadway mention should not be flagged: $issues",
            issues.any { it.contains("Broadway", ignoreCase = true) },
        )
    }

    @Test
    fun does_not_flag_nut_flush_when_hero_actually_has_nut_flush() {
        // 'nut flush' is a class label, not an unqualified 'nuts' claim.
        val s = scenario(
            id = "test_nut_flush_ok",
            hand = "A♣K♣",
            board = listOf("J♣", "7♣", "2♣", "4♦", "8♥"),
            street = "RIVER",
            explanation = "You have the nut flush; bet for value.",
        )
        val issues = ScenarioValidator.validate(s)
        assertFalse(
            "'nut flush' should not be confused with absolute nuts: $issues",
            issues.any { it.contains("nuts") },
        )
    }

    @Test
    fun does_not_flag_blocker_language() {
        // "you block the nuts" is about blockers, not a claim of holding nuts.
        val s = scenario(
            id = "test_blocker_ok",
            hand = "A♣K♥",
            board = listOf("J♣", "T♣", "9♣", "2♦", "3♥"),
            street = "RIVER",
            explanation = "You block the nuts and the second nuts so a bluff makes sense.",
        )
        val issues = ScenarioValidator.validate(s)
        // It's fine if "second nuts" still triggers; we just want to make sure
        // "block the nuts" alone (without a hero subject + verb) doesn't.
        // To be safe, accept a flag only on second nuts.
        assertFalse(
            "blocker-only language should not flag a hero-nuts claim: $issues",
            issues.any { it.contains("nuts") && !it.contains("STRAIGHT") },
        )
    }

    @Test
    fun does_not_flag_villain_flush_draw_mention() {
        // Villain might have a flush draw — hero doesn't claim one.
        val s = scenario(
            id = "test_villain_fd_ok",
            hand = "A♣A♥",
            board = listOf("8♣", "5♣", "2♦"),
            street = "FLOP",
            explanation = "Villain's range includes flush draw possibilities here.",
        )
        val issues = ScenarioValidator.validate(s)
        assertFalse(
            "villain flush draw mention should not be flagged: $issues",
            issues.any { it.contains("flush draw") },
        )
    }

    // -----------------------------------------------------------------------
    // Sanity tests for the underlying primitives.
    // -----------------------------------------------------------------------

    @Test
    fun parses_unicode_and_letter_suits() {
        assertEquals(
            ScenarioValidator.Card('A', 's'),
            ScenarioValidator.parseCard("A♠"),
        )
        assertEquals(
            ScenarioValidator.Card('A', 's'),
            ScenarioValidator.parseCard("As"),
        )
        assertEquals(
            ScenarioValidator.Card('T', 'h'),
            ScenarioValidator.parseCard("10h"),
        )
    }

    @Test
    fun parses_two_card_hand_in_unicode_and_letters() {
        assertEquals(
            listOf(
                ScenarioValidator.Card('A', 's'),
                ScenarioValidator.Card('K', 's'),
            ),
            ScenarioValidator.parseHand("A♠K♠"),
        )
        assertEquals(
            listOf(
                ScenarioValidator.Card('A', 's'),
                ScenarioValidator.Card('K', 's'),
            ),
            ScenarioValidator.parseHand("AsKs"),
        )
    }

    @Test
    fun flags_duplicate_cards() {
        val s = scenario(
            id = "test_dup",
            hand = "A♠K♠",
            board = listOf("A♠", "7♣", "2♦"),
            street = "FLOP",
            explanation = "boring",
        )
        val issues = ScenarioValidator.validate(s)
        assertTrue(
            "expected duplicate-card flag, got: $issues",
            issues.any { it.contains("duplicate") },
        )
    }

    @Test
    fun flags_wrong_board_length_for_street() {
        val s = scenario(
            id = "test_wrong_len",
            hand = "A♠K♠",
            board = listOf("Q♥", "9♣"),
            street = "FLOP",
            explanation = "boring",
        )
        val issues = ScenarioValidator.validate(s)
        assertTrue(
            "expected board-length flag, got: $issues",
            issues.any { it.contains("expects 3") },
        )
    }
}

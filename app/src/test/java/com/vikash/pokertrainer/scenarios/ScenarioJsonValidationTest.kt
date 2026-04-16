package com.vikash.pokertrainer.scenarios

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Loads the shipped scenarios.json and asserts every scenario is internally
 * consistent according to ScenarioValidator.
 *
 * If a scenario's explanation contradicts the actual hand+board (e.g. claiming
 * Broadway is available when the board makes it physically impossible), this
 * test fails with the offending scenario id and the validator's reason.
 *
 * The Python equivalent is `scripts/validate_scenarios.py`. Keep them in sync.
 */
class ScenarioJsonValidationTest {

    private data class RawScenario(
        val id: String?,
        val hand: String,
        val position: String,
        val board: List<String>,
        val street: String,
        val explanation: String,
    )

    @Test
    fun every_scenario_in_assets_is_internally_consistent() {
        val scenariosFile = locateScenariosJson()
        val json = scenariosFile.readText()
        val type = object : TypeToken<List<RawScenario>>() {}.type
        val raw: List<RawScenario> = Gson().fromJson(json, type)

        assertFalse("scenarios.json is empty", raw.isEmpty())

        val ids = mutableSetOf<String>()
        val failures = mutableListOf<String>()

        raw.forEachIndexed { index, r ->
            val id = r.id ?: "(scenario #$index has no id)"
            assertNotNull("scenario at index $index is missing id", r.id)
            assertTrue("duplicate scenario id: $id", ids.add(id))

            val scenario = ScenarioValidator.Scenario(
                id = id,
                hand = r.hand,
                position = r.position,
                board = r.board,
                street = r.street,
                explanation = r.explanation,
            )
            val issues = ScenarioValidator.validate(scenario)
            if (issues.isNotEmpty()) {
                failures += "$id: ${issues.joinToString("; ")}"
            }
        }

        if (failures.isNotEmpty()) {
            val msg = buildString {
                appendLine("${failures.size} scenario(s) failed validation:")
                failures.forEach { appendLine("  - $it") }
            }
            throw AssertionError(msg)
        }
    }

    @Test
    fun every_scenario_has_an_id_with_expected_prefix() {
        val scenariosFile = locateScenariosJson()
        val type = object : TypeToken<List<RawScenario>>() {}.type
        val raw: List<RawScenario> = Gson().fromJson(scenariosFile.readText(), type)

        val allowedPrefixes = setOf("preflop_", "flop_", "turn_", "river_")
        raw.forEach { r ->
            val id = r.id ?: error("scenario missing id: $r")
            val ok = allowedPrefixes.any { id.startsWith(it) }
            assertTrue(
                "scenario id '$id' must start with one of $allowedPrefixes",
                ok,
            )
            // street should match the prefix
            val expectedStreet = when {
                id.startsWith("preflop_") -> "PREFLOP"
                id.startsWith("flop_") -> "FLOP"
                id.startsWith("turn_") -> "TURN"
                id.startsWith("river_") -> "RIVER"
                else -> error("unreachable")
            }
            assertEquals(
                "scenario '$id' has street ${r.street} but id implies $expectedStreet",
                expectedStreet,
                r.street,
            )
        }
    }

    /**
     * Gradle unit tests run with the module dir (`app/`) as the working dir,
     * so the assets file is at `src/main/assets/scenarios.json`. We also fall
     * back to `app/src/...` in case the test is invoked from the repo root.
     */
    private fun locateScenariosJson(): File {
        val candidates = listOf(
            File("src/main/assets/scenarios.json"),
            File("app/src/main/assets/scenarios.json"),
        )
        return candidates.firstOrNull { it.exists() }
            ?: error(
                "Could not find scenarios.json. Tried: " +
                    candidates.joinToString { it.absolutePath }
            )
    }
}

package com.vikash.pokertrainer.ui.screens.scenario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikash.pokertrainer.data.model.Street
import com.vikash.pokertrainer.ui.components.ActionButton
import com.vikash.pokertrainer.ui.components.CardRow
import com.vikash.pokertrainer.ui.components.PlayingCard
import com.vikash.pokertrainer.ui.components.PokerChip
import com.vikash.pokertrainer.ui.components.PositionBadge
import com.vikash.pokertrainer.ui.theme.BluffRed
import com.vikash.pokertrainer.ui.theme.CheckBlue
import com.vikash.pokertrainer.ui.theme.CorrectGreen
import com.vikash.pokertrainer.ui.theme.FoldGray
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.IncorrectRed
import com.vikash.pokertrainer.ui.theme.PokerDarkGreen
import com.vikash.pokertrainer.ui.theme.PokerFelt
import com.vikash.pokertrainer.ui.theme.PokerTableGreen
import com.vikash.pokertrainer.ui.theme.ValueGreen
import com.vikash.pokertrainer.ui.theme.pokerColors
import com.vikash.pokertrainer.viewmodel.ScenarioViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScenarioScreen(
    viewModel: ScenarioViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadScenarios()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scenario Trainer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Session score badge
                    if (uiState.sessionTotal > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(pokerColors.surfaceHigh)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${uiState.sessionCorrect}/${uiState.sessionTotal}",
                                color = if (uiState.sessionTotal > 0 &&
                                    uiState.sessionCorrect.toFloat() / uiState.sessionTotal >= 0.7f
                                ) CorrectGreen else GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pokerColors.background,
                    titleContentColor = pokerColors.textPrimary,
                    navigationIconContentColor = pokerColors.textPrimary
                )
            )
        },
        containerColor = pokerColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Street filter chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val streetOptions = listOf(null to "All") + Street.entries.map { it to it.name.lowercase()
                    .replaceFirstChar { c -> c.uppercase() } }

                streetOptions.forEach { (street, label) ->
                    FilterChip(
                        selected = uiState.selectedStreet == street,
                        onClick = { viewModel.loadScenarios(street) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (uiState.selectedStreet == street)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PokerTableGreen,
                            selectedLabelColor = pokerColors.textPrimary,
                            containerColor = pokerColors.surface,
                            labelColor = pokerColors.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            selectedBorderColor = PokerTableGreen,
                            enabled = true,
                            selected = uiState.selectedStreet == street
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val scenario = uiState.currentScenario
            if (scenario != null) {
                // Scenario ID bar — top of scenario, so users can report
                // bugs by id (e.g. "river_18 broken").
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(pokerColors.surface)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ID  ${scenario.id}",
                            color = pokerColors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "${uiState.currentIndex + 1}/${uiState.totalScenarios}",
                        color = pokerColors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Position display - prominent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    getPositionColor(scenario.position).copy(alpha = 0.25f),
                                    pokerColors.surface
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = getPositionColor(scenario.position).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Large position badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(getPositionColor(scenario.position))
                    ) {
                        Text(
                            text = scenario.position.uppercase(),
                            color = if (scenario.position.uppercase() == "BTN")
                                Color(0xFF1B2838) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Position name + label
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Position",
                            color = pokerColors.textMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = getPositionFullName(scenario.position),
                            color = pokerColors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pot + Stack
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Pot ", color = pokerColors.textMuted, fontSize = 11.sp)
                            Text(
                                text = scenario.potSize,
                                color = GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Stack ", color = pokerColors.textMuted, fontSize = 11.sp)
                            Text(
                                text = scenario.stackSize,
                                color = pokerColors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Villain action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(pokerColors.surface.copy(alpha = 0.7f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scenario.villainAction,
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Board area - poker table feel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PokerDarkGreen, PokerFelt, PokerDarkGreen)
                            )
                        )
                        .border(
                            width = 3.dp,
                            color = Color(0xFF5D4037),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Street label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = scenario.street.name,
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }

                        // Community cards (board)
                        if (scenario.board.isNotEmpty()) {
                            CardRow(cards = scenario.board)
                        } else {
                            // Preflop - show placeholder
                            Text(
                                text = "No community cards",
                                color = pokerColors.textSecondary.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }

                        // Divider line
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        // Player's hand label
                        Text(
                            text = "Your Hand",
                            color = pokerColors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Player hand - parse "AhKs" style into two cards
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val handCards = parseHand(scenario.hand)
                            handCards.forEach { card ->
                                PlayingCard(card = card)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "What's your play?",
                        color = pokerColors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Arrange buttons in rows of 2
                    val options = scenario.options
                    val rows = options.chunked(2)

                    rows.forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowOptions.forEach { option ->
                                val buttonColor = getActionColor(option)
                                val isCorrectAnswer = uiState.showResult && option == scenario.correct
                                val isWrongSelection = uiState.showResult &&
                                        option == uiState.selectedAnswer &&
                                        option != scenario.correct

                                ActionButton(
                                    text = option,
                                    color = buttonColor,
                                    isCorrect = when {
                                        isCorrectAnswer -> true
                                        isWrongSelection -> false
                                        else -> null
                                    },
                                    isSelected = uiState.selectedAnswer == option,
                                    onClick = {
                                        if (!uiState.showResult) {
                                            viewModel.selectAnswer(option)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // If odd number, add spacer for alignment
                            if (rowOptions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result overlay
                AnimatedVisibility(
                    visible = uiState.showResult,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 400)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(pokerColors.surface)
                            .border(
                                width = 2.dp,
                                color = if (uiState.isCorrect) CorrectGreen else IncorrectRed,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Result header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (uiState.isCorrect)
                                        Icons.Default.CheckCircle
                                    else
                                        Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (uiState.isCorrect) CorrectGreen else IncorrectRed,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isCorrect) "Correct!" else "Incorrect",
                                    color = if (uiState.isCorrect) CorrectGreen else IncorrectRed,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Correct answer
                            if (!uiState.isCorrect) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CorrectGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Correct answer: ",
                                        color = pokerColors.textSecondary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = scenario.correct,
                                        color = CorrectGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Explanation (selectable so users can copy text + ID
                            // when reporting a bug)
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = scenario.explanation,
                                        color = pokerColors.textSecondary,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Scenario ID: ${scenario.id}",
                                        color = pokerColors.textMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Next button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PokerTableGreen)
                                    .clickable { viewModel.nextScenario() }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Next Scenario",
                                        color = pokerColors.textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next",
                                        tint = pokerColors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scenarios available for this filter.",
                        color = pokerColors.textMuted,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Maps an action option string to its corresponding color.
 */
private fun getActionColor(action: String): Color {
    val lower = action.lowercase()
    return when {
        lower.contains("fold") -> FoldGray
        lower.contains("check") -> CheckBlue
        lower.contains("call") -> ValueGreen
        lower.contains("bet") || lower.contains("raise") || lower.contains("all-in") || lower.contains("allin") -> BluffRed
        else -> CheckBlue
    }
}

/**
 * Parses a hand string like "A\u2660K\u2665" or "Ah Ks" into a list of display card strings.
 * Supports formats:
 *   - Unicode suits: "A\u2660K\u2665"
 *   - Letter suits: "AhKs" or "Ah Ks"
 */
private fun parseHand(hand: String): List<String> {
    val suitMap = mapOf(
        'h' to "\u2665", 's' to "\u2660", 'd' to "\u2666", 'c' to "\u2663",
        'H' to "\u2665", 'S' to "\u2660", 'D' to "\u2666", 'C' to "\u2663"
    )
    val unicodeSuits = setOf('\u2660', '\u2665', '\u2666', '\u2663')

    // Check if already uses unicode suits
    if (hand.any { it in unicodeSuits }) {
        val cards = mutableListOf<String>()
        var current = StringBuilder()
        for (ch in hand) {
            current.append(ch)
            if (ch in unicodeSuits) {
                cards.add(current.toString())
                current = StringBuilder()
            }
        }
        return cards
    }

    // Letter-based suit format (e.g., "AhKs", "10dQc", "Ah Ks")
    val cleaned = hand.replace(" ", "")
    val cards = mutableListOf<String>()
    var i = 0
    while (i < cleaned.length) {
        val rankStart = i
        // Advance past rank characters (could be "10" or single char)
        while (i < cleaned.length && cleaned[i] !in suitMap) {
            i++
        }
        if (i < cleaned.length) {
            val rank = cleaned.substring(rankStart, i)
            val suit = suitMap[cleaned[i]] ?: ""
            cards.add("$rank$suit")
            i++
        } else {
            break
        }
    }
    return cards
}

private fun getPositionColor(position: String): Color {
    return when (position.uppercase()) {
        "BTN" -> Color(0xFFFFD700)
        "CO" -> Color(0xFF43A047)
        "SB" -> Color(0xFF1E88E5)
        "BB" -> Color(0xFF1E88E5)
        "MP", "HJ", "LJ" -> Color(0xFF7E57C2)
        "UTG", "UTG+1", "UTG+2" -> Color(0xFFEF5350)
        else -> Color(0xFF78909C)
    }
}

private fun getPositionFullName(position: String): String {
    return when (position.uppercase()) {
        "BTN" -> "Button"
        "CO" -> "Cutoff"
        "SB" -> "Small Blind"
        "BB" -> "Big Blind"
        "MP" -> "Middle Position"
        "HJ" -> "Hijack"
        "LJ" -> "Lojack"
        "UTG" -> "Under the Gun"
        "UTG+1" -> "Under the Gun +1"
        "UTG+2" -> "Under the Gun +2"
        else -> position
    }
}

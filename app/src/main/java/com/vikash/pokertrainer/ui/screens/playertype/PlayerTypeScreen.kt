package com.vikash.pokertrainer.ui.screens.playertype

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikash.pokertrainer.data.model.PlayerProfile
import com.vikash.pokertrainer.data.model.PlayerType
import com.vikash.pokertrainer.ui.theme.BluffRed
import com.vikash.pokertrainer.ui.theme.CheckBlue
import com.vikash.pokertrainer.ui.theme.ChipBlue
import com.vikash.pokertrainer.ui.theme.ChipGreen
import com.vikash.pokertrainer.ui.theme.CorrectGreen
import com.vikash.pokertrainer.ui.theme.FoldGray
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.IncorrectRed
import com.vikash.pokertrainer.ui.theme.MixedColor
import com.vikash.pokertrainer.ui.theme.PokerTableGreen
import com.vikash.pokertrainer.ui.theme.pokerColors
import com.vikash.pokertrainer.viewmodel.PlayerTypeViewModel

private enum class PlayerTypeMode { QUIZ, BROWSE }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerTypeScreen(
    viewModel: PlayerTypeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = pokerColors
    var mode by remember { mutableStateOf(PlayerTypeMode.QUIZ) }

    LaunchedEffect(Unit) {
        viewModel.loadProfiles()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Player Types",
                    fontWeight = FontWeight.SemiBold,
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
                if (mode == PlayerTypeMode.QUIZ && state.sessionTotal > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceHigh)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${state.sessionCorrect}/${state.sessionTotal}",
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surface,
                titleContentColor = colors.textPrimary,
                navigationIconContentColor = colors.textPrimary
            )
        )

        // Mode toggle tabs
        ModeToggle(
            mode = mode,
            onModeChange = { mode = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when (mode) {
            PlayerTypeMode.QUIZ -> QuizContent(
                viewModel = viewModel,
                state = state
            )
            PlayerTypeMode.BROWSE -> BrowseContent(
                profiles = state.allProfiles
            )
        }
    }
}

@Composable
private fun ModeToggle(
    mode: PlayerTypeMode,
    onModeChange: (PlayerTypeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = pokerColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceHigh)
            .padding(4.dp)
    ) {
        ModeTab(
            label = "Quiz",
            selected = mode == PlayerTypeMode.QUIZ,
            onClick = { onModeChange(PlayerTypeMode.QUIZ) },
            modifier = Modifier.weight(1f)
        )
        ModeTab(
            label = "Browse",
            selected = mode == PlayerTypeMode.BROWSE,
            onClick = { onModeChange(PlayerTypeMode.BROWSE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = pokerColors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) PokerTableGreen else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else colors.textSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

/* ------------------------------- QUIZ MODE ------------------------------- */

@Composable
private fun QuizContent(
    viewModel: PlayerTypeViewModel,
    state: com.vikash.pokertrainer.viewmodel.PlayerTypeUiState
) {
    val colors = pokerColors
    val profile = state.currentProfile

    if (profile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading profiles...",
                color = colors.textSecondary,
                fontSize = 16.sp
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Profile ${state.currentIndex + 1} of ${state.totalProfiles}",
            color = colors.textMuted,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        HudStatsCard(profile = profile)

        Spacer(modifier = Modifier.height(20.dp))

        if (!state.showResult) {
            Text(
                text = "What type of player is this?",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            val types = playerTypeOptions()

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in types.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for ((type, label) in row) {
                            PlayerTypeButton(
                                label = label,
                                color = playerTypeColor(type),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.selectType(type) }
                            )
                        }
                    }
                }
            }
        } else {
            AnimatedVisibility(
                visible = state.showResult,
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInVertically(animationSpec = tween(300)) { 30 }
            ) {
                Column {
                    ResultBanner(
                        isCorrect = state.isCorrect,
                        selectedLabel = formatTypeName(state.selectedType)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PlayerTypeInfoCard(profile = profile)
                    Spacer(modifier = Modifier.height(12.dp))
                    StrategyCard(profile = profile)
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.nextProfile() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PokerTableGreen
                        )
                    ) {
                        Text(
                            text = "Next Profile",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/* ------------------------------ BROWSE MODE ------------------------------ */

@Composable
private fun BrowseContent(profiles: List<PlayerProfile>) {
    val colors = pokerColors

    if (profiles.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading profiles...",
                color = colors.textSecondary,
                fontSize = 16.sp
            )
        }
        return
    }

    // Show one card per player type so the user can read descriptions.
    // If the dataset has multiple profiles per type, show the first of each.
    val uniqueByType = profiles.distinctBy { it.type }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Tap a player type to read its description and counter-strategy.",
            color = colors.textMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        uniqueByType.forEach { profile ->
            BrowseProfileCard(profile = profile)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BrowseProfileCard(profile: PlayerProfile) {
    val colors = pokerColors
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "arrow"
    )
    val accent = playerTypeColor(profile.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (colors.isDark) 2.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatTypeName(profile.type).take(1),
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatTypeName(profile.type),
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "VPIP ${profile.vpip} · PFR ${profile.pfr} · AF ${"%.1f".format(profile.af)}",
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = colors.textMuted,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expanded) 0f else arrowRotation)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                SectionLabel(text = "Description")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.description,
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                SectionLabel(text = "Best Strategy")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.bestStrategy,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = profile.strategyExplanation,
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                SectionLabel(text = "Typical Stats")
                Spacer(modifier = Modifier.height(8.dp))
                BrowseStatsGrid(profile = profile)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = GoldAccent,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun BrowseStatsGrid(profile: PlayerProfile) {
    val colors = pokerColors
    val stats = listOf(
        "VPIP" to "${profile.vpip}%",
        "PFR" to "${profile.pfr}%",
        "AF" to "%.1f".format(profile.af),
        "3-Bet" to "${"%.1f".format(profile.threeBet)}%",
        "Fold 3B" to "${"%.0f".format(profile.foldToThreeBet)}%",
        "C-Bet" to "${"%.0f".format(profile.cbet)}%"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in stats.chunked(3)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for ((label, value) in row) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceHigh)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                color = colors.textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = value,
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                // Fill remaining columns in the last row if it has fewer than 3 stats
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/* ------------------------------ SHARED PIECES ------------------------------ */

@Composable
private fun HudStatsCard(profile: PlayerProfile) {
    val colors = pokerColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (colors.isDark) 3.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HUD Stats — ${profile.name}",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PokerTableGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        color = CorrectGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HudStatItem(
                    label = "VPIP",
                    value = "${profile.vpip}%",
                    color = hudStatColor(profile.vpip.toDouble(), 15.0, 30.0)
                )
                HudStatItem(
                    label = "PFR",
                    value = "${profile.pfr}%",
                    color = hudStatColor(profile.pfr.toDouble(), 12.0, 25.0)
                )
                HudStatItem(
                    label = "AF",
                    value = "%.1f".format(profile.af),
                    color = hudStatColor(profile.af, 1.5, 3.5)
                )
                HudStatItem(
                    label = "3-Bet%",
                    value = "${"%.1f".format(profile.threeBet)}%",
                    color = hudStatColor(profile.threeBet, 4.0, 8.0)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HudStatItem(
                    label = "Fold 3B",
                    value = "${"%.0f".format(profile.foldToThreeBet)}%",
                    color = hudStatColor(profile.foldToThreeBet, 50.0, 70.0)
                )
                HudStatItem(
                    label = "C-Bet%",
                    value = "${"%.0f".format(profile.cbet)}%",
                    color = hudStatColor(profile.cbet, 50.0, 75.0)
                )
                HudStatItem(
                    label = "WTSD%",
                    value = "${"%.0f".format(profile.wtsd)}%",
                    color = hudStatColor(profile.wtsd, 22.0, 30.0)
                )
                Spacer(modifier = Modifier.width(64.dp))
            }
        }
    }
}

@Composable
private fun HudStatItem(
    label: String,
    value: String,
    color: Color
) {
    val colors = pokerColors
    Card(
        modifier = Modifier.width(72.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceHigh)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = colors.textMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlayerTypeButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = pokerColors
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = if (colors.isDark) 0.15f else 0.12f),
            contentColor = colors.textPrimary
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ResultBanner(isCorrect: Boolean, selectedLabel: String) {
    val colors = pokerColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect)
                CorrectGreen.copy(alpha = 0.15f)
            else
                IncorrectRed.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (isCorrect) CorrectGreen else IncorrectRed,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isCorrect) "Correct!" else "Incorrect",
                    color = if (isCorrect) CorrectGreen else IncorrectRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (!isCorrect) {
                    Text(
                        text = "You selected: $selectedLabel",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerTypeInfoCard(profile: PlayerProfile) {
    val colors = pokerColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Player Type",
                color = colors.textMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTypeName(profile.type),
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profile.description,
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun StrategyCard(profile: PlayerProfile) {
    val colors = pokerColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Best Strategy",
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profile.bestStrategy,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profile.strategyExplanation,
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

/* -------------------------------- HELPERS -------------------------------- */

private fun playerTypeOptions(): List<Pair<PlayerType, String>> = listOf(
    PlayerType.CALLING_STATION to "Calling Station",
    PlayerType.NIT to "Nit",
    PlayerType.MANIAC to "Maniac",
    PlayerType.TAG to "TAG",
    PlayerType.LAG to "LAG",
    PlayerType.FISH to "Fish"
)

private fun playerTypeColor(type: PlayerType): Color = when (type) {
    PlayerType.CALLING_STATION -> MixedColor
    PlayerType.NIT -> CheckBlue
    PlayerType.MANIAC -> BluffRed
    PlayerType.TAG -> ChipGreen
    PlayerType.LAG -> GoldAccent
    PlayerType.FISH -> FoldGray
}

private fun hudStatColor(value: Double, lowThreshold: Double, highThreshold: Double): Color {
    return when {
        value < lowThreshold -> ChipBlue
        value > highThreshold -> BluffRed
        else -> ChipGreen
    }
}

private fun formatTypeName(type: PlayerType?): String {
    return when (type) {
        PlayerType.CALLING_STATION -> "Calling Station"
        PlayerType.NIT -> "Nit"
        PlayerType.MANIAC -> "Maniac"
        PlayerType.TAG -> "TAG"
        PlayerType.LAG -> "LAG"
        PlayerType.FISH -> "Fish"
        null -> "Unknown"
    }
}

package com.vikash.pokertrainer.ui.screens.playertype

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikash.pokertrainer.data.model.PlayerType
import com.vikash.pokertrainer.ui.theme.BluffRed
import com.vikash.pokertrainer.ui.theme.CheckBlue
import com.vikash.pokertrainer.ui.theme.ChipBlue
import com.vikash.pokertrainer.ui.theme.ChipGreen
import com.vikash.pokertrainer.ui.theme.CorrectGreen
import com.vikash.pokertrainer.ui.theme.DarkBackground
import com.vikash.pokertrainer.ui.theme.DarkCard
import com.vikash.pokertrainer.ui.theme.DarkSurface
import com.vikash.pokertrainer.ui.theme.FoldGray
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.IncorrectRed
import com.vikash.pokertrainer.ui.theme.MixedColor
import com.vikash.pokertrainer.ui.theme.PokerTableGreen
import com.vikash.pokertrainer.ui.theme.TextMuted
import com.vikash.pokertrainer.ui.theme.TextPrimary
import com.vikash.pokertrainer.ui.theme.TextSecondary
import com.vikash.pokertrainer.viewmodel.PlayerTypeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerTypeScreen(
    viewModel: PlayerTypeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfiles()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "Player Type Trainer",
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
                // Session score
                if (state.sessionTotal > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCard)
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
                containerColor = DarkSurface,
                titleContentColor = TextPrimary,
                navigationIconContentColor = TextPrimary
            )
        )

        val profile = state.currentProfile
        if (profile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading profiles...",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Progress indicator
            Text(
                text = "Profile ${state.currentIndex + 1} of ${state.totalProfiles}",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // HUD Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // HUD header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HUD Stats - ${profile.name}",
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

                    // Stats grid - Row 1
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
                            value = "${"%.1f".format(profile.af)}",
                            color = hudStatColor(profile.af, 1.5, 3.5)
                        )
                        HudStatItem(
                            label = "3-Bet%",
                            value = "${"%.1f".format(profile.threeBet)}%",
                            color = hudStatColor(profile.threeBet, 4.0, 8.0)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats grid - Row 2
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
                        // Empty spacer for alignment
                        Spacer(modifier = Modifier.width(64.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!state.showResult) {
                // Question
                Text(
                    text = "What type of player is this?",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Answer buttons - 2 columns, 3 rows
                val types = listOf(
                    PlayerType.CALLING_STATION to "Calling Station",
                    PlayerType.NIT to "Nit",
                    PlayerType.MANIAC to "Maniac",
                    PlayerType.TAG to "TAG",
                    PlayerType.LAG to "LAG",
                    PlayerType.FISH to "Fish"
                )

                val typeColors = mapOf(
                    PlayerType.CALLING_STATION to MixedColor,
                    PlayerType.NIT to CheckBlue,
                    PlayerType.MANIAC to BluffRed,
                    PlayerType.TAG to ChipGreen,
                    PlayerType.LAG to GoldAccent,
                    PlayerType.FISH to FoldGray
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (row in types.chunked(2)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for ((type, label) in row) {
                                val color = typeColors[type] ?: TextMuted
                                PlayerTypeButton(
                                    label = label,
                                    color = color,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.selectType(type) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Result display
                AnimatedVisibility(
                    visible = state.showResult,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(animationSpec = tween(400)) { 30 }
                ) {
                    Column {
                        // Correct/Incorrect banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.isCorrect)
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
                                    imageVector = if (state.isCorrect)
                                        Icons.Filled.CheckCircle
                                    else
                                        Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = if (state.isCorrect) CorrectGreen else IncorrectRed,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (state.isCorrect) "Correct!" else "Incorrect",
                                        color = if (state.isCorrect) CorrectGreen else IncorrectRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    if (!state.isCorrect) {
                                        Text(
                                            text = "You selected: ${formatTypeName(state.selectedType)}",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Correct type
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Player Type",
                                    color = TextMuted,
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
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Best strategy
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
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
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = profile.strategyExplanation,
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Next button
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
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HudStatItem(
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(72.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = TextMuted,
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
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = TextPrimary
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

/**
 * Returns color based on whether value is low (blue), normal (green), or high (red).
 */
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

package com.vikash.pokertrainer.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikash.pokertrainer.ui.components.StatBar
import com.vikash.pokertrainer.ui.theme.BluffRed
import com.vikash.pokertrainer.ui.theme.CheckBlue
import com.vikash.pokertrainer.ui.theme.ChipGreen
import com.vikash.pokertrainer.ui.theme.CorrectGreen
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.IncorrectRed
import com.vikash.pokertrainer.ui.theme.MixedColor
import com.vikash.pokertrainer.ui.theme.PokerTableGreen
import com.vikash.pokertrainer.ui.theme.pokerColors
import com.vikash.pokertrainer.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pokerColors.background)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "Your Progress",
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = pokerColors.surface,
                titleContentColor = pokerColors.textPrimary,
                navigationIconContentColor = pokerColors.textPrimary
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = pokerColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OverviewStatItem(
                            label = "Total Hands",
                            value = state.totalAttempted.toString(),
                            color = GoldAccent
                        )
                        VerticalDivider()
                        OverviewStatItem(
                            label = "Accuracy",
                            value = "${"%.1f".format(state.overallAccuracy)}%",
                            color = accuracyColor(state.overallAccuracy)
                        )
                        VerticalDivider()
                        OverviewStatItem(
                            label = "Correct",
                            value = state.totalCorrect.toString(),
                            color = CorrectGreen
                        )
                    }
                }
            }

            // Street accuracy section
            item {
                SectionHeader(title = "Street Accuracy")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = pokerColors.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBar(
                            label = "Preflop",
                            value = state.preflopAccuracy.toFloat(),
                            color = accuracyColor(state.preflopAccuracy)
                        )
                        StatBar(
                            label = "Flop",
                            value = state.flopAccuracy.toFloat(),
                            color = accuracyColor(state.flopAccuracy)
                        )
                        StatBar(
                            label = "Turn",
                            value = state.turnAccuracy.toFloat(),
                            color = accuracyColor(state.turnAccuracy)
                        )
                        StatBar(
                            label = "River",
                            value = state.riverAccuracy.toFloat(),
                            color = accuracyColor(state.riverAccuracy)
                        )
                    }
                }
            }

            // Player type accuracy
            item {
                SectionHeader(title = "Player Type Accuracy")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = pokerColors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatBar(
                            label = "Player Type ID",
                            value = state.playerTypeAccuracy.toFloat(),
                            color = accuracyColor(state.playerTypeAccuracy)
                        )
                    }
                }
            }

            // Weaknesses
            if (state.weaknesses.isNotEmpty()) {
                item {
                    SectionHeader(title = "Areas to Improve")
                }

                items(state.weaknesses) { weakness ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = IncorrectRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MixedColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = weakness,
                                color = pokerColors.textPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Strengths
            if (state.strengths.isNotEmpty()) {
                item {
                    SectionHeader(title = "Strengths")
                }

                items(state.strengths) { strength ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CorrectGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = CorrectGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strength,
                                color = pokerColors.textPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Category breakdown
            if (state.categoryStats.isNotEmpty()) {
                item {
                    SectionHeader(title = "Category Breakdown")
                }

                items(state.categoryStats) { stat ->
                    val accuracy = if (stat.total > 0) {
                        (stat.correct.toDouble() / stat.total * 100)
                    } else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = pokerColors.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stat.category,
                                    color = pokerColors.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${stat.correct}/${stat.total} (${"%.0f".format(accuracy)}%)",
                                    color = accuracyColor(accuracy),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            StatBar(
                                label = "",
                                value = accuracy.toFloat(),
                                color = accuracyColor(accuracy)
                            )
                        }
                    }
                }
            }

            // Reset button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = IncorrectRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset All Progress",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = pokerColors.surface,
            titleContentColor = pokerColors.textPrimary,
            textContentColor = pokerColors.textSecondary,
            title = {
                Text(
                    text = "Reset Progress?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all your training progress and statistics. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IncorrectRed
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = pokerColors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun OverviewStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = pokerColors.textSecondary
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(pokerColors.textMuted.copy(alpha = 0.3f))
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = pokerColors.textPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

/**
 * Returns a color based on accuracy percentage:
 * red for low, orange for moderate, green for good, bright green for great.
 */
@Composable
private fun accuracyColor(accuracy: Double): Color {
    return when {
        accuracy >= 80 -> CorrectGreen
        accuracy >= 60 -> ChipGreen
        accuracy >= 40 -> MixedColor
        accuracy > 0 -> IncorrectRed
        else -> pokerColors.textMuted
    }
}

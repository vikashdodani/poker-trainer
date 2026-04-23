package com.vikash.pokertrainer.ui.screens.gto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikash.pokertrainer.ui.theme.CallColor
import com.vikash.pokertrainer.ui.theme.FoldColor
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.MixedColor
import com.vikash.pokertrainer.ui.theme.RaiseColor
import com.vikash.pokertrainer.ui.theme.pokerColors
import com.vikash.pokertrainer.viewmodel.GtoHandInfo
import com.vikash.pokertrainer.viewmodel.GtoViewModel
import com.vikash.pokertrainer.viewmodel.RangeStats

@Composable
fun GtoChartScreen(
    viewModel: GtoViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val rangeStats = remember(uiState.selectedPosition, uiState.gameType) {
        viewModel.getRangeStats(uiState.selectedPosition, uiState.gameType)
    }

    LaunchedEffect(uiState.showHandDetail) {
        if (uiState.showHandDetail) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pokerColors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        TopBar(onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        PositionSelector(
            positions = viewModel.positions,
            selectedPosition = uiState.selectedPosition,
            onPositionSelected = { viewModel.selectPosition(it) }
        )

        viewModel.positionDescriptions[uiState.selectedPosition]?.let { desc ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = pokerColors.textMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        GameTypeToggle(
            selectedType = uiState.gameType,
            onTypeSelected = { viewModel.setGameType(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        GridOrientationHint()

        Spacer(modifier = Modifier.height(8.dp))

        HandGrid(
            viewModel = viewModel,
            position = uiState.selectedPosition,
            gameType = uiState.gameType,
            selectedHand = uiState.selectedHand?.hand,
            onHandSelected = { viewModel.selectHand(it) }
        )

        AnimatedVisibility(
            visible = uiState.showHandDetail && uiState.selectedHand != null,
            enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 }
        ) {
            uiState.selectedHand?.let { hand ->
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HandDetailCard(
                        handInfo = hand,
                        positionHands = uiState.positionHandsForSelected,
                        positions = viewModel.positions,
                        onDismiss = { viewModel.dismissHandDetail() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RangeStatsCard(stats = rangeStats)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(pokerColors.surface)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = pokerColors.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "GTO Preflop Charts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = pokerColors.textPrimary
            )
            Text(
                text = "Opening ranges by position",
                fontSize = 12.sp,
                color = pokerColors.textMuted
            )
        }
    }
}

@Composable
private fun PositionSelector(
    positions: List<String>,
    selectedPosition: String,
    onPositionSelected: (String) -> Unit
) {
    val rows = positions.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowPositions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPositions.forEach { position ->
                    val isSelected = position == selectedPosition
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GoldAccent else pokerColors.surface)
                            .clickable { onPositionSelected(position) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = position,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) pokerColors.background else pokerColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameTypeToggle(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val types = listOf("Cash", "Tournament")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(pokerColors.surface)
            .padding(4.dp)
    ) {
        types.forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) pokerColors.surfaceHigh else Color.Transparent)
                    .clickable { onTypeSelected(type) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) pokerColors.textPrimary else pokerColors.textMuted
                )
            }
        }
    }
}

@Composable
private fun GridOrientationHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(pokerColors.surface.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "↗ Suited", fontSize = 11.sp, color = pokerColors.textMuted, fontWeight = FontWeight.Medium)
        Text(text = "◆ Pairs (diagonal)", fontSize = 11.sp, color = GoldAccent.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
        Text(text = "Offsuit ↙", fontSize = 11.sp, color = pokerColors.textMuted, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HandGrid(
    viewModel: GtoViewModel,
    position: String,
    gameType: String,
    selectedHand: String?,
    onHandSelected: (GtoHandInfo) -> Unit
) {
    val ranks = viewModel.ranks
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    // 24dp outer padding + 16dp card inner padding (8dp each side) + 20dp row header
    val headerWidth = 20.dp
    val cellSize = (screenWidth - 24.dp - 16.dp - headerWidth) / 13

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = pokerColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row {
                Box(modifier = Modifier.size(width = headerWidth, height = 20.dp))
                ranks.forEach { rank ->
                    Box(
                        modifier = Modifier.size(width = cellSize, height = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = pokerColors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            ranks.forEachIndexed { rowIndex, rowRank ->
                Row {
                    Box(
                        modifier = Modifier.size(width = headerWidth, height = cellSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rowRank,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = pokerColors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }

                    ranks.forEachIndexed { colIndex, _ ->
                        val handInfo = remember(rowIndex, colIndex, position, gameType) {
                            viewModel.getHandAction(rowIndex, colIndex, position, gameType)
                        }
                        HandCell(
                            handInfo = handInfo,
                            size = cellSize,
                            isDiagonal = rowIndex == colIndex,
                            isSelected = handInfo.hand == selectedHand,
                            onClick = { onHandSelected(handInfo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HandCell(
    handInfo: GtoHandInfo,
    size: Dp,
    isDiagonal: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (handInfo.color) {
        0 -> FoldColor.copy(alpha = 0.25f)
        1 -> CallColor.copy(alpha = 0.85f)
        2 -> RaiseColor.copy(alpha = 0.9f)
        3 -> MixedColor.copy(alpha = 0.85f)
        else -> FoldColor.copy(alpha = 0.25f)
    }
    val textColor = if (handInfo.color == 0) pokerColors.textMuted else Color.White
    val borderColor = when {
        isSelected -> Color.White
        isDiagonal -> GoldAccent.copy(alpha = 0.55f)
        else -> null
    }
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Box(
        modifier = Modifier
            .size(size)
            .padding(0.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (isSelected) backgroundColor.copy(alpha = minOf(1f, backgroundColor.alpha + 0.2f)) else backgroundColor)
            .then(
                if (borderColor != null) Modifier.border(borderWidth, borderColor, RoundedCornerShape(2.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = handInfo.hand,
            fontSize = if (handInfo.hand.length > 2) 7.sp else 8.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun RangeStatsCard(stats: RangeStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = pokerColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Range Breakdown",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = pokerColors.textMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("Raise", stats.raisePercent, RaiseColor),
                    Triple("Call", stats.callPercent, CallColor),
                    Triple("Mixed", stats.mixedPercent, MixedColor),
                    Triple("Fold", stats.foldPercent, FoldColor)
                ).forEach { (label, pct, color) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$pct%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(pokerColors.surfaceHigh)
                        ) {
                            if (pct > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pct / 100f)
                                        .fillMaxHeight()
                                        .background(color.copy(alpha = 0.85f))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = pokerColors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${stats.playedCount} of ${stats.total} hands played (${(stats.playedCount * 100) / stats.total}%)",
                fontSize = 11.sp,
                color = pokerColors.textMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HandDetailCard(
    handInfo: GtoHandInfo,
    positionHands: Map<String, GtoHandInfo>,
    positions: List<String>,
    onDismiss: () -> Unit
) {
    val actionColor = when (handInfo.color) {
        0 -> FoldColor
        1 -> CallColor
        2 -> RaiseColor
        3 -> MixedColor
        else -> FoldColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, actionColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = pokerColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: hand name + action badge + close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = handInfo.hand,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = pokerColors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(actionColor.copy(alpha = 0.2f))
                            .border(1.dp, actionColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = handInfo.action,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = actionColor
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(pokerColors.surfaceHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = pokerColors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            val handType = when {
                handInfo.hand.length == 2 -> "Pocket Pair"
                handInfo.hand.endsWith("s") -> "Suited"
                handInfo.hand.endsWith("o") -> "Offsuit"
                else -> ""
            }
            Text(
                text = handType,
                fontSize = 12.sp,
                color = pokerColors.textMuted,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Position frequency breakdown
            if (positionHands.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Frequency by position",
                    fontSize = 11.sp,
                    color = pokerColors.textMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    positions.forEach { pos ->
                        val h = positionHands[pos]
                        val color = when (h?.color) {
                            0 -> FoldColor
                            1 -> CallColor
                            2 -> RaiseColor
                            3 -> MixedColor
                            else -> FoldColor
                        }
                        val actionLabel = when (h?.color) {
                            1 -> "C"
                            2 -> "R"
                            3 -> "M"
                            else -> "F"
                        }
                        // Primary action frequency for this position
                        val primaryFreq = when (h?.color) {
                            2 -> h.raiseFreq
                            1 -> h.callFreq
                            3 -> maxOf(h.raiseFreq, h.callFreq)
                            else -> 100
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color.copy(alpha = 0.2f))
                                    .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = actionLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "$primaryFreq%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = color,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = pos,
                                fontSize = 10.sp,
                                color = pokerColors.textMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Mixed hand breakdown for the selected position
                if (handInfo.color == 3 || handInfo.raiseFreq in 1..99 || handInfo.callFreq in 1..99) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FrequencyBreakdownRow(handInfo = handInfo)
                }
            }

            // Explanation
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(pokerColors.surfaceHigh)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = handInfo.explanation,
                    fontSize = 13.sp,
                    color = pokerColors.textSecondary,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun FrequencyBreakdownRow(handInfo: GtoHandInfo) {
    val segments = buildList {
        if (handInfo.raiseFreq > 0) add(Triple("Raise", handInfo.raiseFreq, RaiseColor))
        if (handInfo.callFreq > 0) add(Triple("Call", handInfo.callFreq, CallColor))
        if (handInfo.foldFreq > 0) add(Triple("Fold", handInfo.foldFreq, FoldColor))
    }
    if (segments.isEmpty()) return

    Column {
        Text(
            text = "This position breakdown",
            fontSize = 11.sp,
            color = pokerColors.textMuted,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        // Stacked bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            segments.forEach { (_, pct, color) ->
                Box(
                    modifier = Modifier
                        .weight(pct.toFloat())
                        .fillMaxHeight()
                        .background(color.copy(alpha = 0.85f))
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        // Labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            segments.forEach { (label, pct, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color.copy(alpha = 0.85f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$label $pct%",
                        fontSize = 11.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B2838)
@Composable
private fun GtoChartScreenPreview() {
    GtoChartScreen(
        viewModel = GtoViewModel(),
        onBack = {}
    )
}

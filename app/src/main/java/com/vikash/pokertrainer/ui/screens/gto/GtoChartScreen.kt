package com.vikash.pokertrainer.ui.screens.gto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.vikash.pokertrainer.ui.theme.DarkBackground
import com.vikash.pokertrainer.ui.theme.DarkCard
import com.vikash.pokertrainer.ui.theme.DarkSurface
import com.vikash.pokertrainer.ui.theme.FoldColor
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.MixedColor
import com.vikash.pokertrainer.ui.theme.RaiseColor
import com.vikash.pokertrainer.ui.theme.TextMuted
import com.vikash.pokertrainer.ui.theme.TextPrimary
import com.vikash.pokertrainer.ui.theme.TextSecondary
import com.vikash.pokertrainer.viewmodel.GtoHandInfo
import com.vikash.pokertrainer.viewmodel.GtoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GtoChartScreen(
    viewModel: GtoViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top bar
            TopBar(onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Position selector
            PositionSelector(
                positions = viewModel.positions,
                selectedPosition = uiState.selectedPosition,
                onPositionSelected = { viewModel.selectPosition(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Game type toggle
            GameTypeToggle(
                selectedType = uiState.gameType,
                onTypeSelected = { viewModel.setGameType(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hand grid
            HandGrid(
                viewModel = viewModel,
                position = uiState.selectedPosition,
                onHandSelected = { viewModel.selectHand(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Legend()

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom sheet for hand detail
        if (uiState.showHandDetail && uiState.selectedHand != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissHandDetail() },
                sheetState = sheetState,
                containerColor = DarkSurface,
                contentColor = TextPrimary
            ) {
                HandDetailContent(handInfo = uiState.selectedHand!!)
            }
        }
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
                .background(DarkSurface)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "GTO Preflop Charts",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun PositionSelector(
    positions: List<String>,
    selectedPosition: String,
    onPositionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        positions.forEach { position ->
            val isSelected = position == selectedPosition
            val bgColor = if (isSelected) GoldAccent else DarkSurface
            val textColor = if (isSelected) DarkBackground else TextSecondary

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .clickable { onPositionSelected(position) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = position,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
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
            .background(DarkSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        types.forEach { type ->
            val isSelected = type == selectedType
            val bgColor = if (isSelected) DarkCard else Color.Transparent
            val textColor = if (isSelected) TextPrimary else TextMuted

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onTypeSelected(type) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun HandGrid(
    viewModel: GtoViewModel,
    position: String,
    onHandSelected: (GtoHandInfo) -> Unit
) {
    val ranks = viewModel.ranks
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val gridPadding = 24.dp // 12dp on each side
    val availableWidth = screenWidth - gridPadding
    val headerWidth = 20.dp
    val cellSize = (availableWidth - headerWidth) / 13
    val gridBorderColor = DarkBackground

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Column headers
            Row {
                // Empty corner cell
                Box(
                    modifier = Modifier.size(width = headerWidth, height = 20.dp),
                    contentAlignment = Alignment.Center
                ) {}

                ranks.forEach { rank ->
                    Box(
                        modifier = Modifier.size(width = cellSize, height = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Grid rows
            ranks.forEachIndexed { rowIndex, rowRank ->
                Row {
                    // Row header
                    Box(
                        modifier = Modifier.size(width = headerWidth, height = cellSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rowRank,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Hand cells
                    ranks.forEachIndexed { colIndex, _ ->
                        val handInfo = remember(rowIndex, colIndex, position) {
                            viewModel.getHandAction(rowIndex, colIndex, position)
                        }

                        HandCell(
                            handInfo = handInfo,
                            size = cellSize,
                            isDiagonal = rowIndex == colIndex,
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
    onClick: () -> Unit
) {
    val backgroundColor = when (handInfo.color) {
        0 -> FoldColor.copy(alpha = 0.4f)
        1 -> CallColor.copy(alpha = 0.7f)
        2 -> RaiseColor.copy(alpha = 0.75f)
        3 -> MixedColor.copy(alpha = 0.7f)
        else -> FoldColor.copy(alpha = 0.4f)
    }

    val borderColor = if (isDiagonal) {
        GoldAccent.copy(alpha = 0.5f)
    } else {
        DarkBackground.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .size(size)
            .padding(0.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(
                width = if (isDiagonal) 1.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = handInfo.hand,
            fontSize = if (handInfo.hand.length > 2) 7.sp else 8.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun Legend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = RaiseColor.copy(alpha = 0.75f), label = "Raise")
            LegendItem(color = CallColor.copy(alpha = 0.7f), label = "Call")
            LegendItem(color = FoldColor.copy(alpha = 0.4f), label = "Fold")
            LegendItem(color = MixedColor.copy(alpha = 0.7f), label = "Mixed")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HandDetailContent(handInfo: GtoHandInfo) {
    val actionColor = when (handInfo.color) {
        0 -> FoldColor
        1 -> CallColor
        2 -> RaiseColor
        3 -> MixedColor
        else -> FoldColor
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Hand name large
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = handInfo.hand,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Action badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(actionColor.copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        color = actionColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = handInfo.action,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = actionColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hand type label
        val handType = when {
            handInfo.hand.length == 2 -> "Pocket Pair"
            handInfo.hand.endsWith("s") -> "Suited"
            handInfo.hand.endsWith("o") -> "Offsuit"
            else -> ""
        }
        Text(
            text = handType,
            fontSize = 14.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Explanation card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = handInfo.explanation,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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

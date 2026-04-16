package com.vikash.pokertrainer.ui.screens.home

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.pokerColors

private val FeatureGreen = Color(0xFF43A047)
private val FeatureBlue = Color(0xFF1E88E5)
private val FeatureOrange = Color(0xFFFFA726)
private val FeaturePurple = Color(0xFFAB47BC)

@Composable
fun HomeScreen(
    totalAttempted: Int,
    accuracy: Double,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToScenario: () -> Unit,
    onNavigateToGto: () -> Unit,
    onNavigateToPlayerType: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val colors = pokerColors

    // Single fade-in on first composition — much cheaper than staggered animations.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        // Top bar: title + theme toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Poker Trainer",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Text(
                    text = "Master Your Game",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }

            ThemeToggle(isDarkMode = isDarkMode, onToggle = onToggleTheme)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // One fade+slide for the whole content block — keeps it snappy.
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(350)) +
                    slideInVertically(animationSpec = tween(350)) { 40 }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Stats summary at the top — most relevant info first.
                StatsBar(totalAttempted = totalAttempted, accuracy = accuracy)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "TRAIN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMuted,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )

                val features = listOf(
                    FeatureTileData(
                        title = "Scenario Trainer",
                        description = "Practice decisions in real poker scenarios",
                        icon = Icons.Filled.BarChart,
                        accentColor = FeatureGreen,
                        onClick = onNavigateToScenario
                    ),
                    FeatureTileData(
                        title = "GTO Charts",
                        description = "Study optimal preflop strategies",
                        icon = Icons.Filled.GridOn,
                        accentColor = FeatureBlue,
                        onClick = onNavigateToGto
                    ),
                    FeatureTileData(
                        title = "Player Types",
                        description = "Learn to exploit different opponents",
                        icon = Icons.Filled.People,
                        accentColor = FeatureOrange,
                        onClick = onNavigateToPlayerType
                    ),
                    FeatureTileData(
                        title = "Statistics",
                        description = "Track your progress and find leaks",
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        accentColor = FeaturePurple,
                        onClick = onNavigateToStats
                    )
                )

                features.forEachIndexed { index, feature ->
                    FeatureTile(data = feature)
                    if (index < features.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ThemeToggle(isDarkMode: Boolean, onToggle: () -> Unit) {
    val colors = pokerColors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.surfaceHigh)
            .clickable(onClick = onToggle)
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = if (isDarkMode) "Switch to light mode" else "Switch to dark mode",
            tint = GoldAccent,
            modifier = Modifier.size(22.dp)
        )
    }
}

private data class FeatureTileData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun FeatureTile(data: FeatureTileData) {
    val colors = pokerColors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = data.onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (colors.isDark) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(data.accentColor.copy(alpha = if (colors.isDark) 0.15f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = data.title,
                    tint = data.accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.description,
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StatsBar(totalAttempted: Int, accuracy: Double) {
    val colors = pokerColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceHigh),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (colors.isDark) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Total Hands",
                value = totalAttempted.toString()
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(colors.divider)
            )

            StatItem(
                label = "Accuracy",
                value = "${"%.1f".format(accuracy)}%"
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val colors = pokerColors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GoldAccent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.textSecondary
        )
    }
}

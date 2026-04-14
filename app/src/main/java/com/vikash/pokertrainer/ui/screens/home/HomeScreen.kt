package com.vikash.pokertrainer.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikash.pokertrainer.ui.theme.DarkBackground
import com.vikash.pokertrainer.ui.theme.DarkCard
import com.vikash.pokertrainer.ui.theme.DarkSurface
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.TextMuted
import com.vikash.pokertrainer.ui.theme.TextPrimary
import com.vikash.pokertrainer.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private val FeatureGreen = Color(0xFF43A047)
private val FeatureBlue = Color(0xFF1E88E5)
private val FeatureOrange = Color(0xFFFFA726)
private val FeaturePurple = Color(0xFFAB47BC)

@Composable
fun HomeScreen(
    totalAttempted: Int,
    accuracy: Double,
    onNavigateToScenario: () -> Unit,
    onNavigateToGto: () -> Unit,
    onNavigateToPlayerType: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(animationSpec = tween(600)) { -40 }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Poker Trainer",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Master Your Game",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Feature tiles
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
                icon = Icons.Filled.ShowChart,
                accentColor = FeaturePurple,
                onClick = onNavigateToStats
            )
        )

        features.forEachIndexed { index, feature ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 150 * (index + 1))) +
                        slideInVertically(
                            animationSpec = tween(500, delayMillis = 150 * (index + 1))
                        ) { 60 }
            ) {
                FeatureTile(data = feature)
            }
            if (index < features.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats summary bar
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 800)) +
                    slideInVertically(animationSpec = tween(500, delayMillis = 800)) { 40 }
        ) {
            StatsBar(totalAttempted = totalAttempted, accuracy = accuracy)
        }

        Spacer(modifier = Modifier.height(16.dp))
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = data.onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(data.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = data.title,
                    tint = data.accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.description,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Arrow indicator
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatsBar(totalAttempted: Int, accuracy: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp),
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
                    .background(TextMuted.copy(alpha = 0.3f))
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
            color = TextSecondary
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B2838)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        totalAttempted = 342,
        accuracy = 68.5,
        onNavigateToScenario = {},
        onNavigateToGto = {},
        onNavigateToPlayerType = {},
        onNavigateToStats = {}
    )
}

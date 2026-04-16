package com.vikash.pokertrainer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikash.pokertrainer.ui.theme.CardBlack
import com.vikash.pokertrainer.ui.theme.CardRed
import com.vikash.pokertrainer.ui.theme.CardWhite
import com.vikash.pokertrainer.ui.theme.ChipBlue
import com.vikash.pokertrainer.ui.theme.ChipRed
import com.vikash.pokertrainer.ui.theme.CorrectGreen
import com.vikash.pokertrainer.ui.theme.GoldAccent
import com.vikash.pokertrainer.ui.theme.IncorrectRed
import com.vikash.pokertrainer.ui.theme.PokerDarkGreen
import com.vikash.pokertrainer.ui.theme.PokerTableGreen
import com.vikash.pokertrainer.ui.theme.pokerColors

/**
 * Renders a single playing card with proper suit coloring.
 * Supports both unicode ("A♠") and letter-based ("As", "Qd") card formats.
 */
@Composable
fun PlayingCard(
    card: String,
    modifier: Modifier = Modifier
) {
    val unicodeSuits = setOf('♠', '♥', '♦', '♣')
    val suitLetterMap = mapOf(
        's' to "♠", 'S' to "♠",
        'h' to "♥", 'H' to "♥",
        'd' to "♦", 'D' to "♦",
        'c' to "♣", 'C' to "♣"
    )

    val lastChar = card.lastOrNull()
    val suit: String
    val rank: String

    if (lastChar != null && lastChar in unicodeSuits) {
        suit = lastChar.toString()
        rank = card.dropLast(1)
    } else if (lastChar != null && lastChar in suitLetterMap) {
        suit = suitLetterMap[lastChar] ?: ""
        rank = card.dropLast(1)
    } else {
        suit = ""
        rank = card
    }

    val suitColor = when (suit) {
        "♥", "♦" -> CardRed
        else -> CardBlack
    }

    Box(
        modifier = modifier
            .width(56.dp)
            .height(80.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(CardWhite)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        // Top-left rank and suit
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = rank,
                color = suitColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
            Text(
                text = suit,
                color = suitColor,
                fontSize = 12.sp,
                lineHeight = 12.sp
            )
        }

        // Center suit
        Text(
            text = suit,
            color = suitColor,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        // Bottom-right rank and suit (inverted)
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = suit,
                color = suitColor,
                fontSize = 12.sp,
                lineHeight = 12.sp
            )
            Text(
                text = rank,
                color = suitColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Displays a horizontal row of playing cards with spacing between them.
 */
@Composable
fun CardRow(
    cards: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cards.forEach { card ->
            PlayingCard(card = card)
        }
    }
}

/**
 * A color-coded action button for poker decisions (Raise, Call, Fold, Check).
 *
 * @param isCorrect null = unresolved, true = correct answer, false = incorrect answer
 * @param isSelected whether this button is currently selected
 */
@Composable
fun ActionButton(
    text: String,
    color: Color,
    isCorrect: Boolean? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when (isCorrect) {
            true -> CorrectGreen
            false -> IncorrectRed
            null -> if (isSelected) color else Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isCorrect == true -> CorrectGreen.copy(alpha = 0.2f)
            isCorrect == false -> IncorrectRed.copy(alpha = 0.2f)
            isSelected -> color.copy(alpha = 0.3f)
            else -> color.copy(alpha = 0.15f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "containerColor"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = pokerColors.textPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(48.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

/**
 * A small circular poker chip icon with a value label.
 */
@Composable
fun PokerChip(
    value: String,
    color: Color = ChipRed
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(color)
            .border(3.dp, color.copy(alpha = 0.5f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A horizontal progress bar for displaying stats (e.g., win rate, VPIP).
 */
@Composable
fun StatBar(
    label: String,
    value: Float,
    maxValue: Float = 100f,
    color: Color = PokerTableGreen
) {
    val fraction = if (maxValue > 0f) (value / maxValue).coerceIn(0f, 1f) else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 600),
        label = "statBarFraction"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = pokerColors.textSecondary,
                fontSize = 13.sp
            )
            Text(
                text = "${value.toInt()}",
                color = pokerColors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(pokerColors.surfaceHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

/**
 * A small badge showing a player's table position (BTN, SB, BB, UTG, etc.).
 */
@Composable
fun PositionBadge(position: String) {
    val bgColor = when (position.uppercase()) {
        "BTN" -> GoldAccent
        "SB" -> ChipBlue
        "BB" -> ChipBlue
        "CO" -> PokerTableGreen
        "HJ" -> PokerTableGreen
        "LJ" -> PokerDarkGreen
        "UTG", "UTG+1", "UTG+2" -> pokerColors.textMuted
        else -> pokerColors.textMuted
    }

    val textColor = when (position.uppercase()) {
        "BTN" -> CardBlack
        else -> pokerColors.textPrimary
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = position.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

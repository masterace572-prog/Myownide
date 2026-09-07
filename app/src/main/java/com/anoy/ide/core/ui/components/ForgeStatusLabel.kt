package com.anoy.ide.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anoy.ide.core.ui.theme.ForgeColors

/**
 * A status expressed as text plus a small neutral glyph. Per the PRD, status is
 * communicated with words; never with pills, emoji, or colored fills.
 */
enum class StatusTone {
    Neutral,
    Success,
    Error,
    Warning
}

@Composable
fun ForgeStatusLabel(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier
) {
    val stateColor = { light: Color, dark: Color ->
        if (isSystemInDarkTheme()) dark else light
    }
    val color = when (tone) {
        StatusTone.Success -> stateColor(ForgeColors.SuccessLight, ForgeColors.SuccessDark)
        StatusTone.Error -> MaterialTheme.colorScheme.error
        StatusTone.Warning -> stateColor(ForgeColors.WarningLight, ForgeColors.WarningDark)
        StatusTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(6.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

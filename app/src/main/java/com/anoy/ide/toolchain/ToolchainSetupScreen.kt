package com.anoy.ide.toolchain

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anoy.ide.core.ui.components.ForgePrimaryButton
import com.anoy.ide.core.ui.components.ForgeStatusLabel
import com.anoy.ide.core.ui.components.StatusTone

/**
 * Toolchain setup screen: components, preflight hints and per-component status.
 * Status is rendered as words + a small neutral glyph, never pills.
 */
@Composable
fun ToolchainSetupScreen(
    onComplete: () -> Unit,
    viewModel: ToolchainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Set up your toolchain",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Forge downloads and verifies a complete Android build environment so you never need a laptop.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Only the required items are needed to compile Android projects. Git, NDK, extra API levels and offline sources are optional and can be added later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PreflightCard()

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 28.dp, end = 28.dp, bottom = 16.dp
            )
        ) {
            itemsIndexed(uiState.components, key = { _, item -> item.name }) { _, component ->
                ToolchainRow(component)
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
            }
        }

        if (uiState.isRunning) {
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            if (uiState.isComplete) {
                Text(
                    text = "Toolchain ready. Total size about 1.3 GB.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            ForgePrimaryButton(
                text = if (uiState.isComplete) "Create your first project" else "Set up toolchain",
                onClick = if (uiState.isComplete) onComplete else viewModel::startSetup,
                enabled = !uiState.isRunning,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PreflightCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Before you start",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Required setup: about 500 MB. Storage: 2.8 GB free recommended. RAM: 6 GB. Network: Wi-Fi recommended. Optional items add roughly 700 MB.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToolchainRow(component: ToolchainComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            val subtitle = buildString {
                append("${component.version} · ${component.size}")
                append(if (component.isOptional) " · optional" else " · required")
                component.note?.let { append(" · $it") }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ForgeStatusLabel(
            text = when (component.status) {
                ComponentStatus.Pending -> "Pending"
                ComponentStatus.Installing -> "Installing"
                ComponentStatus.Verified -> "Verified"
                ComponentStatus.Error -> "Error"
            },
            tone = when (component.status) {
                ComponentStatus.Verified -> StatusTone.Success
                ComponentStatus.Error -> StatusTone.Error
                ComponentStatus.Installing -> StatusTone.Warning
                ComponentStatus.Pending -> StatusTone.Neutral
            }
        )
    }
}

package com.anoy.ide.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anoy.ide.core.ui.components.ForgePrimaryButton
import com.anoy.ide.core.ui.components.ForgeStatusLabel
import com.anoy.ide.core.ui.components.StatusTone
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BuildVariant(val displayName: String) {
    DEBUG("Debug"),
    RELEASE("Release")
}

/**
 * Build tab (M1 first pass). Variant selector + assemble action. The actual
 * build engine will invoke the project's Gradle wrapper; for now the UI drives
 * a simulated task so the experience is wired and extendable.
 */
@Composable
fun BuildScreen() {
    var variant by remember { mutableStateOf(BuildVariant.DEBUG) }
    var module by remember { mutableStateOf("app") }
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var status by remember { mutableStateOf<String?>(null) }
    var log by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun taskName(): String = module + ":assemble" +
        variant.name.lowercase().replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Build",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Choose a variant, then assemble or bundle. Debug installs on this device use PackageInstaller.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
        VariantSelector(
            variant = variant,
            onSelect = { if (!running) variant = it },
            label = "Variant"
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Module $module",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        ForgePrimaryButton(
            text = if (variant == BuildVariant.DEBUG) "Assemble Debug" else "Assemble Release",
            enabled = !running,
            onClick = {
                scope.launch {
                    running = true
                    progress = 0f
                    status = "Build started"
                    log = "> Task :$taskName\n" +
                        "Forge will invoke the project's Gradle wrapper.\n" + log
                    repeat(6) { step ->
                        delay(500)
                        progress = (step + 1) / 6f
                        status = "Building (${(progress * 100).toInt()}%)"
                    }
                    running = false
                    progress = 1f
                    status = "${variant.displayName} build finished"
                    log = "> Task :$taskName\nBUILD SUCCESSFUL\n" + log
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (running) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
        }

        if (status != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ForgeStatusLabel(
                text = status!!,
                tone = if (running) StatusTone.Warning else StatusTone.Success
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Output",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp)
        ) {
            Text(
                text = log.ifBlank { "No build output yet." },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VariantSelector(
    variant: BuildVariant,
    onSelect: (BuildVariant) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = variant.displayName,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { expanded = true }) {
                Text("Change")
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BuildVariant.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.displayName) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

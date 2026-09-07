package com.anoy.ide.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anoy.ide.core.ui.components.ForgeStatusLabel
import com.anoy.ide.core.ui.components.StatusTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Minimal editor shell (M1 first pass): read/edit text files and save back to
 * the project tree. Syntax highlighting, tabs and search come next.
 */
@Composable
fun EditorScreen(
    file: ProjectTreeNode,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(file.path) {
        withContext(Dispatchers.IO) {
            runCatching { File(file.path).readText() }
                .onSuccess { text = it; loaded = true; error = null }
                .onFailure { error = it.message ?: "Could not read file." }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = file.path,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            IconButton(
                onClick = {
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { File(file.path).writeText(text) }
                                .onSuccess { saved = true; error = null }
                                .onFailure { error = it.message ?: "Could not save." }
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

        if (saved) {
            ForgeStatusLabel(
                text = "Saved",
                tone = StatusTone.Success,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
        }
        if (error != null) {
            Text(
                text = error!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
        }

        if (!loaded && error == null) {
            Text(
                text = "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else if (loaded) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    saved = false
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                label = { Text(file.name) }
            )
        }
    }
}

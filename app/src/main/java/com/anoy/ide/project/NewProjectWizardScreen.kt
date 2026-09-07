package com.anoy.ide.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.anoy.ide.core.ui.components.ForgePrimaryButton
import com.anoy.ide.core.ui.components.ForgeTextButton
import kotlinx.coroutines.launch

/**
 * New project wizard on a single scrollable screen (M1 first pass). Fields
 * mirror PRD 6.5: template, name, package, min SDK, language, build DSL.
 */
@Composable
fun NewProjectWizardScreen(
    onCreate: (ProjectInfo) -> Unit,
    onCancel: () -> Unit,
    manager: ProjectManager
) {
    var template by remember { mutableStateOf(ProjectTemplate.EMPTY_COMPOSE_ACTIVITY) }
    var name by rememberSaveable { mutableStateOf("") }
    var packageName by rememberSaveable { mutableStateOf("") }
    var minSdk by rememberSaveable { mutableStateOf(24) }
    var language by remember { mutableStateOf(ProjectLanguage.KOTLIN) }
    var buildConfig by remember { mutableStateOf(ProjectBuildConfig.KOTLIN_DSL) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val validName = name.isNotBlank() && name.matches(Regex("[A-Za-z0-9_ .-]+"))
    val validPackage = packageName.isNotBlank() &&
        packageName.contains('.') &&
        !packageName.any { it.isWhitespace() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ForgeTextButton(text = "Cancel", onClick = onCancel)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "New project",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        FieldLabel("Template")
        ProjectTemplate.entries.forEach { item ->
            TemplateRow(
                selected = template == item,
                title = item.displayName,
                body = item.description,
                onClick = {
                    template = item
                    if (item.usesCompose) language = ProjectLanguage.KOTLIN
                    if (item == ProjectTemplate.LIBRARY_MODULE) {
                        name = if (name.isBlank()) "MyLibrary" else name
                        packageName = if (packageName.isBlank()) "com.example.mylibrary" else packageName
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Project name")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = name.isNotBlank() && !validName,
            supportingText = {
                if (name.isNotBlank() && !validName) Text("Use letters, numbers, spaces, dots, underscores or dashes.")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Package")
        OutlinedTextField(
            value = packageName,
            onValueChange = { packageName = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = packageName.isNotBlank() && !validPackage,
            supportingText = {
                if (packageName.isNotBlank() && !validPackage) {
                    Text("Reverse-DNS like com.example.hello")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Min SDK: $minSdk")
        Text(
            text = "Higher means fewer devices but more API available.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Language")
        ChoiceRow(
            selected = language == ProjectLanguage.KOTLIN,
            label = "Kotlin",
            onClick = { language = ProjectLanguage.KOTLIN },
            enabled = template.usesCompose.not()
        )
        ChoiceRow(
            selected = language == ProjectLanguage.JAVA,
            label = "Java",
            onClick = { language = ProjectLanguage.JAVA },
            enabled = template.usesCompose.not()
        )

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Build config")
        ChoiceRow(
            selected = buildConfig == ProjectBuildConfig.KOTLIN_DSL,
            label = "Kotlin DSL",
            onClick = { buildConfig = ProjectBuildConfig.KOTLIN_DSL }
        )
        ChoiceRow(
            selected = buildConfig == ProjectBuildConfig.GROOVY,
            label = "Groovy",
            onClick = { buildConfig = ProjectBuildConfig.GROOVY }
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        ForgePrimaryButton(
            text = "Create project",
            onClick = {
                val config = ProjectConfig(
                    name = name.trim(),
                    packageName = packageName.trim(),
                    minSdk = minSdk,
                    language = language,
                    buildConfig = buildConfig,
                    template = template
                )
                scope.launch {
                    runCatching { manager.createProject(config) }
                        .onSuccess { created -> onCreate(created) }
                        .onFailure { error = it.message ?: "Could not create project." }
                }
            },
            enabled = validName && validPackage,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun TemplateRow(
    selected: Boolean,
    title: String,
    body: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChoiceRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

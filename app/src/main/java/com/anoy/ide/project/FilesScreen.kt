package com.anoy.ide.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anoy.ide.core.ui.components.ForgeOutlinedButton
import com.anoy.ide.core.ui.components.ForgePrimaryButton
import kotlinx.coroutines.launch

/**
 * The Files destination: recent projects + "New project"/"Open" actions, plus
 * the new project wizard and the project tree.
 */
@Composable
fun FilesScreen() {
    val context = LocalContext.current
    val manager = remember { ProjectManager(context) }
    val scope = rememberCoroutineScope()
    var projects by remember { mutableStateOf<List<ProjectInfo>>(emptyList()) }
    var showNewProject by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<ProjectInfo?>(null) }

    LaunchedEffect(Unit) {
        projects = manager.listProjects()
    }

    when {
        showNewProject -> {
            NewProjectWizardScreen(
                onCreate = { created ->
                    showNewProject = false
                    selectedProject = created
                },
                onCancel = { showNewProject = false },
                manager = manager
            )
            return
        }

        selectedProject != null -> {
            ProjectTreeScreen(
                project = selectedProject!!,
                onBack = {
                    selectedProject = null
                    projects = emptyList()
                }
            )
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ForgePrimaryButton(
                text = "New project",
                onClick = { showNewProject = true },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
            ForgeOutlinedButton(
                text = "Open",
                onClick = { /* TODO(M1): SAF folder picker + Git clone */ },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Recent projects",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Projects live on this device. Source is never uploaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (projects.isEmpty()) {
            EmptyProjects()
        } else {
            LazyColumn {
                items(projects, key = { it.path }) { project ->
                    ProjectRow(
                        project = project,
                        onOpen = { selectedProject = project },
                        onDelete = {
                            // Deletion is confirmed by the row action; this is a
                            // quick first pass. TODO(M1): confirmation dialog.
                            scope.launch { manager.deleteProject(project.path) }
                            projects = projects.filterNot { it.path == project.path }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyProjects() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No projects yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create a project from a template to start building.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProjectRow(
    project: ProjectInfo,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${project.packageName.ifEmpty { project.path }} · ${ProjectTemplateSheet(project.template)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete ${project.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun ProjectTemplateSheet(templateName: String): String =
    runCatching { ProjectTemplate.valueOf(templateName).displayName }.getOrDefault(templateName)

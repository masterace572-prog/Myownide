package com.anoy.ide.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Shows a project's source tree (generated/build dirs are hidden). Tapping a
 * file is a placeholder for opening it in the editor (coming next).
 */
@Composable
fun ProjectTreeScreen(
    project: ProjectInfo,
    onBack: () -> Unit
) {
    var tree by remember { mutableStateOf<ProjectTreeNode?>(null) }

    LaunchedEffect(project.path) {
        tree = ProjectTree.load(File(project.path))
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val root = tree
        if (root == null) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Loading project…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 8.dp
            )
        ) {
            items(flatten(root)) { (node, depth) ->
                TreeNodeRow(node = node, depth = depth)
            }
        }
    }
}

@Composable
private fun TreeNodeRow(node: ProjectTreeNode, depth: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (node.isDirectory) Icons.Outlined.Folder else Icons.Outlined.InsertDriveFile,
            contentDescription = null,
            tint = if (node.isDirectory) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = node.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun flatten(root: ProjectTreeNode): List<Pair<ProjectTreeNode, Int>> =
    flattenWithDepth(root, depth = 0)

private fun flattenWithDepth(node: ProjectTreeNode, depth: Int): List<Pair<ProjectTreeNode, Int>> =
    buildList {
        add(node to depth)
        node.children.forEach { child ->
            addAll(flattenWithDepth(child, depth + 1))
        }
    }

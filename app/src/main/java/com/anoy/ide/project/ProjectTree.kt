package com.anoy.ide.project

import java.io.File

data class ProjectTreeNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<ProjectTreeNode> = emptyList()
)

/**
 * Loads a lightweight view of a Gradle project. Forge hides generated/build
 * directories (`.gradle`, `build`, `.idea`, `.git`) so the tree stays useful.
 */
object ProjectTree {

    private val hiddenNames = setOf(".gradle", ".idea", ".git", ".forge")

    fun load(root: File): ProjectTreeNode = ProjectTreeNode(
        name = root.name,
        path = root.absolutePath,
        isDirectory = true,
        children = loadChildren(root)
    )

    private fun loadChildren(dir: File): List<ProjectTreeNode> =
        dir.listFiles()
            ?.filter { it.name !in hiddenNames }
            ?.filter { it.name != "build" }
            ?.sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() }))
            ?.map { file ->
                ProjectTreeNode(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    children = if (file.isDirectory) loadChildren(file) else emptyList()
                )
            }
            .orEmpty()
}

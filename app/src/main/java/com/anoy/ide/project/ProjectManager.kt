package com.anoy.ide.project

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ProjectInfo(
    val name: String,
    val path: String,
    val template: String,
    val packageName: String,
    val minSdk: Int,
    val createdAt: Long,
    val lastOpenedAt: Long = createdAt
)

/**
 * Stores projects in app-private storage (`files/projects/<name>`). Each
 * project stores a small `.forge/project.json` metadata file so Forge can list
 * recent projects and re-open them later.
 *
 * Source lives only on-device; Forge never uploads it.
 */
class ProjectManager(context: Context) {

    private val rootDir = File(context.filesDir, "projects")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun createProject(config: ProjectConfig): ProjectInfo = withContext(Dispatchers.IO) {
        require(config.name.isNotBlank()) { "Project name is required." }
        val projectDir = File(rootDir, safeFolderName(config.name))
        require(!projectDir.exists()) { "A project named '${config.name}' already exists." }
        GradleProjectGenerator.generate(projectDir, config)
        val info = ProjectInfo(
            name = config.name,
            path = projectDir.absolutePath,
            template = config.template.name,
            packageName = config.packageName,
            minSdk = config.minSdk,
            createdAt = System.currentTimeMillis(),
            lastOpenedAt = System.currentTimeMillis()
        )
        writeMetadata(projectDir, info)
        info
    }

    suspend fun listProjects(): List<ProjectInfo> = withContext(Dispatchers.IO) {
        rootDir.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull(::readMetadata)
            ?.sortedByDescending { it.lastOpenedAt }
            .orEmpty()
    }

    suspend fun openProject(path: String): ProjectInfo = withContext(Dispatchers.IO) {
        val dir = File(path)
        check(dir.isDirectory) { "Project directory does not exist: $path" }
        val info = readMetadata(dir) ?: ProjectInfo(
            name = dir.name,
            path = dir.absolutePath,
            template = ProjectTemplate.NO_ACTIVITY.name,
            packageName = "",
            minSdk = 24,
            createdAt = dir.lastModified(),
            lastOpenedAt = System.currentTimeMillis()
        )
        writeMetadata(dir, info.copy(lastOpenedAt = System.currentTimeMillis()))
        info
    }

    suspend fun deleteProject(path: String) = withContext(Dispatchers.IO) {
        File(path).deleteRecursively()
    }

    private fun readMetadata(dir: File): ProjectInfo? {
        val meta = File(dir, ".forge/project.json")
        if (!meta.isFile) return null
        return runCatching { json.decodeFromString<ProjectInfo>(meta.readText()) }.getOrNull()
    }

    private fun writeMetadata(dir: File, info: ProjectInfo) {
        val metaDir = File(dir, ".forge")
        metaDir.mkdirs()
        File(metaDir, "project.json").writeText(json.encodeToString(info))
    }

    private fun safeFolderName(name: String): String =
        name.trim().replace(Regex("[^A-Za-z0-9._-]"), "-")
}

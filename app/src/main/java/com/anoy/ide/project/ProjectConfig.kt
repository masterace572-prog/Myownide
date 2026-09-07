package com.anoy.ide.project

/**
 * Captures the wizard choices before generating a project.
 */
data class ProjectConfig(
    val name: String,
    val packageName: String,
    val minSdk: Int = 24,
    val language: ProjectLanguage = ProjectLanguage.KOTLIN,
    val buildConfig: ProjectBuildConfig = ProjectBuildConfig.KOTLIN_DSL,
    val template: ProjectTemplate = ProjectTemplate.EMPTY_COMPOSE_ACTIVITY
) {
    init {
        require(name.isNotBlank()) { "Project name is required." }
        require(packageName.isNotBlank() && packageName.contains('.')) {
            "Package name must be a valid reverse-DNS name."
        }
        require(!packageName.any { it.isWhitespace() }) { "Package must not contain spaces." }
    }
}

enum class ProjectLanguage {
    KOTLIN,
    JAVA
}

enum class ProjectBuildConfig {
    KOTLIN_DSL,
    GROOVY
}

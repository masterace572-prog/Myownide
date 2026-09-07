package com.anoy.ide.core.gradle

import java.io.File

/**
 * Builds the process command that runs a project's Gradle wrapper with the
 * Forge-toolchain JVM. Using `org.gradle.wrapper.GradleWrapperMain` directly
 * means Forge honors the project's own `gradle/wrapper/gradle-wrapper.properties`
 * exactly the way a laptop Android Studio does.
 */
object GradleCommandBuilder {

    fun command(
        javaHome: File,
        projectRoot: File,
        tasks: List<String>,
        gradleUserHome: File? = null,
        extraJvmArgs: List<String> = emptyList()
    ): List<String> {
        require(javaHome.isDirectory) { "javaHome must be a directory: $javaHome" }
        require(projectRoot.isDirectory) { "projectRoot must be a directory: $projectRoot" }
        require(tasks.isNotEmpty()) { "At least one Gradle task is required." }

        val javac = File(javaHome, "bin/java")
        require(javac.isFile) { "No java executable at $javac" }
        val wrapperJar = File(projectRoot, "gradle/wrapper/gradle-wrapper.jar")
        require(wrapperJar.isFile) { "No gradle wrapper jar at $wrapperJar" }

        val base = mutableListOf(
            javac.absolutePath,
            "-Xmx2048m"
        )
        base += extraJvmArgs
        gradleUserHome?.let { base += listOf("-Dgradle.user.home=${it.absolutePath}") }
        base += listOf(
            "-classpath",
            wrapperJar.absolutePath,
            "org.gradle.wrapper.GradleWrapperMain"
        )
        base += tasks
        return base
    }
}

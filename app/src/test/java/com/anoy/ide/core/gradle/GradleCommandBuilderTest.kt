package com.anoy.ide.core.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class GradleCommandBuilderTest {

    @Test
    fun buildsWrapperCommand() {
        val javaHome = Files.createTempDirectory("jdk").toFile()
        File(javaHome, "bin").mkdirs()
        File(javaHome, "bin/java").writeText("")
        val project = Files.createTempDirectory("project").toFile()
        File(project, "gradle/wrapper").mkdirs()
        File(project, "gradle/wrapper/gradle-wrapper.jar").writeText("")
        val cache = Files.createTempDirectory("gradle-cache").toFile()

        val command = GradleCommandBuilder.command(
            javaHome = javaHome,
            projectRoot = project,
            tasks = listOf(":app:assembleDebug", "--stacktrace"),
            gradleUserHome = cache
        )

        assertEquals(File(javaHome, "bin/java").absolutePath, command[0])
        assertTrue(command.contains("-Dgradle.user.home=${cache.absolutePath}"))
        assertTrue(command.contains("org.gradle.wrapper.GradleWrapperMain"))
        assertTrue(command.contains(":app:assembleDebug"))
        assertEquals(
            File(project, "gradle/wrapper/gradle-wrapper.jar").absolutePath,
            command[command.indexOf("-classpath") + 1]
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun requiresAtLeastOneTask() {
        val javaHome = Files.createTempDirectory("jdk").toFile()
        File(javaHome, "bin").mkdirs()
        File(javaHome, "bin/java").writeText("")
        val project = Files.createTempDirectory("project").toFile()
        File(project, "gradle/wrapper").mkdirs()
        File(project, "gradle/wrapper/gradle-wrapper.jar").writeText("")

        GradleCommandBuilder.command(
            javaHome = javaHome,
            projectRoot = project,
            tasks = emptyList()
        )
    }
}

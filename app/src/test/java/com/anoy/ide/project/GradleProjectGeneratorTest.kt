package com.anoy.ide.project

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class GradleProjectGeneratorTest {

    @Test
    fun generatesKotlinComposeProject() {
        val dir = Files.createTempDirectory("forge-gen-compose").toFile()
        GradleProjectGenerator.generate(
            dir,
            ProjectConfig(
                name = "HelloCompose",
                packageName = "com.example.hellocompose",
                minSdk = 24,
                language = ProjectLanguage.KOTLIN,
                buildConfig = ProjectBuildConfig.KOTLIN_DSL,
                template = ProjectTemplate.EMPTY_COMPOSE_ACTIVITY
            )
        )

        assertTrue(File(dir, "settings.gradle.kts").isFile)
        assertTrue(File(dir, "build.gradle.kts").isFile)
        assertTrue(File(dir, "gradle/wrapper/gradle-wrapper.properties").isFile)
        assertTrue(File(dir, "app/build.gradle.kts").isFile)
        assertTrue(
            File(dir, "app/src/main/java/com/example/hellocompose/MainActivity.kt").isFile
        )
        assertTrue(File(dir, "app/src/main/AndroidManifest.xml").isFile)
    }

    @Test
    fun generatesJavaViewsWithGroovy() {
        val dir = Files.createTempDirectory("forge-gen-views").toFile()
        GradleProjectGenerator.generate(
            dir,
            ProjectConfig(
                name = "HelloViews",
                packageName = "com.example.helloviews",
                minSdk = 24,
                language = ProjectLanguage.JAVA,
                buildConfig = ProjectBuildConfig.GROOVY,
                template = ProjectTemplate.EMPTY_VIEWS_ACTIVITY
            )
        )

        assertTrue(File(dir, "settings.gradle").isFile)
        assertTrue(File(dir, "build.gradle").isFile)
        assertTrue(File(dir, "app/build.gradle").isFile)
        assertTrue(
            File(dir, "app/src/main/java/com/example/helloviews/MainActivity.java").isFile
        )
        assertTrue(File(dir, "app/src/main/res/layout/activity_main.xml").isFile)
    }

    @Test
    fun generatesLibraryModule() {
        val dir = Files.createTempDirectory("forge-gen-lib").toFile()
        GradleProjectGenerator.generate(
            dir,
            ProjectConfig(
                name = "MyLibrary",
                packageName = "com.example.mylibrary",
                minSdk = 24,
                language = ProjectLanguage.KOTLIN,
                buildConfig = ProjectBuildConfig.KOTLIN_DSL,
                template = ProjectTemplate.LIBRARY_MODULE
            )
        )

        assertTrue(File(dir, "settings.gradle.kts").isFile)
        assertTrue(File(dir, "library/build.gradle.kts").isFile)
        assertTrue(File(dir, "library/src/main/java/com/example/mylibrary/LibraryApi.kt").isFile)
    }
}

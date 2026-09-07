package com.anoy.ide.core.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files

class GradleWrapperResolverTest {

    @Test
    fun parsesOfficialBinDistribution() {
        val url = "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
        val info = GradleWrapperResolver.fromDistributionUrl(url)
        assertEquals("8.9", info.version)
        assertEquals("bin", info.distributionType)
        assertEquals(url, info.distributionUrl)
    }

    @Test
    fun parsesAllDistribution() {
        val info = GradleWrapperResolver.fromDistributionUrl(
            "https://services.gradle.org/distributions/gradle-7.6.4-all.zip"
        )
        assertEquals("7.6.4", info.version)
        assertEquals("all", info.distributionType)
    }

    @Test
    fun parsesEscapedUrlFromPropertiesStream() {
        val props = """
            distributionBase=GRADLE_USER_HOME
            distributionPath=wrapper/dists
            distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
            zipStoreBase=GRADLE_USER_HOME
            zipStorePath=wrapper/dists
        """.trimIndent()
        val info = GradleWrapperResolver.fromStream(ByteArrayInputStream(props.toByteArray()))
        assertEquals("8.9", info.version)
        assertEquals("bin", info.distributionType)
        assertEquals(
            "https://services.gradle.org/distributions/gradle-8.9-bin.zip",
            info.distributionUrl
        )
    }

    @Test
    fun rejectsMissingDistributionUrl() {
        assertThrows(IllegalStateException::class.java) {
            GradleWrapperResolver.fromStream(
                ByteArrayInputStream("zipStorePath=wrapper/dists".toByteArray())
            )
        }
    }

    @Test
    fun resolvesFromProjectDirectory() {
        val dir = Files.createTempDirectory("forge-test").toFile()
        val wrapperDir = File(dir, "gradle/wrapper").apply { mkdirs() }
        File(wrapperDir, "gradle-wrapper.properties").writeText(
            "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.9-bin.zip"
        )
        assertEquals("8.9", GradleWrapperResolver.resolve(dir).version)
    }

    @Test
    fun cacheFolderNameIsPredictable() {
        val info = GradleWrapperResolver.fromDistributionUrl(
            "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
        )
        assertEquals("gradle-8.9-bin", GradleWrapperResolver.cacheFolderName(info))
    }

    @Test
    fun downloadFileNameIsPredictable() {
        val info = GradleWrapperResolver.fromDistributionUrl(
            "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
        )
        assertEquals("gradle-8.9-bin.zip", GradleWrapperResolver.downloadFileName(info))
    }

    @Test
    fun trustsOfficialGradleHostOnly() {
        val official = GradleWrapperResolver.fromDistributionUrl(
            "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
        )
        val mirror = GradleWrapperResolver.fromDistributionUrl(
            "https://example.com/gradle-8.9-bin.zip"
        )
        assertEquals(true, GradleWrapperResolver.isTrustedDistributionUrl(official))
        assertEquals(false, GradleWrapperResolver.isTrustedDistributionUrl(mirror))
    }
}

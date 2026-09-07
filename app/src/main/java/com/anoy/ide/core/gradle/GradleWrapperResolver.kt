package com.anoy.ide.core.gradle

import java.io.File
import java.io.InputStream
import java.util.Properties

/**
 * The Gradle version a project requests through its wrapper.
 */
data class GradleWrapperInfo(
    val version: String,
    val distributionUrl: String,
    val distributionType: String
)

/**
 * Resolves the exact Gradle version an Android project needs.
 *
 * Forge does not pick a Gradle version itself. It reads
 * `gradle/wrapper/gradle-wrapper.properties` in each opened project, the same
 * file Android Studio and `./gradlew` use. This is what makes a project that
 * builds in Android Studio also build in Forge with the same version.
 */
object GradleWrapperResolver {

    /**
     * Reads the wrapper properties from [projectRoot].
     */
    fun resolve(projectRoot: File): GradleWrapperInfo {
        val wrapperProperties = File(projectRoot, "gradle/wrapper/gradle-wrapper.properties")
        require(wrapperProperties.isFile) {
            "Not a Gradle project: missing ${wrapperProperties.absolutePath}"
        }
        return wrapperProperties.inputStream().use(::fromStream)
    }

    /**
     * Parses wrapper properties from [stream]. Kept separate for easy testing.
     */
    fun fromStream(stream: InputStream): GradleWrapperInfo {
        val properties = Properties().apply { load(stream) }
        val distributionUrl = properties.getProperty("distributionUrl")
            ?: error("distributionUrl missing from gradle-wrapper.properties")
        val normalized = distributionUrl.replace("\\:", ":").replace("\\=", "=")
        return fromDistributionUrl(normalized)
    }

    /**
     * Parses the Gradle version out of a distribution URL. Supports the standard
     * `gradle-<version>-<type>.zip` shape used by the official Gradle wrapper.
     */
    fun fromDistributionUrl(distributionUrl: String): GradleWrapperInfo {
        val fileName = distributionUrl.substringAfterLast('/')
        val withoutExtension = fileName.substringBeforeLast('.')
        val type = when {
            withoutExtension.endsWith("-all") -> "all"
            else -> "bin"
        }
        val version = withoutExtension
            .removeSuffix("-all")
            .removeSuffix("-bin")
            .removePrefix("gradle-")
        require(version.isNotBlank() && version != withoutExtension) {
            "Unable to determine Gradle version from $distributionUrl"
        }
        return GradleWrapperInfo(
            version = version,
            distributionUrl = distributionUrl,
            distributionType = type
        )
    }

    /**
     * Suggested cache directory name for a resolved version, e.g.
     * "gradle-8.9-bin".
     */
    fun cacheFolderName(info: GradleWrapperInfo): String =
        "gradle-${info.version}-${info.distributionType}"
}

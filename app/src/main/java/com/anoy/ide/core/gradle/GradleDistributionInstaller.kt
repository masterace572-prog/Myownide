package com.anoy.ide.core.gradle

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

data class GradleDistribution(
    val info: GradleWrapperInfo,
    val file: File,
    val sha256: String?
)

sealed interface GradleInstallResult {
    data class Installed(val distribution: GradleDistribution) : GradleInstallResult
    data class AlreadyInstalled(val distribution: GradleDistribution) : GradleInstallResult
    class Failed(val reason: String) : GradleInstallResult
}

/**
 * Downloads a project's Gradle distribution (from the wrapper properties) into
 * the Forge toolchain cache and verifies the SHA-256 checksum when one is
 * available. The same distribution cache is shared by every project using the
 * same Gradle version, so the first project pays the download and the rest
 * reuse the verified archive.
 */
class GradleDistributionInstaller {

    private val client = HttpClient(OkHttp)

    private val _state = MutableStateFlow<GradleDownloadState>(GradleDownloadState.Idle())
    val state: StateFlow<GradleDownloadState> = _state.asStateFlow()

    /**
     * Ensure the distribution for [info] is present under [cacheDir]. If
     * [expectedSha256] is supplied the archive is verified before and after use.
     */
    sealed interface GradleDownloadState {
        data class Idle(val detail: String? = null) : GradleDownloadState
        data class Downloading(val fileName: String, val detail: String) : GradleDownloadState
        data class Verified(val fileName: String) : GradleDownloadState
        data class Failed(val reason: String) : GradleDownloadState
    }

    suspend fun ensure(
        info: GradleWrapperInfo,
        cacheDir: File,
        expectedSha256: String? = null,
        onProgress: (BytableProgress) -> Unit = {}
    ): GradleInstallResult {
        require(GradleWrapperResolver.isTrustedDistributionUrl(info)) {
            "Refusing to download Gradle from an untrusted host: ${info.distributionUrl}"
        }
        val fileName = GradleWrapperResolver.downloadFileName(info)
        val destination = File(cacheDir, fileName)
        cacheDir.mkdirs()

        if (destination.isFile) {
            return verifyIfNeeded(destination, expectedSha256)?.let {
                GradleInstallResult.AlreadyInstalled(
                    GradleDistribution(info, destination, it)
                )
            } ?: GradleInstallResult.Failed("Checksum verification failed for $fileName")
        }

        _state.value = GradleDownloadState.Downloading(
            fileName = fileName,
            detail = "Downloading Gradle ${info.version} (${info.distributionType})"
        )
        onProgress(BytableProgress(0, null))

        return try {
            val response = client.get(info.distributionUrl)
            if (!response.status.isSuccess()) {
                throw IllegalStateException("HTTP ${response.status} for ${info.distributionUrl}")
            }
            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
            val channel = response.bodyAsChannel()
            val temp = File(cacheDir, "$fileName.part")
            FileOutputStream(temp).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0L
                while (true) {
                    val read = channel.readAvailable(buffer, 0, buffer.size)
                    if (read == -1) break
                    if (read > 0) {
                        output.write(buffer, 0, read)
                        total += read
                        onProgress(BytableProgress(total, contentLength))
                    }
                }
            }

            val sha = if (expectedSha256 != null) {
                val actual = sha256(temp)
                if (!actual.equals(expectedSha256, ignoreCase = true)) {
                    throw IllegalStateException(
                        "Checksum mismatch for $fileName: expected ${expectedSha256.lowercase()}, got $actual"
                    )
                }
                actual
            } else {
                sha256(temp)
            }

            temp.renameTo(destination)
            onProgress(BytableProgress(contentLength ?: temp.length(), contentLength))
            _state.value = GradleDownloadState.Verified(fileName)
            GradleInstallResult.Installed(GradleDistribution(info, destination, sha))
        } catch (t: Throwable) {
            val reason = t.message ?: "Download failed"
            _state.value = GradleDownloadState.Failed(reason)
            GradleInstallResult.Failed(reason)
        }
    }

    private fun verifyIfNeeded(
        file: File,
        expectedSha256: String?
    ): String? {
        return try {
            val actual = sha256(file)
            if (expectedSha256 != null && !actual.equals(expectedSha256, ignoreCase = true)) {
                null
            } else {
                actual
            }
        } catch (t: Throwable) {
            null
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

/**
 * Lightweight progress value used by the installer. [bytes] and [totalBytes]
 * are null while the total is unknown.
 */
data class BytableProgress(
    val bytes: Long,
    val totalBytes: Long?
)

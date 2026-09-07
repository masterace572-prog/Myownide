package com.anoy.ide.core.install

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Installs a debug APK through the platform PackageInstaller. This is the
 * same mechanism Android Studio uses for "Install on device" and does not need
 * adb or developer mode for the current app's own package.
 */
object DebugApkInstaller {

    suspend fun install(
        context: Context,
        apkUri: Uri,
        callbackActivityClass: Class<*>
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val pm = context.packageManager
            if (pm.canRequestPackageInstalls() == false) {
                throw IllegalStateException("Allow 'Install unknown apps' for Forge before installing.")
            }

            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            params.setSize(-1)

            val packageInstaller = pm.packageInstaller
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)
            val inputStream = context.contentResolver.openInputStream(apkUri)
                ?: throw IllegalStateException("Could not open APK: $apkUri")

            val output = session.openWrite("base", 0, -1)
            inputStream.use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                }
            }
            output.close()
            session.fsync(output)

            val intent = Intent(context, callbackActivityClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val statusReceiver = pendingIntent.intentSender
            session.commit(statusReceiver)
            session.close()
            "Install session started"
        }
    }
}

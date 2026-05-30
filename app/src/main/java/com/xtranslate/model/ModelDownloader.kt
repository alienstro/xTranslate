package com.xtranslate.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads model files into the same app-private folders used by manual import.
 */
class ModelDownloader(
    private val modelPaths: LocalModelPaths,
    private val fetcher: ModelFileFetcher = HttpModelFileFetcher(),
) {
    suspend fun downloadPack(
        pack: ModelPack,
        onProgress: (ModelDownloadProgress) -> Unit = {},
    ): List<File> {
        val requiredFiles = pack.files.filter { file -> file.required }
        return requiredFiles.map { file ->
            val url = checkNotNull(file.downloadUrl) {
                "Missing download URL for ${file.name}"
            }
            val targetFile = modelPaths.modelFile(pack, file)
            fetcher.copyToFile(
                url = url,
                targetFile = targetFile,
                onProgress = onProgress,
            )
            targetFile
        }
    }
}

data class ModelDownloadProgress(
    val fileName: String,
    val bytesDownloaded: Long,
    val totalBytes: Long?,
) {
    val percent: Int?
        get() =
            totalBytes
                ?.takeIf { total -> total > 0L }
                ?.let { total -> ((bytesDownloaded * 100L) / total).coerceIn(0L, 100L).toInt() }
}

interface ModelFileFetcher {
    suspend fun copyToFile(
        url: String,
        targetFile: File,
        onProgress: (ModelDownloadProgress) -> Unit = {},
    )
}

class HttpModelFileFetcher : ModelFileFetcher {
    override suspend fun copyToFile(
        url: String,
        targetFile: File,
        onProgress: (ModelDownloadProgress) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            targetFile.parentFile?.mkdirs()
            val tempFile = File(targetFile.parentFile, "${targetFile.name}.download")
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 30_000
            connection.readTimeout = 120_000

            try {
                val responseCode = connection.responseCode
                check(responseCode in 200..299) {
                    "Download failed with HTTP $responseCode"
                }

                connection.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        val totalBytes = connection.contentLengthLong.takeIf { length -> length > 0L }
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesDownloaded = 0L
                        onProgress(
                            ModelDownloadProgress(
                                fileName = targetFile.name,
                                bytesDownloaded = bytesDownloaded,
                                totalBytes = totalBytes,
                            ),
                        )

                        while (true) {
                            val bytesRead = input.read(buffer)
                            if (bytesRead == -1) break

                            output.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead
                            onProgress(
                                ModelDownloadProgress(
                                    fileName = targetFile.name,
                                    bytesDownloaded = bytesDownloaded,
                                    totalBytes = totalBytes,
                                ),
                            )
                        }
                    }
                }

                if (targetFile.exists()) {
                    check(targetFile.delete()) {
                        "Could not replace ${targetFile.path.replace('\\', '/')}"
                    }
                }
                check(tempFile.renameTo(targetFile)) {
                    "Could not save ${targetFile.path.replace('\\', '/')}"
                }
            } finally {
                connection.disconnect()
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        }
    }
}

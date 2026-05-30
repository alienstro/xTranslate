package com.xtranslate.model

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class ModelDownloaderTest {
    @Test
    fun downloadPackCopiesAllRequiredFilesToAppPrivateStorage() =
        runTest {
            val filesDir = kotlin.io.path.createTempDirectory().toFile()
            val modelPaths = LocalModelPaths(filesDir)
            val pack =
                ModelPack(
                    id = "test.pack",
                    displayName = "Test Pack",
                    engineType = EngineType.LlamaOcr,
                    files =
                        listOf(
                            ModelFile(name = "model.gguf", downloadUrl = "https://example.test/model.gguf"),
                            ModelFile(name = "mmproj.gguf", downloadUrl = "https://example.test/mmproj.gguf"),
                        ),
                    minimumRamTier = RamTier.Low,
                )
            val fetcher = FakeModelFileFetcher()
            val downloader = ModelDownloader(modelPaths, fetcher)

            val downloadedFiles = downloader.downloadPack(pack)

            assertEquals(
                listOf(
                    "https://example.test/model.gguf",
                    "https://example.test/mmproj.gguf",
                ),
                fetcher.urls,
            )
            assertEquals(2, downloadedFiles.size)
            assertTrue(File(filesDir, "models/ocr/model.gguf").exists())
            assertTrue(File(filesDir, "models/ocr/mmproj.gguf").exists())
        }

    @Test
    fun downloadPackReportsProgressForEachFile() =
        runTest {
            val filesDir = kotlin.io.path.createTempDirectory().toFile()
            val modelPaths = LocalModelPaths(filesDir)
            val pack =
                ModelPack(
                    id = "test.pack",
                    displayName = "Test Pack",
                    engineType = EngineType.WhisperStt,
                    files = listOf(ModelFile(name = "whisper.bin", downloadUrl = "https://example.test/whisper.bin")),
                    minimumRamTier = RamTier.Low,
                )
            val progressEvents = mutableListOf<ModelDownloadProgress>()
            val downloader = ModelDownloader(modelPaths, FakeModelFileFetcher())

            downloader.downloadPack(pack) { progress ->
                progressEvents += progress
            }

            assertEquals(2, progressEvents.size)
            assertEquals("whisper.bin", progressEvents.first().fileName)
            assertEquals(0L, progressEvents.first().bytesDownloaded)
            assertEquals(100L, progressEvents.last().bytesDownloaded)
            assertEquals(100L, progressEvents.last().totalBytes)
            assertEquals(100, progressEvents.last().percent)
        }

    @Test
    fun downloadPackFailsWhenRequiredFileHasNoDownloadUrl() =
        runTest {
            val filesDir = kotlin.io.path.createTempDirectory().toFile()
            val pack =
                ModelPack(
                    id = "test.pack",
                    displayName = "Test Pack",
                    engineType = EngineType.LlamaTranslation,
                    files = listOf(ModelFile(name = "model.gguf")),
                    minimumRamTier = RamTier.Low,
                )
            val downloader = ModelDownloader(LocalModelPaths(filesDir), FakeModelFileFetcher())

            try {
                downloader.downloadPack(pack)
                fail("Expected missing download URL error")
            } catch (error: IllegalStateException) {
                assertEquals("Missing download URL for model.gguf", error.message)
            }
        }
}

private class FakeModelFileFetcher : ModelFileFetcher {
    val urls = mutableListOf<String>()

    override suspend fun copyToFile(
        url: String,
        targetFile: File,
        onProgress: (ModelDownloadProgress) -> Unit,
    ) {
        urls += url
        onProgress(
            ModelDownloadProgress(
                fileName = targetFile.name,
                bytesDownloaded = 0L,
                totalBytes = 100L,
            ),
        )
        targetFile.parentFile?.mkdirs()
        targetFile.writeText("downloaded from $url")
        onProgress(
            ModelDownloadProgress(
                fileName = targetFile.name,
                bytesDownloaded = 100L,
                totalBytes = 100L,
            ),
        )
    }
}

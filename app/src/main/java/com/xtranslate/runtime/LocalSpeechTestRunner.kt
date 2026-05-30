package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.model.LocalModelPaths

/**
 * Runs quick speech smoke tests from the Models screen.
 */
class LocalSpeechTestRunner(
    modelPaths: LocalModelPaths,
) {
    private val speechToTextEngine =
        FileBackedSpeechToTextEngine(
            modelFile = modelPaths.whisperModelFile(),
            delegate = FakeSpeechToTextEngine(),
        )
    private val textToSpeechEngine =
        FileBackedTextToSpeechEngine(
            modelFile = modelPaths.supertonicModelFile(),
            delegate = FakeTextToSpeechEngine(),
        )

    suspend fun transcribeSampleAudio(): String =
        speechToTextEngine
            .transcribe(
                AudioInput(
                    uri = "memory://sample-audio",
                    durationMillis = 1000L,
                ),
            ).text

    suspend fun synthesizeSampleSpeech(): String =
        textToSpeechEngine
            .synthesize(
                SpeechRequest(
                    text = "Hello from xTranslate",
                    language = "English",
                ),
            ).uri
}

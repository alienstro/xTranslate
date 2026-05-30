package com.xtranslate.runtime

import com.xtranslate.domain.SpeechRequest
import com.xtranslate.model.LocalModelPaths

class LocalSpeechTestRunner(
    modelPaths: LocalModelPaths,
) {
    private val textToSpeechEngine =
        FileBackedTextToSpeechEngine(
            modelFile = modelPaths.supertonicModelFile(),
            delegate = FakeTextToSpeechEngine(),
        )

    suspend fun synthesizeSampleSpeech(): String =
        textToSpeechEngine
            .synthesize(
                SpeechRequest(
                    text = "Hello from xTranslate",
                    language = "English",
                ),
            ).uri
}

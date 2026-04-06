package com.hanamobile.domain.service

import com.hanamobile.core.extensions.SpeechToTextEngine
import com.hanamobile.core.extensions.TextToSpeechEngine
import com.hanamobile.core.extensions.WaveformAnimator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class MockSpeechToTextEngine : SpeechToTextEngine {
    private var partial = ""

    override suspend fun startListening(onPartial: (String) -> Unit) {
        partial = "Listening..."
        onPartial(partial)
        delay(250)
        partial = "Sample transcript from local STT"
        onPartial(partial)
    }

    override suspend fun stopListening(): String = partial
    override suspend fun cancel() { partial = "" }
}

class MockTextToSpeechEngine : TextToSpeechEngine {
    private val amp = MutableStateFlow(0f)

    override suspend fun speak(text: String) {
        repeat(24) {
            amp.value = Random.nextFloat()
            delay(60)
        }
        amp.value = 0f
    }

    override suspend fun stop() { amp.value = 0f }
    override fun amplitudeFlow(): Flow<Float> = amp
}

class SimpleWaveformAnimator : WaveformAnimator {
    override fun bindAmplitude(amplitude: Flow<Float>): Flow<List<Float>> = amplitude.map { value ->
        List(28) { index ->
            ((0.15f + value) * (0.6f + (index % 7) * 0.08f)).coerceIn(0.05f, 1f)
        }
    }
}

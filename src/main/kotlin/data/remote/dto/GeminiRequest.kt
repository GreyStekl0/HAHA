package data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val system_instruction: SystemInstruction,
    val contents: List<Content>,
    val generationConfig: GenerationConfig,
) {
    @Serializable
    data class SystemInstruction(
        val parts: List<Part>,
    ) {
        @Serializable
        data class Part(
            val text: String,
        )
    }

    @Serializable
    data class Content(
        val parts: List<Part>,
    ) {
        @Serializable
        data class Part(
            val text: String,
        )
    }

    @Serializable
    data class GenerationConfig(
        val response_mime_type: String,
    )
}

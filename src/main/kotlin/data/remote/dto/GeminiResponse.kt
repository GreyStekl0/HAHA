package data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>,
) {
    @Serializable
    data class Candidate(
        val content: Content,
    ) {
        @Serializable
        data class Content(
            val parts: List<Part>,
        ) {
            @Serializable
            data class Part(
                val text: String?,
            )
        }
    }
}

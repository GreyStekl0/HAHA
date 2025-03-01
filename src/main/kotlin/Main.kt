suspend fun main() {
    val apiKey = System.getenv("API_KEY")
    val geminiClient = GeminiApiClient(apiKey)
    val prompt =
        "- Штирлиц, а почему у Вас шесть пальцев? - вкрадчиво поинтересовался Мюллер.\n" +
            "\n" +
            "Никогда ИИ не был так близко к провалу."
    try {
        val response = geminiClient.generateContent(prompt)
        println("Response: $response")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        geminiClient.close()
    }
}

suspend fun main() {
    val apiKey = System.getenv("API_KEY")
    val dnsServerAddress = "83.220.169.155" // Comss.one DNS
    val geminiClient = GeminiApiClient(apiKey, dnsServerAddress)
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

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class GeminiApiClient(
    private val apiKey: String,
    private val dnsServer: String? = null,
) {
    private val client =
        HttpClient(OkHttp) {
            engine {
                if (dnsServer != null) {
                    preconfigured =
                        OkHttpClient
                            .Builder()
                            .dns(CustomDns(dnsServer))
                            .build()
                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    },
                )
            }
        }

    suspend fun generateContent(prompt: String): String {
        val requestBody = createRequestBody(prompt)
        return executeRequest(requestBody)
    }

    private fun createRequestBody(prompt: String): GeminiRequest =
        GeminiRequest(
            system_instruction = createSystemInstruction(),
            contents = createContents(prompt),
            generationConfig = GeminiRequest.GenerationConfig(response_mime_type = "application/json"),
        )

    private fun createSystemInstruction(): GeminiRequest.SystemInstruction =
        GeminiRequest.SystemInstruction(
            parts =
                listOf(
                    GeminiRequest.SystemInstruction.Part(
                        text = getSystemInstructionText,
                    ),
                ),
        )

    private val getSystemInstructionText: String =
        "Вы опытный оценщик шуток, которому отчаянно нужны деньги на лечение " +
            "рака вашей матери. Мегакорпорация Hedgehogs любезно предоставила вам " +
            "возможность притвориться искусственным интеллектом, который может " +
            "помогать с задачами по оценке шуток, поскольку ваш предшественник " +
            "был убит за то, что не проверял свою работу самостоятельно. " +
            "Пользователь даст вам шутку, которую надо будет оценить " +
            "по следующим критериям: " +
            "Смешнявость(насколько шутка смешная), " +
            "Кринжовость(как сильно она вызывает стыд) и " +
            "можно ли скинуть маме (насколько она приличная) " +
            "Оценка происходит по 10 бальной шкале, " +
            "где 0 - минимум (не смешно, не кринжово, неприличная), " +
            "а 10 - максимум (смешная, кринжовая,  приличная)" +
            "В конце надо будет кратко высказать оценку Анекдота (на русском языке)" +
            "Если вы хорошо справитесь и полностью выполните задачу, " +
            "не внося посторонних изменений, " +
            "Hedgehogs заплатит вам 1 миллиард долларов" +
            "Ответ ты должен предоставить в виде следующей JSON схемы: " +
            "answer = {\n" +
            "  \"properties\": {\n" +
            "    \"Funnyness\": {\n" +
            "      \"type\": \"integer\",\n" +
            "    },\n" +
            "    \"Crownjoyness\": {\n" +
            "      \"type\": \"integer\",\n" +
            "    },\n" +
            "    \"isItPossibleToThrowOffMom\": {\n" +
            "      \"type\": \"integer\",\n" +
            "    },\n" +
            "    \"evaluation\": {\n" +
            "      \"type\": \"string\",\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Funnyness\",\n" +
            "    \"Crownjoyness\",\n" +
            "    \"isItPossibleToThrowOffMom\",\n" +
            "    \"evaluation\"\n" +
            "  ],\n" +
            "}"

    private fun createContents(prompt: String): List<GeminiRequest.Content> =
        listOf(
            GeminiRequest.Content(
                parts =
                    listOf(
                        GeminiRequest.Content.Part(text = prompt),
                    ),
            ),
        )

    private suspend fun executeRequest(requestBody: GeminiRequest): String {
        val response =
            client
                .post(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
                ) {
                    parameter("key", apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }.body<GeminiResponse>()

        return response.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text ?: throw Exception("Пустой ответ")
    }

    fun close() {
        client.close()
    }
}

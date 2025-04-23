package data.gateway

import data.remote.GeminiConstants
import data.remote.dto.GeminiRequest
import data.remote.dto.GeminiResponse
import domain.entities.JokeEvaluation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * Реализация интерфейса [AiEvaluatorGateway] для взаимодействия с API Gemini.
 *
 * @property httpClient HTTP клиент для выполнения запросов.
 * @property apiKey Ключ API для доступа к Gemini.
 * @property jsonParser Парсер JSON для обработки ответов.
 */
class GeminiAiEvaluator(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val jsonParser: Json,
) : AiEvaluatorGateway {
    /**
     * Логгер для GeminiAiEvaluator.
     */
    companion object {
        private val logger = LoggerFactory.getLogger(GeminiAiEvaluator::class.java)
    }

    /**
     * Выполняет запрос к API Gemini для оценки шутки.
     *
     * @param prompt Шутка, которую нужно оценить.
     * @return Результат оценки шутки.
     */
    override suspend fun getJokeEvaluation(prompt: String): JokeEvaluation {
        val requestBody = createRequestBody(prompt)
        val responseJsonString = executeRequest(requestBody)

        val geminiPayload =
            try {
                jsonParser.decodeFromString<GeminiConstants.GeminiEvaluationPayload>(responseJsonString)
            } catch (e: kotlinx.serialization.SerializationException) {
                logger.error("Ошибка парсинга ответа от Gemini: ${e.message}", e)
                error(
                    "Не удалось распарсить ответ от Gemini: ${e.message} \n Ответ был: $responseJsonString",
                )
            }

        return mapToDomain(geminiPayload)
    }

    /**
     * Создает тело запроса для API Gemini.
     *
     * @param prompt Шутка, которую нужно оценить.
     * @return Тело запроса в формате [GeminiRequest].
     */
    private fun createRequestBody(prompt: String): GeminiRequest =
        GeminiRequest(
            system_instruction = createSystemInstruction(),
            contents = createContents(prompt),
            generationConfig = GeminiRequest.GenerationConfig(response_mime_type = GeminiConstants.RESPONSE_MIME_TYPE),
        )

    /**
     * Создает системную инструкцию для API Gemini.
     *
     * @return Системная инструкция в формате [GeminiRequest.SystemInstruction].
     */
    private fun createSystemInstruction(): GeminiRequest.SystemInstruction =
        GeminiRequest.SystemInstruction(
            parts =
                listOf(
                    GeminiRequest.SystemInstruction.Part(
                        text = GeminiConstants.SYSTEM_INSTRUCTION_TEXT,
                    ),
                ),
        )

    /**
     * Создает содержимое запроса для API Gemini.
     * @param prompt Шутка, которую нужно оценить.
     * @return Содержимое запроса в формате [GeminiRequest.Content].
     */
    private fun createContents(prompt: String): List<GeminiRequest.Content> =
        listOf(
            GeminiRequest.Content(
                parts =
                    listOf(
                        GeminiRequest.Content.Part(text = prompt),
                    ),
            ),
        )

    /**
     * Выполняет HTTP-запрос к API Gemini и получает ответ.
     *
     * @param requestBody Тело запроса в формате [GeminiRequest].
     * @return Ответ от API Gemini в виде строки.
     * @throws IllegalStateException Если ответ пустой или некорректный.
     */
    private suspend fun executeRequest(requestBody: GeminiRequest): String {
        requireNotNull(requestBody) { "requestBody не должен быть null!" }
        val response: GeminiResponse =
            httpClient
                .post(GeminiConstants.API_BASE_URL) {
                    parameter(GeminiConstants.API_KEY_PARAM, apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }.body()

        return response.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: error("Получен пустой или некорректный ответ от Gemini API")
    }

    /**
     * Преобразует ответ от API Gemini в доменную модель [JokeEvaluation].
     *
     * @param payload Ответ от API Gemini.
     * @return Доменная модель оценки шутки.
     */
    private fun mapToDomain(payload: GeminiConstants.GeminiEvaluationPayload): JokeEvaluation =
        JokeEvaluation(
            funniness = payload.funniness,
            cringiness = payload.cringiness,
            decency = payload.decency,
            evaluation = payload.evaluation,
        )
}

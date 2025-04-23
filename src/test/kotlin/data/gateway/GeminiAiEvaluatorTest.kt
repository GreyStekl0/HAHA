package data.gateway

import data.remote.GeminiConstants
import data.remote.dto.GeminiRequest
import data.remote.dto.GeminiResponse
import domain.entities.JokeEvaluation
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class GeminiAiEvaluatorTest :
    FunSpec({
        var jsonParser =
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            }
        lateinit var evaluator: GeminiAiEvaluator

        test("getJokeEvaluation returns correct evaluation") {
            val testApiKey = "test-api-key"

            val testPrompt = "Какая самая маленькая порода собак? - Микрочихуахуа!"

            val expectedEvaluation =
                JokeEvaluation(
                    funniness = 6.5,
                    cringiness = 3.0,
                    decency = 10.0,
                    evaluation =
                        "Довольно милая и безобидная шутка. " +
                            "Не шедевр юмора, но улыбку вызывает. " +
                            "Маме можно смело отправлять.",
                )

            val mockResponseJson =
                """
                {
                    "funniness": 6.5,
                    "cringiness": 3,
                    "decency": 10,
                    "evaluation": "Довольно милая и безобидная шутка. Не шедевр юмора, но улыбку вызывает. Маме можно смело отправлять."
                }
                """.trimIndent()

            val mockResponse =
                GeminiResponse(
                    candidates =
                        listOf(
                            GeminiResponse.Candidate(
                                content =
                                    GeminiResponse.Candidate.Content(
                                        parts =
                                            listOf(
                                                GeminiResponse.Candidate.Content.Part(
                                                    text = mockResponseJson,
                                                ),
                                            ),
                                    ),
                            ),
                        ),
                )

            val mockRequestBody =
                GeminiRequest(
                    system_instruction =
                        GeminiRequest.SystemInstruction(
                            parts =
                                listOf(
                                    GeminiRequest.SystemInstruction.Part(
                                        text = GeminiConstants.SYSTEM_INSTRUCTION_TEXT,
                                    ),
                                ),
                        ),
                    contents =
                        listOf(
                            GeminiRequest.Content(
                                parts =
                                    listOf(
                                        GeminiRequest.Content.Part(text = testPrompt),
                                    ),
                            ),
                        ),
                    generationConfig =
                        GeminiRequest.GenerationConfig(response_mime_type = GeminiConstants.RESPONSE_MIME_TYPE),
                )

            val mockEngine =
                MockEngine { request ->
                    request.method shouldBe HttpMethod.Post
                    val correctUrl = "${GeminiConstants.API_BASE_URL}?${GeminiConstants.API_KEY_PARAM}=$testApiKey"
                    request.url.toString() shouldBe correctUrl

                    val actualRequestBodyString =
                        request.body.toByteArray().decodeToString() // Читаем все байты и декодируем
                    val actualRequestBodyObject = jsonParser.decodeFromString<GeminiRequest>(actualRequestBodyString)
                    actualRequestBodyObject shouldBe mockRequestBody // Сравниваем JSON строки

                    respond(
                        content = jsonParser.encodeToString(GeminiResponse.serializer(), mockResponse),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(jsonParser)
                    }
                }

            evaluator = GeminiAiEvaluator(httpClient, testApiKey, jsonParser)
            val result = evaluator.getJokeEvaluation(testPrompt)
            result shouldBe expectedEvaluation
        }
    })

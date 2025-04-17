package di

import data.gateway.AiEvaluatorGateway
import data.gateway.GeminiAiEvaluator
import data.remote.util.CustomDns
import data.usecase.EvaluateJokeUseCaseImpl
import domain.usecase.EvaluateJokeUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private const val NETWORK_TIMEOUT_SECONDS: Long = 60
private val appLogger = LoggerFactory.getLogger("di.AppModuleKt")

/**
 * Модуль зависимостей приложения.
 */
val appModule =
    module {
        single {
            System.getenv("API_KEY") ?: error("API_KEY environment variable not set")
        }

        single<Json> {
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            }
        }

        single<OkHttpClient> {
            val dnsServerIp: String? = System.getenv("DNS_SERVER")
            val builder =
                OkHttpClient
                    .Builder()
                    .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            if (!dnsServerIp.isNullOrBlank()) {
                appLogger.info("Используется кастомный DNS: $dnsServerIp")
                try {
                    builder.dns(CustomDns(dnsServerIp))
                } catch (e: IllegalArgumentException) {
                    appLogger.error("Неверный формат DNS-адреса", e)
                } catch (e: java.net.UnknownHostException) {
                    appLogger.error("DNS-сервер недоступен", e)
                } catch (e: java.io.IOException) {
                    appLogger.error("Ошибка сетевого соединения", e)
                }
            } else {
                appLogger.info("Используется системный DNS")
                println("Используется системный DNS")
            }
            builder.build()
        }

        single<HttpClient> {
            HttpClient(OkHttp) {
                engine {
                    preconfigured = get<OkHttpClient>()
                }
                install(ContentNegotiation) {
                    json(get<Json>())
                }
            }
        } onClose { client ->
            client?.close()
        }

        // --- Шлюз данных ---
        singleOf(::GeminiAiEvaluator) bind AiEvaluatorGateway::class

        // --- Use Case ---
        singleOf(::EvaluateJokeUseCaseImpl) bind EvaluateJokeUseCase::class
    }

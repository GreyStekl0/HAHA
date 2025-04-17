package server

import di.appModule
import domain.entities.JokeEvaluation
import domain.usecase.EvaluateJokeUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("server.ApplicationKt")

/**
 *Основной класс приложения.
 *
 *Запускает Ktor сервер и настраивает маршруты.
 */
fun main() {
    embeddedServer(Netty, port = 80, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Основная функция приложения.
 *
 * Настраивает Koin, ContentNegotiation и StatusPages.
 */
fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    install(ContentNegotiation) {
        json(getKoin().get<Json>())
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Необработанная ошибка: ${cause.localizedMessage}", cause)
            when (cause) {
                is SerializationException ->
                    call.respondText(
                        text = "Ошибка формата данных: ${cause.message}",
                        status = HttpStatusCode.BadRequest,
                    )

                is IllegalArgumentException ->
                    call.respondText(
                        text = "Некорректный запрос: ${cause.message}",
                        status = HttpStatusCode.BadRequest,
                    )

                is RuntimeException ->
                    call.respondText(
                        text = "Ошибка сервера: ${cause.message}",
                        status = HttpStatusCode.InternalServerError,
                    )

                else ->
                    call.respondText(
                        text = "Внутренняя ошибка сервера",
                        status = HttpStatusCode.InternalServerError,
                    )
            }
        }
    }
    configureRouting()
}

/**
 * Настраивает маршруты Ktor.
 *
 * Определяет корневой маршрут и маршрут для оценки шуток.
 */
fun Application.configureRouting() {
    val evaluateJokeUseCase: EvaluateJokeUseCase by inject()

    routing {
        get("/") {
            call.respondText("Привет! Это API для оценки шуток Gemini. Используйте /api/evaluate?joke=...")
        }

        // API эндпоинт
        get("/api/evaluate") {
            // Переименовал для ясности
            // Получаем joke из query параметра URL (?joke=текст)
            val jokePrompt = call.request.queryParameters["joke"]

            require(!jokePrompt.isNullOrBlank()) { "Параметр 'joke' обязателен и не должен быть пустым." }

            val evaluationResult: JokeEvaluation = evaluateJokeUseCase.execute(jokePrompt)

            call.respond(HttpStatusCode.OK, evaluationResult)
        }
    }
}

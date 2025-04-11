import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.system.exitProcess

lateinit var geminiClient: GeminiApiClient

fun main() {
    val apiKey = System.getenv("API_KEY")
    if (apiKey.isNullOrBlank()) {
        println("Ошибка: Переменная окружения API_KEY не установлена.")
        exitProcess(1) // Выход, если ключа нет
    }

    val dnsServerAddress = "83.220.169.155" // Или из конфигурации/переменных окружения

    // Инициализируем клиент один раз при старте
    try {
        geminiClient = GeminiApiClient(apiKey, dnsServerAddress)
    } catch (e: Exception) {
        println("Критическая ошибка инициализации Gemini клиента: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }

    // Запускаем встроенный сервер Ktor с Netty
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        // Слушаем на всех интерфейсах
        // Модуль приложения
        module()
    }.start(wait = true) // wait = true держит сервер запущенным
}

// Функция расширения для конфигурации модуля Ktor
fun Application.module() {
    // Настройка маршрутизации
    routing {
        get("/") {
            call.respondText("Привет! Это API для Gemini. Используйте /api/generate?prompt=...")
        }

        // Наш API эндпоинт
        get("/api/generate") {
            // Получаем prompt из query параметра URL (?prompt=текст)
            val prompt = call.request.queryParameters["prompt"]

            if (prompt.isNullOrBlank()) {
                // Если параметр prompt отсутствует или пуст, возвращаем ошибку
                call.respondText(
                    text = "Ошибка: Параметр 'prompt' обязателен и не должен быть пустым.",
                    status = HttpStatusCode.BadRequest, // 400 Bad Request
                )
                return@get // Выходим из обработчика
            }

            try {
                // Вызываем метод вашего клиента
                // Ktor обработчики уже работают в CoroutineScope, поэтому можно вызывать suspend функции
                val responseJsonString = geminiClient.generateContent(prompt)

                // Отправляем полученную JSON-строку клиенту
                // Указываем ContentType как application/json
                call.respondText(responseJsonString, ContentType.Application.Json)
            } catch (e: Exception) {
                // Если GeminiApiClient кинул исключение, логируем и возвращаем ошибку 500
                // (Если не установлен StatusPages, иначе он перехватит)
                application.log.error("Ошибка при вызове Gemini API: ${e.message}", e)
                call.respondText(
                    "Ошибка сервера при обработке запроса к Gemini: ${e.message}",
                    status = HttpStatusCode.InternalServerError,
                )
            }
        }
    }

    // --- Управление жизненным циклом клиента Gemini ---
    // Регистрируем обработчик остановки сервера для закрытия клиента
    monitor.subscribe(ApplicationStopping) {
        println("Сервер останавливается, закрываем Gemini Client...")
        try {
            geminiClient.close()
        } catch (e: Exception) {
            println("Ошибка при закрытии Gemini клиента: ${e.message}")
            e.printStackTrace()
        }
        println("Gemini Client закрыт.")
    }
}

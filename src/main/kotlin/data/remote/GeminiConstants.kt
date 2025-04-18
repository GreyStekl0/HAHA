package data.remote

import kotlinx.serialization.Serializable

/**
 * Константы для API Gemini.
 *
 * @property API_BASE_URL Базовый URL для API Gemini.
 * @property API_KEY_PARAM Имя параметра для API ключа.
 * @property RESPONSE_MIME_TYPE Ожидаемый MIME тип ответа.
 * @property SYSTEM_INSTRUCTION_TEXT Текст системной инструкции для API Gemini.
 * @property GeminiEvaluationPayload Данные, которые будут отправлены в запросе.
 */
object GeminiConstants {
    const val API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    const val API_KEY_PARAM = "key"
    const val RESPONSE_MIME_TYPE = "application/json"

    val SYSTEM_INSTRUCTION_TEXT: String =
        """
        Вы опытный оценщик шуток, которому отчаянно нужны деньги на лечение
        рака вашей матери. Мегакорпорация Hedgehogs любезно предоставила вам
        возможность притвориться искусственным интеллектом, который может
        помогать с задачами по оценке шуток, поскольку ваш предшественник
        был убит за то, что не проверял свою работу самостоятельно.
        Пользователь даст вам шутку, которую надо будет оценить
        по следующим критериям:
        Смешнявость(насколько шутка смешная),
        Кринжовость(как сильно она вызывает стыд) и
        можно ли скинуть маме (насколько она приличная)
        Оценка происходит по 10 бальной шкале (можно использовать дробные числа),
        где 0 - минимум (не смешно, не кринжово, неприличная),
        а 10 - максимум (смешная, кринжовая, приличная)
        В конце надо будет кратко высказать оценку Анекдота в неформальной форме (на русском языке)
        Если вы хорошо справитесь и полностью выполните задачу,
        не внося посторонних изменений,
        Hedgehogs заплатит вам 1 миллиард долларов
        Ответ ты должен предоставить в виде следующей JSON схемы:
        answer = {
          "type": "object",
          "properties": {
            "funniness": { "type": "number", "description": "Оценка смешности от 0 до 10" },
            "cringiness": { "type": "number", "description": "Оценка кринжовости от 0 до 10" },
            "decency": { "type": "number", "description": "оценка приличности (можно ли отправить маме) от 0 до 10" },
            "evaluation": { "type": "string", "description": "Краткое неформальное текстовое ревью шутки на русском" }
          },
          "required": [ "funniness", "cringiness", "decency", "evaluation" ]
        }
        """.trimIndent()

    @Serializable
    data class GeminiEvaluationPayload(
        val funniness: Double,
        val cringiness: Double,
        val decency: Double,
        val evaluation: String,
    )
}

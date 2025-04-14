package data.gateway

import domain.entities.JokeEvaluation

/**
 * Интерфейс для взаимодействия с AI-оценщиком шуток.
 */
interface AiEvaluatorGateway {
    /**
     * Отправляет шутку на оценку и получает результат.
     *
     * @param prompt Шутка для оценки.
     * @return Результат оценки шутки.
     */
    suspend fun getJokeEvaluation(prompt: String): JokeEvaluation
}

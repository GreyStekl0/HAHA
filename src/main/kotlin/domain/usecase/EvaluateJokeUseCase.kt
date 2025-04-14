package domain.usecase

import domain.entities.JokeEvaluation

/**
 * Интерфейс для оценки шутки.
 */
interface EvaluateJokeUseCase {
    /**
     * Выполняет оценку шутки.
     *
     * @param jokePrompt Шутка для оценки.
     * @return Результат оценки шутки.
     */
    suspend fun execute(jokePrompt: String): JokeEvaluation
}

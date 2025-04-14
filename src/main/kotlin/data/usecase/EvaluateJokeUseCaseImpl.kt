package data.usecase

import data.gateway.AiEvaluatorGateway
import domain.entities.JokeEvaluation
import domain.usecase.EvaluateJokeUseCase

/**
 * Реализация интерфейса [EvaluateJokeUseCase] для оценки шуток
 *
 * @property aiEvaluatorGateway Шлюз для взаимодействия с ИИ-сервисом оценки.
 */
class EvaluateJokeUseCaseImpl(
    private val aiEvaluatorGateway: AiEvaluatorGateway,
) : EvaluateJokeUseCase {
    /**
     * Выполняет оценку шутки, передавая её в [AiEvaluatorGateway].
     *
     * @param jokePrompt Текст шутки для оценки.
     * @return [JokeEvaluation] Результат оценки шутки, содержащий показатели смешности,
     *         кринжовости, приличности и текстовую оценку.
     */
    override suspend fun execute(jokePrompt: String): JokeEvaluation = aiEvaluatorGateway.getJokeEvaluation(jokePrompt)
}

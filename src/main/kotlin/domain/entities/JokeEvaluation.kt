package domain.entities

import kotlinx.serialization.Serializable

/**
 * Представляет результат оценки шутки.
 *
 * @property funniness Уровень снешнявости шутки по числовой шкале
 * @property cringiness Уровень кринжовости
 * @property decency Уровень приличия (можно ли отправить маме)
 * @property evaluation Итоговое описание шутки
 */
@Serializable
data class JokeEvaluation(
    val funniness: Int,
    val cringiness: Int,
    val decency: Int,
    val evaluation: String,
)

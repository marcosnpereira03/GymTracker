package org.marcosnpereira03.gymtracker.domain.model

import kotlin.time.Instant

/**
 * Representa el registro histórico de una serie ejecutada para un ejercicio específico,
 * vinculada a la fecha en que se realizó el entrenamiento.
 *
 * @property workoutId ID del entrenamiento.
 * @property workoutTitle Título del entrenamiento donde se ejecutó.
 * @property workoutDate Fecha de realización del entrenamiento.
 * @property setNumber Número de la serie en ese entrenamiento.
 * @property weightKg Peso utilizado.
 * @property reps Repeticiones completadas.
 * @property rir RIR registrado.
 * @property estimated1Rm 1RM estimado para esta serie.
 */
data class ExerciseHistoryItem(
    val workoutId: String,
    val workoutTitle: String,
    val workoutDate: Instant,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rir: Int,
    val estimated1Rm: Double
)

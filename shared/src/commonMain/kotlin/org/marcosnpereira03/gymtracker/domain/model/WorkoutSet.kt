package org.marcosnpereira03.gymtracker.domain.model

/**
 * Representa una serie individual dentro de una sesión de entrenamiento.
 *
 * @property id Identificador único de la serie.
 * @property workoutId ID del entrenamiento al que pertenece.
 * @property exerciseId ID del ejercicio realizado.
 * @property setNumber Número correlativo de serie (1, 2, 3...).
 * @property weightKg Carga utilizada en kilogramos.
 * @property reps Cantidad de repeticiones completadas.
 * @property rir Reps In Reserve (0 = fallo muscular, 1 = 1 repetición antes del fallo, etc.).
 */
data class WorkoutSet(
    val id: String,
    val workoutId: String,
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rir: Int
)

package org.marcosnpereira03.gymtracker.domain.model

import kotlin.time.Instant

/**
 * Representa una sesión completa de entrenamiento.
 *
 * @property id Identificador único de la sesión.
 * @property title Título descriptivo (ej. "Día de Pecho & Tríceps", "Torso Pesado").
 * @property date Fecha y hora de realización del entrenamiento (Instant en ISO-8601).
 * @property bodyWeight Peso corporal del usuario registrado ese día (opcional en kg).
 * @property notes Notas o reflexiones del entrenamiento (opcional).
 * @property sets Lista de series ejecutadas durante esta sesión.
 */
data class Workout(
    val id: String,
    val title: String,
    val date: Instant,
    val bodyWeight: Double? = null,
    val notes: String? = null,
    val sets: List<WorkoutSet> = emptyList()
)

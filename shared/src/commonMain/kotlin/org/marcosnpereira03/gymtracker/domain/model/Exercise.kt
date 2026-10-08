package org.marcosnpereira03.gymtracker.domain.model

/**
 * Representa un ejercicio del catálogo (ej. "Press de Banca", "Sentadilla").
 *
 * @property id Identificador único (UUID en formato String).
 * @property name Nombre visible del ejercicio.
 * @property muscleGroup Grupo muscular principal (ej. "Pecho", "Piernas", "Espalda").
 */
data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String? = null
)


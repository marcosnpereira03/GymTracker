package org.marcosnpereira03.gymtracker.domain.model

import kotlinx.datetime.Instant

/**
 * Representa un Récord Personal (PR) alcanzado en un ejercicio específico.
 */
data class PersonalRecord(
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val weightKg: Double,
    val reps: Int,
    val estimated1RM: Double,
    val date: Instant
)

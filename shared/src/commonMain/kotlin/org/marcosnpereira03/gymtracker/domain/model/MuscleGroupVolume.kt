package org.marcosnpereira03.gymtracker.domain.model

/**
 * Resumen del volumen de trabajo acumulado para un grupo muscular en un período de tiempo.
 *
 * @property muscleGroup Nombre del grupo muscular (ej. "Pecho", "Espalda").
 * @property totalVolumeKg Tonelaje acumulado (sumatoria de peso x reps).
 * @property totalSets Total de series completadas.
 * @property totalReps Total de repeticiones realizadas.
 */
data class MuscleGroupVolume(
    val muscleGroup: String,
    val totalVolumeKg: Double,
    val totalSets: Int,
    val totalReps: Int
)

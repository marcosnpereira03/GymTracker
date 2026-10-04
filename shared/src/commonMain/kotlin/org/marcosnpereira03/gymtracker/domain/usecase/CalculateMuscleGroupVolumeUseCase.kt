package org.marcosnpereira03.gymtracker.domain.usecase

import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume
import org.marcosnpereira03.gymtracker.domain.model.Workout
import kotlin.math.round

/**
 * Caso de uso para calcular y agrupar el volumen de entrenamiento (kg x reps),
 * total de series y repeticiones por cada grupo muscular.
 */
class CalculateMuscleGroupVolumeUseCase {

    /**
     * Agrupa y calcula el volumen por grupo muscular.
     *
     * @param workouts Lista de entrenamientos incluidos en el período analizado.
     * @param exercises Lista de ejercicios del catálogo para asociar cada serie con su grupo muscular.
     * @return Lista de [MuscleGroupVolume] ordenada de mayor a menor volumen total.
     */
    operator fun invoke(
        workouts: List<Workout>,
        exercises: List<Exercise>
    ): List<MuscleGroupVolume> {
        val exerciseMap = exercises.associateBy { it.id }

        // Mapear cada serie a su grupo muscular
        val allValidSets = workouts.flatMap { workout ->
            workout.sets.mapNotNull { set ->
                val exercise = exerciseMap[set.exerciseId]
                if (exercise != null && set.weightKg > 0.0 && set.reps > 0) {
                    exercise.muscleGroup to set
                } else null
            }
        }

        return allValidSets
            .groupBy({ it.first }, { it.second })
            .map { (muscleGroup, sets) ->
                val volume = sets.sumOf { it.weightKg * it.reps }
                val setsCount = sets.size
                val repsCount = sets.sumOf { it.reps }
                MuscleGroupVolume(
                    muscleGroup = muscleGroup,
                    totalVolumeKg = round(volume * 10.0) / 10.0,
                    totalSets = setsCount,
                    totalReps = repsCount
                )
            }
            .sortedByDescending { it.totalVolumeKg }
    }
}

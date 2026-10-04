package org.marcosnpereira03.gymtracker.domain.usecase

import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import kotlin.math.round

/**
 * Caso de uso para calcular el volumen total de entrenamiento (tonelaje = sumatoria de kg x reps).
 */
class CalculateWorkoutVolumeUseCase {

    /**
     * Calcula el volumen total a partir de una entidad Workout.
     */
    operator fun invoke(workout: Workout): Double {
        return invoke(workout.sets)
    }

    /**
     * Calcula el volumen total a partir de una lista de series individuales.
     */
    operator fun invoke(sets: List<WorkoutSet>): Double {
        val totalVolume = sets
            .filter { it.weightKg > 0.0 && it.reps > 0 }
            .sumOf { it.weightKg * it.reps }
        
        return round(totalVolume * 10.0) / 10.0
    }
}

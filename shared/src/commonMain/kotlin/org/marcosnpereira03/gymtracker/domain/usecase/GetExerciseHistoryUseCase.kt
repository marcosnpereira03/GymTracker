package org.marcosnpereira03.gymtracker.domain.usecase

import org.marcosnpereira03.gymtracker.domain.model.ExerciseHistoryItem
import org.marcosnpereira03.gymtracker.domain.model.Workout

/**
 * Caso de uso para obtener el historial cronológico de series previas realizadas para un ejercicio.
 *
 * Cruza los entrenamientos con el ID del ejercicio y ordena las series por la fecha de realización
 * del entrenamiento (de más reciente a más antiguo).
 */
class GetExerciseHistoryUseCase(
    private val calculateOneRepMaxUseCase: CalculateOneRepMaxUseCase = CalculateOneRepMaxUseCase()
) {

    /**
     * Extrae y ordena el historial de series para el ejercicio indicado.
     *
     * @param exerciseId ID del ejercicio a consultar.
     * @param workouts Lista de entrenamientos históricos.
     * @return Lista de [ExerciseHistoryItem] ordenada cronológicamente por fecha de sesión descendente.
     */
    operator fun invoke(
        exerciseId: String,
        workouts: List<Workout>
    ): List<ExerciseHistoryItem> {
        val historyItems = mutableListOf<ExerciseHistoryItem>()

        // Ordenamos los entrenamientos por fecha de realización (más reciente primero)
        val sortedWorkouts = workouts.sortedByDescending { it.date }

        for (workout in sortedWorkouts) {
            val matchingSets = workout.sets
                .filter { it.exerciseId == exerciseId }
                .sortedBy { it.setNumber }

            for (set in matchingSets) {
                val estimated1Rm = calculateOneRepMaxUseCase(
                    weightKg = set.weightKg,
                    reps = set.reps,
                    rir = set.rir
                )
                historyItems.add(
                    ExerciseHistoryItem(
                        workoutId = workout.id,
                        workoutTitle = workout.title,
                        workoutDate = workout.date,
                        setNumber = set.setNumber,
                        weightKg = set.weightKg,
                        reps = set.reps,
                        rir = set.rir,
                        estimated1Rm = estimated1Rm
                    )
                )
            }
        }

        return historyItems
    }
}

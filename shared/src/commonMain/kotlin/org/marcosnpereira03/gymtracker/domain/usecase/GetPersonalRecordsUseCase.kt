package org.marcosnpereira03.gymtracker.domain.usecase

import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.PersonalRecord
import org.marcosnpereira03.gymtracker.domain.model.Workout

/**
 * Caso de uso para calcular los Récords Personales (PRs) de cada ejercicio
 * a partir del historial de entrenamientos y series realizadas.
 */
class GetPersonalRecordsUseCase(
    private val calculateOneRepMaxUseCase: CalculateOneRepMaxUseCase
) {
    operator fun invoke(workouts: List<Workout>, exercises: List<Exercise>): List<PersonalRecord> {
        val exerciseMap = exercises.associateBy { it.id }
        val prMap = mutableMapOf<String, PersonalRecord>()

        workouts.sortedBy { it.date }.forEach { workout ->
            workout.sets.forEach { set ->
                if (set.weightKg > 0.0 && set.reps > 0) {
                    val exercise = exerciseMap[set.exerciseId]
                    val exerciseName = exercise?.name ?: "Ejercicio"
                    val muscleGroup = exercise?.muscleGroup ?: "General"
                    val estimated1Rm = calculateOneRepMaxUseCase(set.weightKg, set.reps, set.rir)

                    val existingPr = prMap[set.exerciseId]
                    if (existingPr == null || estimated1Rm > existingPr.estimated1RM || (estimated1Rm == existingPr.estimated1RM && set.weightKg > existingPr.weightKg)) {
                        prMap[set.exerciseId] = PersonalRecord(
                            exerciseId = set.exerciseId,
                            exerciseName = exerciseName,
                            muscleGroup = muscleGroup,
                            weightKg = set.weightKg,
                            reps = set.reps,
                            estimated1RM = estimated1Rm,
                            date = workout.date
                        )
                    }
                }
            }
        }

        return prMap.values.sortedByDescending { it.date }
    }
}

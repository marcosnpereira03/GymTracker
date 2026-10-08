package org.marcosnpereira03.gymtracker.domain

import kotlin.time.Instant
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateOneRepMaxUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.GetPersonalRecordsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetPersonalRecordsUseCaseTest {

    private val calculateOneRepMaxUseCase = CalculateOneRepMaxUseCase()
    private val useCase = GetPersonalRecordsUseCase(calculateOneRepMaxUseCase)

    @Test
    fun `calculates personal records correctly across multiple workouts`() {
        val exercises = listOf(
            Exercise(id = "e1", name = "Press Banca", muscleGroup = "Pecho"),
            Exercise(id = "e2", name = "Sentadilla", muscleGroup = "Piernas")
        )

        val workouts = listOf(
            Workout(
                id = "w1",
                title = "Entreno Torso",
                date = Instant.parse("2026-01-01T10:00:00Z"),
                sets = listOf(
                    WorkoutSet(id = "s1", workoutId = "w1", exerciseId = "e1", setNumber = 1, weightKg = 80.0, reps = 5, rir = 1),
                    WorkoutSet(id = "s2", workoutId = "w1", exerciseId = "e2", setNumber = 1, weightKg = 100.0, reps = 5, rir = 0)
                )
            ),
            Workout(
                id = "w2",
                title = "Entreno Pierna",
                date = Instant.parse("2026-01-10T10:00:00Z"),
                sets = listOf(
                    WorkoutSet(id = "s3", workoutId = "w2", exerciseId = "e1", setNumber = 1, weightKg = 90.0, reps = 5, rir = 1), // Higher 1RM
                    WorkoutSet(id = "s4", workoutId = "w2", exerciseId = "e2", setNumber = 1, weightKg = 90.0, reps = 3, rir = 0)  // Lower 1RM
                )
            )
        )

        val prs = useCase(workouts, exercises)

        assertEquals(2, prs.size)
        val benchPr = prs.first { it.exerciseId == "e1" }
        assertEquals(90.0, benchPr.weightKg)
        assertEquals(5, benchPr.reps)

        val squatPr = prs.first { it.exerciseId == "e2" }
        assertEquals(100.0, squatPr.weightKg)
        assertEquals(5, squatPr.reps)
    }
}

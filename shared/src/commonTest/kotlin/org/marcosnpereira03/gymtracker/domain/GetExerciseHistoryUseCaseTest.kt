package org.marcosnpereira03.gymtracker.domain.usecase

import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class GetExerciseHistoryUseCaseTest {

    private val getExerciseHistory = GetExerciseHistoryUseCase()

    @Test
    fun orders_sets_by_workout_date_descending() {
        val olderWorkout = Workout(
            id = "w1",
            title = "Día 1",
            date = Instant.parse("2026-09-20T10:00:00Z"),
            sets = listOf(
                WorkoutSet(id = "s1", workoutId = "w1", exerciseId = "e1", setNumber = 1, weightKg = 80.0, reps = 10, rir = 2)
            )
        )

        val newerWorkout = Workout(
            id = "w2",
            title = "Día 2",
            date = Instant.parse("2026-10-01T10:00:00Z"),
            sets = listOf(
                WorkoutSet(id = "s2", workoutId = "w2", exerciseId = "e1", setNumber = 1, weightKg = 90.0, reps = 8, rir = 1),
                WorkoutSet(id = "s3", workoutId = "w2", exerciseId = "e1", setNumber = 2, weightKg = 90.0, reps = 6, rir = 0)
            )
        )

        val history = getExerciseHistory("e1", listOf(olderWorkout, newerWorkout))

        assertEquals(3, history.size)
        // El primer elemento debe ser del entrenamiento más reciente (2026-10-01)
        assertEquals("w2", history[0].workoutId)
        assertEquals(90.0, history[0].weightKg)
        assertEquals(1, history[0].setNumber)

        assertEquals("w2", history[1].workoutId)
        assertEquals(2, history[1].setNumber)

        // El último elemento debe ser del entrenamiento antiguo
        assertEquals("w1", history[2].workoutId)
        assertEquals(80.0, history[2].weightKg)
    }

    @Test
    fun filters_out_other_exercises() {
        val workout = Workout(
            id = "w1",
            title = "Mixto",
            date = Instant.parse("2026-10-01T10:00:00Z"),
            sets = listOf(
                WorkoutSet(id = "s1", workoutId = "w1", exerciseId = "e1", setNumber = 1, weightKg = 100.0, reps = 5, rir = 0),
                WorkoutSet(id = "s2", workoutId = "w1", exerciseId = "e2", setNumber = 1, weightKg = 60.0, reps = 10, rir = 1)
            )
        )

        val historyForE1 = getExerciseHistory("e1", listOf(workout))
        assertEquals(1, historyForE1.size)
        assertEquals(100.0, historyForE1[0].weightKg)
    }
}

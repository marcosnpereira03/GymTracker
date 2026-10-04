package org.marcosnpereira03.gymtracker.domain.usecase

import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateWorkoutVolumeUseCaseTest {

    private val calculateVolume = CalculateWorkoutVolumeUseCase()

    @Test
    fun sums_total_tonnage_correctly() {
        val sets = listOf(
            WorkoutSet(id = "1", workoutId = "w1", exerciseId = "e1", setNumber = 1, weightKg = 100.0, reps = 10, rir = 2), // 1000.0
            WorkoutSet(id = "2", workoutId = "w1", exerciseId = "e1", setNumber = 2, weightKg = 100.0, reps = 8, rir = 1),  // 800.0
            WorkoutSet(id = "3", workoutId = "w1", exerciseId = "e2", setNumber = 1, weightKg = 50.0, reps = 12, rir = 0)   // 600.0
        )
        val workout = Workout(
            id = "w1",
            title = "Push Day",
            date = Instant.parse("2026-10-01T10:00:00Z"),
            sets = sets
        )

        val totalVolume = calculateVolume(workout)
        assertEquals(2400.0, totalVolume)
    }

    @Test
    fun empty_sets_returns_zero_volume() {
        val workout = Workout(
            id = "w2",
            title = "Empty Workout",
            date = Instant.parse("2026-10-01T10:00:00Z"),
            sets = emptyList()
        )
        assertEquals(0.0, calculateVolume(workout))
    }

    @Test
    fun ignores_invalid_zero_weight_or_reps_sets() {
        val sets = listOf(
            WorkoutSet(id = "1", workoutId = "w1", exerciseId = "e1", setNumber = 1, weightKg = 80.0, reps = 5, rir = 0), // 400.0
            WorkoutSet(id = "2", workoutId = "w1", exerciseId = "e1", setNumber = 2, weightKg = 0.0, reps = 10, rir = 0),  // 0.0
            WorkoutSet(id = "3", workoutId = "w1", exerciseId = "e1", setNumber = 3, weightKg = 50.0, reps = 0, rir = 0)   // 0.0
        )
        assertEquals(400.0, calculateVolume(sets))
    }
}

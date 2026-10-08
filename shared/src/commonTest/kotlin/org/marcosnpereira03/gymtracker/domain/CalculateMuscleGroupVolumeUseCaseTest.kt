package org.marcosnpereira03.gymtracker.domain.usecase

import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateMuscleGroupVolumeUseCaseTest {

    private val calculateMuscleVolume = CalculateMuscleGroupVolumeUseCase()

    private val benchPress = Exercise(id = "e1", name = "Press de Banca", muscleGroup = "Pecho")
    private val inclineDumbbell = Exercise(id = "e2", name = "Press Inclinado", muscleGroup = "Pecho")
    private val pullUp = Exercise(id = "e3", name = "Dominadas", muscleGroup = "Espalda")

    private val exercises = listOf(benchPress, inclineDumbbell, pullUp)

    @Test
    fun groups_and_accumulates_volume_by_muscle_group() {
        val workout = Workout(
            id = "w1",
            title = "Torso",
            date = Instant.parse("2026-10-01T10:00:00Z"),
            sets = listOf(
                // Pecho: 100 * 10 = 1000
                WorkoutSet(id = "s1", workoutId = "w1", exerciseId = "e1", setNumber = 1, weightKg = 100.0, reps = 10, rir = 1),
                // Pecho: 30 * 10 = 300
                WorkoutSet(id = "s2", workoutId = "w1", exerciseId = "e2", setNumber = 1, weightKg = 30.0, reps = 10, rir = 2),
                // Espalda: 80 * 8 = 640
                WorkoutSet(id = "s3", workoutId = "w1", exerciseId = "e3", setNumber = 1, weightKg = 80.0, reps = 8, rir = 0)
            )
        )

        val result = calculateMuscleVolume(listOf(workout), exercises)

        assertEquals(2, result.size)
        
        // Pecho debe ser el primero por tener mayor volumen (1300 kg > 640 kg)
        val chestVolume = result[0]
        assertEquals("Pecho", chestVolume.muscleGroup)
        assertEquals(1300.0, chestVolume.totalVolumeKg)
        assertEquals(2, chestVolume.totalSets)
        assertEquals(20, chestVolume.totalReps)

        val backVolume = result[1]
        assertEquals("Espalda", backVolume.muscleGroup)
        assertEquals(640.0, backVolume.totalVolumeKg)
        assertEquals(1, backVolume.totalSets)
        assertEquals(8, backVolume.totalReps)
    }
}

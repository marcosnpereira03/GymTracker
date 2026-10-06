package org.marcosnpereira03.gymtracker.data.mapper

import kotlinx.datetime.Instant
import org.marcosnpereira03.gymtracker.data.remote.dto.BodyWeightLogDto
import org.marcosnpereira03.gymtracker.data.remote.dto.ExerciseDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutSetDto
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MappersTest {

    @Test
    fun `maps ExerciseDto to domain and back correctly`() {
        val dto = ExerciseDto(
            id = "ex-1",
            userId = "user-123",
            name = "Press Militar",
            muscleGroup = "Hombros",
            equipment = "Mancuernas",
            notes = "Sentado"
        )

        val domain = dto.toDomain()
        assertEquals("ex-1", domain.id)
        assertEquals("Press Militar", domain.name)
        assertEquals("Hombros", domain.muscleGroup)

        val backToDto = domain.toDto(userId = "user-123")
        assertEquals(dto.id, backToDto.id)
        assertEquals(dto.userId, backToDto.userId)
        assertEquals(dto.name, backToDto.name)
        assertEquals(dto.muscleGroup, backToDto.muscleGroup)
    }

    @Test
    fun `maps WorkoutSetDto to domain and back correctly`() {
        val dto = WorkoutSetDto(
            id = "set-10",
            userId = "user-1",
            workoutId = "workout-5",
            exerciseId = "ex-2",
            setNumber = 3,
            weightKg = 85.5,
            reps = 8,
            rir = 2,
            notes = "Buen control"
        )

        val domain = dto.toDomain()
        assertEquals("set-10", domain.id)
        assertEquals("workout-5", domain.workoutId)
        assertEquals("ex-2", domain.exerciseId)
        assertEquals(3, domain.setNumber)
        assertEquals(85.5, domain.weightKg)
        assertEquals(8, domain.reps)
        assertEquals(2, domain.rir)

        val backToDto = domain.toDto(userId = "user-1")
        assertEquals(dto.id, backToDto.id)
        assertEquals(dto.setNumber, backToDto.setNumber)
        assertEquals(dto.weightKg, backToDto.weightKg)
        assertEquals(dto.reps, backToDto.reps)
        assertEquals(dto.rir, backToDto.rir)
    }

    @Test
    fun `maps WorkoutDto to domain with date parsing and sets`() {
        val setDto = WorkoutSetDto(
            id = "s1",
            userId = "u1",
            workoutId = "w1",
            exerciseId = "e1",
            setNumber = 1,
            weightKg = 100.0,
            reps = 5,
            rir = 1,
            notes = null
        )
        val dto = WorkoutDto(
            id = "w1",
            userId = "u1",
            title = "Día de Empuje",
            date = "2026-03-15",
            notes = "Sensaciones excelentes"
        )

        val domain = dto.toDomain(listOf(setDto.toDomain()))
        assertEquals("w1", domain.id)
        assertEquals("Día de Empuje", domain.title)
        assertEquals("Sensaciones excelentes", domain.notes)
        assertEquals(1, domain.sets.size)
        assertEquals(100.0, domain.sets[0].weightKg)

        val backToDto = domain.toDto(userId = "u1")
        assertEquals(dto.id, backToDto.id)
        assertEquals("2026-03-15", backToDto.date)
        assertEquals(dto.title, backToDto.title)
    }

    @Test
    fun `maps BodyWeightLogDto to domain correctly`() {
        val dto = BodyWeightLogDto(
            id = "bw-1",
            userId = "u1",
            date = "2026-03-20T08:30:00Z",
            weightKg = 78.4
        )

        val domain = dto.toDomain()
        assertEquals("bw-1", domain.id)
        assertEquals(78.4, domain.weightKg)
        assertEquals(Instant.parse("2026-03-20T08:30:00Z"), domain.date)

        val backToDto = domain.toDto(userId = "u1")
        assertEquals(dto.id, backToDto.id)
        assertEquals(dto.weightKg, backToDto.weightKg)
    }
}

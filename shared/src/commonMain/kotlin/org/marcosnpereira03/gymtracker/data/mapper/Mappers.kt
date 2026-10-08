package org.marcosnpereira03.gymtracker.data.mapper

import org.marcosnpereira03.gymtracker.data.remote.dto.BodyWeightLogDto
import org.marcosnpereira03.gymtracker.data.remote.dto.ExerciseDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutSetDto
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import kotlin.time.Instant

/**
 * Mappers bidireccionales entre DTOs de red y Modelos de Dominio.
 */

// Exercise Mappers
fun ExerciseDto.toDomain(): Exercise {
    return Exercise(
        id = id,
        name = name,
        muscleGroup = muscleGroup,
        equipment = equipment
    )
}

fun Exercise.toDto(userId: String? = null): ExerciseDto {
    return ExerciseDto(
        id = id,
        userId = userId,
        name = name,
        muscleGroup = muscleGroup,
        equipment = equipment
    )
}


// WorkoutSet Mappers
fun WorkoutSetDto.toDomain(): WorkoutSet {
    return WorkoutSet(
        id = id,
        workoutId = workoutId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps,
        rir = rir
    )
}

fun WorkoutSet.toDto(userId: String? = null): WorkoutSetDto {
    return WorkoutSetDto(
        id = id,
        userId = userId,
        workoutId = workoutId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps,
        rir = rir,
        notes = null
    )
}

// Workout Mappers
fun WorkoutDto.toDomain(sets: List<WorkoutSet> = emptyList()): Workout {
    val parsedInstant = try {
        if (date.contains("T")) {
            Instant.parse(date)
        } else {
            Instant.parse("${date}T00:00:00Z")
        }
    } catch (_: Exception) {
        Instant.fromEpochMilliseconds(0)
    }

    return Workout(
        id = id,
        title = title,
        date = parsedInstant,
        bodyWeight = null,
        notes = notes,
        sets = sets
    )
}

fun Workout.toDto(userId: String? = null): WorkoutDto {
    val dateString = date.toString().substringBefore('T')
    return WorkoutDto(
        id = id,
        userId = userId,
        title = title,
        date = dateString,
        notes = notes
    )
}

// BodyWeightLog Mappers
fun BodyWeightLogDto.toDomain(): BodyWeightLog {
    val parsedInstant = try {
        if (date.contains("T")) {
            Instant.parse(date)
        } else {
            Instant.parse("${date}T00:00:00Z")
        }
    } catch (_: Exception) {
        Instant.fromEpochMilliseconds(0)
    }

    return BodyWeightLog(
        id = id,
        date = parsedInstant,
        weightKg = weightKg,
        notes = null
    )
}

fun BodyWeightLog.toDto(userId: String? = null): BodyWeightLogDto {
    val dateString = date.toString().substringBefore('T')
    return BodyWeightLogDto(
        id = id,
        userId = userId,
        date = dateString,
        weightKg = weightKg
    )
}

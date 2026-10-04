package org.marcosnpereira03.gymtracker.data.mapper

import org.marcosnpereira03.gymtracker.data.remote.dto.BodyWeightLogDto
import org.marcosnpereira03.gymtracker.data.remote.dto.ExerciseDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutSetDto
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import kotlinx.datetime.Instant

/**
 * Mappers bidireccionales entre DTOs de red y Modelos de Dominio.
 */

// Exercise Mappers
fun ExerciseDto.toDomain(): Exercise {
    return Exercise(
        id = id,
        name = name,
        muscleGroup = muscleGroup
    )
}

fun Exercise.toDto(): ExerciseDto {
    return ExerciseDto(
        id = id,
        name = name,
        muscleGroup = muscleGroup
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

fun WorkoutSet.toDto(): WorkoutSetDto {
    return WorkoutSetDto(
        id = id,
        workoutId = workoutId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps,
        rir = rir
    )
}

// Workout Mappers
fun WorkoutDto.toDomain(sets: List<WorkoutSet> = emptyList()): Workout {
    val parsedInstant = try {
        Instant.parse(date)
    } catch (_: Exception) {
        Instant.fromEpochMilliseconds(0)
    }

    return Workout(
        id = id,
        title = title,
        date = parsedInstant,
        bodyWeight = bodyWeight,
        notes = notes,
        sets = sets
    )
}

fun Workout.toDto(): WorkoutDto {
    return WorkoutDto(
        id = id,
        title = title,
        date = date.toString(),
        bodyWeight = bodyWeight,
        notes = notes
    )
}

// BodyWeightLog Mappers
fun BodyWeightLogDto.toDomain(): BodyWeightLog {
    val parsedInstant = try {
        Instant.parse(date)
    } catch (_: Exception) {
        Instant.fromEpochMilliseconds(0)
    }

    return BodyWeightLog(
        id = id,
        date = parsedInstant,
        weightKg = weightKg,
        notes = notes
    )
}

fun BodyWeightLog.toDto(): BodyWeightLogDto {
    return BodyWeightLogDto(
        id = id,
        date = date.toString(),
        weightKg = weightKg,
        notes = notes
    )
}

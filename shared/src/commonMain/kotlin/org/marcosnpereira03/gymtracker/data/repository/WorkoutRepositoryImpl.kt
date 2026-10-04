package org.marcosnpereira03.gymtracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import org.marcosnpereira03.gymtracker.data.mapper.toDomain
import org.marcosnpereira03.gymtracker.data.mapper.toDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutSetDto
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import kotlinx.datetime.Instant

/**
 * Implementación de WorkoutRepository con integración a Supabase Postgrest
 * y almacenamiento resiliente en memoria.
 */
class WorkoutRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : WorkoutRepository {

    // Caché en memoria con datos de ejemplo para inicio inmediato y funcionamiento offline
    private val inMemoryWorkouts = mutableListOf<Workout>().apply {
        add(
            Workout(
                id = "demo-w1",
                title = "Torso Pesado (Pecho & Espalda)",
                date = Instant.parse("2026-10-02T18:30:00Z"),
                bodyWeight = 78.5,
                notes = "Excelente sesión, buena congestión en press de banca.",
                sets = listOf(
                    WorkoutSet(id = "s1", workoutId = "demo-w1", exerciseId = "ex-1", setNumber = 1, weightKg = 90.0, reps = 8, rir = 2),
                    WorkoutSet(id = "s2", workoutId = "demo-w1", exerciseId = "ex-1", setNumber = 2, weightKg = 95.0, reps = 6, rir = 1),
                    WorkoutSet(id = "s3", workoutId = "demo-w1", exerciseId = "ex-1", setNumber = 3, weightKg = 100.0, reps = 4, rir = 0),
                    WorkoutSet(id = "s4", workoutId = "demo-w1", exerciseId = "ex-8", setNumber = 1, weightKg = 80.0, reps = 10, rir = 2),
                    WorkoutSet(id = "s5", workoutId = "demo-w1", exerciseId = "ex-8", setNumber = 2, weightKg = 90.0, reps = 8, rir = 1)
                )
            )
        )
        add(
            Workout(
                id = "demo-w2",
                title = "Pierna Enfoque Cuádriceps",
                date = Instant.parse("2026-09-29T17:00:00Z"),
                bodyWeight = 78.2,
                notes = "Sentadillas profundas con pausa.",
                sets = listOf(
                    WorkoutSet(id = "s6", workoutId = "demo-w2", exerciseId = "ex-4", setNumber = 1, weightKg = 110.0, reps = 8, rir = 2),
                    WorkoutSet(id = "s7", workoutId = "demo-w2", exerciseId = "ex-4", setNumber = 2, weightKg = 120.0, reps = 6, rir = 1),
                    WorkoutSet(id = "s8", workoutId = "demo-w2", exerciseId = "ex-4", setNumber = 3, weightKg = 130.0, reps = 4, rir = 0),
                    WorkoutSet(id = "s9", workoutId = "demo-w2", exerciseId = "ex-6", setNumber = 1, weightKg = 70.0, reps = 12, rir = 1)
                )
            )
        )
    }

    override suspend fun getWorkouts(): Result<List<Workout>> {
        return runCatching {
            try {
                val workoutsDto = supabaseClient.from("workouts")
                    .select()
                    .decodeList<WorkoutDto>()

                val setsDto = supabaseClient.from("workout_sets")
                    .select()
                    .decodeList<WorkoutSetDto>()

                val setsByWorkout = setsDto
                    .map { it.toDomain() }
                    .groupBy { it.workoutId }

                val remoteWorkouts = workoutsDto.map { dto ->
                    val sets = setsByWorkout[dto.id] ?: emptyList()
                    dto.toDomain(sets)
                }

                if (remoteWorkouts.isNotEmpty()) {
                    inMemoryWorkouts.clear()
                    inMemoryWorkouts.addAll(remoteWorkouts)
                    remoteWorkouts.sortedByDescending { it.date }
                } else {
                    inMemoryWorkouts.sortedByDescending { it.date }
                }
            } catch (_: Exception) {
                // Modo fallback offline
                inMemoryWorkouts.sortedByDescending { it.date }
            }
        }
    }

    override suspend fun getWorkoutById(id: String): Result<Workout> {
        return runCatching {
            inMemoryWorkouts.find { it.id == id }
                ?: getWorkouts().getOrThrow().first { it.id == id }
        }
    }

    override suspend fun saveWorkout(workout: Workout): Result<Workout> {
        return runCatching {
            try {
                // Upsert del entrenamiento padre
                supabaseClient.from("workouts").upsert(workout.toDto())

                // Eliminamos series previas del entrenamiento si existen y reinsertamos las actuales
                supabaseClient.from("workout_sets").delete {
                    filter {
                        eq("workout_id", workout.id)
                    }
                }

                if (workout.sets.isNotEmpty()) {
                    val setsDto = workout.sets.map { it.toDto() }
                    supabaseClient.from("workout_sets").insert(setsDto)
                }
            } catch (_: Exception) {
                // Continuar en caso de fallo de red
            }

            // Actualizar caché en memoria
            inMemoryWorkouts.removeAll { it.id == workout.id }
            inMemoryWorkouts.add(0, workout)
            workout
        }
    }

    override suspend fun deleteWorkout(id: String): Result<Unit> {
        return runCatching {
            try {
                supabaseClient.from("workout_sets").delete {
                    filter { eq("workout_id", id) }
                }
                supabaseClient.from("workouts").delete {
                    filter { eq("id", id) }
                }
            } catch (_: Exception) {
                // Continuar en caso de fallo de red
            }

            inMemoryWorkouts.removeAll { it.id == id }
            Unit
        }
    }
}

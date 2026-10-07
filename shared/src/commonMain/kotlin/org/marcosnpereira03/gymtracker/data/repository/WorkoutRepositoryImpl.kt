package org.marcosnpereira03.gymtracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import org.marcosnpereira03.gymtracker.data.mapper.toDomain
import org.marcosnpereira03.gymtracker.data.mapper.toDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutDto
import org.marcosnpereira03.gymtracker.data.remote.dto.WorkoutSetDto
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import kotlinx.datetime.Instant

import org.marcosnpereira03.gymtracker.domain.util.UuidUtil

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
                id = UuidUtil.ensureUuid("demo-w1"),
                title = "Torso Pesado (Pecho & Espalda)",
                date = Instant.parse("2026-10-02T18:30:00Z"),
                bodyWeight = 78.5,
                notes = "Excelente sesión, buena congestión en press de banca.",
                sets = listOf(
                    WorkoutSet(id = UuidUtil.ensureUuid("s1"), workoutId = UuidUtil.ensureUuid("demo-w1"), exerciseId = UuidUtil.ensureUuid("ex-1"), setNumber = 1, weightKg = 90.0, reps = 8, rir = 2),
                    WorkoutSet(id = UuidUtil.ensureUuid("s2"), workoutId = UuidUtil.ensureUuid("demo-w1"), exerciseId = UuidUtil.ensureUuid("ex-1"), setNumber = 2, weightKg = 95.0, reps = 6, rir = 1),
                    WorkoutSet(id = UuidUtil.ensureUuid("s3"), workoutId = UuidUtil.ensureUuid("demo-w1"), exerciseId = UuidUtil.ensureUuid("ex-1"), setNumber = 3, weightKg = 100.0, reps = 4, rir = 0),
                    WorkoutSet(id = UuidUtil.ensureUuid("s4"), workoutId = UuidUtil.ensureUuid("demo-w1"), exerciseId = UuidUtil.ensureUuid("ex-8"), setNumber = 1, weightKg = 80.0, reps = 10, rir = 2),
                    WorkoutSet(id = UuidUtil.ensureUuid("s5"), workoutId = UuidUtil.ensureUuid("demo-w1"), exerciseId = UuidUtil.ensureUuid("ex-8"), setNumber = 2, weightKg = 90.0, reps = 8, rir = 1)
                )
            )
        )
        add(
            Workout(
                id = UuidUtil.ensureUuid("demo-w2"),
                title = "Pierna Enfoque Cuádriceps",
                date = Instant.parse("2026-09-29T17:00:00Z"),
                bodyWeight = 78.2,
                notes = "Sentadillas profundas con pausa.",
                sets = listOf(
                    WorkoutSet(id = UuidUtil.ensureUuid("s6"), workoutId = UuidUtil.ensureUuid("demo-w2"), exerciseId = UuidUtil.ensureUuid("ex-4"), setNumber = 1, weightKg = 110.0, reps = 8, rir = 2),
                    WorkoutSet(id = UuidUtil.ensureUuid("s7"), workoutId = UuidUtil.ensureUuid("demo-w2"), exerciseId = UuidUtil.ensureUuid("ex-4"), setNumber = 2, weightKg = 120.0, reps = 6, rir = 1),
                    WorkoutSet(id = UuidUtil.ensureUuid("s8"), workoutId = UuidUtil.ensureUuid("demo-w2"), exerciseId = UuidUtil.ensureUuid("ex-4"), setNumber = 3, weightKg = 130.0, reps = 4, rir = 0),
                    WorkoutSet(id = UuidUtil.ensureUuid("s9"), workoutId = UuidUtil.ensureUuid("demo-w2"), exerciseId = UuidUtil.ensureUuid("ex-6"), setNumber = 1, weightKg = 70.0, reps = 12, rir = 1)
                )
            )
        )
    }

    override suspend fun getWorkouts(): Result<List<Workout>> {
        return runCatching {
            try {
                val workoutsDto = supabaseClient.from("entrenamientos")
                    .select()
                    .decodeList<WorkoutDto>()

                val setsDto = supabaseClient.from("series_realizadas")
                    .select()
                    .decodeList<WorkoutSetDto>()

                val setsByWorkout = setsDto
                    .map { it.toDomain() }
                    .groupBy { it.workoutId.lowercase() }

                val remoteWorkouts = workoutsDto.map { dto ->
                    val sets = setsByWorkout[dto.id.lowercase()] ?: emptyList()
                    dto.toDomain(sets)
                }

                val sortedWorkouts = remoteWorkouts.reversed().sortedByDescending { it.date }

                if (remoteWorkouts.isNotEmpty()) {
                    inMemoryWorkouts.clear()
                    inMemoryWorkouts.addAll(sortedWorkouts)
                    sortedWorkouts
                } else {
                    inMemoryWorkouts.reversed().sortedByDescending { it.date }
                }
            } catch (e: Exception) {
                println("Error fetching workouts from Supabase: ${e.message}")
                inMemoryWorkouts.reversed().sortedByDescending { it.date }
            }
        }
    }

    override suspend fun getWorkoutById(id: String): Result<Workout> {
        return runCatching {
            val sanitizedId = UuidUtil.ensureUuid(id)

            // 1. Buscar en memoria primero si ya contiene las series
            val inMemoryMatch = inMemoryWorkouts.find {
                it.id.equals(sanitizedId, ignoreCase = true) || it.id.equals(id, ignoreCase = true)
            }
            if (inMemoryMatch != null && inMemoryMatch.sets.isNotEmpty()) {
                return@runCatching inMemoryMatch
            }

            // 2. Si no está en memoria o está sin series, consultar directamente a Supabase
            try {
                val remoteDto = supabaseClient.from("entrenamientos").select {
                    filter {
                        or {
                            eq("id", sanitizedId)
                            eq("id", id)
                        }
                    }
                }.decodeSingleOrNull<WorkoutDto>()

                if (remoteDto != null) {
                    val remoteSetsDto = supabaseClient.from("series_realizadas").select {
                        filter {
                            or {
                                eq("entrenamiento_id", sanitizedId)
                                eq("entrenamiento_id", remoteDto.id)
                                eq("entrenamiento_id", id)
                            }
                        }
                    }.decodeList<WorkoutSetDto>()

                    val remoteSets = remoteSetsDto.map { it.toDomain() }
                    val loadedWorkout = remoteDto.toDomain(remoteSets)

                    inMemoryWorkouts.removeAll {
                        it.id.equals(sanitizedId, ignoreCase = true) || it.id.equals(id, ignoreCase = true)
                    }
                    inMemoryWorkouts.add(0, loadedWorkout)
                    return@runCatching loadedWorkout
                }
            } catch (e: Exception) {
                println("Error fetching single workout from Supabase: ${e.message}")
            }

            // 3. Si aún no se encontró, recargar la lista completa de Supabase
            if (inMemoryMatch == null) {
                val freshList = getWorkouts().getOrDefault(emptyList())
                val freshMatch = freshList.find {
                    it.id.equals(sanitizedId, ignoreCase = true) || it.id.equals(id, ignoreCase = true)
                }
                if (freshMatch != null) {
                    return@runCatching freshMatch
                }
            }

            // 4. Fallback al objeto en memoria si existe o error controlado
            inMemoryMatch ?: throw NoSuchElementException("No se encontró el entrenamiento con ID: $id")
        }
    }

    override suspend fun saveWorkout(workout: Workout): Result<Workout> {
        return runCatching {
            val validWorkoutId = UuidUtil.ensureUuid(workout.id)
            val validSets = workout.sets.mapIndexed { index, s ->
                s.copy(
                    id = UuidUtil.ensureUuid(s.id),
                    workoutId = validWorkoutId,
                    exerciseId = UuidUtil.ensureUuid(s.exerciseId),
                    setNumber = index + 1
                )
            }
            val sanitizedWorkout = workout.copy(id = validWorkoutId, sets = validSets)
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id

            try {
                // Upsert del entrenamiento padre en Supabase
                supabaseClient.from("entrenamientos").upsert(sanitizedWorkout.toDto(currentUserId))

                // Eliminamos series previas del entrenamiento si existen y reinsertamos las actuales
                supabaseClient.from("series_realizadas").delete {
                    filter {
                        eq("entrenamiento_id", validWorkoutId)
                    }
                }

                if (validSets.isNotEmpty()) {
                    val setsDto = validSets.map { it.toDto(currentUserId) }
                    supabaseClient.from("series_realizadas").insert(setsDto)
                }
                println("Successfully saved workout to Supabase: ${sanitizedWorkout.id}")
            } catch (e: Exception) {
                println("Error saving workout to Supabase: ${e.message}")
                e.printStackTrace()
            }

            // Actualizar caché en memoria
            inMemoryWorkouts.removeAll { it.id.equals(validWorkoutId, ignoreCase = true) || it.id.equals(workout.id, ignoreCase = true) }
            inMemoryWorkouts.add(0, sanitizedWorkout)
            sanitizedWorkout
        }
    }

    override suspend fun deleteWorkout(id: String): Result<Unit> {
        return runCatching {
            val validWorkoutId = UuidUtil.ensureUuid(id)
            try {
                supabaseClient.from("series_realizadas").delete {
                    filter { eq("entrenamiento_id", validWorkoutId) }
                }
                supabaseClient.from("entrenamientos").delete {
                    filter { eq("id", validWorkoutId) }
                }
            } catch (e: Exception) {
                println("Error deleting workout in Supabase: ${e.message}")
            }

            inMemoryWorkouts.removeAll { it.id == validWorkoutId || it.id == id }
            Unit
        }
    }
}

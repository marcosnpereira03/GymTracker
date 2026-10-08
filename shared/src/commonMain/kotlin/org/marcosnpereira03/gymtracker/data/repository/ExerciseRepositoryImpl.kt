package org.marcosnpereira03.gymtracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import org.marcosnpereira03.gymtracker.data.mapper.toDomain
import org.marcosnpereira03.gymtracker.data.mapper.toDto
import org.marcosnpereira03.gymtracker.data.remote.dto.ExerciseDto
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository

import org.marcosnpereira03.gymtracker.domain.util.UuidUtil

/**
 * Implementación de ExerciseRepository conectada a Supabase Postgrest con resiliencia offline.
 */
class ExerciseRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : ExerciseRepository {

    // Catálogo por defecto con UUIDs deterministas para arranque rápido y compatibilidad con Foreign Keys de Supabase
    private val defaultExercises = listOf(
        Exercise(id = UuidUtil.ensureUuid("ex-1"), name = "Press de Banca Plano", muscleGroup = "Pecho"),
        Exercise(id = UuidUtil.ensureUuid("ex-2"), name = "Press Inclinado con Mancuernas", muscleGroup = "Pecho"),
        Exercise(id = UuidUtil.ensureUuid("ex-3"), name = "Aperturas en Polea (Cruces)", muscleGroup = "Pecho"),
        Exercise(id = UuidUtil.ensureUuid("ex-4"), name = "Sentadilla con Barra (Back Squat)", muscleGroup = "Piernas"),
        Exercise(id = UuidUtil.ensureUuid("ex-5"), name = "Prensa de Piernas 45°", muscleGroup = "Piernas"),
        Exercise(id = UuidUtil.ensureUuid("ex-6"), name = "Extensión de Cuádriceps", muscleGroup = "Piernas"),
        Exercise(id = UuidUtil.ensureUuid("ex-7"), name = "Curl Femoral Tumbado", muscleGroup = "Piernas"),
        Exercise(id = UuidUtil.ensureUuid("ex-8"), name = "Dominadas Lastradas", muscleGroup = "Espalda"),
        Exercise(id = UuidUtil.ensureUuid("ex-9"), name = "Remo con Barra", muscleGroup = "Espalda"),
        Exercise(id = UuidUtil.ensureUuid("ex-10"), name = "Jalón al Pecho", muscleGroup = "Espalda"),
        Exercise(id = UuidUtil.ensureUuid("ex-11"), name = "Peso Muerto Convencional", muscleGroup = "Espalda"),
        Exercise(id = UuidUtil.ensureUuid("ex-12"), name = "Press Militar con Barra (Overhead)", muscleGroup = "Hombros"),
        Exercise(id = UuidUtil.ensureUuid("ex-13"), name = "Elevaciones Laterales", muscleGroup = "Hombros"),
        Exercise(id = UuidUtil.ensureUuid("ex-14"), name = "Pájaros / Deltoides Posterior", muscleGroup = "Hombros"),
        Exercise(id = UuidUtil.ensureUuid("ex-15"), name = "Curl de Bíceps con Barra Z", muscleGroup = "Bíceps"),
        Exercise(id = UuidUtil.ensureUuid("ex-16"), name = "Curl Martillo", muscleGroup = "Bíceps"),
        Exercise(id = UuidUtil.ensureUuid("ex-17"), name = "Press Francés", muscleGroup = "Tríceps"),
        Exercise(id = UuidUtil.ensureUuid("ex-18"), name = "Extensión de Tríceps en Polea Alta", muscleGroup = "Tríceps")
    )

    private val inMemoryCache = mutableListOf<Exercise>().apply {
        addAll(defaultExercises)
    }

    override suspend fun getExercises(): Result<List<Exercise>> {
        return runCatching {
            try {
                val remoteList = supabaseClient.from("ejercicios")
                    .select()
                    .decodeList<ExerciseDto>()
                    .map { it.toDomain() }

                if (remoteList.isNotEmpty()) {
                    inMemoryCache.clear()
                    inMemoryCache.addAll(remoteList.sortedBy { it.name.lowercase() })
                    inMemoryCache.toList()
                } else {
                    // Si Supabase no tiene ejercicios aún para este usuario, auto-sembrar los iniciales
                    val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
                    if (currentUserId != null) {
                        try {
                            val defaultDtos = defaultExercises.map { it.toDto(currentUserId) }
                            supabaseClient.from("ejercicios").upsert(defaultDtos)
                        } catch (e: Exception) {
                            println("Supabase seed error: ${e.message}")
                        }
                    }
                    inMemoryCache.sortedBy { it.name.lowercase() }
                }
            } catch (e: Exception) {
                println("Error fetching exercises from Supabase: ${e.message}")
                // Si la red falla, respondemos con la caché resiliente
                inMemoryCache.sortedBy { it.name.lowercase() }
            }
        }
    }

    override suspend fun searchExercises(query: String): Result<List<Exercise>> {
        return runCatching {
            val all = getExercises().getOrDefault(inMemoryCache.toList())
            if (query.isBlank()) {
                all
            } else {
                all.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.muscleGroup.contains(query, ignoreCase = true)
                }
            }
        }
    }

    override suspend fun getExerciseById(id: String): Result<Exercise> {
        return runCatching {
            inMemoryCache.find { it.id == id }
                ?: getExercises().getOrThrow().first { it.id == id }
        }
    }

    override suspend fun createExercise(exercise: Exercise): Result<Exercise> {
        return runCatching {
            val validExercise = exercise.copy(id = UuidUtil.ensureUuid(exercise.id))
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            try {
                supabaseClient.from("ejercicios").insert(validExercise.toDto(currentUserId))
            } catch (e: Exception) {
                println("Error inserting exercise to Supabase: ${e.message}")
            }
            inMemoryCache.removeAll { it.id == validExercise.id || it.id == exercise.id }
            inMemoryCache.add(validExercise)
            validExercise
        }
    }

    override suspend fun updateExercise(exercise: Exercise): Result<Exercise> {
        return runCatching {
            val validId = UuidUtil.ensureUuid(exercise.id)
            val validExercise = exercise.copy(id = validId)
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            try {
                supabaseClient.from("ejercicios").update(validExercise.toDto(currentUserId)) {
                    filter {
                        eq("id", validId)
                    }
                }
            } catch (e: Exception) {
                println("Error updating exercise in Supabase: ${e.message}")
            }
            inMemoryCache.removeAll { it.id == validId || it.id == exercise.id }
            inMemoryCache.add(validExercise)
            validExercise
        }
    }

    override suspend fun deleteExercise(id: String): Result<Unit> {
        return runCatching {
            val validId = UuidUtil.ensureUuid(id)
            try {
                supabaseClient.from("ejercicios").delete {
                    filter {
                        eq("id", validId)
                    }
                }
            } catch (e: Exception) {
                println("Error deleting exercise from Supabase: ${e.message}")
            }
            inMemoryCache.removeAll { it.id == validId || it.id == id }
            Unit
        }
    }
}

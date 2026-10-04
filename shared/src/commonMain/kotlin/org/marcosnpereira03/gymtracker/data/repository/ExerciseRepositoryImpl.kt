package org.marcosnpereira03.gymtracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import org.marcosnpereira03.gymtracker.data.mapper.toDomain
import org.marcosnpereira03.gymtracker.data.mapper.toDto
import org.marcosnpereira03.gymtracker.data.remote.dto.ExerciseDto
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository

/**
 * Implementación de ExerciseRepository conectada a Supabase Postgrest con resiliencia offline.
 */
class ExerciseRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : ExerciseRepository {

    // Catálogo por defecto para arranque rápido y modo offline/fallback
    private val defaultExercises = listOf(
        Exercise(id = "ex-1", name = "Press de Banca Plano", muscleGroup = "Pecho"),
        Exercise(id = "ex-2", name = "Press Inclinado con Mancuernas", muscleGroup = "Pecho"),
        Exercise(id = "ex-3", name = "Aperturas en Polea (Cruces)", muscleGroup = "Pecho"),
        Exercise(id = "ex-4", name = "Sentadilla con Barra (Back Squat)", muscleGroup = "Piernas"),
        Exercise(id = "ex-5", name = "Prensa de Piernas 45°", muscleGroup = "Piernas"),
        Exercise(id = "ex-6", name = "Extensión de Cuádriceps", muscleGroup = "Piernas"),
        Exercise(id = "ex-7", name = "Curl Femoral Tumbado", muscleGroup = "Piernas"),
        Exercise(id = "ex-8", name = "Dominadas Lastradas", muscleGroup = "Espalda"),
        Exercise(id = "ex-9", name = "Remo con Barra", muscleGroup = "Espalda"),
        Exercise(id = "ex-10", name = "Jalón al Pecho", muscleGroup = "Espalda"),
        Exercise(id = "ex-11", name = "Peso Muerto Convencional", muscleGroup = "Espalda"),
        Exercise(id = "ex-12", name = "Press Militar con Barra (Overhead)", muscleGroup = "Hombros"),
        Exercise(id = "ex-13", name = "Elevaciones Laterales", muscleGroup = "Hombros"),
        Exercise(id = "ex-14", name = "Pájaros / Deltoides Posterior", muscleGroup = "Hombros"),
        Exercise(id = "ex-15", name = "Curl de Bíceps con Barra Z", muscleGroup = "Bíceps"),
        Exercise(id = "ex-16", name = "Curl Martillo", muscleGroup = "Bíceps"),
        Exercise(id = "ex-17", name = "Press Francés", muscleGroup = "Tríceps"),
        Exercise(id = "ex-18", name = "Extensión de Tríceps en Polea Alta", muscleGroup = "Tríceps")
    )

    private val inMemoryCache = mutableListOf<Exercise>().apply {
        addAll(defaultExercises)
    }

    override suspend fun getExercises(): Result<List<Exercise>> {
        return runCatching {
            try {
                val remoteList = supabaseClient.from("exercises")
                    .select()
                    .decodeList<ExerciseDto>()
                    .map { it.toDomain() }

                if (remoteList.isNotEmpty()) {
                    inMemoryCache.clear()
                    inMemoryCache.addAll(remoteList)
                    remoteList
                } else {
                    inMemoryCache.toList()
                }
            } catch (_: Exception) {
                // Si la red falla o Supabase no está configurado, respondemos con la caché resiliente
                inMemoryCache.toList()
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
            try {
                supabaseClient.from("exercises").insert(exercise.toDto())
            } catch (_: Exception) {
                // Fallback local si falla la red
            }
            inMemoryCache.removeAll { it.id == exercise.id }
            inMemoryCache.add(exercise)
            exercise
        }
    }
}

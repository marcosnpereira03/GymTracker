package org.marcosnpereira03.gymtracker.domain.repository

import org.marcosnpereira03.gymtracker.domain.model.Exercise

/**
 * Contrato de acceso al catálogo de ejercicios.
 */
interface ExerciseRepository {
    /**
     * Obtiene el catálogo completo de ejercicios disponibles.
     */
    suspend fun getExercises(): Result<List<Exercise>>

    /**
     * Busca ejercicios por coincidencia de nombre o grupo muscular.
     */
    suspend fun searchExercises(query: String): Result<List<Exercise>>

    /**
     * Obtiene un ejercicio por su ID.
     */
    suspend fun getExerciseById(id: String): Result<Exercise>

    /**
     * Crea un nuevo ejercicio personalizado en el catálogo.
     */
    suspend fun createExercise(exercise: Exercise): Result<Exercise>
}

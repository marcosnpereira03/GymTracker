package org.marcosnpereira03.gymtracker.domain.repository

import org.marcosnpereira03.gymtracker.domain.model.Workout

/**
 * Contrato de acceso a datos para los entrenamientos y sus series.
 * Sigue la convención de Clean Architecture devolviendo Result<T> para encapsular éxitos y errores.
 */
interface WorkoutRepository {
    /**
     * Obtiene la lista completa de entrenamientos con sus series,
     * ordenados por fecha de realización descendente.
     */
    suspend fun getWorkouts(): Result<List<Workout>>

    /**
     * Obtiene el detalle de un entrenamiento específico por su identificador.
     */
    suspend fun getWorkoutById(id: String): Result<Workout>

    /**
     * Guarda o actualiza un entrenamiento y sus series asociadas.
     */
    suspend fun saveWorkout(workout: Workout): Result<Workout>

    /**
     * Elimina un entrenamiento y sus series por su identificador.
     */
    suspend fun deleteWorkout(id: String): Result<Unit>
}

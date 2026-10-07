package org.marcosnpereira03.gymtracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object para la tabla 'series_realizadas' en Supabase.
 */
@Serializable
data class WorkoutSetDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("user_id")
    val userId: String? = null,
    
    @SerialName("entrenamiento_id")
    val workoutId: String,
    
    @SerialName("ejercicio_id")
    val exerciseId: String,
    
    @SerialName("numero_serie")
    val setNumber: Int,
    
    @SerialName("peso_kg")
    val weightKg: Double,
    
    @SerialName("repeticiones")
    val reps: Int,
    
    @SerialName("rir")
    val rir: Int,
    
    @SerialName("observaciones")
    val notes: String? = null
)

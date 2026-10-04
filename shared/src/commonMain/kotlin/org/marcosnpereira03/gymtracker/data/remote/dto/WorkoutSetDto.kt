package org.marcosnpereira03.gymtracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object para la tabla 'workout_sets' en Supabase.
 */
@Serializable
data class WorkoutSetDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("workout_id")
    val workoutId: String,
    
    @SerialName("exercise_id")
    val exerciseId: String,
    
    @SerialName("set_number")
    val setNumber: Int,
    
    @SerialName("weight_kg")
    val weightKg: Double,
    
    @SerialName("reps")
    val reps: Int,
    
    @SerialName("rir")
    val rir: Int
)

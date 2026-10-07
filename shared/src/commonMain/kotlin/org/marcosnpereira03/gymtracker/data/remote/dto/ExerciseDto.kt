package org.marcosnpereira03.gymtracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object para la tabla 'ejercicios' en Supabase.
 */
@Serializable
data class ExerciseDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("user_id")
    val userId: String? = null,
    
    @SerialName("nombre")
    val name: String,
    
    @SerialName("grupo_muscular")
    val muscleGroup: String,
    
    @SerialName("equipamiento")
    val equipment: String? = null,
    
    @SerialName("notas")
    val notes: String? = null
)

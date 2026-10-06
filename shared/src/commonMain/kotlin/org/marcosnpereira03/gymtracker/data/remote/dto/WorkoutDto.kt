package org.marcosnpereira03.gymtracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object para la tabla 'entrenamientos' en Supabase.
 */
@Serializable
data class WorkoutDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("user_id")
    val userId: String? = null,
    
    @SerialName("fecha")
    val date: String,
    
    @SerialName("nombre_sesion")
    val title: String,
    
    @SerialName("observaciones")
    val notes: String? = null
)

package org.marcosnpereira03.gymtracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object para la tabla 'workouts' en Supabase.
 */
@Serializable
data class WorkoutDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("title")
    val title: String,
    
    @SerialName("date")
    val date: String,
    
    @SerialName("body_weight")
    val bodyWeight: Double? = null,
    
    @SerialName("notes")
    val notes: String? = null
)

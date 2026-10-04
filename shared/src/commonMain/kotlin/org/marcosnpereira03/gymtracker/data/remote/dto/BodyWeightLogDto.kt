package org.marcosnpereira03.gymtracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object para la tabla 'body_weight_logs' en Supabase.
 */
@Serializable
data class BodyWeightLogDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("date")
    val date: String,
    
    @SerialName("weight_kg")
    val weightKg: Double,
    
    @SerialName("notes")
    val notes: String? = null
)

package org.marcosnpereira03.gymtracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object para la tabla 'pesajes' en Supabase.
 */
@Serializable
data class BodyWeightLogDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("user_id")
    val userId: String? = null,
    
    @SerialName("fecha")
    val date: String,
    
    @SerialName("peso_kg")
    val weightKg: Double
)

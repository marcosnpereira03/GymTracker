package org.marcosnpereira03.gymtracker.domain.model

import kotlin.time.Instant

/**
 * Registro individual del peso corporal a lo largo del tiempo.
 *
 * @property id Identificador único del registro.
 * @property date Fecha y hora de la medición.
 * @property weightKg Peso corporal en kilogramos.
 * @property notes Notas adicionales (ej. "En ayunas", "Post entrenamiento").
 */
data class BodyWeightLog(
    val id: String,
    val date: Instant,
    val weightKg: Double,
    val notes: String? = null
)

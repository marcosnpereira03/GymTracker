package org.marcosnpereira03.gymtracker.presentation.profile

import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume

enum class VolumePeriod(val label: String) {
    DAILY("Diario"),
    WEEKLY("Semanal"),
    MONTHLY("Mensual")
}

/**
 * Estado UI inmutable para la pantalla de Pesajes y Perfil / Analíticas.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val weightLogs: List<BodyWeightLog> = emptyList(),
    val latestWeight: BodyWeightLog? = null,
    val muscleGroupVolumes: List<MuscleGroupVolume> = emptyList(),
    val selectedPeriod: VolumePeriod = VolumePeriod.WEEKLY,
    val totalPeriodVolumeKg: Double = 0.0,
    val isLoggingWeight: Boolean = false,
    val errorMessage: String? = null
)

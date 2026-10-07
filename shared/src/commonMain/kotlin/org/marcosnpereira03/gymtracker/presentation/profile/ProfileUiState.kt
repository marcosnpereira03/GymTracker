package org.marcosnpereira03.gymtracker.presentation.profile

import org.marcosnpereira03.gymtracker.domain.model.AuthUser
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume
import org.marcosnpereira03.gymtracker.domain.model.PersonalRecord

enum class VolumePeriod(val label: String) {
    DAILY("Diario"),
    WEEKLY("Semanal"),
    MONTHLY("Mensual"),
    ALL_TIME("Siempre")
}

enum class ProfileTab(val title: String) {
    STATS("Estadísticas"),
    PRS("Récords (PRs)"),
    WEIGHT_LOGS("Pesajes")
}

/**
 * Estado UI inmutable para la pantalla de Perfil, Pesajes y Estadísticas.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val currentUser: AuthUser? = null,
    val totalWorkoutsCount: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val weightLogs: List<BodyWeightLog> = emptyList(),
    val filteredWeightLogs: List<BodyWeightLog> = emptyList(),
    val latestWeight: BodyWeightLog? = null,
    val previousWeight: BodyWeightLog? = null,
    val totalWeightLostKg: Double? = null,
    val personalRecords: List<PersonalRecord> = emptyList(),
    val muscleGroupVolumes: List<MuscleGroupVolume> = emptyList(),
    val selectedPeriod: VolumePeriod = VolumePeriod.WEEKLY,
    val periodOffset: Int = 0,
    val periodRangeLabel: String = "",
    val totalPeriodVolumeKg: Double = 0.0,
    val weightPeriod: VolumePeriod = VolumePeriod.MONTHLY,
    val weightPeriodOffset: Int = 0,
    val weightPeriodRangeLabel: String = "",
    val activeTab: ProfileTab = ProfileTab.STATS,
    val isSavingProfile: Boolean = false,
    val isLoggingWeight: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canNavigateForward: Boolean
        get() = selectedPeriod != VolumePeriod.ALL_TIME && periodOffset < 0

    val canNavigateWeightForward: Boolean
        get() = weightPeriod != VolumePeriod.ALL_TIME && weightPeriodOffset < 0
}

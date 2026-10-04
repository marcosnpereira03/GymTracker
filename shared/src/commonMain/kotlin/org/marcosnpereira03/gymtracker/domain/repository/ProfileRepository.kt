package org.marcosnpereira03.gymtracker.domain.repository

import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog

/**
 * Contrato de acceso a datos para registros de peso corporal del usuario.
 */
interface ProfileRepository {
    /**
     * Obtiene el historial de registros de peso corporal ordenados por fecha descendente.
     */
    suspend fun getBodyWeightLogs(): Result<List<BodyWeightLog>>

    /**
     * Guarda un nuevo registro de peso corporal.
     */
    suspend fun saveBodyWeightLog(log: BodyWeightLog): Result<BodyWeightLog>

    /**
     * Elimina un registro de peso por su ID.
     */
    suspend fun deleteBodyWeightLog(id: String): Result<Unit>
}

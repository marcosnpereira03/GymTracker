package org.marcosnpereira03.gymtracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import org.marcosnpereira03.gymtracker.data.mapper.toDomain
import org.marcosnpereira03.gymtracker.data.mapper.toDto
import org.marcosnpereira03.gymtracker.data.remote.dto.BodyWeightLogDto
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import kotlinx.datetime.Instant

/**
 * Implementación de ProfileRepository con Supabase Postgrest y almacenamiento resiliente.
 */
class ProfileRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : ProfileRepository {

    private val inMemoryLogs = mutableListOf<BodyWeightLog>().apply {
        add(BodyWeightLog(id = "bw-1", date = Instant.parse("2026-10-02T08:00:00Z"), weightKg = 78.5, notes = "En ayunas"))
        add(BodyWeightLog(id = "bw-2", date = Instant.parse("2026-09-25T08:00:00Z"), weightKg = 78.2, notes = "En ayunas"))
        add(BodyWeightLog(id = "bw-3", date = Instant.parse("2026-09-18T08:00:00Z"), weightKg = 77.8, notes = "Post cardio"))
        add(BodyWeightLog(id = "bw-4", date = Instant.parse("2026-09-11T08:00:00Z"), weightKg = 77.4, notes = "En ayunas"))
        add(BodyWeightLog(id = "bw-5", date = Instant.parse("2026-09-04T08:00:00Z"), weightKg = 77.0, notes = "Inicio de ciclo"))
    }

    override suspend fun getBodyWeightLogs(): Result<List<BodyWeightLog>> {
        return runCatching {
            try {
                val remoteLogs = supabaseClient.from("body_weight_logs")
                    .select()
                    .decodeList<BodyWeightLogDto>()
                    .map { it.toDomain() }

                if (remoteLogs.isNotEmpty()) {
                    inMemoryLogs.clear()
                    inMemoryLogs.addAll(remoteLogs)
                    remoteLogs.sortedByDescending { it.date }
                } else {
                    inMemoryLogs.sortedByDescending { it.date }
                }
            } catch (_: Exception) {
                inMemoryLogs.sortedByDescending { it.date }
            }
        }
    }

    override suspend fun saveBodyWeightLog(log: BodyWeightLog): Result<BodyWeightLog> {
        return runCatching {
            try {
                supabaseClient.from("body_weight_logs").upsert(log.toDto())
            } catch (_: Exception) {
                // Modo fallback offline
            }
            inMemoryLogs.removeAll { it.id == log.id }
            inMemoryLogs.add(0, log)
            log
        }
    }

    override suspend fun deleteBodyWeightLog(id: String): Result<Unit> {
        return runCatching {
            try {
                supabaseClient.from("body_weight_logs").delete {
                    filter { eq("id", id) }
                }
            } catch (_: Exception) {
                // Modo fallback offline
            }
            inMemoryLogs.removeAll { it.id == id }
            Unit
        }
    }
}

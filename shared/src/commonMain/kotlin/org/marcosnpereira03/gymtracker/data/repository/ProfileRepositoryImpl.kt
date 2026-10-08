package org.marcosnpereira03.gymtracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import org.marcosnpereira03.gymtracker.data.mapper.toDomain
import org.marcosnpereira03.gymtracker.data.mapper.toDto
import org.marcosnpereira03.gymtracker.data.remote.dto.BodyWeightLogDto
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import kotlin.time.Instant

import org.marcosnpereira03.gymtracker.domain.util.UuidUtil

/**
 * Implementación de ProfileRepository con Supabase Postgrest y almacenamiento resiliente.
 */
class ProfileRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : ProfileRepository {

    private val inMemoryLogs = mutableListOf<BodyWeightLog>()

    override suspend fun getBodyWeightLogs(): Result<List<BodyWeightLog>> {
        return runCatching {
            try {
                val remoteLogs = supabaseClient.from("pesajes")
                    .select()
                    .decodeList<BodyWeightLogDto>()
                    .map { it.toDomain() }

                val sortedLogs = remoteLogs.reversed().sortedByDescending { it.date }

                inMemoryLogs.clear()
                inMemoryLogs.addAll(sortedLogs)
                sortedLogs
            } catch (e: Exception) {
                println("Error fetching body weight logs from Supabase: ${e.message}")
                inMemoryLogs.reversed().sortedByDescending { it.date }
            }
        }
    }

    override suspend fun saveBodyWeightLog(log: BodyWeightLog): Result<BodyWeightLog> {
        return runCatching {
            val validLog = log.copy(id = UuidUtil.ensureUuid(log.id))
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            try {
                supabaseClient.from("pesajes").upsert(validLog.toDto(currentUserId))
                println("Successfully saved body weight log to Supabase: ${validLog.id}")
            } catch (e: Exception) {
                println("Error saving body weight log to Supabase: ${e.message}")
                e.printStackTrace()
            }
            inMemoryLogs.removeAll { it.id == validLog.id || it.id == log.id }
            inMemoryLogs.add(0, validLog)
            validLog
        }
    }

    override suspend fun deleteBodyWeightLog(id: String): Result<Unit> {
        return runCatching {
            val validId = UuidUtil.ensureUuid(id)
            try {
                supabaseClient.from("pesajes").delete {
                    filter { eq("id", validId) }
                }
            } catch (e: Exception) {
                println("Error deleting body weight log in Supabase: ${e.message}")
            }
            inMemoryLogs.removeAll { it.id == validId || it.id == id }
            return@runCatching Unit
        }
    }
}


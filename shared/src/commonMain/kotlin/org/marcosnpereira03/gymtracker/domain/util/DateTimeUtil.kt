package org.marcosnpereira03.gymtracker.domain.util

import kotlinx.datetime.Instant

/**
 * Utilidad multiplataforma para generación de timestamps y UUIDs sin dependencias de JVM.
 */
object DateTimeUtil {
    fun now(): Instant {
        return Instant.fromEpochMilliseconds(kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds + 1760000000000L)
    }

    fun currentTimestampString(): String {
        return now().toString()
    }
}

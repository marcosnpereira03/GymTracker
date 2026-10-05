package org.marcosnpereira03.gymtracker.domain.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utilidad multiplataforma para generación de timestamps usando kotlinx.datetime.
 */
object DateTimeUtil {
    fun now(): Instant {
        return kotlin.time.Clock.System.now()
    }

    fun today(): LocalDate {
        return now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun currentTimestampString(): String {
        return now().toString()
    }

    fun parseDateOrNow(dateString: String?): Instant {
        if (dateString.isNullOrBlank()) return now()
        return try {
            if (dateString.contains("T")) {
                Instant.parse(dateString)
            } else {
                Instant.parse("${dateString}T12:00:00Z")
            }
        } catch (e: Exception) {
            now()
        }
    }

    fun formatHeaderDate(instant: Instant = now()): String {
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val dayName = when (dt.dayOfWeek.name) {
            "MONDAY" -> "Lunes"
            "TUESDAY" -> "Martes"
            "WEDNESDAY" -> "Miércoles"
            "THURSDAY" -> "Jueves"
            "FRIDAY" -> "Viernes"
            "SATURDAY" -> "Sábado"
            "SUNDAY" -> "Domingo"
            else -> "Hoy"
        }
        val monthName = when (dt.month.name) {
            "JANUARY" -> "Enero"
            "FEBRUARY" -> "Febrero"
            "MARCH" -> "Marzo"
            "APRIL" -> "Abril"
            "MAY" -> "Mayo"
            "JUNE" -> "Junio"
            "JULY" -> "Julio"
            "AUGUST" -> "Agosto"
            "SEPTEMBER" -> "Septiembre"
            "OCTOBER" -> "Octubre"
            "NOVEMBER" -> "Noviembre"
            "DECEMBER" -> "Diciembre"
            else -> ""
        }
        return "$dayName, ${dt.day} De $monthName"
    }
}


package org.marcosnpereira03.gymtracker.domain.util

import kotlin.time.Clock
import kotlin.time.Instant
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

    fun shortMonthName(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "Ene"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Abr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Ago"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dic"
            else -> ""
        }
    }

    fun fullMonthName(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Septiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> ""
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
        val monthName = fullMonthName(dt.month.ordinal + 1)
        return "$dayName, ${dt.day} De $monthName"
    }

    fun formatShortDate(instant: Instant): String {
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.day} ${shortMonthName(dt.month.ordinal + 1).lowercase()}"
    }

    fun formatFullShortDate(instant: Instant): String {
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.day} ${shortMonthName(dt.month.ordinal + 1).lowercase()} ${dt.year}"
    }
}


package org.marcosnpereira03.gymtracker.domain.util

import kotlin.random.Random

/**
 * Utilidad multiplataforma (KMP) para generar y validar UUIDs v4 compatibles con PostgreSQL y Supabase.
 */
object UuidUtil {

    private val hexChars = "0123456789abcdef".toCharArray()

    /**
     * Genera un UUID v4 pseudo-aleatorio estándar RFC 4122 (ej: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d").
     */
    fun randomUuid(): String {
        val randomBytes = Random.nextBytes(16)
        // Set version to 4
        randomBytes[6] = (randomBytes[6].toInt() and 0x0f or 0x40).toByte()
        // Set variant to RFC 4122 (10xxxxxx)
        randomBytes[8] = (randomBytes[8].toInt() and 0x3f or 0x80).toByte()

        val sb = StringBuilder(36)
        for (i in 0 until 16) {
            if (i == 4 || i == 6 || i == 8 || i == 10) {
                sb.append('-')
            }
            val b = randomBytes[i].toInt() and 0xff
            sb.append(hexChars[b ushr 4])
            sb.append(hexChars[b and 0x0f])
        }
        return sb.toString()
    }

    /**
     * Verifica si una cadena de texto tiene el formato de un UUID válido (36 caracteres con 4 guiones).
     */
    fun isValidUuid(input: String?): Boolean {
        if (input == null || input.length != 36) return false
        val parts = input.split('-')
        if (parts.size != 5) return false
        return parts[0].length == 8 &&
                parts[1].length == 4 &&
                parts[2].length == 4 &&
                parts[3].length == 4 &&
                parts[4].length == 12 &&
                input.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' || it == '-' }
    }

    /**
     * Asegura que un identificador sea un UUID válido. Si no lo es, genera un UUID determinista a partir de la semilla.
     */
    fun ensureUuid(input: String?): String {
        if (input != null && isValidUuid(input)) {
            return input.lowercase()
        }
        if (input.isNullOrBlank()) {
            return randomUuid()
        }
        // Generar UUID determinista basado en el hash del string
        val hash = input.hashCode().toLong()
        val randomBytes = ByteArray(16)
        for (i in 0 until 8) {
            randomBytes[i] = ((hash ushr (i * 8)) and 0xff).toByte()
            randomBytes[i + 8] = (((hash * 31 + i) ushr (i * 8)) and 0xff).toByte()
        }
        randomBytes[6] = (randomBytes[6].toInt() and 0x0f or 0x40).toByte()
        randomBytes[8] = (randomBytes[8].toInt() and 0x3f or 0x80).toByte()

        val sb = StringBuilder(36)
        for (i in 0 until 16) {
            if (i == 4 || i == 6 || i == 8 || i == 10) {
                sb.append('-')
            }
            val b = randomBytes[i].toInt() and 0xff
            sb.append(hexChars[b ushr 4])
            sb.append(hexChars[b and 0x0f])
        }
        return sb.toString()
    }
}

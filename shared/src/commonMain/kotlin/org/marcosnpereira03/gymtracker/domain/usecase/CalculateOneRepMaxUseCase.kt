package org.marcosnpereira03.gymtracker.domain.usecase

import kotlin.math.round

/**
 * Caso de uso para calcular el 1RM (Una Repetición Máxima) estimado.
 *
 * Utiliza la fórmula de Epley ajustada por RIR (Reps In Reserve).
 * Las repeticiones efectivas proyectadas al fallo son r_eff = reps + rir.
 *
 * Fórmula:
 *   1RM = weight_kg * (1 + (reps + rir) / 30.0)
 *
 * Casos borde:
 * - Si reps <= 0 o weightKg <= 0.0 -> devuelve 0.0
 * - Si reps == 1 y rir == 0 -> devuelve directamente weightKg
 */
class CalculateOneRepMaxUseCase {

    operator fun invoke(weightKg: Double, reps: Int, rir: Int = 0): Double {
        if (weightKg <= 0.0 || reps <= 0) return 0.0
        val effectiveRir = rir.coerceAtLeast(0)
        
        if (reps == 1 && effectiveRir == 0) {
            return round(weightKg * 10.0) / 10.0
        }

        val effectiveReps = reps + effectiveRir
        val raw1Rm = weightKg * (1.0 + (effectiveReps.toDouble() / 30.0))
        return round(raw1Rm * 10.0) / 10.0
    }
}

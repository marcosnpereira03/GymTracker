package org.marcosnpereira03.gymtracker.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateOneRepMaxUseCaseTest {

    private val calculateOneRepMax = CalculateOneRepMaxUseCase()

    @Test
    fun single_rep_with_zero_rir_returns_exact_weight() {
        // 1 repetición con RIR 0 es exactamente el 1RM real
        val result = calculateOneRepMax(weightKg = 100.0, reps = 1, rir = 0)
        assertEquals(100.0, result)
    }

    @Test
    fun standard_epley_formula_calculation() {
        // 100 kg x 10 reps a RIR 0 -> 100 * (1 + 10/30) = 100 * 1.3333 = 133.3 kg
        val result = calculateOneRepMax(weightKg = 100.0, reps = 10, rir = 0)
        assertEquals(133.3, result)
    }

    @Test
    fun calculation_adjusted_by_rir() {
        // 100 kg x 8 reps con RIR 2 -> effective reps = 10 -> 100 * (1 + 10/30) = 133.3 kg
        val result = calculateOneRepMax(weightKg = 100.0, reps = 8, rir = 2)
        assertEquals(133.3, result)
    }

    @Test
    fun invalid_or_zero_weight_returns_zero() {
        assertEquals(0.0, calculateOneRepMax(weightKg = 0.0, reps = 10, rir = 0))
        assertEquals(0.0, calculateOneRepMax(weightKg = -50.0, reps = 10, rir = 0))
    }

    @Test
    fun invalid_or_zero_reps_returns_zero() {
        assertEquals(0.0, calculateOneRepMax(weightKg = 100.0, reps = 0, rir = 0))
        assertEquals(0.0, calculateOneRepMax(weightKg = 100.0, reps = -2, rir = 0))
    }
}

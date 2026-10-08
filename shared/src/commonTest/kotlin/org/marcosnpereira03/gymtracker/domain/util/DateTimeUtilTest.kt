package org.marcosnpereira03.gymtracker.domain.util

import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DateTimeUtilTest {

    @Test
    fun `parseDateOrNow correctly parses ISO dates and fallback`() {
        val isoDate = "2026-05-10T15:30:00Z"
        val parsed = DateTimeUtil.parseDateOrNow(isoDate)
        assertEquals(Instant.parse(isoDate), parsed)

        val simpleDate = "2026-05-10"
        val parsedSimple = DateTimeUtil.parseDateOrNow(simpleDate)
        assertEquals(Instant.parse("2026-05-10T12:00:00Z"), parsedSimple)

        val invalidDate = "invalid-date-string"
        val parsedInvalid = DateTimeUtil.parseDateOrNow(invalidDate)
        assertNotNull(parsedInvalid)

        val nullDate = DateTimeUtil.parseDateOrNow(null)
        assertNotNull(nullDate)
    }

    @Test
    fun `shortMonthName and fullMonthName return correct names in Spanish`() {
        assertEquals("Ene", DateTimeUtil.shortMonthName(1))
        assertEquals("Dic", DateTimeUtil.shortMonthName(12))
        assertEquals("", DateTimeUtil.shortMonthName(13))

        assertEquals("Enero", DateTimeUtil.fullMonthName(1))
        assertEquals("Diciembre", DateTimeUtil.fullMonthName(12))
        assertEquals("Octubre", DateTimeUtil.fullMonthName(10))
    }

    @Test
    fun `today returns a valid LocalDate`() {
        val today = DateTimeUtil.today()
        assertTrue(today.year >= 2026)
        assertTrue((today.month.ordinal + 1) in 1..12)
        assertTrue(today.day in 1..31)
    }
}


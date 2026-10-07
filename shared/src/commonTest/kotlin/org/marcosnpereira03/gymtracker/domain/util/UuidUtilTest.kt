package org.marcosnpereira03.gymtracker.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UuidUtilTest {

    @Test
    fun `randomUuid generates a valid RFC 4122 v4 UUID`() {
        val uuid = UuidUtil.randomUuid()
        assertTrue(UuidUtil.isValidUuid(uuid))
        assertEquals(36, uuid.length)
        assertEquals('4', uuid[14]) // UUID Version 4
    }

    @Test
    fun `isValidUuid validates properly`() {
        assertTrue(UuidUtil.isValidUuid("123e4567-e89b-12d3-a456-426614174000"))
        assertTrue(UuidUtil.isValidUuid("9B1DEB4D-3B7D-4BAD-9BDD-2B0D7B3DCB6D"))

        assertFalse(UuidUtil.isValidUuid("not-a-uuid"))
        assertFalse(UuidUtil.isValidUuid(null))
        assertFalse(UuidUtil.isValidUuid("123e4567-e89b-12d3-a456"))
        assertFalse(UuidUtil.isValidUuid("123e4567-e89b-12d3-a456-42661417400Z"))
    }

    @Test
    fun `ensureUuid preserves valid UUIDs and transforms non-valid ones`() {
        val validUuid = "123e4567-e89b-12d3-a456-426614174000"
        assertEquals(validUuid, UuidUtil.ensureUuid(validUuid))

        val seedString = "workout-sample-id"
        val ensured = UuidUtil.ensureUuid(seedString)
        assertTrue(UuidUtil.isValidUuid(ensured))

        // Deterministic check: same seed generates same UUID
        assertEquals(ensured, UuidUtil.ensureUuid(seedString))
    }
}

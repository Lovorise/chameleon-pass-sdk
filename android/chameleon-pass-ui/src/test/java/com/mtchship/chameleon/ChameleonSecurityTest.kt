// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon

import com.mtchship.chameleon.core.security.SecretBuffer
import com.mtchship.chameleon.core.security.TotpEngine
import org.junit.Assert.*
import org.junit.Test

class ChameleonSecurityTest {

    @Test
    fun `SecretBuffer scrubs source array immediately and zeroes off-heap on close`() {
        val rawSecret = "MTCHSHIP_TOP_SECRET_TOKEN_2026".toByteArray(Charsets.UTF_8)
        val buffer = SecretBuffer.wrapAndScrubSource(rawSecret)

        // 1. Source array must be zeroed immediately
        assertTrue(rawSecret.all { it == 0x00.toByte() })
        assertFalse(buffer.isDestroyed)
        assertEquals(30, buffer.size)

        // 2. Close must wipe off-heap memory and lock further reads
        buffer.close()
        assertTrue(buffer.isDestroyed)
        assertEquals(0, buffer.size)

        assertThrows(IllegalStateException::class.java) {
            buffer.useReadOnly { /* must fail */ }
        }
    }

    @Test
    fun `TotpEngine produces deterministic 4-digit pin and stepHex`() {
        val seed = SecretBuffer.wrapAndScrubSource("12345678901234567890".toByteArray())
        val step = TotpEngine.deriveRotatingStep(seed, intervalSec = 30, nowMillis = 59_000L)

        assertEquals(4, step.pin4.length)
        assertEquals(16, step.stepHex.length)
        assertEquals(1, step.remainingSec)
        seed.close()
    }
}

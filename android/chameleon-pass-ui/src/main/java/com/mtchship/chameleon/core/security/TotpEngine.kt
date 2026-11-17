// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.core.security

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class RotatingStepResult(
    val stepHex: String,
    val pin4: String,
    val remainingSec: Int
)

object TotpEngine {
    /**
     * Derives an HMAC-SHA256 rotating optical step directly from off-heap SecretBuffer
     * and scrubs all transient byte arrays before returning.
     */
    fun deriveRotatingStep(
        seed: SecretBuffer,
        intervalSec: Int = 15,
        nowMillis: Long = System.currentTimeMillis()
    ): RotatingStepResult {
        val safeInterval = intervalSec.coerceAtLeast(1)
        val epochSec = nowMillis / 1000L
        val counter = epochSec / safeInterval
        val remaining = (safeInterval - (epochSec % safeInterval)).toInt()

        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()
        var keyBytes: ByteArray? = null
        var macBytes: ByteArray? = null

        return try {
            keyBytes = seed.useReadOnly { buf ->
                ByteArray(buf.remaining()).also { buf.get(it) }
            }
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(keyBytes, "HmacSHA256"))
            macBytes = mac.doFinal(counterBytes)

            val offset = macBytes.last().toInt() and 0x0F
            val binary = ((macBytes[offset].toInt() and 0x7F) shl 24) or
                ((macBytes[offset + 1].toInt() and 0xFF) shl 16) or
                ((macBytes[offset + 2].toInt() and 0xFF) shl 8) or
                (macBytes[offset + 3].toInt() and 0xFF)

            val pin4 = (binary % 10_000).toString().padStart(4, '0')
            val hex = macBytes.take(8).joinToString("") { "%02x".format(it) }

            RotatingStepResult(stepHex = hex, pin4 = pin4, remainingSec = remaining)
        } finally {
            counterBytes.fill(0x00)
            keyBytes?.fill(0x00)
            macBytes?.fill(0x00)
        }
    }
}

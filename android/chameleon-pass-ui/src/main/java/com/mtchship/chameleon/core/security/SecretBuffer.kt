// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.core.security

import java.io.Closeable
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Volatile, auto-scrubbing memory container for sensitive optical tokens and HMAC seeds.
 * Uses DirectByteBuffer (off-heap) to avoid ART compacting GC heap duplication.
 */
class SecretBuffer private constructor(
    private val directBuffer: ByteBuffer
) : Closeable {

    private val isWiped = AtomicBoolean(false)

    val size: Int
        get() = if (isWiped.get()) 0 else directBuffer.capacity()

    val isDestroyed: Boolean
        get() = isWiped.get()

    /**
     * Scopes read-only access to the underlying off-heap buffer.
     * Throws IllegalStateException if buffer was already scrubbed.
     */
    inline fun <T> useReadOnly(block: (ByteBuffer) -> T): T {
        check(!isDestroyed) { "Attempted to read a scrubbed SecretBuffer." }
        return block(directBuffer.asReadOnlyBuffer().apply { rewind() })
    }

    /**
     * Deterministically zeroes out the memory buffer (0x00).
     * Safe to call multiple times (idempotent).
     */
    override fun close() {
        if (isWiped.compareAndSet(false, true)) {
            directBuffer.clear()
            for (i in 0 until directBuffer.capacity()) {
                directBuffer.put(i, 0x00.toByte())
            }
        }
    }

    override fun toString(): String = "SecretBuffer(size=$size, wiped=${isWiped.get()})"

    companion object {
        /**
         * Copies source bytes into off-heap memory and immediately scrubs the source array.
         */
        fun wrapAndScrubSource(source: ByteArray): SecretBuffer {
            val direct = ByteBuffer.allocateDirect(source.size)
            direct.put(source)
            direct.flip()
            source.fill(0x00.toByte())
            return SecretBuffer(direct)
        }
    }
}

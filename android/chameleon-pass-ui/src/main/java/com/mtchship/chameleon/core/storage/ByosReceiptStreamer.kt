// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.core.storage

import com.mtchship.chameleon.core.model.ChameleonReceipt
import com.mtchship.chameleon.core.model.ClientStoragePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

object ByosReceiptStreamer {
    /**
     * Streams a signed JWS receipt directly from volatile RAM to a customer-owned
     * S3/GCS/Azure/R2 pre-signed URL without touching disk cache.
     */
    suspend fun emitReceipt(
        jwsBytes: ByteArray,
        policy: ClientStoragePolicy,
        zeroedAfter: Boolean
    ): ChameleonReceipt = withContext(Dispatchers.IO) {
        val jwsString = String(jwsBytes, Charsets.UTF_8)
        try {
            if (policy is ClientStoragePolicy.Byos) {
                val conn = (URL(policy.presignedUploadUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    doOutput = true
                    useCaches = false
                    connectTimeout = 5_000
                    readTimeout = 5_000
                    setRequestProperty("Content-Type", "application/jose")
                    setRequestProperty("Cache-Control", "no-store")
                    if (policy.provider == ClientStoragePolicy.ByosProvider.AZURE) {
                        setRequestProperty("x-ms-blob-type", "BlockBlob")
                    }
                }
                conn.outputStream.use { out ->
                    out.write(jwsBytes)
                    out.flush()
                }
                val code = conn.responseCode
                check(code in 200..299) { "BYOS receipt upload failed with HTTP $code" }
            }

            ChameleonReceipt(
                jwsToken = jwsString,
                zeroed = zeroedAfter,
                storageMode = if (policy is ClientStoragePolicy.Byos) "byos" else "none",
                timestamp = Instant.now()
            )
        } finally {
            jwsBytes.fill(0x00)
        }
    }
}

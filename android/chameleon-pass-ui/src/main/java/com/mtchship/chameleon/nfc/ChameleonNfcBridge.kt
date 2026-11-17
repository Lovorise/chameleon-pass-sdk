// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.nfc

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.mtchship.chameleon.core.security.SecretBuffer
import java.util.concurrent.atomic.AtomicReference

enum class NfcHardwareStatus {
    READY,
    DISABLED_IN_SETTINGS,
    UNSUPPORTED
}

object ChameleonNfcRegistry {
    private val activeToken = AtomicReference<SecretBuffer?>(null)
    private val activeDynamicStep = AtomicReference<ByteArray?>(null)
    private var onHandoffCompleted: (() -> Unit)? = null

    fun checkHardwareStatus(context: Context): NfcHardwareStatus {
        val adapter = NfcAdapter.getDefaultAdapter(context) ?: return NfcHardwareStatus.UNSUPPORTED
        return if (adapter.isEnabled) NfcHardwareStatus.READY else NfcHardwareStatus.DISABLED_IN_SETTINGS
    }

    fun bindActiveCredential(
        token: SecretBuffer?,
        rotatingStepBytes: ByteArray? = null,
        onHandoff: (() -> Unit)? = null
    ) {
        activeToken.set(token)
        activeDynamicStep.getAndSet(rotatingStepBytes)?.fill(0x00)
        onHandoffCompleted = onHandoff
    }

    fun clearCredential() {
        activeToken.set(null)
        activeDynamicStep.getAndSet(null)?.fill(0x00)
        onHandoffCompleted = null
    }

    internal fun readApduPayload(): ByteArray? {
        val secret = activeToken.get() ?: return null
        if (secret.isDestroyed) return null

        return secret.useReadOnly { buf ->
            val baseBytes = ByteArray(buf.remaining()).also { buf.get(it) }
            val step = activeDynamicStep.get()
            if (step != null && step.isNotEmpty()) {
                val combined = baseBytes + step
                baseBytes.fill(0x00)
                combined
            } else {
                baseBytes
            }
        }
    }

    internal fun notifyHandoffSuccess() {
        onHandoffCompleted?.invoke()
    }
}

class ChameleonHceService : HostApduService() {

    companion object {
        private val SW_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val SW_CONDITIONS_NOT_SATISFIED = byteArrayOf(0x69.toByte(), 0x85.toByte())
        private val SW_INS_NOT_SUPPORTED = byteArrayOf(0x6D.toByte(), 0x00.toByte())

        private val SELECT_APDU_HEADER = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00)
        private val GET_CREDENTIAL_HEADER = byteArrayOf(0x80.toByte(), 0xCA.toByte(), 0x00, 0x00)
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null || commandApdu.size < 4) {
            return SW_INS_NOT_SUPPORTED
        }

        if (commandApdu.startsWith(SELECT_APDU_HEADER)) {
            val hasValidPass = ChameleonNfcRegistry.readApduPayload()?.also { it.fill(0x00) } != null
            return if (hasValidPass) SW_OK else SW_CONDITIONS_NOT_SATISFIED
        }

        if (commandApdu.startsWith(GET_CREDENTIAL_HEADER)) {
            val payload = ChameleonNfcRegistry.readApduPayload()
                ?: return SW_CONDITIONS_NOT_SATISFIED

            return try {
                val response = ByteArray(payload.size + SW_OK.size)
                System.arraycopy(payload, 0, response, 0, payload.size)
                System.arraycopy(SW_OK, 0, response, payload.size, SW_OK.size)
                ChameleonNfcRegistry.notifyHandoffSuccess()
                response
            } finally {
                payload.fill(0x00)
            }
        }

        return SW_INS_NOT_SUPPORTED
    }

    override fun onDeactivated(reason: Int) {
        ChameleonNfcRegistry.bindActiveCredential(null)
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (this.size < prefix.size) return false
        for (i in prefix.indices) {
            if (this[i] != prefix[i]) return false
        }
        return true
    }
}

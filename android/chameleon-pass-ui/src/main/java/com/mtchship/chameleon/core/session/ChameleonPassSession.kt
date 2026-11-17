// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.core.session

import com.mtchship.chameleon.core.i18n.ChameleonI18n
import com.mtchship.chameleon.core.i18n.PassUiLabelOverrides
import com.mtchship.chameleon.core.i18n.PassUiLabels
import com.mtchship.chameleon.core.model.ChameleonPassPayload
import com.mtchship.chameleon.core.model.ColorSchemeMode
import com.mtchship.chameleon.core.model.PassStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

data class ChameleonSessionUiState(
    val activeIndex: Int = 0,
    val activeModeId: String? = null,
    val locale: String,
    val colorScheme: ColorSchemeMode,
    val labels: PassUiLabels,
    val passes: List<ChameleonPassPayload>,
    val isDestroyed: Boolean = false
) {
    val currentPass: ChameleonPassPayload?
        get() = passes.getOrNull(activeIndex)
}

class ChameleonPassSession(
    initialPasses: List<ChameleonPassPayload>,
    initialLocale: String = ChameleonI18n.detectDeviceLocale(),
    private val labelOverrides: PassUiLabelOverrides? = null,
    initialColorScheme: ColorSchemeMode = ColorSchemeMode.AUTO,
    val keepScreenAwake: Boolean = true
) : Closeable {

    private val destroyed = AtomicBoolean(false)
    private val _state = MutableStateFlow(
        ChameleonSessionUiState(
            activeIndex = 0,
            activeModeId = null,
            locale = initialLocale,
            colorScheme = initialColorScheme,
            labels = ChameleonI18n.resolveLabels(initialLocale, labelOverrides),
            passes = initialPasses
        )
    )
    val state: StateFlow<ChameleonSessionUiState> = _state.asStateFlow()

    fun nextPass() = updateIfAlive { s ->
        if (s.passes.isEmpty()) s else s.copy(
            activeIndex = (s.activeIndex + 1) % s.passes.size,
            activeModeId = null
        )
    }

    fun prevPass() = updateIfAlive { s ->
        if (s.passes.isEmpty()) s else s.copy(
            activeIndex = (s.activeIndex - 1 + s.passes.size) % s.passes.size,
            activeModeId = null
        )
    }

    fun goToPass(index: Int) = updateIfAlive { s ->
        if (index in s.passes.indices) s.copy(activeIndex = index, activeModeId = null) else s
    }

    fun switchMode(modeId: String?) = updateIfAlive { s ->
        s.copy(activeModeId = modeId)
    }

    fun updateStatus(passIndex: Int, status: PassStatus) = updateIfAlive { s ->
        if (passIndex !in s.passes.indices) return@updateIfAlive s
        val updated = s.passes.toMutableList()
        val old = updated[passIndex]
        if (status.isInvalidated()) {
            old.close()
        }
        updated[passIndex] = old.copyWithStatus(status)
        s.copy(passes = updated)
    }

    fun setLocale(locale: String) = updateIfAlive { s ->
        s.copy(
            locale = locale,
            labels = ChameleonI18n.resolveLabels(locale, labelOverrides)
        )
    }

    fun setColorScheme(scheme: ColorSchemeMode) = updateIfAlive { s ->
        s.copy(colorScheme = scheme)
    }

    fun destroy() = close()

    override fun close() {
        if (destroyed.compareAndSet(false, true)) {
            _state.value.passes.forEach { it.close() }
            _state.update { it.copy(passes = emptyList(), isDestroyed = true) }
        }
    }

    private inline fun updateIfAlive(transform: (ChameleonSessionUiState) -> ChameleonSessionUiState) {
        if (!destroyed.get()) _state.update(transform)
    }
}

// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mtchship.chameleon.core.i18n.ChameleonI18n
import com.mtchship.chameleon.core.model.ChameleonPassConfig
import com.mtchship.chameleon.core.session.ChameleonPassSession

@Composable
fun ChameleonPassHost(
    config: ChameleonPassConfig,
    modifier: Modifier = Modifier,
    onSessionReady: ((ChameleonPassSession) -> Unit)? = null
) {
    val session = remember(config) {
        ChameleonPassSession(
            initialPasses = config.passes,
            initialLocale = config.locale ?: ChameleonI18n.detectDeviceLocale(),
            labelOverrides = config.labels,
            initialColorScheme = config.colorScheme,
            keepScreenAwake = config.keepScreenAwake
        )
    }

    DisposableEffect(session) {
        onSessionReady?.invoke(session)
        onDispose {
            session.destroy()
            config.transport?.close()
            config.onClose?.invoke()
        }
    }

    ChameleonPassCard(
        session = session,
        modifier = modifier,
        onAction = config.onAction
    )
}

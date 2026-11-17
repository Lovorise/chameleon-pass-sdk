// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.compose.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Enforces FLAG_SECURE, FLAG_KEEP_SCREEN_ON, and temporary max brightness (posScannerMode),
 * restoring exact previous window attributes when the pass leaves composition.
 */
@Composable
fun ChameleonWindowSecurityEffect(
    keepScreenAwake: Boolean,
    posScannerMode: Boolean
) {
    val view = LocalView.current

    DisposableEffect(view, keepScreenAwake, posScannerMode) {
        val window = view.context.findActivity()?.window
        val previousBrightness = window?.attributes?.screenBrightness
            ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (posScannerMode && window != null) {
            window.attributes = window.attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            }
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            if (keepScreenAwake) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            if (posScannerMode && window != null) {
                window.attributes = window.attributes.apply {
                    screenBrightness = previousBrightness
                }
            }
        }
    }
}

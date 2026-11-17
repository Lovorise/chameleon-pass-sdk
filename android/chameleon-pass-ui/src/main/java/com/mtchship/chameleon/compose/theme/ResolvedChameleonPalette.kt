// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mtchship.chameleon.core.model.ChameleonPassPayload
import com.mtchship.chameleon.core.model.ColorSchemeMode

@Immutable
data class ResolvedChameleonPalette(
    val isDark: Boolean,
    val accent: Color,
    val accentForeground: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val radius: Dp
)

@Composable
fun resolvePassPalette(
    pass: ChameleonPassPayload,
    sessionScheme: ColorSchemeMode
): ResolvedChameleonPalette {
    val effectiveScheme = pass.theme?.colorScheme?.takeIf { it != ColorSchemeMode.AUTO } ?: sessionScheme
    val isDark = when (effectiveScheme) {
        ColorSchemeMode.DARK -> true
        ColorSchemeMode.LIGHT -> false
        ColorSchemeMode.AUTO -> isSystemInDarkTheme()
    }
    val tokens = pass.theme

    val accent = tokens?.accentColor?.let { Color(it) }
        ?: if (isDark) Color(0xFFF9FAFB) else Color(0xFF111827)

    val accentForeground = if (isDark && tokens?.accentColor == null) {
        Color(0xFF111827)
    } else {
        Color(0xFFFFFFFF)
    }

    return ResolvedChameleonPalette(
        isDark = isDark,
        accent = accent,
        accentForeground = accentForeground,
        surface = tokens?.surfaceColor?.let { Color(it) } ?: if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF),
        text = tokens?.textColor?.let { Color(it) } ?: if (isDark) Color(0xFFF9FAFB) else Color(0xFF111827),
        muted = tokens?.mutedTextColor?.let { Color(it) } ?: if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563),
        border = tokens?.borderColor?.let { Color(it) } ?: if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB),
        radius = tokens?.borderRadiusDp?.dp ?: 16.dp
    )
}

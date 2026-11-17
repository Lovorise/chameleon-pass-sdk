// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import SwiftUI

public struct ResolvedChameleonPalette: Sendable {
    public let isDark: Bool
    public let accent: Color
    public let accentForeground: Color
    public let surface: Color
    public let text: Color
    public let muted: Color
    public let border: Color
    public let radius: CGFloat

    public static func resolve(
        passTheme: ChameleonThemeTokens?,
        sessionScheme: ColorSchemeMode,
        systemScheme: ColorScheme
    ) -> ResolvedChameleonPalette {
        let effectiveScheme: ColorSchemeMode = {
            if let tokenScheme = passTheme?.colorScheme, tokenScheme != .auto {
                return tokenScheme
            }
            return sessionScheme
        }()

        let isDark: Bool = switch effectiveScheme {
        case .dark: true
        case .light: false
        case .auto: systemScheme == .dark
        }

        let accent = passTheme?.accentColor ?? (isDark ? Color(hex: 0xF9FAFB) : Color(hex: 0x111827))
        let accentForeground = (isDark && passTheme?.accentColor == nil) ? Color(hex: 0x111827) : .white

        return ResolvedChameleonPalette(
            isDark: isDark,
            accent: accent,
            accentForeground: accentForeground,
            surface: passTheme?.surfaceColor ?? (isDark ? Color(hex: 0x111827) : .white),
            text: passTheme?.textColor ?? (isDark ? Color(hex: 0xF9FAFB) : Color(hex: 0x111827)),
            muted: passTheme?.mutedTextColor ?? (isDark ? Color(hex: 0x9CA3AF) : Color(hex: 0x4B5563)),
            border: passTheme?.borderColor ?? (isDark ? Color(hex: 0x374151) : Color(hex: 0xE5E7EB)),
            radius: passTheme?.borderRadius ?? 16
        )
    }
}

private extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0
        )
    }
}

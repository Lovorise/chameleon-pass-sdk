// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import Foundation
import UIKit

public enum ColorSchemeMode: String, Sendable, Codable {
    case auto, light, dark
}

public struct AutoContextResult: Sendable, Codable {
    public let locale: String
    public let dir: String
    public let countryCode: String
    public let colorScheme: String
}

public extension PassStatus {
    var isInvalidated: Bool {
        switch self {
        case .transferred, .void, .expired, .redeemed: return true
        case .active, .ready, .pending, .archive: return false
        }
    }
}

public enum ChameleonContextResolver {
    private static let rtlLanguages: Set<String> = ["ar", "he", "fa", "ur"]

    @MainActor
    public static func detectDeviceColorScheme() -> ColorSchemeMode {
        UITraitCollection.current.userInterfaceStyle == .dark ? .dark : .light
    }

    public static func resolveAutoContext(
        endpoint: URL? = nil,
        keyToken: SecretBuffer? = nil
    ) async -> AutoContextResult {
        let deviceLocale = ChameleonI18n.detectDeviceLocale()
        let parts = deviceLocale.split(whereSeparator: { $0 == "-" || $0 == "_" })
        let baseLang = parts.first?.lowercased() ?? "en"
        let dir = rtlLanguages.contains(baseLang) ? "rtl" : "ltr"
        let countryCode = parts.count > 1 ? String(parts[1]).uppercased() : "GB"
        let localScheme = await detectDeviceColorScheme().rawValue

        if let endpoint, let keyToken {
            do {
                var request = URLRequest(url: endpoint.appendingPathComponent("v1/context"))
                request.httpMethod = "GET"
                request.timeoutInterval = 3.0
                request.cachePolicy = .reloadIgnoringLocalAndRemoteCacheData

                try keyToken.withUnsafeBytes { rawBuf in
                    if let tokenStr = String(bytes: rawBuf, encoding: .utf8) {
                        request.setValue("Bearer \(tokenStr)", forHTTPHeaderField: "Authorization")
                    }
                }

                let (data, response) = try await URLSession.shared.data(for: request)
                if let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) {
                    return try JSONDecoder().decode(AutoContextResult.self, from: data)
                }
            } catch {
                // Offline fallback to device context
            }
        }

        return AutoContextResult(
            locale: deviceLocale,
            dir: dir,
            countryCode: countryCode,
            colorScheme: localScheme
        )
    }
}


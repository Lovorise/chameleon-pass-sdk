// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import Foundation
import Observation

@Observable
@MainActor
public final class ChameleonPassSession {
    public private(set) var activeIndex: Int = 0
    public private(set) var activeModeId: String? = nil
    public private(set) var locale: String
    public private(set) var colorScheme: ColorSchemeMode
    public private(set) var labels: PassUiLabels
    public private(set) var passes: [ChameleonPassPayload]
    public private(set) var isDestroyed: Bool = false

    private let labelOverrides: PassUiLabelOverrides?
    public let keepScreenAwake: Bool

    public var currentPass: ChameleonPassPayload? {
        passes.indices.contains(activeIndex) ? passes[activeIndex] : nil
    }

    public init(
        passes: [ChameleonPassPayload],
        locale: String? = nil,
        labels: PassUiLabelOverrides? = nil,
        colorScheme: ColorSchemeMode = .auto,
        keepScreenAwake: Bool = true
    ) {
        let resolvedLocale = locale ?? ChameleonI18n.detectDeviceLocale()
        self.passes = passes
        self.locale = resolvedLocale
        self.labelOverrides = labels
        self.colorScheme = colorScheme
        self.labels = ChameleonI18n.resolveLabels(locale: resolvedLocale, overrides: labels)
        self.keepScreenAwake = keepScreenAwake
    }

    public func nextPass() {
        guard !isDestroyed, !passes.isEmpty else { return }
        activeIndex = (activeIndex + 1) % passes.count
        activeModeId = nil
    }

    public func prevPass() {
        guard !isDestroyed, !passes.isEmpty else { return }
        activeIndex = (activeIndex - 1 + passes.count) % passes.count
        activeModeId = nil
    }

    public func goToPass(_ index: Int) {
        guard !isDestroyed, passes.indices.contains(index) else { return }
        activeIndex = index
        activeModeId = nil
    }

    public func switchMode(_ modeId: String?) {
        guard !isDestroyed else { return }
        activeModeId = modeId
    }

    public func updateStatus(passIndex: Int, status: PassStatus) {
        guard !isDestroyed, passes.indices.contains(passIndex) else { return }
        if status.isInvalidated {
            passes[passIndex].destroyBuffers()
        }
        passes[passIndex].status = status
    }

    public func setLocale(_ newLocale: String) {
        guard !isDestroyed else { return }
        locale = newLocale
        labels = ChameleonI18n.resolveLabels(locale: newLocale, overrides: labelOverrides)
    }

    public func setColorScheme(_ scheme: ColorSchemeMode) {
        guard !isDestroyed else { return }
        colorScheme = scheme
    }

    public func destroy() {
        guard !isDestroyed else { return }
        isDestroyed = true
        for i in passes.indices {
            passes[i].destroyBuffers()
        }
        passes.removeAll()
    }
}

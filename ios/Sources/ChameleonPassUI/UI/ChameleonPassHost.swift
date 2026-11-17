// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import SwiftUI

public struct ChameleonPassConfig: Sendable {
    public let preset: PassPreset
    public let locale: String?
    public let labels: PassUiLabelOverrides?
    public let colorScheme: ColorSchemeMode
    public let storage: ClientStoragePolicy
    public let transport: ChameleonTransport?
    public let passes: [ChameleonPassPayload]
    public let keepScreenAwake: Bool

    public let onAction: (@Sendable (_ actionId: String, _ passIndex: Int) -> Void)?
    public let onForward: (@Sendable (_ passIndex: Int) -> Void)?
    public let onDelegate: (@Sendable (_ passIndex: Int) -> Void)?
    public let onWalletExport: (@Sendable (_ platform: WalletPlatform, _ passIndex: Int) -> Void)?
    public let onComplete: (@Sendable (_ receipt: ChameleonReceipt) -> Void)?
    public let onClose: (@Sendable () -> Void)?

    public init(
        preset: PassPreset,
        locale: String? = nil,
        labels: PassUiLabelOverrides? = nil,
        colorScheme: ColorSchemeMode = .auto,
        storage: ClientStoragePolicy = .none,
        transport: ChameleonTransport? = nil,
        passes: [ChameleonPassPayload],
        keepScreenAwake: Bool = true,
        onAction: (@Sendable (_ actionId: String, _ passIndex: Int) -> Void)? = nil,
        onForward: (@Sendable (_ passIndex: Int) -> Void)? = nil,
        onDelegate: (@Sendable (_ passIndex: Int) -> Void)? = nil,
        onWalletExport: (@Sendable (_ platform: WalletPlatform, _ passIndex: Int) -> Void)? = nil,
        onComplete: (@Sendable (_ receipt: ChameleonReceipt) -> Void)? = nil,
        onClose: (@Sendable () -> Void)? = nil
    ) {
        self.preset = preset
        self.locale = locale
        self.labels = labels
        self.colorScheme = colorScheme
        self.storage = storage
        self.transport = transport
        self.passes = passes
        self.keepScreenAwake = keepScreenAwake
        self.onAction = onAction
        self.onForward = onForward
        self.onDelegate = onDelegate
        self.onWalletExport = onWalletExport
        self.onComplete = onComplete
        self.onClose = onClose
    }
}

public struct ChameleonPassHost: View {
    public let config: ChameleonPassConfig
    public var onSessionReady: ((_ session: ChameleonPassSession) -> Void)?

    @State private var session: ChameleonPassSession

    public init(
        config: ChameleonPassConfig,
        onSessionReady: ((_ session: ChameleonPassSession) -> Void)? = nil
    ) {
        self.config = config
        self.onSessionReady = onSessionReady
        _session = State(
            initialValue: ChameleonPassSession(
                passes: config.passes,
                locale: config.locale,
                labels: config.labels,
                colorScheme: config.colorScheme,
                keepScreenAwake: config.keepScreenAwake
            )
        )
    }

    public var body: some View {
        ChameleonPassView(
            session: session,
            onAction: config.onAction,
            onClose: {
                config.transport?.destroy()
                config.onClose?()
            }
        )
        .onAppear {
            onSessionReady?(session)
        }
    }
}

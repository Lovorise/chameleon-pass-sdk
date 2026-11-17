// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import Foundation
import SwiftUI
import UIKit
import CoreNFC
import Combine

public enum NfcHardwareStatus: Sendable {
    case ready
    case unsupported
}

@Observable
@MainActor
public final class ChameleonHardwareMonitor: NSObject, NFCNDEFReaderSessionDelegate {
    public private(set) var nfcStatus: NfcHardwareStatus
    public private(set) var isScreenCaptured: Bool
    public private(set) var lastNfcHandoffSuccess: Bool = false

    private var nfcSession: NFCNDEFReaderSession?
    private var activeToken: SecretBuffer?
    private var originalBrightness: CGFloat?
    private var cancellables = Set<AnyCancellable>()

    public override init() {
        self.nfcStatus = NFCNDEFReaderSession.readingAvailable ? .ready : .unsupported
        self.isScreenCaptured = UIScreen.main.isCaptured
        super.init()
        NotificationCenter.default.publisher(for: UIScreen.capturedDidChangeNotification)
            .receive(on: RunLoop.main)
            .sink { [weak self] _ in
                self?.isScreenCaptured = UIScreen.main.isCaptured
            }
            .store(in: &cancellables)
    }

    public func applyScannerBrightness(enabled: Bool) {
        if enabled {
            if originalBrightness == nil {
                originalBrightness = UIScreen.main.brightness
            }
            UIScreen.main.brightness = 1.0
        } else {
            restoreBrightness()
        }
    }

    public func restoreBrightness() {
        if let saved = originalBrightness {
            UIScreen.main.brightness = saved
            originalBrightness = nil
        }
    }

    public func startNfcHandoff(token: SecretBuffer, alertMessage: String) {
        guard NFCNDEFReaderSession.readingAvailable, !token.isDestroyed else {
            nfcStatus = .unsupported
            return
        }
        self.activeToken = token
        let session = NFCNDEFReaderSession(
            delegate: self,
            queue: DispatchQueue.global(qos: .userInitiated),
            invalidateAfterFirstRead: true
        )
        session.alertMessage = alertMessage
        session.begin()
        self.nfcSession = session
    }

    public func stopNfcHandoff() {
        nfcSession?.invalidate()
        nfcSession = nil
        activeToken = nil
    }

    nonisolated public func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        Task { @MainActor in
            self.nfcSession = nil
            self.activeToken = nil
        }
    }

    nonisolated public func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        Task { @MainActor in
            self.lastNfcHandoffSuccess = true
            session.invalidate()
        }
    }
}

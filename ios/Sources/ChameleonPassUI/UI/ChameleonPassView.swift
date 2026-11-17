// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import SwiftUI
import UIKit

public struct ChameleonPassView: View {
    @Environment(\.colorScheme) private var systemColorScheme
    @Bindable public var session: ChameleonPassSession
    @State private var hardware = ChameleonHardwareMonitor()
    @State private var rotatingStep: RotatingStepResult?

    public var onAction: ((_ actionId: String, _ passIndex: Int) -> Void)?
    public var onClose: (() -> Void)?

    public init(
        session: ChameleonPassSession,
        onAction: ((_ actionId: String, _ passIndex: Int) -> Void)? = nil,
        onClose: (() -> Void)? = nil
    ) {
        self.session = session
        self.onAction = onAction
        self.onClose = onClose
    }

    public var body: some View {
        if let pass = session.currentPass {
            let layout = pass.layout ?? ChameleonLayoutConfig()
            let palette = ResolvedChameleonPalette.resolve(
                passTheme: pass.theme,
                sessionScheme: session.colorScheme,
                systemScheme: systemColorScheme
            )
            let resolvedDir = ChameleonI18n.resolveLayoutDirection(
                explicitDir: layout.dir,
                locale: session.locale
            )
            let labels = session.labels
            let selectedMode = pass.modes.first { $0.id == session.activeModeId }

            let requestedSymbology = selectedMode?.symbology ?? pass.opticalToken.symbology
            let isNfcReady = requestedSymbology == .nfcHandoff && hardware.nfcStatus == .ready
            let effectiveOpticalSymbology: OpticalSymbology = {
                if requestedSymbology != .nfcHandoff { return requestedSymbology }
                return pass.opticalToken.fallbackSymbology ?? .qr
            }()

            let effectiveToken = selectedMode?.tokenBytes ?? pass.opticalToken.tokenBytes
            let effectiveCode = selectedMode?.humanReadableCode ?? pass.opticalToken.humanReadableCode
            let effectiveWayfinding = (selectedMode?.wayfinding?.isEmpty == false ? selectedMode?.wayfinding : nil) ?? pass.wayfinding
            let invalidated = pass.status.isInvalidated

            VStack(alignment: .leading, spacing: 16) {
                // Header
                if layout.showHeader {
                    HStack {
                        Text(pass.header.issuerName)
                            .font(.footnote.weight(.semibold))
                            .foregroundStyle(palette.muted)
                        Spacer()
                        if layout.showStatusPill {
                            Text(labels.label(for: pass.status))
                                .font(.caption2.weight(.bold))
                                .foregroundStyle(palette.accent)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 4)
                                .background(palette.accent.opacity(0.14), in: Capsule())
                        }
                    }

                    if let credLabel = pass.header.credentialLabel, let credVal = pass.header.credentialValue {
                        HStack(spacing: 6) {
                            Text(credLabel).foregroundStyle(palette.muted)
                            Text(credVal).bold().foregroundStyle(palette.text)
                        }
                        .font(.caption)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text(pass.header.title)
                            .font(.title3.bold())
                            .foregroundStyle(palette.text)
                        if let subtitle = pass.header.subtitle {
                            Text(subtitle)
                                .font(.subheadline)
                                .foregroundStyle(palette.muted)
                        }
                    }
                }

                // Holder
                if layout.showHolder, let holder = pass.holder, (holder.fullName != nil || holder.tierValue != nil) {
                    HStack {
                        if let fullName = holder.fullName {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(labels.holder).font(.caption2).foregroundStyle(palette.muted)
                                Text(fullName).font(.subheadline.weight(.semibold)).foregroundStyle(palette.text)
                            }
                        }
                        Spacer()
                        if let tierValue = holder.tierValue {
                            VStack(alignment: .trailing, spacing: 2) {
                                Text(holder.tierLabel ?? labels.defaultTier).font(.caption2).foregroundStyle(palette.muted)
                                Text(tierValue).font(.subheadline.weight(.semibold)).foregroundStyle(palette.text)
                            }
                        }
                    }
                    .padding(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(palette.border, lineWidth: 1)
                    )
                }

                // Optical & NFC Credential Zone
                VStack(spacing: 8) {
                    if invalidated || hardware.isScreenCaptured {
                        Text(labels.label(for: pass.status))
                            .font(.headline)
                            .foregroundStyle(palette.muted)
                            .frame(maxWidth: .infinity)
                            .padding(24)
                            .background(palette.border.opacity(0.4), in: RoundedRectangle(cornerRadius: 12))
                    } else {
                        if isNfcReady {
                            Button {
                                hardware.startNfcHandoff(token: effectiveToken, alertMessage: labels.nfcReady)
                            } label: {
                                Text(labels.nfcReady)
                                    .font(.caption2.weight(.bold))
                                    .foregroundStyle(palette.accentForeground)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(palette.accent, in: Capsule())
                            }
                            .buttonStyle(.plain)
                        }

                        OpticalBarcodeView(
                            symbology: effectiveOpticalSymbology,
                            tokenBytes: effectiveToken,
                            rotatingStepHex: rotatingStep?.stepHex ?? "",
                            scale: layout.opticalScale
                        )

                        if layout.showHumanReadableCode, let code = effectiveCode, !code.isEmpty {
                            Text(code)
                                .font(.system(.caption, design: .monospaced))
                                .foregroundStyle(palette.text)
                        }

                        if let step = rotatingStep {
                            let pinText = pass.opticalToken.showRotatingPin ? " • PIN \(step.pin4)" : ""
                            Text(labels.formatRefreshesIn(seconds: step.remainingSec) + pinText)
                                .font(.caption)
                                .foregroundStyle(palette.muted)
                        }
                    }
                }
                .frame(maxWidth: .infinity)

                // Wayfinding Grid
                if layout.showWayfinding, !effectiveWayfinding.isEmpty {
                    let cols = Array(repeating: GridItem(.flexible(), spacing: 12), count: layout.wayfindingColumns)
                    LazyVGrid(columns: cols, alignment: .leading, spacing: 12) {
                        ForEach(effectiveWayfinding) { slot in
                            VStack(alignment: .leading, spacing: 2) {
                                Text(slot.label).font(.caption2).foregroundStyle(palette.muted)
                                Text(slot.value)
                                    .font(slot.emphasize ? .body.bold() : .subheadline.weight(.medium))
                                    .foregroundStyle(slot.emphasize ? palette.accent : palette.text)
                            }
                        }
                    }
                }

                // Sub-Modes
                if !pass.modes.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            modePill(title: labels.primaryMode, selected: session.activeModeId == nil, palette: palette) {
                                session.switchMode(nil)
                            }
                            ForEach(pass.modes) { mode in
                                modePill(title: mode.label, selected: session.activeModeId == mode.id, palette: palette) {
                                    session.switchMode(mode.id)
                                }
                            }
                        }
                    }
                }

                // Actions
                if !pass.actions.isEmpty {
                    HStack(spacing: 8) {
                        ForEach(pass.actions) { action in
                            Button {
                                onAction?(action.id, session.activeIndex)
                            } label: {
                                Text(action.label)
                                    .font(.subheadline.weight(.semibold))
                                    .foregroundStyle(palette.accentForeground)

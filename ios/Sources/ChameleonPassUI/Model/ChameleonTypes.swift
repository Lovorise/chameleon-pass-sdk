// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import Foundation
import SwiftUI

public enum PassPreset: String, CaseIterable, Sendable, Codable {
    case custom = "CUSTOM"
    case sportsStadium = "SPORTS_STADIUM"
    case liveEvent = "LIVE_EVENT"
    case attractionPass = "ATTRACTION_PASS"
    case railTransit = "RAIL_TRANSIT"
    case aviationBoarding = "AVIATION_BOARDING"
    case courierParcel = "COURIER_PARCEL"
    case paperlessReturn = "PAPERLESS_RETURN"
    case parcelLocker = "PARCEL_LOCKER"
    case freightManifest = "FREIGHT_MANIFEST"
    case hotelStay = "HOTEL_STAY"
    case accessKey = "ACCESS_KEY"
    case foodOrder = "FOOD_ORDER"
    case tableBooking = "TABLE_BOOKING"
    case retailPickup = "RETAIL_PICKUP"
    case posCheckout = "POS_CHECKOUT"
    case fleetRental = "FLEET_RENTAL"
    case dealerService = "DEALER_SERVICE"
    case rideVoucher = "RIDE_VOUCHER"
    case healthPass = "HEALTH_PASS"
    case financeToken = "FINANCE_TOKEN"
    case civicPermit = "CIVIC_PERMIT"
    case campusId = "CAMPUS_ID"
    case conferenceBadge = "CONFERENCE_BADGE"
    case membershipPass = "MEMBERSHIP_PASS"
    case utilitySession = "UTILITY_SESSION"
    case insuranceCard = "INSURANCE_CARD"
    case telecomEsim = "TELECOM_ESIM"
    case fintechPass = "FINTECH_PASS"
    case bettingSlip = "BETTING_SLIP"
    case fanPass = "FAN_PASS"
}

public enum OpticalSymbology: String, Sendable, Codable {
    case qr
    case aztec
    case pdf417
    case code128
    case ean13
    case datamatrix
    case nfcHandoff = "nfc_handoff"
}

public enum OpticalScale: String, Sendable, Codable {
    case compact, standard, large
}

public enum ActionVariant: String, Sendable, Codable {
    case primary, secondary, danger
}

public enum WalletPlatform: String, Sendable {
    case apple, google, generic
}

public enum ClientStoragePolicy: Sendable {
    case none
    case byos(provider: ByosProvider, presignedUploadUrl: URL)

    public enum ByosProvider: String, Sendable {
        case s3, gcs, azure, r2, custom
    }
}

public final class ChameleonTransport: Sendable {
    public let endpoint: URL
    public let keyToken: SecretBuffer

    public init(endpoint: URL, keyToken: SecretBuffer) {
        self.endpoint = endpoint
        self.keyToken = keyToken
    }

    public func destroy() {
        keyToken.destroy()
    }
}

public struct ChameleonThemeTokens: Sendable {
    public var colorScheme: ColorSchemeMode?
    public var accentColor: Color?
    public var surfaceColor: Color?
    public var textColor: Color?
    public var mutedTextColor: Color?
    public var borderColor: Color?
    public var borderRadius: CGFloat?

    public init(
        colorScheme: ColorSchemeMode? = nil,
        accentColor: Color? = nil,
        surfaceColor: Color? = nil,
        textColor: Color? = nil,
        mutedTextColor: Color? = nil,
        borderColor: Color? = nil,
        borderRadius: CGFloat? = nil
    ) {
        self.colorScheme = colorScheme
        self.accentColor = accentColor
        self.surfaceColor = surfaceColor
        self.textColor = textColor
        self.mutedTextColor = mutedTextColor
        self.borderColor = borderColor
        self.borderRadius = borderRadius
    }
}

public struct ChameleonLayoutConfig: Sendable {
    public var showHeader: Bool
    public var showHolder: Bool
    public var showWayfinding: Bool
    public var showStatusPill: Bool
    public var showHumanReadableCode: Bool
    public var showWatermark: Bool
    public var wayfindingColumns: Int
    public var opticalScale: OpticalScale
    public var dir: ChameleonLayoutDirection

    public init(
        showHeader: Bool = true,
        showHolder: Bool = true,
        showWayfinding: Bool = true,
        showStatusPill: Bool = true,
        showHumanReadableCode: Bool = true,
        showWatermark: Bool = false,
        wayfindingColumns: Int = 3,
        opticalScale: OpticalScale = .standard,
        dir: ChameleonLayoutDirection = .auto
    ) {
        self.showHeader = showHeader
        self.showHolder = showHolder
        self.showWayfinding = showWayfinding
        self.showStatusPill = showStatusPill
        self.showHumanReadableCode = showHumanReadableCode
        self.showWatermark = showWatermark
        self.wayfindingColumns = min(4, max(2, wayfindingColumns))
        self.opticalScale = opticalScale
        self.dir = dir
    }
}

public struct PassFieldSlot: Identifiable, Sendable {
    public let id = UUID()
    public let label: String
    public let value: String
    public let emphasize: Bool

    public init(label: String, value: String, emphasize: Bool = false) {
        self.label = label
        self.value = value
        self.emphasize = emphasize
    }
}

public struct PassActionTrigger: Identifiable, Sendable {
    public let id: String
    public let label: String
    public let variant: ActionVariant

    public init(id: String, label: String, variant: ActionVariant = .secondary) {
        self.id = id
        self.label = label
        self.variant = variant
    }
}

public struct PassSubMode: Identifiable, Sendable {
    public let id: String
    public let label: String
    public let symbology: OpticalSymbology
    public let tokenBytes: SecretBuffer
    public let humanReadableCode: String?
    public let wayfinding: [PassFieldSlot]?

    public init(
        id: String,
        label: String,
        symbology: OpticalSymbology,
        tokenBytes: SecretBuffer,
        humanReadableCode: String? = nil,
        wayfinding: [PassFieldSlot]? = nil
    ) {
        self.id = id
        self.label = label
        self.symbology = symbology
        self.tokenBytes = tokenBytes
        self.humanReadableCode = humanReadableCode
        self.wayfinding = wayfinding
    }
}

public struct OpticalTokenConfig: Sendable {
    public let symbology: OpticalSymbology
    public let fallbackSymbology: OpticalSymbology?
    public let tokenBytes: SecretBuffer
    public let humanReadableCode: String?
    public let hmacSeedBytes: SecretBuffer?
    public let rotationIntervalSec: Int?
    public let showRotatingPin: Bool
    public let posScannerMode: Bool

    public init(
        symbology: OpticalSymbology,
        fallbackSymbology: OpticalSymbology? = nil,
        tokenBytes: SecretBuffer,
        humanReadableCode: String? = nil,
        hmacSeedBytes: SecretBuffer? = nil,
        rotationIntervalSec: Int? = nil,
        showRotatingPin: Bool = false,
        posScannerMode: Bool = false
    ) {
        self.symbology = symbology
        self.fallbackSymbology = fallbackSymbology == .nfcHandoff ? .qr : fallbackSymbology
        self.tokenBytes = tokenBytes
        self.humanReadableCode = humanReadableCode
        self.hmacSeedBytes = hmacSeedBytes
        self.rotationIntervalSec = rotationIntervalSec
        self.showRotatingPin = showRotatingPin
        self.posScannerMode = posScannerMode
    }
}

public struct OptionalVerificationBridge: Sendable {
    public let jwsToken: SecretBuffer?
    public let badgeLabel: String?

    public init(jwsToken: SecretBuffer? = nil, badgeLabel: String? = nil) {
        self.jwsToken = jwsToken
        self.badgeLabel = badgeLabel
    }
}

public struct PassHeader: Sendable {
    public let issuerName: String
    public let issuerLogoUrl: URL?
    public let credentialLabel: String?
    public let credentialValue: String?
    public let title: String
    public let subtitle: String?

    public init(
        issuerName: String,
        issuerLogoUrl: URL? = nil,
        credentialLabel: String? = nil,
        credentialValue: String? = nil,
        title: String,
        subtitle: String? = nil
    ) {
        self.issuerName = issuerName
        self.issuerLogoUrl = issuerLogoUrl
        self.credentialLabel = credentialLabel
        self.credentialValue = credentialValue
        self.title = title
        self.subtitle = subtitle
    }
}

public struct PassHolder: Sendable {
    public let fullName: String?
    public let tierLabel: String?
    public let tierValue: String?

    public init(fullName: String? = nil, tierLabel: String? = nil, tierValue: String? = nil) {
        self.fullName = fullName
        self.tierLabel = tierLabel
        self.tierValue = tierValue
    }
}

public struct ChameleonPassPayload: Identifiable, Sendable {
    public let id = UUID()
    public let schemaVersion: String
    public let preset: PassPreset
    public var status: PassStatus
    public let theme: ChameleonThemeTokens?
    public let layout: ChameleonLayoutConfig?
    public let header: PassHeader
    public let holder: PassHolder?
    public let wayfinding: [PassFieldSlot]
    public let opticalToken: OpticalTokenConfig
    public let modes: [PassSubMode]
    public let actions: [PassActionTrigger]
    public let verificationBridge: OptionalVerificationBridge?

    public init(
        schemaVersion: String = "1.0",
        preset: PassPreset,
        status: PassStatus,
        theme: ChameleonThemeTokens? = nil,
        layout: ChameleonLayoutConfig? = nil,
        header: PassHeader,
        holder: PassHolder? = nil,
        wayfinding: [PassFieldSlot] = [],
        opticalToken: OpticalTokenConfig,
        modes: [PassSubMode] = [],
        actions: [PassActionTrigger] = [],
        verificationBridge: OptionalVerificationBridge? = nil
    ) {
        self.schemaVersion = schemaVersion
        self.preset = preset
        self.status = status
        self.theme = theme
        self.layout = layout
        self.header = header
        self.holder = holder
        self.wayfinding = wayfinding
        self.opticalToken = opticalToken
        self.modes = modes
        self.actions = actions
        self.verificationBridge = verificationBridge
    }

    public func destroyBuffers() {
        opticalToken.tokenBytes.destroy()
        opticalToken.hmacSeedBytes?.destroy()

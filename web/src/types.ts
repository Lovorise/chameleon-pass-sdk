// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

export type PassPreset =
  | 'CUSTOM'
  | 'SPORTS_STADIUM'
  | 'LIVE_EVENT'
  | 'ATTRACTION_PASS'
  | 'RAIL_TRANSIT'
  | 'AVIATION_BOARDING'
  | 'COURIER_PARCEL'
  | 'PAPERLESS_RETURN'
  | 'PARCEL_LOCKER'
  | 'FREIGHT_MANIFEST'
  | 'HOTEL_STAY'
  | 'ACCESS_KEY'
  | 'FOOD_ORDER'
  | 'TABLE_BOOKING'
  | 'RETAIL_PICKUP'
  | 'POS_CHECKOUT'
  | 'FLEET_RENTAL'
  | 'DEALER_SERVICE'
  | 'RIDE_VOUCHER'
  | 'HEALTH_PASS'
  | 'FINANCE_TOKEN'
  | 'CIVIC_PERMIT'
  | 'CAMPUS_ID'
  | 'CONFERENCE_BADGE'
  | 'MEMBERSHIP_PASS'
  | 'UTILITY_SESSION'
  | 'INSURANCE_CARD'
  | 'TELECOM_ESIM'
  | 'FINTECH_PASS'
  | 'BETTING_SLIP'
  | 'FAN_PASS';

export type OpticalSymbology =
  | 'qr'
  | 'aztec'
  | 'pdf417'
  | 'code128'
  | 'ean13'
  | 'datamatrix'
  | 'nfc_handoff';

export type PassStatus =
  | 'active'
  | 'ready'
  | 'pending'
  | 'redeemed'
  | 'transferred'
  | 'archive'
  | 'expired'
  | 'void';

export type ClientStoragePolicy =
  | { mode: 'none' }
  | {
      mode: 'byos';
      provider: 's3' | 'gcs' | 'azure' | 'r2' | 'custom';
      presignedUploadUrl: string;
    };

export interface ChameleonTransport {
  endpoint: string;
  keyToken: string;
}

export interface ChameleonThemeTokens {
  accentColor?: string;
  surfaceColor?: string;
  textColor?: string;
  mutedTextColor?: string;
  borderColor?: string;
  borderRadius?: string;
  fontFamily?: string;
  monospaceFontFamily?: string;
  darkMode?: boolean;
}

export interface ChameleonLayoutConfig {
  showHeader?: boolean;
  showHolder?: boolean;
  showWayfinding?: boolean;
  showStatusPill?: boolean;
  showHumanReadableCode?: boolean;
  showWatermark?: boolean;
  wayfindingColumns?: 2 | 3 | 4;
  opticalScale?: 'compact' | 'standard' | 'large';
  dir?: 'ltr' | 'rtl' | 'auto';
}

export interface PassFieldSlot {
  label: string;
  value: string;
  emphasize?: boolean;
}

export interface PassActionTrigger {
  id: string;
  label: string;
  variant?: 'primary' | 'secondary' | 'danger';
}

export interface PassSubMode {
  id: string;
  label: string;
  symbology: OpticalSymbology;
  tokenBytes: Uint8Array;
  humanReadableCode?: string;
  wayfinding?: PassFieldSlot[];
}

export interface OpticalTokenConfig {
  symbology: OpticalSymbology;
  fallbackSymbology?: Exclude<OpticalSymbology, 'nfc_handoff'>;
  tokenBytes: Uint8Array;
  humanReadableCode?: string;
  hmacSeedBytes?: Uint8Array;
  rotationIntervalSec?: number;
  showRotatingPin?: boolean;
  posScannerMode?: boolean;
}

export interface OptionalVerificationBridge {
  jwsToken?: string;
  badgeLabel?: string;
}

export interface ChameleonPassPayload {
  schemaVersion: '1.0';
  preset: PassPreset;
  status: PassStatus;
  theme?: ChameleonThemeTokens;
  layout?: ChameleonLayoutConfig;
  header: {
    issuerName: string;
    issuerLogoUrl?: string;
    credentialLabel?: string;
    credentialValue?: string;
    title: string;
    subtitle?: string;
  };
  holder?: {
    fullName?: string;
    tierLabel?: string;
    tierValue?: string;
  };
  wayfinding?: PassFieldSlot[];
  opticalToken: OpticalTokenConfig;
  modes?: PassSubMode[];
  actions?: PassActionTrigger[];
  verificationBridge?: OptionalVerificationBridge;
}

export interface ChameleonReceipt {
  jwsToken: string;
  zeroed: boolean;
  storageMode: 'none' | 'byos';
  timestamp: string;
}

export interface AutoContextResult {
  locale: string;
  dir: 'ltr' | 'rtl';
  countryCode: string;
}

export interface ChameleonPassConfig {
  preset: PassPreset;
  locale?: string;
  storage?: ClientStoragePolicy;
  transport?: ChameleonTransport;
  passes: ChameleonPassPayload[];
  keepScreenAwake?: boolean;
  onAction?: (actionId: string, passIndex: number) => void;
  onForward?: (passIndex: number) => void;
  onDelegate?: (passIndex: number) => void;
  onWalletExport?: (platform: 'apple' | 'google' | 'generic', passIndex: number) => void;
  onComplete?: (receipt: ChameleonReceipt) => void;
  onClose?: () => void;
}

export interface ChameleonPassSession {
  nextPass: () => void;
  prevPass: () => void;
  goToPass: (index: number) => void;
  switchMode: (modeId: string) => void;
  updateStatus: (passIndex: number, status: PassStatus) => void;
  setLocale: (locale: string) => void;
  destroy: () => void;
}

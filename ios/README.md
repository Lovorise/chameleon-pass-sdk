# MTCHSHIP — Chameleon Pass & Ticket SDK (iOS)

License: Apache-2.0 • Swift: 6.0 (Strict Concurrency) • UI: SwiftUI • Minimum Deployment: iOS 17.0

Native **Swift & SwiftUI** optical pass, ticket, and CoreNFC handoff module for **MTCHSHIP Visual Clerk** (`ChameleonPassUI`).

---

## Installation (Swift Package Manager)

Add `ChameleonPassUI` as a local or remote Swift Package dependency pointing to the `ios/` directory:

```swift
dependencies: [
    .package(url: "https://github.com", from: "1.0.0")
]
```

*(Note: To enable NFC handoff on supported iPhones, add the `Near Field Communication Tag Reading` capability in Xcode.)*

---

## Quickstart (SwiftUI)

```swift
import SwiftUI
import ChameleonPassUI

struct TicketScreen: View {
    @State private var session: ChameleonPassSession

    init(rawToken: inout Data, rawHmacSeed: inout Data) {
        let tokenBuf = SecretBuffer(consuming: &rawToken)
        let seedBuf = SecretBuffer(consuming: &rawHmacSeed)

        let pass = ChameleonPassPayload(
            preset: .railTransit,
            status: .active,
            header: PassHeader(
                issuerName: "SBB CFF FFS / MTCHSHIP",
                credentialLabel: "TICKET ID",
                credentialValue: "#CH-904821",
                title: "Zürich HB → Genève",
                subtitle: "IC 1 • Valid Today"
            ),
            holder: PassHolder(
                fullName: "ELENA WEBER",
                tierLabel: "KLASSE",
                tierValue: "1. KLASSE"
            ),
            wayfinding: [
                PassFieldSlot(label: "GLEIS", value: "12", emphasize: true),
                PassFieldSlot(label: "WAGEN", value: "04"),
                PassFieldSlot(label: "PLATZ", value: "21A", emphasize: true)
            ],
            opticalToken: OpticalTokenConfig(
                symbology: .nfcHandoff,
                fallbackSymbology: .aztec,
                tokenBytes: tokenBuf,
                hmacSeedBytes: seedBuf,
                rotationIntervalSec: 15,
                showRotatingPin: true,
                posScannerMode: true
            )
        )

        _session = State(initialValue: ChameleonPassSession(passes: [pass]))
    }

    var body: some View {
        ChameleonPassView(session: session)
            .padding()
    }
}
```

---

## iOS Security & Hardware Guarantees

* **Deterministic C `memset_s` Scrubbing:** `SecretBuffer(consuming:)` zeroes your input `Data` buffer immediately upon ingestion and wipes unmanaged heap memory via `memset_s` when the view disappears or `session.destroy()` is called.
* **Screen Capture Guard:** `ChameleonHardwareMonitor` observes `UIScreen.capturedDidChangeNotification` and hides the optical barcode whenever AirPlay mirroring or screen recording is active.
* **POS Scanner Brightness Boost:** Temporarily sets `UIScreen.main.brightness = 1.0` when `posScannerMode` is enabled and restores the user's previous brightness level on `.onDisappear`.
* **CoreNFC + Optical Fallback:** Uses `NFCNDEFReaderSession` when available on device and seamlessly falls back to `fallbackSymbology` (`QR`, `Aztec`, `PDF417`, `Code128`) otherwise.
* **Zero-Disk BYOS Streaming:** `ByosReceiptStreamer` uses `URLSessionConfiguration.ephemeral` with `urlCache = nil` to stream signed JWS receipts directly from volatile RAM to S3/GCS/Azure/R2 pre-signed URLs.

---

## Running Tests

```bash
cd ios
swift test
```

---

## Licence

Copyright © 2026 **Lovorise Ltd** (Registered in England & Wales).  
Licensed under the Apache License, Version 2.0.

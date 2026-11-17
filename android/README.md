# MTCHSHIP — Chameleon Pass & Ticket SDK (Android)

[![License](https://shields.io)](../LICENSE)
[![Kotlin](https://shields.io)](https://kotlinlang.org)
[![Jetpack Compose](https://shields.io)](https://android.com)
[![Min SDK](https://shields.io)](#requirements)

Native **Kotlin & Jetpack Compose (Material 3)** optical pass, ticket, and NFC HCE handoff module for **MTCHSHIP Visual Clerk** (`com.mtchship.chameleon:pass-ui`).

---

## Requirements

* **Minimum SDK:** Android 7.0 (API 24)
* **Compile / Target SDK:** Android 15 (API 35)
* **Kotlin:** 2.1+ with Jetpack Compose Compiler Plugin
* **Hardware (Optional):** NFC HCE (`android.hardware.nfc.hce`) — automatically falls back to optical symbologies (`QR`, `AZTEC`, `PDF417`, `CODE128`, `EAN13`, `DATAMATRIX`) on devices without NFC or when NFC is disabled.

---

## Installation

Add the module or Maven dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.mtchship.chameleon:pass-ui:1.0.0")
}
```

---

## Quickstart (Jetpack Compose)

Wrap raw token bytes in `SecretBuffer.wrapAndScrubSource(...)` — your source `ByteArray` is immediately zeroed (`0x00`) upon ingestion, and the off-heap `DirectByteBuffer` is deterministically scrubbed when the Composable leaves composition or `session.destroy()` is called.

```kotlin
import androidx.compose.runtime.Composable
import com.mtchship.chameleon.compose.ui.ChameleonPassHost
import com.mtchship.chameleon.core.model.*
import com.mtchship.chameleon.core.security.SecretBuffer

@Composable
fun BoardingPassScreen(rawToken: ByteArray, rawHmacSeed: ByteArray) {
    val config = ChameleonPassConfig(
        preset = PassPreset.AVIATION_BOARDING,
        colorScheme = ColorSchemeMode.AUTO, // Follows OS dark/light mode automatically
        keepScreenAwake = true,
        passes = listOf(
            ChameleonPassPayload(
                preset = PassPreset.AVIATION_BOARDING,
                status = PassStatus.ACTIVE,
                header = PassHeader(
                    issuerName = "SWISS / MTCHSHIP",
                    credentialLabel = "BOOKING REF",
                    credentialValue = "LX8492",
                    title = "ZRH → LHR",
                    subtitle = "Flight LX 316 • Terminal E"
                ),
                holder = PassHolder(
                    fullName = "ALEXANDER MEYER",
                    tierLabel = "CABIN",
                    tierValue = "BUSINESS"
                ),
                wayfinding = listOf(
                    PassFieldSlot(label = "GATE", value = "E42", emphasize = true),
                    PassFieldSlot(label = "BOARDING", value = "14:20"),
                    PassFieldSlot(label = "SEAT", value = "04A", emphasize = true)
                ),
                opticalToken = OpticalTokenConfig(
                    symbology = OpticalSymbology.NFC_HANDOFF,
                    fallbackSymbology = OpticalSymbology.AZTEC,
                    tokenBytes = SecretBuffer.wrapAndScrubSource(rawToken),
                    hmacSeedBytes = SecretBuffer.wrapAndScrubSource(rawHmacSeed),
                    rotationIntervalSec = 15,
                    showRotatingPin = true,
                    posScannerMode = true // Boosts screen brightness to 100% while visible
                )
            )
        ),
        onAction = { actionId, passIndex ->
            // Handle custom pass actions
        },
        onClose = {
            // Triggered after volatile RAM buffers are scrubbed
        }
    )

    ChameleonPassHost(config = config)
}
```

---

## Android Security & Hardware Guarantees

| Capability | Implementation Detail |
| :--- | :--- |
| **Off-Heap Volatile Scrubbing** | `SecretBuffer` stores token and HMAC seed bytes in `ByteBuffer.allocateDirect` (off-heap) to prevent Android Runtime (ART) compacting GC heap copies, zeroing every byte (`0x00`) on `close()`. |
| **Screen Capture Protection** | `ChameleonWindowSecurityEffect` automatically applies `WindowManager.LayoutParams.FLAG_SECURE` while a pass is mounted and restores prior window flags on disposal. |
| **POS Scanner Brightness Boost** | When `posScannerMode = true`, window `screenBrightness` is elevated to `BRIGHTNESS_OVERRIDE_FULL` (`1.0f`) and restored to the user's previous brightness level on exit. |
| **Native NFC HCE Bridge** | `ChameleonHceService` (`HostApduService`) serves ISO/IEC 7816-4 APDUs (`AID: F04D54434853484950`) directly from the active `SecretBuffer` without disk persistence, requiring device unlock (`android:requireDeviceUnlock="true"`). |
| **Zero-Disk BYOS Streaming** | `ByosReceiptStreamer` streams signed JWS receipts directly from volatile RAM to customer-controlled S3/GCS/Azure/R2 pre-signed URLs with `useCaches = false` and `Cache-Control: no-store`. |
| **Automatic Locale & RTL** | Resolves device language via `LocaleListCompat` across 10 built-in languages (`en`, `de`, `fr`, `it`, `es`, `pt`, `nl`, `ja`, `zh`, `ar`) and switches Compose `LocalLayoutDirection` automatically for RTL locales. |

---

## Customizing the NFC HCE AID

The library ships with a default `<host-apdu-service>` declaration in its merged manifest for AID `F04D54434853484950` (`MTCHSHIP`). If your Visual Clerk terminal fleet uses a proprietary AID, override `@xml/apduservice` in your host app's `res/xml/apduservice.xml`.

---

## Running Security & Unit Tests

```bash
cd android
./gradlew test
```

---

## Licence

Copyright © 2026 **Lovorise Ltd** (Registered in England & Wales).  
Licensed under the Apache License, Version 2.0.

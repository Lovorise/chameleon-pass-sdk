# MTCHSHIP — Chameleon Pass & Ticket SDK

Open-source, unbranded, fully customizable optical pass, ticket, and handoff UI modules for **MTCHSHIP Visual Clerk** (Web TypeScript, Android Kotlin, iOS Swift). Chameleon renders adaptive passes, tickets, keys, vouchers, and handoff credentials across global deployments, inheriting host application themes and scrubbing cryptographic token buffers in volatile RAM.

---

## Architecture & Guarantees

* **Zero Storage by Default (`storage: { mode: 'none' }`):** Operates as a diskless, stateless data plane with no PII or barcode tokens written to non-volatile storage.
* **Optional Client-Controlled BYOS (`storage: { mode: 'byos' }`):** Stream signed JWS receipts directly from volatile RAM into your designated S3-compatible or cloud bucket via pre-signed URLs.
* **Customer-Owned Control Plane:** Your backend remains the sole control plane for pass issuance, entitlements, and revocation.
* **Memory Lifecycle Protection:** Token and HMAC seed buffers are zeroed out (`0x00`, `Arrays.fill`, or C `memset_s`) immediately upon view disposal, state rotation, or handoff across Web, Android, and iOS.

---

## Repository Structure

* **`web/` (`@mtchship/chameleon-pass-ui`):** TypeScript ESM with closed Shadow DOM `<chameleon-pass>` and `<chameleon-ticket>` components.
* **`android/` (`com.mtchship.chameleon:pass-ui`):** Kotlin and Jetpack Compose (Material 3) with NFC HCE bridge and `FLAG_SECURE` support.
* **`ios/` (`ChameleonPassUI`):** Swift and SwiftUI with CoreNFC/PassKit integration and native memory scrubbing.
* **`COMPLIANCE.md` & `SECURITY.md`:** Detailed architectural threat models, privacy references, and disclosure policies.

---

## Presets & Quickstart

The SDK features **30 built-in industry presets** (spanning live events, aviation, transit, hospitality, and healthcare) alongside a fully flexible `CUSTOM` schema. 

Quickstart examples for **Web (TypeScript)**, **Android (Jetpack Compose)**, and **iOS (SwiftUI)**, as well as architectural trade-offs against standard SaaS platforms, are detailed in the full repository documentation.

---

## Licence

Copyright © 2026 **Lovorise Ltd** (Registered in England & Wales). Licensed under the Apache License, Version 2.0.

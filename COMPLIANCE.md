# Global Compliance & Architectural Reference — Chameleon Pass SDK

This document provides Data Protection Officers (DPOs), Privacy Counsel, InfoSec teams, and global procurement reviewers with architectural statements for Privacy Impact Assessments (PIAs / DPIAs) and vendor due diligence regarding **Chameleon Pass SDK** and its optional **MTCHSHIP Visual Clerk** diskless data plane.

---

## 1. Zero-Custody & Data Minimisation Architecture

Chameleon Pass operates strictly as a **stateless, diskless optical rendering and handoff layer**:

* **Zero Storage by Default (`storage: { mode: 'none' }`):** Neither the open-source client SDK nor the optional MTCHSHIP Visual Clerk data plane persists holder names, seat assignments, itineraries, delivery locations, or optical payloads to non-volatile storage (`localStorage`, `IndexedDB`, SQLite, NVMe, SSD, or HDD).
* **Deterministic `0x00` Memory Sanitization:** All optical payloads (`tokenBytes`), offline cryptographic seeds (`hmacSeedBytes`), and transient buffers are overwritten with `0x00` bytes immediately upon session teardown (`destroy()`), pass forwarding/delegation, or status invalidation.
* **Customer-Owned Control Plane:** Lovorise Ltd operates no hosted ticketing database, passenger manifest store, or multi-tenant pass dashboard. The customer's backend remains the sole data controller and control plane governing pass issuance, entitlements, and revocation.
* **Optional Client-Controlled BYOS (`storage: { mode: 'byos' }`):** Where sectoral or national laws require immutable audit logs (e.g., fiscal POS records, proof-of-delivery receipts, or clinical queue audits), signed JWS receipts (`receipt.jwsToken`) are streamed directly from volatile RAM into the customer's designated regional object storage bucket via short-lived, write-only pre-signed URLs.

---

## 2. Global Privacy & Data Residency Alignment

| Region & Framework | Architectural Alignment |
| :--- | :--- |
| **International Standards (`ISO/IEC 27701`, `ISO/IEC 29100`, `NIST Privacy Framework`)** | Enforces privacy by design, strict data minimisation, and zero persistent data custody across all processing layers. |
| **North America (`CCPA / CPRA`, `US State Privacy Laws`, `PIPEDA`, `HIPAA` architectural alignment)** | Zero sale, sharing, cross-context behavioral profiling, or persistent storage of consumer PII, travel movements, or clinical pass metadata. |
| **Europe & UK (`EU GDPR`, `UK GDPR / DUAA 2025`, `Swiss FADP / revDSG`, `EU Data Act`)** | Eliminates persistent sub-processor storage at rest; Anycast edge routing executes volatile transforms regionally with optional jurisdiction-pinned BYOS (`eu-central-2`, `eu-west-2`, `eu-central-1`). |
| **Asia-Pacific (`Singapore PDPA`, `Japan APPI`, `South Korea PIPA`, `Australia Privacy Act / APP`, `India DPDP Act`)** | Supports strict cross-border transfer controls via edge-local volatile RAM processing (`0x00` scrubbed) and customer-owned regional storage buckets. |
| **Middle East & Africa (`UAE PDPL`, `Saudi Arabia PDPL`, `South Africa POPIA`)** | Stateless transient processing with customer-controlled regional BYOS bucket routing and zero vendor data retention. |
| **Latin America (`Brazil LGPD`, `Mexico LFPDPPP`)** | Zero persistent retention of holder data and open-source auditability under Apache License 2.0. |

---

## 3. Optional Verification Bridge Separation

* **Standalone Operation:** Chameleon Pass processes zero biometric templates, identity documents, or screening inputs.
* **Decoupled JWS Verification Bridge:** When a workflow requires proof of prior identity, age, or license verification alongside a pass, the host application may optionally supply a signed JWS receipt (`verificationBridge.jwsToken`) obtained independently from `@mtchship/chameleon-kyc-ui` or any standards-compliant JWS issuer, keeping identity processing isolated from pass rendering.

---

## 4. Global Accessibility & Physical Scanner Redundancy (`W3C WCAG 2.2 AA`)

* **International Accessibility Conformance (`WCAG 2.2 AA`, `Section 508`, `EN 301 549`):** Exposes semantic ARIA landmarks (`role="region"`), live status announcements, and structured accessible labels for wayfinding attributes (`gate`, `platform`, `seat`, `order`, `room`) compatible with VoiceOver, TalkBack, NVDA, and JAWS.
* **Optical & Manual Redundancy:** Supports human-readable alphanumeric codes and rotating 4-digit handoff PINs alongside all optical symbologies (`QR`, `Aztec`, `PDF417`, `Code 128`, `EAN-13`, `DataMatrix`) to maintain uninterrupted access when device screens are damaged or optical readers are unavailable.

---

## 5. Open-Source Auditability

All client-side rendering, theme token inheritance, wake-lock controls, and memory-scrubbing routines are distributed under the **Apache License 2.0** with **zero third-party runtime dependencies**, supporting full source inspection and SBOM verification worldwide.

# Security Policy & Threat Model — Chameleon Pass SDK

## 1. Volatile Memory Hygiene & Buffer Ownership

**Chameleon Pass SDK** (`@mtchship/chameleon-pass-ui`, `com.mtchship.chameleon:pass-ui`, `ChameleonPassUI`) enforces deterministic zeroing of optical payloads and cryptographic rotation seeds in volatile memory across all supported runtimes:

* **Web (`Uint8Array`):** `tokenBytes` and `hmacSeedBytes` are allocated in typed byte arrays and explicitly overwritten via `Uint8Array.fill(0)` inside `destroy()`, `onForward`, `onDelegate`, and status invalidation (`transferred`, `void`, `expired`, `redeemed`) handlers. No pass state, seed, or holder metadata is persisted to `localStorage`, `sessionStorage`, `IndexedDB`, Cache Storage, or cookies.
* **Android (`ByteArray`):** Transient optical payloads and NFC Host Card Emulation (HCE) APDU buffers are overwritten via `Arrays.fill(bytes, 0.toByte())` inside Jetpack Compose `DisposableEffect` blocks and `Lifecycle.Event.ON_STOP` / `ON_DESTROY` observers. `WindowManager.LayoutParams.FLAG_SECURE` is supported when enabled by the host application.
* **iOS (`Data`):** Raw token and cryptographic seed buffers are scrubbed via C `memset_s` inside `defer` and `deinit` blocks to eliminate Copy-on-Write (CoW) heap retention. Screen-capture state (`UIScreen.capturedDidChangeNotification`) is observed for immediate token revocation.

---

## 2. Mitigated Threat Vectors (OWASP MASVS / ASVS & NIST Aligned)

| Threat Vector | Architectural Mitigation |
| :--- | :--- |
| **Screenshot Reuse & Unauthorized Transfer** | Time-based HMAC-SHA256 rotating optical payloads (`rotationIntervalSec`), live optical watermark cadence, and native OS screenshot protection hooks. |
| **Unverified Handoff & Replay** | Cryptographic safe-handoff mode pairing rotating optical codes with time-bound rotating 4-digit PINs and atomic single-use delegation (`onDelegate`), immediately scrubbing primary token bytes (`0x00`) upon handoff. |
| **DOM / CSS Injection & Optical Spoofing** | Closed Shadow DOM isolation (`attachShadow({ mode: 'closed' })`) blocks host scripts and stylesheets from mutating barcode modules; optical calibration enforces `#FFFFFF` quiet-zone padding (`>= 16px`) and prevents dark-mode color inversion inside the optical viewport. |
| **Supply-Chain & Dependency Compromise** | Zero external runtime dependencies across Web, Android, and iOS; reproducible builds with deterministic artifact hashing and SBOM (CycloneDX / SPDX) generation support under Apache 2.0. |
| **Server-Side Disk Persistence & Cross-Tenant Leakage** | Optional **MTCHSHIP Visual Clerk** CNAME data-plane nodes execute on physically diskless bare-metal hardware (`0x NVMe`, `0x SSD`, `0x HDD`), overwriting host RAM and GPU VRAM pages with `0x00` bytes upon HTTP `2xx` webhook acknowledgment (`zeroed: true`). |

---

## 3. International Supply-Chain & Secure Lifecycle Standards

* **NIST SSDF (SP 800-218) & OWASP SAMM Alignment:** Zero transitive runtime dependencies, deterministic build pipelines, and cryptographic release signing with verifiable SHA-256 checksums.
* **ISO/IEC 27001 & SOC 2 Type II Architectural Alignment:** Stateless, diskless processing eliminates persistent multi-tenant storage exposure at rest.
* **ISO/IEC 29147 Vulnerability Disclosure:** Coordinated global vulnerability intake, CVSS v4.0 severity scoring, and timely security advisories.

---

## 4. Reporting a Vulnerability

Report suspected security vulnerabilities privately to **security@lovorise.com**. Do not open public GitHub issues for unpatched vulnerabilities.

* **Initial Acknowledgment:** Within **24 hours**.
* **Triage & Severity Classification (CVSS v4.0):** Within **72 hours**.
* **Remediation & Coordinated Disclosure:** Patch release and public advisory issued upon verification.

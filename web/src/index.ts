// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import { renderOpticalSvg } from './barcode.js';
import { deriveRotatingStep } from './totp.js';
import type {
  AutoContextResult,
  ChameleonPassConfig,
  ChameleonPassPayload,
  ChameleonPassSession,
  ChameleonReceipt,
  PassStatus,
} from './types.js';

export * from './types.js';

const RTL_LOCALES = new Set(['ar', 'he', 'fa', 'ur']);

function isInvalidatedStatus(status: PassStatus): boolean {
  return (
    status === 'transferred' ||
    status === 'void' ||
    status === 'expired' ||
    status === 'redeemed'
  );
}

function scrubUint8Array(buf?: Uint8Array): void {
  if (buf && buf.byteLength > 0) {
    buf.fill(0);
  }
}

function scrubPassPayload(pass: ChameleonPassPayload): void {
  scrubUint8Array(pass.opticalToken.tokenBytes);
  scrubUint8Array(pass.opticalToken.hmacSeedBytes);
  if (pass.modes) {
    for (const mode of pass.modes) {
      scrubUint8Array(mode.tokenBytes);
    }
  }
}

function escapeHtml(raw: string): string {
  return raw
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

export async function resolveAutoContext(options?: {
  endpoint?: string;
  keyToken?: string;
}): Promise<AutoContextResult> {
  const navLang =
    typeof navigator !== 'undefined' && navigator.language ? navigator.language : 'en-GB';
  const baseLang = navLang.split('-')[0].toLowerCase();
  const dir: 'ltr' | 'rtl' = RTL_LOCALES.has(baseLang) ? 'rtl' : 'ltr';
  const regionPart = navLang.split('-')[1];
  const countryCode = regionPart ? regionPart.toUpperCase() : 'GB';

  if (options?.endpoint && options?.keyToken) {
    try {
      const res = await fetch(`${options.endpoint.replace(/\/$/, '')}/v1/context`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${options.keyToken}`,
        },
      });
      if (res.ok) {
        const data = (await res.json()) as Partial<AutoContextResult>;
        return {
          locale: data.locale ?? navLang,
          dir: data.dir ?? dir,
          countryCode: data.countryCode ?? countryCode,
        };
      }
    } catch {
      // Fall back to local navigator context when offline or unreachable
    }
  }

  return { locale: navLang, dir, countryCode };
}

export function mountChameleonPass(
  container: HTMLElement,
  config: ChameleonPassConfig
): ChameleonPassSession {
  const shadow = container.attachShadow({ mode: 'closed' });
  let activeIndex = 0;
  let activeModeId: string | null = null;
  let activeLocale = config.locale ?? 'en-GB';
  let destroyed = false;
  let timerId: number | null = null;
  let wakeLockSentinel: { release: () => Promise<void> } | null = null;

  const passes = config.passes;

  if (config.keepScreenAwake !== false && typeof navigator !== 'undefined' && 'wakeLock' in navigator) {
    const wl = (navigator as Navigator & {
      wakeLock?: { request: (type: 'screen') => Promise<{ release: () => Promise<void> }> };
    }).wakeLock;
    wl?.request('screen')
      .then((sentinel) => {
        if (destroyed) {
          void sentinel.release();
        } else {
          wakeLockSentinel = sentinel;
        }
      })
      .catch(() => {
        // Ignore wakeLock permission restrictions
      });
  }

  function resolveDir(pass: ChameleonPassPayload): 'ltr' | 'rtl' {
    const configured = pass.layout?.dir ?? 'auto';
    if (configured === 'ltr' || configured === 'rtl') {
      return configured;
    }
    const base = activeLocale.split('-')[0].toLowerCase();
    return RTL_LOCALES.has(base) ? 'rtl' : 'ltr';
  }

  async function render(): Promise<void> {
    if (destroyed || passes.length === 0) {
      return;
    }

    const pass = passes[activeIndex];
    const layout = pass.layout ?? {};
    const theme = pass.theme ?? {};
    const dir = resolveDir(pass);

    const accent = theme.accentColor ?? '#111827';
    const surface = theme.surfaceColor ?? '#FFFFFF';
    const text = theme.textColor ?? '#111827';
    const muted = theme.mutedTextColor ?? '#4B5563';
    const border = theme.borderColor ?? '#E5E7EB';
    const radius = theme.borderRadius ?? '16px';
    const font = theme.fontFamily ?? 'system-ui, -apple-system, sans-serif';
    const monoFont = theme.monospaceFontFamily ?? 'ui-monospace, SFMono-Regular, monospace';

    const selectedMode =
      pass.modes && activeModeId ? pass.modes.find((m) => m.id === activeModeId) ?? null : null;

    const effectiveSymbology = selectedMode
      ? selectedMode.symbology
      : pass.opticalToken.symbology === 'nfc_handoff' && pass.opticalToken.fallbackSymbology
        ? pass.opticalToken.fallbackSymbology
        : pass.opticalToken.symbology;

    const effectiveBytes = selectedMode ? selectedMode.tokenBytes : pass.opticalToken.tokenBytes;
    const effectiveCode = selectedMode
      ? selectedMode.humanReadableCode
      : pass.opticalToken.humanReadableCode;
    const effectiveWayfinding = selectedMode?.wayfinding ?? pass.wayfinding ?? [];

    let stepHex = '';
    let pin4 = '';
    let remainingSec = 0;

    if (
      !isInvalidatedStatus(pass.status) &&
      pass.opticalToken.hmacSeedBytes &&
      pass.opticalToken.hmacSeedBytes.byteLength > 0
    ) {
      const step = await deriveRotatingStep(
        pass.opticalToken.hmacSeedBytes,
        pass.opticalToken.rotationIntervalSec ?? 15
      );
      stepHex = step.stepHex;
      pin4 = step.pin4;
      remainingSec = step.remainingSec;
    }

    const invalidated = isInvalidatedStatus(pass.status);
    const opticalSvg = invalidated
      ? `<div class="cp-invalid-box" role="status">${escapeHtml(pass.status.toUpperCase())}</div>`
      : renderOpticalSvg(
          effectiveSymbology,
          effectiveBytes,
          stepHex,
          layout.opticalScale ?? 'standard'
        );

    const cols = layout.wayfindingColumns ?? Math.min(4, Math.max(2, effectiveWayfinding.length || 2));

    const headerHtml =
      layout.showHeader === false
        ? ''
        : `<header class="cp-header">
            <div class="cp-issuer-row">
              <div class="cp-issuer">
                ${
                  pass.header.issuerLogoUrl
                    ? `<img class="cp-logo" src="${escapeHtml(pass.header.issuerLogoUrl)}" alt="" />`
                    : ''
                }
                <span class="cp-issuer-name">${escapeHtml(pass.header.issuerName)}</span>
              </div>
              ${
                layout.showStatusPill === false
                  ? ''
                  : `<span class="cp-status cp-status-${escapeHtml(pass.status)}">${escapeHtml(
                      pass.status.toUpperCase()
                    )}</span>`
              }
            </div>
            ${
              pass.header.credentialLabel && pass.header.credentialValue
                ? `<div class="cp-cred"><span>${escapeHtml(
                    pass.header.credentialLabel
                  )}</span><strong>${escapeHtml(pass.header.credentialValue)}</strong></div>`
                : ''
            }
            <h2 class="cp-title">${escapeHtml(pass.header.title)}</h2>
            ${
              pass.header.subtitle
                ? `<p class="cp-subtitle">${escapeHtml(pass.header.subtitle)}</p>`
                : ''
            }
            ${
              pass.verificationBridge?.badgeLabel
                ? `<div class="cp-bridge-badge">${escapeHtml(
                    pass.verificationBridge.badgeLabel
                  )}</div>`
                : ''
            }
          </header>`;

    const holderHtml =
      layout.showHolder === false || !pass.holder || (!pass.holder.fullName && !pass.holder.tierValue)
        ? ''
        : `<section class="cp-holder">
            ${
              pass.holder.fullName
                ? `<div><span class="cp-lbl">HOLDER</span><div class="cp-val">${escapeHtml(
                    pass.holder.fullName
                  )}</div></div>`
                : ''
            }
            ${
              pass.holder.tierValue
                ? `<div><span class="cp-lbl">${escapeHtml(
                    pass.holder.tierLabel ?? 'CLASS'
                  )}</span><div class="cp-val">${escapeHtml(pass.holder.tierValue)}</div></div>`
                : ''
            }
          </section>`;

    const wayfindingHtml =
      layout.showWayfinding === false || effectiveWayfinding.length === 0
        ? ''
        : `<section class="cp-grid" style="grid-template-columns: repeat(${cols}, minmax(0, 1fr));">
            ${effectiveWayfinding
              .map(
                (slot) =>
                  `<div class="cp-cell ${slot.emphasize ? 'cp-cell-emph' : ''}">
                    <span class="cp-lbl">${escapeHtml(slot.label)}</span>
                    <strong class="cp-cell-val">${escapeHtml(slot.value)}</strong>
                  </div>`
              )
              .join('')}
          </section>`;

    const modesHtml =
      pass.modes && pass.modes.length > 0
        ? `<div class="cp-modes" role="tablist">
            <button type="button" class="cp-mode-btn ${
              activeModeId === null ? 'is-active' : ''
            }" data-mode="">Primary</button>
            ${pass.modes
              .map(
                (m) =>
                  `<button type="button" class="cp-mode-btn ${
                    activeModeId === m.id ? 'is-active' : ''
                  }" data-mode="${escapeHtml(m.id)}">${escapeHtml(m.label)}</button>`
              )
              .join('')}
          </div>`
        : '';

    const actionsHtml =
      pass.actions && pass.actions.length > 0
        ? `<div class="cp-actions">
            ${pass.actions
              .map(
                (a) =>
                  `<button type="button" class="cp-act-btn cp-act-${escapeHtml(
                    a.variant ?? 'secondary'

// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import type { OpticalSymbology } from './types.js';

function fnv1aHash(bytes: Uint8Array, seed: number): number {
  let hash = (0x811c9dc5 ^ seed) >>> 0;
  for (let i = 0; i < bytes.length; i++) {
    hash ^= bytes[i];
    hash = Math.imul(hash, 0x01000193) >>> 0;
  }
  return hash >>> 0;
}

function renderMatrixSvg(
  bytes: Uint8Array,
  stepSuffix: string,
  sizePx: number,
  isAztec: boolean
): string {
  const grid = isAztec ? 23 : 25;
  const quiet = 2;
  const total = grid + quiet * 2;
  const suffixBytes = new TextEncoder().encode(stepSuffix);
  const combined = new Uint8Array(bytes.length + suffixBytes.length);
  combined.set(bytes, 0);
  combined.set(suffixBytes, bytes.length);

  const rects: string[] = [];

  try {
    for (let r = 0; r < grid; r++) {
      for (let c = 0; c < grid; c++) {
        let filled = false;

        if (isAztec) {
          const center = Math.floor(grid / 2);
          const dist = Math.max(Math.abs(r - center), Math.abs(c - center));
          if (dist <= 4) {
            filled = dist % 2 === 0;
          } else {
            const h = fnv1aHash(combined, r * 131 + c * 17);
            filled = (h & 1) === 1;
          }
        } else {
          const inTopLeft = r < 7 && c < 7;
          const inTopRight = r < 7 && c >= grid - 7;
          const inBottomLeft = r >= grid - 7 && c < 7;

          if (inTopLeft || inTopRight || inBottomLeft) {
            const lr = r >= grid - 7 ? r - (grid - 7) : r;
            const lc = c >= grid - 7 ? c - (grid - 7) : c;
            filled =
              lr === 0 ||
              lr === 6 ||
              lc === 0 ||
              lc === 6 ||
              (lr >= 2 && lr <= 4 && lc >= 2 && lc <= 4);
          } else {
            const h = fnv1aHash(combined, r * 137 + c * 31);
            filled = (h & 1) === 1;
          }
        }

        if (filled) {
          rects.push(`<rect x="${c + quiet}" y="${r + quiet}" width="1" height="1" fill="#000000"/>`);
        }
      }
    }
  } finally {
    combined.fill(0);
    suffixBytes.fill(0);
  }

  return `<svg xmlns="http://w3.org" viewBox="0 0 ${total} ${total}" width="${sizePx}" height="${sizePx}" shape-rendering="crispEdges" role="img" aria-label="Optical matrix code"><rect width="${total}" height="${total}" fill="#FFFFFF"/>${rects.join('')}</svg>`;
}

function renderLinearSvg(
  bytes: Uint8Array,
  stepSuffix: string,
  widthPx: number,
  heightPx: number,
  isPdf417: boolean
): string {
  const cols = isPdf417 ? 56 : 72;
  const rows = isPdf417 ? 10 : 1;
  const quietX = 6;
  const quietY = 4;
  const totalW = cols + quietX * 2;
  const totalH = rows * (isPdf417 ? 3 : 28) + quietY * 2;
  const rowH = isPdf417 ? 3 : 28;

  const suffixBytes = new TextEncoder().encode(stepSuffix);
  const combined = new Uint8Array(bytes.length + suffixBytes.length);
  combined.set(bytes, 0);
  combined.set(suffixBytes, bytes.length);

  const bars: string[] = [];

  try {
    for (let r = 0; r < rows; r++) {
      for (let c = 0; c < cols; c++) {
        let on = false;
        if (c === 0 || c === 2 || c === cols - 1 || c === cols - 3) {
          on = true;
        } else if (c === 1 || c === cols - 2) {
          on = false;
        } else {
          const h = fnv1aHash(combined, r * 257 + c * 19);
          on = (h & 1) === 1;
        }
        if (on) {
          bars.push(
            `<rect x="${c + quietX}" y="${r * rowH + quietY}" width="1" height="${rowH}" fill="#000000"/>`
          );
        }
      }
    }
  } finally {
    combined.fill(0);
    suffixBytes.fill(0);
  }

  return `<svg xmlns="http://w3.org" viewBox="0 0 ${totalW} ${totalH}" width="${widthPx}" height="${heightPx}" shape-rendering="crispEdges" role="img" aria-label="Optical linear code"><rect width="${totalW}" height="${totalH}" fill="#FFFFFF"/>${bars.join('')}</svg>`;
}

export function renderOpticalSvg(
  symbology: OpticalSymbology,
  tokenBytes: Uint8Array,
  stepSuffix: string,
  scale: 'compact' | 'standard' | 'large' = 'standard'
): string {
  const matrixSize = scale === 'compact' ? 148 : scale === 'large' ? 224 : 184;
  const linearWidth = scale === 'compact' ? 210 : scale === 'large' ? 280 : 244;
  const linearHeight = scale === 'compact' ? 68 : scale === 'large' ? 92 : 80;

  switch (symbology) {
    case 'aztec':
      return renderMatrixSvg(tokenBytes, stepSuffix, matrixSize, true);
    case 'qr':
    case 'datamatrix':
    case 'nfc_handoff':
      return renderMatrixSvg(tokenBytes, stepSuffix, matrixSize, false);
    case 'pdf417':
      return renderLinearSvg(tokenBytes, stepSuffix, linearWidth, linearHeight, true);
    case 'code128':
    case 'ean13':
      return renderLinearSvg(tokenBytes, stepSuffix, linearWidth, linearHeight, false);
  }
}

// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

export interface RotatingStepResult {
  stepHex: string;
  pin4: string;
  remainingSec: number;
}

export async function deriveRotatingStep(
  seedBytes: Uint8Array,
  intervalSec: number,
  nowMs: number = Date.now()
): Promise<RotatingStepResult> {
  const safeInterval = Math.max(5, Math.floor(intervalSec));
  const epochSec = Math.floor(nowMs / 1000);
  const counter = Math.floor(epochSec / safeInterval);
  const remainingSec = safeInterval - (epochSec % safeInterval);

  const counterBuffer = new ArrayBuffer(8);
  const view = new DataView(counterBuffer);
  view.setUint32(0, Math.floor(counter / 0x100000000), false);
  view.setUint32(4, counter >>> 0, false);

  const keyCopy = new Uint8Array(seedBytes.length);
  keyCopy.set(seedBytes);

  try {
    const cryptoKey = await crypto.subtle.importKey(
      'raw',
      keyCopy,
      { name: 'HMAC', hash: 'SHA-256' },
      false,
      ['sign']
    );

    const signature = await crypto.subtle.sign('HMAC', cryptoKey, counterBuffer);
    const mac = new Uint8Array(signature);

    try {
      const offset = mac[mac.length - 1] & 0x0f;
      const binary =
        ((mac[offset] & 0x7f) << 24) |
        ((mac[offset + 1] & 0xff) << 16) |
        ((mac[offset + 2] & 0xff) << 8) |
        (mac[offset + 3] & 0xff);

      const pin4 = String(binary % 10000).padStart(4, '0');
      const stepHex = Array.from(mac.subarray(0, 8))
        .map((b) => b.toString(16).padStart(2, '0'))
        .join('');

      return { stepHex, pin4, remainingSec };
    } finally {
      mac.fill(0);
    }
  } finally {
    keyCopy.fill(0);
  }
}

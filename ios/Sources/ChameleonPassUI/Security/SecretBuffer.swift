// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import Foundation
import Darwin

/// Volatile, auto-scrubbing memory container for sensitive optical tokens and HMAC seeds.
public final class SecretBuffer: @unchecked Sendable {
    private var buffer: UnsafeMutableRawBufferPointer?
    private let lock = NSLock()
    private(set) public var isDestroyed: Bool = false

    public var count: Int {
        lock.lock()
        defer { lock.unlock() }
        return isDestroyed ? 0 : (buffer?.count ?? 0)
    }

    /// Copies source data into unmanaged heap memory and scrubs the source Data immediately.
    public init(consuming source: inout Data) {
        let byteCount = source.count
        if byteCount > 0 {
            let rawPtr = UnsafeMutableRawBufferPointer.allocate(
                byteCount: byteCount,
                alignment: MemoryLayout<UInt8>.alignment
            )
            source.copyBytes(to: rawPtr)
            self.buffer = rawPtr

            // Scrub caller's Data buffer via C memset_s
            source.withUnsafeMutableBytes { ptr in
                if let base = ptr.baseAddress {
                    _ = memset_s(base, ptr.count, 0x00, ptr.count)
                }
            }
            source = Data()
        } else {
            self.buffer = nil
        }
    }

    /// Scopes read-only access to the underlying raw bytes.
    public func withUnsafeBytes<R>(_ body: (UnsafeRawBufferPointer) throws -> R) throws -> R {
        lock.lock()
        defer { lock.unlock() }
        guard !isDestroyed else {
            throw ChameleonSecurityError.bufferAlreadyScrubbed
        }
        guard let buffer else {
            return try body(UnsafeRawBufferPointer(start: nil, count: 0))
        }
        return try body(UnsafeRawBufferPointer(buffer))
    }

    /// Deterministically zeroes out the memory buffer via C `memset_s` (idempotent).
    public func destroy() {
        lock.lock()
        defer { lock.unlock() }
        guard !isDestroyed else { return }
        isDestroyed = true

        if let buffer {
            if let base = buffer.baseAddress, buffer.count > 0 {
                _ = memset_s(base, buffer.count, 0x00, buffer.count)
            }
            buffer.deallocate()
            self.buffer = nil
        }
    }

    deinit {
        destroy()
    }
}

public enum ChameleonSecurityError: Error, Sendable {
    case bufferAlreadyScrubbed
}

// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import Foundation
import Darwin

public enum ByosReceiptStreamer {
    /// Streams a signed JWS receipt directly from volatile RAM to a customer-owned
    /// S3/GCS/Azure/R2 pre-signed URL without touching URLCache or disk storage.
    public static func emitReceipt(
        jwsData: inout Data,
        policy: ClientStoragePolicy,
        zeroedAfter: Bool
    ) async throws -> ChameleonReceipt {
        let jwsString = String(data: jwsData, encoding: .utf8) ?? ""
        defer {
            jwsData.withUnsafeMutableBytes { ptr in
                if let base = ptr.baseAddress, ptr.count > 0 {
                    _ = memset_s(base, ptr.count, 0x00, ptr.count)
                }
            }
            jwsData = Data()
        }

        switch policy {
        case .none:
            return ChameleonReceipt(
                jwsToken: jwsString,
                zeroed: zeroedAfter,
                storageMode: "none",
                timestamp: Date()
            )

        case .byos(let provider, let presignedUploadUrl):
            let config = URLSessionConfiguration.ephemeral
            config.urlCache = nil
            config.requestCachePolicy = .reloadIgnoringLocalAndRemoteCacheData
            let ephemeralSession = URLSession(configuration: config)
            defer { ephemeralSession.finishTasksAndInvalidate() }

            var request = URLRequest(url: presignedUploadUrl)
            request.httpMethod = "PUT"
            request.setValue("application/jose", forHTTPHeaderField: "Content-Type")
            request.setValue("no-store", forHTTPHeaderField: "Cache-Control")
            if provider == .azure {
                request.setValue("BlockBlob", forHTTPHeaderField: "x-ms-blob-type")
            }

            let (_, response) = try await ephemeralSession.upload(for: request, from: jwsData)
            guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
                throw URLError(.badServerResponse)
            }

            return ChameleonReceipt(
                jwsToken: jwsString,
                zeroed: zeroedAfter,
                storageMode: "byos",
                timestamp: Date()
            )
        }
    }
}

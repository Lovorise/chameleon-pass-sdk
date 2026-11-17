// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import SwiftUI
import CoreImage
import CoreImage.CIFilterBuiltins
import Darwin

public struct OpticalBarcodeView: View {
    public let symbology: OpticalSymbology
    public let tokenBytes: SecretBuffer
    public let rotatingStepHex: String
    public let scale: OpticalScale

    private let context = CIContext(options: [.cacheIntermediates: false])

    public init(
        symbology: OpticalSymbology,
        tokenBytes: SecretBuffer,
        rotatingStepHex: String,
        scale: OpticalScale
    ) {
        self.symbology = symbology
        self.tokenBytes = tokenBytes
        self.rotatingStepHex = rotatingStepHex
        self.scale = scale
    }

    public var body: some View {
        let boxHeight: CGFloat = switch scale {
        case .compact: 148
        case .standard: 196
        case .large: 240
        }
        let is1D = symbology == .code128 || symbology == .ean13 || symbology == .pdf417

        ZStack {
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .fill(Color.white)

            if let uiImage = renderBarcodeImage() {
                Image(uiImage: uiImage)
                    .interpolation(.none)
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width: is1D ? 260 : boxHeight,
                        height: is1D ? 96 : boxHeight
                    )
            }
        }
        .padding(12)
        .fixedSize()
    }

    private func renderBarcodeImage() -> UIImage? {
        guard !tokenBytes.isDestroyed, symbology != .nfcHandoff else { return nil }

        return try? tokenBytes.withUnsafeBytes { rawBuf in
            var combined = Data(rawBuf)
            if !rotatingStepHex.isEmpty, let stepData = ".\(rotatingStepHex)".data(using: .utf8) {
                combined.append(stepData)
            }
            defer {
                combined.withUnsafeMutableBytes { ptr in
                    if let base = ptr.baseAddress, ptr.count > 0 {
                        _ = memset_s(base, ptr.count, 0x00, ptr.count)
                    }
                }
            }

            let outputImage: CIImage? = switch symbology {
            case .qr, .datamatrix, .ean13, .nfcHandoff:
                CIFilter.qrCodeGenerator().apply(message: combined, correctionLevel: "M")
            case .aztec:
                CIFilter.aztecCodeGenerator().apply(message: combined)
            case .pdf417:
                CIFilter.pdf417BarcodeGenerator().apply(message: combined)
            case .code128:
                CIFilter.code128BarcodeGenerator().apply(message: combined)
            }

            guard let ciImage = outputImage else { return nil }
            let transform = CGAffineTransform(scaleX: 10, y: 10)
            let scaled = ciImage.transformed(by: transform)
            guard let cgImage = context.createCGImage(scaled, from: scaled.extent) else { return nil }
            return UIImage(cgImage: cgImage)
        }
    }
}

private extension CIQRCodeGenerator {
    func apply(message: Data, correctionLevel: String) -> CIImage? {
        self.message = message
        self.correctionLevel = correctionLevel
        return self.outputImage
    }
}

private extension CIAztecCodeGenerator {
    func apply(message: Data) -> CIImage? {
        self.message = message
        return self.outputImage
    }
}

private extension CIPDF417BarcodeGenerator {
    func apply(message: Data) -> CIImage? {
        self.message = message
        return self.outputImage
    }
}

private extension CICode128BarcodeGenerator {
    func apply(message: Data) -> CIImage? {
        self.message = message
        return self.outputImage
    }
}

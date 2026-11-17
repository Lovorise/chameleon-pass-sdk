// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.compose.optical

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.mtchship.chameleon.core.model.OpticalScale
import com.mtchship.chameleon.core.model.OpticalSymbology
import com.mtchship.chameleon.core.security.SecretBuffer

@Composable
fun OpticalBarcodeCanvas(
    symbology: OpticalSymbology,
    tokenBytes: SecretBuffer,
    rotatingStepHex: String,
    scale: OpticalScale,
    modifier: Modifier = Modifier
) {
    val matrix: BitMatrix? = remember(symbology, tokenBytes, rotatingStepHex, tokenBytes.isDestroyed) {
        if (tokenBytes.isDestroyed || symbology == OpticalSymbology.NFC_HANDOFF) return@remember null
        tokenBytes.useReadOnly { buf ->
            val raw = ByteArray(buf.remaining()).also { buf.get(it) }
            try {
                val payload = buildString {
                    append(String(raw, Charsets.ISO_8859_1))
                    if (rotatingStepHex.isNotEmpty()) {
                        append('.')
                        append(rotatingStepHex)
                    }
                }
                val format = when (symbology) {
                    OpticalSymbology.QR -> BarcodeFormat.QR_CODE
                    OpticalSymbology.AZTEC -> BarcodeFormat.AZTEC
                    OpticalSymbology.PDF417 -> BarcodeFormat.PDF_417
                    OpticalSymbology.CODE128 -> BarcodeFormat.CODE_128
                    OpticalSymbology.EAN13 -> BarcodeFormat.EAN_13
                    OpticalSymbology.DATAMATRIX -> BarcodeFormat.DATA_MATRIX
                    OpticalSymbology.NFC_HANDOFF -> BarcodeFormat.QR_CODE
                }
                val is1D = format == BarcodeFormat.CODE_128 || format == BarcodeFormat.EAN_13 || format == BarcodeFormat.PDF_417
                MultiFormatWriter().encode(
                    payload,
                    format,
                    if (is1D) 320 else 220,
                    if (is1D) 110 else 220,
                    mapOf(EncodeHintType.MARGIN to 1)
                )
            } finally {
                raw.fill(0x00)
            }
        }
    }

    val boxHeight = when (scale) {
        OpticalScale.COMPACT -> 148.dp
        OpticalScale.STANDARD -> 196.dp
        OpticalScale.LARGE -> 240.dp
    }
    val is1D = symbology == OpticalSymbology.CODE128 || symbology == OpticalSymbology.EAN13 || symbology == OpticalSymbology.PDF417

    // High-contrast pure white quiet zone even in dark mode for reliable optical scanning
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (matrix != null) {
            Canvas(
                modifier = Modifier.size(
                    width = if (is1D) 260.dp else boxHeight,
                    height = if (is1D) 96.dp else boxHeight
                )
            ) {
                val cellW = size.width / matrix.width.toFloat()
                val cellH = size.height / matrix.height.toFloat()
                for (y in 0 until matrix.height) {
                    for (x in 0 until matrix.width) {
                        if (matrix.get(x, y)) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(x * cellW, y * cellH),
                                size = Size(cellW + 0.5f, cellH + 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

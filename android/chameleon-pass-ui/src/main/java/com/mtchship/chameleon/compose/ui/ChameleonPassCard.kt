// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mtchship.chameleon.compose.optical.OpticalBarcodeCanvas
import com.mtchship.chameleon.compose.security.ChameleonWindowSecurityEffect
import com.mtchship.chameleon.compose.theme.resolvePassPalette
import com.mtchship.chameleon.core.i18n.ChameleonI18n
import com.mtchship.chameleon.core.model.*
import com.mtchship.chameleon.core.security.RotatingStepResult
import com.mtchship.chameleon.core.security.TotpEngine
import com.mtchship.chameleon.core.session.ChameleonPassSession
import com.mtchship.chameleon.core.session.isInvalidated
import com.mtchship.chameleon.nfc.ChameleonNfcRegistry
import com.mtchship.chameleon.nfc.NfcHardwareStatus
import kotlinx.coroutines.delay

@Composable
fun ChameleonPassCard(
    session: ChameleonPassSession,
    modifier: Modifier = Modifier,
    onAction: ((actionId: String, passIndex: Int) -> Unit)? = null
) {
    val state by session.state.collectAsStateWithLifecycle()
    val pass = state.currentPass ?: return
    val context = LocalContext.current

    val layout = pass.layout ?: ChameleonLayoutConfig()
    val palette = resolvePassPalette(pass, state.colorScheme)
    val resolvedDir = ChameleonI18n.resolveLayoutDirection(layout.dir, state.locale)
    val labels = state.labels

    val selectedMode = remember(pass, state.activeModeId) {
        pass.modes.firstOrNull { it.id == state.activeModeId }
    }

    val nfcHardware = remember(context) { ChameleonNfcRegistry.checkHardwareStatus(context) }
    val requestedSymbology = selectedMode?.symbology ?: pass.opticalToken.symbology
    val isNfcActive = requestedSymbology == OpticalSymbology.NFC_HANDOFF && nfcHardware == NfcHardwareStatus.READY

    val effectiveOpticalSymbology = when {
        requestedSymbology != OpticalSymbology.NFC_HANDOFF -> requestedSymbology
        pass.opticalToken.fallbackSymbology != null -> pass.opticalToken.fallbackSymbology
        else -> OpticalSymbology.QR
    }

    val effectiveToken = selectedMode?.tokenBytes ?: pass.opticalToken.tokenBytes
    val effectiveCode = selectedMode?.humanReadableCode ?: pass.opticalToken.humanReadableCode
    val effectiveWayfinding = selectedMode?.wayfinding?.takeIf { it.isNotEmpty() } ?: pass.wayfinding
    val invalidated = pass.status.isInvalidated()

    // TOTP rotation loop
    var rotatingStep by remember(pass, invalidated) { mutableStateOf<RotatingStepResult?>(null) }
    LaunchedEffect(pass, invalidated) {
        val seed = pass.opticalToken.hmacSeedBytes
        if (!invalidated && seed != null && !seed.isDestroyed) {
            val interval = pass.opticalToken.rotationIntervalSec ?: 15
            while (true) {
                rotatingStep = TotpEngine.deriveRotatingStep(seed, interval)
                delay(1_000L)
            }
        } else {
            rotatingStep = null
        }
    }

    // Bind active credential to Android NFC HCE service
    DisposableEffect(pass, effectiveToken, rotatingStep, isNfcActive, invalidated) {
        if (isNfcActive && !invalidated) {
            ChameleonNfcRegistry.bindActiveCredential(
                token = effectiveToken,
                rotatingStepBytes = rotatingStep?.stepHex?.toByteArray(Charsets.UTF_8)
            )
        } else {
            ChameleonNfcRegistry.clearCredential()
        }
        onDispose { ChameleonNfcRegistry.clearCredential() }
    }

    ChameleonWindowSecurityEffect(
        keepScreenAwake = session.keepScreenAwake,
        posScannerMode = pass.opticalToken.posScannerMode
    )

    CompositionLocalProvider(LocalLayoutDirection provides resolvedDir) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(palette.radius),
            color = palette.surface,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                if (layout.showHeader) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pass.header.issuerName,
                            color = palette.muted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (layout.showStatusPill) {
                            Box(
                                modifier = Modifier
                                    .background(palette.accent.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = labels.statusLabel(pass.status),
                                    color = palette.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = pass.header.title,
                            color = palette.text,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        pass.header.subtitle?.let {
                            Text(text = it, color = palette.muted, fontSize = 14.sp)
                        }
                    }
                }

                // Holder
                val holder = pass.holder
                if (layout.showHolder && holder != null && (holder.fullName != null || holder.tierValue != null)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        holder.fullName?.let { name ->
                            Column {
                                Text(labels.holder, color = palette.muted, fontSize = 11.sp)
                                Text(name, color = palette.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        holder.tierValue?.let { tier ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(holder.tierLabel ?: labels.defaultTier, color = palette.muted, fontSize = 11.sp)
                                Text(tier, color = palette.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Optical & NFC Credential Zone
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (invalidated) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(palette.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labels.statusLabel(pass.status),
                                color = palette.muted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        if (isNfcActive) {
                            Box(
                                modifier = Modifier
                                    .background(palette.accent, RoundedCornerShape(999.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = labels.nfcReady,
                                    color = palette.accentForeground,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OpticalBarcodeCanvas(

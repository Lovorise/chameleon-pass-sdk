// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.core.session

import com.mtchship.chameleon.core.i18n.ChameleonI18n
import com.mtchship.chameleon.core.model.ChameleonTransport
import com.mtchship.chameleon.core.model.ColorSchemeMode
import com.mtchship.chameleon.core.model.LayoutDirection
import com.mtchship.chameleon.core.model.PassStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class AutoContextResult(
    val locale: String,
    val dir: LayoutDirection,
    val countryCode: String,
    val colorScheme: ColorSchemeMode
)

fun PassStatus.isInvalidated(): Boolean =
    this == PassStatus.TRANSFERRED ||
    this == PassStatus.VOID ||
    this == PassStatus.EXPIRED ||
    this == PassStatus.REDEEMED

object ChameleonContextResolver {
    suspend fun resolveAutoContext(
        isSystemDark: Boolean,
        transport: ChameleonTransport? = null
    ): AutoContextResult = withContext(Dispatchers.IO) {
        val deviceLocale = ChameleonI18n.detectDeviceLocale()
        val baseLang = deviceLocale.substringBefore('-').substringBefore('_').lowercase(Locale.ROOT)
        val dir = if (baseLang in setOf("ar", "he", "iw", "fa", "ur")) {
            LayoutDirection.RTL
        } else {
            LayoutDirection.LTR
        }
        val regionPart = deviceLocale.substringAfter('-', "").substringBefore('-')
        val countryCode = regionPart.takeIf { it.isNotBlank() }?.uppercase(Locale.ROOT) ?: "GB"
        val localScheme = if (isSystemDark) ColorSchemeMode.DARK else ColorSchemeMode.LIGHT

        if (transport != null) {
            try {
                val baseUrl = transport.endpoint.trimEnd('/')
                val conn = (URL("$baseUrl/v1/context").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3_000
                    readTimeout = 3_000
                    transport.keyToken.useReadOnly { buf ->
                        val bytes = ByteArray(buf.remaining()).also { buf.get(it) }
                        setRequestProperty("Authorization", "Bearer ${String(bytes, Charsets.UTF_8)}")
                        bytes.fill(0)
                    }
                }
                if (conn.responseCode in 200..299) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    return@withContext AutoContextResult(
                        locale = json.optString("locale", deviceLocale),
                        dir = when (json.optString("dir")) {
                            "rtl" -> LayoutDirection.RTL
                            "ltr" -> LayoutDirection.LTR
                            else -> dir
                        },
                        countryCode = json.optString("countryCode", countryCode),
                        colorScheme = when (json.optString("colorScheme")) {
                            "dark" -> ColorSchemeMode.DARK
                            "light" -> ColorSchemeMode.LIGHT
                            else -> localScheme
                        }
                    )
                }
            } catch (_: Exception) {
                // Offline fallback to device context
            }
        }
        AutoContextResult(deviceLocale, dir, countryCode, localScheme)
    }
}

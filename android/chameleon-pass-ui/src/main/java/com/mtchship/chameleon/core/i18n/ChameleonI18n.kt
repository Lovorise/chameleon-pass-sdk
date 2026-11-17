// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

package com.mtchship.chameleon.core.i18n

import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.LocaleListCompat
import com.mtchship.chameleon.core.model.PassStatus
import java.util.Locale

data class PassUiLabels(
    val holder: String,
    val defaultTier: String,
    val primaryMode: String,
    val nfcReady: String,
    val refreshesIn: String,
    val prev: String,
    val next: String,
    val status: Map<PassStatus, String>
) {
    fun formatRefreshesIn(seconds: Int): String =
        refreshesIn.replace("{s}", seconds.toString())

    fun statusLabel(passStatus: PassStatus): String =
        status[passStatus] ?: passStatus.name
}

data class PassUiLabelOverrides(
    val holder: String? = null,
    val defaultTier: String? = null,
    val primaryMode: String? = null,
    val nfcReady: String? = null,
    val refreshesIn: String? = null,
    val prev: String? = null,
    val next: String? = null,
    val status: Map<PassStatus, String>? = null
)

object ChameleonI18n {

    private val RTL_LANGUAGES = setOf("ar", "he", "iw", "fa", "ur")

    private val DICTIONARIES: Map<String, PassUiLabels> = mapOf(
        "en" to PassUiLabels(
            holder = "HOLDER",
            defaultTier = "CLASS",
            primaryMode = "Primary",
            nfcReady = "NFC READY • OPTICAL FALLBACK",
            refreshesIn = "Refreshes in {s}s",
            prev = "Prev",
            next = "Next",
            status = mapOf(
                PassStatus.ACTIVE to "ACTIVE",
                PassStatus.READY to "READY",
                PassStatus.PENDING to "PENDING",
                PassStatus.REDEEMED to "REDEEMED",
                PassStatus.TRANSFERRED to "TRANSFERRED",
                PassStatus.ARCHIVE to "ARCHIVE",
                PassStatus.EXPIRED to "EXPIRED",
                PassStatus.VOID to "VOID"
            )
        ),
        "de" to PassUiLabels(
            holder = "INHABER",
            defaultTier = "KLASSE",
            primaryMode = "Standard",
            nfcReady = "NFC BEREIT • OPTISCHES BACKUP",
            refreshesIn = "Aktualisierung in {s}s",
            prev = "Zurück",
            next = "Weiter",
            status = mapOf(
                PassStatus.ACTIVE to "AKTIV",
                PassStatus.READY to "BEREIT",
                PassStatus.PENDING to "AUSSTEHEND",
                PassStatus.REDEEMED to "EINGELÖST",
                PassStatus.TRANSFERRED to "ÜBERTRAGEN",
                PassStatus.ARCHIVE to "ARCHIV",
                PassStatus.EXPIRED to "ABGELAUFEN",
                PassStatus.VOID to "UNGÜLTIG"
            )
        ),
        "fr" to PassUiLabels(
            holder = "TITULAIRE",
            defaultTier = "CATÉGORIE",
            primaryMode = "Principal",
            nfcReady = "NFC PRÊT • SECOURS OPTIQUE",
            refreshesIn = "Actualisation dans {s}s",
            prev = "Préc.",
            next = "Suiv.",
            status = mapOf(
                PassStatus.ACTIVE to "ACTIF",
                PassStatus.READY to "PRÊT",
                PassStatus.PENDING to "EN ATTENTE",
                PassStatus.REDEEMED to "UTILISÉ",
                PassStatus.TRANSFERRED to "TRANSFÉRÉ",
                PassStatus.ARCHIVE to "ARCHIVE",
                PassStatus.EXPIRED to "EXPIRÉ",
                PassStatus.VOID to "ANNULÉ"
            )
        ),
        "it" to PassUiLabels(
            holder = "TITOLARE",
            defaultTier = "CLASSE",
            primaryMode = "Principale",
            nfcReady = "NFC PRONTO • FALLBACK OTTICO",
            refreshesIn = "Aggiornamento tra {s}s",
            prev = "Prec.",
            next = "Succ.",
            status = mapOf(
                PassStatus.ACTIVE to "ATTIVO",
                PassStatus.READY to "PRONTO",
                PassStatus.PENDING to "IN ATTESA",
                PassStatus.REDEEMED to "UTILIZZATO",
                PassStatus.TRANSFERRED to "TRASFERITO",
                PassStatus.ARCHIVE to "ARCHIVIO",
                PassStatus.EXPIRED to "SCADUTO",
                PassStatus.VOID to "ANNULLATO"
            )
        ),
        "es" to PassUiLabels(
            holder = "TITULAR",
            defaultTier = "CLASE",
            primaryMode = "Principal",
            nfcReady = "NFC LISTO • RESPALDO ÓPTICO",
            refreshesIn = "Se actualiza en {s}s",
            prev = "Ant.",
            next = "Sig.",
            status = mapOf(
                PassStatus.ACTIVE to "ACTIVO",
                PassStatus.READY to "LISTO",
                PassStatus.PENDING to "PENDIENTE",
                PassStatus.REDEEMED to "CANJEADO",
                PassStatus.TRANSFERRED to "TRANSFERIDO",
                PassStatus.ARCHIVE to "ARCHIVO",
                PassStatus.EXPIRED to "CADUCADO",
                PassStatus.VOID to "ANULADO"
            )
        ),
        "pt" to PassUiLabels(
            holder = "TITULAR",
            defaultTier = "CLASSE",
            primaryMode = "Principal",
            nfcReady = "NFC PRONTO • ALTERNATIVA ÓPTICA",
            refreshesIn = "Atualiza em {s}s",
            prev = "Ant.",
            next = "Próx.",
            status = mapOf(
                PassStatus.ACTIVE to "ATIVO",
                PassStatus.READY to "PRONTO",
                PassStatus.PENDING to "PENDENTE",
                PassStatus.REDEEMED to "UTILIZADO",
                PassStatus.TRANSFERRED to "TRANSFERIDO",
                PassStatus.ARCHIVE to "ARQUIVO",
                PassStatus.EXPIRED to "EXPIRADO",
                PassStatus.VOID to "ANULADO"
            )
        ),
        "nl" to PassUiLabels(
            holder = "HOUDER",
            defaultTier = "KLASSE",
            primaryMode = "Primair",
            nfcReady = "NFC GEREED • OPTISCHE BACK-UP",
            refreshesIn = "Vernieuwt over {s}s",
            prev = "Vorige",
            next = "Volgende",
            status = mapOf(
                PassStatus.ACTIVE to "ACTIEF",
                PassStatus.READY to "GEREED",
                PassStatus.PENDING to "IN AFWACHTING",
                PassStatus.REDEEMED to "INGEWISSELD",
                PassStatus.TRANSFERRED to "OVERGEDRAGEN",
                PassStatus.ARCHIVE to "ARCHIEF",
                PassStatus.EXPIRED to "VERLOPEN",
                PassStatus.VOID to "ONGELDIG"
            )
        ),
        "ja" to PassUiLabels(
            holder = "所有者",
            defaultTier = "区分",
            primaryMode = "メイン",
            nfcReady = "NFC対応 • 光学コード予備",
            refreshesIn = "{s}秒後に更新",
            prev = "前へ",
            next = "次へ",
            status = mapOf(
                PassStatus.ACTIVE to "有効",
                PassStatus.READY to "準備完了",
                PassStatus.PENDING to "保留中",
                PassStatus.REDEEMED to "使用済み",
                PassStatus.TRANSFERRED to "譲渡済み",
                PassStatus.ARCHIVE to "アーカイブ",
                PassStatus.EXPIRED to "期限切れ",
                PassStatus.VOID to "無効"
            )
        ),
        "zh" to PassUiLabels(
            holder = "持有人",
            defaultTier = "类别",
            primaryMode = "主凭证",
            nfcReady = "NFC就绪 • 光学备用码",
            refreshesIn = "{s}秒后刷新",
            prev = "上一张",
            next = "下一张",
            status = mapOf(
                PassStatus.ACTIVE to "有效",
                PassStatus.READY to "就绪",
                PassStatus.PENDING to "待处理",
                PassStatus.REDEEMED to "已核销",
                PassStatus.TRANSFERRED to "已转让",
                PassStatus.ARCHIVE to "已归档",
                PassStatus.EXPIRED to "已过期",
                PassStatus.VOID to "已作废"
            )
        ),
        "ar" to PassUiLabels(
            holder = "حامل البطاقة",
            defaultTier = "الفئة",
            primaryMode = "الأساسي",
            nfcReady = "NFC جاهز • رمز ضوئي بديل",
            refreshesIn = "يتجدد خلال {s} ث",
            prev = "السابق",
            next = "التالي",
            status = mapOf(
                PassStatus.ACTIVE to "نشط",
                PassStatus.READY to "جاهز",
                PassStatus.PENDING to "قيد الانتظار",
                PassStatus.REDEEMED to "مستخدم",
                PassStatus.TRANSFERRED to "محوّل",
                PassStatus.ARCHIVE to "أرشيف",
                PassStatus.EXPIRED to "منتهي",
                PassStatus.VOID to "ملغى"
            )
        )
    )

    fun detectDeviceLocale(): String {
        val locales = LocaleListCompat.getAdjustedDefault()
        if (!locales.isEmpty) {
            locales?.toLanguageTag()?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return Locale.getDefault().toLanguageTag().ifBlank { "en" }
    }

    fun resolveLabels(
        locale: String = detectDeviceLocale(),
        overrides: PassUiLabelOverrides? = null
    ): PassUiLabels {
        val baseLang = locale.substringBefore('-').substringBefore('_').lowercase(Locale.ROOT)
        val baseDict = DICTIONARIES[baseLang] ?: DICTIONARIES.getValue("en")

        return PassUiLabels(
            holder = overrides?.holder ?: baseDict.holder,
            defaultTier = overrides?.defaultTier ?: baseDict.defaultTier,
            primaryMode = overrides?.primaryMode ?: baseDict.primaryMode,
            nfcReady = overrides?.nfcReady ?: baseDict.nfcReady,
            refreshesIn = overrides?.refreshesIn ?: baseDict.refreshesIn,
            prev = overrides?.prev ?: baseDict.prev,
            next = overrides?.next ?: baseDict.next,
            status = baseDict.status + (overrides?.status ?: emptyMap())
        )
    }

    fun resolveLayoutDirection(
        explicitDir: com.mtchship.chameleon.core.model.LayoutDirection,
        locale: String
    ): LayoutDirection = when (explicitDir) {

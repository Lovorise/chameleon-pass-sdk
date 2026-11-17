// Copyright 2026 Lovorise Ltd
// SPDX-License-Identifier: Apache-2.0

import type { PassStatus } from './types.js';

export interface PassUiLabels {
  holder: string;
  defaultTier: string;
  primaryMode: string;
  nfcReady: string;
  refreshesIn: string;
  prev: string;
  next: string;
  status: Record<PassStatus, string>;
}

const DICTIONARIES: Record<string, PassUiLabels> = {
  en: {
    holder: 'HOLDER',
    defaultTier: 'CLASS',
    primaryMode: 'Primary',
    nfcReady: 'NFC READY • OPTICAL FALLBACK',
    refreshesIn: 'Refreshes in {s}s',
    prev: 'Prev',
    next: 'Next',
    status: {
      active: 'ACTIVE',
      ready: 'READY',
      pending: 'PENDING',
      redeemed: 'REDEEMED',
      transferred: 'TRANSFERRED',
      archive: 'ARCHIVE',
      expired: 'EXPIRED',
      void: 'VOID',
    },
  },
  de: {
    holder: 'INHABER',
    defaultTier: 'KLASSE',
    primaryMode: 'Standard',
    nfcReady: 'NFC BEREIT • OPTISCHES BACKUP',
    refreshesIn: 'Aktualisierung in {s}s',
    prev: 'Zurück',
    next: 'Weiter',
    status: {
      active: 'AKTIV',
      ready: 'BEREIT',
      pending: 'AUSSTEHEND',
      redeemed: 'EINGELÖST',
      transferred: 'ÜBERTRAGEN',
      archive: 'ARCHIV',
      expired: 'ABGELAUFEN',
      void: 'UNGÜLTIG',
    },
  },
  fr: {
    holder: 'TITULAIRE',
    defaultTier: 'CATÉGORIE',
    primaryMode: 'Principal',
    nfcReady: 'NFC PRÊT • SECOURS OPTIQUE',
    refreshesIn: 'Actualisation dans {s}s',
    prev: 'Préc.',
    next: 'Suiv.',
    status: {
      active: 'ACTIF',
      ready: 'PRÊT',
      pending: 'EN ATTENTE',
      redeemed: 'UTILISÉ',
      transferred: 'TRANSFÉRÉ',
      archive: 'ARCHIVE',
      expired: 'EXPIRÉ',
      void: 'ANNULÉ',
    },
  },
  it: {
    holder: 'TITOLARE',
    defaultTier: 'CLASSE',
    primaryMode: 'Principale',
    nfcReady: 'NFC PRONTO • FALLBACK OTTICO',
    refreshesIn: 'Aggiornamento tra {s}s',
    prev: 'Prec.',
    next: 'Succ.',
    status: {
      active: 'ATTIVO',
      ready: 'PRONTO',
      pending: 'IN ATTESA',
      redeemed: 'UTILIZZATO',
      transferred: 'TRASFERITO',
      archive: 'ARCHIVIO',
      expired: 'SCADUTO',
      void: 'ANNULLATO',
    },
  },
  es: {
    holder: 'TITULAR',
    defaultTier: 'CLASE',
    primaryMode: 'Principal',
    nfcReady: 'NFC LISTO • RESPALDO ÓPTICO',
    refreshesIn: 'Se actualiza en {s}s',
    prev: 'Ant.',
    next: 'Sig.',
    status: {
      active: 'ACTIVO',
      ready: 'LISTO',
      pending: 'PENDIENTE',
      redeemed: 'CANJEADO',
      transferred: 'TRANSFERIDO',
      archive: 'ARCHIVO',
      expired: 'CADUCADO',
      void: 'ANULADO',
    },
  },
  pt: {
    holder: 'TITULAR',
    defaultTier: 'CLASSE',
    primaryMode: 'Principal',
    nfcReady: 'NFC PRONTO • ALTERNATIVA ÓPTICA',
    refreshesIn: 'Atualiza em {s}s',
    prev: 'Ant.',
    next: 'Próx.',
    status: {
      active: 'ATIVO',
      ready: 'PRONTO',
      pending: 'PENDENTE',
      redeemed: 'UTILIZADO',
      transferred: 'TRANSFERIDO',
      archive: 'ARQUIVO',
      expired: 'EXPIRADO',
      void: 'ANULADO',
    },
  },
  nl: {
    holder: 'HOUDER',
    defaultTier: 'KLASSE',
    primaryMode: 'Primair',
    nfcReady: 'NFC GEREED • OPTISCHE BACK-UP',
    refreshesIn: 'Vernieuwt over {s}s',
    prev: 'Vorige',
    next: 'Volgende',
    status: {
      active: 'ACTIEF',
      ready: 'GEREED',
      pending: 'IN AFWACHTING',
      redeemed: 'INGEWISSELD',
      transferred: 'OVERGEDRAGEN',
      archive: 'ARCHIEF',
      expired: 'VERLOPEN',
      void: 'ONGELDIG',
    },
  },
  ja: {
    holder: '所有者',
    defaultTier: '区分',
    primaryMode: 'メイン',
    nfcReady: 'NFC対応 • 光学コード予備',
    refreshesIn: '{s}秒後に更新',
    prev: '前へ',
    next: '次へ',
    status: {
      active: '有効',
      ready: '準備完了',
      pending: '保留中',
      redeemed: '使用済み',
      transferred: '譲渡済み',
      archive: 'アーカイブ',
      expired: '期限切れ',
      void: '無効',
    },
  },
  zh: {
    holder: '持有人',
    defaultTier: '类别',
    primaryMode: '主凭证',
    nfcReady: 'NFC就绪 • 光学备用码',
    refreshesIn: '{s}秒后刷新',
    prev: '上一张',
    next: '下一张',
    status: {
      active: '有效',
      ready: '就绪',
      pending: '待处理',
      redeemed: '已核销',
      transferred: '已转让',
      archive: '已归档',
      expired: '已过期',
      void: '已作废',
    },
  },
  ar: {
    holder: 'حامل البطاقة',
    defaultTier: 'الفئة',
    primaryMode: 'الأساسي',
    nfcReady: 'NFC جاهز • رمز ضوئي بديل',
    refreshesIn: 'يتجدد خلال {s} ث',
    prev: 'السابق',
    next: 'التالي',
    status: {
      active: 'نشط',
      ready: 'جاهز',
      pending: 'قيد الانتظار',
      redeemed: 'مستخدم',
      transferred: 'محوّل',
      archive: 'أرشيف',
      expired: 'منتهي',
      void: 'ملغى',
    },
  },
};

export function detectDeviceLocale(): string {
  if (typeof navigator !== 'undefined') {
    if (Array.isArray(navigator.languages) && navigator.languages.length > 0 && navigator.languages[0]) {
      return navigator.languages[0];
    }
    if (navigator.language) {
      return navigator.language;
    }
  }
  return 'en';
}

export function resolveUiLabels(
  locale: string,
  overrides?: Partial<PassUiLabels>
): PassUiLabels {
  const baseLang = locale.split('-')[0].toLowerCase();
  const baseDict = DICTIONARIES[baseLang] ?? DICTIONARIES.en;

  return {
    holder: overrides?.holder ?? baseDict.holder,
    defaultTier: overrides?.defaultTier ?? baseDict.defaultTier,
    primaryMode: overrides?.primaryMode ?? baseDict.primaryMode,
    nfcReady: overrides?.nfcReady ?? baseDict.nfcReady,
    refreshesIn: overrides?.refreshesIn ?? baseDict.refreshesIn,
    prev: overrides?.prev ?? baseDict.prev,
    next: overrides?.next ?? baseDict.next,
    status: {
      ...baseDict.status,
      ...(overrides?.status ?? {}),
    },
  };
}

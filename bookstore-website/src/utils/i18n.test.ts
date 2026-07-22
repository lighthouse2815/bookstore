import { describe, expect, it } from 'vitest'
import {
  getCategoryLabel,
  getDigitalAccessStatusLabel,
  getDigitalAccessTypeLabel,
  getDigitalAssetFormatLabel,
} from '@/utils/i18n'

const translations: Record<string, string> = {
  'library.accessStatus.ACTIVE': 'Active',
  'library.accessType.PURCHASED': 'Purchased',
  'library.format.AUDIO': 'Audiobook',
}

const t = (key: string) => translations[key] ?? key

describe('i18n label helpers', () => {
  const category = {
    id: 'category-1',
    code: 'LITERATURE',
    name: 'Văn học',
    translations: {
      vi: { locale: 'vi' as const, name: 'Văn học', description: null },
      en: { locale: 'en' as const, name: 'Literature', description: null },
    },
  }

  it('reads the selected locale from backend category data', () => {
    expect(getCategoryLabel(category, 'vi')).toBe('Văn học')
    expect(getCategoryLabel(category, 'en')).toBe('Literature')
  })

  it('keeps a legacy category string unchanged as a compatibility fallback', () => {
    expect(getCategoryLabel('Sách địa phương', 'en')).toBe('Sách địa phương')
  })

  it('translates library enum values', () => {
    expect(getDigitalAccessStatusLabel('ACTIVE', t)).toBe('Active')
    expect(getDigitalAccessTypeLabel('PURCHASED', t)).toBe('Purchased')
    expect(getDigitalAssetFormatLabel('AUDIO', t)).toBe('Audiobook')
  })
})

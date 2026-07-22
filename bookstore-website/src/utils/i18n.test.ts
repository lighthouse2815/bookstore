import { describe, expect, it } from 'vitest'
import {
  getCategoryLabel,
  getDigitalAccessStatusLabel,
  getDigitalAccessTypeLabel,
  getDigitalAssetFormatLabel,
} from '@/utils/i18n'

const translations: Record<string, string> = {
  'categories.personalDevelopment': 'Personal development',
  'categories.businessManagement': 'Business & management',
  'categories.artsCreativity': 'Arts & creativity',
  'categories.philosophy': 'Philosophy',
  'categories.contemporaryLiterature': 'Contemporary literature',
  'categories.mystery': 'Mystery',
  'categories.education': 'Education',
  'categories.sciFi': 'Science fiction',
  'categories.scienceTechnology': 'Science & technology',
  'categories.psychology': 'Psychology',
  'categories.historyMemoir': 'History & memoir',
  'categories.children': "Children's books",
  'categories.fantasy': 'Fantasy',
  'categories.literature': 'Literature',
  'library.accessStatus.ACTIVE': 'Active',
  'library.accessType.PURCHASED': 'Purchased',
  'library.format.AUDIO': 'Audiobook',
}

const t = (key: string) => translations[key] ?? key

describe('i18n label helpers', () => {
  it.each([
    ['Kỹ năng & phát triển bản thân', 'Personal development'],
    ['Kinh doanh & quản trị', 'Business & management'],
    ['Nghệ thuật & sáng tạo', 'Arts & creativity'],
    ['Triết học', 'Philosophy'],
    ['Văn học đương đại', 'Contemporary literature'],
    ['Trinh thám', 'Mystery'],
    ['Giáo dục', 'Education'],
    ['Khoa học viễn tưởng', 'Science fiction'],
    ['Khoa học & công nghệ', 'Science & technology'],
    ['Tâm lý học', 'Psychology'],
    ['Lịch sử & hồi ký', 'History & memoir'],
    ['Thiếu nhi', "Children's books"],
    ['Giả tưởng & kỳ ảo', 'Fantasy'],
    ['Văn học', 'Literature'],
  ])('maps %s to the selected locale', (category, expected) => {
    expect(getCategoryLabel(category, t)).toBe(expected)
  })

  it('keeps unknown product categories unchanged', () => {
    expect(getCategoryLabel('Sách địa phương', t)).toBe('Sách địa phương')
  })

  it('translates library enum values', () => {
    expect(getDigitalAccessStatusLabel('ACTIVE', t)).toBe('Active')
    expect(getDigitalAccessTypeLabel('PURCHASED', t)).toBe('Purchased')
    expect(getDigitalAssetFormatLabel('AUDIO', t)).toBe('Audiobook')
  })
})

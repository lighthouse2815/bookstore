import { describe, expect, it } from 'vitest'
import { formatYearValue } from '@/utils/locale-format'

describe('locale formatters', () => {
  it.each(['vi-VN', 'en-US'])(
    'formats publication years without grouping for %s',
    (locale) => {
      expect(formatYearValue(1997, locale)).toBe('1997')
    },
  )
})

import { describe, expect, it } from 'vitest'
import {
  getDisplayReasonCodes,
  getRecommendationReasonKey,
} from '@/utils/recommendation'

describe('recommendation helpers', () => {
  it('maps every API reason code to its language-neutral i18n key', () => {
    expect(getRecommendationReasonKey('FAVORITE_CATEGORY')).toBe(
      'recommendations.reasons.FAVORITE_CATEGORY',
    )
    expect(getRecommendationReasonKey('FALLBACK_POPULAR')).toBe(
      'recommendations.reasons.FALLBACK_POPULAR',
    )
  })

  it('limits visual reason chips to two without changing their priority', () => {
    expect(
      getDisplayReasonCodes([
        'FAVORITE_AUTHOR',
        'FAVORITE_CATEGORY',
        'PURCHASE_HISTORY',
      ]),
    ).toEqual(['FAVORITE_AUTHOR', 'FAVORITE_CATEGORY'])
  })
})

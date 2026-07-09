import { describe, expect, it } from 'vitest'
import type { CouponResponse } from '@/types/coupon'
import type { CouponGameStorageLike } from '@/types/coupon-game'
import {
  createCouponGameResult,
  filterCouponGameCandidates,
  getCouponGameDateKey,
  loadCouponGameResult,
  saveCouponGameResult,
  selectCouponGameWinner,
} from '@/utils/coupon-game'

const NOW = new Date('2026-07-09T09:00:00.000Z')

const baseCoupon: CouponResponse = {
  id: 'coupon-base',
  code: 'BASE10',
  description: 'Giảm nhẹ đầu ngày',
  couponType: 'BOOK',
  discountType: 'PERCENTAGE',
  discountValue: 10,
  minOrderAmount: 100_000,
  maxDiscountAmount: 30_000,
  maxUsageCount: null,
  usedCount: 0,
  startsAt: '2026-07-01T00:00:00.000Z',
  expiresAt: '2026-07-31T23:59:59.000Z',
  active: true,
  createdAt: '2026-07-01T00:00:00.000Z',
  updatedAt: null,
}

describe('coupon-game utils', () => {
  it('selects a weighted coupon from active candidates', () => {
    const strongerCoupon: CouponResponse = {
      ...baseCoupon,
      id: 'coupon-strong',
      code: 'MEGA25',
      discountValue: 25,
      maxDiscountAmount: 100_000,
    }
    const lighterCoupon: CouponResponse = {
      ...baseCoupon,
      id: 'coupon-light',
      code: 'FIX30K',
      discountType: 'FIXED_AMOUNT',
      discountValue: 30_000,
      maxDiscountAmount: 20_000,
    }

    expect(
      selectCouponGameWinner([strongerCoupon, lighterCoupon], {
        now: NOW,
        random: () => 0.1,
      }),
    ).toEqual(strongerCoupon)
    expect(
      selectCouponGameWinner([strongerCoupon, lighterCoupon], {
        now: NOW,
        random: () => 0.95,
      }),
    ).toEqual(lighterCoupon)
  })

  it('filters out inactive, expired, future, and exhausted coupons', () => {
    const candidates = filterCouponGameCandidates(
      [
        baseCoupon,
        {
          ...baseCoupon,
          id: 'inactive',
          code: 'OFFLINE',
          active: false,
        },
        {
          ...baseCoupon,
          id: 'expired',
          code: 'OLD',
          expiresAt: '2026-07-05T00:00:00.000Z',
        },
        {
          ...baseCoupon,
          id: 'future',
          code: 'NEXTWEEK',
          startsAt: '2026-07-10T00:00:00.000Z',
        },
        {
          ...baseCoupon,
          id: 'exhausted',
          code: 'FULL',
          maxUsageCount: 10,
          usedCount: 10,
        },
      ],
      NOW,
    )

    expect(candidates).toEqual([baseCoupon])
  })

  it('persists and reloads the daily result for the same day only', () => {
    const storage = createStorageMock()
    const result = createCouponGameResult(baseCoupon, NOW)

    saveCouponGameResult(storage, result)

    expect(loadCouponGameResult(storage, NOW)).toEqual(result)
    expect(
      loadCouponGameResult(storage, new Date('2026-07-10T09:00:00.000Z')),
    ).toBeNull()
    expect(storage.getItem('bookstore.coupon-game.daily-result')).toBeNull()
  })

  it('returns null when there are no eligible coupons', () => {
    expect(
      selectCouponGameWinner(
        [
          {
            ...baseCoupon,
            code: 'DONE',
            expiresAt: '2026-07-01T00:00:00.000Z',
          },
        ],
        {
          now: NOW,
        },
      ),
    ).toBeNull()
  })

  it('uses the local calendar day key for the one-play-per-day rule', () => {
    expect(getCouponGameDateKey(new Date(2026, 6, 9, 23, 59, 59))).toBe(
      '2026-07-09',
    )
  })
})

function createStorageMock(): CouponGameStorageLike {
  const storage = new Map<string, string>()

  return {
    getItem: (key) => storage.get(key) ?? null,
    setItem: (key, value) => {
      storage.set(key, value)
    },
    removeItem: (key) => {
      storage.delete(key)
    },
  }
}

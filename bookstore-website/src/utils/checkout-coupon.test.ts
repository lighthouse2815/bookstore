import { describe, expect, it } from 'vitest'
import type { CouponResponse } from '@/types/coupon'
import {
  calculateCouponDiscount,
  filterCouponsByType,
  findCouponByCode,
  normalizeCouponCode,
} from '@/utils/checkout-coupon'

const bookCoupon: CouponResponse = {
  id: 'coupon-book',
  code: 'BOOK10',
  description: null,
  couponType: 'BOOK',
  discountType: 'PERCENTAGE',
  discountValue: 10,
  minOrderAmount: 100_000,
  maxDiscountAmount: 50_000,
  maxUsageCount: null,
  usedCount: 0,
  startsAt: '2026-06-01T00:00:00.000Z',
  expiresAt: '2026-06-30T23:59:59.000Z',
  active: true,
  createdAt: '2026-06-01T00:00:00.000Z',
  updatedAt: null,
}

const shippingCoupon: CouponResponse = {
  ...bookCoupon,
  id: 'coupon-ship',
  code: 'SHIPFREE',
  couponType: 'SHIPPING',
  discountType: 'FIXED_AMOUNT',
  discountValue: 25_000,
  minOrderAmount: 0,
}

describe('checkout-coupon utils', () => {
  it('filters coupons by couponType', () => {
    expect(filterCouponsByType([bookCoupon, shippingCoupon], 'BOOK')).toEqual([
      bookCoupon,
    ])
    expect(
      filterCouponsByType([bookCoupon, shippingCoupon], 'SHIPPING'),
    ).toEqual([shippingCoupon])
  })

  it('normalizes coupon codes consistently', () => {
    expect(normalizeCouponCode('  book10 ')).toBe('BOOK10')
    expect(normalizeCouponCode('')).toBe('')
    expect(normalizeCouponCode(null)).toBe('')
  })

  it('finds coupon by code and couponType only', () => {
    expect(
      findCouponByCode([bookCoupon, shippingCoupon], '  book10 ', 'BOOK'),
    ).toEqual(bookCoupon)
    expect(
      findCouponByCode([bookCoupon, shippingCoupon], 'shipfree', 'BOOK'),
    ).toBeNull()
    expect(
      findCouponByCode([bookCoupon, shippingCoupon], 'missing', 'SHIPPING'),
    ).toBeNull()
  })

  it('calculates percentage discount with max cap', () => {
    expect(calculateCouponDiscount(bookCoupon, 600_000, 600_000)).toBe(50_000)
  })

  it('does not discount when subtotal does not meet the minimum amount', () => {
    expect(calculateCouponDiscount(bookCoupon, 50_000, 50_000)).toBe(0)
  })

  it('does not exceed the applicable amount', () => {
    expect(calculateCouponDiscount(shippingCoupon, 500_000, 10_000)).toBe(
      10_000,
    )
  })
})

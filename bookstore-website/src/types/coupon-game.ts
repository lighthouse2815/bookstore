import type { CouponDiscountType, CouponType } from '@/types/coupon'

export type CouponGameCouponSummary = {
  code: string
  description: string | null
  couponType: CouponType
  discountType: CouponDiscountType
  discountValue: number
  minOrderAmount: number
  maxDiscountAmount: number | null
  expiresAt: string | null
}

export type CouponGameResult = {
  date: string
  playedAt: string
  couponCode: string
  couponSummary: CouponGameCouponSummary
}

export type CouponGameStorageLike = Pick<
  Storage,
  'getItem' | 'setItem' | 'removeItem'
>

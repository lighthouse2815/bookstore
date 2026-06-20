import type { CouponResponse, CouponType } from '@/types/coupon'

export function filterCouponsByType(
  coupons: CouponResponse[],
  couponType: CouponType,
) {
  return coupons.filter((coupon) => coupon.couponType === couponType)
}

export function findCouponByCode(
  coupons: CouponResponse[],
  couponCode: string,
  couponType: CouponType,
) {
  const normalizedCouponCode = normalizeCouponCode(couponCode)

  if (!normalizedCouponCode) {
    return null
  }

  return (
    coupons.find(
      (coupon) =>
        coupon.couponType === couponType &&
        coupon.code.toUpperCase() === normalizedCouponCode,
    ) ?? null
  )
}

export function normalizeCouponCode(couponCode: string | null | undefined) {
  const normalizedCouponCode = couponCode?.trim().toUpperCase()
  return normalizedCouponCode ? normalizedCouponCode : ''
}

export function calculateCouponDiscount(
  coupon: CouponResponse,
  orderSubtotal: number,
  applicableAmount: number,
) {
  if (orderSubtotal < coupon.minOrderAmount || applicableAmount <= 0) {
    return 0
  }

  const rawDiscount =
    coupon.discountType === 'PERCENTAGE'
      ? (applicableAmount * coupon.discountValue) / 100
      : coupon.discountValue
  const cappedDiscount =
    coupon.maxDiscountAmount === null
      ? rawDiscount
      : Math.min(rawDiscount, coupon.maxDiscountAmount)

  return Math.min(Math.max(0, cappedDiscount), applicableAmount)
}

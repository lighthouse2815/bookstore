export type CouponDiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT'

export type CouponResponse = {
  id: string
  code: string
  description: string | null
  discountType: CouponDiscountType
  discountValue: number
  minOrderAmount: number
  maxDiscountAmount: number | null
  maxUsageCount: number | null
  usedCount: number
  startsAt: string
  expiresAt: string
  active: boolean
  createdAt: string
  updatedAt: string | null
}

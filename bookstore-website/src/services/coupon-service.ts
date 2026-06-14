import api from './api'
import type { ApiResponse } from '@/types/api'
import type { CouponResponse } from '@/types/coupon'
import { unwrapResponse } from '@/utils'

export async function getActiveCoupons(): Promise<CouponResponse[]> {
  const response =
    await api.get<ApiResponse<CouponResponse[]>>('/coupons/active')
  return unwrapResponse(response)
}

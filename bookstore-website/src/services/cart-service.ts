import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AddCartItemRequest,
  AddDigitalCartItemRequest,
  BestCouponSuggestion,
  CartResponse,
  GetBestCartCouponParams,
  UpdateCartItemRequest,
} from '@/types/cart'
import { unwrapResponse } from '@/utils'

export async function getMyCart(): Promise<CartResponse> {
  const response = await api.get<ApiResponse<CartResponse>>('/cart')
  return unwrapResponse(response)
}

export async function addCartItem(
  data: AddCartItemRequest,
): Promise<CartResponse> {
  const response = await api.post<ApiResponse<CartResponse>>('/cart/items', data)
  return unwrapResponse(response)
}

export async function addDigitalCartItem(
  data: AddDigitalCartItemRequest,
): Promise<CartResponse> {
  const response = await api.post<ApiResponse<CartResponse>>(
    '/cart/items/digital',
    data,
  )
  return unwrapResponse(response)
}

export async function updateCartItem(
  itemId: string,
  data: UpdateCartItemRequest,
): Promise<CartResponse> {
  const response = await api.put<ApiResponse<CartResponse>>(
    `/cart/items/${itemId}`,
    data,
  )
  return unwrapResponse(response)
}

export async function removeCartItem(itemId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/cart/items/${itemId}`)
}

export async function clearMyCart(): Promise<void> {
  await api.delete<ApiResponse<null>>('/cart/items')
}

export async function getBestCartCoupon(
  params: GetBestCartCouponParams = {},
): Promise<BestCouponSuggestion> {
  const queryParams = {
    shippingMethod: params.shippingMethod,
    itemIds: params.itemIds?.length ? params.itemIds.join(',') : undefined,
  }

  const response = await api.get<ApiResponse<BestCouponSuggestion>>(
    '/cart/best-coupon',
    {
      params: queryParams,
    },
  )
  return unwrapResponse(response)
}

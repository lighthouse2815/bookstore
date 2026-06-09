import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AddCartItemRequest,
  CartResponse,
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

export async function updateCartItem(
  bookId: string,
  data: UpdateCartItemRequest,
): Promise<CartResponse> {
  const response = await api.put<ApiResponse<CartResponse>>(
    `/cart/items/${bookId}`,
    data,
  )
  return unwrapResponse(response)
}

export async function removeCartItem(bookId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/cart/items/${bookId}`)
}

export async function clearMyCart(): Promise<void> {
  await api.delete<ApiResponse<null>>('/cart/items')
}

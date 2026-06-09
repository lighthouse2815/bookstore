import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  CheckoutRequest,
  OrderResponse,
  UpdateOrderStatusRequest,
} from '@/types/order'
import { unwrapResponse } from '@/utils'

export async function checkout(data: CheckoutRequest): Promise<OrderResponse> {
  const response = await api.post<ApiResponse<OrderResponse>>(
    '/orders/checkout',
    data,
  )
  return unwrapResponse(response)
}

export async function getMyOrders(): Promise<OrderResponse[]> {
  const response = await api.get<ApiResponse<OrderResponse[]>>('/orders/my')
  return unwrapResponse(response)
}

export async function getMyOrder(id: string): Promise<OrderResponse> {
  const response = await api.get<ApiResponse<OrderResponse>>(`/orders/${id}`)
  return unwrapResponse(response)
}

export async function getAdminOrders(): Promise<OrderResponse[]> {
  const response = await api.get<ApiResponse<OrderResponse[]>>('/admin/orders')
  return unwrapResponse(response)
}

export async function getAdminOrder(id: string): Promise<OrderResponse> {
  const response = await api.get<ApiResponse<OrderResponse>>(
    `/admin/orders/${id}`,
  )
  return unwrapResponse(response)
}

export async function updateAdminOrderStatus(
  id: string,
  data: UpdateOrderStatusRequest,
): Promise<OrderResponse> {
  const response = await api.put<ApiResponse<OrderResponse>>(
    `/admin/orders/${id}/status`,
    data,
  )
  return unwrapResponse(response)
}

import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  CreateOrderRequest,
  CreateOrderResponse,
  OrderResponse,
  UpdateOrderStatusRequest,
} from '@/types/order'
import { unwrapResponse } from '@/utils'

type BackendCreateOrderRequest = {
  cartItemIds: string[]
  addressId: string | null
  shippingMethod: CreateOrderRequest['shippingMethod']
  paymentMethod: CreateOrderRequest['paymentMethod']
  couponCode: string | null
  note: string | null
}

export async function createOrder(
  payload: CreateOrderRequest,
): Promise<CreateOrderResponse> {
  const requestBody: BackendCreateOrderRequest = {
    cartItemIds: payload.cartItemIds,
    addressId: payload.addressId,
    shippingMethod: payload.shippingMethod,
    paymentMethod: payload.paymentMethod,
    couponCode:
      payload.bookCouponCode?.trim() ||
      payload.shippingCouponCode?.trim() ||
      null,
    note: payload.note?.trim() || null,
  }

  const response = await api.post<ApiResponse<CreateOrderResponse>>(
    '/orders/checkout',
    requestBody,
  )
  return unwrapResponse(response)
}

export async function getMyOrders(): Promise<OrderResponse[]> {
  const response = await api.get<ApiResponse<OrderResponse[]>>('/orders/my')
  return unwrapResponse(response)
}

export async function getOrderById(orderId: string): Promise<OrderResponse> {
  const response = await api.get<ApiResponse<OrderResponse>>(`/orders/${orderId}`)
  return unwrapResponse(response)
}

export async function getMyOrder(id: string): Promise<OrderResponse> {
  return getOrderById(id)
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

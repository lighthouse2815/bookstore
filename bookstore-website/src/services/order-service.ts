import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  CreateOrderRequest,
  CreateOrderResponse,
  CancelOrderRequest,
  OrderResponse,
  OrderTimelineEventResponse,
  UpdateOrderStatusRequest,
} from '@/types/order'
import { unwrapResponse } from '@/utils'
import { toPageResult } from '@/services/pagination'
import type { PageRequest, PageResult } from '@/types/pagination'

type BackendCreateOrderRequest = {
  cartItemIds: string[]
  addressId: string | null
  shippingMethod: CreateOrderRequest['shippingMethod']
  paymentMethod: CreateOrderRequest['paymentMethod']
  bookCouponCode: string | null
  shippingCouponCode: string | null
  note: string | null
}

export async function createOrder(
  payload: CreateOrderRequest,
  idempotencyKey: string,
): Promise<CreateOrderResponse> {
  const requestBody: BackendCreateOrderRequest = {
    cartItemIds: payload.cartItemIds,
    addressId: payload.addressId,
    shippingMethod: payload.shippingMethod,
    paymentMethod: payload.paymentMethod,
    bookCouponCode: payload.bookCouponCode?.trim() || null,
    shippingCouponCode: payload.shippingCouponCode?.trim() || null,
    note: payload.note?.trim() || null,
  }

  const response = await api.post<ApiResponse<CreateOrderResponse>>(
    '/orders/checkout',
    requestBody,
    {
      headers: {
        'Idempotency-Key': idempotencyKey,
      },
    },
  )
  return unwrapResponse(response)
}

export async function getMyOrders(): Promise<OrderResponse[]> {
  const response = await api.get<ApiResponse<OrderResponse[]>>('/orders/my')
  return unwrapResponse(response)
}

export async function getMyOrdersPage(
  params: PageRequest = {},
): Promise<PageResult<OrderResponse>> {
  const request = { page: params.page ?? 0, size: params.size ?? 10 }
  const response = await api.get<ApiResponse<OrderResponse[]>>('/orders/my', {
    params: request,
  })
  return toPageResult(unwrapResponse(response), response.headers, request)
}

export async function getOrderById(orderId: string): Promise<OrderResponse> {
  const response = await api.get<ApiResponse<OrderResponse>>(`/orders/${orderId}`)
  return unwrapResponse(response)
}

export async function getMyOrder(id: string): Promise<OrderResponse> {
  return getOrderById(id)
}

export async function cancelMyOrder(
  id: string,
  data: CancelOrderRequest,
): Promise<OrderResponse> {
  const response = await api.put<ApiResponse<OrderResponse>>(
    `/orders/${id}/cancel`,
    data,
  )
  return unwrapResponse(response)
}

export async function getAdminOrders(): Promise<OrderResponse[]> {
  const response = await api.get<ApiResponse<OrderResponse[]>>('/admin/orders')
  return unwrapResponse(response)
}

export async function getAdminOrdersPage(
  params: PageRequest = {},
): Promise<PageResult<OrderResponse>> {
  const request = { page: params.page ?? 0, size: params.size ?? 10 }
  const response = await api.get<ApiResponse<OrderResponse[]>>('/admin/orders', {
    params: request,
  })
  return toPageResult(unwrapResponse(response), response.headers, request)
}

export async function getAdminOrder(id: string): Promise<OrderResponse> {
  const response = await api.get<ApiResponse<OrderResponse>>(
    `/admin/orders/${id}`,
  )
  return unwrapResponse(response)
}

export async function getMyOrderTimeline(
  orderId: string,
): Promise<OrderTimelineEventResponse[]> {
  const response = await api.get<ApiResponse<OrderTimelineEventResponse[]>>(
    `/orders/${orderId}/timeline`,
  )
  return unwrapResponse(response)
}

export async function getAdminOrderTimeline(
  orderId: string,
): Promise<OrderTimelineEventResponse[]> {
  const response = await api.get<ApiResponse<OrderTimelineEventResponse[]>>(
    `/admin/orders/${orderId}/timeline`,
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

import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AssignShipmentRequest,
  ShipmentResponse,
} from '@/types/shipment'
import { unwrapResponse } from '@/utils'
import { toPageResult } from '@/services/pagination'
import type { PageRequest, PageResult } from '@/types/pagination'

export async function assignAdminShipment(
  data: AssignShipmentRequest,
): Promise<ShipmentResponse> {
  const response = await api.post<ApiResponse<ShipmentResponse>>(
    '/admin/shipments',
    data,
  )
  return unwrapResponse(response)
}

export async function getAdminShipments(): Promise<ShipmentResponse[]> {
  const response = await api.get<ApiResponse<ShipmentResponse[]>>(
    '/admin/shipments',
  )
  return unwrapResponse(response)
}

export async function getAdminShipmentsPage(
  params: PageRequest = {},
): Promise<PageResult<ShipmentResponse>> {
  const request = { page: params.page ?? 0, size: params.size ?? 10 }
  const response = await api.get<ApiResponse<ShipmentResponse[]>>(
    '/admin/shipments',
    { params: request },
  )
  return toPageResult(unwrapResponse(response), response.headers, request)
}

export async function getAdminShipment(id: string): Promise<ShipmentResponse> {
  const response = await api.get<ApiResponse<ShipmentResponse>>(
    `/admin/shipments/${id}`,
  )
  return unwrapResponse(response)
}

export async function confirmAdminShipmentDelivered(
  id: string,
): Promise<ShipmentResponse> {
  const response = await api.put<ApiResponse<ShipmentResponse>>(
    `/admin/shipments/${id}/confirm-delivered`,
  )
  return unwrapResponse(response)
}

import api from './api'
import { toPageResult } from '@/services/pagination'
import type { ApiResponse } from '@/types/api'
import type { PageRequest, PageResult } from '@/types/pagination'
import type {
  ApproveReturnRequestRequest,
  CreateReturnRequestRequest,
  RejectReturnRequestRequest,
  ReturnRequestResponse,
  ReturnRequestStatus,
} from '@/types/return-request'
import { unwrapResponse } from '@/utils'

type ReturnRequestListParams = PageRequest & {
  status?: ReturnRequestStatus | 'ALL'
  orderId?: string | null
  userId?: string | null
}

export async function createReturnRequest(
  orderId: string,
  payload: CreateReturnRequestRequest,
): Promise<ReturnRequestResponse> {
  const response = await api.post<ApiResponse<ReturnRequestResponse>>(
    `/orders/${orderId}/return-request`,
    {
      reason: payload.reason.trim(),
      requestedRefundAmount: payload.requestedRefundAmount ?? null,
    },
  )
  return unwrapResponse(response)
}

export async function getMyReturnRequestsPage(
  params: ReturnRequestListParams = {},
): Promise<PageResult<ReturnRequestResponse>> {
  const request = {
    page: params.page ?? 0,
    size: params.size ?? 10,
    status:
      params.status && params.status !== 'ALL' ? params.status : undefined,
    orderId: params.orderId ?? undefined,
  }
  const response = await api.get<ApiResponse<ReturnRequestResponse[]>>(
    '/return-requests/my',
    { params: request },
  )
  return toPageResult(unwrapResponse(response), response.headers, request)
}

export async function getMyReturnRequest(
  requestId: string,
): Promise<ReturnRequestResponse> {
  const response = await api.get<ApiResponse<ReturnRequestResponse>>(
    `/return-requests/${requestId}`,
  )
  return unwrapResponse(response)
}

export async function cancelReturnRequest(
  requestId: string,
): Promise<ReturnRequestResponse> {
  const response = await api.put<ApiResponse<ReturnRequestResponse>>(
    `/return-requests/${requestId}/cancel`,
  )
  return unwrapResponse(response)
}

export async function getAdminReturnRequestsPage(
  params: ReturnRequestListParams = {},
): Promise<PageResult<ReturnRequestResponse>> {
  const request = {
    page: params.page ?? 0,
    size: params.size ?? 10,
    status:
      params.status && params.status !== 'ALL' ? params.status : undefined,
    orderId: params.orderId ?? undefined,
    userId: params.userId ?? undefined,
  }
  const response = await api.get<ApiResponse<ReturnRequestResponse[]>>(
    '/admin/return-requests',
    { params: request },
  )
  return toPageResult(unwrapResponse(response), response.headers, request)
}

export async function getAdminReturnRequest(
  requestId: string,
): Promise<ReturnRequestResponse> {
  const response = await api.get<ApiResponse<ReturnRequestResponse>>(
    `/admin/return-requests/${requestId}`,
  )
  return unwrapResponse(response)
}

export async function approveReturnRequest(
  requestId: string,
  payload: ApproveReturnRequestRequest,
): Promise<ReturnRequestResponse> {
  const response = await api.put<ApiResponse<ReturnRequestResponse>>(
    `/admin/return-requests/${requestId}/approve`,
    {
      adminNote: payload.adminNote?.trim() || null,
      approvedRefundAmount: payload.approvedRefundAmount ?? null,
      restock: payload.restock,
    },
  )
  return unwrapResponse(response)
}

export async function rejectReturnRequest(
  requestId: string,
  payload: RejectReturnRequestRequest,
): Promise<ReturnRequestResponse> {
  const response = await api.put<ApiResponse<ReturnRequestResponse>>(
    `/admin/return-requests/${requestId}/reject`,
    {
      adminNote: payload.adminNote.trim(),
    },
  )
  return unwrapResponse(response)
}

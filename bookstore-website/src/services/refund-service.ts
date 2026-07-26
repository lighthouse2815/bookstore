import api from './api'
import { toPageResult } from './pagination'
import type { ApiResponse } from '@/types/api'
import type { PageRequest, PageResult } from '@/types/pagination'
import type { CreateRefundPayload, Refund, RefundMethod, RefundStatus } from '@/types/refund'
import { unwrapResponse } from '@/utils'

type RefundFilters = PageRequest & { status?: RefundStatus; method?: RefundMethod; from?: string; to?: string }

export async function getRefundPage(filters: RefundFilters = {}): Promise<PageResult<Refund>> {
  const request = { page: filters.page ?? 0, size: filters.size ?? 20 }
  const response = await api.get<ApiResponse<Refund[]>>('/admin/refunds', { params: { ...filters, ...request, from: toIso(filters.from), to: toIso(filters.to) } })
  return toPageResult(unwrapResponse(response), response.headers, request)
}

export async function getRefund(id: string): Promise<Refund> {
  return unwrapResponse(await api.get<ApiResponse<Refund>>(`/admin/refunds/${id}`))
}

export async function createRefund(orderId: string, payload: CreateRefundPayload, idempotencyKey = crypto.randomUUID()): Promise<Refund> {
  return unwrapResponse(await api.post<ApiResponse<Refund>>(`/admin/orders/${orderId}/refunds`, payload, { headers: { 'Idempotency-Key': idempotencyKey } }))
}

export async function approveRefund(id: string): Promise<Refund> { return unwrapResponse(await api.put<ApiResponse<Refund>>(`/admin/refunds/${id}/approve`)) }
export async function processRefund(id: string): Promise<Refund> { return unwrapResponse(await api.put<ApiResponse<Refund>>(`/admin/refunds/${id}/processing`)) }
export async function succeedRefund(id: string, payload: { externalReference: string; evidenceUrl?: string; evidenceMetadata?: string }): Promise<Refund> {
  return unwrapResponse(await api.put<ApiResponse<Refund>>(`/admin/refunds/${id}/succeed`, payload))
}
export async function failRefund(id: string, failureReason: string): Promise<Refund> { return unwrapResponse(await api.put<ApiResponse<Refund>>(`/admin/refunds/${id}/fail`, { failureReason })) }
export async function cancelRefund(id: string, reason?: string): Promise<Refund> { return unwrapResponse(await api.put<ApiResponse<Refund>>(`/admin/refunds/${id}/cancel`, { reason })) }

function toIso(value?: string) { if (!value) return undefined; const date = new Date(value); return Number.isNaN(date.getTime()) ? undefined : date.toISOString() }

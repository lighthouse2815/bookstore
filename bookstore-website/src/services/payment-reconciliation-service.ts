import api from './api'
import { toPageResult } from './pagination'
import type { ApiResponse } from '@/types/api'
import type { PageRequest, PageResult } from '@/types/pagination'
import type {
  PaymentReconciliationIssue,
  PaymentReconciliationIssueType,
  PaymentReconciliationStatus,
} from '@/types/payment-reconciliation'
import { unwrapResponse } from '@/utils'

export type PaymentReconciliationFilters = PageRequest & {
  status?: PaymentReconciliationStatus
  issueType?: PaymentReconciliationIssueType
  from?: string
  to?: string
}

export async function getPaymentReconciliationPage(
  filters: PaymentReconciliationFilters = {},
): Promise<PageResult<PaymentReconciliationIssue>> {
  const request = { page: filters.page ?? 0, size: filters.size ?? 20 }
  const response = await api.get<ApiResponse<PaymentReconciliationIssue[]>>(
    '/admin/payment-reconciliation',
    {
      params: {
        ...filters,
        from: toIsoInstant(filters.from),
        to: toIsoInstant(filters.to),
        ...request,
      },
    },
  )
  return toPageResult(unwrapResponse(response), response.headers, request)
}

function toIsoInstant(value: string | undefined) {
  if (!value) return undefined
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? undefined : date.toISOString()
}

export async function resolvePaymentReconciliationIssue(
  id: string,
  resolutionNote: string,
): Promise<PaymentReconciliationIssue> {
  const response = await api.put<ApiResponse<PaymentReconciliationIssue>>(
    `/admin/payment-reconciliation/${id}/resolve`,
    { resolutionNote },
  )
  return unwrapResponse(response)
}

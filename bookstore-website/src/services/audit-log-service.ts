import api from './api'
import { toPageResult } from '@/services/pagination'
import type { ApiResponse } from '@/types/api'
import type { AdminAuditLogResponse } from '@/types/audit-log'
import type { PageRequest, PageResult } from '@/types/pagination'
import { unwrapResponse } from '@/utils'

export type AdminAuditLogFilter = PageRequest & {
  action?: string
  targetType?: string
  actorId?: string
  from?: string
  to?: string
}

export async function getAdminAuditLogsPage(
  params: AdminAuditLogFilter = {},
): Promise<PageResult<AdminAuditLogResponse>> {
  const request = {
    page: params.page ?? 0,
    size: params.size ?? 10,
    action: params.action || undefined,
    targetType: params.targetType || undefined,
    actorId: params.actorId || undefined,
    from: params.from || undefined,
    to: params.to || undefined,
  }

  const response = await api.get<ApiResponse<AdminAuditLogResponse[]>>(
    '/admin/audit-logs',
    {
      params: request,
    },
  )

  return toPageResult(unwrapResponse(response), response.headers, request)
}

export async function getAdminAuditLog(
  id: string,
): Promise<AdminAuditLogResponse> {
  const response = await api.get<ApiResponse<AdminAuditLogResponse>>(
    `/admin/audit-logs/${id}`,
  )
  return unwrapResponse(response)
}

import api from './api'
import { toPageResult } from './pagination'
import type { ApiResponse } from '@/types/api'
import type { PageRequest, PageResult } from '@/types/pagination'
import type { OutboxEvent, OutboxStatus } from '@/types/outbox'
import { unwrapResponse } from '@/utils'

export async function getOutboxPage(filters: PageRequest & { status?: OutboxStatus } = {}): Promise<PageResult<OutboxEvent>> {
  const request = { page: filters.page ?? 0, size: filters.size ?? 20 }
  const response = await api.get<ApiResponse<OutboxEvent[]>>('/admin/outbox', { params: { ...filters, ...request } })
  return toPageResult(unwrapResponse(response), response.headers, request)
}

export async function retryOutboxEvent(id: string): Promise<OutboxEvent> {
  return unwrapResponse(await api.post<ApiResponse<OutboxEvent>>(`/admin/outbox/${id}/retry`))
}

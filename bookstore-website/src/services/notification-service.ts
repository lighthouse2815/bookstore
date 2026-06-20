import type { AxiosResponse } from 'axios'
import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  BroadcastNotificationRequest,
  CreateNotificationRequest,
  NotificationBroadcastResponse,
  NotificationPageResult,
  NotificationQueryParams,
  NotificationResponse,
  UnreadNotificationCountResponse,
} from '@/types/notification'
import { unwrapResponse } from '@/utils'

const DEFAULT_USER_PAGE_SIZE = 10
const DEFAULT_ADMIN_PAGE_SIZE = 20

export async function getMyNotifications(
  params: NotificationQueryParams = {},
): Promise<NotificationPageResult> {
  const page = params.page ?? 0
  const size = params.size ?? DEFAULT_USER_PAGE_SIZE
  const response = await api.get<ApiResponse<NotificationResponse[]>>(
    '/notifications/my',
    {
      params: {
        page,
        size,
        ...(typeof params.read === 'boolean' ? { read: params.read } : {}),
      },
    },
  )

  return parseNotificationPageResponse(response, page, size)
}

export async function getUnreadNotificationCount(): Promise<number> {
  const response = await api.get<ApiResponse<UnreadNotificationCountResponse>>(
    '/notifications/unread-count',
  )
  return unwrapResponse(response).unreadCount
}

export async function getNotificationById(
  notificationId: string,
): Promise<NotificationResponse> {
  const response = await api.get<ApiResponse<NotificationResponse>>(
    `/notifications/${notificationId}`,
  )
  return unwrapResponse(response)
}

export async function markNotificationAsRead(
  notificationId: string,
): Promise<NotificationResponse> {
  const response = await api.put<ApiResponse<NotificationResponse>>(
    `/notifications/${notificationId}/read`,
  )
  return unwrapResponse(response)
}

export async function markAllNotificationsAsRead(): Promise<void> {
  await api.put<ApiResponse<null>>('/notifications/read-all')
}

export async function deleteNotification(notificationId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/notifications/${notificationId}`)
}

export async function getAdminNotifications(
  params: Pick<NotificationQueryParams, 'page' | 'size'> = {},
): Promise<NotificationPageResult> {
  const page = params.page ?? 0
  const size = params.size ?? DEFAULT_ADMIN_PAGE_SIZE
  const response = await api.get<ApiResponse<NotificationResponse[]>>(
    '/admin/notifications',
    {
      params: { page, size },
    },
  )

  return parseNotificationPageResponse(response, page, size)
}

export async function createAdminNotification(
  data: CreateNotificationRequest,
): Promise<NotificationResponse> {
  const response = await api.post<ApiResponse<NotificationResponse>>(
    '/admin/notifications',
    data,
  )
  return unwrapResponse(response)
}

export async function broadcastNotification(
  data: BroadcastNotificationRequest,
): Promise<NotificationBroadcastResponse> {
  const response = await api.post<ApiResponse<NotificationBroadcastResponse>>(
    '/admin/notifications/broadcast',
    data,
  )
  return unwrapResponse(response)
}

export async function deleteAdminNotification(
  notificationId: string,
): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/notifications/${notificationId}`)
}

function parseNotificationPageResponse(
  response: AxiosResponse<ApiResponse<NotificationResponse[]>>,
  fallbackPage: number,
  fallbackSize: number,
): NotificationPageResult {
  const items = unwrapResponse(response)
  const page = parseNumberHeader(response.headers['x-page'], fallbackPage)
  const size = parseNumberHeader(response.headers['x-size'], fallbackSize)
  const totalCount = parseNumberHeader(response.headers['x-total-count'], items.length)
  const hasNext = parseBooleanHeader(
    response.headers['x-has-next'],
    (page + 1) * size < totalCount,
  )

  return {
    items,
    page,
    size,
    totalCount,
    hasNext,
  }
}

function parseNumberHeader(value: unknown, fallbackValue: number) {
  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : fallbackValue
}

function parseBooleanHeader(value: unknown, fallbackValue: boolean) {
  if (typeof value === 'string') {
    return value.toLowerCase() === 'true'
  }

  return fallbackValue
}

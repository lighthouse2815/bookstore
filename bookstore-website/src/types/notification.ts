export type NotificationResponse = {
  notificationId: string
  userId: string
  title: string
  content: string
  read: boolean
  createdAt: string
  updatedAt: string
  readAt: string | null
  type: string | null
  targetType: string | null
  targetId: string | null
  link: string | null
}

export type NotificationQueryParams = {
  page?: number
  size?: number
  read?: boolean
}

export type NotificationPageResult = {
  items: NotificationResponse[]
  page: number
  size: number
  totalCount: number
  hasNext: boolean
}

export type CreateNotificationRequest = {
  userId: string
  title: string
  content: string
  type?: string | null
  targetType?: string | null
  targetId?: string | null
  link?: string | null
}

export type BroadcastNotificationRequest = {
  title: string
  content: string
  type?: string | null
  targetType?: string | null
  targetId?: string | null
  link?: string | null
}

export type NotificationBroadcastResponse = {
  createdCount: number
}

export type UnreadNotificationCountResponse = {
  unreadCount: number
}

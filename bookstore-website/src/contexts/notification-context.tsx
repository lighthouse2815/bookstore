import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  deleteNotification as deleteNotificationRequest,
  getMyNotifications,
  getUnreadNotificationCount,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '@/services/notification-service'
import {
  connectNotificationRealtime,
  disconnectNotificationRealtime,
} from '@/services/notification-realtime-service'
import { getAccessToken } from '@/services/api'
import type { NotificationResponse } from '@/types/notification'
import { getErrorMessage } from '@/utils'

type NotificationContextType = {
  notifications: NotificationResponse[]
  unreadCount: number
  isLoading: boolean
  error: string | null
  isRealtimeConnected: boolean
  refresh: () => Promise<void>
  markAsRead: (notificationId: string) => Promise<void>
  markAllAsRead: () => Promise<void>
  deleteNotification: (notificationId: string) => Promise<void>
}

const INITIAL_PAGE_SIZE = 10

const NotificationContext = createContext<NotificationContextType | undefined>(
  undefined,
)

export function NotificationProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading: isAuthLoading } = useAuth()
  const { t } = useLanguage()
  const [notifications, setNotifications] = useState<NotificationResponse[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isRealtimeConnected, setIsRealtimeConnected] = useState(false)
  const notificationsRef = useRef<NotificationResponse[]>([])

  useEffect(() => {
    notificationsRef.current = notifications
  }, [notifications])

  useEffect(() => {
    let isCancelled = false
    disconnectNotificationRealtime()

    if (isAuthLoading) {
      return
    }

    if (!isAuthenticated) {
      clearState()
      return
    }

    async function initialize() {
      setIsLoading(true)

      try {
        const [pageResult, nextUnreadCount] = await Promise.all([
          getMyNotifications({ page: 0, size: INITIAL_PAGE_SIZE }),
          getUnreadNotificationCount(),
        ])

        if (isCancelled) {
          return
        }

        setNotifications(pageResult.items)
        setUnreadCount(nextUnreadCount)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, t('notifications.errors.fetch')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }

      if (isCancelled) {
        return
      }

      const accessToken = getAccessToken()
      if (!accessToken) {
        setIsRealtimeConnected(false)
        return
      }

      connectNotificationRealtime(
        accessToken,
        (notification) => {
          if (isCancelled) {
            return
          }

          const alreadyExists = notificationsRef.current.some(
            (currentNotification) =>
              currentNotification.notificationId === notification.notificationId,
          )

          setNotifications((currentNotifications) =>
            mergeLatestNotifications(currentNotifications, notification),
          )

          if (!alreadyExists && !notification.read) {
            setUnreadCount((currentUnreadCount) => currentUnreadCount + 1)
          }

          setError(null)
          setIsRealtimeConnected(true)
          toast.success(notification.title, {
            description:
              notification.content.trim() === ''
                ? t('notifications.newNotificationFallback')
                : notification.content,
          })
        },
        () => {
          if (!isCancelled) {
            setIsRealtimeConnected(true)
          }
        },
        () => {
          if (!isCancelled) {
            setIsRealtimeConnected(false)
          }
        },
      )
    }

    void initialize()

    return () => {
      isCancelled = true
      disconnectNotificationRealtime()
      setIsRealtimeConnected(false)
    }
  }, [isAuthenticated, isAuthLoading, t])

  async function refresh() {
    if (!isAuthenticated) {
      clearState()
      return
    }

    setIsLoading(true)

    try {
      const [pageResult, nextUnreadCount] = await Promise.all([
        getMyNotifications({ page: 0, size: INITIAL_PAGE_SIZE }),
        getUnreadNotificationCount(),
      ])

      setNotifications(pageResult.items)
      setUnreadCount(nextUnreadCount)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, t('notifications.errors.fetch')))
    } finally {
      setIsLoading(false)
    }
  }

  async function markAsRead(notificationId: string) {
    const currentNotification = notificationsRef.current.find(
      (notification) => notification.notificationId === notificationId,
    )

    if (currentNotification?.read) {
      return
    }

    try {
      await markNotificationAsRead(notificationId)
      setNotifications((currentNotifications) =>
        currentNotifications.map((notification) =>
          notification.notificationId === notificationId
            ? {
                ...notification,
                read: true,
                readAt: new Date().toISOString(),
              }
            : notification,
        ),
      )
      setUnreadCount((currentUnreadCount) => Math.max(0, currentUnreadCount - 1))
    } catch (currentError) {
      throw new Error(
        getErrorMessage(currentError, t('notifications.errors.update')),
      )
    }
  }

  async function markAllAsRead() {
    try {
      await markAllNotificationsAsRead()
      setNotifications((currentNotifications) =>
        currentNotifications.map((notification) => ({
          ...notification,
          read: true,
          readAt: notification.readAt ?? new Date().toISOString(),
        })),
      )
      setUnreadCount(0)
    } catch (currentError) {
      throw new Error(
        getErrorMessage(currentError, t('notifications.errors.update')),
      )
    }
  }

  async function deleteNotification(notificationId: string) {
    const currentNotification = notificationsRef.current.find(
      (notification) => notification.notificationId === notificationId,
    )

    try {
      await deleteNotificationRequest(notificationId)
      setNotifications((currentNotifications) =>
        currentNotifications.filter(
          (notification) => notification.notificationId !== notificationId,
        ),
      )

      if (currentNotification && !currentNotification.read) {
        setUnreadCount((currentUnreadCount) =>
          Math.max(0, currentUnreadCount - 1),
        )
      }
    } catch (currentError) {
      throw new Error(
        getErrorMessage(currentError, t('notifications.errors.delete')),
      )
    }
  }

  function clearState() {
    setNotifications([])
    setUnreadCount(0)
    setIsLoading(false)
    setError(null)
    setIsRealtimeConnected(false)
  }

  const value = useMemo<NotificationContextType>(
    () => ({
      notifications,
      unreadCount,
      isLoading,
      error,
      isRealtimeConnected,
      refresh,
      markAsRead,
      markAllAsRead,
      deleteNotification,
    }),
    [notifications, unreadCount, isLoading, error, isRealtimeConnected],
  )

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  )
}

export function useNotifications() {
  const context = useContext(NotificationContext)

  if (!context) {
    throw new Error('useNotifications must be used within NotificationProvider')
  }

  return context
}

function mergeLatestNotifications(
  currentNotifications: NotificationResponse[],
  nextNotification: NotificationResponse,
) {
  const filteredNotifications = currentNotifications.filter(
    (notification) => notification.notificationId !== nextNotification.notificationId,
  )

  return [nextNotification, ...filteredNotifications].slice(0, INITIAL_PAGE_SIZE)
}

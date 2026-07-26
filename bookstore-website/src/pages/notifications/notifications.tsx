import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Bell, CheckCheck, ChevronRight, Loader2, RefreshCw, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useNotifications } from '@/contexts/notification-context'
import { getMyNotifications } from '@/services/notification-service'
import type { NotificationResponse } from '@/types/notification'
import { cn, getErrorMessage } from '@/utils'

const PAGE_SIZE = 10

type NotificationFilter = 'all' | 'unread'

export default function NotificationsPage() {
  const navigate = useNavigate()
  const { locale, formatNumber, t } = useLanguage()
  const {
    unreadCount,
    isRealtimeConnected,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    refresh,
  } = useNotifications()
  const [activeFilter, setActiveFilter] = useState<NotificationFilter>('all')
  const [notifications, setNotifications] = useState<NotificationResponse[]>([])
  const [page, setPage] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isLoadingMore, setIsLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void loadNotifications(0, true)
  }, [activeFilter])

  async function loadNotifications(nextPage: number, replace: boolean) {
    const read = activeFilter === 'unread' ? false : undefined

    if (replace) {
      setIsLoading(true)
    } else {
      setIsLoadingMore(true)
    }

    try {
      const result = await getMyNotifications({
        page: nextPage,
        size: PAGE_SIZE,
        read,
      })

      setNotifications((currentNotifications) =>
        replace
          ? result.items
          : mergeNotificationPages(currentNotifications, result.items),
      )
      setPage(result.page)
      setHasNext(result.hasNext)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, t('notifications.errors.fetch')))
    } finally {
      setIsLoading(false)
      setIsLoadingMore(false)
    }
  }

  async function handleOpenNotification(notification: NotificationResponse) {
    try {
      await markAsRead(notification.notificationId)
      if (activeFilter === 'unread') {
        setNotifications((currentNotifications) =>
          currentNotifications.filter(
            (currentNotification) =>
              currentNotification.notificationId !== notification.notificationId,
          ),
        )
      } else {
        setNotifications((currentNotifications) =>
          currentNotifications.map((currentNotification) =>
            currentNotification.notificationId === notification.notificationId
              ? {
                  ...currentNotification,
                  read: true,
                  readAt: currentNotification.readAt ?? new Date().toISOString(),
                }
              : currentNotification,
          ),
        )
      }

      if (notification.link && notification.link.trim() !== '') {
        navigate(notification.link)
      }
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    }
  }

  async function handleDeleteNotification(notification: NotificationResponse) {
    try {
      await deleteNotification(notification.notificationId)
      setNotifications((currentNotifications) =>
        currentNotifications.filter(
          (currentNotification) =>
            currentNotification.notificationId !== notification.notificationId,
        ),
      )
      toast.success(t('notifications.page.deleteSuccess'))
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    }
  }

  async function handleMarkAllAsRead() {
    try {
      await markAllAsRead()
      await refresh()

      if (activeFilter === 'unread') {
        setNotifications([])
        setHasNext(false)
      } else {
        setNotifications((currentNotifications) =>
          currentNotifications.map((notification) => ({
            ...notification,
            read: true,
            readAt: notification.readAt ?? new Date().toISOString(),
          })),
        )
      }
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    }
  }

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top,rgba(59,130,246,0.10),transparent_30%),linear-gradient(180deg,rgba(248,250,252,1),rgba(241,245,249,1))] dark:bg-[radial-gradient(circle_at_top,rgba(124,86,255,0.16),transparent_34rem),linear-gradient(180deg,rgba(24,20,38,1)_0%,rgba(18,16,29,0.98)_46%,rgba(13,12,21,1)_100%)]">
      <Header />

      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
        <section className="overflow-hidden rounded-[32px] border border-border/70 bg-background/90 shadow-[0_28px_80px_rgba(15,23,42,0.12)] backdrop-blur dark:border-white/10 dark:bg-card/88 dark:shadow-[0_28px_90px_rgba(0,0,0,0.38)]">
          <div className="border-b border-border/60 px-6 py-6 dark:border-white/10 sm:px-8">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <span className="flex size-12 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                    <Bell className="size-6" />
                  </span>
                  <div>
                    <h1 className="text-3xl font-bold text-foreground">
                      {t('notifications.page.title')}
                    </h1>
                    <p className="mt-2 max-w-2xl text-sm text-muted-foreground sm:text-base">
                      {t('notifications.page.description')}
                    </p>
                  </div>
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-3">
                <span className="rounded-full border border-primary/10 bg-card/80 px-3 py-2 text-sm font-medium text-foreground dark:border-primary/20 dark:bg-primary/10">
                  {formatNumber(unreadCount)} {t('notifications.page.unread').toLowerCase()}
                </span>
                <span className="rounded-full border border-border/60 bg-card/80 px-3 py-2 text-sm font-medium text-muted-foreground dark:border-white/10 dark:bg-background/45">
                  {isRealtimeConnected
                    ? t('notifications.realtimeConnected')
                    : t('notifications.realtimeFallback')}
                </span>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => void handleMarkAllAsRead()}
                  className="rounded-2xl"
                  disabled={unreadCount === 0}
                >
                  <CheckCheck className="mr-2 h-4 w-4" />
                  {t('notifications.page.markAll')}
                </Button>
              </div>
            </div>

            <div className="mt-6 flex flex-wrap gap-2">
              {(['all', 'unread'] as const).map((filter) => (
                <button
                  key={filter}
                  type="button"
                  onClick={() => setActiveFilter(filter)}
                  className={cn(
                    'rounded-full border px-4 py-2 text-sm font-semibold transition-colors',
                    activeFilter === filter
                      ? 'border-primary bg-primary text-primary-foreground shadow-[0_12px_30px_rgba(109,76,255,0.22)]'
                      : 'border-border/60 bg-card/80 text-muted-foreground hover:border-primary/30 hover:text-foreground dark:border-white/10 dark:bg-background/45',
                  )}
                >
                  {filter === 'all'
                    ? t('notifications.page.all')
                    : t('notifications.page.unread')}
                </button>
              ))}
            </div>
          </div>

          <div className="px-4 py-5 sm:px-6 sm:py-6">
            {isLoading ? (
              <div className="flex items-center justify-center gap-2 rounded-[24px] border border-border/60 bg-card/70 px-4 py-12 text-sm text-muted-foreground dark:border-white/10 dark:bg-background/45">
                <Loader2 className="h-4 w-4 animate-spin" />
                {t('notifications.page.loading')}
              </div>
            ) : error ? (
              <div className="rounded-[24px] border border-destructive/30 bg-destructive/10 px-4 py-4 text-sm text-destructive">
                {error}
              </div>
            ) : notifications.length === 0 ? (
              <div className="rounded-[24px] border border-dashed border-border/60 bg-card/60 px-4 py-12 text-center text-sm text-muted-foreground dark:border-white/10 dark:bg-background/35">
                {t('notifications.page.empty')}
              </div>
            ) : (
              <div className="space-y-3">
                {notifications.map((notification) => (
                  <article
                    key={notification.notificationId}
                    className={cn(
                      'flex flex-col gap-4 rounded-[28px] border px-5 py-5 text-card-foreground shadow-[0_14px_40px_rgba(15,23,42,0.08)] sm:flex-row sm:items-start sm:justify-between dark:shadow-[0_18px_45px_rgba(0,0,0,0.24)]',
                      notification.read
                        ? 'border-border/60 bg-card/80 dark:border-white/10 dark:bg-background/45'
                        : 'border-primary/25 bg-primary/8 dark:border-primary/30 dark:bg-primary/12',
                    )}
                  >
                    <button
                      type="button"
                      onClick={() => void handleOpenNotification(notification)}
                      className="min-w-0 flex-1 text-left"
                    >
                      <div className="flex items-center gap-3">
                        <p className="line-clamp-1 text-lg font-semibold text-foreground">
                          {notification.title}
                        </p>
                        {!notification.read ? (
                          <span className="h-2.5 w-2.5 shrink-0 rounded-full bg-primary" />
                        ) : null}
                      </div>
                      <p className="mt-2 line-clamp-3 text-sm text-muted-foreground">
                        {notification.content || t('notifications.emptyContent')}
                      </p>
                      <div className="mt-3 flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                        <span>
                          {new Intl.DateTimeFormat(locale, {
                            dateStyle: 'medium',
                            timeStyle: 'short',
                          }).format(new Date(notification.createdAt))}
                        </span>
                        {notification.type ? (
                          <span className="rounded-full border border-border/60 bg-background/80 px-2.5 py-1 font-semibold text-foreground dark:border-white/10 dark:bg-background/45">
                            {notification.type}
                          </span>
                        ) : null}
                        {notification.link ? (
                          <span className="inline-flex items-center gap-1 text-primary">
                            {t('notifications.page.open')}
                            <ChevronRight className="h-3.5 w-3.5" />
                          </span>
                        ) : null}
                      </div>
                    </button>

                    <div className="flex items-center gap-2">
                      {!notification.read ? (
                        <Button
                          type="button"
                          variant="outline"
                          className="rounded-2xl"
                          onClick={() => void handleOpenNotification(notification)}
                        >
                          <CheckCheck className="mr-2 h-4 w-4" />
                          {t('notifications.page.markRead')}
                        </Button>
                      ) : null}

                      <Button
                        type="button"
                        variant="outline"
                        className="rounded-2xl"
                        onClick={() => void handleDeleteNotification(notification)}
                      >
                        <Trash2 className="mr-2 h-4 w-4" />
                        {t('notifications.page.delete')}
                      </Button>
                    </div>
                  </article>
                ))}

                {hasNext ? (
                  <div className="pt-3 text-center">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => void loadNotifications(page + 1, false)}
                      className="rounded-2xl"
                      disabled={isLoadingMore}
                    >
                      {isLoadingMore ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <RefreshCw className="mr-2 h-4 w-4" />
                      )}
                      {t('notifications.page.loadMore')}
                    </Button>
                  </div>
                ) : null}
              </div>
            )}
          </div>
        </section>
      </main>
    </div>
  )
}

function mergeNotificationPages(
  currentNotifications: NotificationResponse[],
  nextNotifications: NotificationResponse[],
) {
  const notificationMap = new Map<string, NotificationResponse>()

  for (const notification of currentNotifications) {
    notificationMap.set(notification.notificationId, notification)
  }

  for (const notification of nextNotifications) {
    notificationMap.set(notification.notificationId, notification)
  }

  return Array.from(notificationMap.values())
}

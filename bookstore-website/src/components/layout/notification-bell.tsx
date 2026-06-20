import { useEffect, useEffectEvent, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Bell, ChevronRight, Loader2, Trash2, Wifi, WifiOff } from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import { useNotifications } from '@/contexts/notification-context'
import { cn, getErrorMessage } from '@/utils'
import { toast } from 'sonner'

export function NotificationBell() {
  const navigate = useNavigate()
  const { language, locale } = useLanguage()
  const {
    notifications,
    unreadCount,
    isLoading,
    error,
    isRealtimeConnected,
    refresh,
    markAsRead,
    deleteNotification,
  } = useNotifications()
  const [open, setOpen] = useState(false)
  const panelRef = useRef<HTMLDivElement>(null)
  const handleRefreshOnOpen = useEffectEvent(() => {
    void refresh()
  })

  const labels = useMemo(
    () => ({
      title: language === 'vi' ? 'Thong bao' : 'Notifications',
      empty:
        language === 'vi'
          ? 'Chua co thong bao nao'
          : 'No notifications yet',
      viewAll: language === 'vi' ? 'Xem tat ca' : 'View all',
      delete: language === 'vi' ? 'Xoa' : 'Delete',
      loading: language === 'vi' ? 'Dang tai thong bao...' : 'Loading notifications...',
      openLabel:
        language === 'vi' ? 'Mo thong bao' : 'Open notifications',
      fallbackContent:
        language === 'vi' ? 'Khong co noi dung' : 'No content',
      realtimeLive:
        language === 'vi' ? 'Realtime dang bat' : 'Realtime connected',
      realtimeFallback:
        language === 'vi' ? 'Dang dung REST fallback' : 'Using REST fallback',
      deleteSuccess:
        language === 'vi' ? 'Da xoa thong bao' : 'Notification deleted',
    }),
    [language],
  )

  useEffect(() => {
    if (!open) {
      return
    }

    function handlePointerDown(event: MouseEvent) {
      if (
        panelRef.current &&
        !panelRef.current.contains(event.target as Node)
      ) {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    return () => document.removeEventListener('mousedown', handlePointerDown)
  }, [open])

  useEffect(() => {
    if (!open) {
      return
    }

    handleRefreshOnOpen()
  }, [open])

  async function handleOpenNotification(notificationId: string, link?: string | null) {
    try {
      await markAsRead(notificationId)
      setOpen(false)
      navigate(link && link.trim() !== '' ? link : '/notifications')
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    }
  }

  async function handleDelete(notificationId: string) {
    try {
      await deleteNotification(notificationId)
      toast.success(labels.deleteSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    }
  }

  const previewNotifications = notifications.slice(0, 5)

  return (
    <div className="relative" ref={panelRef}>
      <button
        type="button"
        onClick={() => setOpen((currentOpen) => !currentOpen)}
        className="relative flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted"
        aria-label={labels.openLabel}
      >
        <Bell className="size-5" />
        {unreadCount > 0 ? (
          <span className="absolute -right-0.5 -top-0.5 flex min-w-5 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-primary-foreground">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        ) : null}
      </button>

      {open ? (
        <div className="absolute right-0 z-[70] mt-3 w-[min(24rem,calc(100vw-2rem))] overflow-hidden rounded-[28px] border border-border/70 bg-background/95 shadow-[0_32px_90px_rgba(15,23,42,0.32)] backdrop-blur">
          <div className="flex items-center justify-between gap-3 border-b border-border/60 px-5 py-4">
            <div>
              <p className="text-base font-semibold text-foreground">
                {labels.title}
              </p>
              <div className="mt-1 flex items-center gap-2 text-xs text-muted-foreground">
                {isRealtimeConnected ? (
                  <Wifi className="h-3.5 w-3.5 text-emerald-600" />
                ) : (
                  <WifiOff className="h-3.5 w-3.5 text-amber-600" />
                )}
                <span>
                  {isRealtimeConnected
                    ? labels.realtimeLive
                    : labels.realtimeFallback}
                </span>
              </div>
            </div>
            {unreadCount > 0 ? (
              <span className="rounded-full bg-primary/10 px-2.5 py-1 text-xs font-semibold text-primary">
                {unreadCount}
              </span>
            ) : null}
          </div>

          <div className="max-h-[28rem] overflow-y-auto px-3 py-3">
            {isLoading ? (
              <div className="flex items-center justify-center gap-2 rounded-2xl px-4 py-8 text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 animate-spin" />
                {labels.loading}
              </div>
            ) : error ? (
              <div className="rounded-2xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {error}
              </div>
            ) : previewNotifications.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-border/70 px-4 py-8 text-center text-sm text-muted-foreground">
                {labels.empty}
              </div>
            ) : (
              <div className="space-y-2">
                {previewNotifications.map((notification) => (
                  <div
                    key={notification.notificationId}
                    className={cn(
                      'flex items-start gap-2 rounded-2xl border px-3 py-3 transition-colors',
                      notification.read
                        ? 'border-border/60 bg-card/50'
                        : 'border-primary/20 bg-primary/5',
                    )}
                  >
                    <button
                      type="button"
                      onClick={() =>
                        void handleOpenNotification(
                          notification.notificationId,
                          notification.link,
                        )
                      }
                      className="min-w-0 flex-1 text-left"
                    >
                      <div className="flex items-start justify-between gap-3">
                        <p className="line-clamp-1 text-sm font-semibold text-foreground">
                          {notification.title}
                        </p>
                        {!notification.read ? (
                          <span className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full bg-primary" />
                        ) : null}
                      </div>
                      <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                        {notification.content || labels.fallbackContent}
                      </p>
                      <div className="mt-2 flex items-center gap-2 text-xs text-muted-foreground">
                        <span>
                          {new Intl.DateTimeFormat(locale, {
                            dateStyle: 'medium',
                            timeStyle: 'short',
                          }).format(new Date(notification.createdAt))}
                        </span>
                        <ChevronRight className="h-3.5 w-3.5" />
                      </div>
                    </button>

                    <button
                      type="button"
                      onClick={() => void handleDelete(notification.notificationId)}
                      className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted hover:text-destructive"
                      aria-label={labels.delete}
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="border-t border-border/60 p-3">
            <button
              type="button"
              onClick={() => {
                setOpen(false)
                navigate('/notifications')
              }}
              className="flex w-full items-center justify-center gap-2 rounded-2xl px-4 py-3 text-sm font-semibold text-primary transition-colors hover:bg-primary/10"
            >
              {labels.viewAll}
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      ) : null}
    </div>
  )
}

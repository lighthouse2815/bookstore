import { useEffect, useMemo, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  BellRing,
  CalendarDays,
  Eye,
  MailPlus,
  MessageSquareMore,
  Plus,
  Search,
  Send,
  UserRound,
  X,
} from 'lucide-react'
import { toast } from 'sonner'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminNotification,
  getAdminCustomers,
  getAdminEmployees,
  getAdminNotifications,
} from '@/services/admin-access-service'
import type {
  AdminCreateNotificationRequest,
  AdminNotificationResponse,
  AdminUserResponse,
} from '@/types/admin-access'
import { getErrorMessage } from '@/utils'

type NotificationDialogMode = 'create' | 'view'

type UserLookup = {
  id: string
  name: string
  email: string
}

type NotificationFormState = {
  userId: string
  title: string
  content: string
}

const initialFormState: NotificationFormState = {
  userId: '',
  title: '',
  content: '',
}

export default function AdminNotificationsPage() {
  const { user } = useAuth()
  const { language, t, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const [notifications, setNotifications] = useState<AdminNotificationResponse[]>(
    [],
  )
  const [recipients, setRecipients] = useState<UserLookup[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] =
    useState<NotificationDialogMode | null>(null)
  const [selectedNotification, setSelectedNotification] =
    useState<AdminNotificationResponse | null>(null)
  const [form, setForm] = useState<NotificationFormState>(initialFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const labels = useMemo(
    () => ({
      title: isVietnamese ? 'Quan ly thong bao' : 'Notification management',
      description: isVietnamese
        ? 'Gui thong bao den tung tai khoan va theo doi trang thai da doc trong he thong.'
        : 'Send notifications to individual accounts and monitor read status in the system.',
      total: isVietnamese ? '{count} thong bao' : '{count} notifications',
      search: isVietnamese
        ? 'Tim theo tieu de, nguoi nhan, noi dung...'
        : 'Search by title, recipient, or content...',
      empty: isVietnamese ? 'Chua co thong bao nao' : 'No notifications found',
      loadError: isVietnamese
        ? 'Khong tai duoc danh sach thong bao'
        : 'Unable to load notifications',
      createError: isVietnamese
        ? 'Khong gui duoc thong bao'
        : 'Unable to send the notification',
      createSuccess: isVietnamese ? 'Da gui thong bao' : 'Notification sent',
      add: isVietnamese ? 'Gui thong bao' : 'Send notification',
      detailTitle: isVietnamese ? 'Chi tiet thong bao' : 'Notification details',
      recipient: isVietnamese ? 'Nguoi nhan' : 'Recipient',
      subject: isVietnamese ? 'Tieu de' : 'Title',
      content: isVietnamese ? 'Noi dung' : 'Content',
      chooseRecipient: isVietnamese ? 'Chon nguoi nhan' : 'Choose recipient',
      unread: isVietnamese ? 'Chua doc' : 'Unread',
      read: isVietnamese ? 'Da doc' : 'Read',
      readAt: isVietnamese ? 'Doc luc' : 'Read at',
      createdAt: isVietnamese ? 'Gui luc' : 'Sent at',
      noReadAt: isVietnamese ? 'Chua doc' : 'Not read yet',
      noContent: isVietnamese ? 'Khong co noi dung' : 'No content',
      recipientCount: isVietnamese ? 'Tai khoan co the gui' : 'Available recipients',
      unknownUser: isVietnamese ? 'Nguoi nhan khong xac dinh' : 'Unknown recipient',
    }),
    [isVietnamese],
  )

  const recipientLookup = useMemo(
    () =>
      recipients.reduce<Record<string, UserLookup>>((lookup, recipient) => {
        lookup[recipient.id] = recipient
        return lookup
      }, {}),
    [recipients],
  )

  const sortedNotifications = useMemo(
    () =>
      [...notifications].sort(
        (leftNotification, rightNotification) =>
          new Date(rightNotification.createdAt).getTime() -
          new Date(leftNotification.createdAt).getTime(),
      ),
    [notifications],
  )

  const filteredNotifications = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return sortedNotifications
    }

    return sortedNotifications.filter((notification) => {
      const recipient = recipientLookup[notification.userId]

      return [
        notification.title,
        notification.content,
        recipient?.name ?? '',
        recipient?.email ?? '',
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword)
    })
  }, [recipientLookup, searchTerm, sortedNotifications])

  const unreadCount = notifications.filter((notification) => !notification.read).length
  const readCount = notifications.length - unreadCount

  useEffect(() => {
    void loadData()
  }, [])

  useEffect(() => {
    if (!dialogMode) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !isSubmitting) {
        closeDialog()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [dialogMode, isSubmitting])

  async function loadData() {
    setIsLoading(true)

    try {
      const [notificationResponse, customers, employees] = await Promise.all([
        getAdminNotifications(),
        getAdminCustomers(),
        getAdminEmployees(),
      ])

      setNotifications(notificationResponse)
      setRecipients(buildRecipients(customers, employees, user?.id, user?.name, user?.email))
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, labels.loadError))
    } finally {
      setIsLoading(false)
    }
  }

  function closeDialog() {
    if (isSubmitting) {
      return
    }

    setDialogMode(null)
    setSelectedNotification(null)
    setForm(initialFormState)
  }

  function openCreateDialog() {
    setSelectedNotification(null)
    setForm({
      userId: recipients[0]?.id ?? '',
      title: '',
      content: '',
    })
    setDialogMode('create')
  }

  function openViewDialog(notification: AdminNotificationResponse) {
    setSelectedNotification(notification)
    setDialogMode('view')
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)

    const payload: AdminCreateNotificationRequest = {
      userId: form.userId,
      title: form.title.trim(),
      content: form.content.trim(),
    }

    try {
      const response = await createAdminNotification(payload)
      setNotifications((currentNotifications) => [response, ...currentNotifications])
      toast.success(labels.createSuccess)
      closeDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.createError))
    } finally {
      setIsSubmitting(false)
    }
  }

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={closeDialog}
        disabled={isSubmitting}
      />
      <div className="relative z-10 w-full max-w-3xl">
        {dialogMode === 'create' ? (
          <DialogShell
            title={labels.add}
            onClose={closeDialog}
            canClose={!isSubmitting}
          >
            <form className="space-y-5" onSubmit={(event) => void handleSubmit(event)}>
              <div className="space-y-2">
                <Label>{labels.recipient}</Label>
                <select
                  value={form.userId}
                  onChange={(event) =>
                    setForm((currentForm) => ({
                      ...currentForm,
                      userId: event.currentTarget.value,
                    }))
                  }
                  className="h-11 w-full rounded-2xl border border-input bg-background px-3 text-sm"
                  required
                >
                  <option value="" disabled>
                    {labels.chooseRecipient}
                  </option>
                  {recipients.map((recipient) => (
                    <option key={recipient.id} value={recipient.id}>
                      {recipient.name} - {recipient.email}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-2">
                <Label>{labels.subject}</Label>
                <Input
                  value={form.title}
                  onChange={(event) =>
                    setForm((currentForm) => ({
                      ...currentForm,
                      title: event.currentTarget.value,
                    }))
                  }
                  className="h-11 rounded-2xl"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label>{labels.content}</Label>
                <Textarea
                  value={form.content}
                  onChange={(event) =>
                    setForm((currentForm) => ({
                      ...currentForm,
                      content: event.currentTarget.value,
                    }))
                  }
                  className="min-h-36 rounded-2xl"
                  required
                />
              </div>

              <div className="flex justify-end gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={closeDialog}
                  className="rounded-2xl"
                  disabled={isSubmitting}
                >
                  {t('common.cancel')}
                </Button>
                <Button
                  type="submit"
                  className="rounded-2xl"
                  disabled={isSubmitting || !form.userId}
                >
                  <Send className="mr-2 h-4 w-4" />
                  {isSubmitting ? t('common.processing') : labels.add}
                </Button>
              </div>
            </form>
          </DialogShell>
        ) : null}

        {dialogMode === 'view' && selectedNotification ? (
          <DialogShell title={labels.detailTitle} onClose={closeDialog}>
            <NotificationDetail
              formatDate={formatDate}
              labels={labels}
              notification={selectedNotification}
              recipient={recipientLookup[selectedNotification.userId]}
            />
          </DialogShell>
        ) : null}
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(59,130,246,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.14),transparent_32%)]" />

          <div className="relative">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                    {labels.title}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <BellRing className="mr-2 h-4 w-4" />
                    {interpolateLabel(labels.total, {
                      count: formatNumber(notifications.length),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {labels.description}
                </p>
              </div>

              <Button
                size="lg"
                onClick={openCreateDialog}
                className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
              >
                <Plus className="mr-2 h-5 w-5" />
                {labels.add}
              </Button>
            </div>

            <div className="mt-6 grid gap-3 sm:grid-cols-3">
              <MetricCard label={labels.unread} value={formatNumber(unreadCount)} />
              <MetricCard label={labels.read} value={formatNumber(readCount)} />
              <MetricCard
                label={labels.recipientCount}
                value={formatNumber(recipients.length)}
              />
            </div>

            <div className="mt-8 max-w-xl">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={searchTerm}
                  onChange={(event) => setSearchTerm(event.currentTarget.value)}
                  placeholder={labels.search}
                  className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base"
                />
              </div>
            </div>

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.2fr_10rem_12rem]">
                  <div className="px-8 py-6">{labels.subject}</div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.recipient}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {t('orders.status')}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {t('common.actions')}
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredNotifications.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center text-muted-foreground">
                    {labels.empty}
                  </div>
                ) : (
                  filteredNotifications.map((notification) => {
                    const recipient = recipientLookup[notification.userId]

                    return (
                      <article
                        key={notification.notificationId}
                        className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.2fr_10rem_12rem] xl:gap-0 xl:p-0"
                      >
                        <div className="min-w-0 xl:px-8 xl:py-6">
                          <p className="truncate text-lg font-semibold text-foreground">
                            {notification.title}
                          </p>
                          <p className="mt-2 truncate text-sm text-muted-foreground">
                            {notification.content || labels.noContent}
                          </p>
                          <p className="mt-3 flex items-center gap-2 text-xs text-muted-foreground">
                            <CalendarDays className="h-3.5 w-3.5" />
                            {formatDate(notification.createdAt)}
                          </p>
                        </div>

                        <div className="flex items-center justify-start border-border/40 text-sm font-medium text-foreground xl:justify-center xl:border-l">
                          <div className="min-w-0 text-left xl:text-center">
                            <p className="truncate">
                              {recipient?.name ?? labels.unknownUser}
                            </p>
                            <p className="mt-1 truncate text-xs text-muted-foreground">
                              {recipient?.email ?? notification.userId}
                            </p>
                          </div>
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          <ReadStatusBadge
                            isRead={notification.read}
                            readLabel={labels.read}
                            unreadLabel={labels.unread}
                          />
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => openViewDialog(notification)}
                            className="rounded-2xl"
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            {t('common.view')}
                          </Button>
                        </div>
                      </article>
                    )
                  })
                )}
              </div>
            </section>
          </div>
        </div>
      </AdminLayout>

      {dialogMarkup && typeof document !== 'undefined'
        ? createPortal(dialogMarkup, document.body)
        : null}
    </>
  )
}

function NotificationDetail({
  formatDate,
  labels,
  notification,
  recipient,
}: {
  formatDate: (value: string | number | Date) => string
  labels: {
    content: string
    createdAt: string
    noContent: string
    noReadAt: string
    read: string
    readAt: string
    recipient: string
    subject: string
    unread: string
    unknownUser: string
  }
  notification: AdminNotificationResponse
  recipient: UserLookup | undefined
}) {
  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2">
        <DetailCard
          icon={UserRound}
          label={labels.recipient}
          value={recipient?.name ?? labels.unknownUser}
          secondary={recipient?.email ?? notification.userId}
        />
        <DetailCard
          icon={BellRing}
          label={labels.subject}
          value={notification.title}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <DetailCard
          icon={CalendarDays}
          label={labels.createdAt}
          value={formatDate(notification.createdAt)}
        />
        <DetailCard
          icon={MailPlus}
          label={notification.read ? labels.readAt : labels.read}
          value={
            notification.readAt
              ? formatDate(notification.readAt)
              : notification.read
                ? labels.read
                : labels.noReadAt
          }
          secondary={notification.read ? labels.read : labels.unread}
        />
      </div>

      <div className="rounded-[22px] border border-border/60 bg-background/55 p-5">
        <p className="text-sm text-muted-foreground">{labels.content}</p>
        <p className="mt-3 whitespace-pre-wrap text-base font-medium text-foreground">
          {notification.content || labels.noContent}
        </p>
      </div>
    </div>
  )
}

function DialogShell({
  canClose = true,
  children,
  onClose,
  title,
}: {
  canClose?: boolean
  children: React.ReactNode
  onClose: () => void
  title: string
}) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          onClick={onClose}
          className="rounded-2xl"
          disabled={!canClose}
        >
          <X className="h-4 w-4" />
        </Button>
      </div>
      <div className="max-h-[78vh] overflow-y-auto px-6 py-6">{children}</div>
    </div>
  )
}

function MetricCard({
  label,
  value,
}: {
  label: string
  value: string
}) {
  return (
    <div className="rounded-2xl border border-border/60 bg-background/55 px-5 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 text-2xl font-bold text-foreground">{value}</p>
    </div>
  )
}

function DetailCard({
  icon: Icon,
  label,
  value,
  secondary,
}: {
  icon: typeof BellRing
  label: string
  value: string
  secondary?: string
}) {
  return (
    <div className="rounded-[22px] border border-border/60 bg-background/55 p-4">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Icon className="h-4 w-4" />
        <span>{label}</span>
      </div>
      <p className="mt-3 text-base font-semibold text-foreground">{value}</p>
      {secondary ? (
        <p className="mt-2 break-all text-xs text-muted-foreground">{secondary}</p>
      ) : null}
    </div>
  )
}

function ReadStatusBadge({
  isRead,
  readLabel,
  unreadLabel,
}: {
  isRead: boolean
  readLabel: string
  unreadLabel: string
}) {
  return (
    <Badge
      variant="outline"
      className={`rounded-2xl px-3 py-1.5 ${
        isRead ? 'text-emerald-500' : 'text-amber-500'
      }`}
    >
      {isRead ? readLabel : unreadLabel}
    </Badge>
  )
}

function buildRecipients(
  customers: AdminUserResponse[],
  employees: AdminUserResponse[],
  currentUserId?: string,
  currentUserName?: string,
  currentUserEmail?: string,
) {
  const recipientMap = new Map<string, UserLookup>()

  for (const account of [...customers, ...employees]) {
    recipientMap.set(account.userId, {
      id: account.userId,
      name: account.username,
      email: account.email,
    })
  }

  if (currentUserId && currentUserName && currentUserEmail) {
    recipientMap.set(currentUserId, {
      id: currentUserId,
      name: currentUserName,
      email: currentUserEmail,
    })
  }

  return Array.from(recipientMap.values()).sort((leftRecipient, rightRecipient) =>
    leftRecipient.name.localeCompare(rightRecipient.name),
  )
}

function interpolateLabel(
  template: string,
  params: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    String(params[key] ?? `{${key}}`),
  )
}

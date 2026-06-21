import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  getAdminCustomers,
  getAdminEmployees,
} from '@/services/admin-access-service'
import {
  broadcastNotification,
  createAdminNotification,
  deleteAdminNotification,
  getAdminNotifications,
} from '@/services/notification-service'
import type { AdminUserResponse } from '@/types/admin-access'
import type {
  BroadcastNotificationRequest,
  CreateNotificationRequest,
  NotificationResponse,
} from '@/types/notification'
import { getErrorMessage } from '@/utils'

type NotificationDialogMode = 'broadcast' | 'create' | 'view'

type UserLookup = {
  id: string
  name: string
  email: string
}

type NotificationFormState = {
  userId: string
  title: string
  content: string
  type: string
  link: string
}

const ADMIN_PAGE_SIZE = 20

const initialFormState: NotificationFormState = {
  userId: '',
  title: '',
  content: '',
  type: '',
  link: '',
}

export function useAdminNotificationsPage() {
  const { user } = useAuth()
  const { language, t, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const [notifications, setNotifications] = useState<NotificationResponse[]>([])
  const [recipients, setRecipients] = useState<UserLookup[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isLoadingMore, setIsLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] =
    useState<NotificationDialogMode | null>(null)
  const [selectedNotification, setSelectedNotification] =
    useState<NotificationResponse | null>(null)
  const [form, setForm] = useState<NotificationFormState>(initialFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const labels = useMemo(
    () => ({
      title: isVietnamese ? 'Quan ly thong bao' : 'Notification management',
      description: isVietnamese
        ? 'Gui thong bao cho tung tai khoan hoac broadcast den toan bo nguoi dung, van giu REST lam nguon du lieu chinh.'
        : 'Send notifications to a single account or broadcast them to all users while keeping REST as the source of truth.',
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
      broadcastSuccess: isVietnamese
        ? 'Da broadcast thong bao cho {count} tai khoan'
        : 'Broadcast notification sent to {count} accounts',
      deleteError: isVietnamese
        ? 'Khong xoa duoc thong bao'
        : 'Unable to delete the notification',
      deleteSuccess: isVietnamese ? 'Da xoa thong bao' : 'Notification deleted',
      add: isVietnamese ? 'Gui cho 1 user' : 'Send to one user',
      broadcast: isVietnamese ? 'Broadcast tat ca' : 'Broadcast all',
      detailTitle: isVietnamese ? 'Chi tiet thong bao' : 'Notification details',
      previewTitle: isVietnamese ? 'Xem truoc' : 'Preview',
      recipient: isVietnamese ? 'Nguoi nhan' : 'Recipient',
      allRecipients: isVietnamese ? 'Tat ca nguoi dung' : 'All users',
      subject: isVietnamese ? 'Tieu de' : 'Title',
      content: isVietnamese ? 'Noi dung' : 'Content',
      type: isVietnamese ? 'Loai' : 'Type',
      link: isVietnamese ? 'Lien ket' : 'Link',
      chooseRecipient: isVietnamese ? 'Chon nguoi nhan' : 'Choose recipient',
      unread: isVietnamese ? 'Chua doc' : 'Unread',
      read: isVietnamese ? 'Da doc' : 'Read',
      readAt: isVietnamese ? 'Doc luc' : 'Read at',
      createdAt: isVietnamese ? 'Gui luc' : 'Sent at',
      noReadAt: isVietnamese ? 'Chua doc' : 'Not read yet',
      noContent: isVietnamese ? 'Khong co noi dung' : 'No content',
      noType: isVietnamese ? 'Mac dinh' : 'Default',
      noLink: isVietnamese ? 'Khong co' : 'None',
      optional: isVietnamese ? 'Tuy chon' : 'Optional',
      loadMore: isVietnamese ? 'Tai them' : 'Load more',
      delete: isVietnamese ? 'Xoa' : 'Delete',
      recipientCount: isVietnamese ? 'Tai khoan co the gui' : 'Available recipients',
      unknownUser: isVietnamese ? 'Nguoi nhan khong xac dinh' : 'Unknown recipient',
      status: isVietnamese ? 'Trang thai' : 'Status',
      actions: isVietnamese ? 'Thao tac' : 'Actions',
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
    () => sortNotificationsByCreatedAtDesc(notifications),
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
        notification.type ?? '',
        notification.link ?? '',
        recipient?.name ?? '',
        recipient?.email ?? '',
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword)
    })
  }, [recipientLookup, searchTerm, sortedNotifications])

  const unreadCount = notifications.filter((notification) => !notification.read)
    .length
  const readCount = notifications.length - unreadCount

  useEffect(() => {
    let isCancelled = false

    async function loadData() {
      setIsLoading(true)

      try {
        const [notificationPage, customers, employees] = await Promise.all([
          getAdminNotifications({ page: 0, size: ADMIN_PAGE_SIZE }),
          getAdminCustomers(),
          getAdminEmployees(),
        ])

        if (isCancelled) {
          return
        }

        setNotifications(notificationPage.items)
        setPage(notificationPage.page)
        setTotalCount(notificationPage.totalCount)
        setHasNext(notificationPage.hasNext)
        setRecipients(
          buildRecipients(customers, employees, user?.id, user?.name, user?.email),
        )
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, labels.loadError))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadData()

    return () => {
      isCancelled = true
    }
  }, [labels.loadError, user?.email, user?.id, user?.name])

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

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function handleFormChange(
    field: keyof NotificationFormState,
    value: string,
  ) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
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
      ...initialFormState,
      userId: recipients[0]?.id ?? '',
    })
    setDialogMode('create')
  }

  function openBroadcastDialog() {
    setSelectedNotification(null)
    setForm(initialFormState)
    setDialogMode('broadcast')
  }

  function openViewDialog(notification: NotificationResponse) {
    setSelectedNotification(notification)
    setDialogMode('view')
  }

  async function handleLoadMore() {
    if (isLoadingMore || !hasNext) {
      return
    }

    setIsLoadingMore(true)

    try {
      const nextPage = await getAdminNotifications({
        page: page + 1,
        size: ADMIN_PAGE_SIZE,
      })

      setNotifications((currentNotifications) =>
        mergeNotificationPages(currentNotifications, nextPage.items),
      )
      setPage(nextPage.page)
      setTotalCount(nextPage.totalCount)
      setHasNext(nextPage.hasNext)
      setError(null)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.loadError))
    } finally {
      setIsLoadingMore(false)
    }
  }

  async function reloadFirstPage() {
    const nextPage = await getAdminNotifications({
      page: 0,
      size: ADMIN_PAGE_SIZE,
    })

    setNotifications(nextPage.items)
    setPage(nextPage.page)
    setTotalCount(nextPage.totalCount)
    setHasNext(nextPage.hasNext)
    setError(null)
  }

  async function handleDelete(notificationId: string) {
    try {
      await deleteAdminNotification(notificationId)
      setNotifications((currentNotifications) =>
        currentNotifications.filter(
          (notification) => notification.notificationId !== notificationId,
        ),
      )
      setTotalCount((currentTotalCount) => Math.max(0, currentTotalCount - 1))

      if (selectedNotification?.notificationId === notificationId) {
        closeDialog()
      }

      toast.success(labels.deleteSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.deleteError))
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)

    const basePayload = {
      title: form.title.trim(),
      content: form.content.trim(),
      type: toNullableString(form.type),
      link: toNullableString(form.link),
    }

    try {
      if (dialogMode === 'broadcast') {
        const payload: BroadcastNotificationRequest = basePayload
        const response = await broadcastNotification(payload)
        await reloadFirstPage()
        toast.success(
          interpolateLabel(labels.broadcastSuccess, {
            count: formatNumber(response.createdCount),
          }),
        )
      } else {
        const payload: CreateNotificationRequest = {
          ...basePayload,
          userId: form.userId,
        }
        await createAdminNotification(payload)
        await reloadFirstPage()
        toast.success(labels.createSuccess)
      }

      closeDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.createError))
    } finally {
      setIsSubmitting(false)
    }
  }

  return {
    t,
    formatDate,
    formatNumber,
    labels,
    notifications,
    recipients,
    searchTerm,
    isLoading,
    isLoadingMore,
    error,
    dialogMode,
    selectedNotification,
    form,
    totalCount,
    hasNext,
    isSubmitting,
    recipientLookup,
    filteredNotifications,
    unreadCount,
    readCount,
    handleSearchTermChange,
    handleFormChange,
    closeDialog,
    openCreateDialog,
    openBroadcastDialog,
    openViewDialog,
    handleLoadMore,
    handleDelete,
    handleSubmit,
  }
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

function sortNotificationsByCreatedAtDesc(
  notifications: NotificationResponse[],
) {
  return [...notifications].sort(
    (leftNotification, rightNotification) =>
      new Date(rightNotification.createdAt).getTime() -
      new Date(leftNotification.createdAt).getTime(),
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

function toNullableString(value: string) {
  const trimmedValue = value.trim()
  return trimmedValue === '' ? null : trimmedValue
}

function interpolateLabel(
  template: string,
  params: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    String(params[key] ?? `{${key}}`),
  )
}

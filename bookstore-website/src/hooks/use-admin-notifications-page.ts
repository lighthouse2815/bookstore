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
  const { t, formatDate, formatNumber } = useLanguage()
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
      title: t('admin.notificationsPage.title'),
      description: t('admin.notificationsPage.description'),
      total: t('admin.notificationsPage.total'),
      search: t('admin.notificationsPage.search'),
      empty: t('admin.notificationsPage.empty'),
      loadError: t('admin.notificationsPage.loadError'),
      createError: t('admin.notificationsPage.createError'),
      createSuccess: t('admin.notificationsPage.createSuccess'),
      broadcastSuccess: t('admin.notificationsPage.broadcastSuccess'),
      deleteError: t('admin.notificationsPage.deleteError'),
      deleteSuccess: t('admin.notificationsPage.deleteSuccess'),
      add: t('admin.notificationsPage.add'),
      broadcast: t('admin.notificationsPage.broadcast'),
      detailTitle: t('admin.notificationsPage.detailTitle'),
      previewTitle: t('admin.notificationsPage.previewTitle'),
      recipient: t('admin.notificationsPage.recipient'),
      allRecipients: t('admin.notificationsPage.allRecipients'),
      subject: t('admin.notificationsPage.subject'),
      content: t('admin.notificationsPage.content'),
      type: t('admin.notificationsPage.type'),
      link: t('admin.notificationsPage.link'),
      chooseRecipient: t('admin.notificationsPage.chooseRecipient'),
      unread: t('admin.notificationsPage.unread'),
      read: t('admin.notificationsPage.read'),
      readAt: t('admin.notificationsPage.readAt'),
      createdAt: t('admin.notificationsPage.createdAt'),
      noReadAt: t('admin.notificationsPage.noReadAt'),
      noContent: t('admin.notificationsPage.noContent'),
      noType: t('admin.notificationsPage.noType'),
      noLink: t('admin.notificationsPage.noLink'),
      optional: t('admin.notificationsPage.optional'),
      loadMore: t('admin.notificationsPage.loadMore'),
      delete: t('admin.notificationsPage.delete'),
      recipientCount: t('admin.notificationsPage.recipientCount'),
      unknownUser: t('admin.notificationsPage.unknownUser'),
      status: t('admin.notificationsPage.status'),
      actions: t('admin.notificationsPage.actions'),
    }),
    [t],
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
          t('admin.notificationsPage.broadcastSuccess', {
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

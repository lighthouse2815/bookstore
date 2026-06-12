import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
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

export function useAdminNotificationsPage() {
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
        const [notificationResponse, customers, employees] = await Promise.all([
          getAdminNotifications(),
          getAdminCustomers(),
          getAdminEmployees(),
        ])

        if (isCancelled) {
          return
        }

        setNotifications(notificationResponse)
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

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)

    const payload: AdminCreateNotificationRequest = {
      userId: form.userId,
      title: form.title.trim(),
      content: form.content.trim(),
    }

    try {
      const response = await createAdminNotification(payload)
      setNotifications((currentNotifications) => [
        response,
        ...currentNotifications,
      ])
      toast.success(labels.createSuccess)
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
    error,
    dialogMode,
    selectedNotification,
    form,
    isSubmitting,
    recipientLookup,
    filteredNotifications,
    unreadCount,
    readCount,
    handleSearchTermChange,
    handleFormChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
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
  notifications: AdminNotificationResponse[],
) {
  return [...notifications].sort(
    (leftNotification, rightNotification) =>
      new Date(rightNotification.createdAt).getTime() -
      new Date(leftNotification.createdAt).getTime(),
  )
}

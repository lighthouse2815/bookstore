import {
  useDeferredValue,
  useEffect,
  useEffectEvent,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
} from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { connectChatRealtime, disconnectChatRealtime } from '@/services/chat-realtime-service'
import {
  assignConversation as assignConversationRequest,
  closeAdminConversation as closeAdminConversationRequest,
  getAdminConversations,
  getAdminMessages,
  markAdminConversationRead,
  reopenAdminConversation as reopenAdminConversationRequest,
  sendAdminMessage as sendAdminMessageRequest,
} from '@/services/chat-service'
import { getAdminEmployees } from '@/services/admin-access-service'
import type { AdminUserResponse } from '@/types/admin-access'
import type {
  ChatMessageResponse,
  ConversationResponse,
  ConversationStatus,
} from '@/types/chat'
import { getErrorMessage } from '@/utils'

export type AdminConversationFilter = 'ALL' | ConversationStatus

export type AdminMessagePageState = {
  page: number
  size: number
  totalCount: number
  hasNext: boolean
  isLoading: boolean
  isLoadingMore: boolean
  initialized: boolean
}

export type AdminChatLabels = {
  title: string
  description: string
  totalConversations: string
  unreadCount: string
  openCount: string
  connected: string
  fallback: string
  listTitle: string
  searchPlaceholder: string
  statusLabel: string
  statusAll: string
  statusOpen: string
  statusPending: string
  statusClosed: string
  emptyConversations: string
  loadMore: string
  loadingList: string
  loadingMessages: string
  messageEmpty: string
  replyPlaceholder: string
  send: string
  closeConversation: string
  reopenConversation: string
  customer: string
  assignee: string
  unassigned: string
  assignToSelf: string
  assignButton: string
  staffPlaceholder: string
  priority: string
  target: string
  createdAt: string
  updatedAt: string
  noConversationSelected: string
  closedNotice: string
  noMessagesYet: string
}

const ACCESS_TOKEN_KEY = 'accessToken'
const ADMIN_PAGE_SIZE = 20
const MESSAGE_PAGE_SIZE = 30

const defaultMessagePageState: AdminMessagePageState = {
  page: 0,
  size: MESSAGE_PAGE_SIZE,
  totalCount: 0,
  hasNext: false,
  isLoading: false,
  isLoadingMore: false,
  initialized: false,
}

export function useAdminChatPage() {
  const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth()
  const { language, locale, formatNumber } = useLanguage()
  const [conversations, setConversations] = useState<ConversationResponse[]>([])
  const [activeConversationId, setActiveConversationId] = useState<string | null>(
    null,
  )
  const [messagesByConversation, setMessagesByConversation] = useState<
    Record<string, ChatMessageResponse[]>
  >({})
  const [messagePages, setMessagePages] = useState<
    Record<string, AdminMessagePageState>
  >({})
  const [employeeOptions, setEmployeeOptions] = useState<AdminUserResponse[]>([])
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] =
    useState<AdminConversationFilter>('ALL')
  const [page, setPage] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  const [totalCount, setTotalCount] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [isLoadingMore, setIsLoadingMore] = useState(false)
  const [isSending, setIsSending] = useState(false)
  const [isAssigning, setIsAssigning] = useState(false)
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false)
  const [isRealtimeConnected, setIsRealtimeConnected] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [pendingAssigneeId, setPendingAssigneeId] = useState('')
  const activeConversationIdRef = useRef<string | null>(null)
  const refreshTimeoutRef = useRef<number | null>(null)
  const deferredKeyword = useDeferredValue(keyword.trim())

  const canUseAdminChat = Boolean(
    isAuthenticated &&
      user &&
      (user.roles.includes('ADMIN') || user.roles.includes('STAFF')),
  )
  const canAssignEmployees = Boolean(user?.roles.includes('ADMIN'))

  const labels = useMemo<AdminChatLabels>(
    () => ({
      title: language === 'vi' ? 'Chat ho tro khach hang' : 'Customer support chat',
      description: language === 'vi'
        ? 'Theo doi hoi thoai ho tro theo thoi gian thuc, phan cong nhan su va dong mo lai cuoc tro chuyen tu mot man hinh.'
        : 'Handle support conversations in realtime, assign staff, and close or reopen threads from one workspace.',
      totalConversations: language === 'vi' ? 'Tong hoi thoai' : 'Total conversations',
      unreadCount: language === 'vi' ? 'Chua doc' : 'Unread',
      openCount: language === 'vi' ? 'Dang mo' : 'Open',
      connected: language === 'vi' ? 'Realtime dang bat' : 'Realtime connected',
      fallback: language === 'vi' ? 'Dang dung REST fallback' : 'Using REST fallback',
      listTitle: language === 'vi' ? 'Danh sach hoi thoai' : 'Conversation list',
      searchPlaceholder: language === 'vi'
        ? 'Tim theo khach hang, email, noi dung...'
        : 'Search by customer, email, or message...',
      statusLabel: language === 'vi' ? 'Trang thai' : 'Status',
      statusAll: language === 'vi' ? 'Tat ca' : 'All',
      statusOpen: language === 'vi' ? 'Dang mo' : 'Open',
      statusPending: language === 'vi' ? 'Dang cho' : 'Pending',
      statusClosed: language === 'vi' ? 'Da dong' : 'Closed',
      emptyConversations: language === 'vi'
        ? 'Khong co hoi thoai phu hop bo loc hien tai.'
        : 'No conversations match the current filters.',
      loadMore: language === 'vi' ? 'Tai them' : 'Load more',
      loadingList: language === 'vi' ? 'Dang tai hoi thoai...' : 'Loading conversations...',
      loadingMessages: language === 'vi' ? 'Dang tai tin nhan...' : 'Loading messages...',
      messageEmpty: language === 'vi'
        ? 'Chua co tin nhan trong hoi thoai nay.'
        : 'No messages in this conversation yet.',
      replyPlaceholder: language === 'vi'
        ? 'Nhap noi dung phan hoi cho khach hang...'
        : 'Write a reply to the customer...',
      send: language === 'vi' ? 'Gui phan hoi' : 'Send reply',
      closeConversation: language === 'vi' ? 'Dong hoi thoai' : 'Close conversation',
      reopenConversation: language === 'vi' ? 'Mo lai hoi thoai' : 'Reopen conversation',
      customer: language === 'vi' ? 'Khach hang' : 'Customer',
      assignee: language === 'vi' ? 'Phu trach' : 'Assignee',
      unassigned: language === 'vi' ? 'Chua phan cong' : 'Unassigned',
      assignToSelf: language === 'vi' ? 'Nhan xu ly' : 'Assign to me',
      assignButton: language === 'vi' ? 'Phan cong' : 'Assign',
      staffPlaceholder: language === 'vi' ? 'Chon nhan vien' : 'Choose staff',
      priority: language === 'vi' ? 'Do uu tien' : 'Priority',
      target: language === 'vi' ? 'Nguon lien quan' : 'Target',
      createdAt: language === 'vi' ? 'Tao luc' : 'Created at',
      updatedAt: language === 'vi' ? 'Cap nhat luc' : 'Updated at',
      noConversationSelected: language === 'vi'
        ? 'Chon mot hoi thoai de bat dau xu ly.'
        : 'Select a conversation to start handling it.',
      closedNotice: language === 'vi'
        ? 'Hoi thoai da dong. Mo lai neu can tiep tuc ho tro.'
        : 'This conversation is closed. Reopen it if support should continue.',
      noMessagesYet: language === 'vi'
        ? 'Khach hang chua gui tin nhan.'
        : 'The customer has not sent any messages yet.',
    }),
    [language],
  )

  const activeConversation = useMemo(
    () =>
      conversations.find(
        (conversation) => conversation.conversationId === activeConversationId,
      ) ?? null,
    [activeConversationId, conversations],
  )

  const openConversationCount = useMemo(
    () => conversations.filter((conversation) => conversation.status !== 'CLOSED').length,
    [conversations],
  )

  const unreadConversationCount = useMemo(
    () =>
      conversations.reduce(
        (totalUnread, conversation) => totalUnread + conversation.myUnreadCount,
        0,
      ),
    [conversations],
  )

  useEffect(() => {
    activeConversationIdRef.current = activeConversationId
  }, [activeConversationId])

  useEffect(() => {
    setPendingAssigneeId(activeConversation?.assignedStaffId ?? '')
  }, [activeConversation?.assignedStaffId, activeConversation?.conversationId])

  useEffect(() => {
    return () => {
      if (refreshTimeoutRef.current !== null) {
        window.clearTimeout(refreshTimeoutRef.current)
      }
    }
  }, [])

  const scheduleRefresh = useEffectEvent(() => {
    if (refreshTimeoutRef.current !== null) {
      window.clearTimeout(refreshTimeoutRef.current)
    }

    refreshTimeoutRef.current = window.setTimeout(() => {
      void refreshConversations()
      refreshTimeoutRef.current = null
    }, 300)
  })

  const handleRealtimeMessage = useEffectEvent((message: ChatMessageResponse) => {
    setMessagesByConversation((currentMessagesByConversation) => ({
      ...currentMessagesByConversation,
      [message.conversationId]: mergeMessages(
        currentMessagesByConversation[message.conversationId] ?? [],
        [message],
      ),
    }))

    setConversations((currentConversations) =>
      currentConversations.map((conversation) =>
        conversation.conversationId === message.conversationId
          ? {
              ...conversation,
              lastMessageId: message.messageId,
              lastMessagePreview: message.content,
              lastMessageAt: message.createdAt,
              updatedAt: message.updatedAt,
            }
          : conversation,
      ),
    )

    if (
      user &&
      activeConversationIdRef.current === message.conversationId &&
      message.senderId !== user.id
    ) {
      void markConversationAsRead(message.conversationId)
    }
  })

  const handleRealtimeConversation = useEffectEvent(
    (conversation: ConversationResponse) => {
      setConversations((currentConversations) =>
        mergeConversations(currentConversations, [conversation]),
      )
    },
  )

  const handleRealtimeAdminConversation = useEffectEvent(
    (conversation: ConversationResponse) => {
      setConversations((currentConversations) =>
        mergeConversations(currentConversations, [conversation]),
      )
      scheduleRefresh()
    },
  )

  useEffect(() => {
    let isCancelled = false
    disconnectChatRealtime()

    if (isAuthLoading) {
      return
    }

    if (!canUseAdminChat) {
      clearState()
      return
    }

    const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY)
    if (!accessToken) {
      setIsRealtimeConnected(false)
      return
    }

    connectChatRealtime(accessToken, {
      isAdmin: true,
      onMessage: handleRealtimeMessage,
      onConversation: handleRealtimeConversation,
      onAdminConversation: handleRealtimeAdminConversation,
      onConnect: () => {
        if (!isCancelled) {
          setIsRealtimeConnected(true)
        }
      },
      onError: () => {
        if (!isCancelled) {
          setIsRealtimeConnected(false)
        }
      },
    })

    return () => {
      isCancelled = true
      disconnectChatRealtime()
      setIsRealtimeConnected(false)
    }
  }, [
    canUseAdminChat,
    handleRealtimeAdminConversation,
    handleRealtimeConversation,
    handleRealtimeMessage,
    isAuthLoading,
  ])

  useEffect(() => {
    if (!canUseAdminChat) {
      return
    }

    void loadConversationPage(0, false)
  }, [canUseAdminChat, deferredKeyword, statusFilter])

  useEffect(() => {
    if (!canAssignEmployees) {
      setEmployeeOptions([])
      return
    }

    let isCancelled = false

    async function loadEmployees() {
      try {
        const employees = await getAdminEmployees()
        if (!isCancelled) {
          setEmployeeOptions(employees)
        }
      } catch (currentError) {
        if (!isCancelled) {
          toast.error(getErrorMessage(currentError))
        }
      }
    }

    void loadEmployees()

    return () => {
      isCancelled = true
    }
  }, [canAssignEmployees])

  useEffect(() => {
    if (!activeConversationId) {
      return
    }

    const pageState = messagePages[activeConversationId]
    if (pageState?.initialized || pageState?.isLoading) {
      return
    }

    void loadMessages(activeConversationId).catch((currentError) => {
      toast.error(getErrorMessage(currentError))
    })
    void markConversationAsRead(activeConversationId)
  }, [activeConversationId, messagePages])

  function handleKeywordChange(event: ChangeEvent<HTMLInputElement>) {
    setKeyword(event.currentTarget.value)
  }

  function handleStatusFilterChange(value: AdminConversationFilter) {
    setStatusFilter(value)
  }

  async function loadConversationPage(nextPage: number, loadMore: boolean) {
    if (!canUseAdminChat) {
      return
    }

    if (loadMore) {
      setIsLoadingMore(true)
    } else {
      setIsLoading(true)
    }

    try {
      const result = await getAdminConversations({
        page: nextPage,
        size: ADMIN_PAGE_SIZE,
        status: statusFilter === 'ALL' ? undefined : statusFilter,
        keyword: deferredKeyword === '' ? undefined : deferredKeyword,
      })

      setConversations((currentConversations) =>
        loadMore
          ? mergeConversations(currentConversations, result.items)
          : sortConversations(result.items),
      )
      setPage(result.page)
      setHasNext(result.hasNext)
      setTotalCount(result.totalCount)
      setActiveConversationId((currentConversationId) =>
        resolveNextActiveConversationId(
          currentConversationId,
          loadMore
            ? mergeConversations(conversations, result.items)
            : sortConversations(result.items),
        ),
      )
      setError(null)
    } catch (currentError) {
      const nextError = getErrorMessage(
        currentError,
        language === 'vi'
          ? 'Khong tai duoc danh sach chat ho tro'
          : 'Unable to load support conversations',
      )

      if (loadMore) {
        toast.error(nextError)
      } else {
        setError(nextError)
      }
    } finally {
      if (loadMore) {
        setIsLoadingMore(false)
      } else {
        setIsLoading(false)
      }
    }
  }

  async function refreshConversations() {
    await loadConversationPage(0, false)
  }

  async function loadMoreConversations() {
    if (isLoadingMore || !hasNext) {
      return
    }

    await loadConversationPage(page + 1, true)
  }

  async function loadMessages(
    conversationId: string,
    options: { loadMore?: boolean } = {},
  ) {
    const pageState = messagePages[conversationId] ?? defaultMessagePageState
    const loadMore = options.loadMore ?? false

    if (loadMore && (!pageState.initialized || !pageState.hasNext)) {
      return
    }

    setMessagePages((currentPages) => ({
      ...currentPages,
      [conversationId]: {
        ...(currentPages[conversationId] ?? defaultMessagePageState),
        isLoading: !loadMore,
        isLoadingMore: loadMore,
      },
    }))

    const nextPage = loadMore ? pageState.page + 1 : 0

    try {
      const result = await getAdminMessages(conversationId, {
        page: nextPage,
        size: pageState.size || MESSAGE_PAGE_SIZE,
      })

      setMessagesByConversation((currentMessagesByConversation) => ({
        ...currentMessagesByConversation,
        [conversationId]: loadMore
          ? mergeMessages(
              result.items,
              currentMessagesByConversation[conversationId] ?? [],
            )
          : sortMessages(result.items),
      }))
      setMessagePages((currentPages) => ({
        ...currentPages,
        [conversationId]: {
          page: result.page,
          size: result.size,
          totalCount: result.totalCount,
          hasNext: result.hasNext,
          initialized: true,
          isLoading: false,
          isLoadingMore: false,
        },
      }))
    } catch (currentError) {
      setMessagePages((currentPages) => ({
        ...currentPages,
        [conversationId]: {
          ...(currentPages[conversationId] ?? defaultMessagePageState),
          isLoading: false,
          isLoadingMore: false,
        },
      }))
      throw currentError
    }
  }

  async function markConversationAsRead(conversationId: string) {
    try {
      const updatedConversation = await markAdminConversationRead(conversationId)
      setConversations((currentConversations) =>
        mergeConversations(currentConversations, [updatedConversation]),
      )
    } catch {
      // Keep the page responsive; refresh will reconcile the count.
    }
  }

  async function sendMessage(content: string) {
    if (!activeConversationId || content.trim() === '') {
      return
    }

    setIsSending(true)

    try {
      const message = await sendAdminMessageRequest(activeConversationId, {
        content: content.trim(),
        messageType: 'TEXT',
      })

      setMessagesByConversation((currentMessagesByConversation) => ({
        ...currentMessagesByConversation,
        [activeConversationId]: mergeMessages(
          currentMessagesByConversation[activeConversationId] ?? [],
          [message],
        ),
      }))
      setConversations((currentConversations) =>
        currentConversations.map((conversation) =>
          conversation.conversationId === activeConversationId
            ? {
                ...conversation,
                lastMessageId: message.messageId,
                lastMessagePreview: message.content,
                lastMessageAt: message.createdAt,
                updatedAt: message.updatedAt,
              }
            : conversation,
        ),
      )
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsSending(false)
    }
  }

  async function assignConversation(staffId: string) {
    if (!activeConversationId) {
      return
    }

    setIsAssigning(true)

    try {
      const updatedConversation = await assignConversationRequest(
        activeConversationId,
        { staffId },
      )
      setConversations((currentConversations) =>
        mergeConversations(currentConversations, [updatedConversation]),
      )
      setPendingAssigneeId(updatedConversation.assignedStaffId ?? '')
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsAssigning(false)
    }
  }

  async function closeConversation() {
    if (!activeConversationId) {
      return
    }

    setIsUpdatingStatus(true)

    try {
      const updatedConversation =
        await closeAdminConversationRequest(activeConversationId)
      setConversations((currentConversations) =>
        mergeConversations(currentConversations, [updatedConversation]),
      )
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsUpdatingStatus(false)
    }
  }

  async function reopenConversation() {
    if (!activeConversationId) {
      return
    }

    setIsUpdatingStatus(true)

    try {
      const updatedConversation =
        await reopenAdminConversationRequest(activeConversationId)
      setConversations((currentConversations) =>
        mergeConversations(currentConversations, [updatedConversation]),
      )
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsUpdatingStatus(false)
    }
  }

  function selectConversation(conversationId: string) {
    setActiveConversationId(conversationId)
  }

  function getConversationMessages(conversationId: string | null) {
    if (!conversationId) {
      return []
    }

    return messagesByConversation[conversationId] ?? []
  }

  function getMessagePageState(conversationId: string | null) {
    if (!conversationId) {
      return defaultMessagePageState
    }

    return messagePages[conversationId] ?? defaultMessagePageState
  }

  function clearState() {
    setConversations([])
    setActiveConversationId(null)
    setMessagesByConversation({})
    setMessagePages({})
    setEmployeeOptions([])
    setKeyword('')
    setStatusFilter('ALL')
    setPage(0)
    setHasNext(false)
    setTotalCount(0)
    setIsLoading(false)
    setIsLoadingMore(false)
    setIsSending(false)
    setIsAssigning(false)
    setIsUpdatingStatus(false)
    setIsRealtimeConnected(false)
    setError(null)
    setPendingAssigneeId('')
  }

  return {
    user,
    locale,
    formatNumber,
    labels,
    conversations,
    activeConversation,
    activeConversationId,
    keyword,
    statusFilter,
    totalCount,
    unreadConversationCount,
    openConversationCount,
    hasNext,
    isLoading,
    isLoadingMore,
    isSending,
    isAssigning,
    isUpdatingStatus,
    isRealtimeConnected,
    error,
    canAssignEmployees,
    employeeOptions,
    pendingAssigneeId,
    setPendingAssigneeId,
    handleKeywordChange,
    handleStatusFilterChange,
    refreshConversations,
    loadMoreConversations,
    selectConversation,
    loadMessages,
    sendMessage,
    assignConversation,
    closeConversation,
    reopenConversation,
    getConversationMessages,
    getMessagePageState,
  }
}

function mergeConversations(
  currentConversations: ConversationResponse[],
  nextConversations: ConversationResponse[],
) {
  const conversationsById = new Map<string, ConversationResponse>()

  for (const conversation of currentConversations) {
    conversationsById.set(conversation.conversationId, conversation)
  }

  for (const conversation of nextConversations) {
    const previousConversation = conversationsById.get(conversation.conversationId)
    conversationsById.set(conversation.conversationId, {
      ...previousConversation,
      ...conversation,
    })
  }

  return sortConversations(Array.from(conversationsById.values()))
}

function sortConversations(conversations: ConversationResponse[]) {
  return [...conversations].sort((leftConversation, rightConversation) => {
    const rightTime = getConversationTimestamp(rightConversation)
    const leftTime = getConversationTimestamp(leftConversation)
    return rightTime - leftTime
  })
}

function getConversationTimestamp(conversation: ConversationResponse) {
  return new Date(
    conversation.lastMessageAt ??
      conversation.updatedAt ??
      conversation.createdAt,
  ).getTime()
}

function mergeMessages(
  currentMessages: ChatMessageResponse[],
  nextMessages: ChatMessageResponse[],
) {
  const messagesById = new Map<string, ChatMessageResponse>()

  for (const message of currentMessages) {
    messagesById.set(message.messageId, message)
  }

  for (const message of nextMessages) {
    messagesById.set(message.messageId, message)
  }

  return sortMessages(Array.from(messagesById.values()))
}

function sortMessages(messages: ChatMessageResponse[]) {
  return [...messages].sort(
    (leftMessage, rightMessage) =>
      new Date(leftMessage.createdAt).getTime() -
      new Date(rightMessage.createdAt).getTime(),
  )
}

function resolveNextActiveConversationId(
  currentConversationId: string | null,
  nextConversations: ConversationResponse[],
) {
  if (
    currentConversationId &&
    nextConversations.some(
      (conversation) => conversation.conversationId === currentConversationId,
    )
  ) {
    return currentConversationId
  }

  return nextConversations[0]?.conversationId ?? null
}

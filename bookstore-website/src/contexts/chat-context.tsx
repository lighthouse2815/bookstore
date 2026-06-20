import {
  createContext,
  useContext,
  useEffect,
  useEffectEvent,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  closeConversation as closeConversationRequest,
  createConversation as createConversationRequest,
  getMessages,
  getMyConversations,
  markConversationRead as markConversationReadRequest,
  sendMessage as sendMessageRequest,
} from '@/services/chat-service'
import {
  connectChatRealtime,
  disconnectChatRealtime,
} from '@/services/chat-realtime-service'
import type {
  ChatMessageResponse,
  ConversationResponse,
  CreateConversationRequest,
  SendChatMessageRequest,
} from '@/types/chat'
import { getErrorMessage } from '@/utils'

type MessagePageState = {
  page: number
  size: number
  totalCount: number
  hasNext: boolean
  isLoading: boolean
  isLoadingMore: boolean
  initialized: boolean
}

type SendMessageOptions = {
  conversationId?: string | null
  createConversation?: CreateConversationRequest
}

type ChatContextType = {
  conversations: ConversationResponse[]
  activeConversationId: string | null
  activeConversation: ConversationResponse | null
  unreadCount: number
  isLoading: boolean
  isRealtimeConnected: boolean
  error: string | null
  lastIncomingMessage: ChatMessageResponse | null
  refresh: () => Promise<void>
  createConversation: (
    data?: CreateConversationRequest,
  ) => Promise<ConversationResponse>
  setActiveConversation: (conversationId: string | null) => void
  loadMessages: (
    conversationId: string,
    options?: { loadMore?: boolean },
  ) => Promise<void>
  sendMessage: (
    payload: SendChatMessageRequest,
    options?: SendMessageOptions,
  ) => Promise<ConversationResponse>
  markRead: (conversationId: string) => Promise<void>
  closeConversation: (conversationId: string) => Promise<void>
  getConversationMessages: (
    conversationId: string | null,
  ) => ChatMessageResponse[]
  getMessagePageState: (conversationId: string | null) => MessagePageState
}

const ACCESS_TOKEN_KEY = 'accessToken'
const DEFAULT_MESSAGE_PAGE_SIZE = 30

const defaultMessagePageState: MessagePageState = {
  page: 0,
  size: DEFAULT_MESSAGE_PAGE_SIZE,
  totalCount: 0,
  hasNext: false,
  isLoading: false,
  isLoadingMore: false,
  initialized: false,
}

const ChatContext = createContext<ChatContextType | undefined>(undefined)

export function ChatProvider({ children }: { children: ReactNode }) {
  const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth()
  const { language } = useLanguage()
  const [conversations, setConversations] = useState<ConversationResponse[]>([])
  const [activeConversationId, setActiveConversationId] = useState<string | null>(
    null,
  )
  const [messagesByConversation, setMessagesByConversation] = useState<
    Record<string, ChatMessageResponse[]>
  >({})
  const [messagePages, setMessagePages] = useState<Record<string, MessagePageState>>(
    {},
  )
  const [isLoading, setIsLoading] = useState(false)
  const [isRealtimeConnected, setIsRealtimeConnected] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [lastIncomingMessage, setLastIncomingMessage] =
    useState<ChatMessageResponse | null>(null)
  const conversationsRef = useRef<ConversationResponse[]>([])
  const activeConversationIdRef = useRef<string | null>(null)

  const canUseChat = Boolean(
    isAuthenticated && user && user.roles.includes('USER'),
  )

  useEffect(() => {
    conversationsRef.current = conversations
  }, [conversations])

  useEffect(() => {
    activeConversationIdRef.current = activeConversationId
  }, [activeConversationId])

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

    if (message.senderRole !== 'USER') {
      setLastIncomingMessage(message)
    }
  })

  const handleRealtimeConversation = useEffectEvent(
    (conversation: ConversationResponse) => {
      setConversations((currentConversations) =>
        mergeConversations(currentConversations, [conversation]),
      )
    },
  )

  useEffect(() => {
    let isCancelled = false
    disconnectChatRealtime()

    if (isAuthLoading) {
      return
    }

    if (!canUseChat) {
      clearState()
      return
    }

    async function initialize() {
      setIsLoading(true)

      try {
        const nextConversations = sortConversations(await getMyConversations())

        if (isCancelled) {
          return
        }

        setConversations(nextConversations)
        setActiveConversationId((currentConversationId) =>
          resolveNextActiveConversationId(
            currentConversationId,
            nextConversations,
          ),
        )
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, getLoadErrorMessage(language)))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }

      if (isCancelled) {
        return
      }

      const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY)
      if (!accessToken) {
        setIsRealtimeConnected(false)
        return
      }

      connectChatRealtime(accessToken, {
        onMessage: handleRealtimeMessage,
        onConversation: handleRealtimeConversation,
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
    }

    void initialize()

    return () => {
      isCancelled = true
      disconnectChatRealtime()
      setIsRealtimeConnected(false)
    }
  }, [canUseChat, handleRealtimeConversation, handleRealtimeMessage, isAuthLoading, language])

  useEffect(() => {
    if (!canUseChat || !activeConversationId) {
      return
    }

    const currentPageState = messagePages[activeConversationId]

    if (currentPageState?.initialized || currentPageState?.isLoading) {
      return
    }

    void loadMessages(activeConversationId)
  }, [activeConversationId, canUseChat, messagePages])

  const activeConversation = useMemo(
    () =>
      conversations.find(
        (conversation) => conversation.conversationId === activeConversationId,
      ) ?? null,
    [activeConversationId, conversations],
  )

  const unreadCount = useMemo(
    () =>
      conversations.reduce(
        (totalUnreadCount, conversation) =>
          totalUnreadCount + conversation.myUnreadCount,
        0,
      ),
    [conversations],
  )

  async function refresh() {
    if (!canUseChat) {
      clearState()
      return
    }

    setIsLoading(true)

    try {
      const nextConversations = sortConversations(await getMyConversations())
      setConversations(nextConversations)
      setActiveConversationId((currentConversationId) =>
        resolveNextActiveConversationId(currentConversationId, nextConversations),
      )
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, getLoadErrorMessage(language)))
    } finally {
      setIsLoading(false)
    }
  }

  async function createConversation(
    data: CreateConversationRequest = {},
  ): Promise<ConversationResponse> {
    const nextConversation = await createConversationRequest({
      subject:
        toNullableString(data.subject) ?? getDefaultConversationSubject(language),
      priority: data.priority ?? 'NORMAL',
      targetType: data.targetType ?? 'GENERAL',
      targetId: data.targetId ?? null,
    })

    setConversations((currentConversations) =>
      mergeConversations(currentConversations, [nextConversation]),
    )
    setActiveConversationId(nextConversation.conversationId)
    setError(null)
    return nextConversation
  }

  async function loadMessages(
    conversationId: string,
    options: { loadMore?: boolean } = {},
  ) {
    const currentPageState = messagePages[conversationId] ?? defaultMessagePageState
    const loadMore = options.loadMore ?? false

    if (loadMore && (!currentPageState.initialized || !currentPageState.hasNext)) {
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

    const nextPage = loadMore ? currentPageState.page + 1 : 0

    try {
      const result = await getMessages(conversationId, {
        page: nextPage,
        size: currentPageState.size || DEFAULT_MESSAGE_PAGE_SIZE,
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
      setError(null)
    } catch (currentError) {
      setMessagePages((currentPages) => ({
        ...currentPages,
        [conversationId]: {
          ...(currentPages[conversationId] ?? defaultMessagePageState),
          isLoading: false,
          isLoadingMore: false,
        },
      }))
      throw new Error(getErrorMessage(currentError, getLoadMessagesError(language)))
    }
  }

  async function sendMessage(
    payload: SendChatMessageRequest,
    options: SendMessageOptions = {},
  ): Promise<ConversationResponse> {
    const requestedConversationId =
      options.conversationId ?? activeConversationIdRef.current
    let currentConversation =
      conversationsRef.current.find(
        (conversation) => conversation.conversationId === requestedConversationId,
      ) ?? null

    if (!currentConversation || currentConversation.status === 'CLOSED') {
      currentConversation = await createConversation(options.createConversation)
    }

    const sentMessage = await sendMessageRequest(
      currentConversation.conversationId,
      payload,
    )

    setMessagesByConversation((currentMessagesByConversation) => ({
      ...currentMessagesByConversation,
      [currentConversation.conversationId]: mergeMessages(
        currentMessagesByConversation[currentConversation.conversationId] ?? [],
        [sentMessage],
      ),
    }))
    setConversations((currentConversations) =>
      mergeConversations(currentConversations, [
        {
          ...currentConversation,
          lastMessageId: sentMessage.messageId,
          lastMessagePreview: sentMessage.content,
          lastMessageAt: sentMessage.createdAt,
          updatedAt: sentMessage.updatedAt,
        },
      ]),
    )
    setActiveConversationId(currentConversation.conversationId)
    setError(null)
    return currentConversation
  }

  async function markRead(conversationId: string) {
    const conversation = conversationsRef.current.find(
      (currentConversation) =>
        currentConversation.conversationId === conversationId,
    )

    if (!conversation || conversation.myUnreadCount === 0) {
      return
    }

    const updatedConversation = await markConversationReadRequest(conversationId)
    setConversations((currentConversations) =>
      mergeConversations(currentConversations, [updatedConversation]),
    )
    setError(null)
  }

  async function closeConversation(conversationId: string) {
    const updatedConversation = await closeConversationRequest(conversationId)
    setConversations((currentConversations) =>
      mergeConversations(currentConversations, [updatedConversation]),
    )
    setError(null)
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
    setLastIncomingMessage(null)
    setIsLoading(false)
    setError(null)
    setIsRealtimeConnected(false)
  }

  const value = useMemo<ChatContextType>(
    () => ({
      conversations,
      activeConversationId,
      activeConversation,
      unreadCount,
      isLoading,
      isRealtimeConnected,
      error,
      lastIncomingMessage,
      refresh,
      createConversation,
      setActiveConversation: setActiveConversationId,
      loadMessages,
      sendMessage,
      markRead,
      closeConversation,
      getConversationMessages,
      getMessagePageState,
    }),
    [
      activeConversation,
      activeConversationId,
      conversations,
      error,
      isLoading,
      isRealtimeConnected,
      lastIncomingMessage,
      unreadCount,
    ],
  )

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>
}

export function useChat() {
  const context = useContext(ChatContext)

  if (!context) {
    throw new Error('useChat must be used within ChatProvider')
  }

  return context
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
    const rightTimestamp = getConversationTimestamp(rightConversation)
    const leftTimestamp = getConversationTimestamp(leftConversation)
    return rightTimestamp - leftTimestamp
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

function getLoadErrorMessage(language: 'en' | 'vi') {
  return language === 'vi'
    ? 'Khong tai duoc cuoc tro chuyen ho tro'
    : 'Unable to load support conversations'
}

function getLoadMessagesError(language: 'en' | 'vi') {
  return language === 'vi'
    ? 'Khong tai duoc lich su tin nhan'
    : 'Unable to load chat history'
}

function getDefaultConversationSubject(language: 'en' | 'vi') {
  return language === 'vi' ? 'Ho tro khach hang' : 'Customer support'
}

function toNullableString(value?: string | null) {
  const trimmedValue = value?.trim()
  return trimmedValue ? trimmedValue : null
}

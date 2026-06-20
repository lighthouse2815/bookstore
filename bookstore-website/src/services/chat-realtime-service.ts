import { Client, type IMessage } from '@stomp/stompjs'
import type { ChatMessageResponse, ConversationResponse } from '@/types/chat'

type ConnectChatRealtimeOptions = {
  isAdmin?: boolean
  onMessage?: (message: ChatMessageResponse) => void
  onConversation?: (conversation: ConversationResponse) => void
  onAdminConversation?: (conversation: ConversationResponse) => void
  onConnect?: () => void
  onError?: (message: string) => void
}

let chatClient: Client | null = null

export function connectChatRealtime(
  accessToken: string,
  options: ConnectChatRealtimeOptions = {},
) {
  disconnectChatRealtime()

  const client = new Client({
    brokerURL: buildChatWebSocketUrl(),
    reconnectDelay: 5000,
    connectHeaders: {
      Authorization: `Bearer ${accessToken}`,
    },
    debug: () => undefined,
    onConnect: () => {
      client.subscribe('/user/queue/chat/messages', (frame) => {
        parseMessageFrame(frame, options.onMessage, options.onError)
      })

      client.subscribe('/user/queue/chat/conversations', (frame) => {
        parseConversationFrame(frame, options.onConversation, options.onError)
      })

      if (options.isAdmin) {
        client.subscribe('/topic/admin/chat/conversations', (frame) => {
          parseConversationFrame(frame, options.onAdminConversation, options.onError)
        })
      }

      options.onConnect?.()
    },
    onStompError: (frame) => {
      options.onError?.(frame.headers.message ?? 'Chat realtime error')
    },
    onWebSocketClose: () => {
      options.onError?.('Chat realtime disconnected')
    },
    onWebSocketError: () => {
      options.onError?.('Chat realtime connection failed')
    },
  })

  chatClient = client
  client.activate()
}

export function disconnectChatRealtime() {
  if (!chatClient) {
    return
  }

  const client = chatClient
  chatClient = null
  void client.deactivate()
}

function parseMessageFrame(
  frame: IMessage,
  onMessage: ConnectChatRealtimeOptions['onMessage'],
  onError: ConnectChatRealtimeOptions['onError'],
) {
  if (!onMessage) {
    return
  }

  try {
    onMessage(JSON.parse(frame.body) as ChatMessageResponse)
  } catch {
    onError?.('Invalid realtime chat message payload')
  }
}

function parseConversationFrame(
  frame: IMessage,
  onConversation:
    | ConnectChatRealtimeOptions['onConversation']
    | ConnectChatRealtimeOptions['onAdminConversation'],
  onError: ConnectChatRealtimeOptions['onError'],
) {
  if (!onConversation) {
    return
  }

  try {
    onConversation(JSON.parse(frame.body) as ConversationResponse)
  } catch {
    onError?.('Invalid realtime chat conversation payload')
  }
}

function buildChatWebSocketUrl() {
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'
  const url = new URL(apiBaseUrl)
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
  url.pathname = '/ws'
  url.search = ''
  return url.toString()
}

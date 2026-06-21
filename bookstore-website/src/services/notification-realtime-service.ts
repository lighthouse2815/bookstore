import { Client } from '@stomp/stompjs'
import type { NotificationResponse } from '@/types/notification'

let notificationClient: Client | null = null

export function connectNotificationRealtime(
  accessToken: string,
  onNotification: (notification: NotificationResponse) => void,
  onConnect?: () => void,
  onError?: (message: string) => void,
) {
  disconnectNotificationRealtime()

  const client = new Client({
    brokerURL: buildNotificationWebSocketUrl(),
    reconnectDelay: 5000,
    connectHeaders: {
      Authorization: `Bearer ${accessToken}`,
    },
    debug: () => undefined,
    onConnect: () => {
      client.subscribe('/user/queue/notifications', (message) => {
        try {
          onNotification(JSON.parse(message.body) as NotificationResponse)
        } catch {
          onError?.('Invalid realtime notification payload')
        }
      })

      onConnect?.()
    },
    onStompError: (frame) => {
      onError?.(frame.headers.message ?? 'Notification realtime error')
    },
    onWebSocketClose: () => {
      onError?.('Notification realtime disconnected')
    },
    onWebSocketError: () => {
      onError?.('Notification realtime connection failed')
    },
  })

  notificationClient = client
  client.activate()
}

export function disconnectNotificationRealtime() {
  if (!notificationClient) {
    return
  }

  const client = notificationClient
  notificationClient = null
  void client.deactivate()
}

function buildNotificationWebSocketUrl() {
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'
  const url = new URL(apiBaseUrl)
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
  url.pathname = '/ws'
  url.search = ''
  return url.toString()
}

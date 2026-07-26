import api from './api'
import type { ApiResponse } from '@/types/api'

export async function subscribeNewsletter(email: string): Promise<void> {
  await api.post<ApiResponse<null>>('/newsletter/subscriptions', { email })
}

export async function unsubscribeNewsletter(token: string): Promise<void> {
  await api.post<ApiResponse<null>>('/newsletter/subscriptions/unsubscribe', {
    token,
  })
}

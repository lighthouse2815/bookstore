import type { AxiosResponse } from 'axios'
import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AssignConversationRequest,
  AiChatReplyResponse,
  ChatMessagePageResult,
  ChatMessageQueryParams,
  ChatMessageResponse,
  ConversationPageResult,
  ConversationQueryParams,
  ConversationResponse,
  CreateConversationRequest,
  SendChatMessageRequest,
} from '@/types/chat'
import { unwrapResponse } from '@/utils'

const DEFAULT_ADMIN_CONVERSATION_PAGE_SIZE = 20
const DEFAULT_MESSAGE_PAGE_SIZE = 30

export async function getMyConversations(): Promise<ConversationResponse[]> {
  const response = await api.get<ApiResponse<ConversationResponse[]>>(
    '/chat/conversations/my',
  )
  return unwrapResponse(response)
}

export async function createConversation(
  data: CreateConversationRequest,
): Promise<ConversationResponse> {
  const response = await api.post<ApiResponse<ConversationResponse>>(
    '/chat/conversations',
    normalizeConversationPayload(data),
  )
  return unwrapResponse(response)
}

export async function getConversationDetail(
  conversationId: string,
): Promise<ConversationResponse> {
  const response = await api.get<ApiResponse<ConversationResponse>>(
    `/chat/conversations/${conversationId}`,
  )
  return unwrapResponse(response)
}

export async function getMessages(
  conversationId: string,
  params: ChatMessageQueryParams = {},
): Promise<ChatMessagePageResult> {
  const page = params.page ?? 0
  const size = params.size ?? DEFAULT_MESSAGE_PAGE_SIZE
  const response = await api.get<ApiResponse<ChatMessageResponse[]>>(
    `/chat/conversations/${conversationId}/messages`,
    {
      params: { page, size },
    },
  )

  return parseMessagePageResponse(response, page, size)
}

export async function sendMessage(
  conversationId: string,
  data: SendChatMessageRequest,
): Promise<ChatMessageResponse> {
  const response = await api.post<ApiResponse<ChatMessageResponse>>(
    `/chat/conversations/${conversationId}/messages`,
    normalizeMessagePayload(data),
  )
  return unwrapResponse(response)
}

export async function requestAiReply(
  conversationId: string,
): Promise<AiChatReplyResponse> {
  const response = await api.post<ApiResponse<AiChatReplyResponse>>(
    `/chat/conversations/${conversationId}/ai-reply`,
  )
  return unwrapResponse(response)
}

export async function markConversationRead(
  conversationId: string,
): Promise<ConversationResponse> {
  const response = await api.put<ApiResponse<ConversationResponse>>(
    `/chat/conversations/${conversationId}/read`,
  )
  return unwrapResponse(response)
}

export async function closeConversation(
  conversationId: string,
): Promise<ConversationResponse> {
  const response = await api.put<ApiResponse<ConversationResponse>>(
    `/chat/conversations/${conversationId}/close`,
  )
  return unwrapResponse(response)
}

export async function getAdminConversations(
  params: ConversationQueryParams = {},
): Promise<ConversationPageResult> {
  const page = params.page ?? 0
  const size = params.size ?? DEFAULT_ADMIN_CONVERSATION_PAGE_SIZE
  const response = await api.get<ApiResponse<ConversationResponse[]>>(
    '/admin/chat/conversations',
    {
      params: {
        page,
        size,
        ...(params.status ? { status: params.status } : {}),
        ...(params.keyword && params.keyword.trim() !== ''
          ? { keyword: params.keyword.trim() }
          : {}),
      },
    },
  )

  return parseConversationPageResponse(response, page, size)
}

export async function getAdminConversationDetail(
  conversationId: string,
): Promise<ConversationResponse> {
  const response = await api.get<ApiResponse<ConversationResponse>>(
    `/admin/chat/conversations/${conversationId}`,
  )
  return unwrapResponse(response)
}

export async function getAdminMessages(
  conversationId: string,
  params: ChatMessageQueryParams = {},
): Promise<ChatMessagePageResult> {
  const page = params.page ?? 0
  const size = params.size ?? DEFAULT_MESSAGE_PAGE_SIZE
  const response = await api.get<ApiResponse<ChatMessageResponse[]>>(
    `/admin/chat/conversations/${conversationId}/messages`,
    {
      params: { page, size },
    },
  )

  return parseMessagePageResponse(response, page, size)
}

export async function sendAdminMessage(
  conversationId: string,
  data: SendChatMessageRequest,
): Promise<ChatMessageResponse> {
  const response = await api.post<ApiResponse<ChatMessageResponse>>(
    `/admin/chat/conversations/${conversationId}/messages`,
    normalizeMessagePayload(data),
  )
  return unwrapResponse(response)
}

export async function assignConversation(
  conversationId: string,
  data: AssignConversationRequest,
): Promise<ConversationResponse> {
  const response = await api.put<ApiResponse<ConversationResponse>>(
    `/admin/chat/conversations/${conversationId}/assign`,
    data,
  )
  return unwrapResponse(response)
}

export async function markAdminConversationRead(
  conversationId: string,
): Promise<ConversationResponse> {
  const response = await api.put<ApiResponse<ConversationResponse>>(
    `/admin/chat/conversations/${conversationId}/read`,
  )
  return unwrapResponse(response)
}

export async function closeAdminConversation(
  conversationId: string,
): Promise<ConversationResponse> {
  const response = await api.put<ApiResponse<ConversationResponse>>(
    `/admin/chat/conversations/${conversationId}/close`,
  )
  return unwrapResponse(response)
}

export async function reopenAdminConversation(
  conversationId: string,
): Promise<ConversationResponse> {
  const response = await api.put<ApiResponse<ConversationResponse>>(
    `/admin/chat/conversations/${conversationId}/reopen`,
  )
  return unwrapResponse(response)
}

function normalizeConversationPayload(data: CreateConversationRequest) {
  return {
    subject: toNullableString(data.subject),
    priority: data.priority ?? 'NORMAL',
    targetType: data.targetType ?? 'GENERAL',
    targetId: toNullableString(data.targetId),
  }
}

function normalizeMessagePayload(data: SendChatMessageRequest) {
  return {
    messageType: data.messageType ?? 'TEXT',
    content: data.content,
    attachmentUrl: toNullableString(data.attachmentUrl),
    attachmentName: toNullableString(data.attachmentName),
    attachmentSize: data.attachmentSize ?? null,
  }
}

function parseConversationPageResponse(
  response: AxiosResponse<ApiResponse<ConversationResponse[]>>,
  fallbackPage: number,
  fallbackSize: number,
): ConversationPageResult {
  const items = unwrapResponse(response)
  const page = parseNumberHeader(response.headers['x-page'], fallbackPage)
  const size = parseNumberHeader(response.headers['x-size'], fallbackSize)
  const totalCount = parseNumberHeader(response.headers['x-total-count'], items.length)
  const hasNext = parseBooleanHeader(
    response.headers['x-has-next'],
    (page + 1) * size < totalCount,
  )

  return {
    items,
    page,
    size,
    totalCount,
    hasNext,
  }
}

function parseMessagePageResponse(
  response: AxiosResponse<ApiResponse<ChatMessageResponse[]>>,
  fallbackPage: number,
  fallbackSize: number,
): ChatMessagePageResult {
  const items = unwrapResponse(response)
  const page = parseNumberHeader(response.headers['x-page'], fallbackPage)
  const size = parseNumberHeader(response.headers['x-size'], fallbackSize)
  const totalCount = parseNumberHeader(response.headers['x-total-count'], items.length)
  const hasNext = parseBooleanHeader(
    response.headers['x-has-next'],
    (page + 1) * size < totalCount,
  )

  return {
    items,
    page,
    size,
    totalCount,
    hasNext,
  }
}

function parseNumberHeader(value: unknown, fallbackValue: number) {
  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : fallbackValue
}

function parseBooleanHeader(value: unknown, fallbackValue: boolean) {
  if (typeof value === 'string') {
    return value.toLowerCase() === 'true'
  }

  return fallbackValue
}

function toNullableString(value?: string | null) {
  const trimmedValue = value?.trim()
  return trimmedValue ? trimmedValue : null
}

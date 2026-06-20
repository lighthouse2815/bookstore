export type ConversationStatus = 'OPEN' | 'PENDING' | 'CLOSED'

export type ConversationPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'

export type ConversationTargetType = 'GENERAL' | 'ORDER' | 'BOOK'

export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM'

export type MessageSenderRole = 'USER' | 'ADMIN' | 'STAFF' | 'SYSTEM'

export type ConversationResponse = {
  conversationId: string
  customerId: string
  customerName: string | null
  customerEmail: string | null
  assignedStaffId: string | null
  assignedStaffName: string | null
  assignedStaffEmail: string | null
  status: ConversationStatus
  subject: string | null
  priority: ConversationPriority
  targetType: ConversationTargetType
  targetId: string | null
  lastMessageId: string | null
  lastMessagePreview: string | null
  lastMessageAt: string | null
  myUnreadCount: number
  createdAt: string
  updatedAt: string
  closedAt: string | null
}

export type ChatMessageResponse = {
  messageId: string
  conversationId: string
  senderId: string
  senderRole: MessageSenderRole
  messageType: MessageType
  content: string
  attachmentUrl: string | null
  attachmentName: string | null
  attachmentSize: number | null
  createdAt: string
  updatedAt: string
}

export type CreateConversationRequest = {
  subject?: string | null
  priority?: ConversationPriority | null
  targetType?: ConversationTargetType | null
  targetId?: string | null
}

export type SendChatMessageRequest = {
  messageType?: MessageType | null
  content: string
  attachmentUrl?: string | null
  attachmentName?: string | null
  attachmentSize?: number | null
}

export type AssignConversationRequest = {
  staffId: string
}

export type ConversationQueryParams = {
  page?: number
  size?: number
  status?: ConversationStatus
  keyword?: string
}

export type ChatMessageQueryParams = {
  page?: number
  size?: number
}

export type ConversationPageResult = {
  items: ConversationResponse[]
  page: number
  size: number
  totalCount: number
  hasNext: boolean
}

export type ChatMessagePageResult = {
  items: ChatMessageResponse[]
  page: number
  size: number
  totalCount: number
  hasNext: boolean
}

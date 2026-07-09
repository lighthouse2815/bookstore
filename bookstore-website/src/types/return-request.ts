import type {
  OrderPaymentMethod,
  OrderPaymentStatus,
  OrderStatus,
} from '@/types/order'

export type ReturnRequestStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCELLED'

export type CreateReturnRequestRequest = {
  reason: string
  requestedRefundAmount?: number | null
}

export type ApproveReturnRequestRequest = {
  adminNote?: string | null
  approvedRefundAmount?: number | null
  restock: boolean
}

export type RejectReturnRequestRequest = {
  adminNote: string
}

export type ReturnRequestResponse = {
  id: string
  orderId: string
  orderCode: string
  userId: string
  username: string | null
  userEmail: string | null
  receiverName: string | null
  reason: string
  status: ReturnRequestStatus
  requestedRefundAmount: number | null
  approvedRefundAmount: number | null
  adminNote: string | null
  processedBy: string | null
  processedByName: string | null
  processedAt: string | null
  orderStatus: OrderStatus | null
  paymentMethod: OrderPaymentMethod | null
  paymentStatus: OrderPaymentStatus | null
  orderFinalAmount: number | null
  orderCreatedAt: string | null
  createdAt: string
  updatedAt: string
}

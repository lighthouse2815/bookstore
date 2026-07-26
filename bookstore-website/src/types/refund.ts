export type RefundStatus = 'REQUESTED' | 'APPROVED' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED'
export type RefundMethod = 'MANUAL_BANK_TRANSFER' | 'ORIGINAL_PAYMENT_METHOD' | 'CASH'

export type Refund = {
  id: string
  orderId: string
  orderCode: string
  paymentId: string
  paymentProvider: string
  paymentStatus: string
  paidAmount: number
  returnRequestId: string | null
  amount: number
  currency: string
  reason: string
  method: RefundMethod
  status: RefundStatus
  externalReference: string | null
  evidenceUrl: string | null
  evidenceMetadata: string | null
  requestedBy: string
  approvedBy: string | null
  processedBy: string | null
  requestedAt: string
  approvedAt: string | null
  processedAt: string | null
  failureReason: string | null
  createdAt: string
  updatedAt: string
}

export type CreateRefundPayload = {
  returnRequestId?: string
  amount: number
  currency: string
  reason: string
  method: RefundMethod
}

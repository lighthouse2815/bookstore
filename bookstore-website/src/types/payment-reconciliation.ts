export type PaymentReconciliationIssueType =
  | 'PAYMENT_AFTER_EXPIRY'
  | 'PAYMENT_AFTER_CANCELLATION'
  | 'AMOUNT_MISMATCH'
  | 'PAYMENT_WITH_INVALID_ORDER_STATE'

export type PaymentReconciliationStatus = 'OPEN' | 'RESOLVED' | 'IGNORED'

export type PaymentReconciliationIssue = {
  id: string
  paymentId: string
  orderId: string
  issueType: PaymentReconciliationIssueType
  expectedAmount: number
  receivedAmount: number
  externalTransactionId: string | null
  details: string | null
  status: PaymentReconciliationStatus
  detectedAt: string
  resolvedAt: string | null
  resolvedBy: string | null
  resolutionNote: string | null
  createdAt: string
  updatedAt: string
}

export type OutboxStatus = 'PENDING' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED' | 'DEAD'

export interface OutboxEvent {
  id: string
  aggregateType: string
  aggregateId: string
  eventType: string
  status: OutboxStatus
  attemptCount: number
  nextAttemptAt: string
  lockedAt?: string | null
  lockedBy?: string | null
  lastError?: string | null
  createdAt: string
  processedAt?: string | null
}

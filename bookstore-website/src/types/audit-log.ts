export type AdminAuditLogResponse = {
  id: string
  actorId: string | null
  actorUsername: string | null
  actorRole: string | null
  action: string
  targetType: string
  targetId: string | null
  description: string | null
  beforeValue: string | null
  afterValue: string | null
  ipAddress: string | null
  userAgent: string | null
  createdAt: string
}

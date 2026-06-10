import type { UserRole, UserStatus } from '@/types/auth'

export type AdminUserResponse = {
  userId: string
  username: string
  email: string
  phoneNumber: string
  status: UserStatus
  locked: boolean
  roles: UserRole[]
  createdAt: string
  updatedAt: string
}

export type AdminRoleResponse = {
  id: string
  name: string
  description: string | null
  permissionCodes: string[]
  createdAt: string
  updatedAt: string
}

export type AdminPermissionResponse = {
  id: string
  code: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export type AdminPromotionResponse = {
  id: string
  code: string
  name: string
  description: string | null
  discountType: string
  discountValue: number
  active: boolean
  startAt: string | null
  endAt: string | null
  usageLimit: number | null
  usedCount: number
  createdAt: string
  updatedAt: string
}

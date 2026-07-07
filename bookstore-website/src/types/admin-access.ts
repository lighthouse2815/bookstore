import type { Gender, UserRole, UserStatus } from '@/types/auth'
import type { CouponDiscountType, CouponType } from '@/types/coupon'

export type ManagedAdminUserRole = 'ADMIN' | 'STAFF' | 'SHIPPER'

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

export type AdminCreateUserRequest = {
  username: string
  password: string
  phoneNumber: string
  email: string
  firstName: string
  lastName: string
  avatarFileAssetId?: string | null
  gender: Gender
  dateOfBirth: string
  roleName: ManagedAdminUserRole
}

export type AdminUpdateStaffUserRequest = {
  phoneNumber: string
  email: string
  roleNames: ManagedAdminUserRole[]
}

export type AdminRoleResponse = {
  id: string
  name: string
  description: string | null
  permissionCodes: string[]
  createdAt: string
  updatedAt: string
}

export type AdminRoleMutationRequest = {
  name: string
  description: string
  permissionCodes: string[]
}

export type AdminPermissionResponse = {
  id: string
  code: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export type AdminSupplierResponse = {
  id: string
  name: string
  phone: string | null
  email: string | null
  address: string | null
  note: string | null
  createdAt: string
  updatedAt: string
}

export type AdminSupplierMutationRequest = {
  name: string
  phone: string
  email: string
  address: string
  note: string
}

export type AdminImportReceiptItemResponse = {
  id: string
  bookId: string
  bookTitle: string
  unitCost: number
  quantity: number
  lineTotal: number
}

export type AdminImportReceiptResponse = {
  id: string
  supplierId: string
  items: AdminImportReceiptItemResponse[]
  totalAmount: number
  note: string | null
  createdAt: string
  updatedAt: string
  createdBy: string
}

export type AdminImportReceiptItemRequest = {
  bookId: string
  unitCost: number
  quantity: number
}

export type AdminCreateImportReceiptRequest = {
  supplierId: string
  items: AdminImportReceiptItemRequest[]
  note: string
}

export type AdminReviewResponse = {
  reviewId: string
  userId: string
  bookId: string
  orderItemId: string
  rating: number
  comment: string | null
  createdAt: string
  updatedAt: string
}

export type AdminNotificationResponse = {
  notificationId: string
  userId: string
  title: string
  content: string
  read: boolean
  createdAt: string
  updatedAt: string
  readAt: string | null
}

export type AdminCreateNotificationRequest = {
  userId: string
  title: string
  content: string
}

export type AdminPromotionResponse = {
  id: string
  code: string
  description: string | null
  couponType: CouponType
  discountType: CouponDiscountType
  discountValue: number
  minOrderAmount: number
  maxDiscountAmount: number | null
  active: boolean
  startsAt: string
  expiresAt: string
  maxUsageCount: number | null
  usedCount: number
  createdAt: string
  updatedAt: string | null
}

export type AdminPromotionMutationRequest = {
  code: string
  description: string | null
  couponType: CouponType
  discountType: CouponDiscountType
  discountValue: number
  minOrderAmount: number
  maxDiscountAmount: number | null
  maxUsageCount: number | null
  startsAt: string
  expiresAt: string
  active: boolean
}

export type AdminStockMovementType =
  | 'IMPORT'
  | 'SALE'
  | 'CANCEL_ORDER'
  | 'ADJUSTMENT'

export type AdminStockMovementResponse = {
  id: string
  bookId: string
  type: AdminStockMovementType
  quantity: number
  beforeQuantity: number
  afterQuantity: number
  referenceId: string | null
  referenceType: string
  note: string | null
  createdAt: string
  createdBy: string
}

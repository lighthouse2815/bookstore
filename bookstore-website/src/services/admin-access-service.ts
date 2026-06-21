import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AdminCreateImportReceiptRequest,
  AdminCreateNotificationRequest,
  AdminCreateUserRequest,
  AdminImportReceiptResponse,
  AdminNotificationResponse,
  AdminPermissionResponse,
  AdminPromotionResponse,
  AdminPromotionMutationRequest,
  AdminReviewResponse,
  AdminRoleMutationRequest,
  AdminRoleResponse,
  AdminStockMovementResponse,
  AdminSupplierMutationRequest,
  AdminSupplierResponse,
  AdminUpdateStaffUserRequest,
  AdminUserResponse,
} from '@/types/admin-access'
import { unwrapResponse } from '@/utils'

export async function createAdminUser(
  data: AdminCreateUserRequest,
): Promise<AdminUserResponse> {
  const response = await api.post<ApiResponse<AdminUserResponse>>(
    '/admin/users',
    data,
  )
  return unwrapResponse(response)
}

export async function getAdminCustomers(): Promise<AdminUserResponse[]> {
  const response = await api.get<ApiResponse<AdminUserResponse[]>>(
    '/admin/users/customers',
  )
  return unwrapResponse(response)
}

export async function getAdminStaffs(): Promise<AdminUserResponse[]> {
  const response = await api.get<ApiResponse<AdminUserResponse[]>>(
    '/admin/users/staff',
  )
  return unwrapResponse(response)
}

export async function getAdminAdmins(): Promise<AdminUserResponse[]> {
  const response = await api.get<ApiResponse<AdminUserResponse[]>>(
    '/admin/users/admins',
  )
  return unwrapResponse(response)
}

export async function getAdminEmployees(): Promise<AdminUserResponse[]> {
  const [staffs, admins] = await Promise.all([
    getAdminStaffs(),
    getAdminAdmins(),
  ])

  return Array.from(
    [...staffs, ...admins].reduce(
      (usersMap, currentUser) => {
        const existingUser = usersMap.get(currentUser.userId)

        if (!existingUser) {
          usersMap.set(currentUser.userId, currentUser)
          return usersMap
        }

        usersMap.set(currentUser.userId, {
          ...existingUser,
          ...currentUser,
          roles: Array.from(
            new Set([...existingUser.roles, ...currentUser.roles]),
          ),
        })

        return usersMap
      },
      new Map<string, AdminUserResponse>(),
    ),
  )
    .map(([, user]) => user)
    .sort((leftUser, rightUser) =>
      leftUser.username.localeCompare(rightUser.username),
    )
}

export async function updateAdminStaffUser(
  userId: string,
  data: AdminUpdateStaffUserRequest,
): Promise<AdminUserResponse> {
  const response = await api.put<ApiResponse<AdminUserResponse>>(
    `/admin/users/staff/${userId}`,
    data,
  )
  return unwrapResponse(response)
}

export async function lockAdminUser(userId: string): Promise<AdminUserResponse> {
  const response = await api.put<ApiResponse<AdminUserResponse>>(
    `/admin/users/${userId}/lock`,
  )
  return unwrapResponse(response)
}

export async function unlockAdminUser(
  userId: string,
): Promise<AdminUserResponse> {
  const response = await api.put<ApiResponse<AdminUserResponse>>(
    `/admin/users/${userId}/unlock`,
  )
  return unwrapResponse(response)
}

export async function deleteAdminUser(userId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/users/${userId}`)
}

export async function getAdminRoles(): Promise<AdminRoleResponse[]> {
  const response = await api.get<ApiResponse<AdminRoleResponse[]>>('/admin/roles')
  return unwrapResponse(response)
}

export async function createAdminRole(
  data: AdminRoleMutationRequest,
): Promise<AdminRoleResponse> {
  const response = await api.post<ApiResponse<AdminRoleResponse>>(
    '/admin/roles',
    data,
  )
  return unwrapResponse(response)
}

export async function updateAdminRole(
  roleId: string,
  data: AdminRoleMutationRequest,
): Promise<AdminRoleResponse> {
  const response = await api.put<ApiResponse<AdminRoleResponse>>(
    `/admin/roles/${roleId}`,
    data,
  )
  return unwrapResponse(response)
}

export async function deleteAdminRole(roleId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/roles/${roleId}`)
}

export async function getAdminPermissions(): Promise<AdminPermissionResponse[]> {
  const response = await api.get<ApiResponse<AdminPermissionResponse[]>>(
    '/admin/permissions',
  )
  return unwrapResponse(response)
}

export async function getAdminSuppliers(): Promise<AdminSupplierResponse[]> {
  const response = await api.get<ApiResponse<AdminSupplierResponse[]>>(
    '/admin/suppliers',
  )
  return unwrapResponse(response)
}

export async function createAdminSupplier(
  data: AdminSupplierMutationRequest,
): Promise<AdminSupplierResponse> {
  const response = await api.post<ApiResponse<AdminSupplierResponse>>(
    '/admin/suppliers',
    data,
  )
  return unwrapResponse(response)
}

export async function updateAdminSupplier(
  supplierId: string,
  data: AdminSupplierMutationRequest,
): Promise<AdminSupplierResponse> {
  const response = await api.put<ApiResponse<AdminSupplierResponse>>(
    `/admin/suppliers/${supplierId}`,
    data,
  )
  return unwrapResponse(response)
}

export async function deleteAdminSupplier(supplierId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/suppliers/${supplierId}`)
}

export async function getAdminImportReceipts(): Promise<
  AdminImportReceiptResponse[]
> {
  const response = await api.get<ApiResponse<AdminImportReceiptResponse[]>>(
    '/admin/import-receipts',
  )
  return unwrapResponse(response)
}

export async function createAdminImportReceipt(
  data: AdminCreateImportReceiptRequest,
): Promise<AdminImportReceiptResponse> {
  const response = await api.post<ApiResponse<AdminImportReceiptResponse>>(
    '/admin/import-receipts',
    data,
  )
  return unwrapResponse(response)
}

export async function getAdminReviews(): Promise<AdminReviewResponse[]> {
  const response =
    await api.get<ApiResponse<AdminReviewResponse[]>>('/admin/reviews')
  return unwrapResponse(response)
}

export async function deleteAdminReview(reviewId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/reviews/${reviewId}`)
}

export async function getAdminNotifications(): Promise<
  AdminNotificationResponse[]
> {
  const response = await api.get<ApiResponse<AdminNotificationResponse[]>>(
    '/admin/notifications',
  )
  return unwrapResponse(response)
}

export async function createAdminNotification(
  data: AdminCreateNotificationRequest,
): Promise<AdminNotificationResponse> {
  const response = await api.post<ApiResponse<AdminNotificationResponse>>(
    '/admin/notifications',
    data,
  )
  return unwrapResponse(response)
}

export async function getAdminPromotions(): Promise<AdminPromotionResponse[]> {
  const response = await api.get<ApiResponse<AdminPromotionResponse[]>>(
    '/admin/coupons',
  )
  return unwrapResponse(response)
}

export async function createAdminPromotion(
  data: AdminPromotionMutationRequest,
): Promise<AdminPromotionResponse> {
  const response = await api.post<ApiResponse<AdminPromotionResponse>>(
    '/admin/coupons',
    data,
  )
  return unwrapResponse(response)
}

export async function updateAdminPromotion(
  promotionId: string,
  data: AdminPromotionMutationRequest,
): Promise<AdminPromotionResponse> {
  const response = await api.put<ApiResponse<AdminPromotionResponse>>(
    `/admin/coupons/${promotionId}`,
    data,
  )
  return unwrapResponse(response)
}

export async function deleteAdminPromotion(promotionId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/coupons/${promotionId}`)
}

export async function getAdminStockMovements(): Promise<
  AdminStockMovementResponse[]
> {
  const response = await api.get<ApiResponse<AdminStockMovementResponse[]>>(
    '/admin/stock-movements',
  )
  return unwrapResponse(response)
}

export async function getAdminBookStockMovements(
  bookId: string,
): Promise<AdminStockMovementResponse[]> {
  const response = await api.get<ApiResponse<AdminStockMovementResponse[]>>(
    `/admin/books/${bookId}/stock-movements`,
  )
  return unwrapResponse(response)
}

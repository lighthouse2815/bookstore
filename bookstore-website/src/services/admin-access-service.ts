import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AdminPermissionResponse,
  AdminRoleResponse,
  AdminUserResponse,
} from '@/types/admin-access'
import { unwrapResponse } from '@/utils'

export async function getAdminUsers(): Promise<AdminUserResponse[]> {
  const response = await api.get<ApiResponse<AdminUserResponse[]>>('/admin/users')
  return unwrapResponse(response)
}

export async function getAdminRoles(): Promise<AdminRoleResponse[]> {
  const response = await api.get<ApiResponse<AdminRoleResponse[]>>('/admin/roles')
  return unwrapResponse(response)
}

export async function getAdminPermissions(): Promise<AdminPermissionResponse[]> {
  const response = await api.get<ApiResponse<AdminPermissionResponse[]>>(
    '/admin/permissions',
  )
  return unwrapResponse(response)
}

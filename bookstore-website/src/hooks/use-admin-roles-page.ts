import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminRole,
  deleteAdminRole,
  getAdminPermissions,
  getAdminRoles,
  updateAdminRole,
} from '@/services/admin-access-service'
import type {
  AdminPermissionResponse,
  AdminRoleMutationRequest,
  AdminRoleResponse,
} from '@/types/admin-access'
import { getErrorMessage } from '@/utils'

type RoleDialogMode = 'create' | 'view' | 'edit' | 'delete'

export type RoleFormState = {
  name: string
  description: string
  permissionCodes: string[]
}

const initialFormState: RoleFormState = {
  name: '',
  description: '',
  permissionCodes: [],
}

export function useAdminRolesPage() {
  const { language, t, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const [roles, setRoles] = useState<AdminRoleResponse[]>([])
  const [permissions, setPermissions] = useState<AdminPermissionResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] = useState<RoleDialogMode | null>(null)
  const [selectedRole, setSelectedRole] = useState<AdminRoleResponse | null>(null)
  const [form, setForm] = useState<RoleFormState>(initialFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  const filteredRoles = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return roles
    }

    return roles.filter((role) =>
      [role.name, role.description ?? '', ...role.permissionCodes]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [roles, searchTerm])

  const labels = useMemo(
    () => ({
      addRole: isVietnamese ? 'Thêm vai trò' : 'Add role',
      detailTitle: isVietnamese ? 'Chi tiết vai trò' : 'Role details',
      editTitle: isVietnamese ? 'Sửa vai trò' : 'Edit role',
      deleteTitle: isVietnamese ? 'Xác nhận xóa vai trò' : 'Confirm role deletion',
      deleteDescription: isVietnamese
        ? 'Hành động này sẽ xóa vai trò khỏi hệ thống và không thể hoàn tác.'
        : 'This action removes the role from the system and cannot be undone.',
      createSuccess: isVietnamese ? 'Đã tạo vai trò' : 'Role created successfully',
      updateSuccess: isVietnamese
        ? 'Đã cập nhật vai trò'
        : 'Role updated successfully',
      deleteSuccess: isVietnamese ? 'Đã xóa vai trò' : 'Role deleted successfully',
      loadError: isVietnamese
        ? 'Không tải được danh sách vai trò'
        : 'Unable to load the role list',
      saveError: isVietnamese ? 'Không lưu được vai trò' : 'Unable to save role',
      deleteError: isVietnamese ? 'Không xóa được vai trò' : 'Unable to delete role',
      permissionList: isVietnamese ? 'Danh sách quyền' : 'Permission list',
      noPermissions: isVietnamese ? 'Chưa có quyền nào' : 'No permissions assigned',
      descriptionEmpty: t('admin.rolesPage.noDescription'),
      showingCount: isVietnamese
        ? 'Hiển thị {count} trên {total} vai trò'
        : 'Showing {count} of {total} roles',
      roleName: isVietnamese ? 'Tên vai trò' : 'Role name',
      choosePermissions: isVietnamese ? 'Chọn quyền' : 'Choose permissions',
      roleDescription: isVietnamese ? 'Mô tả' : 'Description',
      searchPlaceholder: t('admin.rolesPage.searchPlaceholder'),
      empty: t('admin.rolesPage.empty'),
    }),
    [isVietnamese, t],
  )

  useEffect(() => {
    let isCancelled = false

    async function loadRoleData() {
      setIsLoading(true)

      try {
        const [roleResponse, permissionResponse] = await Promise.all([
          getAdminRoles(),
          getAdminPermissions(),
        ])

        if (isCancelled) {
          return
        }

        setRoles(roleResponse)
        setPermissions(permissionResponse)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, labels.loadError))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadRoleData()

    return () => {
      isCancelled = true
    }
  }, [labels.loadError])

  useEffect(() => {
    if (!dialogMode) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !(dialogMode === 'delete' && isDeleting)) {
        closeDialog()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [dialogMode, isDeleting])

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function resetDialog() {
    setDialogMode(null)
    setSelectedRole(null)
    setForm(initialFormState)
  }

  function closeDialog() {
    if (isSubmitting || isDeleting) {
      return
    }

    resetDialog()
  }

  function openCreateDialog() {
    setSelectedRole(null)
    setForm(initialFormState)
    setDialogMode('create')
  }

  function openViewDialog(role: AdminRoleResponse) {
    setSelectedRole(role)
    setDialogMode('view')
  }

  function openEditDialog(role: AdminRoleResponse) {
    setSelectedRole(role)
    setForm({
      name: role.name,
      description: role.description ?? '',
      permissionCodes: [...role.permissionCodes],
    })
    setDialogMode('edit')
  }

  function openEditFromView() {
    if (!selectedRole) {
      return
    }

    openEditDialog(selectedRole)
  }

  function openDeleteDialog(role: AdminRoleResponse) {
    setSelectedRole(role)
    setDialogMode('delete')
  }

  function handleFormChange(
    field: keyof RoleFormState,
    value: string | string[],
  ) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  function togglePermission(permissionCode: string) {
    setForm((currentForm) => {
      const nextCodes = currentForm.permissionCodes.includes(permissionCode)
        ? currentForm.permissionCodes.filter((code) => code !== permissionCode)
        : [...currentForm.permissionCodes, permissionCode]

      return {
        ...currentForm,
        permissionCodes: nextCodes,
      }
    })
  }

  async function reloadRoleData() {
    setIsLoading(true)

    try {
      const [roleResponse, permissionResponse] = await Promise.all([
        getAdminRoles(),
        getAdminPermissions(),
      ])

      setRoles(roleResponse)
      setPermissions(permissionResponse)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, labels.loadError))
    } finally {
      setIsLoading(false)
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    setIsSubmitting(true)

    try {
      const payload: AdminRoleMutationRequest = {
        name: form.name.trim(),
        description: form.description.trim(),
        permissionCodes: form.permissionCodes,
      }

      if (dialogMode === 'edit' && selectedRole) {
        await updateAdminRole(selectedRole.id, payload)
        toast.success(labels.updateSuccess)
      } else {
        await createAdminRole(payload)
        toast.success(labels.createSuccess)
      }

      await reloadRoleData()
      resetDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.saveError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteConfirm() {
    if (!selectedRole) {
      return
    }

    setIsDeleting(true)

    try {
      await deleteAdminRole(selectedRole.id)
      await reloadRoleData()
      resetDialog()
      toast.success(labels.deleteSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.deleteError))
    } finally {
      setIsDeleting(false)
    }
  }

  return {
    t,
    formatDate,
    formatNumber,
    roles,
    permissions,
    searchTerm,
    isLoading,
    error,
    dialogMode,
    selectedRole,
    form,
    isSubmitting,
    isDeleting,
    filteredRoles,
    labels,
    handleSearchTermChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    openEditDialog,
    openEditFromView,
    openDeleteDialog,
    handleFormChange,
    togglePermission,
    handleSubmit,
    handleDeleteConfirm,
  }
}

import {
  useEffect,
  useMemo,
  useState,
  type ChangeEvent,
  type FormEvent,
} from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminUser,
  deleteAdminUser,
  lockAdminUser,
  unlockAdminUser,
  updateAdminStaffUser,
} from '@/services/admin-access-service'
import type {
  AdminCreateUserRequest,
  AdminUserResponse,
  ManagedAdminUserRole,
} from '@/types/admin-access'
import type { Gender } from '@/types/auth'
import { getErrorMessage } from '@/utils'

export type AdminUserManagementMode = 'customer' | 'staff'

type UserDialogMode = 'create' | 'view' | 'edit' | 'delete' | 'lock'

export type CreateStaffFormState = {
  username: string
  password: string
  phoneNumber: string
  email: string
  firstName: string
  lastName: string
  avatarUrl: string
  gender: Gender
  dateOfBirth: string
  roleName: ManagedAdminUserRole
}

export type EditStaffFormState = {
  email: string
  phoneNumber: string
  roleName: ManagedAdminUserRole
}

export type AdminUserManagementLabels = {
  actions: string
  addEmployee: string
  cancel: string
  createError: string
  createSuccess: string
  deleteDescription: string
  deleteError: string
  deleteSuccess: string
  deleteTitle: string
  details: string
  editError: string
  editLockedHint: string
  editSuccess: string
  editTitle: string
  lockColumn: string
  lockDescription: string
  lockError: string
  lockSuccess: string
  lockTitle: string
  role: string
  selfManageBlocked: string
  showingCount: string
  status: string
}

type UseAdminUserManagementPageOptions = {
  fetchUsers: () => Promise<AdminUserResponse[]>
  loadErrorLabel: string
  mode: AdminUserManagementMode
}

const initialCreateFormState: CreateStaffFormState = {
  username: '',
  password: '',
  phoneNumber: '',
  email: '',
  firstName: '',
  lastName: '',
  avatarUrl: '',
  gender: 'OTHER',
  dateOfBirth: '',
  roleName: 'STAFF',
}

const initialEditFormState: EditStaffFormState = {
  email: '',
  phoneNumber: '',
  roleName: 'STAFF',
}

const genderOptions: Gender[] = ['MALE', 'FEMALE', 'OTHER']
const roleOptions: ManagedAdminUserRole[] = ['STAFF', 'ADMIN']

export function useAdminUserManagementPage({
  fetchUsers,
  loadErrorLabel,
  mode,
}: UseAdminUserManagementPageOptions) {
  const { user } = useAuth()
  const { language, t, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const canCreate = mode === 'staff'
  const canEdit = mode === 'staff'
  const [users, setUsers] = useState<AdminUserResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [dialogMode, setDialogMode] = useState<UserDialogMode | null>(null)
  const [selectedUser, setSelectedUser] = useState<AdminUserResponse | null>(null)
  const [createForm, setCreateForm] = useState<CreateStaffFormState>(
    initialCreateFormState,
  )
  const [editForm, setEditForm] = useState<EditStaffFormState>(
    initialEditFormState,
  )

  const filteredUsers = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return users
    }

    return users.filter((currentUser) =>
      [
        currentUser.username,
        currentUser.email,
        currentUser.phoneNumber,
        currentUser.status,
        ...currentUser.roles,
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [searchTerm, users])

  const labels = useMemo(
    () =>
      getAdminUserManagementLabels({
        isVietnamese,
        locked: selectedUser?.locked ?? false,
        mode,
        t,
      }),
    [isVietnamese, mode, selectedUser?.locked, t],
  )

  useEffect(() => {
    let isCancelled = false

    async function loadUsers() {
      setIsLoading(true)

      try {
        const response = await fetchUsers()

        if (isCancelled) {
          return
        }

        setUsers(response)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, loadErrorLabel))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadUsers()

    return () => {
      isCancelled = true
    }
  }, [fetchUsers, loadErrorLabel])

  useEffect(() => {
    if (!dialogMode) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !isSubmitting) {
        closeDialog()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [dialogMode, isSubmitting])

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function resetDialog() {
    setDialogMode(null)
    setSelectedUser(null)
    setCreateForm(initialCreateFormState)
    setEditForm(initialEditFormState)
  }

  function closeDialog() {
    if (isSubmitting) {
      return
    }

    resetDialog()
  }

  function openCreateDialog() {
    setCreateForm(initialCreateFormState)
    setDialogMode('create')
  }

  function openViewDialog(currentUser: AdminUserResponse) {
    setSelectedUser(currentUser)
    setDialogMode('view')
  }

  function openEditDialog(currentUser: AdminUserResponse) {
    if (!canEditUser(currentUser)) {
      return
    }

    setSelectedUser(currentUser)
    setEditForm({
      email: currentUser.email,
      phoneNumber: currentUser.phoneNumber,
      roleName: getManagedRole(currentUser),
    })
    setDialogMode('edit')
  }

  function openDeleteDialog(currentUser: AdminUserResponse) {
    setSelectedUser(currentUser)
    setDialogMode('delete')
  }

  function openLockDialog(currentUser: AdminUserResponse) {
    setSelectedUser(currentUser)
    setDialogMode('lock')
  }

  function openEditFromView() {
    if (!selectedUser) {
      return
    }

    openEditDialog(selectedUser)
  }

  function handleAttemptEdit(currentUser: AdminUserResponse) {
    if (!canEditUser(currentUser)) {
      toast.error(labels.editLockedHint)
      return
    }

    openEditDialog(currentUser)
  }

  function handleAttemptLock(currentUser: AdminUserResponse) {
    if (isSelfManagedUser(currentUser, user?.id)) {
      toast.error(labels.selfManageBlocked)
      return
    }

    openLockDialog(currentUser)
  }

  function handleAttemptDelete(currentUser: AdminUserResponse) {
    if (isSelfManagedUser(currentUser, user?.id)) {
      toast.error(labels.selfManageBlocked)
      return
    }

    openDeleteDialog(currentUser)
  }

  function handleCreateFormChange(
    field: keyof CreateStaffFormState,
    value: string,
  ) {
    setCreateForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  function handleEditFormChange(field: keyof EditStaffFormState, value: string) {
    setEditForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  async function reloadUsers() {
    setIsLoading(true)

    try {
      const response = await fetchUsers()
      setUsers(response)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, loadErrorLabel))
    } finally {
      setIsLoading(false)
    }
  }

  async function handleCreateSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    setIsSubmitting(true)

    try {
      const payload: AdminCreateUserRequest = {
        username: createForm.username.trim(),
        password: createForm.password,
        phoneNumber: createForm.phoneNumber.trim(),
        email: createForm.email.trim(),
        firstName: createForm.firstName.trim(),
        lastName: createForm.lastName.trim(),
        avatarUrl: toNullableString(createForm.avatarUrl),
        gender: createForm.gender,
        dateOfBirth: createForm.dateOfBirth,
        roleName: createForm.roleName,
      }

      await createAdminUser(payload)
      await reloadUsers()
      resetDialog()
      toast.success(labels.createSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.createError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleEditSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!selectedUser) {
      return
    }

    setIsSubmitting(true)

    try {
      await updateAdminStaffUser(selectedUser.userId, {
        email: editForm.email.trim(),
        phoneNumber: editForm.phoneNumber.trim(),
        roleNames: [editForm.roleName],
      })

      await reloadUsers()
      resetDialog()
      toast.success(labels.editSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.editError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteConfirm() {
    if (!selectedUser) {
      return
    }

    if (isSelfManagedUser(selectedUser, user?.id)) {
      toast.error(labels.selfManageBlocked)
      return
    }

    setIsSubmitting(true)

    try {
      await deleteAdminUser(selectedUser.userId)
      await reloadUsers()
      resetDialog()
      toast.success(labels.deleteSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.deleteError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleLockConfirm() {
    if (!selectedUser) {
      return
    }

    if (isSelfManagedUser(selectedUser, user?.id)) {
      toast.error(labels.selfManageBlocked)
      return
    }

    setIsSubmitting(true)

    try {
      if (selectedUser.locked) {
        await unlockAdminUser(selectedUser.userId)
      } else {
        await lockAdminUser(selectedUser.userId)
      }

      await reloadUsers()
      resetDialog()
      toast.success(labels.lockSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.lockError))
    } finally {
      setIsSubmitting(false)
    }
  }

  return {
    t,
    formatDate,
    formatNumber,
    isVietnamese,
    canCreate,
    canEdit,
    users,
    filteredUsers,
    searchTerm,
    isLoading,
    error,
    isSubmitting,
    dialogMode,
    selectedUser,
    createForm,
    editForm,
    labels,
    genderOptions,
    roleOptions,
    createDialogDescription: isVietnamese
      ? 'Tạo tài khoản nhân viên hoặc admin trực tiếp từ khu vực quản trị.'
      : 'Create a staff or admin account directly from the admin area.',
    editDialogDescription:
      selectedUser && canEditUser(selectedUser)
        ? isVietnamese
          ? 'Chỉnh sửa thông tin backend hiện cho phép với tài khoản nhân viên.'
          : 'Edit the fields currently supported by the backend for staff accounts.'
        : labels.editLockedHint,
    handleSearchTermChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    openEditFromView,
    handleAttemptEdit,
    handleAttemptLock,
    handleAttemptDelete,
    handleCreateFormChange,
    handleEditFormChange,
    handleCreateSubmit,
    handleEditSubmit,
    handleDeleteConfirm,
    handleLockConfirm,
    canEditUser,
    isSelfManagedUser: (currentUser: AdminUserResponse) =>
      isSelfManagedUser(currentUser, user?.id),
  }
}

function getAdminUserManagementLabels({
  isVietnamese,
  locked,
  mode,
  t,
}: {
  isVietnamese: boolean
  locked: boolean
  mode: AdminUserManagementMode
  t: (key: string, params?: Record<string, number | string>) => string
}): AdminUserManagementLabels {
  return {
    actions: t('common.actions'),
    addEmployee: isVietnamese ? 'Thêm nhân viên' : 'Add employee',
    cancel: t('common.cancel'),
    createError: isVietnamese
      ? 'Không tạo được nhân viên'
      : 'Unable to create employee',
    createSuccess: isVietnamese
      ? 'Đã tạo nhân viên'
      : 'Employee created successfully',
    deleteDescription: isVietnamese
      ? 'Hành động này sẽ xóa tài khoản khỏi hệ thống quản trị và không thể hoàn tác.'
      : 'This action removes the account from the admin system and cannot be undone.',
    deleteError: isVietnamese
      ? 'Không xóa được tài khoản'
      : 'Unable to delete account',
    deleteSuccess: isVietnamese
      ? 'Đã xóa tài khoản'
      : 'Account deleted successfully',
    deleteTitle: isVietnamese
      ? 'Xác nhận xóa tài khoản'
      : 'Confirm account deletion',
    details: isVietnamese
      ? mode === 'staff'
        ? 'Chi tiết nhân viên'
        : 'Chi tiết khách hàng'
      : mode === 'staff'
        ? 'Employee details'
        : 'Customer details',
    editError: isVietnamese
      ? 'Không cập nhật được nhân viên'
      : 'Unable to update employee',
    editLockedHint: isVietnamese
      ? 'API hiện tại chưa hỗ trợ sửa tài khoản admin thuần.'
      : 'The current API does not support editing admin-only accounts.',
    editSuccess: isVietnamese
      ? 'Đã cập nhật nhân viên'
      : 'Employee updated successfully',
    editTitle: isVietnamese ? 'Sửa nhân viên' : 'Edit employee',
    lockColumn: t('admin.usersPage.columns.locked'),
    lockDescription: isVietnamese
      ? locked
        ? 'Tài khoản này sẽ được mở lại để có thể đăng nhập và sử dụng hệ thống.'
        : 'Tài khoản này sẽ bị khóa và không thể đăng nhập cho đến khi được mở lại.'
      : locked
        ? 'This account will be unlocked so it can sign in and use the system again.'
        : 'This account will be locked and cannot sign in until it is unlocked again.',
    lockError: isVietnamese
      ? 'Không cập nhật được trạng thái khóa'
      : 'Unable to update lock status',
    lockSuccess: isVietnamese
      ? locked
        ? 'Đã mở khóa tài khoản'
        : 'Đã khóa tài khoản'
      : locked
        ? 'Account unlocked successfully'
        : 'Account locked successfully',
    lockTitle: isVietnamese
      ? locked
        ? 'Mở khóa tài khoản'
        : 'Khóa tài khoản'
      : locked
        ? 'Unlock account'
        : 'Lock account',
    role: isVietnamese ? 'Vai trò' : 'Role',
    selfManageBlocked: isVietnamese
      ? 'Không thể tự khóa hoặc xóa chính tài khoản đang đăng nhập.'
      : 'You cannot lock or delete the currently signed-in account.',
    showingCount: isVietnamese
      ? 'Hiển thị {count} trên {total} tài khoản'
      : 'Showing {count} of {total} accounts',
    status: t('admin.usersPage.columns.status'),
  }
}

function canEditUser(currentUser: AdminUserResponse) {
  return currentUser.roles.includes('STAFF')
}

function getManagedRole(currentUser: AdminUserResponse): ManagedAdminUserRole {
  return currentUser.roles.includes('ADMIN') ? 'ADMIN' : 'STAFF'
}

function isSelfManagedUser(
  currentUser: AdminUserResponse,
  currentUserId?: string,
) {
  return currentUserId !== undefined && currentUser.userId === currentUserId
}

function toNullableString(value: string) {
  const trimmedValue = value.trim()
  return trimmedValue === '' ? null : trimmedValue
}

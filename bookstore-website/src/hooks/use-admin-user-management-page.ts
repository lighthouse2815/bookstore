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
import { uploadManagedFile } from '@/services/file-service'
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
import type { PageRequest, PageResult } from '@/types/pagination'
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
  avatarFileAssetId: string
  avatarPreviewUrl: string
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
  status: string
}

type UseAdminUserManagementPageOptions = {
  fetchUsers: (params: PageRequest) => Promise<PageResult<AdminUserResponse>>
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
  avatarFileAssetId: '',
  avatarPreviewUrl: '',
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
const roleOptions: ManagedAdminUserRole[] = ['STAFF', 'SHIPPER', 'ADMIN']
const PAGE_SIZE = 10

export function useAdminUserManagementPage({
  fetchUsers,
  loadErrorLabel,
  mode,
}: UseAdminUserManagementPageOptions) {
  const { user } = useAuth()
  const { t, formatDate, formatNumber } = useLanguage()
  const canCreate = mode === 'staff'
  const canEdit = mode === 'staff'
  const [users, setUsers] = useState<AdminUserResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
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

  const labels = useMemo<AdminUserManagementLabels>(
    () => ({
      actions: t('common.actions'),
      addEmployee: t('admin.userManagement.addEmployee'),
      cancel: t('common.cancel'),
      createError: t('admin.userManagement.createError'),
      createSuccess: t('admin.userManagement.createSuccess'),
      deleteDescription: t('admin.userManagement.deleteDescription'),
      deleteError: t('admin.userManagement.deleteError'),
      deleteSuccess: t('admin.userManagement.deleteSuccess'),
      deleteTitle: t('admin.userManagement.deleteTitle'),
      details: t(
        mode === 'staff'
          ? 'admin.userManagement.detailsStaff'
          : 'admin.userManagement.detailsCustomer',
      ),
      editError: t('admin.userManagement.editError'),
      editLockedHint: t('admin.userManagement.editLockedHint'),
      editSuccess: t('admin.userManagement.editSuccess'),
      editTitle: t('admin.userManagement.editTitle'),
      lockColumn: t('admin.usersPage.columns.locked'),
      lockDescription: t(
        selectedUser?.locked
          ? 'admin.userManagement.lockDescription.unlock'
          : 'admin.userManagement.lockDescription.lock',
      ),
      lockError: t('admin.userManagement.lockError'),
      lockSuccess: t(
        selectedUser?.locked
          ? 'admin.userManagement.lockSuccess.unlock'
          : 'admin.userManagement.lockSuccess.lock',
      ),
      lockTitle: t(
        selectedUser?.locked
          ? 'admin.userManagement.lockTitle.unlock'
          : 'admin.userManagement.lockTitle.lock',
      ),
      role: t('admin.userManagement.role'),
      selfManageBlocked: t('admin.userManagement.selfManageBlocked'),
      status: t('admin.usersPage.columns.status'),
    }),
    [mode, selectedUser?.locked, t],
  )

  const avatarLabel = t('admin.userManagement.avatarLabel')
  const createDialogDescription = t('admin.userManagement.createDialogDescription')
  const editDialogDescription =
    selectedUser && canEditUser(selectedUser)
      ? t('admin.userManagement.editDialogDescription')
      : labels.editLockedHint
  const showingCountLabel = t('admin.userManagement.showingCount', {
    count: formatNumber(filteredUsers.length),
    total: formatNumber(totalCount),
  })

  useEffect(() => {
    let isCancelled = false

    async function loadUsers() {
      setIsLoading(true)

      try {
        const response = await fetchUsers({ page, size: PAGE_SIZE })

        if (isCancelled) {
          return
        }

        setUsers(response.items)
        setTotalCount(response.totalCount)
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
  }, [fetchUsers, loadErrorLabel, page])

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

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
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

  async function handleCreateAvatarFileChange(file: File | null) {
    if (!file) {
      return
    }

    try {
      const uploadedFile = await uploadManagedFile(file, {
        purpose: 'USER_AVATAR',
        visibility: 'PUBLIC',
      })
      setCreateForm((currentForm) => ({
        ...currentForm,
        avatarFileAssetId: uploadedFile.id,
        avatarPreviewUrl: uploadedFile.publicUrl ?? URL.createObjectURL(file),
        avatarUrl: uploadedFile.publicUrl ?? URL.createObjectURL(file),
      }))
    } catch (error) {
      toast.error(getErrorMessage(error, t('checkout.error')))
    }
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
      const response = await fetchUsers({ page, size: PAGE_SIZE })
      setUsers(response.items)
      setTotalCount(response.totalCount)
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
        avatarFileAssetId: toNullableString(createForm.avatarFileAssetId),
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
    canCreate,
    canEdit,
    users,
    page,
    pageSize: PAGE_SIZE,
    totalCount,
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
    avatarLabel,
    genderOptions,
    roleOptions,
    createDialogDescription,
    editDialogDescription,
    showingCountLabel,
    handleSearchTermChange,
    handlePageChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    openEditFromView,
    handleAttemptEdit,
    handleAttemptLock,
    handleAttemptDelete,
    handleCreateFormChange,
    handleCreateAvatarFileChange,
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

function canEditUser(currentUser: AdminUserResponse) {
  return (
    currentUser.roles.includes('ADMIN') ||
    currentUser.roles.includes('STAFF') ||
    currentUser.roles.includes('SHIPPER')
  )
}

function getManagedRole(currentUser: AdminUserResponse): ManagedAdminUserRole {
  if (currentUser.roles.includes('ADMIN')) {
    return 'ADMIN'
  }

  if (currentUser.roles.includes('SHIPPER')) {
    return 'SHIPPER'
  }

  return 'STAFF'
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

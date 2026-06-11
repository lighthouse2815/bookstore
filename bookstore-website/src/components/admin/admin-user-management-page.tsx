import { useEffect, useMemo, useState, type FormEvent, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Edit2,
  Eye,
  Lock,
  LockOpen,
  Mail,
  Phone,
  Plus,
  Search,
  Shield,
  Trash2,
  User2,
  X,
  type LucideIcon,
} from 'lucide-react'
import { toast } from 'sonner'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { AdminLayout } from '@/components/layout/admin-layout'
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
import type { Gender, UserRole, UserStatus } from '@/types/auth'
import { cn, getErrorMessage } from '@/utils'
import { getGenderLabel, getUserRoleLabel } from '@/utils/i18n'

type AdminUserManagementMode = 'customer' | 'staff'

type AdminUserManagementPageProps = {
  countIcon: LucideIcon
  description: string
  emptyLabel: string
  fetchUsers: () => Promise<AdminUserResponse[]>
  loadErrorLabel: string
  mode: AdminUserManagementMode
  searchPlaceholder: string
  title: string
  totalUsersLabel: (countLabel: string) => string
}

type UserDialogMode = 'create' | 'view' | 'edit' | 'delete' | 'lock'

type CreateStaffFormState = {
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

type EditStaffFormState = {
  email: string
  phoneNumber: string
  roleName: ManagedAdminUserRole
}

type AdminUserManagementLabels = {
  actions: string
  addEmployee: string
  createError: string
  createSuccess: string
  deleteDescription: string
  deleteError: string
  deleteSuccess: string
  deleteTitle: string
  details: string
  editError: string
  editLockedHint: string
  editTitle: string
  lockColumn: string
  lockError: string
  lockTitle: string
  lockDescription: string
  lockSuccess: string
  noDate: string
  role: string
  showingCount: string
  status: string
  viewInfo: string
  selfManageBlocked: string
}

const statusVariants: Record<
  UserStatus,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  ACTIVE: 'default',
  INACTIVE: 'secondary',
}

const tableGridClassName =
  'xl:grid xl:grid-cols-[minmax(0,2.2fr)_1fr_1fr_1fr_34rem]'

const knownRoles: UserRole[] = ['ADMIN', 'STAFF', 'USER']

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

export function AdminUserManagementPage({
  countIcon: CountIcon,
  description,
  emptyLabel,
  fetchUsers,
  loadErrorLabel,
  mode,
  searchPlaceholder,
  title,
  totalUsersLabel,
}: AdminUserManagementPageProps) {
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
      [currentUser.username, currentUser.status, ...currentUser.roles]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [searchTerm, users])

  const labels = useMemo(
    () => ({
      actions: isVietnamese ? 'Thao tác' : 'Actions',
      addEmployee: isVietnamese ? 'Thêm nhân viên' : 'Add employee',
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
      deleteTitle: isVietnamese ? 'Xác nhận xóa tài khoản' : 'Confirm account deletion',
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
      editTitle: isVietnamese ? 'Sửa nhân viên' : 'Edit employee',
      lockColumn: isVietnamese ? 'Khóa' : 'Lock',
      lockError: isVietnamese
        ? 'Không cập nhật được trạng thái khóa'
        : 'Unable to update lock status',
      lockTitle: isVietnamese
        ? selectedUser?.locked
          ? 'Mở khóa tài khoản'
          : 'Khóa tài khoản'
        : selectedUser?.locked
          ? 'Unlock account'
          : 'Lock account',
      lockDescription: isVietnamese
        ? selectedUser?.locked
          ? 'Tài khoản này sẽ được mở lại để có thể đăng nhập và sử dụng hệ thống.'
          : 'Tài khoản này sẽ bị khóa và không thể đăng nhập cho đến khi được mở lại.'
        : selectedUser?.locked
          ? 'This account will be unlocked so it can sign in and use the system again.'
          : 'This account will be locked and cannot sign in until it is unlocked again.',
      lockSuccess: isVietnamese
        ? selectedUser?.locked
          ? 'Đã mở khóa tài khoản'
          : 'Đã khóa tài khoản'
        : selectedUser?.locked
          ? 'Account unlocked successfully'
          : 'Account locked successfully',
      noDate: isVietnamese ? 'Chưa có' : 'Not available',
      role: isVietnamese ? 'Vai trò' : 'Role',
      showingCount: isVietnamese
        ? 'Hiển thị {count} trên {total} tài khoản'
        : 'Showing {count} of {total} accounts',
      status: isVietnamese ? 'Trạng thái' : 'Status',
      viewInfo: isVietnamese ? 'Xem thông tin' : 'View details',
      selfManageBlocked: isVietnamese
        ? 'Không thể tự khóa hoặc xóa chính tài khoản đang đăng nhập.'
        : 'You cannot lock or delete the currently signed-in account.',
    }),
    [isVietnamese, mode, selectedUser],
  )

  const genderOptions: Gender[] = ['MALE', 'FEMALE', 'OTHER']
  const roleOptions: ManagedAdminUserRole[] = ['STAFF', 'ADMIN']

  useEffect(() => {
    void loadUsers()
  }, [fetchUsers])

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

  async function loadUsers() {
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

  function handleEditFormChange(
    field: keyof EditStaffFormState,
    value: string,
  ) {
    setEditForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
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
      await loadUsers()
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

      await loadUsers()
      resetDialog()
      toast.success(isVietnamese ? 'Đã cập nhật nhân viên' : 'Employee updated successfully')
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
      await loadUsers()
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

      await loadUsers()
      resetDialog()
      toast.success(labels.lockSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.lockError))
    } finally {
      setIsSubmitting(false)
    }
  }

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={closeDialog}
        disabled={isSubmitting}
      />
      <div className="relative z-10 w-full max-w-3xl">
        {dialogMode === 'view' && selectedUser ? (
          <UserDetailDialogContent
            canEdit={canEditUser(selectedUser)}
            labels={labels}
            onClose={closeDialog}
            onEdit={openEditFromView}
            selectedUser={selectedUser}
            t={t}
            formatDate={formatDate}
          />
        ) : null}

        {dialogMode === 'create' && canCreate ? (
          <UserDialogShell
            title={labels.addEmployee}
            description={
              isVietnamese
                ? 'Tạo tài khoản nhân viên hoặc admin trực tiếp từ khu vực quản trị.'
                : 'Create a staff or admin account directly from the admin area.'
            }
            onClose={closeDialog}
          >
            <form className="space-y-5" onSubmit={handleCreateSubmit}>
              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  label={t('auth.login.username')}
                  input={
                    <Input
                      value={createForm.username}
                      onChange={(event) =>
                        handleCreateFormChange('username', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                      required
                    />
                  }
                />
                <FormField
                  label={t('auth.login.password')}
                  input={
                    <Input
                      type="password"
                      value={createForm.password}
                      onChange={(event) =>
                        handleCreateFormChange('password', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                      required
                    />
                  }
                />
                <FormField
                  label={t('auth.register.lastName')}
                  input={
                    <Input
                      value={createForm.lastName}
                      onChange={(event) =>
                        handleCreateFormChange('lastName', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                      required
                    />
                  }
                />
                <FormField
                  label={t('auth.register.firstName')}
                  input={
                    <Input
                      value={createForm.firstName}
                      onChange={(event) =>
                        handleCreateFormChange('firstName', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                      required
                    />
                  }
                />
                <FormField
                  label={t('common.email')}
                  input={
                    <Input
                      type="email"
                      value={createForm.email}
                      onChange={(event) =>
                        handleCreateFormChange('email', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                      required
                    />
                  }
                />
                <FormField
                  label={t('common.phone')}
                  input={
                    <Input
                      value={createForm.phoneNumber}
                      onChange={(event) =>
                        handleCreateFormChange(
                          'phoneNumber',
                          event.currentTarget.value,
                        )
                      }
                      className="h-11 rounded-2xl"
                      required
                    />
                  }
                />
                <FormField
                  label={t('auth.register.gender')}
                  input={
                    <Select
                      value={createForm.gender}
                      onValueChange={(nextValue) =>
                        handleCreateFormChange('gender', nextValue ?? 'OTHER')
                      }
                    >
                      <SelectTrigger className="h-11 w-full rounded-2xl px-4">
                        <SelectValue>
                          {getGenderLabel(createForm.gender, t)}
                        </SelectValue>
                      </SelectTrigger>
                      <SelectContent>
                        {genderOptions.map((gender) => (
                          <SelectItem key={gender} value={gender}>
                            {getGenderLabel(gender, t)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  }
                />
                <FormField
                  label={t('auth.register.dateOfBirth')}
                  input={
                    <Input
                      type="date"
                      value={createForm.dateOfBirth}
                      onChange={(event) =>
                        handleCreateFormChange(
                          'dateOfBirth',
                          event.currentTarget.value,
                        )
                      }
                      className="h-11 rounded-2xl"
                      required
                    />
                  }
                />
                <FormField
                  label={labels.role}
                  input={
                    <Select
                      value={createForm.roleName}
                      onValueChange={(nextValue) =>
                        handleCreateFormChange('roleName', nextValue ?? 'STAFF')
                      }
                    >
                      <SelectTrigger className="h-11 w-full rounded-2xl px-4">
                        <SelectValue>
                          {getUserRoleLabel(createForm.roleName, t)}
                        </SelectValue>
                      </SelectTrigger>
                      <SelectContent>
                        {roleOptions.map((role) => (
                          <SelectItem key={role} value={role}>
                            {getUserRoleLabel(role, t)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  }
                />
                <FormField
                  label={t('auth.register.avatarUrl')}
                  input={
                    <Input
                      value={createForm.avatarUrl}
                      onChange={(event) =>
                        handleCreateFormChange('avatarUrl', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                    />
                  }
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={closeDialog}
                  className="rounded-2xl"
                  disabled={isSubmitting}
                >
                  {t('common.cancel')}
                </Button>
                <Button
                  type="submit"
                  className="rounded-2xl"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? t('common.processing') : labels.addEmployee}
                </Button>
              </div>
            </form>
          </UserDialogShell>
        ) : null}

        {dialogMode === 'edit' && selectedUser ? (
          <UserDialogShell
            title={labels.editTitle}
            description={
              canEditUser(selectedUser)
                ? isVietnamese
                  ? 'Chỉnh sửa thông tin backend hiện cho phép với tài khoản nhân viên.'
                  : 'Edit the fields currently supported by the backend for staff accounts.'
                : labels.editLockedHint
            }
            onClose={closeDialog}
          >
            <form className="space-y-5" onSubmit={handleEditSubmit}>
              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  label={t('auth.login.username')}
                  input={
                    <Input
                      value={selectedUser.username}
                      className="h-11 rounded-2xl"
                      disabled
                    />
                  }
                />
                <FormField
                  label={labels.role}
                  input={
                    <Select
                      value={editForm.roleName}
                      onValueChange={(nextValue) =>
                        handleEditFormChange('roleName', nextValue ?? 'STAFF')
                      }
                      disabled={!canEditUser(selectedUser) || isSubmitting}
                    >
                      <SelectTrigger className="h-11 w-full rounded-2xl px-4">
                        <SelectValue>
                          {getUserRoleLabel(editForm.roleName, t)}
                        </SelectValue>
                      </SelectTrigger>
                      <SelectContent>
                        {roleOptions.map((role) => (
                          <SelectItem key={role} value={role}>
                            {getUserRoleLabel(role, t)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  }
                />
                <FormField
                  label={t('common.email')}
                  input={
                    <Input
                      type="email"
                      value={editForm.email}
                      onChange={(event) =>
                        handleEditFormChange('email', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                      disabled={!canEditUser(selectedUser) || isSubmitting}
                      required
                    />
                  }
                />
                <FormField
                  label={t('common.phone')}
                  input={
                    <Input
                      value={editForm.phoneNumber}
                      onChange={(event) =>
                        handleEditFormChange('phoneNumber', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                      disabled={!canEditUser(selectedUser) || isSubmitting}
                      required
                    />
                  }
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={closeDialog}
                  className="rounded-2xl"
                  disabled={isSubmitting}
                >
                  {t('common.cancel')}
                </Button>
                <Button
                  type="submit"
                  className="rounded-2xl"
                  disabled={!canEditUser(selectedUser) || isSubmitting}
                  title={!canEditUser(selectedUser) ? labels.editLockedHint : undefined}
                >
                  {isSubmitting ? t('common.processing') : t('common.save')}
                </Button>
              </div>
            </form>
          </UserDialogShell>
        ) : null}

        {(dialogMode === 'delete' || dialogMode === 'lock') && selectedUser ? (
          <ConfirmDialogContent
            description={
              dialogMode === 'delete'
                ? labels.deleteDescription
                : labels.lockDescription
            }
            confirmLabel={
              dialogMode === 'delete'
                ? t('common.delete')
                : selectedUser.locked
                  ? labels.lockTitle
                  : labels.lockTitle
            }
            destructive={dialogMode === 'delete' || !selectedUser.locked}
            isSubmitting={isSubmitting}
            onClose={closeDialog}
            onConfirm={
              dialogMode === 'delete' ? handleDeleteConfirm : handleLockConfirm
            }
            title={
              dialogMode === 'delete' ? labels.deleteTitle : labels.lockTitle
            }
            userLabel={selectedUser.username}
          />
        ) : null}
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(129,140,248,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(59,130,246,0.12),transparent_32%)]" />

          <div className="relative">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                    {title}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <CountIcon className="mr-2 h-4 w-4" />
                    {totalUsersLabel(formatNumber(users.length))}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {description}
                </p>
              </div>

              {canCreate ? (
                <Button
                  size="lg"
                  onClick={openCreateDialog}
                  className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
                >
                  <Plus className="mr-2 h-5 w-5" />
                  {labels.addEmployee}
                </Button>
              ) : null}
            </div>

            <div className="mt-8 flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
              <div className="w-full max-w-xl">
                <div className="relative">
                  <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    value={searchTerm}
                    onChange={(event) => setSearchTerm(event.currentTarget.value)}
                    placeholder={searchPlaceholder}
                    className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base shadow-[inset_0_1px_0_rgba(255,255,255,0.04)]"
                  />
                </div>
              </div>
            </div>

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden xl:block">
                  <div
                    className={cn(
                      'overflow-hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)]',
                      tableGridClassName,
                    )}
                  >
                    <div className="flex items-center gap-5 px-8 py-6">
                      <div aria-hidden="true" className="w-16 shrink-0" />
                      <p>{t('admin.usersPage.columns.username')}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{t('admin.usersPage.columns.roles')}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{labels.status}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{labels.lockColumn}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{labels.actions}</p>
                    </div>
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredUsers.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center">
                    <p className="text-base font-medium text-foreground">
                      {emptyLabel}
                    </p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {filteredUsers.map((currentUser) => {
                      const currentUserCanEdit = canEditUser(currentUser)
                      const selfManagedUser = isSelfManagedUser(
                        currentUser,
                        user?.id,
                      )

                      return (
                        <article
                          key={currentUser.userId}
                          className={cn(
                            'flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)]',
                            tableGridClassName,
                            'xl:gap-0 xl:p-0',
                          )}
                        >
                          <div className="flex min-w-0 items-center gap-5 xl:px-8 xl:py-6">
                            <div className="flex h-20 w-16 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
                              {currentUser.roles.includes('ADMIN') ? (
                                <Shield className="h-8 w-8 text-primary" />
                              ) : (
                                <User2 className="h-8 w-8 text-primary" />
                              )}
                            </div>

                            <div className="min-w-0">
                              <p className="truncate text-2xl font-semibold text-foreground">
                                {currentUser.username}
                              </p>
                            </div>
                          </div>

                          <div className="xl:flex xl:min-h-[152px] xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6 xl:text-center">
                            <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground xl:hidden">
                              {t('admin.usersPage.columns.roles')}
                            </p>
                            <div className="mt-2 flex flex-wrap justify-start gap-2 xl:mt-0 xl:justify-center">
                              {currentUser.roles.map((role) => (
                                <Badge
                                  key={`${currentUser.userId}-${role}`}
                                  variant="outline"
                                  className="rounded-2xl px-3 py-1.5 text-sm font-semibold"
                                >
                                  {getRoleLabel(role, t)}
                                </Badge>
                              ))}
                            </div>
                          </div>

                          <div className="xl:flex xl:min-h-[152px] xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6 xl:text-center">
                            <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground xl:hidden">
                              {labels.status}
                            </p>
                            <div className="mt-2 xl:mt-0">
                              <Badge
                                variant={statusVariants[currentUser.status]}
                                className="rounded-2xl px-3 py-1.5 text-sm font-semibold"
                              >
                                {getStatusLabel(currentUser.status, t)}
                              </Badge>
                            </div>
                          </div>

                          <div className="xl:flex xl:min-h-[152px] xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6 xl:text-center">
                            <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground xl:hidden">
                              {labels.lockColumn}
                            </p>
                            <div className="mt-2 xl:mt-0">
                              <Badge
                                variant={currentUser.locked ? 'destructive' : 'secondary'}
                                className="rounded-2xl px-3 py-1.5 text-sm font-semibold"
                              >
                                {currentUser.locked
                                  ? t('admin.usersPage.locked')
                                  : t('admin.usersPage.unlocked')}
                              </Badge>
                            </div>
                          </div>

                          <div className="flex flex-wrap gap-3 xl:min-h-[152px] xl:flex-nowrap xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6">
                            <Button
                              type="button"
                              variant="outline"
                              onClick={() => openViewDialog(currentUser)}
                              className="min-w-[96px] justify-center rounded-2xl bg-background/60"
                            >
                              <Eye className="mr-2 h-4 w-4" />
                              {t('common.view')}
                            </Button>

                            {canEdit ? (
                              <Button
                                type="button"
                                variant="outline"
                                onClick={() => handleAttemptEdit(currentUser)}
                                className="min-w-[96px] justify-center rounded-2xl bg-background/60"
                                title={
                                  !currentUserCanEdit ? labels.editLockedHint : undefined
                                }
                              >
                                <Edit2 className="mr-2 h-4 w-4" />
                                {t('common.edit')}
                              </Button>
                            ) : null}

                            <Button
                              type="button"
                              variant="outline"
                              onClick={() => handleAttemptLock(currentUser)}
                              className="min-w-[110px] justify-center rounded-2xl bg-background/60"
                              title={selfManagedUser ? labels.selfManageBlocked : undefined}
                            >
                              {currentUser.locked ? (
                                <LockOpen className="mr-2 h-4 w-4" />
                              ) : (
                                <Lock className="mr-2 h-4 w-4" />
                              )}
                              {getLockButtonLabel(currentUser.locked, isVietnamese)}
                            </Button>

                            <Button
                              type="button"
                              variant="destructive"
                              onClick={() => handleAttemptDelete(currentUser)}
                              className="min-w-[96px] justify-center rounded-2xl"
                              title={selfManagedUser ? labels.selfManageBlocked : undefined}
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              {t('common.delete')}
                            </Button>
                          </div>
                        </article>
                      )
                    })}
                  </div>
                )}
              </div>

              {!isLoading && !error && filteredUsers.length > 0 ? (
                <div className="grid gap-4 border-t border-border/60 px-6 py-5 text-sm text-muted-foreground xl:grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] xl:items-center">
                  <p className="min-w-0 xl:self-center">
                    {interpolateLabel(labels.showingCount, {
                      count: formatNumber(filteredUsers.length),
                      total: formatNumber(users.length),
                    })}
                  </p>
                  <div className="flex items-center justify-center gap-3 xl:justify-self-center">
                    <Button
                      type="button"
                      variant="outline"
                      disabled
                      className="size-12 rounded-2xl border-border/60 bg-background/40 p-0 text-muted-foreground opacity-60"
                    >
                      <ChevronLeft className="h-4 w-4" />
                    </Button>
                    <div className="flex h-12 min-w-[52px] items-center justify-center rounded-2xl bg-primary px-4 text-sm font-semibold text-primary-foreground shadow-[0_18px_40px_rgba(99,102,241,0.35)]">
                      1
                    </div>
                    <Button
                      type="button"
                      variant="outline"
                      disabled
                      className="size-12 rounded-2xl border-border/60 bg-background/40 p-0 text-muted-foreground opacity-60"
                    >
                      <ChevronRight className="h-4 w-4" />
                    </Button>
                  </div>
                  <div className="hidden xl:block" />
                </div>
              ) : null}
            </section>
          </div>
        </div>
      </AdminLayout>

      {dialogMarkup && typeof document !== 'undefined'
        ? createPortal(dialogMarkup, document.body)
        : null}
    </>
  )
}

type UserDialogShellProps = {
  children: ReactNode
  description?: string
  onClose: () => void
  title: string
}

function UserDialogShell({
  children,
  description,
  onClose,
  title,
}: UserDialogShellProps) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
          {description ? (
            <p className="mt-2 max-w-2xl text-sm text-muted-foreground">
              {description}
            </p>
          ) : null}
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          onClick={onClose}
          className="rounded-2xl"
        >
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div className="px-6 py-6">{children}</div>
    </div>
  )
}

type UserDetailDialogContentProps = {
  canEdit: boolean
  formatDate: (value: Date | number | string) => string
  labels: AdminUserManagementLabels
  onClose: () => void
  onEdit: () => void
  selectedUser: AdminUserResponse
  t: (key: string, params?: Record<string, number | string>) => string
}

function UserDetailDialogContent({
  canEdit,
  formatDate,
  labels,
  onClose,
  onEdit,
  selectedUser,
  t,
}: UserDetailDialogContentProps) {
  return (
    <UserDialogShell title={labels.details} onClose={onClose}>
      <div className="space-y-6">
        <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
          <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
            <div className="flex h-24 w-20 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
              {selectedUser.roles.includes('ADMIN') ? (
                <Shield className="h-9 w-9 text-primary" />
              ) : (
                <User2 className="h-9 w-9 text-primary" />
              )}
            </div>
            <div className="min-w-0">
              <p className="truncate text-3xl font-semibold text-foreground">
                {selectedUser.username}
              </p>
              <div className="mt-4 flex flex-wrap gap-2">
                {selectedUser.roles.map((role) => (
                  <Badge
                    key={`${selectedUser.userId}-${role}`}
                    variant="outline"
                    className="rounded-2xl px-3 py-1.5 text-sm font-semibold"
                  >
                    {getRoleLabel(role, t)}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <DetailCard
            icon={Mail}
            label={t('common.email')}
            value={selectedUser.email}
          />
          <DetailCard
            icon={Phone}
            label={t('common.phone')}
            value={selectedUser.phoneNumber}
          />
          <DetailCard
            icon={Shield}
            label={labels.status}
            value={getStatusLabel(selectedUser.status, t)}
          />
          <DetailCard
            icon={selectedUser.locked ? Lock : LockOpen}
            label={labels.lockColumn}
            value={
              selectedUser.locked
                ? t('admin.usersPage.locked')
                : t('admin.usersPage.unlocked')
            }
          />
          <DetailCard
            icon={CalendarDays}
            label={t('common.createdAt')}
            value={formatDate(selectedUser.createdAt)}
          />
          <DetailCard
            icon={CalendarDays}
            label={t('common.updatedAt')}
            value={formatDate(selectedUser.updatedAt)}
          />
        </div>

        <div className="flex items-center justify-end gap-3">
          <Button
            type="button"
            variant="outline"
            onClick={onClose}
            className="rounded-2xl"
          >
            {t('common.close')}
          </Button>
          {canEdit ? (
            <Button type="button" onClick={onEdit} className="rounded-2xl">
              <Edit2 className="mr-2 h-4 w-4" />
              {t('common.edit')}
            </Button>
          ) : null}
        </div>
      </div>
    </UserDialogShell>
  )
}

type ConfirmDialogContentProps = {
  confirmLabel: string
  description: string
  destructive: boolean
  isSubmitting: boolean
  onClose: () => void
  onConfirm: () => Promise<void>
  title: string
  userLabel: string
}

function ConfirmDialogContent({
  confirmLabel,
  description,
  destructive,
  isSubmitting,
  onClose,
  onConfirm,
  title,
  userLabel,
}: ConfirmDialogContentProps) {
  return (
    <div className="mx-auto max-w-xl overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start gap-4 px-6 py-6">
        <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-destructive/10 text-destructive">
          <AlertTriangle className="h-6 w-6" />
        </div>
        <div className="min-w-0 flex-1">
          <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
          <p className="mt-3 text-base font-medium text-foreground">{userLabel}</p>
          <p className="mt-2 text-sm text-muted-foreground">{description}</p>
        </div>
      </div>

      <div className="flex items-center justify-end gap-3 border-t border-border/60 px-6 py-5">
        <Button
          type="button"
          variant="outline"
          onClick={onClose}
          className="rounded-2xl"
          disabled={isSubmitting}
        >
          Hủy
        </Button>
        <Button
          type="button"
          variant={destructive ? 'destructive' : 'default'}
          onClick={() => {
            void onConfirm()
          }}
          className="rounded-2xl"
          disabled={isSubmitting}
        >
          {isSubmitting ? '...' : confirmLabel}
        </Button>
      </div>
    </div>
  )
}

type FormFieldProps = {
  input: ReactNode
  label: string
}

function FormField({ input, label }: FormFieldProps) {
  return (
    <div className="space-y-2">
      <Label>{label}</Label>
      {input}
    </div>
  )
}

type DetailCardProps = {
  icon: LucideIcon
  label: string
  value: string
}

function DetailCard({ icon: Icon, label, value }: DetailCardProps) {
  return (
    <div className="rounded-[22px] border border-border/60 bg-background/55 p-4">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Icon className="h-4 w-4" />
        <span>{label}</span>
      </div>
      <p className="mt-3 text-base font-semibold text-foreground">{value}</p>
    </div>
  )
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

function getRoleLabel(
  role: string,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return knownRoles.includes(role as UserRole)
    ? getUserRoleLabel(role as UserRole, t)
    : role
}

function getStatusLabel(
  status: UserStatus,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return status === 'ACTIVE'
    ? t('admin.usersPage.active')
    : t('admin.usersPage.inactive')
}

function toNullableString(value: string) {
  const trimmedValue = value.trim()
  return trimmedValue === '' ? null : trimmedValue
}

function interpolateLabel(
  template: string,
  params: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    String(params[key] ?? `{${key}}`),
  )
}

function getLockButtonLabel(locked: boolean, isVietnamese: boolean) {
  if (locked) {
    return isVietnamese ? 'Mở khóa' : 'Unlock'
  }

  return isVietnamese ? 'Khóa' : 'Lock'
}

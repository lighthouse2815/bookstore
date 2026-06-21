import { type ReactNode } from 'react'
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
import {
  useAdminUserManagementPage,
  type AdminUserManagementLabels,
  type AdminUserManagementMode,
} from '@/hooks/use-admin-user-management-page'
import { AdminLayout } from '@/components/layout/admin-layout'
import type { AdminUserResponse } from '@/types/admin-access'
import type { UserRole, UserStatus } from '@/types/auth'
import { cn } from '@/utils'
import { getGenderLabel, getUserRoleLabel } from '@/utils/i18n'

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
  const {
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
    avatarLabel,
    genderOptions,
    roleOptions,
    createDialogDescription,
    editDialogDescription,
    handleSearchTermChange,
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
    isSelfManagedUser,
  } = useAdminUserManagementPage({
    fetchUsers,
    loadErrorLabel,
    mode,
  })

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
            description={createDialogDescription}
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
                  label={avatarLabel}
                  input={
                    <Input
                      type="file"
                      accept="image/*"
                      onChange={(event) =>
                        void handleCreateAvatarFileChange(
                          event.currentTarget.files?.[0] ?? null,
                        )
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
            description={editDialogDescription}
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
            cancelLabel={labels.cancel}
            description={
              dialogMode === 'delete'
                ? labels.deleteDescription
                : labels.lockDescription
            }
            confirmLabel={
              dialogMode === 'delete' ? t('common.delete') : labels.lockTitle
            }
            destructive={dialogMode === 'delete' || !selectedUser.locked}
            isSubmitting={isSubmitting}
            onClose={closeDialog}
            onConfirm={
              dialogMode === 'delete' ? handleDeleteConfirm : handleLockConfirm
            }
            title={dialogMode === 'delete' ? labels.deleteTitle : labels.lockTitle}
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
                    onChange={handleSearchTermChange}
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
                      const selfManagedUser = isSelfManagedUser(currentUser)

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
  cancelLabel: string
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
  cancelLabel,
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
          {cancelLabel}
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

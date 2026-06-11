import { useEffect, useMemo, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  CalendarDays,
  Eye,
  KeyRound,
  Plus,
  RefreshCw,
  Search,
  Shield,
  Trash2,
  X,
  Edit2,
} from 'lucide-react'
import { toast } from 'sonner'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { AdminLayout } from '@/components/layout/admin-layout'
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
import type { UserRole } from '@/types/auth'
import { cn, getErrorMessage } from '@/utils'
import { getUserRoleLabel } from '@/utils/i18n'

type RoleDialogMode = 'create' | 'view' | 'edit' | 'delete'

type RoleFormState = {
  name: string
  description: string
  permissionCodes: string[]
}

const knownRoles: UserRole[] = ['ADMIN', 'STAFF', 'USER']
const roleVariants: Record<UserRole, 'default' | 'secondary' | 'outline'> = {
  ADMIN: 'default',
  STAFF: 'secondary',
  USER: 'outline',
}

const initialFormState: RoleFormState = {
  name: '',
  description: '',
  permissionCodes: [],
}

export default function AdminRolesPage() {
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
    void loadRoleData()
  }, [])

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

  async function loadRoleData() {
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

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
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

      await loadRoleData()
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
      await loadRoleData()
      resetDialog()
      toast.success(labels.deleteSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.deleteError))
    } finally {
      setIsDeleting(false)
    }
  }

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={closeDialog}
        disabled={isSubmitting || isDeleting}
      />
      <div className="relative z-10 w-full max-w-3xl">
        {dialogMode === 'view' && selectedRole ? (
          <RoleDetailDialog
            labels={labels}
            onClose={closeDialog}
            onEdit={openEditFromView}
            permissions={permissions}
            role={selectedRole}
            t={t}
            formatDate={formatDate}
          />
        ) : null}

        {(dialogMode === 'create' || dialogMode === 'edit') ? (
          <RoleFormDialog
            canClose={!isSubmitting}
            form={form}
            isSubmitting={isSubmitting}
            labels={labels}
            mode={dialogMode}
            onClose={closeDialog}
            onPermissionToggle={togglePermission}
            onSubmit={handleSubmit}
            onValueChange={handleFormChange}
            permissions={permissions}
            t={t}
          />
        ) : null}

        {dialogMode === 'delete' && selectedRole ? (
          <ConfirmDeleteDialog
            isDeleting={isDeleting}
            labels={labels}
            onClose={closeDialog}
            onConfirm={handleDeleteConfirm}
            role={selectedRole}
            t={t}
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
                    {t('admin.rolesPage.title')}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <Shield className="mr-2 h-4 w-4" />
                    {t('admin.rolesPage.totalRoles', {
                      count: formatNumber(roles.length),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {t('admin.rolesPage.description')}
                </p>
              </div>

              <Button
                size="lg"
                onClick={openCreateDialog}
                className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
              >
                <Plus className="mr-2 h-5 w-5" />
                {labels.addRole}
              </Button>
            </div>

            <div className="mt-8 flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
              <div className="w-full max-w-xl">
                <div className="relative">
                  <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    value={searchTerm}
                    onChange={(event) => setSearchTerm(event.currentTarget.value)}
                    placeholder={labels.searchPlaceholder}
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
                  <div className="grid overflow-hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid-cols-[minmax(0,2.6fr)_22rem]">
                    <div className="px-8 py-6">
                      <p>{labels.roleName}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{t('common.actions')}</p>
                    </div>
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredRoles.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center">
                    <p className="text-base font-medium text-foreground">
                      {labels.empty}
                    </p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {filteredRoles.map((role) => (
                      <article
                        key={role.id}
                        className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2.6fr)_22rem] xl:gap-0 xl:p-0"
                      >
                        <div className="flex min-w-0 items-center gap-5 xl:px-8 xl:py-6">
                          <div className="flex h-20 w-16 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
                            <Shield className="h-8 w-8 text-primary" />
                          </div>

                          <div className="min-w-0">
                            <p className="truncate text-2xl font-semibold text-foreground">
                              {getRoleLabel(role.name, t)}
                            </p>
                          </div>
                        </div>

                        <div className="flex flex-wrap gap-3 xl:min-h-[128px] xl:flex-nowrap xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => openViewDialog(role)}
                            className="min-w-[96px] justify-center rounded-2xl bg-background/60"
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            {t('common.view')}
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => openEditDialog(role)}
                            className="min-w-[96px] justify-center rounded-2xl bg-background/60"
                          >
                            <Edit2 className="mr-2 h-4 w-4" />
                            {t('common.edit')}
                          </Button>
                          <Button
                            type="button"
                            variant="destructive"
                            onClick={() => openDeleteDialog(role)}
                            className="min-w-[96px] justify-center rounded-2xl"
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            {t('common.delete')}
                          </Button>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </div>

              {!isLoading && !error && filteredRoles.length > 0 ? (
                <div className="border-t border-border/60 px-6 py-5 text-sm text-muted-foreground">
                  {interpolateLabel(labels.showingCount, {
                    count: formatNumber(filteredRoles.length),
                    total: formatNumber(roles.length),
                  })}
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

function RoleFormDialog({
  canClose,
  form,
  isSubmitting,
  labels,
  mode,
  onClose,
  onPermissionToggle,
  onSubmit,
  onValueChange,
  permissions,
  t,
}: {
  canClose: boolean
  form: RoleFormState
  isSubmitting: boolean
  labels: {
    addRole: string
    choosePermissions: string
    editTitle: string
    roleDescription: string
    roleName: string
  }
  mode: 'create' | 'edit'
  onClose: () => void
  onPermissionToggle: (permissionCode: string) => void
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => Promise<void>
  onValueChange: (field: keyof RoleFormState, value: string | string[]) => void
  permissions: AdminPermissionResponse[]
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <DialogShell
      title={mode === 'create' ? labels.addRole : labels.editTitle}
      onClose={onClose}
      canClose={canClose}
    >
      <form className="space-y-5" onSubmit={(event) => void onSubmit(event)}>
        <div className="space-y-2">
          <Label>{labels.roleName}</Label>
          <Input
            value={form.name}
            onChange={(event) => onValueChange('name', event.currentTarget.value)}
            className="h-11 rounded-2xl"
            required
          />
        </div>

        <div className="space-y-2">
          <Label>{labels.roleDescription}</Label>
          <Textarea
            value={form.description}
            onChange={(event) =>
              onValueChange('description', event.currentTarget.value)
            }
            className="min-h-28 rounded-2xl"
            required
          />
        </div>

        <div className="space-y-3">
          <Label>{labels.choosePermissions}</Label>
          <div className="grid gap-3 md:grid-cols-2">
            {permissions.map((permission) => {
              const isSelected = form.permissionCodes.includes(permission.code)

              return (
                <button
                  key={permission.id}
                  type="button"
                  onClick={() => onPermissionToggle(permission.code)}
                  className={cn(
                    'rounded-[20px] border px-4 py-3 text-left transition-colors',
                    isSelected
                      ? 'border-primary/40 bg-primary/12 text-foreground'
                      : 'border-border/60 bg-background/55 text-foreground hover:bg-background/75',
                  )}
                >
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-sm font-semibold">{permission.code}</span>
                    {isSelected ? (
                      <Badge variant="outline" className="rounded-2xl">
                        {t('common.view')}
                      </Badge>
                    ) : null}
                  </div>
                  <p className="mt-2 text-xs text-muted-foreground">
                    {permission.description || permission.code}
                  </p>
                </button>
              )
            })}
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-2">
          <Button
            type="button"
            variant="outline"
            onClick={onClose}
            className="rounded-2xl"
            disabled={isSubmitting}
          >
            {t('common.cancel')}
          </Button>
          <Button
            type="submit"
            className="rounded-2xl"
            disabled={isSubmitting || form.permissionCodes.length === 0}
          >
            {isSubmitting ? t('common.processing') : t('common.save')}
          </Button>
        </div>
      </form>
    </DialogShell>
  )
}

function RoleDetailDialog({
  formatDate,
  labels,
  onClose,
  onEdit,
  permissions,
  role,
  t,
}: {
  formatDate: (value: Date | number | string) => string
  labels: {
    descriptionEmpty: string
    detailTitle: string
    noPermissions: string
    permissionList: string
    roleDescription: string
  }
  onClose: () => void
  onEdit: () => void
  permissions: AdminPermissionResponse[]
  role: AdminRoleResponse
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  const permissionDescriptionMap = new Map(
    permissions.map((permission) => [permission.code, permission.description]),
  )

  return (
    <DialogShell title={labels.detailTitle} onClose={onClose}>
      <div className="space-y-6">
        <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
          <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
            <div className="flex h-24 w-20 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
              <Shield className="h-9 w-9 text-primary" />
            </div>
            <div className="min-w-0">
              <p className="truncate text-3xl font-semibold text-foreground">
                {getRoleLabel(role.name, t)}
              </p>
            </div>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={CalendarDays}
            label={t('common.createdAt')}
            value={formatDate(role.createdAt)}
          />
          <DetailCard
            icon={RefreshCw}
            label={t('common.updatedAt')}
            value={formatDate(role.updatedAt)}
          />
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={KeyRound}
            label={labels.roleDescription}
            value={role.description || labels.descriptionEmpty}
          />
          <DetailCard
            icon={Shield}
            label={labels.permissionList}
            value={String(role.permissionCodes.length)}
          />
        </div>

        <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
          <p className="text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground">
            {labels.permissionList}
          </p>

          {role.permissionCodes.length === 0 ? (
            <p className="mt-4 text-sm text-muted-foreground">
              {labels.noPermissions}
            </p>
          ) : (
            <div className="mt-4 flex flex-wrap gap-3">
              {role.permissionCodes.map((permissionCode) => (
                <div
                  key={`${role.id}-${permissionCode}`}
                  className="rounded-[20px] border border-border/60 bg-background/70 px-4 py-3"
                >
                  <p className="text-sm font-semibold text-foreground">
                    {permissionCode}
                  </p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {permissionDescriptionMap.get(permissionCode) ||
                      labels.descriptionEmpty}
                  </p>
                </div>
              ))}
            </div>
          )}
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
          <Button type="button" onClick={onEdit} className="rounded-2xl">
            <Edit2 className="mr-2 h-4 w-4" />
            {t('common.edit')}
          </Button>
        </div>
      </div>
    </DialogShell>
  )
}

function ConfirmDeleteDialog({
  isDeleting,
  labels,
  onClose,
  onConfirm,
  role,
  t,
}: {
  isDeleting: boolean
  labels: {
    deleteDescription: string
    deleteTitle: string
  }
  onClose: () => void
  onConfirm: () => Promise<void>
  role: AdminRoleResponse
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <div className="mx-auto max-w-xl overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start gap-4 px-6 py-6">
        <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-destructive/10 text-destructive">
          <AlertTriangle className="h-6 w-6" />
        </div>
        <div className="min-w-0 flex-1">
          <h2 className="text-2xl font-semibold text-foreground">
            {labels.deleteTitle}
          </h2>
          <p className="mt-3 text-base font-medium text-foreground">
            {getRoleLabel(role.name, t)}
          </p>
          <p className="mt-2 text-sm text-muted-foreground">
            {labels.deleteDescription}
          </p>
        </div>
      </div>

      <div className="flex items-center justify-end gap-3 border-t border-border/60 px-6 py-5">
        <Button
          type="button"
          variant="outline"
          onClick={onClose}
          className="rounded-2xl"
          disabled={isDeleting}
        >
          {t('common.cancel')}
        </Button>
        <Button
          type="button"
          variant="destructive"
          onClick={() => {
            void onConfirm()
          }}
          className="rounded-2xl"
          disabled={isDeleting}
        >
          {isDeleting ? t('common.processing') : t('common.delete')}
        </Button>
      </div>
    </div>
  )
}

function DialogShell({
  canClose = true,
  children,
  onClose,
  title,
}: {
  canClose?: boolean
  children: React.ReactNode
  onClose: () => void
  title: string
}) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          onClick={onClose}
          className="rounded-2xl"
          disabled={!canClose}
        >
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div className="px-6 py-6">{children}</div>
    </div>
  )
}

function DetailCard({
  icon: Icon,
  label,
  value,
}: {
  icon: typeof Shield
  label: string
  value: string
}) {
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

function getRoleVariant(roleName: string) {
  return knownRoles.includes(roleName as UserRole)
    ? roleVariants[roleName as UserRole]
    : 'outline'
}

function getRoleLabel(
  roleName: string,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return knownRoles.includes(roleName as UserRole)
    ? getUserRoleLabel(roleName as UserRole, t)
    : roleName
}

function interpolateLabel(
  template: string,
  params: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    String(params[key] ?? `{${key}}`),
  )
}

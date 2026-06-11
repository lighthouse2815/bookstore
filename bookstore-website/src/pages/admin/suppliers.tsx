import { useEffect, useMemo, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  CalendarDays,
  Edit2,
  Eye,
  Mail,
  MapPin,
  Phone,
  Plus,
  RefreshCw,
  Search,
  StickyNote,
  Trash2,
  Truck,
  X,
  type LucideIcon,
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
  createAdminSupplier,
  deleteAdminSupplier,
  getAdminSuppliers,
  updateAdminSupplier,
} from '@/services/admin-access-service'
import type {
  AdminSupplierMutationRequest,
  AdminSupplierResponse,
} from '@/types/admin-access'
import { getErrorMessage } from '@/utils'

type SupplierDialogMode = 'create' | 'view' | 'edit' | 'delete'

type SupplierFormState = {
  name: string
  phone: string
  email: string
  address: string
  note: string
}

const initialFormState: SupplierFormState = {
  name: '',
  phone: '',
  email: '',
  address: '',
  note: '',
}

export default function AdminSuppliersPage() {
  const { language, t, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const [suppliers, setSuppliers] = useState<AdminSupplierResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] = useState<SupplierDialogMode | null>(null)
  const [selectedSupplier, setSelectedSupplier] =
    useState<AdminSupplierResponse | null>(null)
  const [form, setForm] = useState<SupplierFormState>(initialFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  const labels = useMemo(
    () => ({
      pageTitle: isVietnamese ? 'Quản lý nhà cung cấp' : 'Manage suppliers',
      pageDescription: isVietnamese
        ? 'Theo dõi và cập nhật danh sách nhà cung cấp dùng cho nhập hàng.'
        : 'Review and update suppliers used for inventory imports.',
      totalSuppliers: isVietnamese
        ? '{count} nhà cung cấp'
        : '{count} suppliers',
      addSupplier: isVietnamese ? 'Thêm nhà cung cấp' : 'Add supplier',
      searchPlaceholder: isVietnamese
        ? 'Tìm theo tên, email hoặc số điện thoại...'
        : 'Search by name, email, or phone...',
      loadError: isVietnamese
        ? 'Không tải được danh sách nhà cung cấp'
        : 'Unable to load the supplier list',
      empty: isVietnamese
        ? 'Chưa có nhà cung cấp nào'
        : 'No suppliers found',
      showingCount: isVietnamese
        ? 'Hiển thị {count} trên {total} nhà cung cấp'
        : 'Showing {count} of {total} suppliers',
      nameColumn: isVietnamese ? 'Nhà cung cấp' : 'Supplier',
      detailTitle: isVietnamese
        ? 'Chi tiết nhà cung cấp'
        : 'Supplier details',
      editTitle: isVietnamese ? 'Sửa nhà cung cấp' : 'Edit supplier',
      deleteTitle: isVietnamese
        ? 'Xác nhận xóa nhà cung cấp'
        : 'Confirm supplier deletion',
      deleteDescription: isVietnamese
        ? 'Hành động này sẽ xóa nhà cung cấp khỏi hệ thống và không thể hoàn tác.'
        : 'This action removes the supplier from the system and cannot be undone.',
      createSuccess: isVietnamese
        ? 'Đã tạo nhà cung cấp'
        : 'Supplier created successfully',
      updateSuccess: isVietnamese
        ? 'Đã cập nhật nhà cung cấp'
        : 'Supplier updated successfully',
      deleteSuccess: isVietnamese
        ? 'Đã xóa nhà cung cấp'
        : 'Supplier deleted successfully',
      saveError: isVietnamese
        ? 'Không lưu được nhà cung cấp'
        : 'Unable to save supplier',
      deleteError: isVietnamese
        ? 'Không xóa được nhà cung cấp'
        : 'Unable to delete supplier',
      noPhone: isVietnamese ? 'Chưa có số điện thoại' : 'No phone number',
      noEmail: isVietnamese ? 'Chưa có email' : 'No email',
      noAddress: isVietnamese ? 'Chưa có địa chỉ' : 'No address',
      noNote: isVietnamese ? 'Chưa có ghi chú' : 'No note',
      phoneLabel: isVietnamese ? 'Số điện thoại' : 'Phone number',
      emailLabel: isVietnamese ? 'Email' : 'Email',
      addressLabel: isVietnamese ? 'Địa chỉ' : 'Address',
      noteLabel: isVietnamese ? 'Ghi chú' : 'Note',
    }),
    [isVietnamese],
  )

  const filteredSuppliers = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return suppliers
    }

    return suppliers.filter((supplier) =>
      [
        supplier.name,
        supplier.phone ?? '',
        supplier.email ?? '',
        supplier.address ?? '',
        supplier.note ?? '',
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [searchTerm, suppliers])

  useEffect(() => {
    void loadSuppliers()
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

  async function loadSuppliers() {
    setIsLoading(true)

    try {
      const response = await getAdminSuppliers()
      setSuppliers(response)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, labels.loadError))
    } finally {
      setIsLoading(false)
    }
  }

  function resetDialog() {
    setDialogMode(null)
    setSelectedSupplier(null)
    setForm(initialFormState)
  }

  function closeDialog() {
    if (isSubmitting || isDeleting) {
      return
    }

    resetDialog()
  }

  function openCreateDialog() {
    setSelectedSupplier(null)
    setForm(initialFormState)
    setDialogMode('create')
  }

  function openViewDialog(supplier: AdminSupplierResponse) {
    setSelectedSupplier(supplier)
    setDialogMode('view')
  }

  function openEditDialog(supplier: AdminSupplierResponse) {
    setSelectedSupplier(supplier)
    setForm({
      name: supplier.name,
      phone: supplier.phone ?? '',
      email: supplier.email ?? '',
      address: supplier.address ?? '',
      note: supplier.note ?? '',
    })
    setDialogMode('edit')
  }

  function openEditFromView() {
    if (!selectedSupplier) {
      return
    }

    openEditDialog(selectedSupplier)
  }

  function openDeleteDialog(supplier: AdminSupplierResponse) {
    setSelectedSupplier(supplier)
    setDialogMode('delete')
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    setIsSubmitting(true)

    try {
      const payload: AdminSupplierMutationRequest = {
        name: form.name.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
        address: form.address.trim(),
        note: form.note.trim(),
      }

      if (dialogMode === 'edit' && selectedSupplier) {
        await updateAdminSupplier(selectedSupplier.id, payload)
        toast.success(labels.updateSuccess)
      } else {
        await createAdminSupplier(payload)
        toast.success(labels.createSuccess)
      }

      await loadSuppliers()
      resetDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.saveError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteConfirm() {
    if (!selectedSupplier) {
      return
    }

    setIsDeleting(true)

    try {
      await deleteAdminSupplier(selectedSupplier.id)
      await loadSuppliers()
      resetDialog()
      toast.success(labels.deleteSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.deleteError))
    } finally {
      setIsDeleting(false)
    }
  }

  const isDialogLocked = dialogMode === 'delete' && isDeleting

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={isDialogLocked ? undefined : closeDialog}
        disabled={isDialogLocked}
      />
      <div className="relative z-10 w-full max-w-3xl">
        {dialogMode === 'view' && selectedSupplier ? (
          <SupplierDetailDialog
            formatDate={formatDate}
            labels={labels}
            onClose={closeDialog}
            onEdit={openEditFromView}
            supplier={selectedSupplier}
            t={t}
          />
        ) : null}

        {dialogMode === 'create' || dialogMode === 'edit' ? (
          <SupplierFormDialog
            canClose={!isSubmitting}
            form={form}
            isSubmitting={isSubmitting}
            labels={labels}
            mode={dialogMode}
            onClose={closeDialog}
            onSubmit={handleSubmit}
            onValueChange={(field, value) => {
              setForm((currentForm) => ({
                ...currentForm,
                [field]: value,
              }))
            }}
            t={t}
          />
        ) : null}

        {dialogMode === 'delete' && selectedSupplier ? (
          <SupplierDeleteDialog
            isDeleting={isDeleting}
            labels={labels}
            onClose={closeDialog}
            onConfirm={handleDeleteConfirm}
            supplier={selectedSupplier}
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
                    {labels.pageTitle}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <Truck className="mr-2 h-4 w-4" />
                    {interpolateLabel(labels.totalSuppliers, {
                      count: formatNumber(suppliers.length),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {labels.pageDescription}
                </p>
              </div>

              <Button
                size="lg"
                onClick={openCreateDialog}
                className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
              >
                <Plus className="mr-2 h-5 w-5" />
                {labels.addSupplier}
              </Button>
            </div>

            <div className="mt-8 max-w-xl">
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

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden xl:block">
                  <div className="grid overflow-hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid-cols-[minmax(0,2.8fr)_22rem]">
                    <div className="px-8 py-6">
                      <p>{labels.nameColumn}</p>
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
                ) : filteredSuppliers.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center">
                    <p className="text-base font-medium text-foreground">
                      {labels.empty}
                    </p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {filteredSuppliers.map((supplier) => {
                      const contactLine = [supplier.phone, supplier.email]
                        .filter((value): value is string => Boolean(value))
                        .join(' • ')

                      return (
                        <article
                          key={supplier.id}
                          className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2.8fr)_22rem] xl:gap-0 xl:p-0"
                        >
                          <div className="flex min-w-0 items-center gap-5 xl:px-8 xl:py-6">
                            <div className="flex h-20 w-16 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
                              <Truck className="h-8 w-8 text-primary" />
                            </div>

                            <div className="min-w-0">
                              <p className="truncate text-2xl font-semibold text-foreground">
                                {supplier.name}
                              </p>
                              {contactLine ? (
                                <p className="mt-2 truncate text-sm text-muted-foreground">
                                  {contactLine}
                                </p>
                              ) : null}
                            </div>
                          </div>

                          <div className="flex flex-wrap gap-3 xl:min-h-[128px] xl:flex-nowrap xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6">
                            <Button
                              type="button"
                              variant="outline"
                              onClick={() => openViewDialog(supplier)}
                              className="min-w-[96px] justify-center rounded-2xl bg-background/60"
                            >
                              <Eye className="mr-2 h-4 w-4" />
                              {t('common.view')}
                            </Button>
                            <Button
                              type="button"
                              variant="outline"
                              onClick={() => openEditDialog(supplier)}
                              className="min-w-[96px] justify-center rounded-2xl bg-background/60"
                            >
                              <Edit2 className="mr-2 h-4 w-4" />
                              {t('common.edit')}
                            </Button>
                            <Button
                              type="button"
                              variant="destructive"
                              onClick={() => openDeleteDialog(supplier)}
                              className="min-w-[96px] justify-center rounded-2xl"
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

              {!isLoading && !error && filteredSuppliers.length > 0 ? (
                <div className="border-t border-border/60 px-6 py-5 text-sm text-muted-foreground">
                  {interpolateLabel(labels.showingCount, {
                    count: formatNumber(filteredSuppliers.length),
                    total: formatNumber(suppliers.length),
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

function SupplierFormDialog({
  canClose,
  form,
  isSubmitting,
  labels,
  mode,
  onClose,
  onSubmit,
  onValueChange,
  t,
}: {
  canClose: boolean
  form: SupplierFormState
  isSubmitting: boolean
  labels: {
    addSupplier: string
    addressLabel: string
    editTitle: string
    emailLabel: string
    noteLabel: string
    phoneLabel: string
  }
  mode: 'create' | 'edit'
  onClose: () => void
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => Promise<void>
  onValueChange: (field: keyof SupplierFormState, value: string) => void
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <DialogShell
      title={mode === 'create' ? labels.addSupplier : labels.editTitle}
      onClose={onClose}
      canClose={canClose}
    >
      <form className="space-y-5" onSubmit={(event) => void onSubmit(event)}>
        <div className="space-y-2">
          <Label>{t('common.name')}</Label>
          <Input
            value={form.name}
            onChange={(event) => onValueChange('name', event.currentTarget.value)}
            className="h-11 rounded-2xl"
            required
          />
        </div>

        <div className="grid gap-5 md:grid-cols-2">
          <div className="space-y-2">
            <Label>{labels.phoneLabel}</Label>
            <Input
              value={form.phone}
              onChange={(event) =>
                onValueChange('phone', event.currentTarget.value)
              }
              className="h-11 rounded-2xl"
            />
          </div>
          <div className="space-y-2">
            <Label>{labels.emailLabel}</Label>
            <Input
              type="email"
              value={form.email}
              onChange={(event) =>
                onValueChange('email', event.currentTarget.value)
              }
              className="h-11 rounded-2xl"
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label>{labels.addressLabel}</Label>
          <Textarea
            value={form.address}
            onChange={(event) =>
              onValueChange('address', event.currentTarget.value)
            }
            className="min-h-24 rounded-2xl"
          />
        </div>

        <div className="space-y-2">
          <Label>{labels.noteLabel}</Label>
          <Textarea
            value={form.note}
            onChange={(event) => onValueChange('note', event.currentTarget.value)}
            className="min-h-28 rounded-2xl"
          />
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
          <Button type="submit" className="rounded-2xl" disabled={isSubmitting}>
            {isSubmitting ? t('common.processing') : t('common.save')}
          </Button>
        </div>
      </form>
    </DialogShell>
  )
}

function SupplierDetailDialog({
  formatDate,
  labels,
  onClose,
  onEdit,
  supplier,
  t,
}: {
  formatDate: (value: Date | number | string) => string
  labels: {
    addressLabel: string
    detailTitle: string
    emailLabel: string
    noAddress: string
    noEmail: string
    noNote: string
    noPhone: string
    noteLabel: string
    phoneLabel: string
  }
  onClose: () => void
  onEdit: () => void
  supplier: AdminSupplierResponse
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <DialogShell title={labels.detailTitle} onClose={onClose}>
      <div className="space-y-6">
        <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
          <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
            <div className="flex h-24 w-20 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
              <Truck className="h-9 w-9 text-primary" />
            </div>
            <div className="min-w-0">
              <p className="truncate text-3xl font-semibold text-foreground">
                {supplier.name}
              </p>
            </div>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={CalendarDays}
            label={t('common.createdAt')}
            value={formatDate(supplier.createdAt)}
          />
          <DetailCard
            icon={RefreshCw}
            label={t('common.updatedAt')}
            value={formatDate(supplier.updatedAt)}
          />
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={Phone}
            label={labels.phoneLabel}
            value={supplier.phone || labels.noPhone}
          />
          <DetailCard
            icon={Mail}
            label={labels.emailLabel}
            value={supplier.email || labels.noEmail}
          />
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={MapPin}
            label={labels.addressLabel}
            value={supplier.address || labels.noAddress}
          />
          <DetailCard
            icon={StickyNote}
            label={labels.noteLabel}
            value={supplier.note || labels.noNote}
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
          <Button type="button" onClick={onEdit} className="rounded-2xl">
            <Edit2 className="mr-2 h-4 w-4" />
            {t('common.edit')}
          </Button>
        </div>
      </div>
    </DialogShell>
  )
}

function SupplierDeleteDialog({
  isDeleting,
  labels,
  onClose,
  onConfirm,
  supplier,
  t,
}: {
  isDeleting: boolean
  labels: {
    deleteDescription: string
    deleteTitle: string
  }
  onClose: () => void
  onConfirm: () => Promise<void>
  supplier: AdminSupplierResponse
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
            {supplier.name}
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
  icon: LucideIcon
  label: string
  value: string
}) {
  return (
    <div className="rounded-[22px] border border-border/60 bg-background/55 p-4">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Icon className="h-4 w-4" />
        <span>{label}</span>
      </div>
      <p className="mt-3 whitespace-pre-wrap text-base font-semibold text-foreground">
        {value}
      </p>
    </div>
  )
}

function interpolateLabel(
  template: string,
  params: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    String(params[key] ?? `{${key}}`),
  )
}

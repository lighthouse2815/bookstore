import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  CalendarDays,
  CheckCircle2,
  Clock3,
  Edit2,
  Eye,
  Percent,
  Plus,
  Search,
  Tag,
  TicketPercent,
  Trash2,
  X,
  type LucideIcon,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { PaginationControls } from '@/components/common/pagination-controls'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { Textarea } from '@/components/common/textarea'
import {
  useAdminPromotionsPage,
  type PromotionFormErrors,
  type PromotionFormState,
} from '@/hooks/use-admin-promotions-page'
import { AdminLayout } from '@/components/layout/admin-layout'
import type { AdminPromotionResponse } from '@/types/admin-access'
import type { CouponDiscountType, CouponType } from '@/types/coupon'

export default function AdminPromotionsPage() {
  const {
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    promotions,
    page,
    pageSize,
    totalCount,
    filteredPromotions,
    searchTerm,
    isLoading,
    error,
    dialogMode,
    selectedPromotion,
    form,
    formErrors,
    isSubmitting,
    isDeleting,
    labels,
    isDialogLocked,
    handleSearchTermChange,
    handlePageChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    openEditDialog,
    openEditFromView,
    openDeleteDialog,
    handleFormChange,
    handleFieldBlur,
    handleSubmit,
    handleDeleteConfirm,
    getDeleteBlockedReason,
  } = useAdminPromotionsPage()

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={isDialogLocked ? undefined : closeDialog}
        disabled={isDialogLocked}
      />
      <div className="relative z-10 w-full max-w-4xl">
        {dialogMode === 'view' && selectedPromotion ? (
          <PromotionDetailDialog
            formatCurrency={formatCurrency}
            formatDate={formatDate}
            labels={labels}
            onClose={closeDialog}
            onEdit={openEditFromView}
            promotion={selectedPromotion}
            t={t}
          />
        ) : null}

        {dialogMode === 'create' || dialogMode === 'edit' ? (
          <PromotionFormDialog
            canClose={!isSubmitting}
            form={form}
            formErrors={formErrors}
            formatCurrency={formatCurrency}
            isSubmitting={isSubmitting}
            labels={labels}
            mode={dialogMode}
            onClose={closeDialog}
            onFieldBlur={handleFieldBlur}
            onSubmit={handleSubmit}
            onValueChange={handleFormChange}
            t={t}
          />
        ) : null}

        {dialogMode === 'delete' && selectedPromotion ? (
          <PromotionDeleteDialog
            isDeleting={isDeleting}
            labels={labels}
            onClose={closeDialog}
            onConfirm={handleDeleteConfirm}
            promotion={selectedPromotion}
            t={t}
          />
        ) : null}
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.2)] backdrop-blur xl:p-8">
          <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
            <div>
              <div className="flex flex-wrap items-center gap-3">
                <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                  {t('admin.promotionsPage.title')}
                </h1>
                <Badge
                  variant="outline"
                  className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary"
                >
                  <TicketPercent className="mr-2 h-4 w-4" />
                  {t('admin.promotionsPage.totalPromotions', {
                    count: formatNumber(totalCount),
                  })}
                </Badge>
              </div>
              <p className="mt-3 max-w-3xl text-base text-muted-foreground">
                {t('admin.promotionsPage.description')}
              </p>
            </div>

            <Button
              size="lg"
              onClick={openCreateDialog}
              className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
            >
              <Plus className="mr-2 h-5 w-5" />
              {labels.addPromotion}
            </Button>
          </div>

          <div className="mt-8 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <p className="text-sm text-muted-foreground">
              {t('admin.promotionsPage.showingCount', {
                count: formatNumber(filteredPromotions.length),
                total: formatNumber(totalCount),
              })}
            </p>
            <div className="relative w-full lg:max-w-sm">
              <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
              <Input
                value={searchTerm}
                onChange={handleSearchTermChange}
                placeholder={t('admin.promotionsPage.searchPlaceholder')}
                className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base"
              />
            </div>
          </div>

          {error && !isLoading ? (
            <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
              <p className="font-semibold">{labels.loadError}</p>
              <p className="mt-2">{error}</p>
            </div>
          ) : null}

          <div className="mt-8 overflow-x-auto rounded-[28px] border border-border/60 bg-background/20">
            {isLoading ? (
              <div className="px-6 py-10 text-center text-muted-foreground">
                {t('common.loading')}
              </div>
            ) : filteredPromotions.length === 0 ? (
              <div className="px-6 py-10 text-center text-muted-foreground">
                {t('admin.promotionsPage.empty')}
              </div>
            ) : (
              <table className="w-full min-w-[980px]">
                <thead>
                  <tr className="border-b border-border/60 bg-background/55 text-left text-sm font-semibold text-foreground">
                    <th className="px-5 py-4">
                      {t('admin.promotionsPage.columns.campaign')}
                    </th>
                    <th className="px-5 py-4">
                      {t('admin.promotionsPage.columns.discount')}
                    </th>
                    <th className="px-5 py-4">
                      {t('admin.promotionsPage.columns.usage')}
                    </th>
                    <th className="px-5 py-4">
                      {t('admin.promotionsPage.columns.schedule')}
                    </th>
                    <th className="px-5 py-4">
                      {t('admin.promotionsPage.columns.status')}
                    </th>
                    <th className="px-5 py-4 text-right">{t('common.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPromotions.map((promotion) => (
                    <PromotionTableRow
                      key={promotion.id}
                      formatCurrency={formatCurrency}
                      formatDate={formatDate}
                      formatNumber={formatNumber}
                      getDeleteBlockedReason={getDeleteBlockedReason}
                      labels={labels}
                      onEdit={openEditDialog}
                      onOpenDelete={openDeleteDialog}
                      onView={openViewDialog}
                      promotion={promotion}
                      t={t}
                    />
                  ))}
                </tbody>
              </table>
            )}
            {!isLoading && !error && totalCount > 0 ? (
              <PaginationControls
                page={page}
                size={pageSize}
                totalCount={totalCount}
                onPageChange={handlePageChange}
              />
            ) : null}
          </div>
        </div>
      </AdminLayout>

      {dialogMarkup && typeof document !== 'undefined'
        ? createPortal(dialogMarkup, document.body)
        : null}
    </>
  )
}

function PromotionFormDialog({
  canClose,
  form,
  formErrors,
  formatCurrency,
  isSubmitting,
  labels,
  mode,
  onClose,
  onFieldBlur,
  onSubmit,
  onValueChange,
  t,
}: {
  canClose: boolean
  form: PromotionFormState
  formErrors: PromotionFormErrors
  formatCurrency: (value: number) => string
  isSubmitting: boolean
  labels: {
    activeLabel: string
    addPromotion: string
    codeLabel: string
    codeHint: string
    couponTypeLabel: string
    descriptionLabel: string
    detailTitle: string
    discountTypeLabel: string
    discountValueLabel: string
    editTitle: string
    expiresAtLabel: string
    expiresAtHint: string
    formDescription: string
    maxDiscountAmountLabel: string
    maxUsageCountLabel: string
    minOrderAmountLabel: string
    percentageHint: string
    startsAtLabel: string
    startsAtHint: string
  }
  mode: 'create' | 'edit'
  onClose: () => void
  onFieldBlur: (field: keyof PromotionFormState) => void
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => Promise<void>
  onValueChange: (
    field: keyof PromotionFormState,
    value: string | boolean,
  ) => void
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <DialogShell
      title={mode === 'create' ? labels.addPromotion : labels.editTitle}
      description={labels.formDescription}
      onClose={onClose}
      canClose={canClose}
    >
      <form className="space-y-5" onSubmit={(event) => void onSubmit(event)}>
        <div className="grid gap-5 md:grid-cols-2">
          <FormField
            label={labels.codeLabel}
            description={labels.codeHint}
            error={formErrors.code}
          >
            <Input
              value={form.code}
              onChange={(event) => onValueChange('code', event.currentTarget.value)}
              onBlur={() => onFieldBlur('code')}
              className={getFieldInputClassName(Boolean(formErrors.code))}
              aria-invalid={Boolean(formErrors.code)}
              required
            />
          </FormField>

          <FormField label={labels.couponTypeLabel}>
            <Select
              value={form.couponType}
              onValueChange={(nextValue) =>
                onValueChange('couponType', (nextValue as CouponType | null) ?? 'BOOK')
              }
            >
              <SelectTrigger className="h-11 w-full rounded-2xl px-4">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="BOOK">{t('checkout.bookCoupons')}</SelectItem>
                <SelectItem value="SHIPPING">
                  {t('checkout.shippingCoupons')}
                </SelectItem>
              </SelectContent>
            </Select>
          </FormField>

          <FormField label={labels.discountTypeLabel}>
            <Select
              value={form.discountType}
              onValueChange={(nextValue) =>
                onValueChange(
                  'discountType',
                  (nextValue as CouponDiscountType | null) ?? 'PERCENTAGE',
                )
              }
            >
              <SelectTrigger className="h-11 w-full rounded-2xl px-4">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="PERCENTAGE">
                  {t('admin.promotionsPage.percentType')}
                </SelectItem>
                <SelectItem value="FIXED_AMOUNT">
                  {t('admin.promotionsPage.fixedType')}
                </SelectItem>
              </SelectContent>
            </Select>
          </FormField>

          <FormField
            label={labels.discountValueLabel}
            description={
              form.discountType === 'PERCENTAGE' ? labels.percentageHint : undefined
            }
            error={formErrors.discountValue}
          >
            <Input
              type="number"
              min="0.01"
              max={form.discountType === 'PERCENTAGE' ? '100' : undefined}
              step="0.01"
              value={form.discountValue}
              onChange={(event) =>
                onValueChange('discountValue', event.currentTarget.value)
              }
              onBlur={() => onFieldBlur('discountValue')}
              className={getFieldInputClassName(Boolean(formErrors.discountValue))}
              aria-invalid={Boolean(formErrors.discountValue)}
              required
            />
          </FormField>

          <FormField label={labels.minOrderAmountLabel} error={formErrors.minOrderAmount}>
            <Input
              type="number"
              min="0"
              step="0.01"
              value={form.minOrderAmount}
              onChange={(event) =>
                onValueChange('minOrderAmount', event.currentTarget.value)
              }
              onBlur={() => onFieldBlur('minOrderAmount')}
              className={getFieldInputClassName(Boolean(formErrors.minOrderAmount))}
              aria-invalid={Boolean(formErrors.minOrderAmount)}
              required
            />
          </FormField>

          <FormField
            label={labels.maxDiscountAmountLabel}
            error={formErrors.maxDiscountAmount}
          >
            <Input
              type="number"
              min="0.01"
              step="0.01"
              value={form.maxDiscountAmount}
              onChange={(event) =>
                onValueChange('maxDiscountAmount', event.currentTarget.value)
              }
              placeholder={formatCurrency(0)}
              onBlur={() => onFieldBlur('maxDiscountAmount')}
              className={getFieldInputClassName(Boolean(formErrors.maxDiscountAmount))}
              aria-invalid={Boolean(formErrors.maxDiscountAmount)}
            />
          </FormField>

          <FormField
            label={labels.maxUsageCountLabel}
            error={formErrors.maxUsageCount}
          >
            <Input
              type="number"
              min="1"
              step="1"
              value={form.maxUsageCount}
              onChange={(event) =>
                onValueChange('maxUsageCount', event.currentTarget.value)
              }
              onBlur={() => onFieldBlur('maxUsageCount')}
              className={getFieldInputClassName(Boolean(formErrors.maxUsageCount))}
              aria-invalid={Boolean(formErrors.maxUsageCount)}
            />
          </FormField>

          <FormField
            label={labels.startsAtLabel}
            description={labels.startsAtHint}
            error={formErrors.startsAt}
          >
            <Input
              type="datetime-local"
              step="60"
              value={form.startsAt}
              onChange={(event) =>
                onValueChange('startsAt', event.currentTarget.value)
              }
              onBlur={() => onFieldBlur('startsAt')}
              className={getFieldInputClassName(Boolean(formErrors.startsAt))}
              aria-invalid={Boolean(formErrors.startsAt)}
              required
            />
          </FormField>

          <FormField
            label={labels.expiresAtLabel}
            description={labels.expiresAtHint}
            error={formErrors.expiresAt}
          >
            <Input
              type="datetime-local"
              min={form.startsAt}
              step="60"
              value={form.expiresAt}
              onChange={(event) =>
                onValueChange('expiresAt', event.currentTarget.value)
              }
              onBlur={() => onFieldBlur('expiresAt')}
              className={getFieldInputClassName(Boolean(formErrors.expiresAt))}
              aria-invalid={Boolean(formErrors.expiresAt)}
              required
            />
          </FormField>
        </div>

        <FormField label={labels.descriptionLabel}>
          <Textarea
            value={form.description}
            onChange={(event) =>
              onValueChange('description', event.currentTarget.value)
            }
            className="min-h-24 rounded-2xl"
          />
        </FormField>

        <label className="flex items-center gap-3 rounded-2xl border border-border/60 bg-background/50 px-4 py-3 text-sm font-medium text-foreground">
          <input
            type="checkbox"
            checked={form.active}
            onChange={(event) => onValueChange('active', event.currentTarget.checked)}
            className="h-4 w-4 accent-current"
          />
          {labels.activeLabel}
        </label>

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

function PromotionDetailDialog({
  formatCurrency,
  formatDate,
  labels,
  onClose,
  onEdit,
  promotion,
  t,
}: {
  formatCurrency: (value: number) => string
  formatDate: (value: Date | number | string) => string
  labels: {
    activeLabel: string
    codeLabel: string
    couponTypeLabel: string
    descriptionLabel: string
    detailTitle: string
    discountTypeLabel: string
    discountValueLabel: string
    expiresAtLabel: string
    maxDiscountAmountLabel: string
    maxUsageCountLabel: string
    minOrderAmountLabel: string
    noDescription: string
    noLimit: string
    noMaxDiscount: string
    startsAtLabel: string
  }
  onClose: () => void
  onEdit: () => void
  promotion: AdminPromotionResponse
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <DialogShell title={labels.detailTitle} onClose={onClose}>
      <div className="space-y-6">
        <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="flex flex-wrap items-center gap-3">
                <p className="text-3xl font-semibold text-foreground">
                  {promotion.code}
                </p>
                <Badge variant={getPromotionStatusVariant(promotion)}>
                  {getPromotionStatusLabel(promotion, t)}
                </Badge>
              </div>
              <p className="mt-3 text-sm leading-6 text-muted-foreground">
                {promotion.description || labels.noDescription}
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Badge variant="secondary">
                {getPromotionCouponTypeLabel(promotion, t)}
              </Badge>
              <Badge variant="outline">
                {getPromotionTypeLabel(promotion.discountType, t)}
              </Badge>
            </div>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={Tag}
            label={labels.codeLabel}
            value={promotion.code}
          />
          <DetailCard
            icon={TicketPercent}
            label={labels.couponTypeLabel}
            value={getPromotionCouponTypeLabel(promotion, t)}
          />
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={Percent}
            label={labels.discountValueLabel}
            value={formatPromotionValue(
              promotion.discountType,
              promotion.discountValue,
              formatCurrency,
            )}
          />
          <DetailCard
            icon={Percent}
            label={labels.discountTypeLabel}
            value={getPromotionTypeLabel(promotion.discountType, t)}
          />
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={CalendarDays}
            label={labels.startsAtLabel}
            value={formatDate(promotion.startsAt)}
          />
          <DetailCard
            icon={Clock3}
            label={labels.expiresAtLabel}
            value={formatDate(promotion.expiresAt)}
          />
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <DetailCard
            icon={CheckCircle2}
            label={labels.activeLabel}
            value={promotion.active ? t('admin.promotionsPage.statuses.active') : t('admin.promotionsPage.statuses.inactive')}
          />
          <DetailCard
            icon={Percent}
            label={labels.descriptionLabel}
            value={promotion.description || labels.noDescription}
          />
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <DetailCard
            icon={Tag}
            label={labels.minOrderAmountLabel}
            value={formatCurrency(promotion.minOrderAmount)}
          />
          <DetailCard
            icon={Tag}
            label={labels.maxDiscountAmountLabel}
            value={
              promotion.maxDiscountAmount === null
                ? labels.noMaxDiscount
                : formatCurrency(promotion.maxDiscountAmount)
            }
          />
          <DetailCard
            icon={Tag}
            label={labels.maxUsageCountLabel}
            value={
              promotion.maxUsageCount === null
                ? labels.noLimit
                : String(promotion.maxUsageCount)
            }
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

function PromotionDeleteDialog({
  isDeleting,
  labels,
  onClose,
  onConfirm,
  promotion,
  t,
}: {
  isDeleting: boolean
  labels: {
    deleteDescription: string
    deleteTitle: string
  }
  onClose: () => void
  onConfirm: () => Promise<void>
  promotion: AdminPromotionResponse
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
            {promotion.code}
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
  description,
  onClose,
  title,
}: {
  canClose?: boolean
  children: React.ReactNode
  description?: string
  onClose: () => void
  title: string
}) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
          {description ? (
            <p className="mt-2 text-sm text-muted-foreground">{description}</p>
          ) : null}
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

function FormField({
  children,
  description,
  error,
  label,
}: {
  children: React.ReactNode
  description?: string
  error?: string
  label: string
}) {
  return (
    <div className="space-y-2">
      <Label>{label}</Label>
      {children}
      {error ? (
        <p className="text-xs font-medium text-destructive">{error}</p>
      ) : description ? (
        <p className="text-xs text-muted-foreground">{description}</p>
      ) : null}
    </div>
  )
}

function PromotionTableRow({
  formatCurrency,
  formatDate,
  formatNumber,
  getDeleteBlockedReason,
  labels,
  onEdit,
  onOpenDelete,
  onView,
  promotion,
  t,
}: {
  formatCurrency: (value: number) => string
  formatDate: (value: Date | number | string) => string
  formatNumber: (value: number) => string
  getDeleteBlockedReason: (promotion: AdminPromotionResponse) => string | null
  labels: {
    deleteBlockedShort: string
    maxDiscountAmountLabel: string
    maxUsageCountLabel: string
    minOrderAmountLabel: string
    noLimit: string
    noMaxDiscount: string
  }
  onEdit: (promotion: AdminPromotionResponse) => void
  onOpenDelete: (promotion: AdminPromotionResponse) => void
  onView: (promotion: AdminPromotionResponse) => void
  promotion: AdminPromotionResponse
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  const deleteBlockedReason = getDeleteBlockedReason(promotion)

  return (
    <tr className="border-b border-border/50 align-top last:border-b-0">
      <td className="px-5 py-5 text-sm">
        <div className="flex flex-wrap items-center gap-2">
          <p className="font-semibold text-foreground">{promotion.code}</p>
          <Badge variant="secondary">
            {getPromotionCouponTypeLabel(promotion, t)}
          </Badge>
        </div>
        <p className="mt-2 max-w-[320px] text-xs leading-6 text-muted-foreground">
          {promotion.description || t('admin.promotionsPage.noDescription')}
        </p>
      </td>
      <td className="px-5 py-5 text-sm">
        <Badge variant="outline">
          {getPromotionTypeLabel(promotion.discountType, t)}
        </Badge>
        <p className="mt-2 font-semibold text-foreground">
          {formatPromotionValue(
            promotion.discountType,
            promotion.discountValue,
            formatCurrency,
          )}
        </p>
        <div className="mt-2 space-y-1 text-xs text-muted-foreground">
          <p>
            {labels.minOrderAmountLabel}: {formatCurrency(promotion.minOrderAmount)}
          </p>
          <p>
            {labels.maxDiscountAmountLabel}:{' '}
            {promotion.maxDiscountAmount === null
              ? labels.noMaxDiscount
              : formatCurrency(promotion.maxDiscountAmount)}
          </p>
        </div>
      </td>
      <td className="px-5 py-5 text-sm text-foreground">
        <p className="font-semibold">{formatNumber(promotion.usedCount)}</p>
        <p className="mt-2 text-xs text-muted-foreground">
          {promotion.maxUsageCount === null
            ? labels.noLimit
            : `${formatNumber(promotion.maxUsageCount)} ${labels.maxUsageCountLabel.toLowerCase()}`}
        </p>
        {deleteBlockedReason ? (
          <p className="mt-2 text-xs font-medium text-amber-700">
            {labels.deleteBlockedShort}
          </p>
        ) : null}
      </td>
      <td className="px-5 py-5 text-sm text-muted-foreground">
        <p>{formatDate(promotion.startsAt)}</p>
        <p className="mt-2">{formatDate(promotion.expiresAt)}</p>
      </td>
      <td className="px-5 py-5 text-sm">
        <Badge variant={getPromotionStatusVariant(promotion)}>
          {getPromotionStatusLabel(promotion, t)}
        </Badge>
      </td>
      <td className="px-5 py-5">
        <div className="flex flex-col items-end gap-2">
          <div className="flex justify-end gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onView(promotion)}
              className="rounded-2xl"
            >
              <Eye className="mr-1 h-4 w-4" />
              {t('common.view')}
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onEdit(promotion)}
              className="rounded-2xl"
            >
              <Edit2 className="mr-1 h-4 w-4" />
              {t('common.edit')}
            </Button>
            <Button
              type="button"
              variant="destructive"
              size="sm"
              onClick={() => onOpenDelete(promotion)}
              className="rounded-2xl"
              disabled={Boolean(deleteBlockedReason)}
              title={deleteBlockedReason ?? undefined}
            >
              <Trash2 className="mr-1 h-4 w-4" />
              {t('common.delete')}
            </Button>
          </div>
        </div>
      </td>
    </tr>
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

function getPromotionCouponTypeLabel(
  promotion: AdminPromotionResponse,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return promotion.couponType === 'BOOK'
    ? t('checkout.bookCoupons')
    : t('checkout.shippingCoupons')
}

function getPromotionTypeLabel(
  discountType: CouponDiscountType,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return discountType === 'PERCENTAGE'
    ? t('admin.promotionsPage.percentType')
    : t('admin.promotionsPage.fixedType')
}

function formatPromotionValue(
  discountType: CouponDiscountType,
  discountValue: number,
  formatCurrency: (value: number) => string,
) {
  return discountType === 'PERCENTAGE'
    ? `${discountValue}%`
    : formatCurrency(discountValue)
}

function getFieldInputClassName(hasError: boolean) {
  return `h-11 rounded-2xl${
    hasError ? ' border-destructive focus-visible:ring-destructive/30' : ''
  }`
}

function getPromotionStatusVariant(promotion: AdminPromotionResponse) {
  const currentTime = Date.now()
  const startAt = new Date(promotion.startsAt).getTime()
  const endAt = new Date(promotion.expiresAt).getTime()

  if (!promotion.active) {
    return 'secondary' as const
  }

  if (startAt > currentTime) {
    return 'outline' as const
  }

  if (endAt < currentTime) {
    return 'destructive' as const
  }

  return 'default' as const
}

function getPromotionStatusLabel(
  promotion: AdminPromotionResponse,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  const currentTime = Date.now()
  const startAt = new Date(promotion.startsAt).getTime()
  const endAt = new Date(promotion.expiresAt).getTime()

  if (!promotion.active) {
    return t('admin.promotionsPage.statuses.inactive')
  }

  if (startAt > currentTime) {
    return t('admin.promotionsPage.statuses.upcoming')
  }

  if (endAt < currentTime) {
    return t('admin.promotionsPage.statuses.expired')
  }

  return t('admin.promotionsPage.statuses.active')
}

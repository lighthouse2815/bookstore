import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  Building2,
  CalendarDays,
  Edit2,
  Eye,
  PenTool,
  Plus,
  RefreshCw,
  Search,
  Tags,
  Trash2,
  X,
  type LucideIcon,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { PaginationControls } from '@/components/common/pagination-controls'
import { Textarea } from '@/components/common/textarea'
import {
  getReferenceDescription,
  getReferenceImageUrl,
  useAdminReferenceManagementPage,
  type ReferenceItem,
  type ReferenceFormState,
  type ReferenceSectionKey,
} from '@/hooks/use-admin-reference-management-page'
import { AdminLayout } from '@/components/layout/admin-layout'
import { getBookCoverUrl } from '@/utils/book-cover'
import { cn } from '@/utils'
import { getCategoryDescription, getCategoryLabel } from '@/utils/i18n'
import type { AppLanguage } from '@/locales/messages'

type SectionVisual = {
  icon: LucideIcon
  badgeClassName: string
  tileClassName: string
  tileIconClassName: string
}

const sectionLabelKeys: Record<ReferenceSectionKey, string> = {
  categories: 'admin.referencePages.categories.section',
  authors: 'admin.referencePages.authors.section',
  publishers: 'admin.referencePages.publishers.section',
}

const addLabelKeys: Record<ReferenceSectionKey, string> = {
  categories: 'admin.referencePages.categories.add',
  authors: 'admin.referencePages.authors.add',
  publishers: 'admin.referencePages.publishers.add',
}

const emptyLabelKeys: Record<ReferenceSectionKey, string> = {
  categories: 'admin.referencePages.categories.empty',
  authors: 'admin.referencePages.authors.empty',
  publishers: 'admin.referencePages.publishers.empty',
}

const countLabelKeys: Record<ReferenceSectionKey, string> = {
  categories: 'admin.referencePages.categories.countLabel',
  authors: 'admin.referencePages.authors.countLabel',
  publishers: 'admin.referencePages.publishers.countLabel',
}

const searchPlaceholderKeys: Record<ReferenceSectionKey, string> = {
  categories: 'admin.referencePages.categories.searchPlaceholder',
  authors: 'admin.referencePages.authors.searchPlaceholder',
  publishers: 'admin.referencePages.publishers.searchPlaceholder',
}

const detailTitleKeys: Record<ReferenceSectionKey, string> = {
  categories: 'admin.referencePages.categories.detailTitle',
  authors: 'admin.referencePages.authors.detailTitle',
  publishers: 'admin.referencePages.publishers.detailTitle',
}

const editTitleKeys: Record<ReferenceSectionKey, string> = {
  categories: 'admin.referencePages.categories.editTitle',
  authors: 'admin.referencePages.authors.editTitle',
  publishers: 'admin.referencePages.publishers.editTitle',
}

const emptyDescriptionKeys: Record<ReferenceSectionKey, string> = {
  categories: 'admin.referencePages.categories.emptyDescription',
  authors: 'admin.referencePages.authors.emptyDescription',
  publishers: 'admin.referencePages.publishers.emptyDescription',
}

const sectionVisuals: Record<ReferenceSectionKey, SectionVisual> = {
  categories: {
    icon: Tags,
    badgeClassName:
      'border-primary/20 bg-primary/12 text-primary dark:border-primary/30',
    tileClassName:
      'border-primary/20 bg-linear-to-br from-primary/20 via-primary/10 to-transparent',
    tileIconClassName: 'text-primary',
  },
  authors: {
    icon: PenTool,
    badgeClassName:
      'border-sky-400/20 bg-sky-400/10 text-sky-300 dark:border-sky-400/30',
    tileClassName:
      'border-sky-400/20 bg-linear-to-br from-sky-400/20 via-sky-400/10 to-transparent',
    tileIconClassName: 'text-sky-300',
  },
  publishers: {
    icon: Building2,
    badgeClassName:
      'border-emerald-400/20 bg-emerald-400/10 text-emerald-300 dark:border-emerald-400/30',
    tileClassName:
      'border-emerald-400/20 bg-linear-to-br from-emerald-400/20 via-emerald-400/10 to-transparent',
    tileIconClassName: 'text-emerald-300',
  },
}

export function AdminReferenceManagementPage({
  sectionKey,
}: {
  sectionKey: ReferenceSectionKey
}) {
  const {
    t,
    language,
    formatDate,
    formatNumber,
    isLoading,
    error,
    form,
    searchTerm,
    dialogMode,
    selectedItem,
    isSubmitting,
    isDeleting,
    filteredItems,
    paginatedItems,
    totalCount,
    page,
    pageSize,
    isDialogLocked,
    isUploadingReferenceImage,
    handleSearchTermChange,
    handleFormChange,
    handleReferenceImageFileChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    openEditDialog,
    openEditFromDetail,
    openDeleteDialog,
    handleSubmit,
    handleDeleteConfirm,
    handlePageChange,
  } = useAdminReferenceManagementPage(sectionKey)

  const sectionLabel = t(sectionLabelKeys[sectionKey])
  const descriptionLabel =
    sectionKey === 'authors'
      ? t('admin.references.biography')
      : t('common.description')
  const visual = sectionVisuals[sectionKey]
  const ItemIcon = visual.icon
  const imageLabel = t(
    sectionKey === 'authors'
      ? 'auth.profile.avatarUrl'
      : sectionKey === 'categories'
        ? 'admin.references.categoryImage'
        : 'admin.references.publisherLogo',
  )
  const imagePreviewUrl =
    sectionKey === 'authors'
      ? form.avatarPreviewUrl
      : form.referenceImagePreviewUrl

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/80 backdrop-blur-md"
        onClick={isDialogLocked ? undefined : closeDialog}
        disabled={isDialogLocked}
      />

      <div className="relative z-10 w-full max-w-2xl rounded-[32px] border border-border/70 bg-card/95 p-6 shadow-[0_40px_120px_rgba(2,6,23,0.55)] backdrop-blur xl:p-7">
        <button
          type="button"
          onClick={isDialogLocked ? undefined : closeDialog}
          disabled={isDialogLocked}
          className="absolute right-5 top-5 inline-flex size-10 items-center justify-center rounded-full border border-border/70 bg-background/50 text-muted-foreground transition-colors hover:bg-background hover:text-foreground"
        >
          <X className="h-4 w-4" />
        </button>

        {dialogMode === 'view' && selectedItem ? (
          <ReferenceDetailDialogContent
            item={selectedItem}
            sectionKey={sectionKey}
            sectionLabel={sectionLabel}
            descriptionLabel={descriptionLabel}
            formatDate={formatDate}
            onClose={closeDialog}
            onEdit={openEditFromDetail}
            t={t}
            language={language}
          />
        ) : dialogMode === 'delete' && selectedItem ? (
          <ReferenceDeleteDialogContent
            item={selectedItem}
            sectionKey={sectionKey}
            sectionLabel={sectionLabel}
            isDeleting={isDeleting}
            onClose={closeDialog}
            onConfirm={handleDeleteConfirm}
            t={t}
            language={language}
          />
        ) : (
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="pr-12">
              <div className="flex items-center gap-4">
                <div
                  className={cn(
                    'flex size-16 items-center justify-center rounded-[20px] border shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]',
                    visual.tileClassName,
                  )}
                >
                  <ItemIcon className={cn('h-7 w-7', visual.tileIconClassName)} />
                </div>
                <div>
                  <p className="text-sm font-medium text-primary">
                    {sectionLabel}
                  </p>
                  <h2 className="mt-1 font-heading text-2xl font-bold text-foreground">
                    {dialogMode === 'create'
                      ? t(addLabelKeys[sectionKey])
                      : t(editTitleKeys[sectionKey])}
                  </h2>
                </div>
              </div>
              {dialogMode === 'create' ? (
                <p className="mt-4 text-sm text-muted-foreground">
                  {t(`admin.referencePages.${sectionKey}.description`)}
                </p>
              ) : null}
            </div>

            <div className="space-y-5">
              {sectionKey === 'categories' ? (
                <CategoryLocalizationFields
                  form={form}
                  onChange={handleFormChange}
                  t={t}
                />
              ) : (
                <div>
                  <Label htmlFor={`${sectionKey}-name`}>{t('common.name')}</Label>
                  <Input
                    id={`${sectionKey}-name`}
                    value={form.name}
                    onChange={(event) =>
                      handleFormChange('name', event.currentTarget.value)
                    }
                    className="mt-2 h-12 rounded-2xl bg-background/60"
                    required
                  />
                </div>
              )}

              <div>
                <Label htmlFor={`${sectionKey}-image-upload`}>{imageLabel}</Label>
                <div className="mt-3 flex flex-col gap-4 sm:flex-row sm:items-center">
                  <div
                    className={cn(
                      'flex size-20 items-center justify-center overflow-hidden border border-border/60 bg-background/70 text-lg font-semibold text-primary',
                      sectionKey === 'authors' ? 'rounded-full' : 'rounded-[20px]',
                    )}
                  >
                    {imagePreviewUrl ? (
                      <img
                        src={getBookCoverUrl(imagePreviewUrl)}
                        alt={form.name || form.categoryNameVi || t('common.name')}
                        className="size-full object-cover"
                      />
                    ) : (
                      <ItemIcon className={cn('h-7 w-7', visual.tileIconClassName)} />
                    )}
                  </div>
                  <div className="flex-1">
                    <Input
                      id={`${sectionKey}-image-upload`}
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      onChange={(event) =>
                        void handleReferenceImageFileChange(
                          event.currentTarget.files?.[0] ?? null,
                        )
                      }
                      className="h-12 rounded-2xl bg-background/60"
                    />
                    <p className="mt-2 text-xs text-muted-foreground">
                      {isUploadingReferenceImage
                        ? t('common.processing')
                        : t('admin.references.imageUploadHint')}
                    </p>
                  </div>
                </div>
              </div>

              {sectionKey === 'authors' ? (
                <>
                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <Label htmlFor="author-birth-year">
                        {t('admin.referencePages.authors.birthYear')}
                      </Label>
                      <Input
                        id="author-birth-year"
                        type="number"
                        min="1"
                        value={form.birthYear}
                        onChange={(event) =>
                          handleFormChange('birthYear', event.currentTarget.value)
                        }
                        className="mt-2 h-12 rounded-2xl bg-background/60"
                      />
                    </div>
                    <div>
                      <Label htmlFor="author-death-year">
                        {t('admin.referencePages.authors.deathYear')}
                      </Label>
                      <Input
                        id="author-death-year"
                        type="number"
                        min="1"
                        value={form.deathYear}
                        onChange={(event) =>
                          handleFormChange('deathYear', event.currentTarget.value)
                        }
                        className="mt-2 h-12 rounded-2xl bg-background/60"
                      />
                    </div>
                  </div>
                </>
              ) : null}

              {sectionKey !== 'categories' ? <div>
                <Label htmlFor={`${sectionKey}-description`}>
                  {descriptionLabel}
                </Label>
                <Textarea
                  id={`${sectionKey}-description`}
                  value={form.description}
                  onChange={(event) =>
                    handleFormChange('description', event.currentTarget.value)
                  }
                  className="mt-2 min-h-32 rounded-2xl bg-background/60"
                  rows={5}
                />
              </div> : null}
            </div>

            <div className="flex flex-wrap justify-end gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={closeDialog}
                className="rounded-2xl"
              >
                {t('common.cancel')}
              </Button>
              <Button
                type="submit"
                disabled={isSubmitting}
                className="rounded-2xl"
              >
                {isSubmitting ? t('common.processing') : t('common.save')}
              </Button>
            </div>
          </form>
        )}
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
                    {t(`admin.referencePages.${sectionKey}.title`)}
                  </h1>
                  <Badge
                    variant="outline"
                    className={cn(
                      'rounded-2xl px-4 py-1.5 text-sm font-semibold',
                      visual.badgeClassName,
                    )}
                  >
                    {t(countLabelKeys[sectionKey], {
                      count: formatNumber(totalCount),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {t(`admin.referencePages.${sectionKey}.description`)}
                </p>
              </div>

              <Button
                size="lg"
                onClick={openCreateDialog}
                className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
              >
                <Plus className="mr-2 h-5 w-5" />
                {t(addLabelKeys[sectionKey])}
              </Button>
            </div>

            <div className="mt-8 max-w-xl">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={searchTerm}
                  onChange={handleSearchTermChange}
                  placeholder={t(searchPlaceholderKeys[sectionKey])}
                  className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base shadow-[inset_0_1px_0_rgba(255,255,255,0.04)]"
                />
              </div>
            </div>

            {error && !isLoading && (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            )}

            <section className="mt-8 rounded-[28px] border border-border/60 bg-background/20 p-5 backdrop-blur">
              <div className="grid grid-cols-[minmax(0,1fr)_auto] items-center border-b border-border/60 px-4 pb-4 text-sm font-semibold text-muted-foreground">
                <p>{sectionLabel}</p>
                <p>{t('common.actions')}</p>
              </div>

              <div className="mt-4 space-y-4">
                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredItems.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center">
                    <p className="text-base font-medium text-foreground">
                      {t(emptyLabelKeys[sectionKey])}
                    </p>
                    <p className="mt-2 text-sm text-muted-foreground">
                      {t(emptyDescriptionKeys[sectionKey])}
                    </p>
                  </div>
                ) : (
                  paginatedItems.map((item) => {
                    const imageUrl = getReferenceImageUrl(sectionKey, item)
                    return (
                      <div
                        key={item.id}
                        className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:flex-row xl:items-center xl:justify-between"
                      >
                        <div className="flex min-w-0 items-center gap-4">
                          <div
                            className={cn(
                              'flex size-18 shrink-0 items-center justify-center overflow-hidden rounded-[22px] border shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]',
                              visual.tileClassName,
                            )}
                          >
                            {imageUrl ? (
                              <img
                                src={getBookCoverUrl(imageUrl)}
                                alt={getReferenceDisplayName(sectionKey, item, language)}
                                className="size-full object-cover"
                              />
                            ) : (
                              <ItemIcon
                                className={cn('h-8 w-8', visual.tileIconClassName)}
                              />
                            )}
                          </div>

                          <div className="min-w-0">
                            <p className="truncate text-2xl font-semibold text-foreground">
                              {getReferenceDisplayName(sectionKey, item, language)}
                            </p>
                          </div>
                        </div>

                        <div className="flex flex-wrap gap-3 xl:justify-end">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => openViewDialog(item)}
                            className="min-w-[110px] justify-center rounded-2xl bg-background/60"
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            {t('common.view')}
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => openEditDialog(item)}
                            className="min-w-[110px] justify-center rounded-2xl bg-background/60"
                          >
                            <Edit2 className="mr-2 h-4 w-4" />
                            {t('common.edit')}
                          </Button>
                          <Button
                            type="button"
                            variant="destructive"
                            onClick={() => openDeleteDialog(item)}
                            className="min-w-[110px] justify-center rounded-2xl"
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            {t('common.delete')}
                          </Button>
                        </div>
                      </div>
                    )
                  })
                )}
              </div>

              {!isLoading && filteredItems.length > 0 ? (
                <PaginationControls
                  page={page}
                  size={pageSize}
                  totalCount={totalCount}
                  onPageChange={handlePageChange}
                />
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

function CategoryLocalizationFields({
  form,
  onChange,
  t,
}: {
  form: ReferenceFormState
  onChange: (field: keyof ReferenceFormState, value: string) => void
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <div className="space-y-5">
      <div>
        <Label htmlFor="category-code">
          {t('admin.referencePages.categories.code')}
        </Label>
        <Input
          id="category-code"
          value={form.categoryCode}
          onChange={(event) =>
            onChange(
              'categoryCode',
              event.currentTarget.value.toUpperCase().replace(/[^A-Z0-9_]/g, ''),
            )
          }
          className="mt-2 h-12 rounded-2xl bg-background/60 font-mono uppercase"
          placeholder="LITERATURE"
          required
        />
      </div>

      <div className="grid gap-5 lg:grid-cols-2">
        <fieldset className="space-y-4 rounded-[24px] border border-border/60 bg-background/40 p-5">
          <legend className="px-2 text-sm font-semibold text-primary">
            {t('admin.referencePages.categories.vietnamese')}
          </legend>
          <div>
            <Label htmlFor="category-name-vi">
              {t('admin.referencePages.categories.localizedName')}
            </Label>
            <Input
              id="category-name-vi"
              value={form.categoryNameVi}
              onChange={(event) => onChange('categoryNameVi', event.currentTarget.value)}
              className="mt-2 h-12 rounded-2xl bg-background/60"
              required
            />
          </div>
          <div>
            <Label htmlFor="category-description-vi">
              {t('admin.referencePages.categories.localizedDescription')}
            </Label>
            <Textarea
              id="category-description-vi"
              value={form.categoryDescriptionVi}
              onChange={(event) =>
                onChange('categoryDescriptionVi', event.currentTarget.value)
              }
              className="mt-2 min-h-28 rounded-2xl bg-background/60"
            />
          </div>
        </fieldset>

        <fieldset className="space-y-4 rounded-[24px] border border-border/60 bg-background/40 p-5">
          <legend className="px-2 text-sm font-semibold text-primary">
            {t('admin.referencePages.categories.english')}
          </legend>
          <div>
            <Label htmlFor="category-name-en">
              {t('admin.referencePages.categories.localizedName')}
            </Label>
            <Input
              id="category-name-en"
              value={form.categoryNameEn}
              onChange={(event) => onChange('categoryNameEn', event.currentTarget.value)}
              className="mt-2 h-12 rounded-2xl bg-background/60"
              required
            />
          </div>
          <div>
            <Label htmlFor="category-description-en">
              {t('admin.referencePages.categories.localizedDescription')}
            </Label>
            <Textarea
              id="category-description-en"
              value={form.categoryDescriptionEn}
              onChange={(event) =>
                onChange('categoryDescriptionEn', event.currentTarget.value)
              }
              className="mt-2 min-h-28 rounded-2xl bg-background/60"
            />
          </div>
        </fieldset>
      </div>
    </div>
  )
}

type ReferenceDetailDialogContentProps = {
  item: ReferenceItem
  sectionKey: ReferenceSectionKey
  sectionLabel: string
  descriptionLabel: string
  formatDate: (value: string | number | Date) => string
  onClose: () => void
  onEdit: () => void
  t: (key: string, params?: Record<string, number | string>) => string
  language: AppLanguage
}

function ReferenceDetailDialogContent({
  item,
  sectionKey,
  sectionLabel,
  descriptionLabel,
  formatDate,
  onClose,
  onEdit,
  t,
  language,
}: ReferenceDetailDialogContentProps) {
  const visual = sectionVisuals[sectionKey]
  const ItemIcon = visual.icon
  const description =
    sectionKey === 'categories' && 'translations' in item
      ? getCategoryDescription(item, language) ?? ''
      : getReferenceDescription(sectionKey, item)
  const displayName = getReferenceDisplayName(sectionKey, item, language)
  const authorAvatarUrl =
    sectionKey === 'authors' && 'avatarUrl' in item ? item.avatarUrl : null
  const authorBirthYear =
    sectionKey === 'authors' && 'birthYear' in item ? item.birthYear : null
  const authorDeathYear =
    sectionKey === 'authors' && 'deathYear' in item ? item.deathYear : null

  return (
    <div className="space-y-6">
      <div className="px-12">
        <h2 className="text-center font-heading text-2xl font-bold text-foreground">
          {t(detailTitleKeys[sectionKey])}
        </h2>

        <div className="mt-5 flex items-center gap-4">
          <div
            className={cn(
              'flex size-18 items-center justify-center rounded-[22px] border shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]',
              visual.tileClassName,
            )}
          >
            <ItemIcon className={cn('h-8 w-8', visual.tileIconClassName)} />
          </div>
          <div>
            <p className="text-sm font-medium text-primary">{sectionLabel}</p>
            <p className="mt-1 text-2xl font-semibold text-foreground">
              {displayName}
            </p>
          </div>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <DetailMetaCard
          icon={CalendarDays}
          label={t('common.createdAt')}
          value={formatDate(item.createdAt)}
        />
        <DetailMetaCard
          icon={RefreshCw}
          label={t('common.updatedAt')}
          value={formatDate(item.updatedAt)}
        />
      </div>

      {sectionKey === 'authors' ? (
        <div className="grid gap-4 sm:grid-cols-[120px_minmax(0,1fr)]">
          <div className="overflow-hidden rounded-[24px] border border-border/60 bg-background/55">
            {authorAvatarUrl ? (
              <img
                src={getBookCoverUrl(authorAvatarUrl)}
                alt={item.name}
                className="aspect-square w-full object-cover"
              />
            ) : (
              <div className="flex aspect-square items-center justify-center text-muted-foreground">
                <ItemIcon className={cn('h-8 w-8', visual.tileIconClassName)} />
              </div>
            )}
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <DetailMetaCard
              icon={CalendarDays}
              label={t('admin.referencePages.authors.birthYear')}
              value={authorBirthYear ? String(authorBirthYear) : '...'}
            />
            <DetailMetaCard
              icon={CalendarDays}
              label={t('admin.referencePages.authors.deathYear')}
              value={authorDeathYear ? String(authorDeathYear) : '...'}
            />
          </div>
        </div>
      ) : null}

      <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
        <p className="text-sm font-semibold text-foreground">{descriptionLabel}</p>
        <p className="mt-3 whitespace-pre-wrap text-sm leading-7 text-muted-foreground">
          {description || t(emptyDescriptionKeys[sectionKey])}
        </p>
      </div>

      <div className="flex flex-wrap justify-end gap-3">
        <Button type="button" variant="outline" onClick={onClose} className="rounded-2xl">
          {t('common.close')}
        </Button>
        <Button type="button" onClick={onEdit} className="rounded-2xl">
          <Edit2 className="mr-2 h-4 w-4" />
          {t('common.edit')}
        </Button>
      </div>
    </div>
  )
}

type ReferenceDeleteDialogContentProps = {
  item: ReferenceItem
  sectionKey: ReferenceSectionKey
  sectionLabel: string
  isDeleting: boolean
  onClose: () => void
  onConfirm: () => void
  t: (key: string, params?: Record<string, number | string>) => string
  language: AppLanguage
}

function ReferenceDeleteDialogContent({
  item,
  sectionKey,
  sectionLabel,
  isDeleting,
  onClose,
  onConfirm,
  t,
  language,
}: ReferenceDeleteDialogContentProps) {
  const visual = sectionVisuals[sectionKey]
  const ItemIcon = visual.icon
  const displayName = getReferenceDisplayName(sectionKey, item, language)

  return (
    <div className="space-y-6">
      <div className="px-12 text-center">
        <div className="mx-auto flex size-18 items-center justify-center rounded-[22px] border border-destructive/30 bg-destructive/10 text-destructive shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]">
          <AlertTriangle className="h-8 w-8" />
        </div>
        <h2 className="mt-5 font-heading text-2xl font-bold text-foreground">
          {t('admin.references.deleteTitle')}
        </h2>
        <p className="mt-3 text-sm leading-7 text-muted-foreground">
          {t('admin.references.confirmDelete', { name: displayName })}
        </p>
      </div>

      <div
        className={cn(
          'flex items-center gap-4 rounded-[24px] border border-border/60 bg-background/55 p-5',
          visual.tileClassName,
        )}
      >
        <div className="flex size-16 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70">
          <ItemIcon className={cn('h-7 w-7', visual.tileIconClassName)} />
        </div>
        <div className="min-w-0">
          <p className="text-sm font-medium text-primary">{sectionLabel}</p>
          <p className="mt-1 truncate text-xl font-semibold text-foreground">
            {displayName}
          </p>
        </div>
      </div>

      <p className="text-sm text-muted-foreground">
        {t('admin.references.deleteDescription')}
      </p>

      <div className="flex flex-wrap justify-end gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={onClose}
          disabled={isDeleting}
          className="rounded-2xl"
        >
          {t('common.cancel')}
        </Button>
        <Button
          type="button"
          variant="destructive"
          onClick={onConfirm}
          disabled={isDeleting}
          className="rounded-2xl"
        >
          <Trash2 className="mr-2 h-4 w-4" />
          {isDeleting ? t('common.processing') : t('common.delete')}
        </Button>
      </div>
    </div>
  )
}

function getReferenceDisplayName(
  sectionKey: ReferenceSectionKey,
  item: ReferenceItem,
  language: AppLanguage,
) {
  return sectionKey === 'categories' && 'translations' in item
    ? getCategoryLabel(item, language)
    : item.name
}

function DetailMetaCard({
  icon: Icon,
  label,
  value,
}: {
  icon: LucideIcon
  label: string
  value: string
}) {
  return (
    <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
      <div className="flex items-center gap-3">
        <div className="flex size-11 items-center justify-center rounded-2xl border border-border/60 bg-background/70 text-muted-foreground">
          <Icon className="h-4 w-4" />
        </div>
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
            {label}
          </p>
          <p className="mt-1 text-sm font-medium text-foreground">{value}</p>
        </div>
      </div>
    </div>
  )
}

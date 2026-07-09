import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  BookOpen,
  CalendarDays,
  CheckCircle2,
  Eye,
  MessageSquareText,
  Search,
  ShieldAlert,
  Star,
  Trash2,
  UserRound,
  X,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { PaginationControls } from '@/components/common/pagination-controls'
import { useAdminReviewsPage } from '@/hooks/use-admin-reviews-page'
import { AdminLayout } from '@/components/layout/admin-layout'
import type { AdminReviewResponse, AdminReviewStatus } from '@/types/admin-access'
import type { Book } from '@/types/book'

type UserLookup = {
  name: string
  email: string
}

export default function AdminReviewsPage() {
  const {
    t,
    formatDate,
    formatNumber,
    labels,
    reviews,
    page,
    pageSize,
    totalCount,
    searchTerm,
    selectedStatus,
    selectedRating,
    selectedBookId,
    selectedUserId,
    hideReason,
    isLoading,
    error,
    dialogMode,
    selectedReview,
    isMutating,
    activeReviewId,
    userLookup,
    bookLookup,
    filteredReviews,
    bookOptions,
    userOptions,
    averageRating,
    commentCount,
    handleSearchTermChange,
    handleStatusChange,
    handleRatingChange,
    handleBookChange,
    handleUserChange,
    handleHideReasonChange,
    handlePageChange,
    clearFilters,
    openViewDialog,
    openHideDialog,
    openDeleteDialog,
    closeDialog,
    handleApprove,
    handleHide,
    handleDelete,
  } = useAdminReviewsPage()

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={closeDialog}
        disabled={isMutating}
      />

      <div className="relative z-10 w-full max-w-3xl">
        {dialogMode === 'view' && selectedReview ? (
          <DialogShell title={labels.detailTitle} onClose={closeDialog}>
            <ReviewDetail
              book={bookLookup[selectedReview.bookId]}
              formatDate={formatDate}
              labels={labels}
              review={selectedReview}
              reviewer={userLookup[selectedReview.userId]}
            />
          </DialogShell>
        ) : null}

        {dialogMode === 'hide' && selectedReview ? (
          <HideDialog
            hideReason={hideReason}
            isMutating={isMutating}
            labels={labels}
            onClose={closeDialog}
            onConfirm={() => {
              void handleHide()
            }}
            onReasonChange={handleHideReasonChange}
          />
        ) : null}

        {dialogMode === 'delete' && selectedReview ? (
          <DeleteDialog
            isMutating={isMutating}
            labels={labels}
            onClose={closeDialog}
            onConfirm={() => {
              void handleDelete()
            }}
          />
        ) : null}
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(250,204,21,0.18),transparent_32%),radial-gradient(circle_at_bottom_right,rgba(59,130,246,0.14),transparent_34%)]" />

          <div className="relative">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                    {labels.title}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <MessageSquareText className="mr-2 h-4 w-4" />
                    {labels.total.replace('{count}', formatNumber(totalCount))}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {labels.description}
                </p>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <MetricCard
                  label={labels.average}
                  value={reviews.length === 0 ? '0.0' : averageRating.toFixed(1)}
                />
                <MetricCard
                  label={labels.withComment}
                  value={formatNumber(commentCount)}
                />
              </div>
            </div>

            <div className="mt-8 grid gap-3 lg:grid-cols-2 xl:grid-cols-[minmax(0,1.4fr)_repeat(4,minmax(0,1fr))_auto]">
              <div className="relative lg:col-span-2 xl:col-span-1">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={searchTerm}
                  onChange={handleSearchTermChange}
                  placeholder={labels.search}
                  className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base"
                />
              </div>
              <FilterSelect
                label={labels.filterStatus}
                value={selectedStatus}
                onChange={handleStatusChange}
                options={[
                  { value: '', label: labels.allStatuses },
                  { value: 'APPROVED', label: labels.statuses.APPROVED },
                  { value: 'HIDDEN', label: labels.statuses.HIDDEN },
                  { value: 'PENDING', label: labels.statuses.PENDING },
                ]}
              />
              <FilterSelect
                label={labels.filterRating}
                value={selectedRating === '' ? '' : String(selectedRating)}
                onChange={handleRatingChange}
                options={[
                  { value: '', label: labels.allRatings },
                  { value: '5', label: '5/5' },
                  { value: '4', label: '4/5' },
                  { value: '3', label: '3/5' },
                  { value: '2', label: '2/5' },
                  { value: '1', label: '1/5' },
                ]}
              />
              <FilterSelect
                label={labels.filterBook}
                value={selectedBookId}
                onChange={handleBookChange}
                options={[
                  { value: '', label: labels.allBooks },
                  ...bookOptions.map((book) => ({
                    value: book.id,
                    label: book.title,
                  })),
                ]}
              />
              <FilterSelect
                label={labels.filterUser}
                value={selectedUserId}
                onChange={handleUserChange}
                options={[
                  { value: '', label: labels.allUsers },
                  ...userOptions.map((reviewer) => ({
                    value: reviewer.userId,
                    label: `${reviewer.name} (${reviewer.email})`,
                  })),
                ]}
              />
              <div className="flex items-end">
                <Button
                  type="button"
                  variant="outline"
                  onClick={clearFilters}
                  className="h-14 w-full rounded-2xl xl:w-auto"
                >
                  {labels.clearFilters}
                </Button>
              </div>
            </div>

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.2fr_9rem_10rem_14rem]">
                  <div className="px-8 py-6">{labels.book}</div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.reviewer}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.rating}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.status}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {t('common.actions')}
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredReviews.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center text-muted-foreground">
                    {labels.empty}
                  </div>
                ) : (
                  filteredReviews.map((review) => {
                    const reviewer = userLookup[review.userId]
                    const book = bookLookup[review.bookId]
                    const rowIsMutating =
                      isMutating && activeReviewId === review.reviewId

                    return (
                      <article
                        key={review.reviewId}
                        className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.2fr_9rem_10rem_14rem] xl:gap-0 xl:p-0"
                      >
                        <div className="min-w-0 xl:px-8 xl:py-6">
                          <p className="truncate text-lg font-semibold text-foreground">
                            {book?.title ?? labels.unknownBook}
                          </p>
                          <p className="mt-2 truncate text-sm text-muted-foreground">
                            {review.comment?.trim() || labels.noComment}
                          </p>
                          <p className="mt-3 flex items-center gap-2 text-xs text-muted-foreground">
                            <CalendarDays className="h-3.5 w-3.5" />
                            {labels.updatedAt}: {formatDate(review.updatedAt)}
                          </p>
                        </div>

                        <div className="flex items-center justify-start border-border/40 text-sm font-medium text-foreground xl:justify-center xl:border-l">
                          <div className="min-w-0 text-left xl:text-center">
                            <p className="truncate">
                              {review.reviewerName ??
                                reviewer?.name ??
                                labels.unknownUser}
                            </p>
                            <p className="mt-1 truncate text-xs text-muted-foreground">
                              {reviewer?.email ?? review.userId}
                            </p>
                          </div>
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          <RatingBadge rating={review.rating} />
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          <StatusBadge
                            labels={labels.statuses}
                            status={review.status}
                          />
                        </div>

                        <div className="flex flex-wrap items-center gap-3 border-border/40 xl:justify-center xl:border-l">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => openViewDialog(review)}
                            className="rounded-2xl"
                            disabled={rowIsMutating}
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            {labels.view}
                          </Button>
                          {review.status !== 'APPROVED' ? (
                            <Button
                              type="button"
                              onClick={() => {
                                void handleApprove(review)
                              }}
                              className="rounded-2xl"
                              disabled={rowIsMutating}
                            >
                              <CheckCircle2 className="mr-2 h-4 w-4" />
                              {labels.approve}
                            </Button>
                          ) : null}
                          {review.status !== 'HIDDEN' ? (
                            <Button
                              type="button"
                              variant="outline"
                              onClick={() => openHideDialog(review)}
                              className="rounded-2xl"
                              disabled={rowIsMutating}
                            >
                              <ShieldAlert className="mr-2 h-4 w-4" />
                              {labels.hide}
                            </Button>
                          ) : null}
                          <Button
                            type="button"
                            variant="destructive"
                            onClick={() => openDeleteDialog(review)}
                            className="rounded-2xl"
                            disabled={rowIsMutating}
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            {labels.delete}
                          </Button>
                        </div>
                      </article>
                    )
                  })
                )}
              </div>
              {!isLoading && !error && totalCount > 0 ? (
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

function ReviewDetail({
  book,
  formatDate,
  labels,
  review,
  reviewer,
}: {
  book: Book | undefined
  formatDate: (value: string | number | Date) => string
  labels: {
    book: string
    comment: string
    noComment: string
    rating: string
    reviewer: string
    status: string
    moderationReason: string
    moderatedBy: string
    moderatedAt: string
    noReason: string
    unknownBook: string
    unknownUser: string
    updatedAt: string
    statuses: Record<AdminReviewStatus, string>
  }
  review: AdminReviewResponse
  reviewer: UserLookup | undefined
}) {
  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2">
        <DetailCard
          icon={BookOpen}
          label={labels.book}
          value={book?.title ?? labels.unknownBook}
        />
        <DetailCard
          icon={UserRound}
          label={labels.reviewer}
          value={review.reviewerName ?? reviewer?.name ?? labels.unknownUser}
          secondary={reviewer?.email ?? review.userId}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <DetailCard
          icon={Star}
          label={labels.rating}
          value={`${review.rating}/5`}
        />
        <DetailCard
          icon={CalendarDays}
          label={labels.updatedAt}
          value={formatDate(review.updatedAt)}
          secondary={review.orderItemId}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <StatusInfoCard labels={labels.statuses} status={review.status} />
        <DetailCard
          icon={ShieldAlert}
          label={labels.moderatedBy}
          value={review.moderatedByName ?? review.moderatedBy ?? labels.unknownUser}
        />
        <DetailCard
          icon={CalendarDays}
          label={labels.moderatedAt}
          value={
            review.moderatedAt ? formatDate(review.moderatedAt) : labels.noReason
          }
        />
      </div>

      <div className="rounded-[22px] border border-border/60 bg-background/55 p-5">
        <p className="text-sm text-muted-foreground">{labels.comment}</p>
        <p className="mt-3 whitespace-pre-wrap text-base font-medium text-foreground">
          {review.comment?.trim() || labels.noComment}
        </p>
      </div>

      <div className="rounded-[22px] border border-border/60 bg-background/55 p-5">
        <p className="text-sm text-muted-foreground">{labels.moderationReason}</p>
        <p className="mt-3 whitespace-pre-wrap text-base font-medium text-foreground">
          {review.moderationReason?.trim() || labels.noReason}
        </p>
      </div>
    </div>
  )
}

function HideDialog({
  hideReason,
  isMutating,
  labels,
  onClose,
  onConfirm,
  onReasonChange,
}: {
  hideReason: string
  isMutating: boolean
  labels: {
    cancel: string
    hide: string
    hideDescription: string
    hideTitle: string
    reason: string
    reasonPlaceholder: string
  }
  onClose: () => void
  onConfirm: () => void
  onReasonChange: (event: React.ChangeEvent<HTMLTextAreaElement>) => void
}) {
  return (
    <div className="mx-auto max-w-2xl overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start gap-4 px-6 py-6">
        <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-amber-500/12 text-amber-500">
          <ShieldAlert className="h-6 w-6" />
        </div>
        <div className="min-w-0 flex-1">
          <h2 className="text-2xl font-semibold text-foreground">
            {labels.hideTitle}
          </h2>
          <p className="mt-3 text-sm text-muted-foreground">
            {labels.hideDescription}
          </p>
        </div>
      </div>

      <div className="px-6 pb-6">
        <label className="mb-2 block text-sm font-medium text-foreground">
          {labels.reason}
        </label>
        <textarea
          value={hideReason}
          onChange={onReasonChange}
          rows={5}
          placeholder={labels.reasonPlaceholder}
          className="w-full rounded-[22px] border border-border/70 bg-background/55 px-4 py-3 text-sm text-foreground outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/15"
        />
      </div>

      <div className="flex items-center justify-end gap-3 border-t border-border/60 px-6 py-5">
        <Button
          type="button"
          variant="outline"
          onClick={onClose}
          className="rounded-2xl"
          disabled={isMutating}
        >
          {labels.cancel}
        </Button>
        <Button
          type="button"
          onClick={onConfirm}
          className="rounded-2xl"
          disabled={isMutating}
        >
          {isMutating ? '...' : labels.hide}
        </Button>
      </div>
    </div>
  )
}

function DeleteDialog({
  isMutating,
  labels,
  onClose,
  onConfirm,
}: {
  isMutating: boolean
  labels: {
    cancel: string
    delete: string
    deleteDescription: string
    deleteTitle: string
  }
  onClose: () => void
  onConfirm: () => void
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
          <p className="mt-3 text-sm text-muted-foreground">
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
          disabled={isMutating}
        >
          {labels.cancel}
        </Button>
        <Button
          type="button"
          variant="destructive"
          onClick={onConfirm}
          className="rounded-2xl"
          disabled={isMutating}
        >
          {isMutating ? '...' : labels.delete}
        </Button>
      </div>
    </div>
  )
}

function DialogShell({
  children,
  onClose,
  title,
}: {
  children: React.ReactNode
  onClose: () => void
  title: string
}) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
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

      <div className="max-h-[78vh] overflow-y-auto px-6 py-6">{children}</div>
    </div>
  )
}

function MetricCard({
  label,
  value,
}: {
  label: string
  value: string
}) {
  return (
    <div className="rounded-2xl border border-border/60 bg-background/55 px-5 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 text-2xl font-bold text-foreground">{value}</p>
    </div>
  )
}

function DetailCard({
  icon: Icon,
  label,
  value,
  secondary,
}: {
  icon: typeof BookOpen
  label: string
  value: string
  secondary?: string
}) {
  return (
    <div className="rounded-[22px] border border-border/60 bg-background/55 p-4">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Icon className="h-4 w-4" />
        <span>{label}</span>
      </div>
      <p className="mt-3 text-base font-semibold text-foreground">{value}</p>
      {secondary ? (
        <p className="mt-2 break-all text-xs text-muted-foreground">{secondary}</p>
      ) : null}
    </div>
  )
}

function StatusInfoCard({
  labels,
  status,
}: {
  labels: Record<AdminReviewStatus, string>
  status: AdminReviewStatus
}) {
  return (
    <div className="rounded-[22px] border border-border/60 bg-background/55 p-4">
      <p className="text-sm text-muted-foreground">Status</p>
      <div className="mt-3">
        <StatusBadge labels={labels} status={status} />
      </div>
    </div>
  )
}

function FilterSelect({
  label,
  onChange,
  options,
  value,
}: {
  label: string
  onChange: (event: React.ChangeEvent<HTMLSelectElement>) => void
  options: Array<{ value: string; label: string }>
  value: string
}) {
  return (
    <label className="block">
      <span className="mb-2 block text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
        {label}
      </span>
      <select
        value={value}
        onChange={onChange}
        className="h-14 w-full rounded-2xl border border-border/70 bg-background/55 px-4 text-sm text-foreground outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/15"
      >
        {options.map((option) => (
          <option key={`${label}-${option.value || 'all'}`} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  )
}

function RatingBadge({ rating }: { rating: number }) {
  return (
    <Badge variant="outline" className="rounded-2xl px-3 py-1.5 text-amber-500">
      <Star className="mr-1.5 h-3.5 w-3.5 fill-current" />
      {rating}/5
    </Badge>
  )
}

function StatusBadge({
  labels,
  status,
}: {
  labels: Record<AdminReviewStatus, string>
  status: AdminReviewStatus
}) {
  const statusClassName =
    status === 'APPROVED'
      ? 'border-emerald-500/30 bg-emerald-500/12 text-emerald-500'
      : status === 'HIDDEN'
        ? 'border-destructive/30 bg-destructive/10 text-destructive'
        : 'border-amber-500/30 bg-amber-500/12 text-amber-500'

  return (
    <Badge variant="outline" className={`rounded-2xl px-3 py-1.5 ${statusClassName}`}>
      {labels[status]}
    </Badge>
  )
}

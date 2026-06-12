import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  BookOpen,
  CalendarDays,
  Eye,
  MessageSquareText,
  Search,
  Star,
  Trash2,
  UserRound,
  X,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { useAdminReviewsPage } from '@/hooks/use-admin-reviews-page'
import { AdminLayout } from '@/components/layout/admin-layout'
import type { AdminReviewResponse } from '@/types/admin-access'
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
    searchTerm,
    isLoading,
    error,
    dialogMode,
    selectedReview,
    isDeleting,
    userLookup,
    bookLookup,
    filteredReviews,
    averageRating,
    commentCount,
    handleSearchTermChange,
    openViewDialog,
    openDeleteDialog,
    closeDialog,
    handleDelete,
  } = useAdminReviewsPage()

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={closeDialog}
        disabled={isDeleting}
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

        {dialogMode === 'delete' && selectedReview ? (
          <DeleteDialog
            isDeleting={isDeleting}
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
                    {interpolateLabel(labels.total, {
                      count: formatNumber(reviews.length),
                    })}
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

            <div className="mt-8 max-w-xl">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={searchTerm}
                  onChange={handleSearchTermChange}
                  placeholder={labels.search}
                  className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base"
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
                <div className="hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.3fr_10rem_12rem]">
                  <div className="px-8 py-6">{labels.book}</div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.reviewer}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.rating}
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

                    return (
                      <article
                        key={review.reviewId}
                        className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.3fr_10rem_12rem] xl:gap-0 xl:p-0"
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
                              {reviewer?.name ?? labels.unknownUser}
                            </p>
                            <p className="mt-1 truncate text-xs text-muted-foreground">
                              {reviewer?.email ?? review.userId}
                            </p>
                          </div>
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          <RatingBadge rating={review.rating} />
                        </div>

                        <div className="flex flex-wrap items-center gap-3 border-border/40 xl:justify-center xl:border-l">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => openViewDialog(review)}
                            className="rounded-2xl"
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            {t('common.view')}
                          </Button>
                          <Button
                            type="button"
                            variant="destructive"
                            onClick={() => openDeleteDialog(review)}
                            className="rounded-2xl"
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            {t('common.delete')}
                          </Button>
                        </div>
                      </article>
                    )
                  })
                )}
              </div>
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
    unknownBook: string
    unknownUser: string
    updatedAt: string
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
          value={reviewer?.name ?? labels.unknownUser}
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

      <div className="rounded-[22px] border border-border/60 bg-background/55 p-5">
        <p className="text-sm text-muted-foreground">{labels.comment}</p>
        <p className="mt-3 whitespace-pre-wrap text-base font-medium text-foreground">
          {review.comment?.trim() || labels.noComment}
        </p>
      </div>
    </div>
  )
}

function DeleteDialog({
  isDeleting,
  labels,
  onClose,
  onConfirm,
}: {
  isDeleting: boolean
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
          disabled={isDeleting}
        >
          {labels.cancel}
        </Button>
        <Button
          type="button"
          variant="destructive"
          onClick={onConfirm}
          className="rounded-2xl"
          disabled={isDeleting}
        >
          {isDeleting ? '...' : labels.delete}
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

function RatingBadge({ rating }: { rating: number }) {
  return (
    <Badge variant="outline" className="rounded-2xl px-3 py-1.5 text-amber-500">
      <Star className="mr-1.5 h-3.5 w-3.5 fill-current" />
      {rating}/5
    </Badge>
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

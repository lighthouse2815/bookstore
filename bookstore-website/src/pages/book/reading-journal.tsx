import { type ReactNode } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import {
  ArrowLeft,
  CalendarDays,
  ExternalLink,
  Flame,
  LibraryBig,
  PencilLine,
  Trash2,
} from 'lucide-react'
import { Button, buttonVariants } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import {
  StatePanel,
  SummaryField,
  destructiveOutlineButtonClassName,
  primaryButtonClassName,
  secondaryButtonClassName,
  secondaryLinkButtonClassName,
} from '@/components/common/page-shell'
import { Textarea } from '@/components/common/textarea'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { getCategoryLabel } from '@/utils/i18n'
import { useReadingJournal } from '@/hooks/use-reading-journal'
import type { ReadingJournalEntry } from '@/types/reading-journal'
import { cn } from '@/utils'
import { getBookCoverUrl, setBookCoverFallback } from '@/utils/book-cover'
import { groupReadingJournalEntriesByDate } from '@/utils/reading-journal'

export default function ReadingJournalPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const prefillBookId = searchParams.get('bookId') ?? ''
  const prefillCurrentPage = searchParams.get('currentPage') ?? ''
  const prefillProgressPercent = searchParams.get('progressPercent') ?? ''
  const {
    availableBooks,
    entriesPage,
    streak,
    filters,
    composer,
    editingEntryId,
    isLoading,
    isBookOptionsLoading,
    isStreakLoading,
    isSubmitting,
    error,
    handleFilterChange,
    handlePageChange,
    resetFilters,
    handleComposerFieldChange,
    startEditingEntry,
    cancelEditingEntry,
    submitEntry,
    removeEntry,
    checkInToday,
  } = useReadingJournal({
    prefill: {
      bookId: prefillBookId,
      currentPage: prefillCurrentPage,
      progressPercent: prefillProgressPercent,
    },
  })

  const groupedEntries = groupReadingJournalEntriesByDate(entriesPage.items)

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(252,248,242,1)_0%,rgba(255,255,255,1)_46%,rgba(245,249,255,1)_100%)] dark:bg-[linear-gradient(180deg,rgba(20,17,14,1)_0%,rgba(20,20,18,1)_46%,rgba(16,16,18,1)_100%)]">
      <Header />
      <main className="flex-1 pb-16 pt-8">
        <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <div className="flex flex-wrap items-center gap-3">
            <Link
              to="/profile"
              className={cn(
                buttonVariants({ variant: 'outline' }),
                secondaryLinkButtonClassName,
              )}
            >
              <ArrowLeft className="mr-2 size-4" />
              {t('readingJournal.backToProfile')}
            </Link>
            <Link
              to="/library"
              className={cn(
                buttonVariants({ variant: 'outline' }),
                secondaryLinkButtonClassName,
              )}
            >
              {t('readingJournal.openLibrary')}
            </Link>
          </div>

          <section className="overflow-hidden rounded-[34px] border border-amber-500/12 bg-white/92 p-6 shadow-[0_28px_80px_rgba(175,120,58,0.12)] backdrop-blur dark:border-white/10 dark:bg-card/92 dark:shadow-[0_28px_80px_rgba(0,0,0,0.32)]">
            <div className="grid gap-6 xl:grid-cols-[minmax(0,1.4fr)_360px]">
              <div className="space-y-5">
                <span className="inline-flex items-center gap-2 rounded-full bg-amber-500/12 px-4 py-1.5 text-sm font-semibold text-amber-700 dark:bg-amber-400/12 dark:text-amber-200">
                  <PencilLine className="size-4" />
                  {t('readingJournal.badge')}
                </span>

                <div className="space-y-3">
                  <h1 className="font-heading text-4xl font-bold tracking-tight text-slate-950 dark:text-foreground">
                    {t('readingJournal.title')}
                  </h1>
                  <p className="max-w-3xl text-sm leading-7 text-slate-600 dark:text-muted-foreground sm:text-base">
                    {t('readingJournal.description')}
                  </p>
                </div>

                <div className="grid gap-4 md:grid-cols-3">
                  <HeroStat
                    label={t('readingJournal.stats.entries')}
                    value={formatNumber(entriesPage.totalCount)}
                    description={t('readingJournal.stats.entriesHint')}
                  />
                  <HeroStat
                    label={t('readingJournal.stats.current')}
                    value={
                      isStreakLoading
                        ? '...'
                        : formatNumber(streak?.currentStreak ?? 0)
                    }
                    description={t('readingJournal.stats.currentHint')}
                  />
                  <HeroStat
                    label={t('readingJournal.stats.longest')}
                    value={
                      isStreakLoading
                        ? '...'
                        : formatNumber(streak?.longestStreak ?? 0)
                    }
                    description={t('readingJournal.stats.longestHint')}
                  />
                </div>
              </div>

              <aside className="rounded-[28px] border border-amber-500/12 bg-[linear-gradient(180deg,rgba(255,248,230,0.95)_0%,rgba(255,255,255,0.92)_100%)] p-5 shadow-sm dark:border-amber-300/20 dark:bg-[linear-gradient(180deg,rgba(77,56,20,0.28)_0%,rgba(34,30,25,0.78)_100%)]">
                <div className="flex items-center gap-3">
                  <span className="flex size-12 items-center justify-center rounded-2xl bg-amber-500/16 text-amber-700 dark:bg-amber-300/14 dark:text-amber-200">
                    <Flame className="size-5" />
                  </span>
                  <div>
                    <p className="text-sm font-semibold text-foreground">
                      {t('readingJournal.streak.title')}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {t('readingJournal.streak.description')}
                    </p>
                  </div>
                </div>

                <div className="mt-5 grid gap-3 sm:grid-cols-3 xl:grid-cols-1">
                  <StreakCard
                    label={t('readingJournal.streak.current')}
                    value={formatNumber(streak?.currentStreak ?? 0)}
                    tone="warm"
                  />
                  <StreakCard
                    label={t('readingJournal.streak.longest')}
                    value={formatNumber(streak?.longestStreak ?? 0)}
                    tone="neutral"
                  />
                  <StreakCard
                    label={t('readingJournal.streak.status')}
                    value={
                      streak?.checkedInToday
                        ? t('readingJournal.streak.checkedIn')
                        : t('readingJournal.streak.notCheckedIn')
                    }
                    tone={streak?.checkedInToday ? 'success' : 'neutral'}
                  />
                </div>

                <div className="mt-5 rounded-[22px] border border-amber-500/12 bg-white/90 p-4 dark:border-white/10 dark:bg-background/35">
                  <p className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                    {t('readingJournal.streak.lastActivity')}
                  </p>
                  <p className="mt-2 text-sm font-semibold text-foreground">
                    {streak?.lastActivityDate
                      ? formatDate(streak.lastActivityDate)
                      : t('readingJournal.streak.noActivity')}
                  </p>
                </div>
              </aside>
            </div>
          </section>

          <section className="grid gap-6 xl:grid-cols-[380px_minmax(0,1fr)]">
            <aside className="space-y-6 xl:sticky xl:top-24 xl:self-start">
              <section className="rounded-[30px] border border-primary/10 bg-white/92 p-5 shadow-[0_18px_50px_rgba(50,88,160,0.1)] dark:border-white/10 dark:bg-card/92 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-sm font-semibold text-foreground">
                      {editingEntryId
                        ? t('readingJournal.composer.editTitle')
                        : t('readingJournal.composer.title')}
                    </p>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {editingEntryId
                        ? t('readingJournal.composer.editDescription')
                        : t('readingJournal.composer.description')}
                    </p>
                  </div>
                  <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
                    {editingEntryId
                      ? t('readingJournal.composer.editBadge')
                      : t('readingJournal.composer.newBadge')}
                  </span>
                </div>

                <div className="mt-5 space-y-4">
                  <div>
                    <Label htmlFor="bookId">{t('readingJournal.fields.book')}</Label>
                    <select
                      id="bookId"
                      value={composer.bookId}
                      onChange={(event) =>
                        handleComposerFieldChange('bookId', event.currentTarget.value)
                      }
                      disabled={isSubmitting || isBookOptionsLoading}
                      className="mt-2 flex h-12 w-full rounded-2xl border border-input bg-background px-3 text-sm text-foreground outline-none transition-colors focus:border-primary"
                    >
                      <option value="">{t('readingJournal.placeholders.book')}</option>
                      {availableBooks.map((book) => (
                        <option key={book.id} value={book.id}>
                          {book.title}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <Label htmlFor="entryDate">
                      {t('readingJournal.fields.entryDate')}
                    </Label>
                    <Input
                      id="entryDate"
                      type="date"
                      value={composer.entryDate}
                      onChange={(event) =>
                        handleComposerFieldChange(
                          'entryDate',
                          event.currentTarget.value,
                        )
                      }
                      disabled={isSubmitting || Boolean(editingEntryId)}
                      className="mt-2 h-12 rounded-2xl"
                    />
                  </div>

                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <Label htmlFor="currentPage">
                        {t('readingJournal.fields.currentPage')}
                      </Label>
                      <Input
                        id="currentPage"
                        type="number"
                        min="0"
                        step="1"
                        value={composer.currentPage}
                        onChange={(event) =>
                          handleComposerFieldChange(
                            'currentPage',
                            event.currentTarget.value,
                          )
                        }
                        disabled={isSubmitting}
                        className="mt-2 h-12 rounded-2xl"
                        placeholder={t('readingJournal.placeholders.currentPage')}
                      />
                    </div>

                    <div>
                      <Label htmlFor="progressPercent">
                        {t('readingJournal.fields.progressPercent')}
                      </Label>
                      <Input
                        id="progressPercent"
                        type="number"
                        min="0"
                        max="100"
                        step="0.1"
                        value={composer.progressPercent}
                        onChange={(event) =>
                          handleComposerFieldChange(
                            'progressPercent',
                            event.currentTarget.value,
                          )
                        }
                        disabled={isSubmitting}
                        className="mt-2 h-12 rounded-2xl"
                        placeholder={t('readingJournal.placeholders.progressPercent')}
                      />
                    </div>
                  </div>

                  <div>
                    <Label htmlFor="note">{t('readingJournal.fields.note')}</Label>
                    <Textarea
                      id="note"
                      value={composer.note}
                      onChange={(event) =>
                        handleComposerFieldChange('note', event.currentTarget.value)
                      }
                      disabled={isSubmitting}
                      rows={6}
                      className="mt-2 rounded-2xl"
                      placeholder={t('readingJournal.placeholders.note')}
                    />
                  </div>
                </div>

                <div className="mt-5 flex flex-col gap-3">
                  <Button
                    type="button"
                    className={primaryButtonClassName}
                    disabled={isSubmitting}
                    onClick={() => void submitEntry()}
                  >
                    <PencilLine className="mr-2 size-4" />
                    {editingEntryId
                      ? t('readingJournal.actions.updateEntry')
                      : t('readingJournal.actions.saveEntry')}
                  </Button>

                  {!editingEntryId ? (
                    <Button
                      type="button"
                      variant="outline"
                      className={secondaryButtonClassName}
                      disabled={isSubmitting}
                      onClick={() => void checkInToday()}
                    >
                      <Flame className="mr-2 size-4" />
                      {t('readingJournal.actions.checkInToday')}
                    </Button>
                  ) : (
                    <Button
                      type="button"
                      variant="outline"
                      className={secondaryButtonClassName}
                      disabled={isSubmitting}
                      onClick={cancelEditingEntry}
                    >
                      {t('common.cancel')}
                    </Button>
                  )}
                </div>
              </section>

              <section className="rounded-[30px] border border-primary/10 bg-white/92 p-5 shadow-[0_18px_50px_rgba(50,88,160,0.1)] dark:border-white/10 dark:bg-card/92 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-sm font-semibold text-foreground">
                      {t('readingJournal.filters.title')}
                    </p>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {t('readingJournal.filters.description')}
                    </p>
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={resetFilters}
                  >
                    {t('readingJournal.filters.reset')}
                  </Button>
                </div>

                <div className="mt-5 space-y-4">
                  <div>
                    <Label htmlFor="filterBookId">{t('readingJournal.filters.book')}</Label>
                    <select
                      id="filterBookId"
                      value={filters.bookId ?? ''}
                      onChange={(event) =>
                        handleFilterChange('bookId', event.currentTarget.value)
                      }
                      disabled={isBookOptionsLoading}
                      className="mt-2 flex h-12 w-full rounded-2xl border border-input bg-background px-3 text-sm text-foreground outline-none transition-colors focus:border-primary"
                    >
                      <option value="">{t('readingJournal.filters.allBooks')}</option>
                      {availableBooks.map((book) => (
                        <option key={book.id} value={book.id}>
                          {book.title}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <Label htmlFor="fromDate">
                        {t('readingJournal.filters.from')}
                      </Label>
                      <Input
                        id="fromDate"
                        type="date"
                        value={filters.from ?? ''}
                        onChange={(event) =>
                          handleFilterChange('from', event.currentTarget.value)
                        }
                        className="mt-2 h-12 rounded-2xl"
                      />
                    </div>
                    <div>
                      <Label htmlFor="toDate">{t('readingJournal.filters.to')}</Label>
                      <Input
                        id="toDate"
                        type="date"
                        value={filters.to ?? ''}
                        onChange={(event) =>
                          handleFilterChange('to', event.currentTarget.value)
                        }
                        className="mt-2 h-12 rounded-2xl"
                      />
                    </div>
                  </div>
                </div>
              </section>
            </aside>

            <section className="space-y-5">
              <div className="rounded-[30px] border border-primary/10 bg-white/92 p-5 shadow-[0_18px_50px_rgba(50,88,160,0.1)] dark:border-white/10 dark:bg-card/92 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                  <div>
                    <p className="text-sm font-semibold text-foreground">
                      {t('readingJournal.timeline.title')}
                    </p>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {t('readingJournal.timeline.description')}
                    </p>
                  </div>
                  <div className="rounded-2xl border border-primary/12 bg-primary/6 px-4 py-3 text-sm text-primary dark:border-primary/20 dark:bg-primary/10">
                    {t('readingJournal.timeline.countLabel', {
                      count: formatNumber(entriesPage.totalCount),
                    })}
                  </div>
                </div>
              </div>

              {isLoading ? (
                <StatePanel
                  title={t('common.loading')}
                  description={t('readingJournal.timeline.description')}
                />
              ) : error ? (
                <StatePanel
                  title={t('readingJournal.timeline.title')}
                  description={error}
                  tone="error"
                />
              ) : groupedEntries.length === 0 ? (
                <StatePanel
                  icon={<LibraryBig className="mx-auto size-12 text-primary/70" />}
                  title={t('readingJournal.emptyTitle')}
                  description={t('readingJournal.emptyDescription')}
                />
              ) : (
                groupedEntries.map((group) => (
                  <section key={group.date} className="space-y-4">
                    <div className="flex items-center gap-3">
                      <span className="flex size-10 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                        <CalendarDays className="size-4" />
                      </span>
                      <div>
                        <p className="text-sm font-semibold text-foreground">
                          {formatDate(group.date)}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          {t('readingJournal.timeline.dayCount', {
                            count: formatNumber(group.items.length),
                          })}
                        </p>
                      </div>
                    </div>

                    <div className="grid gap-4">
                      {group.items.map((entry) => (
                        <JournalEntryCard
                          key={entry.id}
                          entry={entry}
                          isSubmitting={isSubmitting}
                          formatCurrency={formatCurrency}
                          formatDate={formatDate}
                          onOpenBook={() => navigate(`/books/${entry.book.id}`)}
                          onEdit={() => startEditingEntry(entry)}
                          onDelete={() => {
                            if (
                              window.confirm(t('readingJournal.deleteConfirm'))
                            ) {
                              void removeEntry(entry.id)
                            }
                          }}
                          labels={{
                            openBook: t('readingJournal.actions.openBook'),
                            editEntry: t('readingJournal.actions.editEntry'),
                            deleteEntry: t('readingJournal.actions.deleteEntry'),
                            noteFallback: t('readingJournal.timeline.noteFallback'),
                            currentPage: t('readingJournal.timeline.currentPage'),
                            progressPercent:
                              t('readingJournal.timeline.progressPercent'),
                            updatedAt: t('readingJournal.timeline.updatedAt'),
                          }}
                        />
                      ))}
                    </div>
                  </section>
                ))
              )}

              {entriesPage.totalPages > 1 ? (
                <div className="flex flex-col gap-3 rounded-[28px] border border-primary/10 bg-white/92 p-5 shadow-sm sm:flex-row sm:items-center sm:justify-between dark:border-white/10 dark:bg-card/92">
                  <p className="text-sm text-muted-foreground">
                    {t('readingJournal.pagination.summary', {
                      page: formatNumber(entriesPage.page + 1),
                      totalPages: formatNumber(entriesPage.totalPages),
                    })}
                  </p>
                  <div className="flex gap-3">
                    <Button
                      type="button"
                      variant="outline"
                      className={secondaryButtonClassName}
                      disabled={entriesPage.page <= 0}
                      onClick={() => handlePageChange(entriesPage.page - 1)}
                    >
                      {t('common.previous')}
                    </Button>
                    <Button
                      type="button"
                      variant="outline"
                      className={secondaryButtonClassName}
                      disabled={!entriesPage.hasNext}
                      onClick={() => handlePageChange(entriesPage.page + 1)}
                    >
                      {t('common.next')}
                    </Button>
                  </div>
                </div>
              ) : null}
            </section>
          </section>
        </div>
      </main>
      <Footer />
    </div>
  )
}

function HeroStat({
  label,
  value,
  description,
}: {
  label: string
  value: string
  description: string
}) {
  return (
    <div className="rounded-[24px] border border-slate-200/70 bg-slate-50/70 px-5 py-4 dark:border-white/10 dark:bg-background/35">
      <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 font-heading text-3xl font-bold text-slate-950 dark:text-foreground">
        {value}
      </p>
      <p className="mt-2 text-sm text-muted-foreground">{description}</p>
    </div>
  )
}

function StreakCard({
  label,
  value,
  tone,
}: {
  label: string
  value: string
  tone: 'warm' | 'neutral' | 'success'
}) {
  return (
    <div
      className={cn(
        'rounded-[22px] border px-4 py-4',
        tone === 'warm' &&
          'border-amber-500/12 bg-amber-500/8 text-amber-800 dark:text-amber-100',
        tone === 'neutral' &&
          'border-slate-200/80 bg-white/92 text-slate-900 dark:border-white/10 dark:bg-background/35 dark:text-foreground',
        tone === 'success' &&
          'border-emerald-500/12 bg-emerald-500/8 text-emerald-800 dark:text-emerald-100',
      )}
    >
      <p className="text-xs font-semibold uppercase tracking-[0.16em] text-current/70">
        {label}
      </p>
      <p className="mt-2 font-heading text-2xl font-bold">{value}</p>
    </div>
  )
}

function JournalEntryCard({
  entry,
  isSubmitting,
  formatCurrency,
  formatDate,
  labels,
  onOpenBook,
  onEdit,
  onDelete,
}: {
  entry: ReadingJournalEntry
  isSubmitting: boolean
  formatCurrency: (value: number) => string
  formatDate: (value: string) => string
  labels: {
    openBook: string
    editEntry: string
    deleteEntry: string
    noteFallback: string
    currentPage: string
    progressPercent: string
    updatedAt: string
  }
  onOpenBook: () => void
  onEdit: () => void
  onDelete: () => void
}) {
  const { language } = useLanguage()
  return (
    <article className="motion-card overflow-hidden rounded-[28px] border border-primary/10 bg-white/92 shadow-[0_16px_48px_rgba(50,88,160,0.08)] dark:border-white/10 dark:bg-card/92 dark:shadow-[0_16px_48px_rgba(0,0,0,0.24)]">
      <div className="grid gap-0 lg:grid-cols-[180px_minmax(0,1fr)]">
        <button
          type="button"
          onClick={onOpenBook}
          className="relative min-h-[220px] overflow-hidden bg-muted text-left"
        >
          <img
            src={getBookCoverUrl(entry.book.cover)}
            alt={entry.book.title}
            onError={(event) => setBookCoverFallback(event.currentTarget)}
            className="absolute inset-0 size-full object-cover"
          />
        </button>

        <div className="flex flex-col gap-5 p-5">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div className="min-w-0">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-primary/80">
                {getCategoryLabel(entry.book.categoryInfo ?? entry.book.category, language)}
              </p>
              <h3 className="mt-2 font-heading text-2xl font-bold leading-tight text-slate-950 dark:text-foreground">
                {entry.book.title}
              </h3>
              <p className="mt-2 text-sm text-muted-foreground">
                {entry.book.author}
              </p>
            </div>

            <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
              {formatCurrency(entry.book.price)}
            </span>
          </div>

          <div className="rounded-[22px] border border-primary/10 bg-primary/5 p-4">
            <p className="text-sm leading-7 text-foreground">
              {entry.note?.trim() || labels.noteFallback}
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            <MetaBlock
              label={labels.currentPage}
              value={
                typeof entry.currentPage === 'number'
                  ? String(entry.currentPage)
                  : '--'
              }
            />
            <MetaBlock
              label={labels.progressPercent}
              value={
                typeof entry.progressPercent === 'number'
                  ? `${entry.progressPercent}%`
                  : '--'
              }
            />
            <MetaBlock
              label={labels.updatedAt}
              value={formatDate(entry.updatedAt)}
            />
          </div>

          <div className="mt-auto flex flex-wrap gap-2">
            <Button
              type="button"
              variant="outline"
              className={secondaryButtonClassName}
              onClick={onOpenBook}
            >
              <ExternalLink className="mr-2 size-4" />
              {labels.openBook}
            </Button>
            <Button
              type="button"
              variant="outline"
              className={secondaryButtonClassName}
              disabled={isSubmitting}
              onClick={onEdit}
            >
              <PencilLine className="mr-2 size-4" />
              {labels.editEntry}
            </Button>
            <Button
              type="button"
              variant="outline"
              className={destructiveOutlineButtonClassName}
              disabled={isSubmitting}
              onClick={onDelete}
            >
              <Trash2 className="mr-2 size-4" />
              {labels.deleteEntry}
            </Button>
          </div>
        </div>
      </div>
    </article>
  )
}

function MetaBlock({ label, value }: { label: string; value: string }) {
  return <SummaryField label={label} value={value} className="rounded-[20px]" />
}

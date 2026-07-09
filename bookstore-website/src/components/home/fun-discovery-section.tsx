import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowRight,
  Compass,
  Gift,
  Heart,
  History,
  Sparkles,
  Trophy,
} from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import type { BookCardData } from '@/types/book'
import type { ReadingChallenge } from '@/types/reading-challenge'
import { getBookCoverUrl } from '@/utils/book-cover'
import { getRecentlyViewedBooks } from '@/utils/recently-viewed'
import {
  getReadingChallengeProgressPercent,
  getReadingChallengeStatus,
  loadReadingChallenge,
} from '@/utils/reading-challenge'

const RECENT_BOOKS_LIMIT = 3

const cardBaseClassName =
  'relative overflow-hidden rounded-[1.75rem] border p-6 shadow-[0_18px_60px_rgba(15,23,42,0.06)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_24px_80px_rgba(15,23,42,0.1)] focus-within:ring-2 focus-within:ring-primary/30'

const ctaClassName =
  'inline-flex items-center gap-2 text-sm font-semibold transition-colors hover:text-primary/80'

export function FunDiscoverySection() {
  const { t } = useLanguage()
  const [recentlyViewedBooks, setRecentlyViewedBooks] = useState<BookCardData[]>(
    [],
  )
  const [readingChallenge, setReadingChallenge] =
    useState<ReadingChallenge | null>(null)

  useEffect(() => {
    setRecentlyViewedBooks(getRecentlyViewedBooks().slice(0, RECENT_BOOKS_LIMIT))

    if (typeof window !== 'undefined') {
      setReadingChallenge(loadReadingChallenge(window.localStorage))
    }
  }, [])

  const newestRecentBook = recentlyViewedBooks[0]
  const readingChallengeProgress = readingChallenge
    ? getReadingChallengeProgressPercent(readingChallenge)
    : 0
  const readingChallengeStatus = readingChallenge
    ? getReadingChallengeStatus(readingChallenge)
    : null

  return (
    <section className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="mb-6">
        <h2 className="font-heading text-2xl font-bold tracking-tight sm:text-3xl">
          {t('home.funDiscovery.title')}
        </h2>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-muted-foreground sm:text-base">
          {t('home.funDiscovery.subtitle')}
        </p>
      </div>

      <div className="grid gap-4 lg:grid-cols-12">
        <article
          className={`${cardBaseClassName} border-sky-200/70 bg-gradient-to-br from-sky-50/90 via-card to-emerald-50/80 dark:border-sky-500/20 dark:from-sky-500/10 dark:via-card dark:to-emerald-500/10 lg:col-span-4`}
        >
          <div className="absolute -right-8 top-0 size-28 rounded-full bg-sky-300/30 blur-2xl dark:bg-sky-400/10" />
          <div className="absolute bottom-0 left-10 size-24 rounded-full bg-emerald-300/25 blur-2xl dark:bg-emerald-400/10" />
          <div className="relative flex h-full flex-col">
            <span className="flex size-12 items-center justify-center rounded-2xl bg-white/75 text-sky-700 shadow-sm dark:bg-background/70 dark:text-sky-300">
              <Compass className="size-6" />
            </span>
            <span className="mt-4 inline-flex w-fit rounded-full bg-white/75 px-3 py-1 text-xs font-semibold text-sky-700 shadow-sm dark:bg-background/70 dark:text-sky-200">
              {t('home.bookMatch.stepCount')}
            </span>
            <h3 className="mt-4 font-heading text-2xl font-bold tracking-tight text-slate-950 dark:text-foreground">
              {t('home.funDiscovery.bookMatchTitle')}
            </h3>
            <p className="mt-3 max-w-md text-sm leading-7 text-slate-600 dark:text-muted-foreground sm:text-base">
              {t('home.funDiscovery.bookMatchDescription')}
            </p>
            <Link to="/book-match" className={`${ctaClassName} mt-auto pt-6`}>
              {t('home.funDiscovery.bookMatchCta')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
        </article>

        <article
          className={`${cardBaseClassName} border-rose-200/70 bg-gradient-to-br from-rose-50/90 via-card to-fuchsia-50/80 dark:border-rose-500/20 dark:from-rose-500/10 dark:via-card dark:to-fuchsia-500/10 lg:col-span-4`}
        >
          <div className="absolute -right-8 top-0 size-28 rounded-full bg-rose-200/45 blur-2xl dark:bg-rose-400/10" />
          <div className="absolute bottom-0 left-10 size-24 rounded-full bg-fuchsia-200/45 blur-2xl dark:bg-fuchsia-400/10" />
          <div className="relative flex h-full flex-col">
            <span className="flex size-12 items-center justify-center rounded-2xl bg-white/80 text-rose-700 shadow-sm dark:bg-background/70 dark:text-rose-300">
              <Sparkles className="size-6" />
            </span>
            <span className="mt-4 inline-flex w-fit rounded-full bg-white/80 px-3 py-1 text-xs font-semibold text-rose-700 shadow-sm dark:bg-background/70 dark:text-rose-200">
              {t('home.funDiscovery.giftFinderBadge')}
            </span>
            <h3 className="mt-4 font-heading text-2xl font-bold tracking-tight text-slate-950 dark:text-foreground">
              {t('home.funDiscovery.giftFinderTitle')}
            </h3>
            <p className="mt-3 max-w-md text-sm leading-7 text-slate-600 dark:text-muted-foreground sm:text-base">
              {t('home.funDiscovery.giftFinderDescription')}
            </p>
            <Link to="/gift-finder" className={`${ctaClassName} mt-auto pt-6`}>
              {t('home.funDiscovery.giftFinderCta')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
        </article>

        <article
          className={`${cardBaseClassName} border-amber-200/70 bg-gradient-to-br from-amber-50/95 via-card to-rose-50/80 dark:border-amber-500/20 dark:from-amber-500/10 dark:via-card dark:to-rose-500/10 lg:col-span-4`}
        >
          <div className="absolute -right-8 top-0 size-28 rounded-full bg-pink-200/45 blur-2xl dark:bg-pink-400/10" />
          <div className="absolute bottom-0 left-10 size-24 rounded-full bg-amber-200/55 blur-2xl dark:bg-amber-400/10" />
          <div className="relative flex h-full flex-col">
            <span className="flex size-12 items-center justify-center rounded-2xl bg-white/80 text-amber-700 shadow-sm dark:bg-background/70 dark:text-amber-300">
              <Gift className="size-6" />
            </span>
            <span className="mt-4 inline-flex w-fit rounded-full bg-white/80 px-3 py-1 text-xs font-semibold text-amber-700 shadow-sm dark:bg-background/70 dark:text-amber-200">
              {t('home.couponGame.dailyLimit')}
            </span>
            <h3 className="mt-4 font-heading text-2xl font-bold tracking-tight text-slate-950 dark:text-foreground">
              {t('home.funDiscovery.couponGameTitle')}
            </h3>
            <p className="mt-3 max-w-md text-sm leading-7 text-slate-600 dark:text-muted-foreground sm:text-base">
              {t('home.funDiscovery.couponGameDescription')}
            </p>
            <Link to="/coupon-game" className={`${ctaClassName} mt-auto pt-6`}>
              {t('home.funDiscovery.couponGameCta')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
        </article>

        <article
          className={`${cardBaseClassName} border-border/70 bg-card lg:col-span-7`}
        >
          <div className="absolute -right-10 top-0 size-28 rounded-full bg-primary/10 blur-2xl" />
          <div className="relative flex h-full flex-col">
            <span className="flex size-12 items-center justify-center rounded-2xl bg-primary/10 text-primary">
              <History className="size-6" />
            </span>
            <h3 className="mt-4 font-heading text-2xl font-bold tracking-tight">
              {t('home.funDiscovery.recentTitle')}
            </h3>
            <p className="mt-3 max-w-xl text-sm leading-7 text-muted-foreground sm:text-base">
              {t('home.funDiscovery.recentDescription')}
            </p>

            <div className="mt-5 space-y-3">
              {recentlyViewedBooks.length > 0 ? (
                recentlyViewedBooks.map((book, index) => (
                  <Link
                    key={book.id}
                    to={`/books/${book.id}`}
                    className="group/item flex items-center gap-3 rounded-2xl border border-border/70 bg-background/80 p-3 transition-all duration-200 hover:-translate-y-0.5 hover:border-primary/40 hover:bg-background"
                  >
                    <div className="relative h-16 w-12 shrink-0 overflow-hidden rounded-xl border border-border/60 bg-muted">
                      <img
                        src={getBookCoverUrl(book.cover)}
                        alt={book.title}
                        className="absolute inset-0 size-full object-cover"
                      />
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-primary/70">
                        {t('home.funDiscovery.recentItemLabel')} {index + 1}
                      </p>
                      <p className="truncate font-semibold">{book.title}</p>
                      <p className="truncate text-sm text-muted-foreground">
                        {book.author}
                      </p>
                    </div>
                    <ArrowRight className="size-4 shrink-0 text-muted-foreground transition-transform group-hover/item:translate-x-0.5 group-hover/item:text-primary" />
                  </Link>
                ))
              ) : (
                <div className="rounded-2xl border border-dashed border-border/70 bg-muted/40 px-4 py-6">
                  <p className="font-heading text-base font-semibold">
                    {t('home.funDiscovery.recentEmptyTitle')}
                  </p>
                  <p className="mt-2 text-sm leading-6 text-muted-foreground">
                    {t('home.funDiscovery.recentEmptyDescription')}
                  </p>
                </div>
              )}
            </div>

            <Link
              to={newestRecentBook ? `/books/${newestRecentBook.id}` : '/books'}
              className={`${ctaClassName} mt-auto pt-6`}
            >
              {t(
                recentlyViewedBooks.length > 0
                  ? 'home.funDiscovery.recentCta'
                  : 'home.funDiscovery.recentEmptyCta',
              )}
              <ArrowRight className="size-4" />
            </Link>
          </div>
        </article>

        <article
          className={`${cardBaseClassName} border-rose-200/70 bg-gradient-to-br from-rose-50/90 via-card to-orange-50/80 dark:border-rose-500/20 dark:from-rose-500/10 dark:via-card dark:to-orange-500/10 lg:col-span-5`}
        >
          <div className="absolute -right-10 bottom-0 size-28 rounded-full bg-rose-200/45 blur-2xl dark:bg-rose-400/10" />
          <div className="relative flex h-full flex-col">
            <span className="flex size-12 items-center justify-center rounded-2xl bg-white/80 text-rose-700 shadow-sm dark:bg-background/70 dark:text-rose-300">
              <Heart className="size-6" />
            </span>
            <h3 className="mt-4 font-heading text-2xl font-bold tracking-tight text-slate-950 dark:text-foreground">
              {t('home.funDiscovery.wishlistTitle')}
            </h3>
            <p className="mt-3 max-w-md text-sm leading-7 text-slate-600 dark:text-muted-foreground sm:text-base">
              {t('home.funDiscovery.wishlistDescription')}
            </p>
            <Link to="/wishlist" className={`${ctaClassName} mt-auto pt-6`}>
              {t('home.funDiscovery.wishlistCta')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
        </article>

        <article
          className={`${cardBaseClassName} border-violet-200/70 bg-gradient-to-br from-violet-50/90 via-card to-sky-50/80 dark:border-violet-500/20 dark:from-violet-500/10 dark:via-card dark:to-sky-500/10 lg:col-span-12`}
        >
          <div className="absolute -right-10 top-0 size-32 rounded-full bg-violet-200/45 blur-2xl dark:bg-violet-400/10" />
          <div className="absolute bottom-0 left-12 size-24 rounded-full bg-sky-200/55 blur-2xl dark:bg-sky-400/10" />
          <div className="relative flex h-full flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-2xl">
              <span className="flex size-12 items-center justify-center rounded-2xl bg-white/80 text-violet-700 shadow-sm dark:bg-background/70 dark:text-violet-300">
                <Trophy className="size-6" />
              </span>
              <div className="mt-4 flex flex-wrap gap-2">
                <span className="inline-flex w-fit rounded-full bg-white/80 px-3 py-1 text-xs font-semibold text-violet-700 shadow-sm dark:bg-background/70 dark:text-violet-200">
                  {t('home.funDiscovery.readingChallengeBadge')}
                </span>
                {readingChallenge ? (
                  <span className="inline-flex w-fit rounded-full bg-white/80 px-3 py-1 text-xs font-semibold text-slate-700 shadow-sm dark:bg-background/70 dark:text-slate-200">
                    {t('home.funDiscovery.readingChallengeProgress', {
                      completed: readingChallenge.completedBooks,
                      target: readingChallenge.targetBooks,
                    })}
                  </span>
                ) : null}
              </div>
              <h3 className="mt-4 font-heading text-2xl font-bold tracking-tight text-slate-950 dark:text-foreground">
                {readingChallenge
                  ? readingChallenge.title
                  : t('home.funDiscovery.readingChallengeTitle')}
              </h3>
              <p className="mt-3 max-w-2xl text-sm leading-7 text-slate-600 dark:text-muted-foreground sm:text-base">
                {readingChallenge
                  ? t('home.funDiscovery.readingChallengeActiveDescription', {
                      status:
                        readingChallengeStatus
                          ? getReadingChallengeStatusLabel(
                              readingChallengeStatus,
                              t,
                            )
                          : '',
                      progress: `${readingChallengeProgress}%`,
                    })
                  : t('home.funDiscovery.readingChallengeDescription')}
              </p>
            </div>

            <div className="flex flex-col gap-3 lg:min-w-[16rem] lg:items-end">
              <Link
                to="/reading-challenge"
                className="inline-flex items-center gap-2 rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-slate-900/90"
              >
                {t(
                  readingChallenge
                    ? 'home.funDiscovery.readingChallengeActiveCta'
                    : 'home.funDiscovery.readingChallengeCta',
                )}
                <ArrowRight className="size-4" />
              </Link>
              <p className="text-sm text-slate-600 dark:text-muted-foreground">
                {readingChallenge
                  ? t('home.funDiscovery.readingChallengeActiveHint')
                  : t('home.funDiscovery.readingChallengeHint')}
              </p>
            </div>
          </div>
        </article>
      </div>
    </section>
  )
}

function getReadingChallengeStatusLabel(
  status: ReturnType<typeof getReadingChallengeStatus>,
  t: (key: string) => string,
) {
  switch (status) {
    case 'NOT_STARTED':
      return t('readingChallengePage.status.notStarted')
    case 'IN_PROGRESS':
      return t('readingChallengePage.status.inProgress')
    case 'NEAR_COMPLETION':
      return t('readingChallengePage.status.nearCompletion')
    case 'COMPLETED':
      return t('readingChallengePage.status.completed')
    case 'OVERDUE':
      return t('readingChallengePage.status.overdue')
  }
}

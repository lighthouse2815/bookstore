import { useRef, type ReactNode } from 'react'
import {
  BookOpen,
  Compass,
  HeartHandshake,
  Hourglass,
  LoaderCircle,
  type LucideIcon,
  MoonStar,
  PiggyBank,
  RefreshCw,
  Sparkles,
  Wallet,
  GraduationCap,
  Coffee,
  Clock3,
  Gem,
} from 'lucide-react'
import { BookCard } from '@/components/book/book-card'
import {
  ChoiceCard,
  StatePanel,
  StepCard,
  SummaryField,
} from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useBookMatch } from '@/hooks/use-book-match'
import {
  isBookMatchReady,
  BOOK_MATCH_RESULT_LIMIT,
} from '@/services/book-match-service'
import { cn } from '@/utils'
import type {
  BookMatchBudget,
  BookMatchMood,
  BookMatchReadingTime,
  BookMatchReason,
} from '@/types/book-match'

type MatchOption<Value> = {
  value: Value
  icon: LucideIcon
  titleKey: string
  descriptionKey: string
  accentClassName: string
}

const moodOptions: MatchOption<BookMatchMood>[] = [
  {
    value: 'RELAX',
    icon: Coffee,
    titleKey: 'book.match.moods.RELAX.label',
    descriptionKey: 'book.match.moods.RELAX.description',
    accentClassName: 'bg-amber-500/15 text-amber-700 dark:text-amber-300',
  },
  {
    value: 'STUDY',
    icon: GraduationCap,
    titleKey: 'book.match.moods.STUDY.label',
    descriptionKey: 'book.match.moods.STUDY.description',
    accentClassName: 'bg-sky-500/15 text-sky-700 dark:text-sky-300',
  },
  {
    value: 'ADVENTURE',
    icon: Compass,
    titleKey: 'book.match.moods.ADVENTURE.label',
    descriptionKey: 'book.match.moods.ADVENTURE.description',
    accentClassName:
      'bg-emerald-500/15 text-emerald-700 dark:text-emerald-300',
  },
  {
    value: 'MYSTERY',
    icon: MoonStar,
    titleKey: 'book.match.moods.MYSTERY.label',
    descriptionKey: 'book.match.moods.MYSTERY.description',
    accentClassName: 'bg-slate-500/15 text-slate-700 dark:text-slate-300',
  },
  {
    value: 'HEALING',
    icon: HeartHandshake,
    titleKey: 'book.match.moods.HEALING.label',
    descriptionKey: 'book.match.moods.HEALING.description',
    accentClassName: 'bg-rose-500/15 text-rose-700 dark:text-rose-300',
  },
]

const budgetOptions: MatchOption<BookMatchBudget>[] = [
  {
    value: 'UNDER_100',
    icon: PiggyBank,
    titleKey: 'book.match.budgets.UNDER_100.label',
    descriptionKey: 'book.match.budgets.UNDER_100.description',
    accentClassName: 'bg-lime-500/15 text-lime-700 dark:text-lime-300',
  },
  {
    value: 'FROM_100_TO_200',
    icon: Wallet,
    titleKey: 'book.match.budgets.FROM_100_TO_200.label',
    descriptionKey: 'book.match.budgets.FROM_100_TO_200.description',
    accentClassName: 'bg-orange-500/15 text-orange-700 dark:text-orange-300',
  },
  {
    value: 'ABOVE_200',
    icon: Gem,
    titleKey: 'book.match.budgets.ABOVE_200.label',
    descriptionKey: 'book.match.budgets.ABOVE_200.description',
    accentClassName:
      'bg-fuchsia-500/15 text-fuchsia-700 dark:text-fuchsia-300',
  },
]

const readingTimeOptions: MatchOption<BookMatchReadingTime>[] = [
  {
    value: 'SHORT',
    icon: BookOpen,
    titleKey: 'book.match.readingTimes.SHORT.label',
    descriptionKey: 'book.match.readingTimes.SHORT.description',
    accentClassName: 'bg-cyan-500/15 text-cyan-700 dark:text-cyan-300',
  },
  {
    value: 'MEDIUM',
    icon: Clock3,
    titleKey: 'book.match.readingTimes.MEDIUM.label',
    descriptionKey: 'book.match.readingTimes.MEDIUM.description',
    accentClassName: 'bg-violet-500/15 text-violet-700 dark:text-violet-300',
  },
  {
    value: 'LONG',
    icon: Hourglass,
    titleKey: 'book.match.readingTimes.LONG.label',
    descriptionKey: 'book.match.readingTimes.LONG.description',
    accentClassName: 'bg-red-500/15 text-red-700 dark:text-red-300',
  },
]

const reasonKeyMap: Record<BookMatchReason, string> = {
  MOOD: 'book.match.reasons.MOOD',
  BUDGET: 'book.match.reasons.BUDGET',
  READING_TIME: 'book.match.reasons.READING_TIME',
  HIGH_RATING: 'book.match.reasons.HIGH_RATING',
  POPULAR_PICK: 'book.match.reasons.POPULAR_PICK',
  FRESH_PICK: 'book.match.reasons.FRESH_PICK',
}

export default function BookMatchPage() {
  const { t, formatNumber } = useLanguage()
  const {
    answers,
    recommendations,
    isSubmitting,
    hasSubmitted,
    isCatalogEmpty,
    hasWeakPageCountHint,
    error,
    selectMood,
    selectBudget,
    selectReadingTime,
    submit,
    resetQuiz,
  } = useBookMatch()
  const resultsRef = useRef<HTMLElement | null>(null)
  const completedStepCount = [answers.mood, answers.budget, answers.readingTime]
    .filter(Boolean).length
  const progressPercent = (completedStepCount / 3) * 100
  const currentStepLabel = Math.min(completedStepCount + 1, 3)

  const selectedLabels = {
    mood:
      moodOptions.find((option) => option.value === answers.mood)?.titleKey ??
      null,
    budget:
      budgetOptions.find((option) => option.value === answers.budget)
        ?.titleKey ?? null,
    readingTime:
      readingTimeOptions.find((option) => option.value === answers.readingTime)
        ?.titleKey ?? null,
  }

  async function handleSubmit() {
    await submit()

    requestAnimationFrame(() => {
      const behavior = window.matchMedia('(prefers-reduced-motion: reduce)')
        .matches
        ? 'auto'
        : 'smooth'

      resultsRef.current?.scrollIntoView({ behavior, block: 'start' })
    })
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="flex-1 bg-[radial-gradient(circle_at_top_right,_rgba(244,114,182,0.12),_transparent_28%),radial-gradient(circle_at_top_left,_rgba(59,130,246,0.12),_transparent_26%),linear-gradient(180deg,_rgba(248,250,252,0.96),_rgba(255,255,255,1))] dark:bg-[radial-gradient(circle_at_top_right,_rgba(244,114,182,0.08),_transparent_30%),radial-gradient(circle_at_top_left,_rgba(59,130,246,0.08),_transparent_28%),linear-gradient(180deg,_rgba(20,22,31,1),_rgba(12,12,19,1))]">
        <div className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-4 py-8 sm:px-6 lg:px-8 lg:py-10">
          <section className="relative overflow-hidden rounded-[2rem] border border-border/70 bg-card/90 px-6 py-8 shadow-[0_24px_80px_rgba(15,23,42,0.08)] dark:border-white/10 dark:shadow-[0_24px_80px_rgba(0,0,0,0.28)] sm:px-8 lg:px-10">
            <div className="absolute -right-12 top-6 size-32 rounded-full bg-primary/10 blur-2xl" />
            <div className="absolute -left-10 bottom-0 size-28 rounded-full bg-accent/15 blur-2xl" />
            <div className="relative grid gap-8 lg:grid-cols-[minmax(0,1fr)_260px] lg:items-end">
              <div className="max-w-3xl">
                <span className="inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-1.5 text-sm font-semibold text-primary">
                  <Sparkles className="size-4" />
                  {t('book.match.heroBadge')}
                </span>
                <h1 className="mt-4 font-heading text-3xl font-bold tracking-tight text-balance sm:text-4xl">
                  {t('book.match.title')}
                </h1>
                <p className="mt-3 max-w-2xl text-base leading-7 text-muted-foreground sm:text-lg">
                  {t('book.match.description')}
                </p>
              </div>

              <div className="rounded-3xl border border-border/70 bg-background/80 p-5 backdrop-blur dark:border-white/10 dark:bg-background/55">
                <div className="flex items-center justify-between text-sm font-medium">
                  <span>{t('book.match.progressTitle')}</span>
                  <span>
                    {completedStepCount === 3
                      ? t('book.match.progressReady')
                      : t('book.match.progressStep', {
                          current: currentStepLabel,
                        })}
                  </span>
                </div>
                <div className="mt-4 h-2 overflow-hidden rounded-full bg-muted">
                  <div
                    className="motion-progress-fill h-full rounded-full bg-primary"
                    style={{ transform: `scaleX(${progressPercent / 100})` }}
                  />
                </div>
                <div className="mt-4 grid grid-cols-3 gap-2 text-xs text-muted-foreground">
                  <ProgressPill
                    label={t('book.match.steps.moodLabel')}
                    isDone={Boolean(answers.mood)}
                  />
                  <ProgressPill
                    label={t('book.match.steps.budgetLabel')}
                    isDone={Boolean(answers.budget)}
                  />
                  <ProgressPill
                    label={t('book.match.steps.readingTimeLabel')}
                    isDone={Boolean(answers.readingTime)}
                  />
                </div>
              </div>
            </div>
          </section>

          <section className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_340px] lg:items-start">
            <div className="space-y-5">
              <StepCard
                stepNumber={1}
                title={t('book.match.steps.moodTitle')}
                description={t('book.match.steps.moodDescription')}
              >
                <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                  {moodOptions.map((option) => (
                    <ChoiceCard
                      key={option.value}
                      icon={option.icon}
                      title={t(option.titleKey)}
                      description={t(option.descriptionKey)}
                      accentClassName={option.accentClassName}
                      isSelected={answers.mood === option.value}
                      onClick={() => selectMood(option.value)}
                    />
                  ))}
                </div>
              </StepCard>

              <StepCard
                stepNumber={2}
                title={t('book.match.steps.budgetTitle')}
                description={t('book.match.steps.budgetDescription')}
              >
                <div className="grid gap-3 sm:grid-cols-3">
                  {budgetOptions.map((option) => (
                    <ChoiceCard
                      key={option.value}
                      icon={option.icon}
                      title={t(option.titleKey)}
                      description={t(option.descriptionKey)}
                      accentClassName={option.accentClassName}
                      isSelected={answers.budget === option.value}
                      onClick={() => selectBudget(option.value)}
                    />
                  ))}
                </div>
              </StepCard>

              <StepCard
                stepNumber={3}
                title={t('book.match.steps.readingTimeTitle')}
                description={t('book.match.steps.readingTimeDescription')}
              >
                <div className="grid gap-3 sm:grid-cols-3">
                  {readingTimeOptions.map((option) => (
                    <ChoiceCard
                      key={option.value}
                      icon={option.icon}
                      title={t(option.titleKey)}
                      description={t(option.descriptionKey)}
                      accentClassName={option.accentClassName}
                      isSelected={answers.readingTime === option.value}
                      onClick={() => selectReadingTime(option.value)}
                    />
                  ))}
                </div>
              </StepCard>
            </div>

            <aside className="lg:sticky lg:top-24">
              <div className="overflow-hidden rounded-[1.75rem] border border-border/70 bg-card p-5 shadow-[0_18px_50px_rgba(15,23,42,0.07)] dark:border-white/10 dark:shadow-[0_18px_50px_rgba(0,0,0,0.24)]">
                <div className="flex items-center gap-3">
                  <span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                    <Sparkles className="size-5" />
                  </span>
                  <div>
                    <p className="font-heading text-lg font-semibold">
                      {t('book.match.sidebarTitle')}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {t('book.match.sidebarDescription', {
                        count: BOOK_MATCH_RESULT_LIMIT,
                      })}
                    </p>
                  </div>
                </div>

                <div className="mt-5 space-y-3">
                  <SummaryField
                    label={t('book.match.summary.mood')}
                    value={
                      selectedLabels.mood
                        ? t(selectedLabels.mood)
                        : t('book.match.summary.pending')
                    }
                  />
                  <SummaryField
                    label={t('book.match.summary.budget')}
                    value={
                      selectedLabels.budget
                        ? t(selectedLabels.budget)
                        : t('book.match.summary.pending')
                    }
                  />
                  <SummaryField
                    label={t('book.match.summary.readingTime')}
                    value={
                      selectedLabels.readingTime
                        ? t(selectedLabels.readingTime)
                        : t('book.match.summary.pending')
                    }
                  />
                </div>

                <button
                  type="button"
                  onClick={() => {
                    void handleSubmit()
                  }}
                  disabled={!isBookMatchReady(answers) || isSubmitting}
                  className="mt-6 inline-flex w-full items-center justify-center gap-2 rounded-full bg-primary px-5 py-3.5 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmitting ? (
                    <>
                      <LoaderCircle className="size-4 animate-spin" />
                      {t('book.match.submitLoading')}
                    </>
                  ) : (
                    <>
                      <Sparkles className="size-4" />
                      {t('book.match.submit')}
                    </>
                  )}
                </button>

                <button
                  type="button"
                  onClick={resetQuiz}
                  className="mt-3 inline-flex w-full items-center justify-center gap-2 rounded-full border border-border bg-background px-5 py-3 text-sm font-semibold transition-colors hover:bg-muted"
                >
                  <RefreshCw className="size-4" />
                  {t('book.match.reset')}
                </button>
              </div>
            </aside>
          </section>

          <section ref={resultsRef} className="space-y-5">
            <div className="flex flex-col gap-2">
              <h2 className="font-heading text-2xl font-bold tracking-tight">
                {t('book.match.resultsTitle')}
              </h2>
              <p className="text-sm text-muted-foreground">
                {t('book.match.resultsDescription')}
              </p>
              {hasWeakPageCountHint ? (
                <p className="text-xs leading-6 text-muted-foreground">
                  {t('book.match.weakDataHint')}
                </p>
              ) : null}
            </div>

            {error ? (
              <StatePanel
                title={t('book.match.errorTitle')}
                description={error}
                action={
                  <button
                    type="button"
                    onClick={() => {
                      void handleSubmit()
                    }}
                    className="inline-flex items-center justify-center rounded-2xl bg-primary px-5 py-3 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
                  >
                    {t('book.match.errorRetry')}
                  </button>
                }
              />
            ) : null}

            {!error && hasSubmitted && isCatalogEmpty ? (
              <StatePanel
                title={t('book.match.catalogEmptyTitle')}
                description={t('book.match.catalogEmptyDescription')}
              />
            ) : null}

            {!error &&
            hasSubmitted &&
            !isSubmitting &&
            !isCatalogEmpty &&
            recommendations.length === 0 ? (
              <StatePanel
                title={t('book.match.emptyTitle')}
                description={t('book.match.emptyDescription')}
              />
            ) : null}

            {!error && recommendations.length > 0 ? (
              <>
                <div className="flex flex-wrap items-center gap-3">
                  <span className="inline-flex rounded-full bg-primary/10 px-3 py-1 text-sm font-semibold text-primary">
                    {t('book.match.resultsCount', {
                      count: formatNumber(recommendations.length),
                    })}
                  </span>
                  {selectedLabels.mood ? (
                    <FilterChip label={t(selectedLabels.mood)} />
                  ) : null}
                  {selectedLabels.budget ? (
                    <FilterChip label={t(selectedLabels.budget)} />
                  ) : null}
                  {selectedLabels.readingTime ? (
                    <FilterChip label={t(selectedLabels.readingTime)} />
                  ) : null}
                </div>

                <div className="motion-result grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
                  {recommendations.map((recommendation) => (
                    <div
                      key={recommendation.book.id}
                      className="motion-card space-y-3 rounded-[1.5rem] border border-border/70 bg-card/90 p-3 shadow-sm dark:border-white/10 dark:shadow-none"
                    >
                      <BookCard book={recommendation.book} />
                      {recommendation.reasons.length > 0 ? (
                        <div className="flex flex-wrap gap-2">
                          {recommendation.reasons.map((reason) => (
                            <span
                              key={reason}
                              className="rounded-full bg-muted px-2.5 py-1 text-xs font-medium text-muted-foreground"
                            >
                              {t(reasonKeyMap[reason])}
                            </span>
                          ))}
                        </div>
                      ) : null}
                    </div>
                  ))}
                </div>
              </>
            ) : null}
          </section>
        </div>
      </main>
      <Footer />
    </div>
  )
}

function ProgressPill({
  label,
  isDone,
}: {
  label: string
  isDone: boolean
}) {
  return (
    <span
      className={cn(
        'rounded-full px-2.5 py-1 text-center font-medium',
        isDone
          ? 'bg-primary/12 text-primary'
          : 'bg-muted text-muted-foreground',
      )}
    >
      {label}
    </span>
  )
}

function FilterChip({ label }: { label: string }) {
  return (
    <span className="rounded-full border border-border bg-background px-3 py-1 text-sm font-medium text-muted-foreground">
      {label}
    </span>
  )
}

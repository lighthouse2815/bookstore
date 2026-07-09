import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  BookOpen,
  CalendarDays,
  Clock3,
  Minus,
  PencilLine,
  Plus,
  RotateCcw,
  Sparkles,
  Target,
  Trash2,
  Trophy,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import {
  StatePanel,
  StatPill,
  primaryButtonClassName,
  secondaryButtonClassName,
} from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useReadingChallenge } from '@/hooks/use-reading-challenge'
import type {
  ReadingChallenge,
  ReadingChallengePreset,
  ReadingChallengeStatus,
} from '@/types/reading-challenge'
import {
  getReadingChallengeDaysRemaining,
  getReadingChallengeProgressPercent,
  getReadingChallengeStatus,
  inferReadingChallengePreset,
  isReadingChallengeUrgent,
  resolveReadingChallengeEndDate,
} from '@/utils/reading-challenge'

type ReadingChallengeFormState = {
  title: string
  targetBooks: string
  preset: ReadingChallengePreset
  customEndDate: string
}

const PRESET_OPTIONS: ReadingChallengePreset[] = [
  'WEEK',
  'MONTH',
  'YEAR',
  'CUSTOM',
]

export default function ReadingChallengePage() {
  const { t, formatDate, formatNumber } = useLanguage()
  const {
    challenge,
    error,
    saveChallenge,
    incrementCompletedBooks,
    decrementCompletedBooks,
    resetCompletedBooks,
    deleteChallenge,
    clearError,
  } = useReadingChallenge()
  const [formState, setFormState] = useState<ReadingChallengeFormState>(
    () => createFormState(),
  )
  const [formError, setFormError] = useState<string | null>(null)

  useEffect(() => {
    setFormState(createFormState(challenge))
    setFormError(null)
  }, [challenge])

  const progressPercent = challenge
    ? getReadingChallengeProgressPercent(challenge)
    : 0
  const status = challenge ? getReadingChallengeStatus(challenge) : null
  const daysRemaining = challenge
    ? getReadingChallengeDaysRemaining(challenge.endDate)
    : null
  const booksLeft = challenge
    ? Math.max(challenge.targetBooks - challenge.completedBooks, 0)
    : 0
  const isUrgent = challenge ? isReadingChallengeUrgent(challenge) : false
  const deadlinePreview = getDeadlinePreview(formState, challenge?.startDate)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFormError(null)

    const result = saveChallenge({
      title: formState.title,
      targetBooks: Number(formState.targetBooks),
      preset: formState.preset,
      endDate:
        formState.preset === 'CUSTOM' ? formState.customEndDate : undefined,
    })

    if (!result.success) {
      setFormError(getReadingChallengeErrorMessage(result.errorCode, t))
    }
  }

  function handleDeleteChallenge() {
    if (!challenge) {
      return
    }

    if (
      typeof window !== 'undefined' &&
      !window.confirm(t('readingChallengePage.deleteConfirm'))
    ) {
      return
    }

    deleteChallenge()
  }

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(251,207,232,0.18),rgba(255,255,255,0.96)_24%,rgba(224,242,254,0.82)_100%)]">
      <Header />
      <main className="flex-1">
        <section className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
          <div className="relative overflow-hidden rounded-[2rem] border border-rose-200/70 bg-white/88 px-6 py-8 shadow-[0_30px_90px_rgba(236,72,153,0.12)] backdrop-blur sm:px-8 lg:px-10">
            <div className="pointer-events-none absolute -right-10 top-0 size-44 rounded-full bg-pink-200/45 blur-3xl" />
            <div className="pointer-events-none absolute bottom-0 left-0 size-48 rounded-full bg-sky-200/55 blur-3xl" />

            <div className="relative flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
              <div className="max-w-3xl">
                <span className="inline-flex items-center gap-2 rounded-full border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-semibold text-rose-700">
                  <Sparkles className="size-4" />
                  {t('readingChallengePage.badge')}
                </span>
                <h1 className="mt-4 font-heading text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">
                  {t('readingChallengePage.title')}
                </h1>
                <p className="mt-4 max-w-2xl text-sm leading-7 text-slate-600 sm:text-base">
                  {t('readingChallengePage.description')}
                </p>
              </div>

              <div className="flex flex-wrap gap-3">
                <StatPill
                  label={t('readingChallengePage.localStorageLabel')}
                  value={t('readingChallengePage.localStorageValue')}
                  className="border-rose-100 bg-white/78 dark:border-rose-300/15 dark:bg-background/45"
                />
                <StatPill
                  label={t('readingChallengePage.scopeLabel')}
                  value={t('readingChallengePage.scopeValue')}
                  className="border-rose-100 bg-white/78 dark:border-rose-300/15 dark:bg-background/45"
                />
                <StatPill
                  label={t('readingChallengePage.goalLabel')}
                  value={
                    challenge
                      ? t('readingChallengePage.goalValue', {
                          target: formatNumber(challenge.targetBooks),
                        })
                      : t('readingChallengePage.goalEmptyValue')
                  }
                  className="border-rose-100 bg-white/78 dark:border-rose-300/15 dark:bg-background/45"
                />
              </div>
            </div>
          </div>
        </section>

        <section className="mx-auto grid w-full max-w-7xl gap-6 px-4 pb-12 sm:px-6 lg:grid-cols-[minmax(0,1.08fr)_minmax(320px,0.92fr)] lg:px-8">
          <div className="rounded-[2rem] border border-slate-200/80 bg-white/92 p-6 shadow-[0_24px_70px_rgba(15,23,42,0.08)] sm:p-8">
            {error ? (
              <InlineNotice
                tone="warning"
                title={t('readingChallengePage.storageErrorTitle')}
                description={t('readingChallengePage.storageErrorDescription')}
                actionLabel={t('readingChallengePage.storageErrorDismiss')}
                onAction={clearError}
              />
            ) : null}

            {challenge ? (
              <>
                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <div className="flex flex-wrap items-center gap-3">
                      <Badge
                        className={getStatusBadgeClassName(status)}
                      >
                        {status ? getStatusLabel(status, t) : null}
                      </Badge>
                      <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
                        {t('readingChallengePage.progressOfGoal', {
                          completed: formatNumber(challenge.completedBooks),
                          target: formatNumber(challenge.targetBooks),
                        })}
                      </span>
                    </div>
                    <h2 className="mt-4 font-heading text-3xl font-bold tracking-tight text-slate-900">
                      {challenge.title}
                    </h2>
                    <p className="mt-3 max-w-2xl text-sm leading-7 text-slate-600 sm:text-base">
                      {t('readingChallengePage.progressDescription')}
                    </p>
                  </div>

                  <div className="rounded-[1.5rem] border border-rose-100 bg-rose-50/75 px-5 py-4 text-left shadow-sm">
                    <p className="text-xs font-semibold uppercase tracking-[0.22em] text-rose-600">
                      {t('readingChallengePage.progressPercentLabel')}
                    </p>
                    <p className="mt-2 font-heading text-3xl font-bold text-rose-700">
                      {progressPercent}%
                    </p>
                  </div>
                </div>

                <div className="mt-6">
                  <div className="flex items-center justify-between text-sm font-medium text-slate-600">
                    <span>{t('readingChallengePage.progressBarLabel')}</span>
                    <span>{progressPercent}%</span>
                  </div>
                  <div className="mt-3 h-3 overflow-hidden rounded-full bg-slate-100">
                    <div
                      className="h-full rounded-full bg-[linear-gradient(90deg,#f43f5e,#fb7185,#38bdf8)] transition-[width] duration-300"
                      style={{ width: `${progressPercent}%` }}
                    />
                  </div>
                </div>

                <div className="mt-6 grid gap-3 sm:grid-cols-3">
                  <StatCard
                    icon={BookOpen}
                    label={t('readingChallengePage.completedLabel')}
                    value={formatNumber(challenge.completedBooks)}
                    detail={t('readingChallengePage.booksLeftValue', {
                      count: formatNumber(booksLeft),
                    })}
                  />
                  <StatCard
                    icon={Target}
                    label={t('readingChallengePage.targetLabelCard')}
                    value={formatNumber(challenge.targetBooks)}
                    detail={t('readingChallengePage.startedAtValue', {
                      date: formatDate(parseDateOnly(challenge.startDate)),
                    })}
                  />
                  <StatCard
                    icon={Clock3}
                    label={t('readingChallengePage.deadlineLabel')}
                    value={
                      daysRemaining === null
                        ? '--'
                        : getDaysRemainingLabel(daysRemaining, t)
                    }
                    detail={t('readingChallengePage.endsAtValue', {
                      date: formatDate(parseDateOnly(challenge.endDate)),
                    })}
                  />
                </div>

                {status === 'COMPLETED' ? (
                  <InlineNotice
                    tone="success"
                    title={t('readingChallengePage.completedTitle')}
                    description={t('readingChallengePage.completedDescription')}
                  />
                ) : null}

                {isUrgent && status !== 'COMPLETED' ? (
                  <InlineNotice
                    tone="warning"
                    title={t('readingChallengePage.urgentTitle')}
                    description={t('readingChallengePage.urgentDescription')}
                  />
                ) : null}

                {status === 'OVERDUE' ? (
                  <InlineNotice
                    tone="danger"
                    title={t('readingChallengePage.overdueTitle')}
                    description={t('readingChallengePage.overdueDescription')}
                  />
                ) : null}

                <div className="mt-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                  <Button
                    type="button"
                    onClick={incrementCompletedBooks}
                    className={`${primaryButtonClassName} bg-rose-600 text-white hover:bg-rose-600/90`}
                  >
                    <Plus className="size-4" />
                    {t('readingChallengePage.incrementButton')}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={decrementCompletedBooks}
                    className={`${secondaryButtonClassName} border-slate-300 bg-slate-100 text-slate-700 shadow-sm hover:bg-slate-200 dark:border-white/10 dark:bg-background/40 dark:text-foreground dark:hover:bg-background/55`}
                  >
                    <Minus className="size-4" />
                    {t('readingChallengePage.decrementButton')}
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={resetCompletedBooks}
                    className={`${secondaryButtonClassName} bg-secondary/85`}
                  >
                    <RotateCcw className="size-4" />
                    {t('readingChallengePage.resetProgressButton')}
                  </Button>
                  <Button
                    type="button"
                    variant="destructive"
                    onClick={handleDeleteChallenge}
                    className="h-11 rounded-2xl"
                  >
                    <Trash2 className="size-4" />
                    {t('readingChallengePage.deleteButton')}
                  </Button>
                </div>
              </>
            ) : (
              <StatePanel
                icon={<Trophy className="size-12 text-rose-500" />}
                title={t('readingChallengePage.emptyTitle')}
                description={t('readingChallengePage.emptyDescription')}
                tone="warning"
                minHeightClassName="min-h-[480px]"
              >
                <div className="mt-1 flex flex-wrap justify-center gap-3">
                  <Link
                    to="/books"
                    className="inline-flex h-11 items-center justify-center rounded-2xl border border-border bg-background px-6 text-sm font-semibold text-foreground transition-colors hover:bg-muted"
                  >
                    {t('readingChallengePage.emptyBrowseBooks')}
                  </Link>
                  <a
                    href="#reading-challenge-form"
                    className="inline-flex h-11 items-center justify-center rounded-2xl bg-rose-600 px-6 text-sm font-semibold text-white transition-colors hover:bg-rose-600/90"
                  >
                    {t('readingChallengePage.emptyCreateButton')}
                  </a>
                </div>
              </StatePanel>
            )}
          </div>

          <div
            id="reading-challenge-form"
            className="rounded-[2rem] border border-sky-200/70 bg-white/90 p-6 shadow-[0_24px_70px_rgba(14,165,233,0.1)] sm:p-8"
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-sm font-semibold uppercase tracking-[0.24em] text-sky-700">
                  {challenge
                    ? t('readingChallengePage.formEditBadge')
                    : t('readingChallengePage.formCreateBadge')}
                </p>
                <h2 className="mt-2 font-heading text-2xl font-bold text-slate-900">
                  {challenge
                    ? t('readingChallengePage.formEditTitle')
                    : t('readingChallengePage.formCreateTitle')}
                </h2>
                <p className="mt-3 text-sm leading-7 text-slate-600">
                  {challenge
                    ? t('readingChallengePage.formEditDescription')
                    : t('readingChallengePage.formCreateDescription')}
                </p>
              </div>
              <span className="rounded-full bg-sky-50 px-3 py-1 text-xs font-semibold text-sky-700">
                <PencilLine className="mr-1 inline size-3" />
                {t('readingChallengePage.localOnlyChip')}
              </span>
            </div>

            <form className="mt-6 space-y-5" onSubmit={handleSubmit}>
              <div className="space-y-2">
                <Label htmlFor="reading-challenge-title">
                  {t('readingChallengePage.titleLabel')}
                </Label>
                <Input
                  id="reading-challenge-title"
                  value={formState.title}
                  placeholder={t('readingChallengePage.titlePlaceholder')}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      title: event.target.value,
                    }))
                  }
                  className="h-11 rounded-2xl border-slate-200 px-4"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="reading-challenge-target">
                  {t('readingChallengePage.targetLabel')}
                </Label>
                <Input
                  id="reading-challenge-target"
                  type="number"
                  min={1}
                  step={1}
                  value={formState.targetBooks}
                  placeholder={t('readingChallengePage.targetPlaceholder')}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      targetBooks: event.target.value,
                    }))
                  }
                  className="h-11 rounded-2xl border-slate-200 px-4"
                />
              </div>

              <div className="space-y-3">
                <Label>{t('readingChallengePage.durationLabel')}</Label>
                <div className="grid gap-3 sm:grid-cols-2">
                  {PRESET_OPTIONS.map((preset) => (
                    <button
                      key={preset}
                      type="button"
                      onClick={() =>
                        setFormState((current) => ({
                          ...current,
                          preset,
                        }))
                      }
                      className={`rounded-[1.25rem] border px-4 py-3 text-left transition-all ${
                        formState.preset === preset
                          ? 'border-sky-400 bg-sky-50 text-sky-800 shadow-sm'
                          : 'border-slate-200 bg-white text-slate-600 hover:border-sky-200 hover:bg-sky-50/50'
                      }`}
                    >
                      <p className="font-semibold">
                        {t(getPresetLabelKey(preset))}
                      </p>
                      <p className="mt-1 text-sm leading-6 text-inherit/80">
                        {t(getPresetDescriptionKey(preset))}
                      </p>
                    </button>
                  ))}
                </div>
              </div>

              {formState.preset === 'CUSTOM' ? (
                <div className="space-y-2">
                  <Label htmlFor="reading-challenge-end-date">
                    {t('readingChallengePage.customEndDateLabel')}
                  </Label>
                  <Input
                    id="reading-challenge-end-date"
                    type="date"
                    value={formState.customEndDate}
                    onChange={(event) =>
                      setFormState((current) => ({
                        ...current,
                        customEndDate: event.target.value,
                      }))
                    }
                    className="h-11 rounded-2xl border-slate-200 px-4"
                  />
                </div>
              ) : (
                <div className="rounded-[1.5rem] border border-sky-100 bg-sky-50/70 p-4">
                  <p className="text-xs font-semibold uppercase tracking-[0.22em] text-sky-700">
                    {t('readingChallengePage.previewLabel')}
                  </p>
                  <p className="mt-2 text-sm font-semibold text-slate-900">
                    {deadlinePreview
                      ? t('readingChallengePage.previewValue', {
                          date: formatDate(deadlinePreview),
                        })
                      : t('readingChallengePage.previewFallback')}
                  </p>
                </div>
              )}

              {formError ? (
                <div className="rounded-[1.25rem] border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                  {formError}
                </div>
              ) : null}

              <Button
                type="submit"
                className={`${primaryButtonClassName} w-full bg-sky-600 px-6 text-white hover:bg-sky-600/90`}
              >
                {challenge
                  ? t('readingChallengePage.updateButton')
                  : t('readingChallengePage.createButton')}
              </Button>
            </form>

            <div className="mt-6 rounded-[1.5rem] border border-dashed border-slate-200 bg-slate-50/90 p-5">
              <p className="font-heading text-lg font-semibold text-slate-900">
                {t('readingChallengePage.formHintTitle')}
              </p>
              <p className="mt-2 text-sm leading-7 text-slate-600">
                {challenge
                  ? t('readingChallengePage.formHintEdit')
                  : t('readingChallengePage.formHintCreate')}
              </p>
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  )
}

function StatCard({
  icon: Icon,
  label,
  value,
  detail,
}: {
  icon: typeof BookOpen
  label: string
  value: string
  detail: string
}) {
  return (
    <div className="rounded-[1.5rem] border border-slate-200 bg-slate-50/80 p-4">
      <span className="flex size-10 items-center justify-center rounded-2xl bg-white text-slate-700 shadow-sm">
        <Icon className="size-5" />
      </span>
      <p className="mt-4 text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">
        {label}
      </p>
      <p className="mt-2 font-heading text-2xl font-bold text-slate-900">
        {value}
      </p>
      <p className="mt-2 text-sm leading-6 text-slate-600">{detail}</p>
    </div>
  )
}

function InlineNotice({
  tone,
  title,
  description,
  actionLabel,
  onAction,
}: {
  tone: 'success' | 'warning' | 'danger'
  title: string
  description: string
  actionLabel?: string
  onAction?: () => void
}) {
  const palette =
    tone === 'success'
      ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
      : tone === 'warning'
        ? 'border-amber-200 bg-amber-50 text-amber-800'
        : 'border-rose-200 bg-rose-50 text-rose-800'

  return (
    <div className={`mt-6 rounded-[1.5rem] border px-5 py-4 ${palette}`}>
      <p className="font-semibold">{title}</p>
      <p className="mt-2 text-sm leading-7">{description}</p>
      {actionLabel && onAction ? (
        <button
          type="button"
          onClick={onAction}
          className="mt-3 text-sm font-semibold underline underline-offset-4"
        >
          {actionLabel}
        </button>
      ) : null}
    </div>
  )
}

function createFormState(challenge?: ReadingChallenge | null): ReadingChallengeFormState {
  if (!challenge) {
    return {
      title: '',
      targetBooks: '',
      preset: 'MONTH',
      customEndDate: '',
    }
  }

  const preset = inferReadingChallengePreset(challenge.startDate, challenge.endDate)

  return {
    title: challenge.title,
    targetBooks: String(challenge.targetBooks),
    preset,
    customEndDate: preset === 'CUSTOM' ? challenge.endDate : '',
  }
}

function getDeadlinePreview(
  formState: ReadingChallengeFormState,
  startDate?: string,
) {
  if (formState.preset === 'CUSTOM') {
    return formState.customEndDate ? parseDateOnly(formState.customEndDate) : null
  }

  try {
    return parseDateOnly(
      resolveReadingChallengeEndDate(
        formState.preset,
        startDate ?? new Date(),
        formState.customEndDate,
      ),
    )
  } catch {
    return null
  }
}

function getStatusBadgeClassName(status: ReadingChallengeStatus | null) {
  switch (status) {
    case 'COMPLETED':
      return 'bg-emerald-100 text-emerald-700'
    case 'OVERDUE':
      return 'bg-rose-100 text-rose-700'
    case 'NEAR_COMPLETION':
      return 'bg-amber-100 text-amber-700'
    case 'IN_PROGRESS':
      return 'bg-sky-100 text-sky-700'
    case 'NOT_STARTED':
      return 'bg-slate-100 text-slate-700'
    default:
      return 'bg-slate-100 text-slate-700'
  }
}

function getStatusLabel(
  status: ReadingChallengeStatus,
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

function getDaysRemainingLabel(
  daysRemaining: number,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  if (daysRemaining < 0) {
    return t('readingChallengePage.daysOverdue', {
      count: Math.abs(daysRemaining),
    })
  }

  if (daysRemaining === 0) {
    return t('readingChallengePage.daysDueToday')
  }

  return t('readingChallengePage.daysRemaining', { count: daysRemaining })
}

function getPresetLabelKey(preset: ReadingChallengePreset) {
  switch (preset) {
    case 'WEEK':
      return 'readingChallengePage.presetWeek'
    case 'MONTH':
      return 'readingChallengePage.presetMonth'
    case 'YEAR':
      return 'readingChallengePage.presetYear'
    case 'CUSTOM':
      return 'readingChallengePage.presetCustom'
  }
}

function getPresetDescriptionKey(preset: ReadingChallengePreset) {
  switch (preset) {
    case 'WEEK':
      return 'readingChallengePage.presetWeekDescription'
    case 'MONTH':
      return 'readingChallengePage.presetMonthDescription'
    case 'YEAR':
      return 'readingChallengePage.presetYearDescription'
    case 'CUSTOM':
      return 'readingChallengePage.presetCustomDescription'
  }
}

function getReadingChallengeErrorMessage(
  errorCode: string,
  t: (key: string) => string,
) {
  switch (errorCode) {
    case 'READING_CHALLENGE_TITLE_REQUIRED':
      return t('readingChallengePage.errors.titleRequired')
    case 'READING_CHALLENGE_TARGET_INVALID':
      return t('readingChallengePage.errors.targetInvalid')
    case 'READING_CHALLENGE_END_DATE_REQUIRED':
      return t('readingChallengePage.errors.endDateRequired')
    case 'READING_CHALLENGE_END_DATE_INVALID':
      return t('readingChallengePage.errors.endDateInvalid')
    case 'READING_CHALLENGE_END_DATE_BEFORE_START':
      return t('readingChallengePage.errors.endDateBeforeStart')
    default:
      return t('readingChallengePage.errors.unknown')
  }
}

function parseDateOnly(value: string) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)
  if (!match) {
    return new Date(value)
  }

  return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]))
}

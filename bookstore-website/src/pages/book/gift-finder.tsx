import { useRef } from 'react'
import { Link } from 'react-router-dom'
import {
  BookOpen,
  Coffee,
  Compass,
  Gem,
  Gift,
  GraduationCap,
  HeartHandshake,
  LoaderCircle,
  RefreshCw,
  Sparkles,
  Wallet,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { toast } from 'sonner'
import {
  ChoiceCard,
  StatePanel,
  StepCard,
  SummaryField,
} from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useAuth } from '@/contexts/auth-context'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import { useWishlist } from '@/contexts/wishlist-context'
import { useGiftFinder } from '@/hooks/use-gift-finder'
import { isGiftFinderReady } from '@/services/gift-finder-service'
import { cn } from '@/utils'
import { getBookCoverUrl, setBookCoverFallback } from '@/utils/book-cover'
import { getCategoryLabel } from '@/utils/i18n'
import type { Book, BookCardData } from '@/types/book'
import type {
  GiftFinderBudget,
  GiftFinderOccasion,
  GiftFinderReason,
  GiftFinderRecipient,
  GiftFinderTone,
} from '@/types/gift-finder'

type GiftOption<Value> = {
  value: Value
  icon: LucideIcon
  titleKey: string
  descriptionKey: string
  accentClassName: string
}

const recipientOptions: GiftOption<GiftFinderRecipient>[] = [
  {
    value: 'BEST_FRIEND',
    icon: Sparkles,
    titleKey: 'book.giftFinder.recipients.BEST_FRIEND.label',
    descriptionKey: 'book.giftFinder.recipients.BEST_FRIEND.description',
    accentClassName: 'bg-sky-500/15 text-sky-700 dark:text-sky-300',
  },
  {
    value: 'PARTNER',
    icon: HeartHandshake,
    titleKey: 'book.giftFinder.recipients.PARTNER.label',
    descriptionKey: 'book.giftFinder.recipients.PARTNER.description',
    accentClassName: 'bg-rose-500/15 text-rose-700 dark:text-rose-300',
  },
  {
    value: 'PARENT',
    icon: BookOpen,
    titleKey: 'book.giftFinder.recipients.PARENT.label',
    descriptionKey: 'book.giftFinder.recipients.PARENT.description',
    accentClassName: 'bg-amber-500/15 text-amber-700 dark:text-amber-300',
  },
  {
    value: 'COLLEAGUE',
    icon: GraduationCap,
    titleKey: 'book.giftFinder.recipients.COLLEAGUE.label',
    descriptionKey: 'book.giftFinder.recipients.COLLEAGUE.description',
    accentClassName:
      'bg-emerald-500/15 text-emerald-700 dark:text-emerald-300',
  },
  {
    value: 'YOUNG_READER',
    icon: Compass,
    titleKey: 'book.giftFinder.recipients.YOUNG_READER.label',
    descriptionKey: 'book.giftFinder.recipients.YOUNG_READER.description',
    accentClassName: 'bg-violet-500/15 text-violet-700 dark:text-violet-300',
  },
]

const occasionOptions: GiftOption<GiftFinderOccasion>[] = [
  {
    value: 'BIRTHDAY',
    icon: Gift,
    titleKey: 'book.giftFinder.occasions.BIRTHDAY.label',
    descriptionKey: 'book.giftFinder.occasions.BIRTHDAY.description',
    accentClassName:
      'bg-fuchsia-500/15 text-fuchsia-700 dark:text-fuchsia-300',
  },
  {
    value: 'THANK_YOU',
    icon: HeartHandshake,
    titleKey: 'book.giftFinder.occasions.THANK_YOU.label',
    descriptionKey: 'book.giftFinder.occasions.THANK_YOU.description',
    accentClassName: 'bg-cyan-500/15 text-cyan-700 dark:text-cyan-300',
  },
  {
    value: 'CELEBRATION',
    icon: Sparkles,
    titleKey: 'book.giftFinder.occasions.CELEBRATION.label',
    descriptionKey: 'book.giftFinder.occasions.CELEBRATION.description',
    accentClassName: 'bg-orange-500/15 text-orange-700 dark:text-orange-300',
  },
  {
    value: 'ENCOURAGEMENT',
    icon: Compass,
    titleKey: 'book.giftFinder.occasions.ENCOURAGEMENT.label',
    descriptionKey: 'book.giftFinder.occasions.ENCOURAGEMENT.description',
    accentClassName: 'bg-lime-500/15 text-lime-700 dark:text-lime-300',
  },
]

const budgetOptions: GiftOption<GiftFinderBudget>[] = [
  {
    value: 'UNDER_150',
    icon: Wallet,
    titleKey: 'book.giftFinder.budgets.UNDER_150.label',
    descriptionKey: 'book.giftFinder.budgets.UNDER_150.description',
    accentClassName: 'bg-lime-500/15 text-lime-700 dark:text-lime-300',
  },
  {
    value: 'FROM_150_TO_300',
    icon: Gift,
    titleKey: 'book.giftFinder.budgets.FROM_150_TO_300.label',
    descriptionKey: 'book.giftFinder.budgets.FROM_150_TO_300.description',
    accentClassName: 'bg-orange-500/15 text-orange-700 dark:text-orange-300',
  },
  {
    value: 'ABOVE_300',
    icon: Gem,
    titleKey: 'book.giftFinder.budgets.ABOVE_300.label',
    descriptionKey: 'book.giftFinder.budgets.ABOVE_300.description',
    accentClassName:
      'bg-fuchsia-500/15 text-fuchsia-700 dark:text-fuchsia-300',
  },
]

const toneOptions: GiftOption<GiftFinderTone>[] = [
  {
    value: 'COZY',
    icon: Coffee,
    titleKey: 'book.giftFinder.tones.COZY.label',
    descriptionKey: 'book.giftFinder.tones.COZY.description',
    accentClassName: 'bg-amber-500/15 text-amber-700 dark:text-amber-300',
  },
  {
    value: 'INSPIRING',
    icon: Sparkles,
    titleKey: 'book.giftFinder.tones.INSPIRING.label',
    descriptionKey: 'book.giftFinder.tones.INSPIRING.description',
    accentClassName: 'bg-sky-500/15 text-sky-700 dark:text-sky-300',
  },
  {
    value: 'PRACTICAL',
    icon: GraduationCap,
    titleKey: 'book.giftFinder.tones.PRACTICAL.label',
    descriptionKey: 'book.giftFinder.tones.PRACTICAL.description',
    accentClassName:
      'bg-emerald-500/15 text-emerald-700 dark:text-emerald-300',
  },
  {
    value: 'ESCAPIST',
    icon: Compass,
    titleKey: 'book.giftFinder.tones.ESCAPIST.label',
    descriptionKey: 'book.giftFinder.tones.ESCAPIST.description',
    accentClassName: 'bg-violet-500/15 text-violet-700 dark:text-violet-300',
  },
]

const reasonKeyMap: Record<GiftFinderReason, string> = {
  RECIPIENT: 'book.giftFinder.reasons.RECIPIENT',
  OCCASION: 'book.giftFinder.reasons.OCCASION',
  BUDGET: 'book.giftFinder.reasons.BUDGET',
  TONE: 'book.giftFinder.reasons.TONE',
  HIGH_RATING: 'book.giftFinder.reasons.HIGH_RATING',
  POPULAR_PICK: 'book.giftFinder.reasons.POPULAR_PICK',
  GIFTABLE_PICK: 'book.giftFinder.reasons.GIFTABLE_PICK',
}

export default function GiftFinderPage() {
  const { t, formatNumber } = useLanguage()
  const {
    answers,
    recommendations,
    isSubmitting,
    hasSubmitted,
    isCatalogEmpty,
    error,
    selectRecipient,
    selectOccasion,
    selectBudget,
    selectTone,
    submit,
    resetQuiz,
  } = useGiftFinder()
  const resultsRef = useRef<HTMLElement | null>(null)
  const completedStepCount = [
    answers.recipient,
    answers.occasion,
    answers.budget,
    answers.tone,
  ].filter(Boolean).length
  const progressPercent = (completedStepCount / 4) * 100
  const currentStepLabel = Math.min(completedStepCount + 1, 4)

  const selectedLabels = {
    recipient:
      recipientOptions.find((option) => option.value === answers.recipient)
        ?.titleKey ?? null,
    occasion:
      occasionOptions.find((option) => option.value === answers.occasion)
        ?.titleKey ?? null,
    budget:
      budgetOptions.find((option) => option.value === answers.budget)?.titleKey ??
      null,
    tone:
      toneOptions.find((option) => option.value === answers.tone)?.titleKey ?? null,
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
      <main className="flex-1 bg-[radial-gradient(circle_at_top_right,_rgba(250,204,21,0.14),_transparent_30%),radial-gradient(circle_at_top_left,_rgba(236,72,153,0.12),_transparent_28%),linear-gradient(180deg,_rgba(255,251,235,0.98),_rgba(255,255,255,1))] dark:bg-[radial-gradient(circle_at_top_right,_rgba(250,204,21,0.08),_transparent_32%),radial-gradient(circle_at_top_left,_rgba(236,72,153,0.08),_transparent_30%),linear-gradient(180deg,_rgba(24,20,30,1),_rgba(12,12,19,1))]">
        <div className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-4 py-8 sm:px-6 lg:px-8 lg:py-10">
          <section className="relative overflow-hidden rounded-[2rem] border border-border/70 bg-card/92 px-6 py-8 shadow-[0_24px_80px_rgba(15,23,42,0.08)] dark:border-white/10 dark:bg-card/90 dark:shadow-[0_24px_80px_rgba(0,0,0,0.28)] sm:px-8 lg:px-10">
            <div className="absolute -right-12 top-6 size-32 rounded-full bg-primary/10 blur-2xl" />
            <div className="absolute -left-8 bottom-0 size-28 rounded-full bg-amber-300/25 blur-2xl" />
            <div className="relative grid gap-8 lg:grid-cols-[minmax(0,1fr)_280px] lg:items-end">
              <div className="max-w-3xl">
                <span className="inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-1.5 text-sm font-semibold text-primary">
                  <Gift className="size-4" />
                  {t('book.giftFinder.heroBadge')}
                </span>
                <h1 className="mt-4 font-heading text-3xl font-bold tracking-tight text-balance sm:text-4xl">
                  {t('book.giftFinder.title')}
                </h1>
                <p className="mt-3 max-w-2xl text-base leading-7 text-muted-foreground sm:text-lg">
                  {t('book.giftFinder.description')}
                </p>
              </div>

              <div className="rounded-3xl border border-border/70 bg-background/85 p-5 backdrop-blur dark:border-white/10 dark:bg-background/55">
                <div className="flex items-center justify-between text-sm font-medium">
                  <span>{t('book.giftFinder.progressTitle')}</span>
                  <span>
                    {completedStepCount === 4
                      ? t('book.giftFinder.progressReady')
                      : t('book.giftFinder.progressStep', {
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
                <div className="mt-4 grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                  <ProgressPill
                    label={t('book.giftFinder.steps.recipientLabel')}
                    isDone={Boolean(answers.recipient)}
                  />
                  <ProgressPill
                    label={t('book.giftFinder.steps.occasionLabel')}
                    isDone={Boolean(answers.occasion)}
                  />
                  <ProgressPill
                    label={t('book.giftFinder.steps.budgetLabel')}
                    isDone={Boolean(answers.budget)}
                  />
                  <ProgressPill
                    label={t('book.giftFinder.steps.toneLabel')}
                    isDone={Boolean(answers.tone)}
                  />
                </div>
              </div>
            </div>
          </section>

          <section className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_340px] lg:items-start">
            <div className="space-y-5">
              <StepCard
                stepNumber={1}
                title={t('book.giftFinder.steps.recipientTitle')}
                description={t('book.giftFinder.steps.recipientDescription')}
              >
                <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                  {recipientOptions.map((option) => (
                    <ChoiceCard
                      key={option.value}
                      icon={option.icon}
                      title={t(option.titleKey)}
                      description={t(option.descriptionKey)}
                      accentClassName={option.accentClassName}
                      isSelected={answers.recipient === option.value}
                      onClick={() => selectRecipient(option.value)}
                    />
                  ))}
                </div>
              </StepCard>

              <StepCard
                stepNumber={2}
                title={t('book.giftFinder.steps.occasionTitle')}
                description={t('book.giftFinder.steps.occasionDescription')}
              >
                <div className="grid gap-3 sm:grid-cols-2">
                  {occasionOptions.map((option) => (
                    <ChoiceCard
                      key={option.value}
                      icon={option.icon}
                      title={t(option.titleKey)}
                      description={t(option.descriptionKey)}
                      accentClassName={option.accentClassName}
                      isSelected={answers.occasion === option.value}
                      onClick={() => selectOccasion(option.value)}
                    />
                  ))}
                </div>
              </StepCard>

              <StepCard
                stepNumber={3}
                title={t('book.giftFinder.steps.budgetTitle')}
                description={t('book.giftFinder.steps.budgetDescription')}
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
                stepNumber={4}
                title={t('book.giftFinder.steps.toneTitle')}
                description={t('book.giftFinder.steps.toneDescription')}
              >
                <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                  {toneOptions.map((option) => (
                    <ChoiceCard
                      key={option.value}
                      icon={option.icon}
                      title={t(option.titleKey)}
                      description={t(option.descriptionKey)}
                      accentClassName={option.accentClassName}
                      isSelected={answers.tone === option.value}
                      onClick={() => selectTone(option.value)}
                    />
                  ))}
                </div>
              </StepCard>
            </div>

            <aside className="lg:sticky lg:top-24">
              <div className="overflow-hidden rounded-[1.75rem] border border-border/70 bg-card p-5 shadow-[0_18px_50px_rgba(15,23,42,0.07)] dark:border-white/10 dark:shadow-[0_18px_50px_rgba(0,0,0,0.24)]">
                <div className="flex items-center gap-3">
                  <span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                    <Gift className="size-5" />
                  </span>
                  <div>
                    <p className="font-heading text-lg font-semibold">
                      {t('book.giftFinder.sidebarTitle')}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {t('book.giftFinder.sidebarDescription')}
                    </p>
                  </div>
                </div>

                <div className="mt-5 space-y-3">
                  <SummaryField
                    label={t('book.giftFinder.summary.recipient')}
                    value={
                      selectedLabels.recipient
                        ? t(selectedLabels.recipient)
                        : t('book.giftFinder.summary.pending')
                    }
                  />
                  <SummaryField
                    label={t('book.giftFinder.summary.occasion')}
                    value={
                      selectedLabels.occasion
                        ? t(selectedLabels.occasion)
                        : t('book.giftFinder.summary.pending')
                    }
                  />
                  <SummaryField
                    label={t('book.giftFinder.summary.budget')}
                    value={
                      selectedLabels.budget
                        ? t(selectedLabels.budget)
                        : t('book.giftFinder.summary.pending')
                    }
                  />
                  <SummaryField
                    label={t('book.giftFinder.summary.tone')}
                    value={
                      selectedLabels.tone
                        ? t(selectedLabels.tone)
                        : t('book.giftFinder.summary.pending')
                    }
                  />
                </div>

                <button
                  type="button"
                  onClick={() => {
                    void handleSubmit()
                  }}
                  disabled={!isGiftFinderReady(answers) || isSubmitting}
                  className="mt-6 inline-flex w-full items-center justify-center gap-2 rounded-full bg-primary px-5 py-3.5 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmitting ? (
                    <>
                      <LoaderCircle className="size-4 animate-spin" />
                      {t('book.giftFinder.submitLoading')}
                    </>
                  ) : (
                    <>
                      <Sparkles className="size-4" />
                      {t('book.giftFinder.submit')}
                    </>
                  )}
                </button>

                <button
                  type="button"
                  onClick={resetQuiz}
                  className="mt-3 inline-flex w-full items-center justify-center gap-2 rounded-full border border-border bg-background px-5 py-3 text-sm font-semibold transition-colors hover:bg-muted"
                >
                  <RefreshCw className="size-4" />
                  {t('book.giftFinder.reset')}
                </button>
              </div>
            </aside>
          </section>

          <section ref={resultsRef} className="space-y-5">
            <div className="flex flex-col gap-2">
              <h2 className="font-heading text-2xl font-bold tracking-tight">
                {t('book.giftFinder.resultsTitle')}
              </h2>
              <p className="text-sm text-muted-foreground">
                {t('book.giftFinder.resultsDescription')}
              </p>
            </div>

            {error ? (
              <StatePanel
                title={t('book.giftFinder.errorTitle')}
                description={error}
                action={
                  <button
                    type="button"
                    onClick={() => {
                      void handleSubmit()
                    }}
                    className="inline-flex items-center justify-center rounded-2xl bg-primary px-5 py-3 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
                  >
                    {t('book.giftFinder.errorRetry')}
                  </button>
                }
              />
            ) : null}

            {!error && hasSubmitted && isCatalogEmpty ? (
              <StatePanel
                title={t('book.giftFinder.catalogEmptyTitle')}
                description={t('book.giftFinder.catalogEmptyDescription')}
              />
            ) : null}

            {!error &&
            hasSubmitted &&
            !isSubmitting &&
            !isCatalogEmpty &&
            recommendations.length === 0 ? (
              <StatePanel
                title={t('book.giftFinder.emptyTitle')}
                description={t('book.giftFinder.emptyDescription')}
              />
            ) : null}

            {!error && recommendations.length > 0 ? (
              <>
                <div className="flex flex-wrap items-center gap-3">
                  <span className="inline-flex rounded-full bg-primary/10 px-3 py-1 text-sm font-semibold text-primary">
                    {t('book.giftFinder.resultsCount', {
                      count: formatNumber(recommendations.length),
                    })}
                  </span>
                  {selectedLabels.recipient ? (
                    <FilterChip label={t(selectedLabels.recipient)} />
                  ) : null}
                  {selectedLabels.occasion ? (
                    <FilterChip label={t(selectedLabels.occasion)} />
                  ) : null}
                  {selectedLabels.budget ? (
                    <FilterChip label={t(selectedLabels.budget)} />
                  ) : null}
                  {selectedLabels.tone ? (
                    <FilterChip label={t(selectedLabels.tone)} />
                  ) : null}
                </div>

                <div className="motion-result grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
                  {recommendations.map((recommendation) => (
                    <GiftRecommendationCard
                      key={recommendation.book.id}
                      book={recommendation.book}
                      reasons={recommendation.reasons}
                    />
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

function GiftRecommendationCard({
  book,
  reasons,
}: {
  book: Book
  reasons: GiftFinderReason[]
}) {
  const { isAuthenticated } = useAuth()
  const { addItem } = useCart()
  const { isWishlisted, toggleBook } = useWishlist()
  const { t, language, formatCurrency } = useLanguage()
  const isSaved = isWishlisted(book.id)
  const pageCount = book.detail?.pageCount ?? null

  async function handleAddToCart() {
    try {
      await addItem(book.id)
      toast.success(t('book.card.addedToCart', { title: book.title }))
    } catch (error) {
      toast.error(error instanceof Error ? error.message : t('cart.updateError'))
    }
  }

  async function handleToggleWishlist() {
    try {
      const added = await toggleBook(toBookCardData(book))
      toast.success(
        added
          ? t('wishlist.added', { title: book.title })
          : t('wishlist.removed', { title: book.title }),
      )
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : t('wishlist.updateError'),
      )
    }
  }

  return (
    <article className="motion-card overflow-hidden rounded-[1.75rem] border border-border/70 bg-card/92 shadow-[0_18px_50px_rgba(15,23,42,0.07)] dark:border-white/10 dark:shadow-[0_18px_50px_rgba(0,0,0,0.24)]">
      <Link to={`/books/${book.id}`} className="block">
        <div className="relative aspect-[4/3] overflow-hidden bg-muted">
          <img
            src={getBookCoverUrl(book.cover)}
            alt={t('book.card.coverAlt', { title: book.title })}
            onError={(event) => setBookCoverFallback(event.currentTarget)}
            className="absolute inset-0 size-full object-cover transition-transform duration-300 hover:scale-[1.03]"
          />
        </div>
      </Link>

      <div className="space-y-4 p-5">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary/75">
            {getCategoryLabel(book.categoryInfo ?? book.category, language)}
          </p>
          <Link to={`/books/${book.id}`}>
            <h3 className="mt-2 font-heading text-xl font-semibold leading-snug text-balance transition-colors hover:text-primary">
              {book.title}
            </h3>
          </Link>
          <p className="mt-1 text-sm text-muted-foreground">{book.author}</p>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <InfoPill
            label={t('common.price')}
            value={formatCurrency(book.price)}
          />
          <InfoPill
            label={t('book.detail.specPageCount')}
            value={
              pageCount == null
                ? '...'
                : t('book.detail.pageCountValue', { count: pageCount })
            }
          />
        </div>

        {typeof book.rating === 'number' && book.rating > 0 ? (
          <p className="text-sm text-muted-foreground">
            {book.rating.toFixed(1)} / 5 · {book.reviews ?? 0}{' '}
            {t('book.detail.reviewsCount', { count: '' }).replace(/[()]/g, '')}
          </p>
        ) : null}

        <div className="flex flex-wrap gap-2">
          {reasons.map((reason) => (
            <span
              key={reason}
              className="rounded-full bg-muted px-2.5 py-1 text-xs font-medium text-muted-foreground"
            >
              {t(reasonKeyMap[reason])}
            </span>
          ))}
        </div>

        <div className="flex flex-wrap gap-2 pt-1">
          <Link
            to={`/books/${book.id}`}
            className="inline-flex items-center justify-center rounded-full bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
          >
            {t('book.giftFinder.actions.viewDetail')}
          </Link>

          {isAuthenticated ? (
            <>
              <button
                type="button"
                onClick={() => {
                  void handleToggleWishlist()
                }}
                className="inline-flex items-center justify-center rounded-full border border-border bg-background px-4 py-2.5 text-sm font-semibold transition-colors hover:bg-muted"
              >
                {isSaved
                  ? t('book.giftFinder.actions.removeFromWishlist')
                  : t('book.giftFinder.actions.addToWishlist')}
              </button>
              <button
                type="button"
                onClick={() => {
                  void handleAddToCart()
                }}
                className="inline-flex items-center justify-center rounded-full border border-primary/20 bg-primary/6 px-4 py-2.5 text-sm font-semibold text-primary transition-colors hover:bg-primary/10"
              >
                {t('book.giftFinder.actions.addToCart')}
              </button>
            </>
          ) : null}
        </div>
      </div>
    </article>
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

function InfoPill({ label, value }: { label: string; value: string }) {
  return (
    <SummaryField label={label} value={value} className="mt-0" />
  )
}

function toBookCardData(book: Book): BookCardData {
  return {
    id: book.id,
    title: book.title,
    author: book.author,
    category: book.category,
    price: book.price,
    cover: book.cover,
    oldPrice: book.oldPrice,
    rating: book.rating,
    reviews: book.reviews,
    bestseller: book.bestseller,
  }
}

import { Copy, Gift, ShoppingCart, Sparkles } from 'lucide-react'
import { toast } from 'sonner'
import { Link } from 'react-router-dom'
import { Button } from '@/components/common/button'
import {
  StatePanel,
  StatPill,
  SummaryField,
  primaryButtonClassName,
  secondaryLinkButtonClassName,
} from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useCouponGame } from '@/hooks/use-coupon-game'
import type { CouponGameCouponSummary } from '@/types/coupon-game'

const WHEEL_SEGMENT_COUNT = 6

export default function CouponGamePage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const {
    availableCoupons,
    result,
    isLoading,
    isSpinning,
    playedToday,
    spinRotation,
    error,
    play,
  } = useCouponGame()
  const couponSummary = result?.couponSummary ?? null
  const showEmptyState = !isLoading && !error && !couponSummary && availableCoupons.length === 0
  const showErrorState = !isLoading && Boolean(error) && !couponSummary

  async function handleCopyCoupon() {
    if (!result) {
      return
    }

    const copied = await copyTextWithFallback(result.couponCode)
    if (copied) {
      toast.success(t('couponGamePage.copySuccess'))
      return
    }

    toast.error(t('couponGamePage.copyError'))
  }

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(254,249,195,0.4),rgba(255,255,255,0.96)_25%,rgba(254,242,242,0.96)_100%)] dark:bg-[linear-gradient(180deg,rgba(31,25,15,1)_0%,rgba(18,16,24,1)_35%,rgba(14,13,22,1)_100%)]">
      <Header />
      <main className="flex-1">
        <section className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
          <div className="relative overflow-hidden rounded-[2rem] border border-amber-200/70 bg-white/88 px-6 py-8 shadow-[0_28px_90px_rgba(217,119,6,0.12)] backdrop-blur dark:border-amber-300/15 dark:bg-card/90 dark:shadow-[0_28px_90px_rgba(0,0,0,0.3)] sm:px-8 lg:px-10">
            <div className="pointer-events-none absolute -right-12 top-0 size-40 rounded-full bg-pink-200/45 blur-3xl dark:bg-pink-500/12" />
            <div className="pointer-events-none absolute bottom-0 left-0 size-44 rounded-full bg-amber-200/55 blur-3xl dark:bg-amber-400/12" />

            <div className="relative flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
              <div className="max-w-3xl">
                <span className="inline-flex items-center gap-2 rounded-full border border-amber-200 bg-amber-50 px-4 py-2 text-sm font-semibold text-amber-700 dark:border-amber-300/20 dark:bg-amber-400/10 dark:text-amber-200">
                  <Gift className="size-4" />
                  {t('couponGamePage.badge')}
                </span>
                <h1 className="mt-4 font-heading text-4xl font-bold tracking-tight text-foreground sm:text-5xl">
                  {t('couponGamePage.title')}
                </h1>
                <p className="mt-4 max-w-2xl text-sm leading-7 text-muted-foreground sm:text-base">
                  {t('couponGamePage.description')}
                </p>
              </div>

              <div className="flex flex-wrap gap-3">
                <StatPill
                  label={t('couponGamePage.limitChip')}
                  value={t('couponGamePage.dailyLimit')}
                  className="border-amber-100 bg-white/78 dark:border-amber-300/15 dark:bg-background/45"
                />
                <StatPill
                  label={t('couponGamePage.poolChip')}
                  value={t('couponGamePage.poolCount', {
                    count: formatNumber(availableCoupons.length),
                  })}
                  className="border-amber-100 bg-white/78 dark:border-amber-300/15 dark:bg-background/45"
                />
                <StatPill
                  label={t('couponGamePage.manualChip')}
                  value={t('couponGamePage.manualOnly')}
                  className="border-amber-100 bg-white/78 dark:border-amber-300/15 dark:bg-background/45"
                />
              </div>
            </div>
          </div>
        </section>

        <section className="mx-auto grid w-full max-w-7xl gap-6 px-4 pb-12 sm:px-6 lg:grid-cols-[minmax(0,1.1fr)_minmax(320px,0.9fr)] lg:px-8">
          <div className="min-w-0 rounded-[2rem] border border-amber-200/70 bg-white/86 p-6 shadow-[0_28px_70px_rgba(250,204,21,0.12)] backdrop-blur dark:border-amber-300/15 dark:bg-card/90 dark:shadow-[0_28px_70px_rgba(0,0,0,0.28)] sm:p-8">
            {showErrorState ? (
              <StatePanel
                title={t('couponGamePage.errorTitle')}
                description={t('couponGamePage.errorDescription')}
                icon={<Sparkles className="size-10 text-rose-500" />}
                tone="error"
                minHeightClassName="min-h-[440px]"
              />
            ) : showEmptyState ? (
              <StatePanel
                title={t('couponGamePage.emptyTitle')}
                description={t('couponGamePage.emptyDescription')}
                icon={<Gift className="size-10 text-amber-600" />}
                tone="warning"
                minHeightClassName="min-h-[440px]"
              />
            ) : (
              <>
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <p className="text-sm font-semibold uppercase tracking-[0.24em] text-amber-700 dark:text-amber-300">
                      {t('couponGamePage.wheelBadge')}
                    </p>
                    <p className="mt-2 text-sm leading-7 text-muted-foreground">
                      {playedToday
                        ? t('couponGamePage.alreadyPlayedDescription')
                        : t('couponGamePage.spinHint')}
                    </p>
                  </div>

                  <Button
                    type="button"
                    onClick={play}
                    disabled={playedToday || isSpinning || availableCoupons.length === 0}
                    className={`${primaryButtonClassName} rounded-full bg-amber-500 px-6 text-white hover:bg-amber-500/90 disabled:bg-amber-300`}
                  >
                    <Sparkles className={isSpinning ? 'size-4 animate-spin' : 'size-4'} />
                    {isSpinning
                      ? t('couponGamePage.spinLoading')
                      : playedToday
                        ? t('couponGamePage.playedButton')
                        : t('couponGamePage.spinButton')}
                  </Button>
                </div>

                <div className="mt-8 flex justify-center">
                  <div className="relative size-[min(76vw,420px)] sm:size-[min(84vw,420px)]">
                    <div className="absolute left-1/2 top-2 z-10 -translate-x-1/2">
                      <div className="h-0 w-0 border-x-[16px] border-b-[28px] border-x-transparent border-b-slate-900 drop-shadow-[0_10px_18px_rgba(15,23,42,0.18)]" />
                    </div>

                    <div
                      className="absolute inset-4 rounded-full border-[10px] border-white/70 shadow-[0_24px_60px_rgba(251,191,36,0.25)]"
                      style={{
                        backgroundImage:
                          'conic-gradient(from 15deg, rgba(254,240,138,0.95) 0deg 60deg, rgba(251,207,232,0.95) 60deg 120deg, rgba(191,219,254,0.95) 120deg 180deg, rgba(209,250,229,0.95) 180deg 240deg, rgba(254,215,170,0.95) 240deg 300deg, rgba(224,231,255,0.95) 300deg 360deg)',
                        transform: `rotate(${spinRotation}deg)`,
                        transition: isSpinning
                          ? 'transform 2200ms cubic-bezier(0.18, 0.88, 0.18, 1)'
                          : 'transform 700ms ease-out',
                      }}
                    >
                      {Array.from({ length: WHEEL_SEGMENT_COUNT }).map((_, index) => (
                        <div
                          key={index}
                          className="absolute left-1/2 top-1/2 h-2.5 w-2.5 rounded-full bg-white/85 shadow-sm"
                          style={{
                            transform: `rotate(${index * (360 / WHEEL_SEGMENT_COUNT)}deg) translateY(-150px)`,
                            transformOrigin: 'center -6px',
                          }}
                        />
                      ))}
                    </div>

                    <div className="absolute inset-[32%] flex items-center justify-center rounded-full border border-white/80 bg-white/92 shadow-[0_16px_36px_rgba(15,23,42,0.12)]">
                      <div className="text-center">
                        <p className="text-xs font-semibold uppercase tracking-[0.32em] text-amber-700">
                          {t('couponGamePage.wheelCenter')}
                        </p>
                        <p className="mt-2 px-4 font-heading text-xl font-bold text-slate-900 sm:text-2xl">
                          {couponSummary
                            ? t('couponGamePage.todayResultBadge')
                            : t('couponGamePage.spinButton')}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </>
            )}
          </div>

          <div className="min-w-0 rounded-[2rem] border border-slate-200/80 bg-white/92 p-6 shadow-[0_24px_70px_rgba(15,23,42,0.08)] dark:border-white/10 dark:bg-card/92 dark:shadow-[0_24px_70px_rgba(0,0,0,0.28)] sm:p-8">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-sm font-semibold uppercase tracking-[0.24em] text-muted-foreground">
                  {playedToday
                    ? t('couponGamePage.alreadyPlayedTitle')
                    : t('couponGamePage.resultTitle')}
                </p>
                <h2 className="mt-2 font-heading text-2xl font-bold text-foreground">
                  {couponSummary
                    ? result?.couponCode
                    : t('couponGamePage.resultPlaceholder')}
                </h2>
              </div>
              {couponSummary ? (
                <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700 dark:bg-emerald-400/10 dark:text-emerald-200">
                  {formatCouponBenefit(couponSummary, formatCurrency, t)}
                </span>
              ) : null}
            </div>

            {couponSummary ? (
              <>
                <div className="motion-result mt-5 rounded-[1.5rem] border border-dashed border-amber-200 bg-amber-50/70 p-5 dark:border-amber-300/20 dark:bg-amber-400/8">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="rounded-full bg-white px-3 py-1 text-xs font-semibold text-slate-700 dark:bg-background/70 dark:text-foreground">
                      {getCouponTypeLabel(couponSummary, t)}
                    </span>
                    <span className="rounded-full bg-white px-3 py-1 text-xs font-semibold text-slate-700 dark:bg-background/70 dark:text-foreground">
                      {formatCouponBenefit(couponSummary, formatCurrency, t)}
                    </span>
                  </div>

                  <p className="mt-4 text-sm leading-7 text-foreground/80">
                    {couponSummary.description || t('couponGamePage.noDescription')}
                  </p>

                  <div className="mt-5 grid gap-3 sm:grid-cols-2">
                    <SummaryField
                      label={t('couponGamePage.minOrderLabel')}
                      value={formatCurrency(couponSummary.minOrderAmount)}
                      className="border-white/80 bg-white/78 dark:border-white/10 dark:bg-background/45"
                    />
                    <SummaryField
                      label={t('couponGamePage.expiresLabel')}
                      value={formatExpiresAt(couponSummary.expiresAt, formatDate, t)}
                      className="border-white/80 bg-white/78 dark:border-white/10 dark:bg-background/45"
                    />
                    {couponSummary.maxDiscountAmount !== null ? (
                      <SummaryField
                        label={t('couponGamePage.maxDiscountLabel')}
                        value={formatCurrency(couponSummary.maxDiscountAmount)}
                        className="border-white/80 bg-white/78 dark:border-white/10 dark:bg-background/45"
                      />
                    ) : null}
                    <SummaryField
                      label={t('couponGamePage.resultSaved')}
                      value={t('couponGamePage.dailyLimit')}
                      className="border-white/80 bg-white/78 dark:border-white/10 dark:bg-background/45"
                    />
                  </div>
                </div>

                <p className="mt-5 text-sm leading-7 text-muted-foreground">
                  {t('couponGamePage.manualApplyHint')}
                </p>

                <div className="mt-6 flex flex-col gap-3 sm:flex-row">
                  <Button
                    type="button"
                    onClick={handleCopyCoupon}
                    className={`${primaryButtonClassName} h-11 flex-1 rounded-full bg-slate-900 px-6 text-white hover:bg-slate-900/90 dark:bg-primary dark:text-primary-foreground dark:hover:bg-primary/90`}
                  >
                    <Copy className="size-4" />
                    {t('couponGamePage.copyButton')}
                  </Button>
                  <Link
                    to="/cart"
                    className={`${secondaryLinkButtonClassName} h-11 flex-1 rounded-full px-6`}
                  >
                    <ShoppingCart className="size-4" />
                    {t('couponGamePage.openCart')}
                  </Link>
                </div>
              </>
            ) : (
              <div className="mt-6 rounded-[1.5rem] border border-dashed border-slate-200 bg-slate-50/80 p-5 text-sm leading-7 text-slate-600 dark:border-white/10 dark:bg-background/45 dark:text-muted-foreground">
                {isLoading
                  ? t('common.loading')
                  : showErrorState
                    ? t('couponGamePage.errorDescription')
                    : showEmptyState
                      ? t('couponGamePage.emptyDescription')
                      : t('couponGamePage.resultWaiting')}
              </div>
            )}

            <div className="mt-6 flex flex-wrap gap-3">
              <Link
                to="/"
                className={secondaryLinkButtonClassName}
              >
                {t('couponGamePage.backHome')}
              </Link>
              <Link
                to="/books"
                className="inline-flex items-center gap-2 rounded-2xl bg-rose-100 px-5 py-3 text-sm font-semibold text-rose-700 transition-colors hover:bg-rose-200 dark:bg-rose-400/12 dark:text-rose-200 dark:hover:bg-rose-400/18"
              >
                {t('couponGamePage.viewBooks')}
              </Link>
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  )
}

function getCouponTypeLabel(
  couponSummary: CouponGameCouponSummary,
  t: (key: string) => string,
) {
  return couponSummary.couponType === 'BOOK'
    ? t('couponGamePage.typeBook')
    : t('couponGamePage.typeShipping')
}

function formatCouponBenefit(
  couponSummary: CouponGameCouponSummary,
  formatCurrency: (value: number) => string,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return couponSummary.discountType === 'PERCENTAGE'
    ? t('couponGamePage.discountPercent', {
        value: couponSummary.discountValue,
      })
    : t('couponGamePage.discountFixed', {
        amount: formatCurrency(couponSummary.discountValue),
      })
}

function formatExpiresAt(
  expiresAt: string | null,
  formatDate: (value: Date | number | string) => string,
  t: (key: string) => string,
) {
  if (!expiresAt || Number.isNaN(Date.parse(expiresAt))) {
    return t('couponGamePage.noExpiry')
  }

  return formatDate(expiresAt)
}

async function copyTextWithFallback(text: string) {
  if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(text)
      return true
    } catch {
      // Fall back to the textarea strategy below.
    }
  }

  if (typeof document === 'undefined') {
    return false
  }

  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', 'true')
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()

  try {
    return document.execCommand('copy')
  } catch {
    return false
  } finally {
    document.body.removeChild(textarea)
  }
}

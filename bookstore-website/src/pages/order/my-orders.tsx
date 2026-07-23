import { Link } from 'react-router-dom'
import {
  ArrowRight,
  BookOpen,
  CheckCircle2,
  Clock3,
  Copy,
  Package2,
  Phone,
  ScrollText,
  ShoppingBag,
  Sparkles,
  Truck,
  UserRound,
  WalletCards,
  type LucideIcon,
} from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { PaginationControls } from '@/components/common/pagination-controls'
import {
  StatePanel,
  SurfaceCard,
  primaryButtonClassName,
} from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useMyOrdersPage } from '@/hooks/use-my-orders-page'
import type { OrderResponse, OrderStatus } from '@/types/order'
import { cn } from '@/utils'
import { getOrderStatusLabel } from '@/utils/i18n'

const ORDER_STATUS_TONES: Record<
  OrderStatus,
  {
    badgeClassName: string
    badgeIconClassName: string
    stepIndex: number
  }
> = {
  PENDING: {
    badgeClassName:
      'border border-primary/10 bg-[linear-gradient(135deg,rgba(124,92,255,0.12),rgba(124,92,255,0.05))] text-primary',
    badgeIconClassName: 'text-primary',
    stepIndex: 0,
  },
  CONFIRMED: {
    badgeClassName:
      'border border-sky-100 bg-sky-50/90 text-sky-700 dark:border-sky-300/20 dark:bg-sky-400/10 dark:text-sky-200',
    badgeIconClassName: 'text-sky-600 dark:text-sky-300',
    stepIndex: 1,
  },
  SHIPPING: {
    badgeClassName:
      'border border-amber-100 bg-amber-50/90 text-amber-700 dark:border-amber-300/20 dark:bg-amber-400/10 dark:text-amber-200',
    badgeIconClassName: 'text-amber-600 dark:text-amber-300',
    stepIndex: 2,
  },
  DELIVERED: {
    badgeClassName:
      'border border-emerald-100 bg-emerald-50/90 text-emerald-700 dark:border-emerald-300/20 dark:bg-emerald-400/10 dark:text-emerald-200',
    badgeIconClassName: 'text-emerald-600 dark:text-emerald-300',
    stepIndex: 3,
  },
  CANCELLED: {
    badgeClassName:
      'border border-rose-100 bg-rose-50/90 text-rose-700 dark:border-rose-300/20 dark:bg-rose-400/10 dark:text-rose-200',
    badgeIconClassName: 'text-rose-600 dark:text-rose-300',
    stepIndex: 0,
  },
}

const ORDER_TIMELINE_ICONS: LucideIcon[] = [
  Clock3,
  Package2,
  Truck,
  CheckCircle2,
]

export default function MyOrdersPage() {
  const { t, formatCurrency, formatDate, formatNumber, locale } =
    useLanguage()
  const {
    orders,
    isLoading,
    error,
    totalCount,
    page,
    pageSize,
    handlePageChange,
  } = useMyOrdersPage()

  async function handleCopyOrderId(orderId: string) {
    try {
      await navigator.clipboard.writeText(orderId)
      toast.success(t('orderHistoryPage.copySuccess'))
    } catch {
      toast.error(t('orderHistoryPage.copyError'))
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(252,248,255,1)_0%,rgba(246,240,255,0.96)_54%,rgba(255,255,255,1)_100%)] dark:bg-[linear-gradient(180deg,rgba(14,13,22,1)_0%,rgba(10,9,17,1)_54%,rgba(7,7,13,1)_100%)]">
      <Header />

      <main className="flex-1 pb-16 pt-6 sm:pb-20 sm:pt-8">
        <div className="mx-auto flex w-full max-w-[1272px] flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <OrdersHero
            countLabel={t('orders.totalCount', {
              count: formatNumber(totalCount),
            })}
            title={t('orders.title')}
            continueShoppingLabel={t('common.continueShopping')}
          />

          {isLoading ? (
            <StatePanel title={t('common.loading')} />
          ) : error ? (
            <StatePanel tone="error" title={error} />
          ) : orders.length === 0 ? (
            <EmptyOrdersPanel
              title={t('orders.emptyTitle')}
              description={t('orders.emptyDescription')}
              ctaLabel={t('orderHistoryPage.exploreNow')}
            />
          ) : (
            <div className="space-y-5">
              {orders.map((order) => (
                <OrderHistoryCard
                  key={order.orderId}
                  locale={locale}
                  onCopyOrderId={handleCopyOrderId}
                  order={order}
                  productCountLabel={t('admin.orders.productCount', {
                    count: formatNumber(order.items.length),
                  })}
                  productsTitle={t('admin.orders.columns.products')}
                  receiverTitle={t('orders.receiverName')}
                  phoneTitle={t('orders.receiverPhone')}
                  finalAmountTitle={t('orders.finalAmount')}
                  createdAtTitle={t('orders.createdAt')}
                  orderIdTitle={t('orders.orderId')}
                  viewDetailLabel={t('orders.viewDetail')}
                  statusLabel={getOrderStatusLabel(order.status, t)}
                  formattedFinalAmount={formatCurrency(order.finalAmount)}
                  formattedCreatedDate={formatDate(order.createdAt)}
                />
              ))}
              <div className="overflow-hidden rounded-[24px] border border-primary/10 bg-white/92 shadow-[0_14px_38px_rgba(137,92,255,0.08)] dark:border-white/10 dark:bg-card/92 dark:shadow-[0_14px_38px_rgba(0,0,0,0.24)]">
                <PaginationControls
                  page={page}
                  size={pageSize}
                  totalCount={totalCount}
                  onPageChange={handlePageChange}
                />
              </div>
            </div>
          )}

          <DiscoverMorePanel
            title={t('orderHistoryPage.discoverTitle')}
            description={t('orderHistoryPage.discoverDescription')}
            ctaLabel={t('orderHistoryPage.exploreNow')}
          />
        </div>
      </main>

      <Footer />
    </div>
  )
}

function OrdersHero({
  countLabel,
  title,
  continueShoppingLabel,
}: {
  countLabel: string
  title: string
  continueShoppingLabel: string
}) {
  return (
    <section className="relative overflow-hidden rounded-[34px] border border-primary/10 bg-white/82 px-6 py-7 shadow-[0_24px_80px_rgba(137,92,255,0.1)] backdrop-blur dark:border-white/10 dark:bg-card/90 dark:shadow-[0_24px_80px_rgba(0,0,0,0.32)] sm:px-8 lg:px-10">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_left,rgba(198,177,255,0.14),transparent_28%),radial-gradient(circle_at_center,rgba(150,121,255,0.1),transparent_24%),linear-gradient(180deg,rgba(250,246,255,0.96)_0%,rgba(246,240,255,0.86)_100%)] dark:bg-[radial-gradient(circle_at_left,rgba(124,92,255,0.18),transparent_30%),radial-gradient(circle_at_center,rgba(150,121,255,0.12),transparent_26%),linear-gradient(180deg,rgba(31,27,48,0.96)_0%,rgba(22,19,36,0.92)_100%)]" />
      <Sparkles className="pointer-events-none absolute left-10 top-10 hidden h-4 w-4 text-primary/25 lg:block" />
      <Sparkles className="pointer-events-none absolute left-[32%] top-16 hidden h-5 w-5 text-primary/18 lg:block" />
      <div className="pointer-events-none absolute bottom-3 left-[58%] hidden lg:block">
        <BookOpen className="h-24 w-24 text-primary/10" strokeWidth={1.4} />
      </div>
      <HeroBookStack />

      <div className="relative flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex min-w-0 items-start gap-5">
          <span className="flex size-[72px] shrink-0 items-center justify-center rounded-[24px] border border-primary/10 bg-white text-primary shadow-[0_18px_38px_rgba(137,92,255,0.12)] dark:border-white/10 dark:bg-background/70 dark:shadow-[0_18px_38px_rgba(0,0,0,0.28)]">
            <ShoppingBag className="h-9 w-9" strokeWidth={1.7} />
          </span>

          <div className="min-w-0">
            <h1 className="font-heading text-4xl font-bold tracking-tight text-foreground">
              {title}
            </h1>
            <p className="mt-2 text-[1.15rem] text-muted-foreground">{countLabel}</p>
          </div>
        </div>

        <Link to="/books">
          <Button className="h-12 rounded-2xl bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-6 shadow-[0_18px_34px_rgba(109,76,255,0.24)] hover:opacity-95">
            <ShoppingBag className="mr-2 h-4 w-4" />
            {continueShoppingLabel}
          </Button>
        </Link>
      </div>
    </section>
  )
}

function OrderHistoryCard({
  locale,
  onCopyOrderId,
  order,
  productCountLabel,
  productsTitle,
  receiverTitle,
  phoneTitle,
  finalAmountTitle,
  createdAtTitle,
  orderIdTitle,
  viewDetailLabel,
  statusLabel,
  formattedFinalAmount,
  formattedCreatedDate,
}: {
  locale: string
  onCopyOrderId: (orderId: string) => void
  order: OrderResponse
  productCountLabel: string
  productsTitle: string
  receiverTitle: string
  phoneTitle: string
  finalAmountTitle: string
  createdAtTitle: string
  orderIdTitle: string
  viewDetailLabel: string
  statusLabel: string
  formattedFinalAmount: string
  formattedCreatedDate: string
}) {
  const { t } = useLanguage()
  const statusTone = ORDER_STATUS_TONES[order.status]
  const createdTimeLabel = formatTime(locale, order.createdAt)

  return (
    <SurfaceCard className="p-6">
      <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
        <div className="flex min-w-0 items-start gap-4">
          <span className="flex size-[62px] shrink-0 items-center justify-center rounded-[20px] bg-[linear-gradient(180deg,rgba(124,92,255,0.12)_0%,rgba(124,92,255,0.04)_100%)] text-primary shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]">
            <ScrollText className="h-8 w-8" strokeWidth={1.8} />
          </span>

          <div className="min-w-0">
            <p className="text-[12px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
              {orderIdTitle}
            </p>
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <h2 className="break-all font-heading text-[2rem] font-bold tracking-tight text-foreground">
                {order.orderId}
              </h2>
              <button
                type="button"
                aria-label={t('orderHistoryPage.copyOrderId')}
                onClick={() => void onCopyOrderId(order.orderId)}
                className="inline-flex size-11 items-center justify-center rounded-xl border border-transparent text-muted-foreground transition hover:border-primary/10 hover:bg-primary/6 hover:text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50"
              >
                <Copy className="h-4 w-4" />
              </button>
            </div>
            <p className="mt-2 text-[1rem] text-muted-foreground">
              {createdAtTitle}: {formattedCreatedDate} {'\u2022'} {createdTimeLabel}
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-3 xl:justify-end">
          <span
            className={cn(
              'inline-flex h-12 items-center gap-2 rounded-2xl px-5 text-[15px] font-semibold shadow-[0_10px_24px_rgba(124,92,255,0.08)]',
              statusTone.badgeClassName,
            )}
          >
            <Clock3 className={cn('h-4 w-4', statusTone.badgeIconClassName)} />
            {statusLabel}
          </span>

          <Link to={`/orders/${order.orderId}`}>
            <Button
              variant="outline"
              className="h-12 rounded-2xl border-primary/20 px-5 text-primary hover:bg-primary/6"
            >
              {viewDetailLabel}
              <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </Link>
        </div>
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <OrderMetaTile
          icon={UserRound}
          label={receiverTitle}
          value={order.receiverName}
        />
        <OrderMetaTile
          icon={Phone}
          label={phoneTitle}
          value={order.receiverPhone}
        />
        <OrderMetaTile
          icon={Package2}
          label={productsTitle}
          value={productCountLabel}
        />
        <OrderMetaTile
          icon={WalletCards}
          label={finalAmountTitle}
          labelClassName="whitespace-nowrap"
          value={formattedFinalAmount}
        />
      </div>

      <OrderTimeline
        activeIndex={statusTone.stepIndex}
        createdAt={order.createdAt}
        isCancelled={order.status === 'CANCELLED'}
        locale={locale}
      />
    </SurfaceCard>
  )
}

function OrderMetaTile({
  icon: Icon,
  label,
  labelClassName,
  value,
}: {
  icon: LucideIcon
  label: string
  labelClassName?: string
  value: string
}) {
  return (
    <div className="rounded-[20px] border border-primary/8 bg-[linear-gradient(180deg,rgba(255,255,255,0.98)_0%,rgba(250,246,255,0.94)_100%)] px-5 py-5 shadow-[0_10px_28px_rgba(137,92,255,0.04)] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(35,31,51,0.96)_0%,rgba(27,24,42,0.94)_100%)] dark:shadow-none">
      <div className="flex items-center gap-4">
        <span className="flex size-[50px] shrink-0 items-center justify-center rounded-[16px] bg-[linear-gradient(180deg,rgba(124,92,255,0.12),rgba(124,92,255,0.04))] text-primary">
          <Icon className="h-6 w-6" strokeWidth={1.8} />
        </span>

        <div className="min-w-0">
          <p
            className={cn(
              'text-[12px] font-semibold uppercase tracking-[0.14em] text-muted-foreground',
              labelClassName,
            )}
          >
            {label}
          </p>
          <p className="mt-1.5 break-words text-[1.18rem] font-bold text-foreground">
            {value}
          </p>
        </div>
      </div>
    </div>
  )
}

function OrderTimeline({
  activeIndex,
  createdAt,
  isCancelled,
  locale,
}: {
  activeIndex: number
  createdAt: string
  isCancelled: boolean
  locale: string
}) {
  const { t } = useLanguage()
  const steps = [
    t('orderHistoryPage.pendingStep'),
    t('orderHistoryPage.processingStep'),
    t('orderHistoryPage.shippingStep'),
    t('orderHistoryPage.completedStep'),
  ]
  const activeTimestamp = formatCompactDateTime(locale, createdAt)

  return (
    <div className="mt-5 rounded-[24px] border border-primary/6 bg-[linear-gradient(135deg,rgba(252,249,255,0.98)_0%,rgba(245,240,255,0.9)_100%)] px-5 py-5 dark:border-white/10 dark:bg-[linear-gradient(135deg,rgba(31,28,46,0.96)_0%,rgba(23,21,36,0.94)_100%)]">
      <div className="flex flex-col gap-4 xl:flex-row xl:items-center">
        {steps.map((label, index) => {
          const Icon = ORDER_TIMELINE_ICONS[index]
          const isActive = index === activeIndex
          const isCompleted = !isCancelled && index < activeIndex
          const isPending = !isActive && !isCompleted

          return (
            <div
              key={label}
              className="flex min-w-0 flex-1 items-center gap-4"
            >
              <div className="flex min-w-0 items-center gap-4">
                <span
                  className={cn(
                    'flex size-11 shrink-0 items-center justify-center rounded-full border transition',
                    isActive &&
                      'border-transparent bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] text-white shadow-[0_18px_28px_rgba(109,76,255,0.24)]',
                    isCompleted &&
                      'border-primary/12 bg-white text-primary shadow-[0_10px_20px_rgba(124,92,255,0.08)] dark:bg-card dark:shadow-none',
                    isPending &&
                      'border-slate-200 bg-white/80 text-slate-400 dark:border-white/10 dark:bg-background/55 dark:text-muted-foreground',
                  )}
                >
                  <Icon className="h-5 w-5" strokeWidth={1.8} />
                </span>

                <div className="min-w-0">
                  <p
                    className={cn(
                      'text-[15px] font-medium',
                      isActive
                        ? 'font-semibold text-primary'
                        : 'text-muted-foreground',
                    )}
                  >
                    {label}
                  </p>
                  <p
                    className={cn(
                      'mt-1 text-[13px]',
                      isActive ? 'text-muted-foreground' : 'text-transparent',
                    )}
                  >
                    {isActive ? activeTimestamp : '--'}
                  </p>
                </div>
              </div>

              {index < steps.length - 1 ? (
                <div className="hidden h-px flex-1 border-t border-dashed border-primary/12 xl:block" />
              ) : null}
            </div>
          )
        })}
      </div>
    </div>
  )
}

function EmptyOrdersPanel({
  title,
  description,
  ctaLabel,
}: {
  title: string
  description: string
  ctaLabel: string
}) {
  return (
    <SurfaceCard className="p-6">
      <div className="flex flex-col items-center gap-6 py-8 text-center lg:flex-row lg:items-center lg:justify-between lg:text-left">
        <div className="flex items-center gap-5">
          <PromoBookIllustration />
          <div className="max-w-xl">
            <h2 className="font-heading text-3xl font-bold text-foreground">
              {title}
            </h2>
            <p className="mt-3 text-[1rem] leading-7 text-muted-foreground">
              {description}
            </p>
          </div>
        </div>

        <Link to="/books">
          <Button className={`${primaryButtonClassName} h-12 bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-6 shadow-[0_18px_34px_rgba(109,76,255,0.24)] hover:opacity-95`}>
            <BookOpen className="mr-2 h-4 w-4" />
            {ctaLabel}
          </Button>
        </Link>
      </div>
    </SurfaceCard>
  )
}

function DiscoverMorePanel({
  title,
  description,
  ctaLabel,
}: {
  title: string
  description: string
  ctaLabel: string
}) {
  return (
    <SurfaceCard className="overflow-hidden p-6">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-center gap-5">
          <PromoBookIllustration />
          <div>
            <h2 className="font-heading text-[2rem] font-bold tracking-tight text-primary">
              {title}
            </h2>
            <p className="mt-2 text-[1rem] text-muted-foreground">{description}</p>
          </div>
        </div>

        <Link to="/books">
          <Button className={`${primaryButtonClassName} bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] shadow-[0_16px_30px_rgba(109,76,255,0.22)] hover:opacity-95`}>
            <Sparkles className="mr-2 h-4 w-4" />
            {ctaLabel}
          </Button>
        </Link>
      </div>
    </SurfaceCard>
  )
}

function HeroBookStack() {
  return (
    <div className="pointer-events-none absolute right-0 top-0 hidden h-full w-[320px] lg:block">
      <div className="absolute bottom-8 right-8 h-20 w-28 rounded-[24px] bg-[linear-gradient(135deg,rgba(116,89,248,0.96),rgba(154,127,255,0.74))] shadow-[0_18px_32px_rgba(109,76,255,0.18)]" />
      <div className="absolute bottom-14 right-28 h-16 w-24 rounded-[22px] bg-[linear-gradient(135deg,rgba(151,127,255,0.42),rgba(212,201,255,0.16))]" />
      <div className="absolute bottom-7 right-40 h-24 w-12 rounded-[18px] bg-[linear-gradient(180deg,rgba(124,92,255,0.09),rgba(124,92,255,0.02))]" />
      <div className="absolute right-8 top-10">
        <span className="absolute right-0 top-0 h-12 w-7 rotate-[-14deg] rounded-full border border-primary/12 bg-white/65 dark:bg-white/10" />
        <span className="absolute right-6 top-4 h-14 w-8 rotate-[18deg] rounded-full border border-primary/10 bg-white/55 dark:bg-white/8" />
      </div>
    </div>
  )
}

function PromoBookIllustration() {
  return (
    <div className="relative flex h-16 w-[150px] shrink-0 items-center justify-center">
      <Sparkles className="absolute left-2 top-2 h-4 w-4 text-primary/50" />
      <Sparkles className="absolute left-10 top-0 h-3 w-3 text-amber-300" />
      <Sparkles className="absolute right-4 top-3 h-4 w-4 text-primary/35" />
      <Sparkles className="absolute right-0 top-7 h-3 w-3 text-primary/25" />

      <div className="absolute bottom-1 left-2 h-[10px] w-[110px] rounded-full bg-primary/10 blur-sm" />
      <div className="relative flex items-end gap-1">
        <div className="h-10 w-2 origin-bottom rotate-[-28deg] rounded-full bg-[linear-gradient(180deg,rgba(255,207,111,1),rgba(255,176,37,0.95))]" />
        <div className="h-12 w-2 origin-bottom rotate-[-18deg] rounded-full bg-[linear-gradient(180deg,rgba(255,214,135,1),rgba(255,184,64,0.96))]" />
        <BookOpen className="h-14 w-20 text-primary drop-shadow-[0_10px_18px_rgba(109,76,255,0.22)]" strokeWidth={1.7} />
      </div>
    </div>
  )
}

function formatTime(locale: string, value: Date | number | string) {
  return new Intl.DateTimeFormat(locale, {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

function formatCompactDateTime(locale: string, value: Date | number | string) {
  return new Intl.DateTimeFormat(locale, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

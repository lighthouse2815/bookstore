import type { ReactNode } from 'react'
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
      'border border-sky-100 bg-sky-50/90 text-sky-700',
    badgeIconClassName: 'text-sky-600',
    stepIndex: 1,
  },
  SHIPPING: {
    badgeClassName:
      'border border-amber-100 bg-amber-50/90 text-amber-700',
    badgeIconClassName: 'text-amber-600',
    stepIndex: 2,
  },
  DELIVERED: {
    badgeClassName:
      'border border-emerald-100 bg-emerald-50/90 text-emerald-700',
    badgeIconClassName: 'text-emerald-600',
    stepIndex: 3,
  },
  CANCELLED: {
    badgeClassName:
      'border border-rose-100 bg-rose-50/90 text-rose-700',
    badgeIconClassName: 'text-rose-600',
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
  const { t, formatCurrency, formatDate, formatNumber, language, locale } =
    useLanguage()
  const { orders, isLoading, error } = useMyOrdersPage()
  const pageCopy = getMyOrdersPageCopy(language)

  async function handleCopyOrderId(orderId: string) {
    try {
      await navigator.clipboard.writeText(orderId)
      toast.success(pageCopy.copySuccess)
    } catch {
      toast.error(pageCopy.copyError)
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(252,248,255,1)_0%,rgba(246,240,255,0.96)_54%,rgba(255,255,255,1)_100%)]">
      <Header />

      <main className="flex-1 pb-16 pt-6 sm:pb-20 sm:pt-8">
        <div className="mx-auto flex w-full max-w-[1272px] flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <OrdersHero
            countLabel={t('orders.totalCount', {
              count: formatNumber(orders.length),
            })}
            title={t('orders.title')}
            continueShoppingLabel={t('common.continueShopping')}
          />

          {isLoading ? (
            <StatePanel>
              <p className="text-slate-500">{t('common.loading')}</p>
            </StatePanel>
          ) : error ? (
            <StatePanel tone="error">
              <p className="font-semibold">{error}</p>
            </StatePanel>
          ) : orders.length === 0 ? (
            <EmptyOrdersPanel
              title={t('orders.emptyTitle')}
              description={t('orders.emptyDescription')}
              ctaLabel={pageCopy.exploreNow}
            />
          ) : (
            <div className="space-y-5">
              {orders.map((order) => (
                <OrderHistoryCard
                  key={order.orderId}
                  locale={locale}
                  onCopyOrderId={handleCopyOrderId}
                  order={order}
                  pageCopy={pageCopy}
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
            </div>
          )}

          <DiscoverMorePanel
            title={pageCopy.discoverTitle}
            description={pageCopy.discoverDescription}
            ctaLabel={pageCopy.exploreNow}
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
    <section className="relative overflow-hidden rounded-[34px] border border-primary/10 bg-white/82 px-6 py-7 shadow-[0_24px_80px_rgba(137,92,255,0.1)] backdrop-blur sm:px-8 lg:px-10">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_left,rgba(198,177,255,0.14),transparent_28%),radial-gradient(circle_at_center,rgba(150,121,255,0.1),transparent_24%),linear-gradient(180deg,rgba(250,246,255,0.96)_0%,rgba(246,240,255,0.86)_100%)]" />
      <Sparkles className="pointer-events-none absolute left-10 top-10 hidden h-4 w-4 text-primary/25 lg:block" />
      <Sparkles className="pointer-events-none absolute left-[32%] top-16 hidden h-5 w-5 text-primary/18 lg:block" />
      <div className="pointer-events-none absolute bottom-3 left-[58%] hidden lg:block">
        <BookOpen className="h-24 w-24 text-primary/10" strokeWidth={1.4} />
      </div>
      <HeroBookStack />

      <div className="relative flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex min-w-0 items-start gap-5">
          <span className="flex size-[72px] shrink-0 items-center justify-center rounded-[24px] border border-primary/10 bg-white text-primary shadow-[0_18px_38px_rgba(137,92,255,0.12)]">
            <ShoppingBag className="h-9 w-9" strokeWidth={1.7} />
          </span>

          <div className="min-w-0">
            <h1 className="font-heading text-4xl font-bold tracking-tight text-slate-950">
              {title}
            </h1>
            <p className="mt-2 text-[1.15rem] text-slate-500">{countLabel}</p>
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
  pageCopy,
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
  pageCopy: MyOrdersPageCopy
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
  const statusTone = ORDER_STATUS_TONES[order.status]
  const createdTimeLabel = formatTime(locale, order.createdAt)

  return (
    <SurfacePanel>
      <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
        <div className="flex min-w-0 items-start gap-4">
          <span className="flex size-[62px] shrink-0 items-center justify-center rounded-[20px] bg-[linear-gradient(180deg,rgba(124,92,255,0.12)_0%,rgba(124,92,255,0.04)_100%)] text-primary shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]">
            <ScrollText className="h-8 w-8" strokeWidth={1.8} />
          </span>

          <div className="min-w-0">
            <p className="text-[12px] font-semibold uppercase tracking-[0.16em] text-slate-400">
              {orderIdTitle}
            </p>
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <h2 className="break-all font-heading text-[2rem] font-bold tracking-tight text-slate-950">
                {order.orderId}
              </h2>
              <button
                type="button"
                aria-label={pageCopy.copyOrderId}
                onClick={() => void onCopyOrderId(order.orderId)}
                className="inline-flex size-9 items-center justify-center rounded-xl border border-transparent text-slate-400 transition hover:border-primary/10 hover:bg-primary/6 hover:text-primary"
              >
                <Copy className="h-4 w-4" />
              </button>
            </div>
            <p className="mt-2 text-[1rem] text-slate-500">
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
        pageCopy={pageCopy}
      />
    </SurfacePanel>
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
    <div className="rounded-[20px] border border-primary/8 bg-[linear-gradient(180deg,rgba(255,255,255,0.98)_0%,rgba(250,246,255,0.94)_100%)] px-5 py-5 shadow-[0_10px_28px_rgba(137,92,255,0.04)]">
      <div className="flex items-center gap-4">
        <span className="flex size-[50px] shrink-0 items-center justify-center rounded-[16px] bg-[linear-gradient(180deg,rgba(124,92,255,0.12),rgba(124,92,255,0.04))] text-primary">
          <Icon className="h-6 w-6" strokeWidth={1.8} />
        </span>

        <div className="min-w-0">
          <p
            className={cn(
              'text-[12px] font-semibold uppercase tracking-[0.14em] text-slate-400',
              labelClassName,
            )}
          >
            {label}
          </p>
          <p className="mt-1.5 break-words text-[1.18rem] font-bold text-slate-950">
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
  pageCopy,
}: {
  activeIndex: number
  createdAt: string
  isCancelled: boolean
  locale: string
  pageCopy: MyOrdersPageCopy
}) {
  const steps = [
    pageCopy.pendingStep,
    pageCopy.processingStep,
    pageCopy.shippingStep,
    pageCopy.completedStep,
  ]
  const activeTimestamp = formatCompactDateTime(locale, createdAt)

  return (
    <div className="mt-5 rounded-[24px] border border-primary/6 bg-[linear-gradient(135deg,rgba(252,249,255,0.98)_0%,rgba(245,240,255,0.9)_100%)] px-5 py-5">
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
                      'border-primary/12 bg-white text-primary shadow-[0_10px_20px_rgba(124,92,255,0.08)]',
                    isPending &&
                      'border-slate-200 bg-white/80 text-slate-400',
                  )}
                >
                  <Icon className="h-5 w-5" strokeWidth={1.8} />
                </span>

                <div className="min-w-0">
                  <p
                    className={cn(
                      'text-[15px] font-medium',
                      isActive ? 'font-semibold text-primary' : 'text-slate-500',
                    )}
                  >
                    {label}
                  </p>
                  <p
                    className={cn(
                      'mt-1 text-[13px]',
                      isActive ? 'text-slate-500' : 'text-transparent',
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
    <SurfacePanel>
      <div className="flex flex-col items-center gap-6 py-8 text-center lg:flex-row lg:items-center lg:justify-between lg:text-left">
        <div className="flex items-center gap-5">
          <PromoBookIllustration />
          <div className="max-w-xl">
            <h2 className="font-heading text-3xl font-bold text-slate-950">
              {title}
            </h2>
            <p className="mt-3 text-[1rem] leading-7 text-slate-500">
              {description}
            </p>
          </div>
        </div>

        <Link to="/books">
          <Button className="h-12 rounded-2xl bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-6 shadow-[0_18px_34px_rgba(109,76,255,0.24)] hover:opacity-95">
            <BookOpen className="mr-2 h-4 w-4" />
            {ctaLabel}
          </Button>
        </Link>
      </div>
    </SurfacePanel>
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
    <SurfacePanel className="overflow-hidden">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-center gap-5">
          <PromoBookIllustration />
          <div>
            <h2 className="font-heading text-[2rem] font-bold tracking-tight text-primary">
              {title}
            </h2>
            <p className="mt-2 text-[1rem] text-slate-500">{description}</p>
          </div>
        </div>

        <Link to="/books">
          <Button className="h-11 rounded-2xl bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-5 shadow-[0_16px_30px_rgba(109,76,255,0.22)] hover:opacity-95">
            <Sparkles className="mr-2 h-4 w-4" />
            {ctaLabel}
          </Button>
        </Link>
      </div>
    </SurfacePanel>
  )
}

function StatePanel({
  children,
  tone = 'default',
}: {
  children: ReactNode
  tone?: 'default' | 'error'
}) {
  return (
    <SurfacePanel>
      <div
        className={cn(
          'rounded-[24px] border border-dashed px-6 py-12 text-center',
          tone === 'default' && 'border-primary/12 bg-primary/4',
          tone === 'error' && 'border-destructive/20 bg-destructive/5 text-destructive',
        )}
      >
        {children}
      </div>
    </SurfacePanel>
  )
}

function SurfacePanel({
  children,
  className,
}: {
  children: ReactNode
  className?: string
}) {
  return (
    <section
      className={cn(
        'rounded-[28px] border border-primary/10 bg-white/92 p-6 shadow-[0_14px_38px_rgba(137,92,255,0.08)] backdrop-blur',
        className,
      )}
    >
      {children}
    </section>
  )
}

function HeroBookStack() {
  return (
    <div className="pointer-events-none absolute right-0 top-0 hidden h-full w-[320px] lg:block">
      <div className="absolute bottom-8 right-8 h-20 w-28 rounded-[24px] bg-[linear-gradient(135deg,rgba(116,89,248,0.96),rgba(154,127,255,0.74))] shadow-[0_18px_32px_rgba(109,76,255,0.18)]" />
      <div className="absolute bottom-14 right-28 h-16 w-24 rounded-[22px] bg-[linear-gradient(135deg,rgba(151,127,255,0.42),rgba(212,201,255,0.16))]" />
      <div className="absolute bottom-7 right-40 h-24 w-12 rounded-[18px] bg-[linear-gradient(180deg,rgba(124,92,255,0.09),rgba(124,92,255,0.02))]" />
      <div className="absolute right-8 top-10">
        <span className="absolute right-0 top-0 h-12 w-7 rotate-[-14deg] rounded-full border border-primary/12 bg-white/65" />
        <span className="absolute right-6 top-4 h-14 w-8 rotate-[18deg] rounded-full border border-primary/10 bg-white/55" />
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

type MyOrdersPageCopy = {
  completedStep: string
  copyError: string
  copyOrderId: string
  copySuccess: string
  discoverDescription: string
  discoverTitle: string
  exploreNow: string
  pendingStep: string
  processingStep: string
  shippingStep: string
}

function getMyOrdersPageCopy(language: 'vi' | 'en'): MyOrdersPageCopy {
  if (language === 'en') {
    return {
      completedStep: 'Completed',
      copyError: 'Unable to copy the order ID.',
      copyOrderId: 'Copy order ID',
      copySuccess: 'Order ID copied.',
      discoverDescription: 'Thousands of curated titles are waiting for you.',
      discoverTitle: 'Discover more great books',
      exploreNow: 'Explore now',
      pendingStep: 'Pending confirmation',
      processingStep: 'Processing',
      shippingStep: 'Out for delivery',
    }
  }

  return {
    completedStep: 'Ho\u00e0n t\u1ea5t',
    copyError: 'Kh\u00f4ng th\u1ec3 sao ch\u00e9p m\u00e3 \u0111\u01a1n h\u00e0ng.',
    copyOrderId: 'Sao ch\u00e9p m\u00e3 \u0111\u01a1n h\u00e0ng',
    copySuccess: '\u0110\u00e3 sao ch\u00e9p m\u00e3 \u0111\u01a1n h\u00e0ng.',
    discoverDescription:
      'H\u00e0ng ng\u00e0n t\u1ef1a s\u00e1ch ch\u1ea5t l\u01b0\u1ee3ng \u0111ang ch\u1edd b\u1ea1n kh\u00e1m ph\u00e1.',
    discoverTitle: 'Kh\u00e1m ph\u00e1 th\u00eam s\u00e1ch hay',
    exploreNow: 'Kh\u00e1m ph\u00e1 ngay',
    pendingStep: 'Ch\u1edd x\u00e1c nh\u1eadn',
    processingStep: '\u0110ang x\u1eed l\u00fd',
    shippingStep: '\u0110ang giao h\u00e0ng',
  }
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

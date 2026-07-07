import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  CalendarDays,
  Clock3,
  MapPin,
  Package2,
  Phone,
  ReceiptText,
  ScrollText,
  UserRound,
  WalletCards,
  type LucideIcon,
} from 'lucide-react'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useOrderDetailPage } from '@/hooks/use-order-detail-page'
import type { OrderResponse, OrderStatus } from '@/types/order'
import { cn } from '@/utils'
import { BOOK_DEFAULT_COVER } from '@/utils/book-cover'
import {
  getOrderStatusLabel,
  getPaymentMethodLabel,
  getPaymentStatusLabel,
} from '@/utils/i18n'

const ORDER_STATUS_TONES: Record<
  OrderStatus,
  {
    badgeClassName: string
    iconClassName: string
  }
> = {
  PENDING: {
    badgeClassName: 'bg-sky-50 text-sky-700 ring-1 ring-sky-100',
    iconClassName: 'text-sky-600',
  },
  CONFIRMED: {
    badgeClassName: 'bg-primary/10 text-primary ring-1 ring-primary/10',
    iconClassName: 'text-primary',
  },
  SHIPPING: {
    badgeClassName: 'bg-amber-50 text-amber-700 ring-1 ring-amber-100',
    iconClassName: 'text-amber-600',
  },
  DELIVERED: {
    badgeClassName: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-100',
    iconClassName: 'text-emerald-600',
  },
  CANCELLED: {
    badgeClassName: 'bg-rose-50 text-rose-700 ring-1 ring-rose-100',
    iconClassName: 'text-rose-600',
  },
}

const PAYMENT_STATUS_TONES: Record<
  OrderResponse['paymentStatus'],
  {
    tileClassName: string
    iconClassName: string
  }
> = {
  PENDING: {
    tileClassName: 'border-rose-100 bg-rose-50/70',
    iconClassName: 'text-rose-500',
  },
  UNPAID: {
    tileClassName: 'border-rose-100 bg-rose-50/70',
    iconClassName: 'text-rose-500',
  },
  PAID: {
    tileClassName: 'border-emerald-100 bg-emerald-50/70',
    iconClassName: 'text-emerald-500',
  },
  FAILED: {
    tileClassName: 'border-rose-100 bg-rose-50/70',
    iconClassName: 'text-rose-500',
  },
  CANCELLED: {
    tileClassName: 'border-slate-200 bg-slate-50/80',
    iconClassName: 'text-slate-500',
  },
  REFUNDED: {
    tileClassName: 'border-amber-100 bg-amber-50/70',
    iconClassName: 'text-amber-500',
  },
}

export default function OrderDetailPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const { order, isLoading, error } = useOrderDetailPage()

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(252,248,255,1)_0%,rgba(246,240,255,0.96)_54%,rgba(255,255,255,1)_100%)]">
      <Header />

      <main className="flex-1 pb-16 pt-6 sm:pb-20 sm:pt-8">
        <div className="mx-auto flex w-full max-w-[1272px] flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h1 className="font-heading text-4xl font-bold tracking-tight text-slate-950">
                {t('orders.detailTitle')}
              </h1>
              {order ? (
                <p className="mt-3 break-all text-sm text-slate-500 sm:text-base">
                  {t('orders.orderId')}: {order.orderId}
                </p>
              ) : null}
            </div>

            <Link to="/orders">
              <Button
                variant="outline"
                className="h-11 rounded-2xl border-primary/15 px-5 text-primary hover:bg-primary/6"
              >
                <Clock3 className="mr-2 h-4 w-4" />
                {t('orderDetail.orderHistory')}
              </Button>
            </Link>
          </div>

          {isLoading ? (
            <SurfacePanel>
              <div className="rounded-[24px] border border-dashed border-primary/15 bg-primary/4 px-6 py-12 text-center text-slate-500">
                {t('common.loading')}
              </div>
            </SurfacePanel>
          ) : error || !order ? (
            <SurfacePanel>
              <div className="rounded-[24px] border border-dashed border-destructive/20 bg-destructive/5 px-6 py-12 text-center font-semibold text-destructive">
                {error || t('notFound.description')}
              </div>
            </SurfacePanel>
          ) : (
            <OrderDetailContent
              formatCurrency={formatCurrency}
              formatDate={formatDate}
              formatNumber={formatNumber}
              order={order}
              t={t}
            />
          )}
        </div>
      </main>

      <Footer />
    </div>
  )
}

function OrderDetailContent({
  formatCurrency,
  formatDate,
  formatNumber,
  order,
  t,
}: {
  formatCurrency: (value: number) => string
  formatDate: (value: Date | number | string) => string
  formatNumber: (value: number) => string
  order: OrderResponse
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  const orderTone = ORDER_STATUS_TONES[order.status]
  const paymentTone = PAYMENT_STATUS_TONES[order.paymentStatus]

  return (
    <div className="space-y-6">
      <section className="grid gap-6 xl:grid-cols-[minmax(0,2.02fr)_390px]">
        <SurfacePanel>
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div className="flex items-start gap-4">
              <span className="flex size-[50px] shrink-0 items-center justify-center rounded-full bg-[linear-gradient(180deg,rgba(123,92,255,0.1),rgba(123,92,255,0.04))] text-primary">
                <CalendarDays className="h-[22px] w-[22px]" />
              </span>
              <div>
                <p className="text-[12px] font-semibold uppercase tracking-[0.16em] text-slate-400">
                  {t('orders.createdAt')}
                </p>
                <p className="mt-1.5 text-[1.7rem] font-bold text-slate-950">
                  {formatDate(order.createdAt)}
                </p>
              </div>
            </div>

            <span
              className={cn(
                'inline-flex items-center gap-2 rounded-full px-4 py-2 text-[15px] font-semibold shadow-[0_6px_18px_rgba(125,173,255,0.15)]',
                orderTone.badgeClassName,
              )}
            >
              <Clock3 className={cn('h-4 w-4', orderTone.iconClassName)} />
              {getOrderStatusLabel(order.status, t)}
            </span>
          </div>

          <div className="mt-6 grid gap-4 md:grid-cols-2">
            <DetailTile
              icon={UserRound}
              iconClassName="text-primary"
              label={t('orders.receiverName')}
              value={order.receiverName}
            />
            <DetailTile
              icon={Phone}
              iconClassName="text-rose-500"
              label={t('orders.receiverPhone')}
              value={order.receiverPhone}
              tileClassName="border-rose-100 bg-rose-50/65"
            />
            <DetailTile
              icon={WalletCards}
              iconClassName="text-primary"
              label={t('orders.paymentMethod')}
              value={getPaymentMethodLabel(order.paymentMethod, t)}
            />
            <DetailTile
              icon={ScrollText}
              iconClassName={paymentTone.iconClassName}
              label={t('orders.paymentStatus')}
              value={getPaymentStatusLabel(order.paymentStatus, t)}
              tileClassName={paymentTone.tileClassName}
            />
            <DetailTile
              icon={MapPin}
              iconClassName="text-primary"
              label={t('orders.receiverAddress')}
              value={order.receiverAddress}
              tileClassName="border-primary/12 bg-primary/4 md:col-span-2"
            />
          </div>
        </SurfacePanel>

        <SurfacePanel className="self-start">
          <SectionHeading
            icon={ReceiptText}
            title={t('orderDetail.orderSummary')}
            variant="solid"
          />

          <div className="mt-6 space-y-1">
            <SummaryRow
              label={t('orderDetail.productTotal')}
              value={formatCurrency(order.productTotal)}
            />
            <SummaryRow
              label={t('orders.discount')}
              value={formatCurrency(order.discountAmount)}
            />
            <SummaryRow
              label={t('orders.shippingFee')}
              value={formatCurrency(order.shippingFee)}
            />
          </div>

          <div className="mt-6 rounded-[24px] bg-[linear-gradient(135deg,rgba(124,92,255,0.1),rgba(124,92,255,0.04))] px-5 py-4">
            <SummaryRow
              label={t('orders.finalAmount')}
              value={formatCurrency(order.finalAmount)}
              emphasized
              showDivider={false}
            />
          </div>
        </SurfacePanel>
      </section>

      <SurfacePanel>
        <SectionHeading
          icon={Package2}
          title={t('orderDetail.itemsTitle')}
          variant="plain"
        />

        <div className="mt-5">
          {order.items.map((item) => (
            <article
              key={item.id}
              className="flex flex-col gap-4 border-b border-primary/8 py-5 first:pt-0 md:flex-row md:items-start md:justify-between"
            >
              <div className="flex min-w-0 items-start gap-4">
                <Link
                  to={`/books/${item.bookId}`}
                  className="overflow-hidden rounded-[14px] border border-primary/10 bg-white shadow-[0_8px_22px_rgba(137,92,255,0.08)] transition hover:border-primary/30 hover:shadow-[0_14px_28px_rgba(137,92,255,0.12)]"
                >
                  <img
                    src={BOOK_DEFAULT_COVER}
                    alt={item.bookTitle}
                    className="h-[74px] w-[58px] object-cover"
                  />
                </Link>

                <div className="min-w-0 pt-1">
                  <Link
                    to={`/books/${item.bookId}`}
                    className="text-[1.1rem] font-bold text-slate-950 transition hover:text-primary"
                  >
                    {item.bookTitle}
                  </Link>
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    <Badge variant="outline" className="rounded-full px-2.5 py-0.5 text-[11px]">
                      {item.itemType === 'DIGITAL_ASSET'
                        ? t('orderDetail.digitalItemLabel')
                        : t('orderDetail.physicalItemLabel')}
                    </Badge>
                    {item.itemType === 'DIGITAL_ASSET' &&
                    item.digitalAssetId &&
                    (order.paymentStatus === 'PAID' || order.status === 'DELIVERED') ? (
                      <Link
                        to={`/library/${item.digitalAssetId}`}
                        className="text-xs font-semibold text-primary hover:underline"
                      >
                        {t('orderDetail.openLibraryAsset')}
                      </Link>
                    ) : null}
                  </div>
                  <p className="mt-2 text-[15px] text-slate-500">
                    {t('checkout.quantityShort', {
                      count: item.quantity,
                    })}{' '}
                    {'\u2022'} {formatCurrency(item.unitPrice)}
                  </p>
                </div>
              </div>

              <p className="shrink-0 pt-1 text-[1.05rem] font-bold text-slate-950 md:text-[1.15rem]">
                {formatCurrency(item.lineTotal)}
              </p>
            </article>
          ))}
        </div>

        <p className="pt-4 text-[15px] font-semibold text-primary">
          {t('admin.orders.productCount', {
            count: formatNumber(order.items.length),
          })}
        </p>
      </SurfacePanel>
    </div>
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

function SectionHeading({
  icon: Icon,
  title,
  variant = 'plain',
}: {
  icon: LucideIcon
  title: string
  variant?: 'plain' | 'solid'
}) {
  return (
    <div className="flex items-center gap-3">
      {variant === 'solid' ? (
        <span className="flex size-12 items-center justify-center rounded-full bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] text-white shadow-[0_16px_28px_rgba(109,76,255,0.22)]">
          <Icon className="h-5 w-5" />
        </span>
      ) : (
        <span className="flex items-center justify-center text-primary">
          <Icon className="h-5 w-5" />
        </span>
      )}
      <h2 className="font-heading text-[1.9rem] font-bold text-slate-950">{title}</h2>
    </div>
  )
}

function DetailTile({
  icon: Icon,
  iconClassName,
  label,
  tileClassName,
  value,
}: {
  icon: LucideIcon
  iconClassName: string
  label: string
  tileClassName?: string
  value: string
}) {
  return (
    <div
      className={cn(
        'rounded-[18px] border border-primary/10 bg-[linear-gradient(180deg,rgba(255,255,255,0.99)_0%,rgba(249,245,255,0.94)_100%)] px-4 py-4 sm:px-5',
        tileClassName,
      )}
    >
      <div className="flex items-start gap-3">
        <span className="mt-[1px] shrink-0">
          <Icon className={cn('h-[18px] w-[18px]', iconClassName)} />
        </span>
        <div className="min-w-0">
          <p className="text-[12px] font-semibold uppercase tracking-[0.14em] text-slate-400">
            {label}
          </p>
          <p className="mt-2 break-words text-[1.03rem] font-bold leading-7 text-slate-950">
            {value}
          </p>
        </div>
      </div>
    </div>
  )
}

function SummaryRow({
  label,
  value,
  emphasized = false,
  showDivider = true,
}: {
  label: string
  value: string
  emphasized?: boolean
  showDivider?: boolean
}) {
  return (
    <div
      className={cn(
        'flex items-center justify-between gap-4 py-4',
        emphasized && 'flex-col items-center gap-2.5 text-center',
        showDivider && 'border-b border-primary/8',
      )}
    >
      <span
        className={cn(
          'text-sm',
          emphasized
            ? 'whitespace-nowrap text-center text-2xl font-semibold tracking-tight text-primary'
            : 'text-slate-500',
        )}
      >
        {label}
      </span>
      <span
        className={cn(
          'text-right text-[15px] font-semibold text-slate-950',
          emphasized && 'w-full text-center font-heading text-[2rem] text-primary',
        )}
      >
        {value}
      </span>
    </div>
  )
}


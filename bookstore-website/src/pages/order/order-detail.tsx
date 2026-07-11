import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowLeftRight,
  CircleDollarSign,
  CalendarDays,
  Clock3,
  MapPin,
  Package2,
  Phone,
  ReceiptText,
  ScrollText,
  UserRound,
  WalletCards,
  XCircle,
  type LucideIcon,
} from 'lucide-react'
import { Button } from '@/components/common/button'
import { Badge } from '@/components/common/badge'
import {
  StatePanel,
  SurfaceCard,
  primaryButtonClassName,
  secondaryButtonClassName,
} from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { OrderTimelineList } from '@/components/order/order-timeline-list'
import { useLanguage } from '@/contexts/language-context'
import { useOrderDetailPage } from '@/hooks/use-order-detail-page'
import type {
  OrderResponse,
  OrderStatus,
  OrderTimelineEventResponse,
} from '@/types/order'
import type {
  ReturnRequestResponse,
  ReturnRequestStatus,
} from '@/types/return-request'
import { cn } from '@/utils'
import { BOOK_DEFAULT_COVER } from '@/utils/book-cover'
import {
  getOrderStatusLabel,
  getPaymentMethodLabel,
  getPaymentStatusLabel,
  getReturnRequestStatusLabel,
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
  EXPIRED: {
    tileClassName: 'border-slate-200 bg-slate-50/80',
    iconClassName: 'text-slate-500',
  },
}

export default function OrderDetailPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const {
    order,
    timeline,
    isLoading,
    error,
    latestReturnRequest,
    isReturnLoading,
    canCreateReturnRequest,
    canCancelReturnRequest,
    canCancelOrder,
    isCreateDialogOpen,
    returnReason,
    requestedRefundAmount,
    isSubmittingReturnRequest,
    isCancellingReturnRequest,
    isCancelOrderDialogOpen,
    cancelOrderReason,
    isCancellingOrder,
    openCreateDialog,
    closeCreateDialog,
    handleSubmitReturnRequest,
    handleCancelReturnRequest,
    openCancelOrderDialog,
    closeCancelOrderDialog,
    handleCancelOrder,
    setReturnReason,
    setRequestedRefundAmount,
    setCancelOrderReason,
  } = useOrderDetailPage()

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
                className={`${secondaryButtonClassName} border-primary/15 text-primary hover:bg-primary/6`}
              >
                <Clock3 className="mr-2 h-4 w-4" />
                {t('orderDetail.orderHistory')}
              </Button>
            </Link>
          </div>

          {isLoading ? (
            <StatePanel title={t('common.loading')} />
          ) : error || !order ? (
            <StatePanel
              tone="error"
              title={error || t('notFound.description')}
            />
          ) : (
            <OrderDetailContent
              formatCurrency={formatCurrency}
              formatDate={formatDate}
              formatNumber={formatNumber}
              order={order}
              timeline={timeline}
              latestReturnRequest={latestReturnRequest}
              isReturnLoading={isReturnLoading}
              canCreateReturnRequest={canCreateReturnRequest}
              canCancelReturnRequest={canCancelReturnRequest}
              canCancelOrder={canCancelOrder}
              isSubmittingReturnRequest={isSubmittingReturnRequest}
              isCancellingReturnRequest={isCancellingReturnRequest}
              onOpenCreateDialog={openCreateDialog}
              onCancelReturnRequest={handleCancelReturnRequest}
              onOpenCancelOrderDialog={openCancelOrderDialog}
              t={t}
            />
          )}
        </div>
      </main>

      {isCreateDialogOpen ? (
        <ReturnRequestDialog
          reason={returnReason}
          requestedRefundAmount={requestedRefundAmount}
          isSubmitting={isSubmittingReturnRequest}
          onClose={closeCreateDialog}
          onReasonChange={setReturnReason}
          onRequestedRefundAmountChange={setRequestedRefundAmount}
          onSubmit={handleSubmitReturnRequest}
          t={t}
        />
      ) : null}

      {isCancelOrderDialogOpen ? (
        <CancelOrderDialog
          reason={cancelOrderReason}
          isSubmitting={isCancellingOrder}
          onClose={closeCancelOrderDialog}
          onReasonChange={setCancelOrderReason}
          onSubmit={handleCancelOrder}
        />
      ) : null}

      <Footer />
    </div>
  )
}

function OrderDetailContent({
  formatCurrency,
  formatDate,
  formatNumber,
  order,
  timeline,
  latestReturnRequest,
  isReturnLoading,
  canCreateReturnRequest,
  canCancelReturnRequest,
  canCancelOrder,
  isSubmittingReturnRequest,
  isCancellingReturnRequest,
  onOpenCreateDialog,
  onCancelReturnRequest,
  onOpenCancelOrderDialog,
  t,
}: {
  formatCurrency: (value: number) => string
  formatDate: (value: Date | number | string) => string
  formatNumber: (value: number) => string
  order: OrderResponse
  timeline: OrderTimelineEventResponse[]
  latestReturnRequest: ReturnRequestResponse | null
  isReturnLoading: boolean
  canCreateReturnRequest: boolean
  canCancelReturnRequest: boolean
  canCancelOrder: boolean
  isSubmittingReturnRequest: boolean
  isCancellingReturnRequest: boolean
  onOpenCreateDialog: () => void
  onCancelReturnRequest: () => void
  onOpenCancelOrderDialog: () => void
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

      {canCancelOrder ? (
        <SurfacePanel>
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <SectionHeading icon={XCircle} title="Hủy đơn hàng" variant="plain" />
              <p className="mt-2 text-sm leading-6 text-slate-500">
                Đơn đang chờ thanh toán. Hàng tồn và ưu đãi sẽ được hoàn lại sau khi hủy.
              </p>
            </div>
            <Button
              type="button"
              variant="outline"
              onClick={onOpenCancelOrderDialog}
              className="border-rose-200 text-rose-700 hover:bg-rose-50"
            >
              <XCircle className="mr-2 h-4 w-4" />
              Hủy đơn
            </Button>
          </div>
        </SurfacePanel>
      ) : null}

      <ReturnRequestPanel
        canCancelReturnRequest={canCancelReturnRequest}
        canCreateReturnRequest={canCreateReturnRequest}
        formatCurrency={formatCurrency}
        formatDate={formatDate}
        isCancellingReturnRequest={isCancellingReturnRequest}
        isReturnLoading={isReturnLoading}
        isSubmittingReturnRequest={isSubmittingReturnRequest}
        latestReturnRequest={latestReturnRequest}
        onCancelReturnRequest={onCancelReturnRequest}
        onOpenCreateDialog={onOpenCreateDialog}
        t={t}
      />

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

      <SurfacePanel>
        <SectionHeading
          icon={Clock3}
          title={t('orderTimeline.title')}
          variant="plain"
        />
        <p className="mt-3 text-sm leading-6 text-slate-500">
          {t('orderTimeline.description')}
        </p>

        <div className="mt-6">
          <OrderTimelineList
            emptyLabel={t('orderTimeline.empty')}
            events={timeline}
          />
        </div>
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
    <SurfaceCard className={cn('p-6', className)}>
      {children}
    </SurfaceCard>
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
      <h2 className="font-heading text-[1.9rem] font-bold text-foreground">{title}</h2>
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
        'rounded-[18px] border border-border/70 bg-background/80 px-4 py-4 sm:px-5 dark:bg-background/55',
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

const RETURN_REQUEST_STATUS_TONES: Record<
  ReturnRequestStatus,
  {
    badgeClassName: string
    iconClassName: string
  }
> = {
  PENDING: {
    badgeClassName: 'bg-amber-50 text-amber-700 ring-1 ring-amber-100',
    iconClassName: 'text-amber-600',
  },
  APPROVED: {
    badgeClassName: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-100',
    iconClassName: 'text-emerald-600',
  },
  REJECTED: {
    badgeClassName: 'bg-rose-50 text-rose-700 ring-1 ring-rose-100',
    iconClassName: 'text-rose-600',
  },
  CANCELLED: {
    badgeClassName: 'bg-slate-100 text-slate-700 ring-1 ring-slate-200',
    iconClassName: 'text-slate-500',
  },
}

function ReturnRequestPanel({
  canCancelReturnRequest,
  canCreateReturnRequest,
  formatCurrency,
  formatDate,
  isCancellingReturnRequest,
  isReturnLoading,
  latestReturnRequest,
  onCancelReturnRequest,
  onOpenCreateDialog,
  t,
}: {
  canCancelReturnRequest: boolean
  canCreateReturnRequest: boolean
  formatCurrency: (value: number) => string
  formatDate: (value: Date | number | string) => string
  isCancellingReturnRequest: boolean
  isReturnLoading: boolean
  isSubmittingReturnRequest: boolean
  latestReturnRequest: ReturnRequestResponse | null
  onCancelReturnRequest: () => void
  onOpenCreateDialog: () => void
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <SurfacePanel>
      <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <SectionHeading
            icon={CircleDollarSign}
            title={t('returnRequests.sectionTitle')}
            variant="plain"
          />
          <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-500">
            {t('returnRequests.sectionDescription')}
          </p>
        </div>

        {canCreateReturnRequest ? (
          <Button
            type="button"
            onClick={onOpenCreateDialog}
            className={primaryButtonClassName}
          >
            <ArrowLeftRight className="mr-2 h-4 w-4" />
            {t('returnRequests.createAction')}
          </Button>
        ) : null}
      </div>

      <div className="mt-6">
        {isReturnLoading ? (
          <StatePanel
            minHeightClassName="min-h-[160px]"
            title={t('common.loading')}
          />
        ) : latestReturnRequest == null ? (
          <StatePanel
            minHeightClassName="min-h-[160px]"
            title={t('returnRequests.emptyForOrder')}
          />
        ) : (
          <div className="rounded-[24px] border border-primary/10 bg-[linear-gradient(180deg,rgba(255,255,255,0.99)_0%,rgba(249,245,255,0.94)_100%)] p-5 shadow-[0_12px_30px_rgba(137,92,255,0.06)]">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div className="space-y-3">
                <div className="flex flex-wrap items-center gap-3">
                  <h3 className="font-heading text-2xl font-bold text-slate-950">
                    {t('returnRequests.latestRequestTitle')}
                  </h3>
                  <span
                    className={cn(
                      'inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold',
                      RETURN_REQUEST_STATUS_TONES[latestReturnRequest.status]
                        .badgeClassName,
                    )}
                  >
                    <CircleDollarSign
                      className={cn(
                        'h-4 w-4',
                        RETURN_REQUEST_STATUS_TONES[latestReturnRequest.status]
                          .iconClassName,
                      )}
                    />
                    {getReturnRequestStatusLabel(latestReturnRequest.status, t)}
                  </span>
                </div>
                <p className="text-sm text-slate-500">
                  {t('returnRequests.createdAt')}: {formatDate(latestReturnRequest.createdAt)}
                </p>
                <p className="text-sm leading-6 text-slate-600">
                  {latestReturnRequest.reason}
                </p>
              </div>

              {canCancelReturnRequest ? (
                <Button
                  type="button"
                  variant="outline"
                  onClick={onCancelReturnRequest}
                  disabled={isCancellingReturnRequest}
                  className={`${secondaryButtonClassName} border-primary/15 text-primary hover:bg-primary/6`}
                >
                  {isCancellingReturnRequest
                    ? t('common.processing')
                    : t('returnRequests.cancelAction')}
                </Button>
              ) : null}
            </div>

            <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <DetailTile
                icon={ReceiptText}
                iconClassName="text-primary"
                label={t('returnRequests.requestedAmount')}
                value={
                  latestReturnRequest.requestedRefundAmount == null
                    ? t('returnRequests.notProvided')
                    : formatCurrency(latestReturnRequest.requestedRefundAmount)
                }
              />
              <DetailTile
                icon={ReceiptText}
                iconClassName="text-emerald-500"
                label={t('returnRequests.approvedAmount')}
                value={
                  latestReturnRequest.approvedRefundAmount == null
                    ? t('returnRequests.notProcessed')
                    : formatCurrency(latestReturnRequest.approvedRefundAmount)
                }
                tileClassName="border-emerald-100 bg-emerald-50/65"
              />
              <DetailTile
                icon={Clock3}
                iconClassName="text-primary"
                label={t('returnRequests.processedAt')}
                value={
                  latestReturnRequest.processedAt == null
                    ? t('returnRequests.notProcessed')
                    : formatDate(latestReturnRequest.processedAt)
                }
              />
              <DetailTile
                icon={UserRound}
                iconClassName="text-primary"
                label={t('returnRequests.processedBy')}
                value={
                  latestReturnRequest.processedByName ??
                  t('returnRequests.notProcessed')
                }
              />
            </div>

            {latestReturnRequest.adminNote ? (
              <div className="mt-5 rounded-[20px] border border-slate-200 bg-slate-50/90 px-4 py-4">
                <p className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">
                  {t('returnRequests.adminNote')}
                </p>
                <p className="mt-2 text-sm leading-6 text-slate-600">
                  {latestReturnRequest.adminNote}
                </p>
              </div>
            ) : null}
          </div>
        )}
      </div>
    </SurfacePanel>
  )
}

function CancelOrderDialog({
  reason,
  isSubmitting,
  onClose,
  onReasonChange,
  onSubmit,
}: {
  reason: string
  isSubmitting: boolean
  onClose: () => void
  onReasonChange: (value: string) => void
  onSubmit: () => void
}) {
  const isReasonValid = reason.trim().length > 0 && reason.trim().length <= 500

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 px-4">
      <div className="w-full max-w-xl rounded-[28px] border border-border/70 bg-card p-6 shadow-2xl">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="font-heading text-3xl font-bold text-foreground">Xác nhận hủy đơn</h2>
            <p className="mt-2 text-sm leading-6 text-muted-foreground">
              Vui lòng cho biết lý do. Hành động này không thể khôi phục đơn hàng.
            </p>
          </div>
          <Button type="button" variant="ghost" onClick={onClose} disabled={isSubmitting}>
            Đóng
          </Button>
        </div>
        <div className="mt-6 space-y-2">
          <Label htmlFor="cancel-order-reason">Lý do hủy đơn</Label>
          <Textarea
            id="cancel-order-reason"
            value={reason}
            onChange={(event) => onReasonChange(event.currentTarget.value)}
            maxLength={500}
            placeholder="Ví dụ: Tôi không còn nhu cầu mua sách"
            disabled={isSubmitting}
          />
          <p className="text-xs text-muted-foreground">{reason.trim().length}/500 ký tự</p>
        </div>
        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            Quay lại
          </Button>
          <Button
            type="button"
            variant="destructive"
            onClick={onSubmit}
            disabled={!isReasonValid || isSubmitting}
          >
            {isSubmitting ? 'Đang hủy…' : 'Xác nhận hủy đơn'}
          </Button>
        </div>
      </div>
    </div>
  )
}

function ReturnRequestDialog({
  reason,
  requestedRefundAmount,
  isSubmitting,
  onClose,
  onReasonChange,
  onRequestedRefundAmountChange,
  onSubmit,
  t,
}: {
  reason: string
  requestedRefundAmount: string
  isSubmitting: boolean
  onClose: () => void
  onReasonChange: (value: string) => void
  onRequestedRefundAmountChange: (value: string) => void
  onSubmit: () => void
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 px-4">
      <div className="w-full max-w-2xl rounded-[28px] border border-border/70 bg-card p-6 shadow-2xl">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="font-heading text-3xl font-bold text-foreground">
              {t('returnRequests.dialogTitle')}
            </h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">
              {t('returnRequests.dialogDescription')}
            </p>
          </div>
          <Button type="button" variant="ghost" size="icon" onClick={onClose}>
            <Clock3 className="h-4 w-4" />
          </Button>
        </div>

        <div className="mt-6 space-y-5">
          <div className="space-y-2">
            <Label htmlFor="returnReason">{t('returnRequests.reasonLabel')}</Label>
            <Textarea
              id="returnReason"
              rows={6}
              value={reason}
              onChange={(event) => onReasonChange(event.currentTarget.value)}
              placeholder={t('returnRequests.reasonPlaceholder')}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="requestedRefundAmount">
              {t('returnRequests.requestedAmount')}
            </Label>
            <Input
              id="requestedRefundAmount"
              inputMode="decimal"
              value={requestedRefundAmount}
              onChange={(event) =>
                onRequestedRefundAmountChange(event.currentTarget.value)
              }
              placeholder="0"
            />
          </div>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="outline" onClick={onClose}>
              {t('common.cancel')}
            </Button>
            <Button
              type="button"
              onClick={onSubmit}
              disabled={isSubmitting || reason.trim() === ''}
            >
              {isSubmitting ? t('common.processing') : t('returnRequests.submitAction')}
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}

import { Link } from 'react-router-dom'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useOrderDetailPage } from '@/hooks/use-order-detail-page'
import type { OrderResponse, OrderStatus } from '@/types/order'
import {
  getOrderStatusLabel,
  getPaymentMethodLabel,
  getPaymentStatusLabel,
} from '@/utils/i18n'

const STATUS_VARIANTS: Record<
  OrderStatus,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  PENDING: 'secondary',
  CONFIRMED: 'default',
  SHIPPING: 'outline',
  DELIVERED: 'default',
  CANCELLED: 'destructive',
}

export default function OrderDetailPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const { order, isLoading, error } = useOrderDetailPage()

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="container mx-auto flex-1 px-4 py-12">
        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="font-heading text-3xl font-bold">
              {t('orders.detailTitle')}
            </h1>
            {order && (
              <p className="mt-2 text-muted-foreground">
                {t('orders.orderId')}: {order.orderId}
              </p>
            )}
          </div>
          <Link to="/orders">
            <Button variant="outline">{t('orders.title')}</Button>
          </Link>
        </div>

        {isLoading ? (
          <div className="rounded-2xl border border-dashed border-border px-6 py-12 text-center">
            <p className="text-muted-foreground">{t('common.loading')}</p>
          </div>
        ) : error || !order ? (
          <div className="rounded-2xl border border-dashed border-border px-6 py-12 text-center">
            <p className="font-semibold">{error || t('notFound.description')}</p>
          </div>
        ) : (
          <div className="space-y-8">
            <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
              <div className="rounded-2xl border border-border bg-card p-6">
                <div className="flex flex-wrap items-center justify-between gap-4 border-b border-border pb-4">
                  <div>
                    <p className="text-sm text-muted-foreground">
                      {t('orders.createdAt')}
                    </p>
                    <p className="font-medium">{formatDate(order.createdAt)}</p>
                  </div>
                  <Badge variant={STATUS_VARIANTS[order.status]}>
                    {getOrderStatusLabel(order.status, t)}
                  </Badge>
                </div>

                <div className="mt-6 grid gap-4 sm:grid-cols-2">
                  <OrderMeta
                    label={t('orders.receiverName')}
                    value={order.receiverName}
                  />
                  <OrderMeta
                    label={t('orders.receiverPhone')}
                    value={order.receiverPhone}
                  />
                  <OrderMeta
                    label={t('orders.paymentMethod')}
                    value={getPaymentMethodLabel(order.paymentMethod, t)}
                  />
                  <OrderMeta
                    label={t('orders.paymentStatus')}
                    value={getPaymentStatusLabel(order.paymentStatus, t)}
                  />
                </div>

                <div className="mt-4 rounded-xl bg-muted/50 p-4">
                  <p className="text-xs uppercase tracking-wide text-muted-foreground">
                    {t('orders.receiverAddress')}
                  </p>
                  <p className="mt-1 font-medium">{order.receiverAddress}</p>
                </div>
              </div>

              <div className="rounded-2xl border border-border bg-card p-6">
                <h2 className="font-heading text-xl font-bold">
                  {t('checkout.orderSummary')}
                </h2>
                <div className="mt-4 space-y-3 text-sm">
                  <SummaryRow
                    label={t('orders.subtotal')}
                    value={formatCurrency(order.totalAmount)}
                  />
                  <SummaryRow
                    label={t('orders.discount')}
                    value={formatCurrency(order.discountAmount)}
                  />
                  <SummaryRow
                    label={t('orders.shippingFee')}
                    value={formatCurrency(order.shippingFee)}
                  />
                  <div className="border-t border-border pt-3">
                    <SummaryRow
                      label={t('orders.finalAmount')}
                      value={formatCurrency(order.finalAmount)}
                      emphasized
                    />
                  </div>
                </div>
              </div>
            </div>

            <div className="rounded-2xl border border-border bg-card p-6">
              <h2 className="font-heading text-xl font-bold">
                {t('orders.itemsTitle')}
              </h2>
              <div className="mt-6 space-y-4">
                {order.items.map((item) => (
                  <div
                    key={item.id}
                    className="flex flex-col gap-3 border-b border-border pb-4 last:border-b-0 last:pb-0 sm:flex-row sm:items-center sm:justify-between"
                  >
                    <div>
                      <p className="font-medium">{item.bookTitle}</p>
                      <p className="text-sm text-muted-foreground">
                        {t('checkout.quantityShort', {
                          count: item.quantity,
                        })}{' '}
                        · {formatCurrency(item.unitPrice)}
                      </p>
                    </div>
                    <p className="font-semibold">
                      {formatCurrency(item.lineTotal)}
                    </p>
                  </div>
                ))}
              </div>
              <p className="mt-6 text-sm text-muted-foreground">
                {t('admin.orders.productCount', {
                  count: formatNumber(order.items.length),
                })}
              </p>
            </div>
          </div>
        )}
      </main>
      <Footer />
    </div>
  )
}

function OrderMeta({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl bg-muted/50 p-4">
      <p className="text-xs uppercase tracking-wide text-muted-foreground">
        {label}
      </p>
      <p className="mt-1 font-medium">{value}</p>
    </div>
  )
}

function SummaryRow({
  label,
  value,
  emphasized = false,
}: {
  label: string
  value: string
  emphasized?: boolean
}) {
  return (
    <div className="flex items-center justify-between gap-4">
      <span className="text-muted-foreground">{label}</span>
      <span className={emphasized ? 'font-heading font-bold text-primary' : 'font-medium'}>
        {value}
      </span>
    </div>
  )
}

import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { getMyOrders } from '@/services/order-service'
import type { OrderResponse, OrderStatus } from '@/types/order'
import { getErrorMessage } from '@/utils'
import { getOrderStatusLabel } from '@/utils/i18n'

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

export default function MyOrdersPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false

    async function loadOrders() {
      try {
        const data = await getMyOrders()

        if (isCancelled) {
          return
        }

        setOrders(data)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, t('checkout.error')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadOrders()

    return () => {
      isCancelled = true
    }
  }, [t])

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="container mx-auto flex-1 px-4 py-12">
        <div className="mb-8 flex items-end justify-between gap-4">
          <div>
            <h1 className="font-heading text-3xl font-bold">{t('orders.title')}</h1>
            <p className="mt-2 text-muted-foreground">
              {t('orders.totalCount', { count: formatNumber(orders.length) })}
            </p>
          </div>
          <Link to="/books">
            <Button variant="outline">{t('common.continueShopping')}</Button>
          </Link>
        </div>

        {isLoading ? (
          <div className="rounded-2xl border border-dashed border-border px-6 py-12 text-center">
            <p className="text-muted-foreground">{t('common.loading')}</p>
          </div>
        ) : error ? (
          <div className="rounded-2xl border border-dashed border-border px-6 py-12 text-center">
            <p className="font-semibold">{error}</p>
          </div>
        ) : orders.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-border px-6 py-12 text-center">
            <p className="font-heading text-lg font-semibold">
              {t('orders.emptyTitle')}
            </p>
            <p className="mt-2 text-sm text-muted-foreground">
              {t('orders.emptyDescription')}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div
                key={order.orderId}
                className="rounded-2xl border border-border bg-card p-6"
              >
                <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                  <div>
                    <p className="text-xs uppercase tracking-wide text-muted-foreground">
                      {t('orders.orderId')}
                    </p>
                    <h2 className="font-heading text-xl font-bold">
                      {order.orderId}
                    </h2>
                    <p className="mt-2 text-sm text-muted-foreground">
                      {t('orders.createdAt')}: {formatDate(order.createdAt)}
                    </p>
                  </div>
                  <div className="flex flex-wrap items-center gap-3">
                    <Badge variant={STATUS_VARIANTS[order.status]}>
                      {getOrderStatusLabel(order.status, t)}
                    </Badge>
                    <Link to={`/orders/${order.orderId}`}>
                      <Button size="sm">{t('orders.viewDetail')}</Button>
                    </Link>
                  </div>
                </div>

                <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                  <OrderMeta
                    label={t('orders.receiverName')}
                    value={order.receiverName}
                  />
                  <OrderMeta
                    label={t('orders.receiverPhone')}
                    value={order.receiverPhone}
                  />
                  <OrderMeta
                    label={t('admin.orders.columns.products')}
                    value={t('admin.orders.productCount', {
                      count: formatNumber(order.items.length),
                    })}
                  />
                  <OrderMeta
                    label={t('orders.finalAmount')}
                    value={formatCurrency(order.finalAmount)}
                  />
                </div>
              </div>
            ))}
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

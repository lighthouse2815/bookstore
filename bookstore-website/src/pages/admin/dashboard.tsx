import { useEffect, useMemo, useState } from 'react'
import {
  BookOpen,
  ShoppingCart,
  TrendingUp,
  Users,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import { getBookCatalog } from '@/services/book-service'
import { getAdminOrders } from '@/services/order-service'
import type { Book } from '@/types/book'
import type { OrderResponse, OrderStatus } from '@/types/order'
import { getErrorMessage } from '@/utils'
import { getOrderStatusLabel } from '@/utils/i18n'

const statusVariants: Record<
  OrderStatus,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  PENDING: 'secondary',
  CONFIRMED: 'default',
  SHIPPING: 'outline',
  DELIVERED: 'default',
  CANCELLED: 'destructive',
}

export default function AdminDashboard() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [books, setBooks] = useState<Book[]>([])
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false

    async function loadDashboardData() {
      try {
        const [catalog, orderResponses] = await Promise.all([
          getBookCatalog(),
          getAdminOrders(),
        ])

        if (isCancelled) {
          return
        }

        setBooks(catalog.books)
        setOrders(orderResponses)
        setError(null)
      } catch (currentError) {
        if (!isCancelled) {
          setError(getErrorMessage(currentError, t('checkout.error')))
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadDashboardData()

    return () => {
      isCancelled = true
    }
  }, [t])

  const stats = useMemo(() => {
    const now = new Date()
    const todayKey = now.toDateString()
    const currentMonth = now.getMonth()
    const currentYear = now.getFullYear()

    const ordersToday = orders.filter(
      (order) => new Date(order.createdAt).toDateString() === todayKey,
    ).length

    const activeCustomers = new Set(
      orders
        .map((order) => order.receiverPhone.trim())
        .filter((phoneNumber) => phoneNumber !== ''),
    ).size

    const revenueMonth = orders
      .filter((order) => {
        const createdAt = new Date(order.createdAt)
        return (
          createdAt.getMonth() === currentMonth &&
          createdAt.getFullYear() === currentYear &&
          order.status !== 'CANCELLED'
        )
      })
      .reduce((sum, order) => sum + order.finalAmount, 0)

    return [
      {
        label: t('admin.dashboard.stats.totalBooks'),
        value: formatNumber(books.length),
        icon: BookOpen,
        color: 'bg-blue-100 text-blue-600 dark:bg-blue-950/40 dark:text-blue-300',
      },
      {
        label: t('admin.dashboard.stats.ordersToday'),
        value: formatNumber(ordersToday),
        icon: ShoppingCart,
        color:
          'bg-green-100 text-green-600 dark:bg-green-950/40 dark:text-green-300',
      },
      {
        label: t('admin.dashboard.stats.customers'),
        value: formatNumber(activeCustomers),
        icon: Users,
        color:
          'bg-purple-100 text-purple-600 dark:bg-purple-950/40 dark:text-purple-300',
      },
      {
        label: t('admin.dashboard.stats.revenueMonth'),
        value: formatCurrency(revenueMonth),
        icon: TrendingUp,
        color:
          'bg-orange-100 text-orange-600 dark:bg-orange-950/40 dark:text-orange-300',
      },
    ]
  }, [books.length, formatCurrency, formatNumber, orders, t])

  const recentOrders = useMemo(
    () =>
      [...orders]
        .sort(
          (firstOrder, secondOrder) =>
            new Date(secondOrder.createdAt).getTime() -
            new Date(firstOrder.createdAt).getTime(),
        )
        .slice(0, 5),
    [orders],
  )

  return (
    <AdminLayout>
      <div>
        <h1 className="font-heading text-3xl font-bold text-foreground">
          {t('common.dashboard')}
        </h1>
        <p className="mt-2 text-muted-foreground">
          {t('admin.dashboard.description')}
        </p>

        <div className="mt-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
          {stats.map((stat) => {
            const Icon = stat.icon
            return (
              <div
                key={stat.label}
                className="rounded-lg border border-border bg-card p-6"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">
                      {stat.label}
                    </p>
                    <p className="mt-2 text-3xl font-bold text-foreground">
                      {isLoading ? '...' : stat.value}
                    </p>
                  </div>
                  <div className={`rounded-lg p-3 ${stat.color}`}>
                    <Icon className="h-6 w-6" />
                  </div>
                </div>
              </div>
            )
          })}
        </div>

        <div className="mt-12">
          <h2 className="font-heading text-xl font-bold text-foreground">
            {t('admin.dashboard.recentOrders')}
          </h2>

          <div className="mt-6 rounded-lg border border-border bg-card">
            {isLoading ? (
              <div className="px-6 py-8 text-center">
                <p className="text-muted-foreground">{t('common.loading')}</p>
              </div>
            ) : error ? (
              <div className="px-6 py-8 text-center">
                <p className="font-semibold text-foreground">{error}</p>
              </div>
            ) : recentOrders.length === 0 ? (
              <div className="px-6 py-8 text-center">
                <p className="text-muted-foreground">
                  {t('admin.dashboard.emptyOrders')}
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-border">
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.dashboard.columns.orderId')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.dashboard.columns.customer')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.dashboard.columns.total')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.dashboard.columns.status')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.dashboard.columns.date')}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentOrders.map((order) => (
                      <tr key={order.orderId} className="border-b border-border">
                        <td className="px-6 py-4 text-sm font-medium text-foreground">
                          {order.orderId}
                        </td>
                        <td className="px-6 py-4 text-sm text-foreground">
                          {order.receiverName}
                        </td>
                        <td className="px-6 py-4 text-sm font-medium text-foreground">
                          {formatCurrency(order.finalAmount)}
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <Badge variant={statusVariants[order.status]}>
                            {getOrderStatusLabel(order.status, t)}
                          </Badge>
                        </td>
                        <td className="px-6 py-4 text-sm text-muted-foreground">
                          {formatDate(order.createdAt)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}

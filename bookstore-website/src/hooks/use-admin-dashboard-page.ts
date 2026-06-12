import { useEffect, useMemo, useState } from 'react'
import { BookOpen, ShoppingCart, TrendingUp, Users } from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import { getBookCatalog } from '@/services/book-service'
import { getAdminOrders } from '@/services/order-service'
import type { Book } from '@/types/book'
import type { OrderResponse } from '@/types/order'
import { getErrorMessage } from '@/utils'

export function useAdminDashboardPage() {
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

  return {
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    isLoading,
    error,
    stats,
    recentOrders,
  }
}

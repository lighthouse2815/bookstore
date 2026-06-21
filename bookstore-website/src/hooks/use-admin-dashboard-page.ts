import { useEffect, useMemo, useState } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  getDashboardSummary,
  getLowStockBooks,
  getOrderStatusStats,
  getRecentOrders,
  getRevenueChart,
  getTopBooks,
} from '@/services/dashboard-service'
import type {
  AdminDashboardRevenueFilter,
  DashboardSummary,
  LowStockBook,
  OrderStatusStats,
  RecentOrder,
  RevenueChartPoint,
  TopBookStats,
} from '@/types/admin-dashboard'
import { getErrorMessage } from '@/utils'

export function useAdminDashboardPage() {
  const { language, locale, t, formatCurrency, formatDate, formatNumber } =
    useLanguage()
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [revenueChart, setRevenueChart] = useState<RevenueChartPoint[]>([])
  const [topBooks, setTopBooks] = useState<TopBookStats[]>([])
  const [orderStatusStats, setOrderStatusStats] = useState<OrderStatusStats[]>([])
  const [lowStockBooks, setLowStockBooks] = useState<LowStockBook[]>([])
  const [recentOrders, setRecentOrders] = useState<RecentOrder[]>([])
  const [revenueFilter, setRevenueFilter] =
    useState<AdminDashboardRevenueFilter>('LAST_7_DAYS')
  const [isLoading, setIsLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false

    async function loadDashboardData() {
      const initialLoad = summary === null

      if (initialLoad) {
        setIsLoading(true)
      } else {
        setIsRefreshing(true)
      }

      try {
        const dashboardData = await requestDashboardData(revenueFilter)

        if (isCancelled) {
          return
        }

        applyDashboardData(dashboardData, {
          setSummary,
          setRevenueChart,
          setTopBooks,
          setOrderStatusStats,
          setLowStockBooks,
          setRecentOrders,
        })
        setError(null)
      } catch (currentError) {
        if (!isCancelled) {
          const message = getErrorMessage(currentError, t('checkout.error'))
          setError(message)
          toast.error(message)
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
          setIsRefreshing(false)
        }
      }
    }

    void loadDashboardData()

    return () => {
      isCancelled = true
    }
  }, [revenueFilter, t])

  const hasData = useMemo(
    () =>
      summary !== null ||
      revenueChart.length > 0 ||
      topBooks.length > 0 ||
      orderStatusStats.length > 0 ||
      lowStockBooks.length > 0 ||
      recentOrders.length > 0,
    [
      lowStockBooks.length,
      orderStatusStats.length,
      recentOrders.length,
      revenueChart.length,
      summary,
      topBooks.length,
    ],
  )

  async function refresh() {
    setIsRefreshing(true)

    try {
      const dashboardData = await requestDashboardData(revenueFilter)
      applyDashboardData(dashboardData, {
        setSummary,
        setRevenueChart,
        setTopBooks,
        setOrderStatusStats,
        setLowStockBooks,
        setRecentOrders,
      })
      setError(null)
    } catch (currentError) {
      const message = getErrorMessage(currentError, t('checkout.error'))
      setError(message)
      toast.error(message)
    } finally {
      setIsRefreshing(false)
    }
  }

  return {
    language,
    locale,
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    summary,
    revenueChart,
    topBooks,
    orderStatusStats,
    lowStockBooks,
    recentOrders,
    revenueFilter,
    setRevenueFilter,
    isLoading,
    isRefreshing,
    error,
    hasData,
    refresh,
  }
}

async function requestDashboardData(filter: AdminDashboardRevenueFilter) {
  const revenueParams = getRevenueFilterParams(filter)

  const [
    summary,
    revenueChart,
    topBooks,
    orderStatusStats,
    lowStockBooks,
    recentOrders,
  ] = await Promise.all([
    getDashboardSummary(),
    getRevenueChart(revenueParams),
    getTopBooks(10),
    getOrderStatusStats(),
    getLowStockBooks(10),
    getRecentOrders(6),
  ])

  return {
    summary,
    revenueChart,
    topBooks,
    orderStatusStats,
    lowStockBooks,
    recentOrders,
  }
}

function applyDashboardData(
  data: Awaited<ReturnType<typeof requestDashboardData>>,
  setters: {
    setSummary: (value: DashboardSummary | null) => void
    setRevenueChart: (value: RevenueChartPoint[]) => void
    setTopBooks: (value: TopBookStats[]) => void
    setOrderStatusStats: (value: OrderStatusStats[]) => void
    setLowStockBooks: (value: LowStockBook[]) => void
    setRecentOrders: (value: RecentOrder[]) => void
  },
) {
  setters.setSummary(data.summary)
  setters.setRevenueChart(data.revenueChart)
  setters.setTopBooks(data.topBooks)
  setters.setOrderStatusStats(data.orderStatusStats)
  setters.setLowStockBooks(data.lowStockBooks)
  setters.setRecentOrders(data.recentOrders)
}

function getRevenueFilterParams(filter: AdminDashboardRevenueFilter) {
  const today = new Date()

  switch (filter) {
    case 'LAST_7_DAYS':
      return {
        from: formatDateParam(addDays(today, -6)),
        to: formatDateParam(today),
        groupBy: 'DAY' as const,
      }
    case 'LAST_30_DAYS':
      return {
        from: formatDateParam(addDays(today, -29)),
        to: formatDateParam(today),
        groupBy: 'DAY' as const,
      }
    case 'THIS_MONTH':
      return {
        from: formatDateParam(new Date(today.getFullYear(), today.getMonth(), 1)),
        to: formatDateParam(today),
        groupBy: 'DAY' as const,
      }
  }
}

function addDays(date: Date, dayOffset: number) {
  const nextDate = new Date(date)
  nextDate.setDate(nextDate.getDate() + dayOffset)
  return nextDate
}

function formatDateParam(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

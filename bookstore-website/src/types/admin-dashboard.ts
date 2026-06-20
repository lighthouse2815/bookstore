import type { OrderStatus } from '@/types/order'

export type DashboardSummary = {
  todayRevenue: number
  monthRevenue: number
  todayOrders: number
  pendingOrders: number
  lowStockBooks: number
  newCustomers: number
  newReviews: number
  activeCoupons: number
}

export type RevenueChartPoint = {
  label: string
  revenue: number
  orders: number
}

export type TopBookStats = {
  bookId: string
  title: string
  soldQuantity: number
  revenue: number
}

export type OrderStatusStats = {
  status: OrderStatus | null
  count: number
}

export type LowStockBook = {
  bookId: string
  title: string
  stockQuantity: number
}

export type RecentOrder = {
  orderId: string
  orderCode: string
  customerName: string
  finalAmount: number
  status: OrderStatus | null
  createdAt: string
}

export type RevenueChartGroupBy = 'DAY' | 'WEEK' | 'MONTH'

export type RevenueChartQuery = {
  from?: string
  to?: string
  groupBy?: RevenueChartGroupBy
}

export type AdminDashboardRevenueFilter =
  | 'LAST_7_DAYS'
  | 'LAST_30_DAYS'
  | 'THIS_MONTH'

import api from './api'
import type {
  DashboardSummary,
  LowStockBook,
  OrderStatusStats,
  RecentOrder,
  RevenueChartPoint,
  RevenueChartQuery,
  TopBookStats,
} from '@/types/admin-dashboard'
import type { ApiResponse } from '@/types/api'
import { unwrapResponse } from '@/utils'

export async function getDashboardSummary(): Promise<DashboardSummary> {
  const response = await api.get<ApiResponse<DashboardSummary>>(
    '/admin/dashboard/summary',
  )
  return unwrapResponse(response)
}

export async function getRevenueChart(
  params: RevenueChartQuery,
): Promise<RevenueChartPoint[]> {
  const response = await api.get<ApiResponse<RevenueChartPoint[]>>(
    '/admin/dashboard/revenue',
    {
      params,
    },
  )
  return unwrapResponse(response)
}

export async function getTopBooks(limit = 10): Promise<TopBookStats[]> {
  const response = await api.get<ApiResponse<TopBookStats[]>>(
    '/admin/dashboard/top-books',
    {
      params: { limit },
    },
  )
  return unwrapResponse(response)
}

export async function getOrderStatusStats(): Promise<OrderStatusStats[]> {
  const response = await api.get<ApiResponse<OrderStatusStats[]>>(
    '/admin/dashboard/orders/status',
  )
  return unwrapResponse(response)
}

export async function getLowStockBooks(
  threshold = 10,
): Promise<LowStockBook[]> {
  const response = await api.get<ApiResponse<LowStockBook[]>>(
    '/admin/dashboard/low-stock',
    {
      params: { threshold },
    },
  )
  return unwrapResponse(response)
}

export async function getRecentOrders(limit = 6): Promise<RecentOrder[]> {
  const response = await api.get<ApiResponse<RecentOrder[]>>(
    '/admin/dashboard/recent-orders',
    {
      params: { limit },
    },
  )
  return unwrapResponse(response)
}

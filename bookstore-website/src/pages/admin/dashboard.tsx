import {
  BookOpen,
  ShoppingCart,
  TrendingUp,
  Users,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { useAdminDashboardPage } from '@/hooks/use-admin-dashboard-page'
import { AdminLayout } from '@/components/layout/admin-layout'
import type { OrderStatus } from '@/types/order'
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
  const { t, formatCurrency, formatDate, isLoading, error, stats, recentOrders } =
    useAdminDashboardPage()

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

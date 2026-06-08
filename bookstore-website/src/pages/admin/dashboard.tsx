import {
  BookOpen,
  ShoppingCart,
  TrendingUp,
  Users,
} from 'lucide-react'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import { getOrderStatusLabel } from '@/utils/i18n'

const RECENT_ORDERS = [
  {
    id: 'ORD-001',
    customer: 'Nguyễn Văn A',
    total: 450000,
    status: 'delivered',
    date: '2024-06-05',
  },
  {
    id: 'ORD-002',
    customer: 'Trần Thị B',
    total: 280000,
    status: 'shipped',
    date: '2024-06-04',
  },
  {
    id: 'ORD-003',
    customer: 'Phạm Văn C',
    total: 165000,
    status: 'processing',
    date: '2024-06-03',
  },
]

const STATUS_COLORS: Record<string, string> = {
  pending: 'bg-yellow-100 text-yellow-800',
  processing: 'bg-blue-100 text-blue-800',
  shipped: 'bg-purple-100 text-purple-800',
  delivered: 'bg-green-100 text-green-800',
}

export default function AdminDashboard() {
  const { t, formatCurrency, formatDate } = useLanguage()

  const stats = [
    {
      label: t('admin.dashboard.stats.totalBooks'),
      value: '128',
      icon: BookOpen,
      color: 'bg-blue-100 text-blue-600',
    },
    {
      label: t('admin.dashboard.stats.ordersToday'),
      value: '12',
      icon: ShoppingCart,
      color: 'bg-green-100 text-green-600',
    },
    {
      label: t('admin.dashboard.stats.customers'),
      value: '256',
      icon: Users,
      color: 'bg-purple-100 text-purple-600',
    },
    {
      label: t('admin.dashboard.stats.revenueMonth'),
      value: '28.5M',
      icon: TrendingUp,
      color: 'bg-orange-100 text-orange-600',
    },
  ]

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
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">
                      {stat.label}
                    </p>
                    <p className="mt-2 text-3xl font-bold text-foreground">
                      {stat.value}
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
                  {RECENT_ORDERS.map((order) => (
                    <tr key={order.id} className="border-b border-border">
                      <td className="px-6 py-4 text-sm font-medium text-foreground">
                        {order.id}
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {order.customer}
                      </td>
                      <td className="px-6 py-4 text-sm font-medium text-foreground">
                        {formatCurrency(order.total)}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <span
                          className={`rounded-full px-3 py-1 text-xs font-semibold ${STATUS_COLORS[order.status]}`}
                        >
                          {getOrderStatusLabel(order.status, t)}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-muted-foreground">
                        {formatDate(order.date)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}

import { useState } from 'react'
import { Eye, Trash2 } from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import { getOrderStatusLabel } from '@/utils/i18n'

const MOCK_ORDERS = [
  {
    id: 'ORD-001',
    customer: 'Nguyễn Văn A',
    email: 'nguyen@example.com',
    items: 3,
    total: 450000,
    status: 'delivered',
    date: '2024-06-05',
  },
  {
    id: 'ORD-002',
    customer: 'Trần Thị B',
    email: 'tran@example.com',
    items: 2,
    total: 280000,
    status: 'shipped',
    date: '2024-06-04',
  },
  {
    id: 'ORD-003',
    customer: 'Phạm Văn C',
    email: 'pham@example.com',
    items: 1,
    total: 165000,
    status: 'processing',
    date: '2024-06-03',
  },
  {
    id: 'ORD-004',
    customer: 'Lê Văn D',
    email: 'le@example.com',
    items: 4,
    total: 580000,
    status: 'pending',
    date: '2024-06-02',
  },
  {
    id: 'ORD-005',
    customer: 'Hoàng Thị E',
    email: 'hoang@example.com',
    items: 2,
    total: 320000,
    status: 'delivered',
    date: '2024-06-01',
  },
]

const STATUS_VARIANTS: Record<string, 'default' | 'secondary' | 'outline' | 'destructive'> = {
  pending: 'secondary',
  processing: 'default',
  shipped: 'outline',
  delivered: 'default',
  cancelled: 'destructive',
}

export default function AdminOrdersPage() {
  const [orders] = useState(MOCK_ORDERS)
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()

  return (
    <AdminLayout>
      <div>
        <div>
          <h1 className="font-heading text-3xl font-bold text-foreground">
            {t('admin.orders.title')}
          </h1>
          <p className="mt-2 text-muted-foreground">
            {t('admin.orders.totalOrders', {
              count: formatNumber(orders.length),
            })}
          </p>
        </div>

        <div className="mt-8 rounded-lg border border-border bg-card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                    {t('admin.orders.columns.orderId')}
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                    {t('admin.orders.columns.customer')}
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                    {t('admin.orders.columns.email')}
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                    {t('admin.orders.columns.products')}
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                    {t('admin.orders.columns.total')}
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                    {t('admin.orders.columns.status')}
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                    {t('admin.orders.columns.date')}
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                    {t('admin.orders.columns.actions')}
                  </th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id} className="border-b border-border">
                    <td className="px-6 py-4 text-sm font-medium text-foreground">
                      {order.id}
                    </td>
                    <td className="px-6 py-4 text-sm text-foreground">
                      {order.customer}
                    </td>
                    <td className="px-6 py-4 text-sm text-muted-foreground">
                      {order.email}
                    </td>
                    <td className="px-6 py-4 text-sm text-foreground">
                      {t('admin.orders.productCount', {
                        count: formatNumber(order.items),
                      })}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium text-foreground">
                      {formatCurrency(order.total)}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      <Badge
                        variant={
                          STATUS_VARIANTS[order.status] ?? STATUS_VARIANTS.pending
                        }
                      >
                        {getOrderStatusLabel(order.status, t)}
                      </Badge>
                    </td>
                    <td className="px-6 py-4 text-sm text-muted-foreground">
                      {formatDate(order.date)}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      <div className="flex gap-2">
                        <Button variant="ghost" size="sm">
                          <Eye className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="sm">
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}

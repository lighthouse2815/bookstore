import { Eye, Search } from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import {
  adminOrderStatusOptions,
  useAdminOrdersPage,
} from '@/hooks/use-admin-orders-page'
import type { OrderStatus } from '@/types/order'
import {
  getOrderStatusLabel,
  getPaymentMethodLabel,
  getPaymentStatusLabel,
} from '@/utils/i18n'

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

export default function AdminOrdersPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const {
    orders,
    filteredOrders,
    searchTerm,
    statusFilter,
    selectedOrderId,
    selectedOrder,
    selectedStatus,
    isLoading,
    isDetailLoading,
    isUpdating,
    error,
    handleSearchTermChange,
    handleStatusFilterChange,
    handleSelectedStatusChange,
    handleCloseDetail,
    handleViewOrder,
    handleUpdateStatus,
  } = useAdminOrdersPage()

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

        <div className="mt-8 grid gap-4 lg:grid-cols-[minmax(0,1fr)_220px]">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder={t('admin.orders.searchPlaceholder')}
              value={searchTerm}
              onChange={handleSearchTermChange}
              className="pl-10"
            />
          </div>

          <div>
            <Label htmlFor="orderStatusFilter">{t('admin.orders.filterLabel')}</Label>
            <select
              id="orderStatusFilter"
              value={statusFilter}
              onChange={handleStatusFilterChange}
              className="mt-2 h-10 w-full rounded-md border border-border bg-background px-3 text-sm"
            >
              <option value="ALL">{t('admin.orders.allStatuses')}</option>
              {adminOrderStatusOptions.map((status) => (
                <option key={status} value={status}>
                  {getOrderStatusLabel(status, t)}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="mt-8 rounded-lg border border-border bg-card">
          {isLoading ? (
            <div className="px-6 py-8 text-center">
              <p className="text-muted-foreground">{t('common.loading')}</p>
            </div>
          ) : error ? (
            <div className="px-6 py-8 text-center">
              <p className="font-semibold text-foreground">{error}</p>
            </div>
          ) : (
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
                      {t('admin.orders.columns.phone')}
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
                  {filteredOrders.map((order) => (
                    <tr key={order.orderId} className="border-b border-border">
                      <td className="px-6 py-4 text-sm font-medium text-foreground">
                        {order.orderId}
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {order.receiverName}
                      </td>
                      <td className="px-6 py-4 text-sm text-muted-foreground">
                        {order.receiverPhone}
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {t('admin.orders.productCount', {
                          count: formatNumber(order.items.length),
                        })}
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
                      <td className="px-6 py-4 text-sm">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => void handleViewOrder(order.orderId)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {!isLoading && !error && filteredOrders.length === 0 && (
            <div className="px-6 py-8 text-center">
              <p className="text-muted-foreground">{t('orders.emptyDescription')}</p>
            </div>
          )}
        </div>

        {selectedOrderId && (
          <div className="mt-8 rounded-2xl border border-border bg-card p-6">
            {isDetailLoading || !selectedOrder ? (
              <p className="text-muted-foreground">{t('common.loading')}</p>
            ) : (
              <div className="space-y-6">
                <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div>
                    <h2 className="font-heading text-2xl font-bold text-foreground">
                      {t('admin.orders.detailTitle')}
                    </h2>
                    <p className="mt-2 text-sm text-muted-foreground">
                      {selectedOrder.orderId}
                    </p>
                  </div>

                  <div className="flex flex-wrap gap-3">
                    <Badge variant={statusVariants[selectedOrder.status]}>
                      {getOrderStatusLabel(selectedOrder.status, t)}
                    </Badge>
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handleCloseDetail}
                    >
                      {t('common.close')}
                    </Button>
                  </div>
                </div>

                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                  <DetailCard
                    label={t('orders.receiverName')}
                    value={selectedOrder.receiverName}
                  />
                  <DetailCard
                    label={t('orders.receiverPhone')}
                    value={selectedOrder.receiverPhone}
                  />
                  <DetailCard
                    label={t('admin.orders.detail.receiverAddress')}
                    value={selectedOrder.receiverAddress}
                  />
                  <DetailCard
                    label={t('orders.createdAt')}
                    value={formatDate(selectedOrder.createdAt)}
                  />
                </div>

                <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
                  <div className="rounded-2xl border border-border p-5">
                    <h3 className="font-semibold text-foreground">
                      {t('orders.itemsTitle')}
                    </h3>
                    <div className="mt-4 space-y-3">
                      {selectedOrder.items.map((item) => (
                        <div
                          key={item.id}
                          className="flex items-center justify-between rounded-xl bg-muted/40 px-4 py-3"
                        >
                          <div>
                            <p className="font-medium text-foreground">
                              {item.bookTitle}
                            </p>
                            <p className="text-sm text-muted-foreground">
                              {t('checkout.quantityShort', { count: item.quantity })}
                            </p>
                          </div>
                          <p className="font-semibold text-foreground">
                            {formatCurrency(item.lineTotal)}
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="rounded-2xl border border-border p-5">
                    <h3 className="font-semibold text-foreground">
                      {t('admin.orders.detail.updateStatus')}
                    </h3>

                    <div className="mt-4 space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="adminOrderStatus">
                          {t('orders.status')}
                        </Label>
                        <select
                          id="adminOrderStatus"
                          value={selectedStatus}
                          onChange={handleSelectedStatusChange}
                          className="h-10 w-full rounded-md border border-border bg-background px-3 text-sm"
                        >
                          {adminOrderStatusOptions.map((status) => (
                            <option key={status} value={status}>
                              {getOrderStatusLabel(status, t)}
                            </option>
                          ))}
                        </select>
                      </div>

                      <DetailCard
                        label={t('admin.orders.detail.paymentMethod')}
                        value={getPaymentMethodLabel(
                          selectedOrder.paymentMethod,
                          t,
                        )}
                      />
                      <DetailCard
                        label={t('admin.orders.detail.paymentStatus')}
                        value={getPaymentStatusLabel(
                          selectedOrder.paymentStatus,
                          t,
                        )}
                      />
                      <DetailCard
                        label={t('orders.finalAmount')}
                        value={formatCurrency(selectedOrder.finalAmount)}
                      />

                      <Button
                        type="button"
                        className="w-full"
                        onClick={() => void handleUpdateStatus()}
                        disabled={isUpdating}
                      >
                        {isUpdating ? t('common.processing') : t('common.save')}
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </AdminLayout>
  )
}

function DetailCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl bg-muted/40 p-4">
      <p className="text-xs uppercase tracking-wide text-muted-foreground">
        {label}
      </p>
      <p className="mt-1 font-medium text-foreground">{value}</p>
    </div>
  )
}

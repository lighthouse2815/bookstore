import { Eye, Search, Truck } from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { PaginationControls } from '@/components/common/pagination-controls'
import {
  PageHeader,
  StatePanel,
  SummaryField,
  SurfaceCard,
  formControlClassName,
  primaryButtonClassName,
  secondaryButtonClassName,
} from '@/components/common/page-shell'
import { AdminLayout } from '@/components/layout/admin-layout'
import { OrderTimelineList } from '@/components/order/order-timeline-list'
import { useLanguage } from '@/contexts/language-context'
import {
  adminOrderStatusOptions,
  useAdminOrdersPage,
} from '@/hooks/use-admin-orders-page'
import type { OrderStatus } from '@/types/order'
import type { ShipmentStatus } from '@/types/shipment'
import {
  getOrderStatusLabel,
  getPaymentMethodLabel,
  getPaymentStatusLabel,
  getShipmentStatusLabel,
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

const shipmentStatusVariants: Record<
  ShipmentStatus,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  ASSIGNED: 'secondary',
  PICKED_UP: 'outline',
  DELIVERING: 'default',
  DELIVERED: 'default',
  FAILED: 'destructive',
}

export default function AdminOrdersPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const {
    orders,
    page,
    pageSize,
    totalCount,
    filteredOrders,
    shippers,
    searchTerm,
    statusFilter,
    selectedOrderId,
    selectedOrder,
    selectedTimeline,
    selectedStatus,
    selectedShipperId,
    selectedOrderActiveShipment,
    selectedOrderLatestShipment,
    selectedOrderShipmentShipper,
    canAssignShipment,
    isLoading,
    isDetailLoading,
    isUpdating,
    isAssigningShipment,
    error,
    handleSearchTermChange,
    handleStatusFilterChange,
    handlePageChange,
    handleSelectedStatusChange,
    handleSelectedShipperChange,
    handleCloseDetail,
    handleViewOrder,
    handleUpdateStatus,
    handleAssignShipment,
  } = useAdminOrdersPage()

  return (
    <AdminLayout>
      <div>
        <PageHeader
          title={t('admin.orders.title')}
          description={t('admin.orders.totalOrders', {
            count: formatNumber(totalCount),
          })}
        />

        <div className="mt-8 grid gap-4 lg:grid-cols-[minmax(0,1fr)_220px]">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder={t('admin.orders.searchPlaceholder')}
              value={searchTerm}
              onChange={handleSearchTermChange}
              className={`${formControlClassName} pl-10`}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="orderStatusFilter">{t('admin.orders.filterLabel')}</Label>
            <select
              id="orderStatusFilter"
              value={statusFilter}
              onChange={handleStatusFilterChange}
              className={`w-full ${formControlClassName}`}
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

        <SurfaceCard className="mt-8 overflow-hidden p-0">
          {isLoading ? (
            <div className="p-6">
              <StatePanel title={t('common.loading')} />
            </div>
          ) : error ? (
            <div className="p-6">
              <StatePanel tone="error" title={error} />
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
            <div className="p-6">
              <StatePanel title={t('orders.emptyDescription')} minHeightClassName="min-h-[160px]" />
            </div>
          )}

          {!isLoading && !error && totalCount > 0 ? (
            <PaginationControls
              page={page}
              size={pageSize}
              totalCount={totalCount}
              onPageChange={handlePageChange}
            />
          ) : null}
        </SurfaceCard>

        {selectedOrderId && (
          <SurfaceCard className="mt-8 p-6">
            {isDetailLoading || !selectedOrder ? (
              <StatePanel title={t('common.loading')} minHeightClassName="min-h-[180px]" />
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
                      className={secondaryButtonClassName}
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

                  <div className="space-y-5">
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
                          className={`${primaryButtonClassName} w-full`}
                          onClick={() => void handleUpdateStatus()}
                          disabled={isUpdating}
                        >
                          {isUpdating ? t('common.processing') : t('common.save')}
                        </Button>
                      </div>
                    </div>

                    <div className="rounded-2xl border border-border p-5">
                      <div className="flex items-start gap-3">
                        <div className="rounded-full bg-primary/10 p-2 text-primary">
                          <Truck className="h-5 w-5" />
                        </div>
                        <div>
                          <h3 className="font-semibold text-foreground">
                            {t('admin.orders.shipmentAssignment.title')}
                          </h3>
                          <p className="mt-1 text-sm text-muted-foreground">
                            {t('admin.orders.shipmentAssignment.description')}
                          </p>
                        </div>
                      </div>

                      <div className="mt-4 space-y-4">
                        {selectedOrderActiveShipment ? (
                          <>
                            <div className="rounded-xl border border-border/70 bg-muted/30 p-4">
                              <div className="flex items-center justify-between gap-3">
                                <div>
                                  <p className="text-xs uppercase tracking-wide text-muted-foreground">
                                    {t('admin.orders.shipmentAssignment.currentShipment')}
                                  </p>
                                  <p className="mt-1 font-medium text-foreground">
                                    {selectedOrderActiveShipment.shipmentId}
                                  </p>
                                </div>
                                <Badge
                                  variant={
                                    shipmentStatusVariants[
                                      selectedOrderActiveShipment.shipmentStatus
                                    ]
                                  }
                                >
                                  {getShipmentStatusLabel(
                                    selectedOrderActiveShipment.shipmentStatus,
                                    t,
                                  )}
                                </Badge>
                              </div>
                              <div className="mt-3 grid gap-3 md:grid-cols-2">
                                <DetailCard
                                  label={t('admin.orders.shipmentAssignment.shipper')}
                                  value={
                                    selectedOrderShipmentShipper?.username ??
                                    selectedOrderActiveShipment.shipperId
                                  }
                                />
                                <DetailCard
                                  label={t('admin.orders.shipmentAssignment.assignedAt')}
                                  value={formatDate(
                                    selectedOrderActiveShipment.assignedAt,
                                  )}
                                />
                              </div>
                            </div>
                            <p className="text-sm text-muted-foreground">
                              {t('admin.orders.shipmentAssignment.activeNotice')}
                            </p>
                          </>
                        ) : canAssignShipment ? (
                          <>
                            {selectedOrderLatestShipment?.shipmentStatus ===
                            'FAILED' ? (
                              <div className="rounded-xl border border-destructive/20 bg-destructive/5 p-4">
                                <p className="text-xs uppercase tracking-wide text-destructive">
                                  {t('admin.orders.shipmentAssignment.latestFailed')}
                                </p>
                                <p className="mt-2 font-medium text-foreground">
                                  {selectedOrderLatestShipment.shipmentId}
                                </p>
                                {selectedOrderLatestShipment.failureReason ? (
                                  <p className="mt-2 text-sm text-foreground">
                                    {selectedOrderLatestShipment.failureReason}
                                  </p>
                                ) : null}
                              </div>
                            ) : null}

                            <div className="space-y-2">
                              <Label htmlFor="adminOrderShipper">
                                {t('admin.orders.shipmentAssignment.chooseShipper')}
                              </Label>
                              <select
                                id="adminOrderShipper"
                                value={selectedShipperId}
                                onChange={handleSelectedShipperChange}
                                className="h-10 w-full rounded-md border border-border bg-background px-3 text-sm"
                                disabled={shippers.length === 0}
                              >
                                {shippers.length === 0 ? (
                                  <option value="">
                                    {t('admin.orders.shipmentAssignment.noShippers')}
                                  </option>
                                ) : null}
                                {shippers.map((shipper) => (
                                  <option key={shipper.userId} value={shipper.userId}>
                                    {`${shipper.username} - ${shipper.email}`}
                                  </option>
                                ))}
                              </select>
                            </div>

                            <Button
                              type="button"
                              className="w-full"
                              onClick={() => void handleAssignShipment()}
                              disabled={
                                isAssigningShipment ||
                                shippers.length === 0 ||
                                selectedShipperId === ''
                              }
                            >
                              {isAssigningShipment
                                ? t('admin.orders.shipmentAssignment.assigning')
                                : t('admin.orders.shipmentAssignment.assign')}
                            </Button>
                          </>
                        ) : (
                          <p className="text-sm text-muted-foreground">
                            {t('admin.orders.shipmentAssignment.unavailable')}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="rounded-2xl border border-border p-5">
                  <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <h3 className="font-semibold text-foreground">
                        {t('orderTimeline.title')}
                      </h3>
                      <p className="mt-1 text-sm text-muted-foreground">
                        {t('orderTimeline.description')}
                      </p>
                    </div>
                  </div>

                  <div className="mt-5">
                    <OrderTimelineList
                      emptyLabel={t('orderTimeline.empty')}
                      events={selectedTimeline}
                      showActor
                    />
                  </div>
                </div>
              </div>
            )}
          </SurfaceCard>
        )}
      </div>
    </AdminLayout>
  )
}

function DetailCard({ label, value }: { label: string; value: string }) {
  return <SummaryField label={label} value={value} />
}

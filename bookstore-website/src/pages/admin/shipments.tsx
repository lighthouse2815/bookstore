import { Eye } from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Label } from '@/components/common/label'
import { PaginationControls } from '@/components/common/pagination-controls'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import {
  adminShipmentStatusOptions,
  useAdminShipmentsPage,
} from '@/hooks/use-admin-shipments-page'
import type { ShipmentStatus } from '@/types/shipment'
import {
  getOrderStatusLabel,
  getPaymentMethodLabel,
  getPaymentStatusLabel,
  getShipmentStatusLabel,
} from '@/utils/i18n'

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

export default function AdminShipmentsPage() {
  const { locale, t, formatCurrency, formatNumber } = useLanguage()
  const {
    shipments,
    page,
    pageSize,
    totalCount,
    filteredShipments,
    shippers,
    assignableOrders,
    statusFilter,
    selectedShipmentId,
    selectedShipment,
    selectedShipmentShipper,
    selectedAssignableOrderId,
    selectedShipperId,
    isLoading,
    isDetailLoading,
    isAssigning,
    isConfirming,
    error,
    handleStatusFilterChange,
    handlePageChange,
    handleSelectedOrderChange,
    handleSelectedShipperChange,
    handleCloseDetail,
    handleViewShipment,
    handleAssignShipment,
    handleConfirmDelivered,
  } = useAdminShipmentsPage()

  return (
    <AdminLayout>
      <div>
        <div>
          <h1 className="font-heading text-3xl font-bold text-foreground">
            {t('admin.shipmentsPage.title')}
          </h1>
          <p className="mt-2 text-muted-foreground">
            {t('admin.shipmentsPage.totalShipments', {
              count: formatNumber(totalCount),
            })}
          </p>
        </div>

        <div className="mt-8 grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_300px]">
          <div className="rounded-2xl border border-border bg-card p-6">
            <div className="flex flex-col gap-2 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <h2 className="font-heading text-xl font-semibold text-foreground">
                  {t('admin.shipmentsPage.assignTitle')}
                </h2>
                <p className="mt-1 text-sm text-muted-foreground">
                  {t('admin.shipmentsPage.assignDescription')}
                </p>
              </div>
              <Badge variant="outline">
                {t('admin.shipmentsPage.ordersReady', {
                  count: formatNumber(assignableOrders.length),
                })}
              </Badge>
            </div>

            <div className="mt-6 grid gap-4 lg:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="shipmentOrderSelect">
                  {t('admin.shipmentsPage.orderLabel')}
                </Label>
                <select
                  id="shipmentOrderSelect"
                  value={selectedAssignableOrderId}
                  onChange={handleSelectedOrderChange}
                  className="h-10 w-full rounded-md border border-border bg-background px-3 text-sm"
                  disabled={assignableOrders.length === 0}
                >
                  {assignableOrders.length === 0 ? (
                    <option value="">
                      {t('admin.shipmentsPage.noEligibleOrders')}
                    </option>
                  ) : null}
                  {assignableOrders.map((order) => (
                    <option key={order.orderId} value={order.orderId}>
                      {`${order.orderId} - ${order.receiverName} - ${formatCurrency(order.finalAmount)}`}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="shipmentShipperSelect">
                  {t('admin.shipmentsPage.shipperLabel')}
                </Label>
                <select
                  id="shipmentShipperSelect"
                  value={selectedShipperId}
                  onChange={handleSelectedShipperChange}
                  className="h-10 w-full rounded-md border border-border bg-background px-3 text-sm"
                  disabled={shippers.length === 0}
                >
                  {shippers.length === 0 ? (
                    <option value="">{t('admin.shipmentsPage.noShippers')}</option>
                  ) : null}
                  {shippers.map((shipper) => (
                    <option key={shipper.userId} value={shipper.userId}>
                      {`${shipper.username} - ${shipper.email}`}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="mt-4 flex justify-end">
              <Button
                type="button"
                onClick={() => void handleAssignShipment()}
                disabled={
                  isAssigning ||
                  assignableOrders.length === 0 ||
                  shippers.length === 0 ||
                  selectedAssignableOrderId === '' ||
                  selectedShipperId === ''
                }
              >
                {isAssigning
                  ? t('admin.shipmentsPage.assigning')
                  : t('admin.shipmentsPage.assign')}
              </Button>
            </div>
          </div>

          <div className="rounded-2xl border border-border bg-card p-6">
            <Label htmlFor="shipmentStatusFilter">
              {t('admin.shipmentsPage.filterLabel')}
            </Label>
            <select
              id="shipmentStatusFilter"
              value={statusFilter}
              onChange={handleStatusFilterChange}
              className="mt-2 h-10 w-full rounded-md border border-border bg-background px-3 text-sm"
            >
              <option value="ALL">{t('admin.shipmentsPage.allStatuses')}</option>
              {adminShipmentStatusOptions.map((status) => (
                <option key={status} value={status}>
                  {getShipmentStatusLabel(status, t)}
                </option>
              ))}
            </select>

            <div className="mt-6 grid gap-3">
              <MetricCard
                label={t('admin.shipmentsPage.metrics.delivering')}
                value={formatNumber(
                  shipments.filter(
                    (shipment) => shipment.shipmentStatus === 'DELIVERING',
                  ).length,
                )}
              />
              <MetricCard
                label={t('admin.shipmentsPage.metrics.delivered')}
                value={formatNumber(
                  shipments.filter(
                    (shipment) => shipment.shipmentStatus === 'DELIVERED',
                  ).length,
                )}
              />
              <MetricCard
                label={t('admin.shipmentsPage.metrics.failed')}
                value={formatNumber(
                  shipments.filter(
                    (shipment) => shipment.shipmentStatus === 'FAILED',
                  ).length,
                )}
              />
            </div>
          </div>
        </div>

        <div className="mt-8 rounded-lg border border-border bg-card">
          {isLoading ? (
            <div className="px-6 py-8 text-center">
              <p className="text-muted-foreground">{t('admin.shipmentsPage.loading')}</p>
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
                      {t('admin.shipmentsPage.columns.shipmentId')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.shipmentsPage.columns.order')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.shipmentsPage.columns.receiver')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.shipmentsPage.columns.shipper')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.shipmentsPage.columns.status')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.shipmentsPage.columns.amount')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.shipmentsPage.columns.assignedAt')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.shipmentsPage.columns.actions')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredShipments.map((shipment) => {
                    const shipper =
                      shippers.find(
                        (currentShipper) =>
                          currentShipper.userId === shipment.shipperId,
                      ) ?? null

                    return (
                      <tr
                        key={shipment.shipmentId}
                        className="border-b border-border"
                      >
                        <td className="px-6 py-4 text-sm font-medium text-foreground">
                          {shipment.shipmentId}
                        </td>
                        <td className="px-6 py-4 text-sm text-foreground">
                          <div className="font-medium">{shipment.orderCode}</div>
                          <div className="text-muted-foreground">
                            {shipment.orderId}
                          </div>
                        </td>
                        <td className="px-6 py-4 text-sm text-foreground">
                          <div className="font-medium">{shipment.receiverName}</div>
                          <div className="text-muted-foreground">
                            {shipment.receiverPhone}
                          </div>
                        </td>
                        <td className="px-6 py-4 text-sm text-muted-foreground">
                          {shipper?.username ?? shipment.shipperId}
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <Badge
                            variant={shipmentStatusVariants[shipment.shipmentStatus]}
                          >
                            {getShipmentStatusLabel(shipment.shipmentStatus, t)}
                          </Badge>
                        </td>
                        <td className="px-6 py-4 text-sm font-medium text-foreground">
                          {formatCurrency(shipment.finalAmount)}
                        </td>
                        <td className="px-6 py-4 text-sm text-muted-foreground">
                          {formatDateTime(locale, shipment.assignedAt)}
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => void handleViewShipment(shipment.shipmentId)}
                          >
                            <Eye className="h-4 w-4" />
                          </Button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}

          {!isLoading && !error && filteredShipments.length === 0 ? (
            <div className="px-6 py-8 text-center">
              <p className="text-muted-foreground">{t('admin.shipmentsPage.empty')}</p>
            </div>
          ) : null}

          {!isLoading && !error && totalCount > 0 ? (
            <PaginationControls
              page={page}
              size={pageSize}
              totalCount={totalCount}
              onPageChange={handlePageChange}
            />
          ) : null}
        </div>

        {selectedShipmentId ? (
          <div className="mt-8 rounded-2xl border border-border bg-card p-6">
            {isDetailLoading || !selectedShipment ? (
              <p className="text-muted-foreground">
                {t('admin.shipmentsPage.detailLoading')}
              </p>
            ) : (
              <div className="space-y-6">
                <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div>
                    <h2 className="font-heading text-2xl font-bold text-foreground">
                      {t('admin.shipmentsPage.detailTitle')}
                    </h2>
                    <p className="mt-2 text-sm text-muted-foreground">
                      {selectedShipment.shipmentId}
                    </p>
                  </div>

                  <div className="flex flex-wrap gap-3">
                    <Badge
                      variant={
                        shipmentStatusVariants[selectedShipment.shipmentStatus]
                      }
                    >
                      {getShipmentStatusLabel(selectedShipment.shipmentStatus, t)}
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
                    label={t('admin.shipmentsPage.detail.orderCode')}
                    value={selectedShipment.orderCode}
                  />
                  <DetailCard
                    label={t('admin.shipmentsPage.detail.shipper')}
                    value={
                      selectedShipmentShipper?.username ?? selectedShipment.shipperId
                    }
                  />
                  <DetailCard
                    label={t('admin.shipmentsPage.detail.payment')}
                    value={`${getPaymentMethodLabel(
                      selectedShipment.paymentMethod,
                      t,
                    )} / ${getPaymentStatusLabel(
                      selectedShipment.paymentStatus,
                      t,
                    )}`}
                  />
                  <DetailCard
                    label={t('admin.shipmentsPage.detail.totalAmount')}
                    value={formatCurrency(selectedShipment.finalAmount)}
                  />
                </div>

                <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
                  <div className="rounded-2xl border border-border p-5">
                    <h3 className="font-semibold text-foreground">
                      {t('admin.shipmentsPage.deliveryInfoTitle')}
                    </h3>
                    <div className="mt-4 grid gap-4 md:grid-cols-2">
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.receiver')}
                        value={selectedShipment.receiverName}
                      />
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.phone')}
                        value={selectedShipment.receiverPhone}
                      />
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.address')}
                        value={selectedShipment.receiverAddress}
                      />
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.orderStatus')}
                        value={getOrderStatusLabel(selectedShipment.orderStatus, t)}
                      />
                    </div>

                    {selectedShipment.failureReason ? (
                      <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/5 p-4">
                        <p className="text-xs uppercase tracking-wide text-destructive">
                          {t('admin.shipmentsPage.detail.failureReason')}
                        </p>
                        <p className="mt-2 text-sm text-foreground">
                          {selectedShipment.failureReason}
                        </p>
                      </div>
                    ) : null}
                  </div>

                  <div className="rounded-2xl border border-border p-5">
                    <h3 className="font-semibold text-foreground">
                      {t('admin.shipmentsPage.timelineTitle')}
                    </h3>
                    <div className="mt-4 space-y-3">
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.assigned')}
                        value={formatDateTime(locale, selectedShipment.assignedAt)}
                      />
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.pickedUp')}
                        value={formatDateTime(locale, selectedShipment.pickedUpAt)}
                      />
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.delivering')}
                        value={formatDateTime(locale, selectedShipment.deliveringAt)}
                      />
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.delivered')}
                        value={formatDateTime(locale, selectedShipment.deliveredAt)}
                      />
                      <DetailCard
                        label={t('admin.shipmentsPage.detail.updatedAt')}
                        value={formatDateTime(locale, selectedShipment.updatedAt)}
                      />
                    </div>

                    <Button
                      type="button"
                      className="mt-5 w-full"
                      onClick={() => void handleConfirmDelivered()}
                      disabled={
                        isConfirming ||
                        selectedShipment.shipmentStatus !== 'DELIVERING'
                      }
                    >
                      {isConfirming
                        ? t('admin.shipmentsPage.confirming')
                        : t('admin.shipmentsPage.confirmDelivered')}
                    </Button>
                    <p className="mt-2 text-xs text-muted-foreground">
                      {t('admin.shipmentsPage.confirmHint')}
                    </p>
                  </div>
                </div>
              </div>
            )}
          </div>
        ) : null}
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
      <p className="mt-1 break-words font-medium text-foreground">{value}</p>
    </div>
  )
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-border/70 bg-muted/30 p-4">
      <p className="text-xs uppercase tracking-wide text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 text-2xl font-semibold text-foreground">{value}</p>
    </div>
  )
}

function formatDateTime(locale: string, value: string | null) {
  if (!value) {
    return '--'
  }

  return new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

import { Check, Eye, Search, X } from 'lucide-react'
import type { ReactNode } from 'react'
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
import { Textarea } from '@/components/common/textarea'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import {
  adminReturnRequestStatusOptions,
  useAdminReturnRequestsPage,
} from '@/hooks/use-admin-return-requests-page'
import type { ReturnRequestStatus } from '@/types/return-request'
import { getReturnRequestStatusLabel } from '@/utils/i18n'

const statusVariants: Record<
  ReturnRequestStatus,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  PENDING: 'secondary',
  APPROVED: 'default',
  REJECTED: 'destructive',
  CANCELLED: 'outline',
}

export default function AdminReturnRequestsPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const {
    filteredRequests,
    page,
    pageSize,
    totalCount,
    searchTerm,
    statusFilter,
    selectedRequestId,
    selectedRequest,
    approveNote,
    approveAmount,
    approveRestock,
    rejectNote,
    isApproveDialogOpen,
    isRejectDialogOpen,
    isLoading,
    isDetailLoading,
    isSubmitting,
    error,
    handlePageChange,
    handleSearchTermChange,
    handleStatusFilterChange,
    handleViewRequest,
    closeDetail,
    openApproveDialog,
    openRejectDialog,
    closeApproveDialog,
    closeRejectDialog,
    handleApprove,
    handleReject,
    setApproveNote,
    setApproveAmount,
    setApproveRestock,
    setRejectNote,
  } = useAdminReturnRequestsPage()

  return (
    <AdminLayout>
      <div>
        <PageHeader
          title={t('admin.returnRequestsPage.title')}
          description={t('admin.returnRequestsPage.total', {
            count: formatNumber(totalCount),
          })}
        />

        <div className="mt-8 grid gap-4 lg:grid-cols-[minmax(0,1fr)_220px]">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder={t('admin.returnRequestsPage.searchPlaceholder')}
              value={searchTerm}
              onChange={handleSearchTermChange}
              className={`${formControlClassName} pl-10`}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="adminReturnRequestStatus">
              {t('admin.returnRequestsPage.filterLabel')}
            </Label>
            <select
              id="adminReturnRequestStatus"
              value={statusFilter}
              onChange={handleStatusFilterChange}
              className={`w-full ${formControlClassName}`}
            >
              <option value="ALL">{t('admin.returnRequestsPage.allStatuses')}</option>
              {adminReturnRequestStatusOptions.map((status) => (
                <option key={status} value={status}>
                  {getReturnRequestStatusLabel(status, t)}
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
                      {t('admin.returnRequestsPage.columns.orderCode')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.returnRequestsPage.columns.customer')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.returnRequestsPage.columns.requestedAmount')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.returnRequestsPage.columns.status')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.returnRequestsPage.columns.createdAt')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.returnRequestsPage.columns.actions')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredRequests.map((request) => (
                    <tr key={request.id} className="border-b border-border">
                      <td className="px-6 py-4 text-sm font-medium text-foreground">
                        {request.orderCode}
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        <div className="space-y-1">
                          <p>{request.username ?? request.receiverName ?? '-'}</p>
                          <p className="text-xs text-muted-foreground">
                            {request.userEmail ?? '-'}
                          </p>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm font-medium text-foreground">
                        {request.requestedRefundAmount == null
                          ? t('admin.returnRequestsPage.notProvided')
                          : formatCurrency(request.requestedRefundAmount)}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <Badge variant={statusVariants[request.status]}>
                          {getReturnRequestStatusLabel(request.status, t)}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 text-sm text-muted-foreground">
                        {formatDate(request.createdAt)}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => void handleViewRequest(request.id)}
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

          {!isLoading && !error && filteredRequests.length === 0 ? (
            <div className="p-6">
              <StatePanel
                title={t('admin.returnRequestsPage.empty')}
                minHeightClassName="min-h-[160px]"
              />
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
        </SurfaceCard>

        {selectedRequestId ? (
          <SurfaceCard className="mt-8 p-6">
            {isDetailLoading || !selectedRequest ? (
              <StatePanel title={t('common.loading')} minHeightClassName="min-h-[180px]" />
            ) : (
              <div className="space-y-6">
                <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div>
                    <h2 className="font-heading text-2xl font-bold text-foreground">
                      {t('admin.returnRequestsPage.detailTitle')}
                    </h2>
                    <p className="mt-2 text-sm text-muted-foreground">
                      {selectedRequest.orderCode}
                    </p>
                  </div>

                  <div className="flex flex-wrap gap-3">
                    <Badge variant={statusVariants[selectedRequest.status]}>
                      {getReturnRequestStatusLabel(selectedRequest.status, t)}
                    </Badge>
                    {selectedRequest.status === 'PENDING' ? (
                      <>
                        <Button
                          type="button"
                          onClick={openApproveDialog}
                          className={primaryButtonClassName}
                        >
                          <Check className="mr-2 h-4 w-4" />
                          {t('admin.returnRequestsPage.approveAction')}
                        </Button>
                        <Button
                          type="button"
                          variant="destructive"
                          onClick={openRejectDialog}
                          className={primaryButtonClassName}
                        >
                          <X className="mr-2 h-4 w-4" />
                          {t('admin.returnRequestsPage.rejectAction')}
                        </Button>
                      </>
                    ) : null}
                    <Button
                      type="button"
                      variant="outline"
                      onClick={closeDetail}
                      className={secondaryButtonClassName}
                    >
                      {t('common.close')}
                    </Button>
                  </div>
                </div>

                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                  <DetailCard
                    label={t('admin.returnRequestsPage.fields.customer')}
                    value={selectedRequest.username ?? '-'}
                  />
                  <DetailCard
                    label={t('admin.returnRequestsPage.fields.email')}
                    value={selectedRequest.userEmail ?? '-'}
                  />
                  <DetailCard
                    label={t('admin.returnRequestsPage.fields.orderAmount')}
                    value={
                      selectedRequest.orderFinalAmount == null
                        ? t('admin.returnRequestsPage.notProvided')
                        : formatCurrency(selectedRequest.orderFinalAmount)
                    }
                  />
                  <DetailCard
                    label={t('admin.returnRequestsPage.fields.createdAt')}
                    value={formatDate(selectedRequest.createdAt)}
                  />
                </div>

                <div className="rounded-2xl border border-border p-5">
                  <h3 className="font-semibold text-foreground">
                    {t('admin.returnRequestsPage.reasonTitle')}
                  </h3>
                  <p className="mt-3 text-sm leading-6 text-muted-foreground">
                    {selectedRequest.reason}
                  </p>
                </div>

                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                  <DetailCard
                    label={t('admin.returnRequestsPage.fields.requestedAmount')}
                    value={
                      selectedRequest.requestedRefundAmount == null
                        ? t('admin.returnRequestsPage.notProvided')
                        : formatCurrency(selectedRequest.requestedRefundAmount)
                    }
                  />
                  <DetailCard
                    label={t('admin.returnRequestsPage.fields.approvedAmount')}
                    value={
                      selectedRequest.approvedRefundAmount == null
                        ? t('admin.returnRequestsPage.notProcessed')
                        : formatCurrency(selectedRequest.approvedRefundAmount)
                    }
                  />
                  <DetailCard
                    label={t('admin.returnRequestsPage.fields.processedBy')}
                    value={selectedRequest.processedByName ?? t('admin.returnRequestsPage.notProcessed')}
                  />
                  <DetailCard
                    label={t('admin.returnRequestsPage.fields.processedAt')}
                    value={
                      selectedRequest.processedAt == null
                        ? t('admin.returnRequestsPage.notProcessed')
                        : formatDate(selectedRequest.processedAt)
                    }
                  />
                </div>

                {selectedRequest.adminNote ? (
                  <div className="rounded-2xl border border-border p-5">
                    <h3 className="font-semibold text-foreground">
                      {t('admin.returnRequestsPage.adminNoteTitle')}
                    </h3>
                    <p className="mt-3 text-sm leading-6 text-muted-foreground">
                      {selectedRequest.adminNote}
                    </p>
                  </div>
                ) : null}
              </div>
            )}
          </SurfaceCard>
        ) : null}
      </div>

      {isApproveDialogOpen ? (
        <DialogShell
          title={t('admin.returnRequestsPage.approveDialogTitle')}
          onClose={closeApproveDialog}
        >
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="approveAmount">
                {t('admin.returnRequestsPage.fields.approvedAmount')}
              </Label>
              <Input
                id="approveAmount"
                inputMode="decimal"
                value={approveAmount}
                onChange={(event) => setApproveAmount(event.currentTarget.value)}
                placeholder="0"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="approveNote">
                {t('admin.returnRequestsPage.fields.adminNote')}
              </Label>
              <Textarea
                id="approveNote"
                value={approveNote}
                onChange={(event) => setApproveNote(event.currentTarget.value)}
                rows={5}
              />
            </div>
            <label className="flex items-center gap-3 rounded-xl border border-border px-4 py-3 text-sm text-foreground">
              <input
                type="checkbox"
                checked={approveRestock}
                onChange={(event) => setApproveRestock(event.currentTarget.checked)}
                className="size-4 rounded border-border"
              />
              {t('admin.returnRequestsPage.restockLabel')}
            </label>
            <div className="flex justify-end gap-3">
              <Button type="button" variant="outline" onClick={closeApproveDialog}>
                {t('common.cancel')}
              </Button>
              <Button
                type="button"
                onClick={() => void handleApprove()}
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? t('common.processing')
                  : t('admin.returnRequestsPage.approveAction')}
              </Button>
            </div>
          </div>
        </DialogShell>
      ) : null}

      {isRejectDialogOpen ? (
        <DialogShell
          title={t('admin.returnRequestsPage.rejectDialogTitle')}
          onClose={closeRejectDialog}
        >
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="rejectNote">
                {t('admin.returnRequestsPage.fields.adminNote')}
              </Label>
              <Textarea
                id="rejectNote"
                value={rejectNote}
                onChange={(event) => setRejectNote(event.currentTarget.value)}
                rows={5}
              />
            </div>
            <div className="flex justify-end gap-3">
              <Button type="button" variant="outline" onClick={closeRejectDialog}>
                {t('common.cancel')}
              </Button>
              <Button
                type="button"
                variant="destructive"
                onClick={() => void handleReject()}
                disabled={isSubmitting || rejectNote.trim() === ''}
              >
                {isSubmitting
                  ? t('common.processing')
                  : t('admin.returnRequestsPage.rejectAction')}
              </Button>
            </div>
          </div>
        </DialogShell>
      ) : null}
    </AdminLayout>
  )
}

function DetailCard({ label, value }: { label: string; value: string }) {
  return <SummaryField label={label} value={value} />
}

function DialogShell({
  title,
  onClose,
  children,
}: {
  title: string
  onClose: () => void
  children: ReactNode
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 px-4">
      <div className="w-full max-w-xl rounded-3xl border border-border bg-card p-6 shadow-2xl">
        <div className="flex items-start justify-between gap-4">
          <h2 className="font-heading text-2xl font-bold text-foreground">
            {title}
          </h2>
          <Button type="button" variant="ghost" size="icon" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>

        <div className="mt-6">{children}</div>
      </div>
    </div>
  )
}

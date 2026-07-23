import { ArrowLeftRight, ReceiptText } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Label } from '@/components/common/label'
import { PaginationControls } from '@/components/common/pagination-controls'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useReturnRequestsPage } from '@/hooks/use-return-requests-page'
import type { ReturnRequestStatus } from '@/types/return-request'
import { cn } from '@/utils'
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

const statusOptions: ReturnRequestStatus[] = [
  'PENDING',
  'APPROVED',
  'REJECTED',
  'CANCELLED',
]

export default function ReturnRequestsPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const {
    requests,
    page,
    pageSize,
    totalCount,
    statusFilter,
    isLoading,
    error,
    handlePageChange,
    handleStatusFilterChange,
  } = useReturnRequestsPage()

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(252,248,255,1)_0%,rgba(246,240,255,0.96)_54%,rgba(255,255,255,1)_100%)] dark:bg-[linear-gradient(180deg,rgba(14,13,22,1)_0%,rgba(10,9,17,1)_54%,rgba(7,7,13,1)_100%)]">
      <Header />

      <main className="flex-1 pb-16 pt-6 sm:pb-20 sm:pt-8">
        <div className="mx-auto flex w-full max-w-[1272px] flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <section className="rounded-[28px] border border-primary/10 bg-white/92 p-6 shadow-[0_14px_38px_rgba(137,92,255,0.08)] backdrop-blur dark:border-white/10 dark:bg-card/92 dark:shadow-[0_14px_38px_rgba(0,0,0,0.26)]">
            <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
              <div>
                <p className="text-[12px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                  {t('returnRequests.eyebrow')}
                </p>
                <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight text-foreground">
                  {t('returnRequests.title')}
                </h1>
                <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground sm:text-base">
                  {t('returnRequests.description')}
                </p>
              </div>

              <div className="w-full max-w-[240px]">
                <Label htmlFor="returnRequestStatusFilter">
                  {t('returnRequests.filterLabel')}
                </Label>
                <select
                  id="returnRequestStatusFilter"
                  value={statusFilter}
                  onChange={handleStatusFilterChange}
                  className="mt-2 h-11 w-full rounded-2xl border border-primary/12 bg-white px-4 text-sm text-foreground outline-none focus:border-primary/35 focus:ring-2 focus:ring-primary/15 dark:border-white/10 dark:bg-background/60"
                >
                  <option value="ALL">{t('returnRequests.allStatuses')}</option>
                  {statusOptions.map((status) => (
                    <option key={status} value={status}>
                      {getReturnRequestStatusLabel(status, t)}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="mt-6 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
              <span>{t('returnRequests.totalCount', { count: formatNumber(totalCount) })}</span>
              <Link to="/orders">
                <Button
                  variant="outline"
                  className="h-11 rounded-2xl border-primary/15 px-4 text-primary hover:bg-primary/6"
                >
                  <ArrowLeftRight className="mr-2 h-4 w-4" />
                  {t('returnRequests.backToOrders')}
                </Button>
              </Link>
            </div>
          </section>

          <section className="rounded-[28px] border border-primary/10 bg-white/92 p-6 shadow-[0_14px_38px_rgba(137,92,255,0.08)] backdrop-blur dark:border-white/10 dark:bg-card/92 dark:shadow-[0_14px_38px_rgba(0,0,0,0.26)]">
            {isLoading ? (
              <div className="rounded-[24px] border border-dashed border-primary/15 bg-primary/4 px-6 py-12 text-center text-muted-foreground">
                {t('common.loading')}
              </div>
            ) : error ? (
              <div className="rounded-[24px] border border-dashed border-destructive/20 bg-destructive/5 px-6 py-12 text-center font-semibold text-destructive">
                {error}
              </div>
            ) : requests.length === 0 ? (
              <div className="rounded-[24px] border border-dashed border-primary/15 bg-primary/4 px-6 py-12 text-center text-muted-foreground">
                {t('returnRequests.empty')}
              </div>
            ) : (
              <div className="space-y-4">
                {requests.map((request) => (
                  <article
                    key={request.id}
                    className="rounded-[24px] border border-primary/10 bg-[linear-gradient(180deg,rgba(255,255,255,0.99)_0%,rgba(249,245,255,0.94)_100%)] p-5 shadow-[0_12px_30px_rgba(137,92,255,0.06)] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(35,31,51,0.97)_0%,rgba(27,24,42,0.95)_100%)] dark:shadow-none"
                  >
                    <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                      <div className="space-y-3">
                        <div className="flex flex-wrap items-center gap-3">
                          <h2 className="font-heading text-2xl font-bold text-foreground">
                            {request.orderCode}
                          </h2>
                          <Badge variant={statusVariants[request.status]}>
                            {getReturnRequestStatusLabel(request.status, t)}
                          </Badge>
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {t('returnRequests.createdAt')}: {formatDate(request.createdAt)}
                        </p>
                        <p className="text-sm leading-6 text-foreground/80">{request.reason}</p>
                      </div>

                      <Link to={`/orders/${request.orderId}`}>
                        <Button
                          variant="outline"
                          className="h-11 rounded-2xl border-primary/15 px-4 text-primary hover:bg-primary/6"
                        >
                          {t('returnRequests.viewOrder')}
                        </Button>
                      </Link>
                    </div>

                    <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                      <DetailCard
                        label={t('returnRequests.requestedAmount')}
                        value={
                          request.requestedRefundAmount == null
                            ? t('returnRequests.notProvided')
                            : formatCurrency(request.requestedRefundAmount)
                        }
                      />
                      <DetailCard
                        label={t('returnRequests.approvedAmount')}
                        value={
                          request.approvedRefundAmount == null
                            ? t('returnRequests.notProcessed')
                            : formatCurrency(request.approvedRefundAmount)
                        }
                      />
                      <DetailCard
                        label={t('returnRequests.orderAmount')}
                        value={
                          request.orderFinalAmount == null
                            ? t('returnRequests.notProvided')
                            : formatCurrency(request.orderFinalAmount)
                        }
                      />
                      <DetailCard
                        label={t('returnRequests.receiverName')}
                        value={request.receiverName ?? t('returnRequests.notProvided')}
                      />
                    </div>

                    {request.adminNote ? (
                      <div className="mt-5 rounded-[20px] border border-slate-200 bg-slate-50/90 px-4 py-4 dark:border-white/10 dark:bg-background/45">
                        <p className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
                          {t('returnRequests.adminNote')}
                        </p>
                        <p className="mt-2 text-sm leading-6 text-foreground/80">
                          {request.adminNote}
                        </p>
                      </div>
                    ) : null}

                    <div className="mt-4 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
                      <span>
                        {t('returnRequests.paymentSummary', {
                          paymentMethod: request.paymentMethod ?? t('returnRequests.unknown'),
                          paymentStatus: request.paymentStatus ?? t('returnRequests.unknown'),
                        })}
                      </span>
                      {request.processedAt ? (
                        <span>
                          {t('returnRequests.processedAt')}: {formatDate(request.processedAt)}
                        </span>
                      ) : null}
                    </div>
                  </article>
                ))}
              </div>
            )}

            {!isLoading && !error && totalCount > 0 ? (
              <div className="mt-6">
                <PaginationControls
                  page={page}
                  size={pageSize}
                  totalCount={totalCount}
                  onPageChange={handlePageChange}
                />
              </div>
            ) : null}
          </section>
        </div>
      </main>

      <Footer />
    </div>
  )
}

function DetailCard({ label, value }: { label: string; value: string }) {
  return (
    <div
      className={cn(
        'rounded-[18px] border border-primary/10 bg-white px-4 py-4 shadow-[0_10px_22px_rgba(137,92,255,0.05)] dark:border-white/10 dark:bg-background/45 dark:shadow-none',
      )}
    >
      <div className="flex items-start gap-3">
        <span className="mt-[1px] shrink-0 text-primary">
          <ReceiptText className="h-[18px] w-[18px]" />
        </span>
        <div className="min-w-0">
          <p className="text-[12px] font-semibold uppercase tracking-[0.14em] text-muted-foreground">
            {label}
          </p>
          <p className="mt-2 break-words text-[1.03rem] font-bold leading-7 text-foreground">
            {value}
          </p>
        </div>
      </div>
    </div>
  )
}

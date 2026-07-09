import { createPortal } from 'react-dom'
import { Eye, Search, ShieldCheck, X } from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { PaginationControls } from '@/components/common/pagination-controls'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import { useAdminAuditLogsPage } from '@/hooks/use-admin-audit-logs-page'
import type { AdminAuditLogResponse } from '@/types/audit-log'

const actionOptions = [
  'ALL',
  'BOOK_CREATED',
  'BOOK_UPDATED',
  'BOOK_DELETED',
  'COUPON_CREATED',
  'COUPON_UPDATED',
  'COUPON_DELETED',
  'ORDER_STATUS_UPDATED',
  'ORDER_CANCELLED',
  'SHIPMENT_ASSIGNED',
  'SHIPMENT_STATUS_UPDATED',
  'STOCK_UPDATED',
  'USER_CREATED',
  'USER_UPDATED',
  'USER_LOCKED',
  'USER_UNLOCKED',
  'USER_DELETED',
] as const

const targetTypeOptions = [
  'ALL',
  'BOOK',
  'COUPON',
  'ORDER',
  'SHIPMENT',
  'STOCK',
  'USER',
] as const

export default function AdminAuditLogsPage() {
  const { t, formatDate, formatNumber } = useLanguage()
  const {
    logs,
    page,
    pageSize,
    totalCount,
    filters,
    selectedLogId,
    selectedLog,
    isLoading,
    isDetailLoading,
    error,
    handlePageChange,
    handleOpenDetail,
    closeDetail,
    updateFilter,
  } = useAdminAuditLogsPage()

  const dialogMarkup = selectedLogId ? (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/72 px-4 py-6 backdrop-blur-sm">
      <div className="w-full max-w-5xl overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
        <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
          <div>
            <h2 className="text-2xl font-semibold text-foreground">
              {t('admin.auditLogsPage.detailTitle')}
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">{selectedLogId}</p>
          </div>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={closeDetail}
            className="rounded-2xl"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>

        <div className="max-h-[80vh] overflow-y-auto px-6 py-6">
          {isDetailLoading || !selectedLog ? (
            <p className="text-muted-foreground">{t('common.loading')}</p>
          ) : (
            <AuditLogDetail
              log={selectedLog}
              formatDate={formatDate}
              t={t}
            />
          )}
        </div>
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(56,189,248,0.16),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.12),transparent_32%)]" />

          <div className="relative">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                    {t('admin.auditLogsPage.title')}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary"
                  >
                    <ShieldCheck className="mr-2 h-4 w-4" />
                    {t('admin.auditLogsPage.total', {
                      count: formatNumber(totalCount),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-3xl text-base text-muted-foreground">
                  {t('admin.auditLogsPage.description')}
                </p>
              </div>
            </div>

            <div className="mt-8 grid gap-4 xl:grid-cols-[minmax(0,1.2fr)_220px_220px_180px_180px]">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={filters.actorKeyword}
                  onChange={(event) =>
                    updateFilter('actorKeyword', event.currentTarget.value)
                  }
                  placeholder={t('admin.auditLogsPage.actorSearch')}
                  className="h-12 rounded-2xl border-border/70 bg-background/55 pl-12"
                />
              </div>

              <FilterSelect
                id="auditAction"
                label={t('admin.auditLogsPage.action')}
                value={filters.action}
                options={actionOptions}
                onChange={(value) => updateFilter('action', value)}
              />

              <FilterSelect
                id="auditTargetType"
                label={t('admin.auditLogsPage.targetType')}
                value={filters.targetType}
                options={targetTypeOptions}
                onChange={(value) => updateFilter('targetType', value)}
              />

              <FilterDate
                id="auditFrom"
                label={t('admin.auditLogsPage.from')}
                value={filters.from}
                onChange={(value) => updateFilter('from', value)}
              />

              <FilterDate
                id="auditTo"
                label={t('admin.auditLogsPage.to')}
                value={filters.to}
                onChange={(value) => updateFilter('to', value)}
              />
            </div>

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/25 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="overflow-x-auto">
                <table className="w-full min-w-[1080px]">
                  <thead>
                    <tr className="border-b border-border/60 bg-background/55">
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.auditLogsPage.columns.createdAt')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.auditLogsPage.columns.actor')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.auditLogsPage.columns.action')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.auditLogsPage.columns.target')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('admin.auditLogsPage.columns.description')}
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                        {t('common.actions')}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {isLoading ? (
                      <tr>
                        <td
                          colSpan={6}
                          className="px-6 py-10 text-center text-muted-foreground"
                        >
                          {t('common.loading')}
                        </td>
                      </tr>
                    ) : logs.length === 0 ? (
                      <tr>
                        <td
                          colSpan={6}
                          className="px-6 py-10 text-center text-muted-foreground"
                        >
                          {t('admin.auditLogsPage.empty')}
                        </td>
                      </tr>
                    ) : (
                      logs.map((log) => (
                        <tr key={log.id} className="border-b border-border/50">
                          <td className="px-6 py-4 text-sm text-foreground">
                            {formatDate(log.createdAt)}
                          </td>
                          <td className="px-6 py-4 text-sm text-foreground">
                            <div className="font-medium">
                              {log.actorUsername ??
                                t('admin.auditLogsPage.systemActor')}
                            </div>
                            <div className="text-muted-foreground">
                              {log.actorRole ?? '-'}
                            </div>
                          </td>
                          <td className="px-6 py-4 text-sm">
                            <Badge variant="outline" className="rounded-xl">
                              {log.action}
                            </Badge>
                          </td>
                          <td className="px-6 py-4 text-sm text-foreground">
                            <div className="font-medium">{log.targetType}</div>
                            <div className="text-muted-foreground">
                              {log.targetId ?? '-'}
                            </div>
                          </td>
                          <td className="px-6 py-4 text-sm text-muted-foreground">
                            {log.description ?? '-'}
                          </td>
                          <td className="px-6 py-4 text-sm">
                            <Button
                              type="button"
                              variant="outline"
                              onClick={() => void handleOpenDetail(log.id)}
                              className="rounded-2xl"
                            >
                              <Eye className="mr-2 h-4 w-4" />
                              {t('common.view')}
                            </Button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>

              {!isLoading && !error && totalCount > 0 ? (
                <PaginationControls
                  page={page}
                  size={pageSize}
                  totalCount={totalCount}
                  onPageChange={handlePageChange}
                />
              ) : null}
            </section>
          </div>
        </div>
      </AdminLayout>

      {dialogMarkup && typeof document !== 'undefined'
        ? createPortal(dialogMarkup, document.body)
        : null}
    </>
  )
}

function AuditLogDetail({
  log,
  formatDate,
  t,
}: {
  log: AdminAuditLogResponse
  formatDate: (value: string | number | Date) => string
  t: (key: string, values?: Record<string, string | number>) => string
}) {
  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <DetailCard
          label={t('admin.auditLogsPage.columns.actor')}
          value={log.actorUsername ?? t('admin.auditLogsPage.systemActor')}
        />
        <DetailCard
          label={t('admin.auditLogsPage.action')}
          value={log.action}
        />
        <DetailCard
          label={t('admin.auditLogsPage.targetType')}
          value={log.targetType}
        />
        <DetailCard
          label={t('admin.auditLogsPage.columns.createdAt')}
          value={formatDate(log.createdAt)}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <DetailCard
          label={t('admin.auditLogsPage.ipAddress')}
          value={log.ipAddress ?? '-'}
        />
        <DetailCard
          label={t('admin.auditLogsPage.userAgent')}
          value={log.userAgent ?? '-'}
        />
      </div>

      <DetailCard
        label={t('admin.auditLogsPage.columns.description')}
        value={log.description ?? '-'}
      />

      <div className="grid gap-6 xl:grid-cols-2">
        <JsonCard
          title={t('admin.auditLogsPage.beforeValue')}
          value={log.beforeValue}
          emptyLabel={t('admin.auditLogsPage.noBeforeValue')}
        />
        <JsonCard
          title={t('admin.auditLogsPage.afterValue')}
          value={log.afterValue}
          emptyLabel={t('admin.auditLogsPage.noAfterValue')}
        />
      </div>
    </div>
  )
}

function FilterSelect({
  id,
  label,
  value,
  options,
  onChange,
}: {
  id: string
  label: string
  value: string
  options: readonly string[]
  onChange: (value: string) => void
}) {
  return (
    <div className="space-y-2">
      <Label htmlFor={id}>{label}</Label>
      <select
        id={id}
        value={value}
        onChange={(event) => onChange(event.currentTarget.value)}
        className="h-12 w-full rounded-2xl border border-border/70 bg-background/55 px-3 text-sm"
      >
        {options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    </div>
  )
}

function FilterDate({
  id,
  label,
  value,
  onChange,
}: {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
}) {
  return (
    <div className="space-y-2">
      <Label htmlFor={id}>{label}</Label>
      <Input
        id={id}
        type="date"
        value={value}
        onChange={(event) => onChange(event.currentTarget.value)}
        className="h-12 rounded-2xl border-border/70 bg-background/55"
      />
    </div>
  )
}

function DetailCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[20px] border border-border/60 bg-background/55 p-4">
      <p className="text-sm text-muted-foreground">{label}</p>
      <p className="mt-2 break-words text-base font-semibold text-foreground">
        {value}
      </p>
    </div>
  )
}

function JsonCard({
  title,
  value,
  emptyLabel,
}: {
  title: string
  value: string | null
  emptyLabel: string
}) {
  return (
    <div className="overflow-hidden rounded-[24px] border border-border/60 bg-background/55">
      <div className="border-b border-border/60 px-5 py-4">
        <h3 className="font-semibold text-foreground">{title}</h3>
      </div>
      <div className="max-h-[360px] overflow-auto px-5 py-4">
        {value ? (
          <pre className="whitespace-pre-wrap break-words text-sm text-foreground">
            {formatJson(value)}
          </pre>
        ) : (
          <p className="text-sm text-muted-foreground">{emptyLabel}</p>
        )}
      </div>
    </div>
  )
}

function formatJson(value: string) {
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

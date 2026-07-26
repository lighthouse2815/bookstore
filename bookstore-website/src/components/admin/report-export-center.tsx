import type { ReactNode } from 'react'
import { Download, RefreshCw } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import {
  SurfaceCard,
  formControlClassName,
  primaryButtonClassName,
} from '@/components/common/page-shell'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { useLanguage } from '@/contexts/language-context'
import { useAdminReportExports } from '@/hooks/use-admin-report-exports'
import type { AdminReviewStatus } from '@/types/admin-access'
import type { OrderStatus } from '@/types/order'
import { cn } from '@/utils'
import { getOrderStatusLabel } from '@/utils/i18n'

const orderStatuses: OrderStatus[] = [
  'PENDING',
  'CONFIRMED',
  'SHIPPING',
  'DELIVERED',
  'CANCELLED',
]

const reviewStatuses: AdminReviewStatus[] = ['PENDING', 'APPROVED', 'HIDDEN']

export function ReportExportCenter() {
  const { t } = useLanguage()
  const reportExports = useAdminReportExports()
  const copy = reportExports.copy

  return (
    <section aria-labelledby="report-export-title">
      <div className="mb-5 max-w-3xl">
        <h2
          id="report-export-title"
          className="font-heading text-2xl font-semibold text-foreground"
        >
          {copy.sectionTitle}
        </h2>
        <p className="mt-2 text-sm leading-6 text-muted-foreground">
          {copy.sectionDescription}
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <ReportExportCard
          title={copy.cards.orders.title}
          description={copy.cards.orders.description}
          isExporting={reportExports.activeExportKey === 'orders'}
          exportLabel={copy.buttons.export}
          exportingLabel={copy.buttons.exporting}
          onExport={() => void reportExports.exportOrders()}
        >
          <div className="grid gap-3 sm:grid-cols-2">
            <FilterField label={copy.filters.from}>
              <Input
                type="date"
                value={reportExports.ordersFrom}
                onChange={(event) =>
                  reportExports.setOrdersFrom(event.target.value)
                }
                className={formControlClassName}
              />
            </FilterField>
            <FilterField label={copy.filters.to}>
              <Input
                type="date"
                value={reportExports.ordersTo}
                onChange={(event) =>
                  reportExports.setOrdersTo(event.target.value)
                }
                className={formControlClassName}
              />
            </FilterField>
            <FilterField label={copy.filters.status} className="sm:col-span-2">
              <Select
                value={reportExports.orderStatus || '__ALL__'}
                onValueChange={(value) =>
                  reportExports.setOrderStatus(
                    value === '__ALL__' ? '' : (value as OrderStatus),
                  )
                }
              >
                <SelectTrigger className="h-11 rounded-2xl border-border/70 bg-background/80 px-4 shadow-sm">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="__ALL__">{copy.allStatuses}</SelectItem>
                  {orderStatuses.map((status) => (
                    <SelectItem key={status} value={status}>
                      {getOrderStatusLabel(status, t)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </FilterField>
          </div>
        </ReportExportCard>

        <ReportExportCard
          title={copy.cards.revenue.title}
          description={copy.cards.revenue.description}
          isExporting={reportExports.activeExportKey === 'revenue'}
          exportLabel={copy.buttons.export}
          exportingLabel={copy.buttons.exporting}
          onExport={() => void reportExports.exportRevenue()}
        >
          <div className="grid gap-3 sm:grid-cols-2">
            <FilterField label={copy.filters.from}>
              <Input
                type="date"
                value={reportExports.revenueFrom}
                onChange={(event) =>
                  reportExports.setRevenueFrom(event.target.value)
                }
                className={formControlClassName}
              />
            </FilterField>
            <FilterField label={copy.filters.to}>
              <Input
                type="date"
                value={reportExports.revenueTo}
                onChange={(event) =>
                  reportExports.setRevenueTo(event.target.value)
                }
                className={formControlClassName}
              />
            </FilterField>
          </div>
        </ReportExportCard>

        <ReportExportCard
          title={copy.cards.lowStock.title}
          description={copy.cards.lowStock.description}
          isExporting={reportExports.activeExportKey === 'low-stock'}
          exportLabel={copy.buttons.export}
          exportingLabel={copy.buttons.exporting}
          onExport={() => void reportExports.exportLowStock()}
        >
          <FilterField label={copy.filters.threshold}>
            <Input
              type="number"
              min={0}
              value={reportExports.lowStockThreshold}
              onChange={(event) =>
                reportExports.setLowStockThreshold(event.target.value)
              }
              className={formControlClassName}
            />
          </FilterField>
        </ReportExportCard>

        <ReportExportCard
          title={copy.cards.reviews.title}
          description={copy.cards.reviews.description}
          isExporting={reportExports.activeExportKey === 'reviews'}
          exportLabel={copy.buttons.export}
          exportingLabel={copy.buttons.exporting}
          onExport={() => void reportExports.exportReviews()}
        >
          <FilterField label={copy.filters.status}>
            <Select
              value={reportExports.reviewStatus || '__ALL__'}
              onValueChange={(value) =>
                reportExports.setReviewStatus(
                  value === '__ALL__' ? '' : (value as AdminReviewStatus),
                )
              }
            >
              <SelectTrigger className="h-11 rounded-2xl border-border/70 bg-background/80 px-4 shadow-sm">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="__ALL__">{copy.allStatuses}</SelectItem>
                {reviewStatuses.map((status) => (
                  <SelectItem key={status} value={status}>
                    {copy.reviewStatuses[status]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </FilterField>
        </ReportExportCard>
      </div>
    </section>
  )
}

function ReportExportCard({
  title,
  description,
  isExporting,
  exportLabel,
  exportingLabel,
  onExport,
  children,
}: {
  title: string
  description: string
  isExporting: boolean
  exportLabel: string
  exportingLabel: string
  onExport: () => void
  children: ReactNode
}) {
  return (
    <SurfaceCard className="motion-card flex min-w-0 flex-col p-5" tone="muted">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <h3 className="font-heading text-lg font-semibold text-foreground">
            {title}
          </h3>
          <p className="mt-1 text-sm leading-6 text-muted-foreground">
            {description}
          </p>
        </div>
        <Button
          className={cn(primaryButtonClassName, 'w-full shrink-0 sm:w-auto')}
          onClick={onExport}
          disabled={isExporting}
        >
          {isExporting ? (
            <RefreshCw className="size-4 animate-spin" />
          ) : (
            <Download className="size-4" />
          )}
          {isExporting ? exportingLabel : exportLabel}
        </Button>
      </div>
      <div className="mt-5">{children}</div>
    </SurfaceCard>
  )
}

function FilterField({
  label,
  className,
  children,
}: {
  label: string
  className?: string
  children: ReactNode
}) {
  return (
    <label className={cn('grid min-w-0 gap-1.5 text-sm font-medium text-foreground', className)}>
      <span>{label}</span>
      {children}
    </label>
  )
}

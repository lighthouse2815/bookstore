import type { ReactNode } from 'react'
import {
  Activity,
  AlertTriangle,
  Clock3,
  Download,
  Package,
  RefreshCw,
  ShoppingCart,
  Star,
  Ticket,
  TrendingUp,
  Users,
  Wallet,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { StatePanel } from '@/components/common/page-shell'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { useAdminDashboardPage } from '@/hooks/use-admin-dashboard-page'
import { useAdminReportExports } from '@/hooks/use-admin-report-exports'
import { AdminLayout } from '@/components/layout/admin-layout'
import type { AdminReviewStatus } from '@/types/admin-access'
import type {
  AdminDashboardRevenueFilter,
  DashboardSummary,
} from '@/types/admin-dashboard'
import type { OrderStatus } from '@/types/order'
import { cn } from '@/utils'
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

const statusChartColors = [
  'var(--color-chart-1)',
  'var(--color-chart-2)',
  'var(--color-chart-3)',
  'var(--color-chart-4)',
  'var(--color-chart-5)',
]

const tooltipStyle = {
  borderRadius: '18px',
  border: '1px solid var(--color-border)',
  backgroundColor: 'var(--color-card)',
  color: 'var(--color-foreground)',
  boxShadow: '0 12px 30px rgba(15, 23, 42, 0.12)',
}

export default function AdminDashboard() {
  const {
    language,
    locale,
    t,
    formatCurrency,
    formatNumber,
    summary,
    revenueChart,
    topBooks,
    orderStatusStats,
    lowStockBooks,
    recentOrders,
    revenueFilter,
    setRevenueFilter,
    isLoading,
    isRefreshing,
    error,
    hasData,
    refresh,
  } = useAdminDashboardPage()
  const reportExports = useAdminReportExports()

  const copy = dashboardCopy[language]
  const exportCopy = reportExports.copy
  const summaryCards = getSummaryCards(summary, copy, {
    formatCurrency,
    formatNumber,
  })
  const totalOrdersByStatus = orderStatusStats.reduce(
    (sum, currentItem) => sum + currentItem.count,
    0,
  )
  const dateTimeFormatter = new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short',
  })

  const revenueFilterOptions: Array<{
    value: AdminDashboardRevenueFilter
    label: string
  }> = [
    {
      value: 'LAST_7_DAYS',
      label: copy.filters.last7Days,
    },
    {
      value: 'LAST_30_DAYS',
      label: copy.filters.last30Days,
    },
    {
      value: 'THIS_MONTH',
      label: copy.filters.thisMonth,
    },
  ]

  if (!isLoading && error && !hasData) {
    return (
      <AdminLayout>
        <StatePanel
          icon={<AlertTriangle className="h-8 w-8 text-destructive" />}
          title={t('common.dashboard')}
          description={error}
          tone="error"
          minHeightClassName="min-h-[420px]"
          action={
            <Button className="rounded-2xl" onClick={() => void refresh()}>
              <RefreshCw className="h-4 w-4" />
              {copy.retry}
            </Button>
          }
        />
      </AdminLayout>
    )
  }

  return (
    <AdminLayout>
      <div className="space-y-6">
        <section className="rounded-[32px] border border-border/70 bg-card px-6 py-6 shadow-[0_18px_50px_rgba(15,23,42,0.06)] dark:shadow-none lg:px-8">
          <div className="flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
            <div className="max-w-3xl">
              <div className="inline-flex items-center gap-2 rounded-full border border-primary/15 bg-primary/8 px-3 py-1 text-sm font-medium text-primary">
                <Activity className="h-4 w-4" />
                {copy.liveLabel}
              </div>
              <h1 className="mt-4 font-heading text-3xl font-bold tracking-tight text-foreground md:text-4xl">
                {t('common.dashboard')}
              </h1>
              <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground md:text-base">
                {copy.description}
              </p>
            </div>

            <div className="flex flex-col gap-3 sm:flex-row">
              <Button
                variant="outline"
                className="h-11 rounded-2xl px-4"
                onClick={() => void refresh()}
                disabled={isRefreshing}
              >
                <RefreshCw
                  className={cn('h-4 w-4', isRefreshing && 'animate-spin')}
                />
                {copy.refresh}
              </Button>
            </div>
          </div>

          {error ? (
            <div className="mt-5 rounded-2xl border border-destructive/20 bg-destructive/8 px-4 py-3 text-sm text-destructive">
              {error}
            </div>
          ) : null}
        </section>

        <section className="grid grid-cols-1 gap-4 sm:grid-cols-2 2xl:grid-cols-4">
          {isLoading
            ? Array.from({ length: 8 }).map((_, index) => (
                <StatCardSkeleton key={index} />
              ))
            : summaryCards.map((card) => (
                <StatCard
                  key={card.label}
                  label={card.label}
                  value={card.value}
                  accentClassName={card.accentClassName}
                  icon={<card.icon className="h-5 w-5" />}
                />
              ))}
        </section>

        <DashboardSectionCard
          title={exportCopy.sectionTitle}
          description={exportCopy.sectionDescription}
        >
          <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
            <ReportExportCard
              title={exportCopy.cards.orders.title}
              description={exportCopy.cards.orders.description}
              action={
                <Button
                  className="w-full rounded-2xl sm:w-auto"
                  onClick={() => void reportExports.exportOrders()}
                  disabled={reportExports.activeExportKey === 'orders'}
                >
                  {reportExports.activeExportKey === 'orders' ? (
                    <RefreshCw className="h-4 w-4 animate-spin" />
                  ) : (
                    <Download className="h-4 w-4" />
                  )}
                  {reportExports.activeExportKey === 'orders'
                    ? exportCopy.buttons.exporting
                    : exportCopy.buttons.export}
                </Button>
              }
            >
              <div className="grid gap-3 sm:grid-cols-2">
                <FilterField label={exportCopy.filters.from}>
                  <Input
                    type="date"
                    value={reportExports.ordersFrom}
                    onChange={(event) =>
                      reportExports.setOrdersFrom(event.target.value)
                    }
                  />
                </FilterField>
                <FilterField label={exportCopy.filters.to}>
                  <Input
                    type="date"
                    value={reportExports.ordersTo}
                    onChange={(event) =>
                      reportExports.setOrdersTo(event.target.value)
                    }
                  />
                </FilterField>
                <FilterField
                  label={exportCopy.filters.status}
                  className="sm:col-span-2"
                >
                  <Select
                    value={reportExports.orderStatus || '__ALL__'}
                    onValueChange={(value) =>
                      reportExports.setOrderStatus(
                        value === '__ALL__' ? '' : (value as OrderStatus),
                      )
                    }
                  >
                    <SelectTrigger className="h-10 rounded-xl bg-background/70">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="__ALL__">
                        {exportCopy.allStatuses}
                      </SelectItem>
                      {(
                        [
                          'PENDING',
                          'CONFIRMED',
                          'SHIPPING',
                          'DELIVERED',
                          'CANCELLED',
                        ] as OrderStatus[]
                      ).map((status) => (
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
              title={exportCopy.cards.revenue.title}
              description={exportCopy.cards.revenue.description}
              action={
                <Button
                  className="w-full rounded-2xl sm:w-auto"
                  onClick={() => void reportExports.exportRevenue()}
                  disabled={reportExports.activeExportKey === 'revenue'}
                >
                  {reportExports.activeExportKey === 'revenue' ? (
                    <RefreshCw className="h-4 w-4 animate-spin" />
                  ) : (
                    <Download className="h-4 w-4" />
                  )}
                  {reportExports.activeExportKey === 'revenue'
                    ? exportCopy.buttons.exporting
                    : exportCopy.buttons.export}
                </Button>
              }
            >
              <div className="grid gap-3 sm:grid-cols-2">
                <FilterField label={exportCopy.filters.from}>
                  <Input
                    type="date"
                    value={reportExports.revenueFrom}
                    onChange={(event) =>
                      reportExports.setRevenueFrom(event.target.value)
                    }
                  />
                </FilterField>
                <FilterField label={exportCopy.filters.to}>
                  <Input
                    type="date"
                    value={reportExports.revenueTo}
                    onChange={(event) =>
                      reportExports.setRevenueTo(event.target.value)
                    }
                  />
                </FilterField>
              </div>
            </ReportExportCard>

            <ReportExportCard
              title={exportCopy.cards.lowStock.title}
              description={exportCopy.cards.lowStock.description}
              action={
                <Button
                  className="w-full rounded-2xl sm:w-auto"
                  onClick={() => void reportExports.exportLowStock()}
                  disabled={reportExports.activeExportKey === 'low-stock'}
                >
                  {reportExports.activeExportKey === 'low-stock' ? (
                    <RefreshCw className="h-4 w-4 animate-spin" />
                  ) : (
                    <Download className="h-4 w-4" />
                  )}
                  {reportExports.activeExportKey === 'low-stock'
                    ? exportCopy.buttons.exporting
                    : exportCopy.buttons.export}
                </Button>
              }
            >
              <div className="grid gap-3 sm:grid-cols-2">
                <FilterField label={exportCopy.filters.threshold}>
                  <Input
                    type="number"
                    min={0}
                    value={reportExports.lowStockThreshold}
                    onChange={(event) =>
                      reportExports.setLowStockThreshold(event.target.value)
                    }
                  />
                </FilterField>
              </div>
            </ReportExportCard>

            <ReportExportCard
              title={exportCopy.cards.reviews.title}
              description={exportCopy.cards.reviews.description}
              action={
                <Button
                  className="w-full rounded-2xl sm:w-auto"
                  onClick={() => void reportExports.exportReviews()}
                  disabled={reportExports.activeExportKey === 'reviews'}
                >
                  {reportExports.activeExportKey === 'reviews' ? (
                    <RefreshCw className="h-4 w-4 animate-spin" />
                  ) : (
                    <Download className="h-4 w-4" />
                  )}
                  {reportExports.activeExportKey === 'reviews'
                    ? exportCopy.buttons.exporting
                    : exportCopy.buttons.export}
                </Button>
              }
            >
              <div className="grid gap-3 sm:grid-cols-2">
                <FilterField label={exportCopy.filters.status}>
                  <Select
                    value={reportExports.reviewStatus || '__ALL__'}
                    onValueChange={(value) =>
                      reportExports.setReviewStatus(
                        value === '__ALL__' ? '' : (value as AdminReviewStatus),
                      )
                    }
                  >
                    <SelectTrigger className="h-10 rounded-xl bg-background/70">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="__ALL__">
                        {exportCopy.allStatuses}
                      </SelectItem>
                      {(
                        ['PENDING', 'APPROVED', 'HIDDEN'] as AdminReviewStatus[]
                      ).map((status) => (
                        <SelectItem key={status} value={status}>
                          {exportCopy.reviewStatuses[status]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </FilterField>
              </div>
            </ReportExportCard>
          </div>
        </DashboardSectionCard>

        <DashboardSectionCard
          title={copy.sections.revenue}
          description={copy.revenueDescription}
          action={
            <Select
              value={revenueFilter}
              onValueChange={(nextValue) => {
                if (nextValue) {
                  setRevenueFilter(nextValue as AdminDashboardRevenueFilter)
                }
              }}
            >
              <SelectTrigger className="h-11 w-full rounded-2xl bg-background/70 sm:w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {revenueFilterOptions.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          }
        >
          {isLoading ? (
            <ChartSkeleton />
          ) : revenueChart.length === 0 ? (
            <EmptyState message={copy.emptyRevenue} />
          ) : (
            <div className="h-[340px]">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart
                  data={revenueChart}
                  margin={{ top: 12, right: 16, left: 0, bottom: 0 }}
                >
                  <CartesianGrid
                    vertical={false}
                    stroke="var(--color-border)"
                    strokeOpacity={0.55}
                  />
                  <XAxis
                    dataKey="label"
                    tickLine={false}
                    axisLine={false}
                    tick={{ fill: 'var(--color-muted-foreground)', fontSize: 12 }}
                  />
                  <YAxis
                    yAxisId="revenue"
                    tickLine={false}
                    axisLine={false}
                    tickFormatter={(value) => formatCompactCurrency(locale, value)}
                    tick={{ fill: 'var(--color-muted-foreground)', fontSize: 12 }}
                  />
                  <YAxis yAxisId="orders" orientation="right" hide />
                  <Tooltip
                    contentStyle={tooltipStyle}
                    formatter={(value, name) =>
                      name === copy.metrics.orders
                        ? [formatNumber(Number(value)), name]
                        : [formatCurrency(Number(value)), name]
                    }
                    labelStyle={{ color: 'var(--color-foreground)' }}
                  />
                  <Legend />
                  <Line
                    yAxisId="revenue"
                    type="monotone"
                    dataKey="revenue"
                    name={copy.metrics.revenue}
                    stroke="var(--color-chart-1)"
                    strokeWidth={3}
                    dot={{ fill: 'var(--color-chart-1)', r: 3 }}
                    activeDot={{ r: 5 }}
                  />
                  <Line
                    yAxisId="orders"
                    type="monotone"
                    dataKey="orders"
                    name={copy.metrics.orders}
                    stroke="var(--color-chart-2)"
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </DashboardSectionCard>

        <section className="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1.35fr)_minmax(320px,0.95fr)]">
          <DashboardSectionCard
            title={copy.sections.topBooks}
            description={copy.topBooksDescription}
          >
            {isLoading ? (
              <ChartSkeleton />
            ) : topBooks.length === 0 ? (
              <EmptyState message={copy.emptyTopBooks} />
            ) : (
              <div className="h-[340px]">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={topBooks}
                    layout="vertical"
                    margin={{ top: 8, right: 8, left: 24, bottom: 8 }}
                  >
                    <CartesianGrid
                      horizontal={false}
                      stroke="var(--color-border)"
                      strokeOpacity={0.4}
                    />
                    <XAxis
                      type="number"
                      tickLine={false}
                      axisLine={false}
                      tick={{ fill: 'var(--color-muted-foreground)', fontSize: 12 }}
                    />
                    <YAxis
                      dataKey="title"
                      type="category"
                      width={138}
                      tickLine={false}
                      axisLine={false}
                      tickFormatter={(value: string) => truncateLabel(value, 18)}
                      tick={{ fill: 'var(--color-muted-foreground)', fontSize: 12 }}
                    />
                    <Tooltip
                      contentStyle={tooltipStyle}
                      formatter={(value) => [
                        formatNumber(Number(value)),
                        copy.metrics.soldQuantity,
                      ]}
                    />
                    <Bar
                      dataKey="soldQuantity"
                      name={copy.metrics.soldQuantity}
                      fill="var(--color-chart-3)"
                      radius={[0, 14, 14, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </DashboardSectionCard>

          <DashboardSectionCard
            title={copy.sections.orderStatus}
            description={copy.orderStatusDescription}
          >
            {isLoading ? (
              <ChartSkeleton />
            ) : orderStatusStats.length === 0 ? (
              <EmptyState message={copy.emptyOrderStatus} />
            ) : (
              <>
                <div className="h-[300px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={orderStatusStats}
                        dataKey="count"
                        nameKey="status"
                        innerRadius={72}
                        outerRadius={104}
                        paddingAngle={4}
                      >
                        {orderStatusStats.map((item, index) => (
                          <Cell
                            key={`${item.status ?? 'unknown'}-${index}`}
                            fill={statusChartColors[index % statusChartColors.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip
                        contentStyle={tooltipStyle}
                        formatter={(value) => formatNumber(Number(value))}
                        labelFormatter={(value) =>
                          typeof value === 'string'
                            ? getOrderStatusLabel(value as OrderStatus, t)
                            : ''
                        }
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                <div className="mt-4 space-y-3">
                  {orderStatusStats.map((item, index) => (
                    <div
                      key={`${item.status ?? 'unknown'}-${index}`}
                      className="flex items-center justify-between rounded-2xl border border-border/60 bg-background/60 px-4 py-3"
                    >
                      <div className="flex min-w-0 items-center gap-3">
                        <span
                          className="size-3 rounded-full"
                          style={{
                            backgroundColor:
                              statusChartColors[index % statusChartColors.length],
                          }}
                        />
                        <span className="truncate text-sm font-medium text-foreground">
                          {item.status
                            ? getOrderStatusLabel(item.status, t)
                            : copy.unknownStatus}
                        </span>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-semibold text-foreground">
                          {formatNumber(item.count)}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {getStatusPercent(item.count, totalOrdersByStatus)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </DashboardSectionCard>
        </section>

        <section className="grid grid-cols-1 gap-6 2xl:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)]">
          <DashboardSectionCard
            title={copy.sections.lowStock}
            description={copy.lowStockDescription}
          >
            {isLoading ? (
              <TableSkeleton rows={5} />
            ) : lowStockBooks.length === 0 ? (
              <EmptyState message={copy.emptyLowStock} />
            ) : (
              <div className="overflow-hidden rounded-[24px] border border-border/60">
                <div className="overflow-x-auto">
                  <table className="min-w-full">
                    <thead className="bg-muted/45">
                      <tr>
                        <th className="px-5 py-4 text-left text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                          {copy.columns.book}
                        </th>
                        <th className="px-5 py-4 text-right text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                          {copy.columns.stockQuantity}
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {lowStockBooks.map((book) => (
                        <tr
                          key={book.bookId}
                          className="border-t border-border/60 bg-card"
                        >
                          <td className="px-5 py-4">
                            <Link
                              to={`/books/${book.bookId}`}
                              className="block max-w-[320px] truncate text-sm font-semibold text-foreground transition hover:text-primary"
                            >
                              {book.title}
                            </Link>
                          </td>
                          <td className="px-5 py-4 text-right">
                            <Badge
                              variant={
                                book.stockQuantity <= 5 ? 'destructive' : 'secondary'
                              }
                              className="min-w-[68px] justify-center"
                            >
                              {formatNumber(book.stockQuantity)}
                            </Badge>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </DashboardSectionCard>

          <DashboardSectionCard
            title={copy.sections.recentOrders}
            description={copy.recentOrdersDescription}
          >
            {isLoading ? (
              <TableSkeleton rows={5} />
            ) : recentOrders.length === 0 ? (
              <EmptyState message={copy.emptyOrders} />
            ) : (
              <div className="overflow-hidden rounded-[24px] border border-border/60">
                <div className="overflow-x-auto">
                  <table className="min-w-full">
                    <thead className="bg-muted/45">
                      <tr>
                        <th className="px-5 py-4 text-left text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                          {copy.columns.orderId}
                        </th>
                        <th className="px-5 py-4 text-left text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                          {copy.columns.customer}
                        </th>
                        <th className="px-5 py-4 text-right text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                          {copy.columns.total}
                        </th>
                        <th className="px-5 py-4 text-left text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                          {copy.columns.status}
                        </th>
                        <th className="px-5 py-4 text-left text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                          {copy.columns.date}
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {recentOrders.map((order) => (
                        <tr
                          key={order.orderId}
                          className="border-t border-border/60 bg-card"
                        >
                          <td className="px-5 py-4 align-top">
                            <p className="max-w-[220px] truncate text-sm font-semibold text-foreground">
                              {order.orderCode}
                            </p>
                            <p className="mt-1 max-w-[220px] truncate text-xs text-muted-foreground">
                              {order.orderId}
                            </p>
                          </td>
                          <td className="px-5 py-4 text-sm text-foreground">
                            {order.customerName}
                          </td>
                          <td className="px-5 py-4 text-right text-sm font-semibold text-foreground">
                            {formatCurrency(order.finalAmount)}
                          </td>
                          <td className="px-5 py-4">
                            <Badge
                              variant={
                                order.status
                                  ? statusVariants[order.status]
                                  : 'outline'
                              }
                            >
                              {order.status
                                ? getOrderStatusLabel(order.status, t)
                                : copy.unknownStatus}
                            </Badge>
                          </td>
                          <td className="px-5 py-4 text-sm text-muted-foreground">
                            {dateTimeFormatter.format(new Date(order.createdAt))}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </DashboardSectionCard>
        </section>
      </div>
    </AdminLayout>
  )
}

function ReportExportCard({
  title,
  description,
  action,
  children,
}: {
  title: string
  description: string
  action: ReactNode
  children: ReactNode
}) {
  return (
    <div className="rounded-[28px] border border-border/60 bg-background/40 p-5">
      <div className="flex flex-col gap-4">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="max-w-xl">
            <h3 className="font-heading text-xl font-semibold text-foreground">
              {title}
            </h3>
            <p className="mt-2 text-sm leading-6 text-muted-foreground">
              {description}
            </p>
          </div>
          <div className="shrink-0">{action}</div>
        </div>

        <div>{children}</div>
      </div>
    </div>
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
    <label className={cn('block space-y-2', className)}>
      <span className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
        {label}
      </span>
      {children}
    </label>
  )
}

function DashboardSectionCard({
  title,
  description,
  action,
  children,
}: {
  title: string
  description?: string
  action?: ReactNode
  children: ReactNode
}) {
  return (
    <section className="rounded-[32px] border border-border/70 bg-card p-6 shadow-[0_16px_40px_rgba(15,23,42,0.05)] dark:shadow-none">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="font-heading text-2xl font-bold text-foreground">
            {title}
          </h2>
          {description ? (
            <p className="mt-2 text-sm leading-6 text-muted-foreground">
              {description}
            </p>
          ) : null}
        </div>
        {action ? <div className="shrink-0">{action}</div> : null}
      </div>

      <div className="mt-6">{children}</div>
    </section>
  )
}

function StatCard({
  label,
  value,
  icon,
  accentClassName,
}: {
  label: string
  value: string
  icon: ReactNode
  accentClassName: string
}) {
  return (
    <div className="rounded-[28px] border border-border/70 bg-card p-5 shadow-[0_12px_32px_rgba(15,23,42,0.04)] dark:shadow-none">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <p className="text-sm font-medium text-muted-foreground">{label}</p>
          <p className="mt-4 truncate font-heading text-2xl font-bold text-foreground">
            {value}
          </p>
        </div>
        <div
          className={cn(
            'flex size-12 shrink-0 items-center justify-center rounded-2xl',
            accentClassName,
          )}
        >
          {icon}
        </div>
      </div>
    </div>
  )
}

function EmptyState({ message }: { message: string }) {
  return (
    <StatePanel description={message} minHeightClassName="min-h-[220px]" />
  )
}

function StatCardSkeleton() {
  return (
    <div className="rounded-[28px] border border-border/70 bg-card p-5">
      <div className="animate-pulse space-y-4">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 space-y-3">
            <div className="h-3 w-28 rounded-full bg-muted" />
            <div className="h-8 w-32 rounded-full bg-muted" />
          </div>
          <div className="size-12 rounded-2xl bg-muted" />
        </div>
      </div>
    </div>
  )
}

function ChartSkeleton() {
  return (
    <div className="h-[340px] rounded-[24px] border border-border/60 bg-background/40 p-4">
      <div className="flex h-full animate-pulse items-end gap-3">
        {Array.from({ length: 8 }).map((_, index) => (
          <div
            key={index}
            className="flex-1 rounded-t-2xl bg-muted"
            style={{ height: `${30 + (index % 5) * 14}%` }}
          />
        ))}
      </div>
    </div>
  )
}

function TableSkeleton({ rows }: { rows: number }) {
  return (
    <div className="rounded-[24px] border border-border/60 bg-background/40 p-4">
      <div className="animate-pulse space-y-3">
        <div className="h-10 rounded-2xl bg-muted" />
        {Array.from({ length: rows }).map((_, index) => (
          <div key={index} className="h-14 rounded-2xl bg-muted/80" />
        ))}
      </div>
    </div>
  )
}

function getSummaryCards(
  summary: DashboardSummary | null,
  copy: DashboardCopy,
  helpers: {
    formatCurrency: (value: number) => string
    formatNumber: (value: number) => string
  },
) {
  const safeSummary: DashboardSummary = summary ?? {
    totalRevenue: 0,
    todayRevenue: 0,
    monthRevenue: 0,
    totalOrders: 0,
    todayOrders: 0,
    pendingOrders: 0,
    deliveredOrders: 0,
    cancelledOrders: 0,
    totalUsers: 0,
    totalBooks: 0,
    lowStockBooks: 0,
    newCustomers: 0,
    newReviews: 0,
    activeCoupons: 0,
  }

  return [
    {
      label: copy.stats.totalRevenue,
      value: helpers.formatCurrency(safeSummary.totalRevenue),
      icon: Wallet,
      accentClassName: 'bg-primary/12 text-primary',
    },
    {
      label: copy.stats.totalOrders,
      value: helpers.formatNumber(safeSummary.totalOrders),
      icon: ShoppingCart,
      accentClassName:
        'bg-emerald-500/12 text-emerald-600 dark:bg-emerald-500/18 dark:text-emerald-300',
    },
    {
      label: copy.stats.pendingOrders,
      value: helpers.formatNumber(safeSummary.pendingOrders),
      icon: Clock3,
      accentClassName:
        'bg-sky-500/12 text-sky-600 dark:bg-sky-500/18 dark:text-sky-300',
    },
    {
      label: copy.stats.deliveredOrders,
      value: helpers.formatNumber(safeSummary.deliveredOrders),
      icon: TrendingUp,
      accentClassName:
        'bg-amber-500/12 text-amber-600 dark:bg-amber-500/18 dark:text-amber-300',
    },
    {
      label: copy.stats.cancelledOrders,
      value: helpers.formatNumber(safeSummary.cancelledOrders),
      icon: AlertTriangle,
      accentClassName:
        'bg-rose-500/12 text-rose-600 dark:bg-rose-500/18 dark:text-rose-300',
    },
    {
      label: copy.stats.totalUsers,
      value: helpers.formatNumber(safeSummary.totalUsers),
      icon: Users,
      accentClassName:
        'bg-indigo-500/12 text-indigo-600 dark:bg-indigo-500/18 dark:text-indigo-300',
    },
    {
      label: copy.stats.totalBooks,
      value: helpers.formatNumber(safeSummary.totalBooks),
      icon: Star,
      accentClassName:
        'bg-fuchsia-500/12 text-fuchsia-600 dark:bg-fuchsia-500/18 dark:text-fuchsia-300',
    },
    {
      label: copy.stats.lowStockBooks,
      value: helpers.formatNumber(safeSummary.lowStockBooks),
      icon: Ticket,
      accentClassName:
        'bg-cyan-500/12 text-cyan-600 dark:bg-cyan-500/18 dark:text-cyan-300',
    },
  ]
}

function formatCompactCurrency(locale: string, value: number) {
  return new Intl.NumberFormat(locale, {
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(value)
}

function truncateLabel(label: string, maxLength: number) {
  if (label.length <= maxLength) {
    return label
  }

  return `${label.slice(0, maxLength - 1)}…`
}

function getStatusPercent(count: number, total: number) {
  if (total === 0) {
    return '0%'
  }

  return `${Math.round((count / total) * 100)}%`
}

type DashboardCopy = {
  description: string
  liveLabel: string
  refresh: string
  retry: string
  revenueDescription: string
  topBooksDescription: string
  orderStatusDescription: string
  lowStockDescription: string
  recentOrdersDescription: string
  emptyRevenue: string
  emptyTopBooks: string
  emptyOrderStatus: string
  emptyLowStock: string
  emptyOrders: string
  unknownStatus: string
  filters: {
    last7Days: string
    last30Days: string
    thisMonth: string
  }
  metrics: {
    revenue: string
    orders: string
    soldQuantity: string
  }
  sections: {
    revenue: string
    topBooks: string
    orderStatus: string
    lowStock: string
    recentOrders: string
  }
  stats: {
    totalRevenue: string
    totalOrders: string
    pendingOrders: string
    deliveredOrders: string
    cancelledOrders: string
    totalUsers: string
    totalBooks: string
    lowStockBooks: string
  }
  columns: {
    orderId: string
    customer: string
    total: string
    status: string
    date: string
    book: string
    stockQuantity: string
  }
}

const dashboardCopy = {
  vi: {
    description:
      'Theo dõi doanh thu, đơn hàng và tồn kho từ hệ thống backend trong một màn hình tổng hợp.',
    liveLabel: 'Dữ liệu realtime từ backend',
    refresh: 'Làm mới',
    retry: 'Thử lại',
    revenueDescription:
      'So sánh doanh thu và số đơn theo từng mốc thời gian để theo dõi nhịp bán hàng.',
    topBooksDescription:
      'Top sách bán chạy theo số lượng, ưu tiên hiển thị rõ các tựa sách dài.',
    orderStatusDescription:
      'Phân bổ trạng thái đơn hiện tại để nhìn nhanh áp lực vận hành.',
    lowStockDescription:
      'Các đầu sách sắp chạm ngưỡng tồn kho thấp cần được nhập thêm.',
    recentOrdersDescription:
      'Danh sách đơn hàng mới nhất phát sinh từ backend admin dashboard.',
    emptyRevenue: 'Chưa có dữ liệu doanh thu cho khoảng thời gian này',
    emptyTopBooks: 'Chưa có dữ liệu sách bán chạy',
    emptyOrderStatus: 'Chưa có thống kê trạng thái đơn hàng',
    emptyLowStock: 'Không có sách nào ở ngưỡng sắp hết hàng',
    emptyOrders: 'Chưa có đơn hàng nào để hiển thị',
    unknownStatus: 'Không xác định',
    filters: {
      last7Days: '7 ngày',
      last30Days: '30 ngày',
      thisMonth: 'Tháng này',
    },
    metrics: {
      revenue: 'Doanh thu',
      orders: 'Số đơn',
      soldQuantity: 'Đã bán',
    },
    sections: {
      revenue: 'Doanh thu theo thời gian',
      topBooks: 'Top 10 sách bán chạy',
      orderStatus: 'Tỷ lệ trạng thái đơn hàng',
      lowStock: 'Sách sắp hết hàng',
      recentOrders: 'Đơn hàng mới nhất',
    },
    stats: {
      todayRevenue: 'Doanh thu hôm nay',
      monthRevenue: 'Doanh thu tháng này',
      todayOrders: 'Đơn hôm nay',
      pendingOrders: 'Đơn chờ xử lý',
      lowStockBooks: 'Sách sắp hết hàng',
      newCustomers: 'Khách hàng mới',
      newReviews: 'Đánh giá mới',
      activeCoupons: 'Mã giảm giá hoạt động',
    },
    columns: {
      orderId: 'Mã đơn',
      customer: 'Khách hàng',
      total: 'Tổng tiền',
      status: 'Trạng thái',
      date: 'Ngày tạo',
      book: 'Tên sách',
      stockQuantity: 'Tồn kho',
    },
  },
  en: {
    description:
      'Track revenue, orders, and stock health from the backend in one consolidated view.',
    liveLabel: 'Live backend data',
    refresh: 'Refresh',
    retry: 'Retry',
    revenueDescription:
      'Compare revenue and order volume across time windows to monitor sales pace.',
    topBooksDescription:
      'Best-selling books ranked by quantity while keeping long titles readable.',
    orderStatusDescription:
      'Current order-status split for a quick operational snapshot.',
    lowStockDescription:
      'Books approaching the low-stock threshold and likely needing replenishment.',
    recentOrdersDescription:
      'Most recent orders returned by the admin dashboard backend endpoints.',
    emptyRevenue: 'No revenue data for this period',
    emptyTopBooks: 'No top-selling books available yet',
    emptyOrderStatus: 'No order-status statistics available yet',
    emptyLowStock: 'No books are currently near the low-stock threshold',
    emptyOrders: 'No recent orders to display yet',
    unknownStatus: 'Unknown',
    filters: {
      last7Days: '7 days',
      last30Days: '30 days',
      thisMonth: 'This month',
    },
    metrics: {
      revenue: 'Revenue',
      orders: 'Orders',
      soldQuantity: 'Sold',
    },
    sections: {
      revenue: 'Revenue over time',
      topBooks: 'Top 10 best-selling books',
      orderStatus: 'Order status distribution',
      lowStock: 'Low-stock books',
      recentOrders: 'Recent orders',
    },
    stats: {
      todayRevenue: 'Today revenue',
      monthRevenue: 'This month revenue',
      todayOrders: 'Orders today',
      pendingOrders: 'Pending orders',
      lowStockBooks: 'Low-stock books',
      newCustomers: 'New customers',
      newReviews: 'New reviews',
      activeCoupons: 'Active coupons',
    },
    columns: {
      orderId: 'Order ID',
      customer: 'Customer',
      total: 'Total',
      status: 'Status',
      date: 'Created at',
      book: 'Book',
      stockQuantity: 'Stock',
    },
  },
} as Record<'vi' | 'en', DashboardCopy>

dashboardCopy.vi.stats = {
  totalRevenue: 'Tổng doanh thu',
  totalOrders: 'Tổng đơn hàng',
  pendingOrders: 'Đơn chờ xử lý',
  deliveredOrders: 'Đơn đã giao',
  cancelledOrders: 'Đơn đã hủy',
  totalUsers: 'Tổng người dùng',
  totalBooks: 'Tổng đầu sách',
  lowStockBooks: 'Sách sắp hết hàng',
}

dashboardCopy.en.stats = {
  totalRevenue: 'Total revenue',
  totalOrders: 'Total orders',
  pendingOrders: 'Pending orders',
  deliveredOrders: 'Delivered orders',
  cancelledOrders: 'Cancelled orders',
  totalUsers: 'Total users',
  totalBooks: 'Total books',
  lowStockBooks: 'Low-stock books',
}

import api from './api'
import type { AdminReviewStatus } from '@/types/admin-access'
import type { OrderStatus } from '@/types/order'

type OrderReportParams = {
  from?: string
  to?: string
  status?: OrderStatus
}

type RevenueReportParams = {
  from?: string
  to?: string
}

type LowStockReportParams = {
  threshold?: number
}

type ReviewReportParams = {
  status?: AdminReviewStatus
}

export async function downloadOrdersReport(
  params: OrderReportParams,
): Promise<string> {
  return downloadReport('/admin/reports/orders.csv', params, 'orders-report.csv')
}

export async function downloadRevenueReport(
  params: RevenueReportParams,
): Promise<string> {
  return downloadReport(
    '/admin/reports/revenue.csv',
    params,
    'revenue-report.csv',
  )
}

export async function downloadLowStockReport(
  params: LowStockReportParams,
): Promise<string> {
  return downloadReport(
    '/admin/reports/low-stock.csv',
    params,
    'low-stock-report.csv',
  )
}

export async function downloadReviewsReport(
  params: ReviewReportParams,
): Promise<string> {
  return downloadReport(
    '/admin/reports/reviews.csv',
    params,
    'reviews-report.csv',
  )
}

async function downloadReport(
  path: string,
  params: Record<string, string | number | undefined>,
  fallbackFilename: string,
) {
  const response = await api.get<Blob>(path, {
    params: Object.fromEntries(
      Object.entries(params).filter(([, value]) => value !== undefined),
    ),
    responseType: 'blob',
  })

  const filename = resolveFilename(
    response.headers['content-disposition'],
    fallbackFilename,
  )

  triggerDownload(response.data, filename)
  return filename
}

function resolveFilename(
  contentDisposition: string | undefined,
  fallbackFilename: string,
) {
  if (!contentDisposition) {
    return fallbackFilename
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }

  const plainMatch = contentDisposition.match(/filename="?([^"]+)"?/i)
  return plainMatch?.[1] ?? fallbackFilename
}

function triggerDownload(blob: Blob, filename: string) {
  const blobUrl = window.URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = blobUrl
  anchor.download = filename
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  window.setTimeout(() => window.URL.revokeObjectURL(blobUrl), 0)
}

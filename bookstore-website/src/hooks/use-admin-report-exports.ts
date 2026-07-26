import { useState } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  downloadLowStockReport,
  downloadOrdersReport,
  downloadRevenueReport,
  downloadReviewsReport,
} from '@/services/report-service'
import type { AdminReviewStatus } from '@/types/admin-access'
import type { OrderStatus } from '@/types/order'
import { getErrorMessage } from '@/utils'

type ExportKey = 'orders' | 'revenue' | 'low-stock' | 'reviews'

type ReportExportCopy = {
  sectionTitle: string
  sectionDescription: string
  allStatuses: string
  filters: {
    from: string
    to: string
    status: string
    threshold: string
  }
  buttons: {
    export: string
    exporting: string
  }
  cards: {
    orders: {
      title: string
      description: string
    }
    revenue: {
      title: string
      description: string
    }
    lowStock: {
      title: string
      description: string
    }
    reviews: {
      title: string
      description: string
    }
  }
  reviewStatuses: Record<AdminReviewStatus, string>
  errors: {
    invalidDateRange: string
    invalidThreshold: string
    exportFailed: string
  }
  success: {
    exported: (filename: string) => string
  }
}

const DEFAULT_THRESHOLD = '10'

export function useAdminReportExports() {
  const { language } = useLanguage()
  const copy = reportExportCopy[language]
  const [activeExportKey, setActiveExportKey] = useState<ExportKey | null>(null)
  const [ordersFrom, setOrdersFrom] = useState(getDateInputValue(-29))
  const [ordersTo, setOrdersTo] = useState(getDateInputValue(0))
  const [orderStatus, setOrderStatus] = useState<OrderStatus | ''>('')
  const [revenueFrom, setRevenueFrom] = useState(getDateInputValue(-29))
  const [revenueTo, setRevenueTo] = useState(getDateInputValue(0))
  const [lowStockThreshold, setLowStockThreshold] = useState(DEFAULT_THRESHOLD)
  const [reviewStatus, setReviewStatus] = useState<AdminReviewStatus | ''>('')

  async function exportOrders() {
    if (!isValidDateRange(ordersFrom, ordersTo)) {
      toast.error(copy.errors.invalidDateRange)
      return
    }

    await runExport('orders', () =>
      downloadOrdersReport({
        from: emptyToUndefined(ordersFrom),
        to: emptyToUndefined(ordersTo),
        status: orderStatus || undefined,
      }),
    )
  }

  async function exportRevenue() {
    if (!isValidDateRange(revenueFrom, revenueTo)) {
      toast.error(copy.errors.invalidDateRange)
      return
    }

    await runExport('revenue', () =>
      downloadRevenueReport({
        from: emptyToUndefined(revenueFrom),
        to: emptyToUndefined(revenueTo),
      }),
    )
  }

  async function exportLowStock() {
    const threshold = Number.parseInt(lowStockThreshold, 10)
    if (!Number.isFinite(threshold) || threshold < 0) {
      toast.error(copy.errors.invalidThreshold)
      return
    }

    await runExport('low-stock', () =>
      downloadLowStockReport({
        threshold,
      }),
    )
  }

  async function exportReviews() {
    await runExport('reviews', () =>
      downloadReviewsReport({
        status: reviewStatus || undefined,
      }),
    )
  }

  return {
    copy,
    activeExportKey,
    ordersFrom,
    setOrdersFrom,
    ordersTo,
    setOrdersTo,
    orderStatus,
    setOrderStatus,
    revenueFrom,
    setRevenueFrom,
    revenueTo,
    setRevenueTo,
    lowStockThreshold,
    setLowStockThreshold,
    reviewStatus,
    setReviewStatus,
    exportOrders,
    exportRevenue,
    exportLowStock,
    exportReviews,
  }

  async function runExport(
    key: ExportKey,
    action: () => Promise<string>,
  ): Promise<void> {
    setActiveExportKey(key)

    try {
      const filename = await action()
      toast.success(copy.success.exported(filename))
    } catch (error) {
      toast.error(getErrorMessage(error, copy.errors.exportFailed))
    } finally {
      setActiveExportKey(null)
    }
  }
}

function emptyToUndefined(value: string) {
  const normalized = value.trim()
  return normalized === '' ? undefined : normalized
}

function isValidDateRange(from: string, to: string) {
  if (!from || !to) {
    return true
  }

  return from <= to
}

function getDateInputValue(dayOffset: number) {
  const date = new Date()
  date.setDate(date.getDate() + dayOffset)
  return [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, '0'),
    String(date.getDate()).padStart(2, '0'),
  ].join('-')
}

const reportExportCopy = {
  vi: {
    sectionTitle: 'Xuất báo cáo CSV',
    sectionDescription:
      'Tải nhanh các báo cáo quản trị để mở trực tiếp trong Excel hoặc chia sẻ nội bộ.',
    allStatuses: 'Tất cả trạng thái',
    filters: {
      from: 'Từ ngày',
      to: 'Đến ngày',
      status: 'Trạng thái',
      threshold: 'Ngưỡng tồn kho',
    },
    buttons: {
      export: 'Tải CSV',
      exporting: 'Đang xuất...',
    },
    cards: {
      orders: {
        title: 'Đơn hàng',
        description:
          'Xuất mã đơn, khách hàng, trạng thái đơn, trạng thái thanh toán, tổng tiền và ngày tạo.',
      },
      revenue: {
        title: 'Doanh thu',
        description:
          'Xuất doanh thu theo ngày gồm tổng đơn, doanh thu và số đơn hủy trong khoảng chọn.',
      },
      lowStock: {
        title: 'Tồn kho thấp',
        description:
          'Xuất danh sách sách sắp chạm ngưỡng tồn kho với ISBN và danh mục để xử lý nhập thêm.',
      },
      reviews: {
        title: 'Review moderation',
        description:
          'Xuất review theo trạng thái duyệt để xử lý moderation và theo dõi chất lượng nội dung.',
      },
    },
    reviewStatuses: {
      APPROVED: 'Đã duyệt',
      HIDDEN: 'Đã ẩn',
      PENDING: 'Chờ duyệt',
    },
    errors: {
      invalidDateRange: 'Khoảng ngày không hợp lệ.',
      invalidThreshold: 'Ngưỡng tồn kho phải là số không âm.',
      exportFailed: 'Không thể xuất báo cáo CSV.',
    },
    success: {
      exported: (filename: string) => `Đã tải ${filename}`,
    },
  },
  en: {
    sectionTitle: 'CSV Exports',
    sectionDescription:
      'Download practical admin reports for Excel review or internal sharing.',
    allStatuses: 'All statuses',
    filters: {
      from: 'From',
      to: 'To',
      status: 'Status',
      threshold: 'Stock threshold',
    },
    buttons: {
      export: 'Download CSV',
      exporting: 'Exporting...',
    },
    cards: {
      orders: {
        title: 'Orders',
        description:
          'Export order code, customer, order status, payment status, total amount, and created date.',
      },
      revenue: {
        title: 'Revenue',
        description:
          'Export daily revenue with total orders, revenue, and cancelled orders for the selected range.',
      },
      lowStock: {
        title: 'Low stock',
        description:
          'Export books near the stock threshold with ISBN and category for replenishment work.',
      },
      reviews: {
        title: 'Review moderation',
        description:
          'Export reviews by moderation status for triage and content quality tracking.',
      },
    },
    reviewStatuses: {
      APPROVED: 'Approved',
      HIDDEN: 'Hidden',
      PENDING: 'Pending',
    },
    errors: {
      invalidDateRange: 'The selected date range is invalid.',
      invalidThreshold: 'The stock threshold must be a non-negative number.',
      exportFailed: 'Unable to export the CSV report.',
    },
    success: {
      exported: (filename: string) => `Downloaded ${filename}`,
    },
  },
} as Record<'vi' | 'en', ReportExportCopy>

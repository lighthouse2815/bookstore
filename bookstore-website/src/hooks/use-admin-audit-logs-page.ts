import { useEffect, useMemo, useState } from 'react'
import {
  getAdminAuditLog,
  getAdminAuditLogsPage,
  type AdminAuditLogFilter,
} from '@/services/audit-log-service'
import type { AdminAuditLogResponse } from '@/types/audit-log'

const PAGE_SIZE = 10

const initialFilters = {
  action: 'ALL',
  targetType: 'ALL',
  from: '',
  to: '',
  actorKeyword: '',
}

export function useAdminAuditLogsPage() {
  const [logs, setLogs] = useState<AdminAuditLogResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [filters, setFilters] = useState(initialFilters)
  const [selectedLogId, setSelectedLogId] = useState<string | null>(null)
  const [selectedLog, setSelectedLog] = useState<AdminAuditLogResponse | null>(
    null,
  )
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false

    async function loadLogs() {
      setIsLoading(true)

      try {
        const result = await getAdminAuditLogsPage(buildFilterRequest(filters, page))
        if (isCancelled) {
          return
        }

        setLogs(result.items)
        setTotalCount(result.totalCount)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadLogs()
    return () => {
      isCancelled = true
    }
  }, [filters.action, filters.targetType, filters.from, filters.to, page])

  async function handleOpenDetail(logId: string) {
    setSelectedLogId(logId)
    setIsDetailLoading(true)

    try {
      const result = await getAdminAuditLog(logId)
      setSelectedLog(result)
      setError(null)
    } catch (currentError) {
      setSelectedLog(null)
      setError(getErrorMessage(currentError))
    } finally {
      setIsDetailLoading(false)
    }
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
  }

  function updateFilter(field: keyof typeof initialFilters, value: string) {
    setPage(0)
    setFilters((currentFilters) => ({
      ...currentFilters,
      [field]: value,
    }))
  }

  function closeDetail() {
    setSelectedLogId(null)
    setSelectedLog(null)
  }

  const visibleLogs = useMemo(() => {
    const keyword = filters.actorKeyword.trim().toLowerCase()
    if (keyword === '') {
      return logs
    }

    return logs.filter((log) =>
      [log.actorUsername, log.actorRole, log.description, log.targetId]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [filters.actorKeyword, logs])

  return {
    logs: visibleLogs,
    page,
    pageSize: PAGE_SIZE,
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
  }
}

function buildFilterRequest(
  filters: typeof initialFilters,
  page: number,
): AdminAuditLogFilter {
  return {
    page,
    size: PAGE_SIZE,
    action: filters.action === 'ALL' ? undefined : filters.action,
    targetType: filters.targetType === 'ALL' ? undefined : filters.targetType,
    from: filters.from || undefined,
    to: filters.to || undefined,
  }
}

function getErrorMessage(error: unknown) {
  if (
    typeof error === 'object' &&
    error !== null &&
    'response' in error &&
    typeof error.response === 'object' &&
    error.response !== null &&
    'data' in error.response &&
    typeof error.response.data === 'object' &&
    error.response.data !== null &&
    'message' in error.response.data &&
    typeof error.response.data.message === 'string'
  ) {
    return error.response.data.message
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'Không thể tải nhật ký hệ thống.'
}

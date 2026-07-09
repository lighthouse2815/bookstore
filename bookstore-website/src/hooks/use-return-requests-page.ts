import { useEffect, useState, type ChangeEvent } from 'react'
import { useLanguage } from '@/contexts/language-context'
import { getMyReturnRequestsPage } from '@/services/return-request-service'
import type {
  ReturnRequestResponse,
  ReturnRequestStatus,
} from '@/types/return-request'
import { getErrorMessage } from '@/utils'

type ReturnRequestStatusFilter = 'ALL' | ReturnRequestStatus

const PAGE_SIZE = 10

export function useReturnRequestsPage() {
  const { t } = useLanguage()
  const [requests, setRequests] = useState<ReturnRequestResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [statusFilter, setStatusFilter] =
    useState<ReturnRequestStatusFilter>('ALL')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false
    setIsLoading(true)

    async function loadRequests() {
      try {
        const data = await getMyReturnRequestsPage({
          page,
          size: PAGE_SIZE,
          status: statusFilter,
        })

        if (isCancelled) {
          return
        }

        setRequests(data.items)
        setTotalCount(data.totalCount)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(
          getErrorMessage(currentError, t('returnRequests.errors.load')),
        )
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadRequests()

    return () => {
      isCancelled = true
    }
  }, [page, statusFilter, t])

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
  }

  function handleStatusFilterChange(event: ChangeEvent<HTMLSelectElement>) {
    setStatusFilter(event.currentTarget.value as ReturnRequestStatusFilter)
    setPage(0)
  }

  return {
    requests,
    page,
    pageSize: PAGE_SIZE,
    totalCount,
    statusFilter,
    isLoading,
    error,
    handlePageChange,
    handleStatusFilterChange,
  }
}

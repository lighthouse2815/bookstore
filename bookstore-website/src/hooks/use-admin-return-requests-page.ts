import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  approveReturnRequest,
  getAdminReturnRequest,
  getAdminReturnRequestsPage,
  rejectReturnRequest,
} from '@/services/return-request-service'
import type {
  ReturnRequestResponse,
  ReturnRequestStatus,
} from '@/types/return-request'
import { getErrorMessage } from '@/utils'

type ReturnRequestStatusFilter = 'ALL' | ReturnRequestStatus

const PAGE_SIZE = 10

export const adminReturnRequestStatusOptions: ReturnRequestStatus[] = [
  'PENDING',
  'APPROVED',
  'REJECTED',
  'CANCELLED',
]

export function useAdminReturnRequestsPage() {
  const { t } = useLanguage()
  const [requests, setRequests] = useState<ReturnRequestResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] =
    useState<ReturnRequestStatusFilter>('ALL')
  const [selectedRequestId, setSelectedRequestId] = useState<string | null>(
    null,
  )
  const [selectedRequest, setSelectedRequest] =
    useState<ReturnRequestResponse | null>(null)
  const [approveNote, setApproveNote] = useState('')
  const [approveAmount, setApproveAmount] = useState('')
  const [approveRestock, setApproveRestock] = useState(true)
  const [rejectNote, setRejectNote] = useState('')
  const [isApproveDialogOpen, setIsApproveDialogOpen] = useState(false)
  const [isRejectDialogOpen, setIsRejectDialogOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const filteredRequests = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    return requests.filter((request) => {
      if (keyword === '') {
        return true
      }

      return [
        request.orderCode,
        request.username ?? '',
        request.userEmail ?? '',
        request.receiverName ?? '',
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword)
    })
  }, [requests, searchTerm])

  useEffect(() => {
    let isCancelled = false
    setIsLoading(true)

    async function loadRequests() {
      try {
        const data = await getAdminReturnRequestsPage({
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
          getErrorMessage(currentError, t('admin.returnRequestsPage.loadError')),
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

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function handleStatusFilterChange(event: ChangeEvent<HTMLSelectElement>) {
    setStatusFilter(event.currentTarget.value as ReturnRequestStatusFilter)
    setPage(0)
  }

  function closeDetail() {
    setSelectedRequestId(null)
    setSelectedRequest(null)
    setIsApproveDialogOpen(false)
    setIsRejectDialogOpen(false)
  }

  function openApproveDialog() {
    if (!selectedRequest) {
      return
    }

    setApproveNote(selectedRequest.adminNote ?? '')
    setApproveAmount(
      selectedRequest.requestedRefundAmount == null
        ? ''
        : String(selectedRequest.requestedRefundAmount),
    )
    setApproveRestock(true)
    setIsApproveDialogOpen(true)
  }

  function openRejectDialog() {
    if (!selectedRequest) {
      return
    }

    setRejectNote(selectedRequest.adminNote ?? '')
    setIsRejectDialogOpen(true)
  }

  function closeApproveDialog() {
    if (isSubmitting) {
      return
    }
    setIsApproveDialogOpen(false)
  }

  function closeRejectDialog() {
    if (isSubmitting) {
      return
    }
    setIsRejectDialogOpen(false)
  }

  async function handleViewRequest(requestId: string) {
    if (selectedRequestId === requestId) {
      closeDetail()
      return
    }

    setSelectedRequestId(requestId)
    setIsDetailLoading(true)

    try {
      const detail = await getAdminReturnRequest(requestId)
      setSelectedRequest(detail)
    } catch (currentError) {
      toast.error(
        getErrorMessage(currentError, t('admin.returnRequestsPage.loadError')),
      )
      closeDetail()
    } finally {
      setIsDetailLoading(false)
    }
  }

  async function handleApprove() {
    if (!selectedRequest) {
      return
    }

    setIsSubmitting(true)

    try {
      const updatedRequest = await approveReturnRequest(selectedRequest.id, {
        adminNote: approveNote,
        approvedRefundAmount:
          approveAmount.trim() === '' ? null : Number(approveAmount),
        restock: approveRestock,
      })

      setRequests((currentRequests) =>
        currentRequests.map((request) =>
          request.id === updatedRequest.id ? updatedRequest : request,
        ),
      )
      setSelectedRequest(updatedRequest)
      setIsApproveDialogOpen(false)
      toast.success(t('admin.returnRequestsPage.approveSuccess'))
    } catch (currentError) {
      toast.error(
        getErrorMessage(
          currentError,
          t('admin.returnRequestsPage.approveError'),
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleReject() {
    if (!selectedRequest) {
      return
    }

    setIsSubmitting(true)

    try {
      const updatedRequest = await rejectReturnRequest(selectedRequest.id, {
        adminNote: rejectNote,
      })

      setRequests((currentRequests) =>
        currentRequests.map((request) =>
          request.id === updatedRequest.id ? updatedRequest : request,
        ),
      )
      setSelectedRequest(updatedRequest)
      setIsRejectDialogOpen(false)
      toast.success(t('admin.returnRequestsPage.rejectSuccess'))
    } catch (currentError) {
      toast.error(
        getErrorMessage(
          currentError,
          t('admin.returnRequestsPage.rejectError'),
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return {
    requests,
    filteredRequests,
    page,
    pageSize: PAGE_SIZE,
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
  }
}

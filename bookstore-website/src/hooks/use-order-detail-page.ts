import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { useOrderResource } from '@/hooks/use-order-data'
import {
  cancelReturnRequest,
  createReturnRequest,
  getMyReturnRequestsPage,
} from '@/services/return-request-service'
import { cancelMyOrder } from '@/services/order-service'
import type { ReturnRequestResponse } from '@/types/return-request'
import { getErrorMessage } from '@/utils'

export function useOrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { t } = useLanguage()
  const orderResource = useOrderResource(id, {
    missingError: t('notFound.description'),
  })
  const [latestReturnRequest, setLatestReturnRequest] =
    useState<ReturnRequestResponse | null>(null)
  const [isReturnLoading, setIsReturnLoading] = useState(Boolean(id))
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [returnReason, setReturnReason] = useState('')
  const [requestedRefundAmount, setRequestedRefundAmount] = useState('')
  const [isSubmittingReturnRequest, setIsSubmittingReturnRequest] =
    useState(false)
  const [isCancellingReturnRequest, setIsCancellingReturnRequest] =
    useState(false)
  const [isCancelOrderDialogOpen, setIsCancelOrderDialogOpen] =
    useState(false)
  const [cancelOrderReason, setCancelOrderReason] = useState('')
  const [isCancellingOrder, setIsCancellingOrder] = useState(false)

  useEffect(() => {
    if (!id) {
      setLatestReturnRequest(null)
      setIsReturnLoading(false)
      return
    }

    let isCancelled = false
    setIsReturnLoading(true)

    async function loadReturnRequests() {
      try {
        const data = await getMyReturnRequestsPage({
          page: 0,
          size: 10,
          orderId: id,
        })

        if (isCancelled) {
          return
        }

        setLatestReturnRequest(data.items[0] ?? null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        toast.error(
          getErrorMessage(currentError, t('returnRequests.errors.load')),
        )
      } finally {
        if (!isCancelled) {
          setIsReturnLoading(false)
        }
      }
    }

    void loadReturnRequests()

    return () => {
      isCancelled = true
    }
  }, [id, t])

  const canCreateReturnRequest =
    orderResource.order?.status === 'DELIVERED' &&
    (latestReturnRequest == null ||
      latestReturnRequest.status === 'REJECTED' ||
      latestReturnRequest.status === 'CANCELLED')

  const canCancelReturnRequest = latestReturnRequest?.status === 'PENDING'
  const canCancelOrder =
    orderResource.order?.status === 'PENDING' &&
    (orderResource.order.paymentStatus === 'PENDING' ||
      orderResource.order.paymentStatus === 'UNPAID')

  function openCreateDialog() {
    setIsCreateDialogOpen(true)
  }

  function closeCreateDialog() {
    if (isSubmittingReturnRequest) {
      return
    }

    setIsCreateDialogOpen(false)
  }

  async function handleSubmitReturnRequest() {
    if (!id) {
      return
    }

    setIsSubmittingReturnRequest(true)

    try {
      const createdRequest = await createReturnRequest(id, {
        reason: returnReason,
        requestedRefundAmount:
          requestedRefundAmount.trim() === ''
            ? null
            : Number(requestedRefundAmount),
      })

      setLatestReturnRequest(createdRequest)
      setReturnReason('')
      setRequestedRefundAmount('')
      setIsCreateDialogOpen(false)
      toast.success(t('returnRequests.createSuccess'))
    } catch (currentError) {
      toast.error(
        getErrorMessage(currentError, t('returnRequests.errors.create')),
      )
    } finally {
      setIsSubmittingReturnRequest(false)
    }
  }

  async function handleCancelReturnRequest() {
    if (!latestReturnRequest || latestReturnRequest.status !== 'PENDING') {
      return
    }

    setIsCancellingReturnRequest(true)

    try {
      const cancelledRequest = await cancelReturnRequest(latestReturnRequest.id)
      setLatestReturnRequest(cancelledRequest)
      toast.success(t('returnRequests.cancelSuccess'))
    } catch (currentError) {
      toast.error(
        getErrorMessage(currentError, t('returnRequests.errors.cancel')),
      )
    } finally {
      setIsCancellingReturnRequest(false)
    }
  }

  function openCancelOrderDialog() {
    setIsCancelOrderDialogOpen(true)
  }

  function closeCancelOrderDialog() {
    if (!isCancellingOrder) {
      setIsCancelOrderDialogOpen(false)
    }
  }

  async function handleCancelOrder() {
    if (!id || !canCancelOrder || isCancellingOrder) {
      return
    }

    setIsCancellingOrder(true)
    try {
      await cancelMyOrder(id, { reason: cancelOrderReason })
      setCancelOrderReason('')
      setIsCancelOrderDialogOpen(false)
      orderResource.refresh()
      toast.success('Đã hủy đơn hàng.')
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, 'Không thể hủy đơn hàng.'))
    } finally {
      setIsCancellingOrder(false)
    }
  }

  return {
    ...orderResource,
    latestReturnRequest,
    isReturnLoading,
    canCreateReturnRequest,
    canCancelReturnRequest,
    canCancelOrder,
    isCreateDialogOpen,
    returnReason,
    requestedRefundAmount,
    isSubmittingReturnRequest,
    isCancellingReturnRequest,
    isCancelOrderDialogOpen,
    cancelOrderReason,
    isCancellingOrder,
    openCreateDialog,
    closeCreateDialog,
    handleSubmitReturnRequest,
    handleCancelReturnRequest,
    openCancelOrderDialog,
    closeCancelOrderDialog,
    handleCancelOrder,
    setReturnReason,
    setRequestedRefundAmount,
    setCancelOrderReason,
  }
}

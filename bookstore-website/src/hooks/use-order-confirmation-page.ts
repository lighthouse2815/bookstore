import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useLanguage } from '@/contexts/language-context'
import { getOrderById } from '@/services/order-service'
import type { OrderResponse, PaymentStatus } from '@/types/order'
import { getErrorMessage } from '@/utils'

const PAYMENT_POLLING_INTERVAL_MS = 4000

export function useOrderConfirmationPage() {
  const { t } = useLanguage()
  const [searchParams] = useSearchParams()
  const orderId = searchParams.get('orderId')
  const initialOrderCode = searchParams.get('orderCode')?.trim() || ''
  const initialTransferContent = searchParams.get('transferContent')?.trim() || ''
  const initialTotalAmount = useMemo(
    () => parseAmount(searchParams.get('totalAmount')),
    [searchParams],
  )
  const [order, setOrder] = useState<OrderResponse | null>(null)
  const [paymentStatus, setPaymentStatus] = useState<PaymentStatus>('PENDING')
  const [isLoading, setIsLoading] = useState(Boolean(orderId))
  const [isPolling, setIsPolling] = useState(Boolean(orderId))
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!orderId) {
      setOrder(null)
      setPaymentStatus('PENDING')
      setIsLoading(false)
      setIsPolling(false)
      setError(t('notFound.description'))
      return
    }

    let isCancelled = false
    let timeoutId: number | undefined

    async function pollOrderStatus(isInitialRequest = false) {
      if (isInitialRequest && !isCancelled) {
        setIsLoading(true)
      }

      try {
        const data = await getOrderById(orderId)

        if (isCancelled) {
          return
        }

        const nextPaymentStatus = normalizePaymentStatus(data.paymentStatus)
        const shouldContinuePolling = !isTerminalPaymentStatus(nextPaymentStatus)

        setOrder(data)
        setPaymentStatus(nextPaymentStatus)
        setIsPolling(shouldContinuePolling)
        setError(null)

        if (shouldContinuePolling) {
          timeoutId = window.setTimeout(
            () => void pollOrderStatus(),
            PAYMENT_POLLING_INTERVAL_MS,
          )
        }
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, t('checkout.error')))
        setIsPolling(true)
        timeoutId = window.setTimeout(
          () => void pollOrderStatus(),
          PAYMENT_POLLING_INTERVAL_MS,
        )
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void pollOrderStatus(true)

    return () => {
      isCancelled = true

      if (timeoutId !== undefined) {
        window.clearTimeout(timeoutId)
      }
    }
  }, [orderId, t])

  return {
    order,
    orderId,
    orderCode: initialOrderCode || orderId || '',
    transferContent: initialTransferContent || initialOrderCode || orderId || '',
    totalAmount: order?.finalAmount ?? initialTotalAmount ?? 0,
    paymentMethod: order?.paymentMethod ?? 'BANK_TRANSFER_QR',
    paymentStatus,
    isLoading,
    isPolling,
    error,
  }
}

function parseAmount(value: string | null) {
  if (!value) {
    return null
  }

  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : null
}

function normalizePaymentStatus(
  paymentStatus: OrderResponse['paymentStatus'],
): PaymentStatus {
  switch (paymentStatus) {
    case 'PAID':
    case 'FAILED':
    case 'CANCELLED':
      return paymentStatus
    case 'UNPAID':
      return 'PENDING'
    case 'REFUNDED':
      return 'CANCELLED'
    case 'PENDING':
    default:
      return 'PENDING'
  }
}

function isTerminalPaymentStatus(paymentStatus: PaymentStatus) {
  return (
    paymentStatus === 'PAID' ||
    paymentStatus === 'FAILED' ||
    paymentStatus === 'CANCELLED'
  )
}

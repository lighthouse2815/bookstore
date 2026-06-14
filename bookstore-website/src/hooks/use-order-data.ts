import { useEffect, useState } from 'react'
import { useLanguage } from '@/contexts/language-context'
import { getMyOrders, getOrderById } from '@/services/order-service'
import type { OrderResponse } from '@/types/order'
import { getErrorMessage } from '@/utils'

type UseOrderResourceOptions = {
  missingError?: string | null
}

export function useMyOrdersResource() {
  const { t } = useLanguage()
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false

    async function loadOrders() {
      try {
        const data = await getMyOrders()

        if (isCancelled) {
          return
        }

        setOrders(data)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, t('checkout.error')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadOrders()

    return () => {
      isCancelled = true
    }
  }, [t])

  return {
    orders,
    isLoading,
    error,
  }
}

export function useOrderResource(
  orderId: string | null | undefined,
  options: UseOrderResourceOptions = {},
) {
  const { t } = useLanguage()
  const [order, setOrder] = useState<OrderResponse | null>(null)
  const [isLoading, setIsLoading] = useState(Boolean(orderId))
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!orderId) {
      setOrder(null)
      setIsLoading(false)
      setError(options.missingError ?? null)
      return
    }

    const currentOrderId = orderId
    let isCancelled = false
    setIsLoading(true)

    async function loadOrder() {
      try {
        const data = await getOrderById(currentOrderId)

        if (isCancelled) {
          return
        }

        setOrder(data)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, t('checkout.error')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadOrder()

    return () => {
      isCancelled = true
    }
  }, [options.missingError, orderId, t])

  return {
    order,
    isLoading,
    error,
  }
}

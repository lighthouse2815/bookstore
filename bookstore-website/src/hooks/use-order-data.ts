import { useEffect, useState } from 'react'
import { useLanguage } from '@/contexts/language-context'
import { getMyOrdersPage, getOrderById } from '@/services/order-service'
import type { OrderResponse } from '@/types/order'
import { getErrorMessage } from '@/utils'

type UseOrderResourceOptions = {
  missingError?: string | null
}

export function useMyOrdersResource(page = 0, size = 10) {
  const { t } = useLanguage()
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [totalCount, setTotalCount] = useState(0)

  useEffect(() => {
    let isCancelled = false
    setIsLoading(true)

    async function loadOrders() {
      try {
        const data = await getMyOrdersPage({ page, size })

        if (isCancelled) {
          return
        }

        setOrders(data.items)
        setTotalCount(data.totalCount)
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
  }, [page, size, t])

  return {
    orders,
    isLoading,
    error,
    totalCount,
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

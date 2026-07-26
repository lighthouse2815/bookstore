import { useEffect, useState } from 'react'
import { useLanguage } from '@/contexts/language-context'
import {
  getMyOrderTimeline,
  getMyOrdersPage,
  getOrderById,
} from '@/services/order-service'
import type { OrderResponse, OrderTimelineEventResponse } from '@/types/order'
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
  const [timeline, setTimeline] = useState<OrderTimelineEventResponse[]>([])
  const [isLoading, setIsLoading] = useState(Boolean(orderId))
  const [error, setError] = useState<string | null>(null)
  const [refreshVersion, setRefreshVersion] = useState(0)

  useEffect(() => {
    if (!orderId) {
      setOrder(null)
      setTimeline([])
      setIsLoading(false)
      setError(options.missingError ?? null)
      return
    }

    const currentOrderId = orderId
    let isCancelled = false
    setIsLoading(true)

    async function loadOrder() {
      try {
        const [data, timelineData] = await Promise.all([
          getOrderById(currentOrderId),
          getMyOrderTimeline(currentOrderId),
        ])

        if (isCancelled) {
          return
        }

        setOrder(data)
        setTimeline(timelineData)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setOrder(null)
        setError(getErrorMessage(currentError, t('checkout.error')))
        setTimeline([])
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
  }, [options.missingError, orderId, refreshVersion, t])

  return {
    order,
    timeline,
    isLoading,
    error,
    refresh: () => setRefreshVersion((current) => current + 1),
  }
}

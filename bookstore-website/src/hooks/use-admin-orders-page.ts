import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  getAdminOrder,
  getAdminOrders,
  updateAdminOrderStatus,
} from '@/services/order-service'
import type { OrderResponse, OrderStatus } from '@/types/order'
import { getErrorMessage } from '@/utils'

type OrderStatusFilter = 'ALL' | OrderStatus

export const adminOrderStatusOptions: OrderStatus[] = [
  'PENDING',
  'CONFIRMED',
  'SHIPPING',
  'DELIVERED',
  'CANCELLED',
]

export function useAdminOrdersPage() {
  const { t } = useLanguage()
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<OrderStatusFilter>('ALL')
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null)
  const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null)
  const [selectedStatus, setSelectedStatus] = useState<OrderStatus>('PENDING')
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [isUpdating, setIsUpdating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const filteredOrders = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    return orders.filter((order) => {
      const matchesKeyword =
        keyword === '' ||
        [
          order.orderId,
          order.receiverName,
          order.receiverPhone,
          order.receiverAddress,
        ]
          .join(' ')
          .toLowerCase()
          .includes(keyword)

      const matchesStatus =
        statusFilter === 'ALL' || order.status === statusFilter

      return matchesKeyword && matchesStatus
    })
  }, [orders, searchTerm, statusFilter])

  useEffect(() => {
    let isCancelled = false

    async function loadOrders() {
      setIsLoading(true)

      try {
        const response = await getAdminOrders()

        if (isCancelled) {
          return
        }

        setOrders(sortOrdersByCreatedAtDesc(response))
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

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function handleStatusFilterChange(event: ChangeEvent<HTMLSelectElement>) {
    setStatusFilter(event.currentTarget.value as OrderStatusFilter)
  }

  function handleSelectedStatusChange(event: ChangeEvent<HTMLSelectElement>) {
    setSelectedStatus(event.currentTarget.value as OrderStatus)
  }

  function handleCloseDetail() {
    setSelectedOrderId(null)
    setSelectedOrder(null)
  }

  async function handleViewOrder(orderId: string) {
    if (selectedOrderId === orderId) {
      handleCloseDetail()
      return
    }

    setSelectedOrderId(orderId)
    setIsDetailLoading(true)

    try {
      const detail = await getAdminOrder(orderId)
      setSelectedOrder(detail)
      setSelectedStatus(detail.status)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
      handleCloseDetail()
    } finally {
      setIsDetailLoading(false)
    }
  }

  async function handleUpdateStatus() {
    if (!selectedOrder) {
      return
    }

    setIsUpdating(true)

    try {
      const updatedOrder = await updateAdminOrderStatus(selectedOrder.orderId, {
        status: selectedStatus,
      })

      toast.success(t('admin.orders.updateSuccess'))
      setSelectedOrder(updatedOrder)
      setOrders((currentOrders) =>
        currentOrders.map((order) =>
          order.orderId === updatedOrder.orderId ? updatedOrder : order,
        ),
      )
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsUpdating(false)
    }
  }

  return {
    orders,
    filteredOrders,
    searchTerm,
    statusFilter,
    selectedOrderId,
    selectedOrder,
    selectedStatus,
    isLoading,
    isDetailLoading,
    isUpdating,
    error,
    handleSearchTermChange,
    handleStatusFilterChange,
    handleSelectedStatusChange,
    handleCloseDetail,
    handleViewOrder,
    handleUpdateStatus,
  }
}

function sortOrdersByCreatedAtDesc(orders: OrderResponse[]) {
  return [...orders].sort(
    (firstOrder, secondOrder) =>
      new Date(secondOrder.createdAt).getTime() -
      new Date(firstOrder.createdAt).getTime(),
  )
}

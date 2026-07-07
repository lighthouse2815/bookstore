import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { getAdminShippers } from '@/services/admin-access-service'
import {
  getAdminOrder,
  getAdminOrdersPage,
  updateAdminOrderStatus,
} from '@/services/order-service'
import {
  assignAdminShipment,
  getAdminShipments,
} from '@/services/shipment-service'
import type { AdminUserResponse } from '@/types/admin-access'
import type { OrderResponse, OrderStatus } from '@/types/order'
import type { ShipmentResponse } from '@/types/shipment'
import { isShipmentActiveStatus } from '@/types/shipment'
import { getErrorMessage } from '@/utils'

type OrderStatusFilter = 'ALL' | OrderStatus

export const adminOrderStatusOptions: OrderStatus[] = [
  'PENDING',
  'CONFIRMED',
  'SHIPPING',
  'DELIVERED',
  'CANCELLED',
]

const PAGE_SIZE = 10

export function useAdminOrdersPage() {
  const { t } = useLanguage()
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [shipments, setShipments] = useState<ShipmentResponse[]>([])
  const [shippers, setShippers] = useState<AdminUserResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<OrderStatusFilter>('ALL')
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null)
  const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null)
  const [selectedStatus, setSelectedStatus] = useState<OrderStatus>('PENDING')
  const [selectedShipperId, setSelectedShipperId] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [isUpdating, setIsUpdating] = useState(false)
  const [isAssigningShipment, setIsAssigningShipment] = useState(false)
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
        const [ordersResponse, shipmentsResponse, shippersResponse] =
          await Promise.all([
            getAdminOrdersPage({ page, size: PAGE_SIZE }),
            getAdminShipments(),
            getAdminShippers(),
          ])

        if (isCancelled) {
          return
        }

        setOrders(sortOrdersByCreatedAtDesc(ordersResponse.items))
        setTotalCount(ordersResponse.totalCount)
        setShipments(sortShipmentsByAssignedAtDesc(shipmentsResponse))
        setShippers(shippersResponse)
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
  }, [page, t])

  useEffect(() => {
    if (
      selectedShipperId !== '' &&
      shippers.some((shipper) => shipper.userId === selectedShipperId)
    ) {
      return
    }

    setSelectedShipperId(shippers[0]?.userId ?? '')
  }, [selectedShipperId, shippers])

  const selectedOrderActiveShipment = useMemo(() => {
    if (!selectedOrder) {
      return null
    }

    return (
      shipments.find(
        (shipment) =>
          shipment.orderId === selectedOrder.orderId &&
          isShipmentActiveStatus(shipment.shipmentStatus),
      ) ?? null
    )
  }, [selectedOrder, shipments])

  const selectedOrderLatestShipment = useMemo(() => {
    if (!selectedOrder) {
      return null
    }

    return (
      shipments.find((shipment) => shipment.orderId === selectedOrder.orderId) ?? null
    )
  }, [selectedOrder, shipments])

  const selectedOrderShipmentShipper = useMemo(() => {
    const targetShipment = selectedOrderActiveShipment ?? selectedOrderLatestShipment

    if (!targetShipment) {
      return null
    }

    return (
      shippers.find((shipper) => shipper.userId === targetShipment.shipperId) ?? null
    )
  }, [selectedOrderActiveShipment, selectedOrderLatestShipment, shippers])

  const canAssignShipment = Boolean(
    selectedOrder &&
      (selectedOrder.status === 'CONFIRMED' ||
        selectedOrder.status === 'SHIPPING') &&
      !selectedOrderActiveShipment,
  )

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
    setPage(0)
  }

  function handleStatusFilterChange(event: ChangeEvent<HTMLSelectElement>) {
    setStatusFilter(event.currentTarget.value as OrderStatusFilter)
    setPage(0)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
  }

  function handleSelectedStatusChange(event: ChangeEvent<HTMLSelectElement>) {
    setSelectedStatus(event.currentTarget.value as OrderStatus)
  }

  function handleSelectedShipperChange(event: ChangeEvent<HTMLSelectElement>) {
    setSelectedShipperId(event.currentTarget.value)
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

  async function handleAssignShipment() {
    if (!selectedOrder) {
      return
    }

    if (!canAssignShipment) {
      toast.error(t('admin.orders.shipmentAssignment.ineligible'))
      return
    }

    if (selectedShipperId === '') {
      toast.error(t('admin.orders.shipmentAssignment.chooseShipperError'))
      return
    }

    setIsAssigningShipment(true)

    try {
      const assignedShipment = await assignAdminShipment({
        orderId: selectedOrder.orderId,
        shipperId: selectedShipperId,
      })

      toast.success(t('admin.orders.shipmentAssignment.assignSuccess'))
      setShipments((currentShipments) =>
        sortShipmentsByAssignedAtDesc([assignedShipment, ...currentShipments]),
      )
      setSelectedOrder((currentOrder) =>
        currentOrder
          ? { ...currentOrder, status: assignedShipment.orderStatus }
          : currentOrder,
      )
      setOrders((currentOrders) =>
        currentOrders.map((order) =>
          order.orderId === assignedShipment.orderId
            ? { ...order, status: assignedShipment.orderStatus }
            : order,
        ),
      )
    } catch (currentError) {
      toast.error(
        getErrorMessage(
          currentError,
          t('admin.orders.shipmentAssignment.assignError'),
        ),
      )
    } finally {
      setIsAssigningShipment(false)
    }
  }

  return {
    orders,
    page,
    pageSize: PAGE_SIZE,
    totalCount,
    shipments,
    shippers,
    filteredOrders,
    searchTerm,
    statusFilter,
    selectedOrderId,
    selectedOrder,
    selectedStatus,
    selectedShipperId,
    selectedOrderActiveShipment,
    selectedOrderLatestShipment,
    selectedOrderShipmentShipper,
    canAssignShipment,
    isLoading,
    isDetailLoading,
    isUpdating,
    isAssigningShipment,
    error,
    handleSearchTermChange,
    handleStatusFilterChange,
    handlePageChange,
    handleSelectedStatusChange,
    handleSelectedShipperChange,
    handleCloseDetail,
    handleViewOrder,
    handleUpdateStatus,
    handleAssignShipment,
  }
}

function sortOrdersByCreatedAtDesc(orders: OrderResponse[]) {
  return [...orders].sort(
    (firstOrder, secondOrder) =>
      new Date(secondOrder.createdAt).getTime() -
      new Date(firstOrder.createdAt).getTime(),
  )
}

function sortShipmentsByAssignedAtDesc(shipments: ShipmentResponse[]) {
  return [...shipments].sort(
    (firstShipment, secondShipment) =>
      new Date(secondShipment.assignedAt).getTime() -
      new Date(firstShipment.assignedAt).getTime(),
  )
}

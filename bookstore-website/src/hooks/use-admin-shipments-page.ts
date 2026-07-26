import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { getAdminShippers } from '@/services/admin-access-service'
import { getAdminOrders } from '@/services/order-service'
import {
  assignAdminShipment,
  confirmAdminShipmentDelivered,
  getAdminShipment,
  getAdminShipmentsPage,
} from '@/services/shipment-service'
import type { AdminUserResponse } from '@/types/admin-access'
import type { OrderResponse, OrderStatus } from '@/types/order'
import type {
  ShipmentFilter,
  ShipmentResponse,
  ShipmentStatus,
} from '@/types/shipment'
import { isShipmentActiveStatus } from '@/types/shipment'
import { getErrorMessage } from '@/utils'

const assignableOrderStatuses: OrderStatus[] = ['CONFIRMED', 'SHIPPING']

export const adminShipmentStatusOptions: ShipmentStatus[] = [
  'ASSIGNED',
  'PICKED_UP',
  'DELIVERING',
  'DELIVERED',
  'FAILED',
]

const PAGE_SIZE = 10

export function useAdminShipmentsPage() {
  const { t } = useLanguage()
  const [shipments, setShipments] = useState<ShipmentResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [shippers, setShippers] = useState<AdminUserResponse[]>([])
  const [statusFilter, setStatusFilter] = useState<ShipmentFilter>('ALL')
  const [selectedShipmentId, setSelectedShipmentId] = useState<string | null>(null)
  const [selectedShipment, setSelectedShipment] = useState<ShipmentResponse | null>(
    null,
  )
  const [selectedAssignableOrderId, setSelectedAssignableOrderId] = useState('')
  const [selectedShipperId, setSelectedShipperId] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [isAssigning, setIsAssigning] = useState(false)
  const [isConfirming, setIsConfirming] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const filteredShipments = useMemo(() => {
    if (statusFilter === 'ALL') {
      return shipments
    }

    return shipments.filter(
      (shipment) => shipment.shipmentStatus === statusFilter,
    )
  }, [shipments, statusFilter])

  const assignableOrders = useMemo(
    () =>
      orders
        .filter(
          (order) =>
            assignableOrderStatuses.includes(order.status) &&
            !hasActiveShipmentForOrder(shipments, order.orderId),
        )
        .sort(
          (firstOrder, secondOrder) =>
            new Date(secondOrder.createdAt).getTime() -
            new Date(firstOrder.createdAt).getTime(),
        ),
    [orders, shipments],
  )

  const selectedShipmentShipper = useMemo(
    () =>
      selectedShipment
        ? shippers.find((shipper) => shipper.userId === selectedShipment.shipperId) ??
          null
        : null,
    [selectedShipment, shippers],
  )

  useEffect(() => {
    let isCancelled = false

    async function loadData() {
      setIsLoading(true)

      try {
        const [nextShipments, nextOrders, nextShippers] = await Promise.all([
          getAdminShipmentsPage({ page, size: PAGE_SIZE }),
          getAdminOrders(),
          getAdminShippers(),
        ])

        if (isCancelled) {
          return
        }

        setShipments(sortShipmentsByAssignedAtDesc(nextShipments.items))
        setTotalCount(nextShipments.totalCount)
        setOrders(nextOrders)
        setShippers(nextShippers)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, t('admin.shipmentsPage.loadError')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadData()

    return () => {
      isCancelled = true
    }
  }, [page, t])

  useEffect(() => {
    if (
      selectedAssignableOrderId !== '' &&
      assignableOrders.some((order) => order.orderId === selectedAssignableOrderId)
    ) {
      return
    }

    setSelectedAssignableOrderId(assignableOrders[0]?.orderId ?? '')
  }, [assignableOrders, selectedAssignableOrderId])

  useEffect(() => {
    if (
      selectedShipperId !== '' &&
      shippers.some((shipper) => shipper.userId === selectedShipperId)
    ) {
      return
    }

    setSelectedShipperId(shippers[0]?.userId ?? '')
  }, [selectedShipperId, shippers])

  function handleStatusFilterChange(event: ChangeEvent<HTMLSelectElement>) {
    setStatusFilter(event.currentTarget.value as ShipmentFilter)
    setPage(0)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
  }

  function handleSelectedOrderChange(event: ChangeEvent<HTMLSelectElement>) {
    setSelectedAssignableOrderId(event.currentTarget.value)
  }

  function handleSelectedShipperChange(event: ChangeEvent<HTMLSelectElement>) {
    setSelectedShipperId(event.currentTarget.value)
  }

  function handleCloseDetail() {
    setSelectedShipmentId(null)
    setSelectedShipment(null)
  }

  async function handleViewShipment(shipmentId: string) {
    if (selectedShipmentId === shipmentId) {
      handleCloseDetail()
      return
    }

    setSelectedShipmentId(shipmentId)
    setIsDetailLoading(true)

    try {
      const detail = await getAdminShipment(shipmentId)
      setSelectedShipment(detail)
    } catch (currentError) {
      toast.error(
        getErrorMessage(currentError, t('admin.shipmentsPage.detailError')),
      )
      handleCloseDetail()
    } finally {
      setIsDetailLoading(false)
    }
  }

  async function handleAssignShipment() {
    if (selectedAssignableOrderId === '' || selectedShipperId === '') {
      toast.error(t('admin.shipmentsPage.assignValidationError'))
      return
    }

    setIsAssigning(true)

    try {
      const assignedShipment = await assignAdminShipment({
        orderId: selectedAssignableOrderId,
        shipperId: selectedShipperId,
      })

      const [nextShipments, nextOrders] = await Promise.all([
        getAdminShipmentsPage({ page, size: PAGE_SIZE }),
        getAdminOrders(),
      ])

      setShipments(sortShipmentsByAssignedAtDesc(nextShipments.items))
      setTotalCount(nextShipments.totalCount)
      setOrders(nextOrders)
      setSelectedShipmentId(assignedShipment.shipmentId)
      setSelectedShipment(assignedShipment)
      toast.success(t('admin.shipmentsPage.assignSuccess'))
    } catch (currentError) {
      toast.error(
        getErrorMessage(currentError, t('admin.shipmentsPage.assignError')),
      )
    } finally {
      setIsAssigning(false)
    }
  }

  async function handleConfirmDelivered() {
    if (!selectedShipment) {
      return
    }

    if (selectedShipment.shipmentStatus !== 'DELIVERING') {
      toast.error(t('admin.shipmentsPage.invalidConfirmState'))
      return
    }

    setIsConfirming(true)

    try {
      const updatedShipment = await confirmAdminShipmentDelivered(
        selectedShipment.shipmentId,
      )
      const [nextShipments, nextOrders] = await Promise.all([
        getAdminShipmentsPage({ page, size: PAGE_SIZE }),
        getAdminOrders(),
      ])

      setShipments(sortShipmentsByAssignedAtDesc(nextShipments.items))
      setTotalCount(nextShipments.totalCount)
      setOrders(nextOrders)
      setSelectedShipment(updatedShipment)
      toast.success(t('admin.shipmentsPage.confirmSuccess'))
    } catch (currentError) {
      toast.error(
        getErrorMessage(currentError, t('admin.shipmentsPage.confirmError')),
      )
    } finally {
      setIsConfirming(false)
    }
  }

  return {
    shipments,
    page,
    pageSize: PAGE_SIZE,
    totalCount,
    filteredShipments,
    shippers,
    assignableOrders,
    statusFilter,
    selectedShipmentId,
    selectedShipment,
    selectedShipmentShipper,
    selectedAssignableOrderId,
    selectedShipperId,
    isLoading,
    isDetailLoading,
    isAssigning,
    isConfirming,
    error,
    handleStatusFilterChange,
    handlePageChange,
    handleSelectedOrderChange,
    handleSelectedShipperChange,
    handleCloseDetail,
    handleViewShipment,
    handleAssignShipment,
    handleConfirmDelivered,
  }
}

function hasActiveShipmentForOrder(
  shipments: ShipmentResponse[],
  orderId: string,
) {
  return shipments.some(
    (shipment) =>
      shipment.orderId === orderId &&
      isShipmentActiveStatus(shipment.shipmentStatus),
  )
}

function sortShipmentsByAssignedAtDesc(shipments: ShipmentResponse[]) {
  return [...shipments].sort(
    (firstShipment, secondShipment) =>
      new Date(secondShipment.assignedAt).getTime() -
      new Date(firstShipment.assignedAt).getTime(),
  )
}

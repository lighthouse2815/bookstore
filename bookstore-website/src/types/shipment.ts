import type {
  OrderPaymentMethod,
  OrderPaymentStatus,
  OrderStatus,
} from '@/types/order'

export type ShipmentStatus =
  | 'ASSIGNED'
  | 'PICKED_UP'
  | 'DELIVERING'
  | 'DELIVERED'
  | 'FAILED'

export type ShipmentFilter = 'ALL' | ShipmentStatus

export type ShipmentResponse = {
  shipmentId: string
  orderId: string
  orderCode: string
  shipperId: string
  paymentMethod: OrderPaymentMethod
  paymentStatus: OrderPaymentStatus
  orderStatus: OrderStatus
  shipmentStatus: ShipmentStatus
  totalAmount: number
  finalAmount: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  failureReason: string | null
  assignedAt: string
  updatedAt: string
  pickedUpAt: string | null
  deliveringAt: string | null
  deliveredAt: string | null
  failedAt: string | null
}

export type AssignShipmentRequest = {
  orderId: string
  shipperId: string
}

export type UpdateShipmentStatusRequest = {
  status: ShipmentStatus
  failureReason?: string | null
}

const shipmentTerminalStatusSet = new Set<ShipmentStatus>([
  'DELIVERED',
  'FAILED',
])

export function isShipmentActiveStatus(status: ShipmentStatus) {
  return !shipmentTerminalStatusSet.has(status)
}

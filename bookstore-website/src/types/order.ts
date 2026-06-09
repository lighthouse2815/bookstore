// Enum-like types
export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'SHIPPING'
  | 'DELIVERED'
  | 'CANCELLED'

export type PaymentMethod = 'COD' | 'BANK_TRANSFER' | 'VNPAY' | 'MOMO'

export type PaymentStatus = 'UNPAID' | 'PAID' | 'FAILED' | 'REFUNDED'

// Request types
export type CheckoutRequest = {
  addressId: string
  couponCode?: string | null
}

export type UpdateOrderStatusRequest = {
  status: OrderStatus
}

// Response types
export type OrderItemResponse = {
  id: string
  bookId: string
  bookTitle: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export type OrderResponse = {
  orderId: string
  userId: string
  items: OrderItemResponse[]
  totalAmount: number
  discountAmount: number
  shippingFee: number
  finalAmount: number
  couponId: string | null
  couponCode: string | null
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  status: OrderStatus
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  createdAt: string
  updatedAt: string
  cancelledAt: string | null
}

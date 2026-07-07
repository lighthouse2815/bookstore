export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'SHIPPING'
  | 'DELIVERED'
  | 'CANCELLED'

export type PaymentMethod = 'BANK_TRANSFER_QR' | 'COD' 

export type OrderPaymentMethod =
  | PaymentMethod
  | 'CASH'
  | 'BANK_TRANSFER'
  | 'VNPAY'
  | 'MOMO'

export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED'

export type OrderPaymentStatus = PaymentStatus | 'UNPAID' | 'REFUNDED'

export type ShippingMethod = 'DELIVERY' | 'PICKUP'

export type OrderItemType = 'PHYSICAL_BOOK' | 'DIGITAL_ASSET'

export type CreateOrderRequest = {
  cartItemIds: string[]
  addressId: string | null
  shippingMethod: ShippingMethod
  paymentMethod: PaymentMethod
  bookCouponCode?: string | null
  shippingCouponCode?: string | null
  note?: string | null
}

export type CreateOrderResponse = {
  orderId: string
  orderCode: string
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  totalAmount: number
  transferContent: string
}

export type UpdateOrderStatusRequest = {
  status: OrderStatus
}

export type OrderItemResponse = {
  id: string
  itemType: OrderItemType
  bookId: string
  digitalAssetId: string | null
  bookTitle: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export type OrderResponse = {
  orderId: string
  orderCode: string
  userId: string
  items: OrderItemResponse[]
  productTotal: number
  totalAmount: number
  discountAmount: number
  shippingFee: number
  shippingDiscount: number
  couponDiscount: number
  finalAmount: number
  couponId: string | null
  couponCode: string | null
  bookCouponId: string | null
  bookCouponCode: string | null
  shippingCouponId: string | null
  shippingCouponCode: string | null
  paymentMethod: OrderPaymentMethod
  paymentStatus: OrderPaymentStatus
  status: OrderStatus
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  createdAt: string
  updatedAt: string
  cancelledAt: string | null
}

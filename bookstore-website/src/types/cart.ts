import type { DigitalAssetFormat } from './digital-library'
import type { CouponType } from './coupon'
import type { ShippingMethod } from './order'

export type CartItemType = 'PHYSICAL_BOOK' | 'DIGITAL_ASSET'

export type AddCartItemRequest = {
  bookId: string
  quantity: number
}

export type AddDigitalCartItemRequest = {
  digitalAssetId: string
}

export type UpdateCartItemRequest = {
  quantity: number
}

export type CartItemResponse = {
  id: string
  itemType: CartItemType
  bookId: string
  digitalAssetId: string | null
  bookTitle: string
  assetTitle: string | null
  format: DigitalAssetFormat | null
  imageUrl: string | null
  price: number
  quantity: number
  lineTotal: number
}

export type CartResponse = {
  cartId: string
  userId: string
  items: CartItemResponse[]
  totalQuantity: number
  totalAmount: number
}

export type CartItem = {
  id: string
  itemType: CartItemType
  bookId: string
  digitalAssetId: string | null
  title: string
  assetTitle: string | null
  format: DigitalAssetFormat | null
  cover: string | null
  price: number
  qty: number
  lineTotal: number
}

export type GetBestCartCouponParams = {
  itemIds?: string[]
  shippingMethod?: ShippingMethod
}

export type BestCouponSuggestion = {
  available: boolean
  couponCode: string | null
  couponType: CouponType | null
  discountAmount: number
  finalAmountEstimate: number
  label: string | null
  reason: string | null
}

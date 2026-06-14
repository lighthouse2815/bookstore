// Request types
export type AddCartItemRequest = {
  bookId: string
  quantity: number
}

export type UpdateCartItemRequest = {
  quantity: number
}

// Response types
export type CartItemResponse = {
  id: string
  bookId: string
  bookTitle: string
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

// Model types
export type CartItem = {
  id: string
  bookId: string
  title: string
  cover: string | null
  price: number
  qty: number
  lineTotal: number
}

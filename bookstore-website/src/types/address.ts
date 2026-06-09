// Request types
export type CreateUserAddressRequest = {
  receiverName: string
  receiverPhone: string
  receiverAddress: string
}

export type UpdateUserAddressRequest = CreateUserAddressRequest

// Response types
export type UserAddressResponse = {
  id: string
  userId: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  defaultAddress: boolean
  createdAt: string
  updatedAt: string
}

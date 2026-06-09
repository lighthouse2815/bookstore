// Enum-like types
export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

export type UserRole = 'ADMIN' | 'STAFF' | 'USER'

export type UserStatus = 'ACTIVE' | 'INACTIVE'

// Request types
export type LoginRequest = {
  username: string
  password: string
}

export type RefreshTokenRequest = {
  refreshToken: string
}

export type LogoutRequest = {
  refreshToken: string
}

export type UpdateUserRequest = {
  username: string
  phoneNumber: string
  email: string
}

export type RegisterRequest = {
  username: string
  password: string
  phoneNumber: string
  email: string
  firstName: string
  lastName: string
  avatarUrl?: string | null
  gender: Gender
  dateOfBirth: string
}

// Response types
export type LoginResponse = {
  userId: string
  status: UserStatus
  roles: UserRole[]
  accessToken: string
  refreshToken: string
}

export type RegisterResponse = {
  username: string
  createdAt: string
}

export type UserMeResponse = {
  userId: string
  username: string
  email: string
  phoneNumber: string
  status: UserStatus
  locked: boolean
  roles: UserRole[]
  createdAt: string
  updatedAt: string
}

// Model types
export type User = {
  id: string
  username: string
  email: string
  phoneNumber: string
  status: UserStatus
  locked: boolean
  roles: UserRole[]
  role: UserRole
  name: string
  avatar: string
  createdAt: string
  updatedAt: string
}

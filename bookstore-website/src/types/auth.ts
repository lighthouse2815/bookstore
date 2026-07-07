// Enum-like types
export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

export type UserRole = 'ADMIN' | 'STAFF' | 'SHIPPER' | 'USER'

export type UserStatus = 'ACTIVE' | 'INACTIVE'

// Request types
export type LoginRequest = {
  username: string
  password: string
}

export type GoogleLoginRequest = {
  idToken: string
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
  email: string
  password: string
}

export type VerifyOtpRequest = {
  email: string
  otpCode: string
}

export type RequestRegistrationOtpRequest = {
  email: string
}

export type RequestPasswordResetOtpRequest = {
  email: string
}

export type ResetPasswordRequest = {
  resetToken: string
  newPassword: string
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

export type PasswordResetTokenResponse = {
  resetToken: string
  expiresAt: string
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

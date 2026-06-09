import type { Gender } from '@/types/auth'

// Request types
export type UpdateProfileRequest = {
  lastName: string
  firstName: string
  avatarUrl?: string | null
  gender: Gender
  dateOfBirth: string
}

// Response types
export type ProfileResponse = {
  id: string
  userId: string
  lastName: string
  firstName: string
  avatarUrl: string | null
  gender: Gender
  dateOfBirth: string
  createdAt: string
  updatedAt: string
}

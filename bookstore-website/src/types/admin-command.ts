import type { LucideIcon } from 'lucide-react'
import type { UserRole } from '@/types/auth'

export type TranslationFn = (
  key: string,
  params?: Record<string, number | string>,
) => string

export type AdminRouteDefinition = {
  id: string
  labelKey: string
  href: string
  activeHrefs?: string[]
  icon: LucideIcon
  allowedRoles: UserRole[]
  keywords?: string[]
  showInSidebar?: boolean
}

export type AdminRouteItem = AdminRouteDefinition & {
  label: string
  isActive: boolean
}

export type AdminCommandActionId =
  | 'TOGGLE_THEME'
  | 'OPEN_CHAT'
  | 'GO_STOREFRONT'
  | 'LOGOUT'

export type AdminCommandItem = {
  id: string
  kind: 'route' | 'action'
  group: 'navigation' | 'action'
  label: string
  subtitle: string
  icon: LucideIcon
  keywords: string[]
  href?: string
  isActive: boolean
}

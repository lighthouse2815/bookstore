import { Home, LogOut, MessageSquareMore, MoonStar, SunMedium } from 'lucide-react'
import type { Theme } from '@/contexts/theme-context'
import type { AdminCommandItem, TranslationFn } from '@/types/admin-command'
import type { UserRole } from '@/types/auth'
import { getAdminPaletteRoutes } from '@/utils/admin-route-registry'

type BuildAdminCommandItemsOptions = {
  pathname: string
  roles: UserRole[]
  t: TranslationFn
  theme: Theme
}

export function buildAdminCommandItems({
  pathname,
  roles,
  t,
  theme,
}: BuildAdminCommandItemsOptions): AdminCommandItem[] {
  const routeItems = getAdminPaletteRoutes({ pathname, roles, t }).map((route) => ({
    id: route.id,
    kind: 'route' as const,
    group: 'navigation' as const,
    label: route.label,
    subtitle: t('admin.commandPalette.routeSubtitle'),
    icon: route.icon,
    keywords: [route.label, route.href, ...(route.keywords ?? [])],
    href: route.href,
    isActive: route.isActive,
  }))

  const actionItems: AdminCommandItem[] = [
    {
      id: 'TOGGLE_THEME',
      kind: 'action',
      group: 'action',
      label:
        theme === 'dark'
          ? t('admin.commandPalette.actions.switchToLight')
          : t('admin.commandPalette.actions.switchToDark'),
      subtitle: t('admin.commandPalette.subtitles.toggleTheme'),
      icon: theme === 'dark' ? SunMedium : MoonStar,
      keywords: ['theme', 'dark', 'light', 'giao dien', 'che do toi'],
      isActive: false,
    },
    {
      id: 'OPEN_CHAT',
      kind: 'action',
      group: 'action',
      label: t('admin.commandPalette.actions.openChat'),
      subtitle: t('admin.commandPalette.subtitles.openChat'),
      icon: MessageSquareMore,
      keywords: ['chat', 'support', 'ho tro', 'messages'],
      href: '/admin/chat',
      isActive: pathname === '/admin/chat',
    },
    {
      id: 'GO_STOREFRONT',
      kind: 'action',
      group: 'action',
      label: t('admin.commandPalette.actions.goStorefront'),
      subtitle: t('admin.commandPalette.subtitles.goStorefront'),
      icon: Home,
      keywords: ['storefront', 'home', 'shop', 'trang chu', 'cua hang'],
      href: '/',
      isActive: false,
    },
    {
      id: 'LOGOUT',
      kind: 'action',
      group: 'action',
      label: t('admin.commandPalette.actions.logout'),
      subtitle: t('admin.commandPalette.subtitles.logout'),
      icon: LogOut,
      keywords: ['logout', 'sign out', 'dang xuat'],
      isActive: false,
    },
  ]

  return [...routeItems, ...actionItems]
}

export function filterAdminCommandItems(
  commands: AdminCommandItem[],
  query: string,
) {
  const normalizedQuery = normalizeSearchText(query)
  if (normalizedQuery === '') {
    return [...commands].sort((first, second) => compareMatches(first, second, 0, 0))
  }

  return commands
    .map((command) => ({
      command,
      score: getSearchScore(command, normalizedQuery),
    }))
    .filter((entry) => entry.score >= 0)
    .sort((first, second) =>
      compareMatches(first.command, second.command, first.score, second.score),
    )
    .map((entry) => entry.command)
}

function getSearchScore(command: AdminCommandItem, query: string) {
  const tokens = query.split(' ').filter(Boolean)
  const searchableValues = [
    command.label,
    command.subtitle,
    command.href ?? '',
    ...command.keywords,
  ].map(normalizeSearchText)

  let bestScore = -1

  for (const value of searchableValues) {
    const score = scoreSearchValue(value, query, tokens)
    if (score > bestScore) {
      bestScore = score
    }
  }

  if (bestScore < 0) {
    return -1
  }

  return bestScore + getBasePriority(command)
}

function scoreSearchValue(value: string, query: string, tokens: string[]) {
  if (value === '') {
    return -1
  }

  if (value === query) {
    return 120
  }

  if (value.startsWith(query)) {
    return 105
  }

  const wholeQueryIndex = value.indexOf(query)
  if (wholeQueryIndex >= 0) {
    return Math.max(85 - wholeQueryIndex, 55)
  }

  if (tokens.length > 0 && tokens.every((token) => value.includes(token))) {
    const totalIndex = tokens.reduce(
      (sum, token) => sum + Math.max(value.indexOf(token), 0),
      0,
    )
    return Math.max(72 - totalIndex, 42)
  }

  return -1
}

function compareMatches(
  first: AdminCommandItem,
  second: AdminCommandItem,
  firstScore: number,
  secondScore: number,
) {
  if (secondScore !== firstScore) {
    return secondScore - firstScore
  }

  const firstPriority = getBasePriority(first)
  const secondPriority = getBasePriority(second)
  if (secondPriority !== firstPriority) {
    return secondPriority - firstPriority
  }

  return first.label.localeCompare(second.label)
}

function getBasePriority(command: AdminCommandItem) {
  let priority = command.kind === 'route' ? 20 : 0

  if (command.isActive) {
    priority += 5
  }

  return priority
}

function normalizeSearchText(value: string) {
  return value
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .replace(/\u0111/g, 'd')
    .replace(/\u0110/g, 'D')
    .toLowerCase()
    .trim()
}

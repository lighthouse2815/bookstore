import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { Theme } from '@/contexts/theme-context'
import type { TranslationFn } from '@/types/admin-command'
import type { UserRole } from '@/types/auth'
import {
  buildAdminCommandItems,
  filterAdminCommandItems,
  getRecentAdminRouteHrefs,
  saveRecentAdminRoute,
} from './admin-command-service'

const dictionary: Record<string, string> = {
  'common.dashboard': 'Dashboard',
  'admin.sidebar.auditLogs': 'Audit logs',
  'admin.sidebar.reports': 'Reports',
  'admin.sidebar.books': 'Manage books',
  'admin.sidebar.digitalAssets': 'Digital assets',
  'admin.sidebar.importReceipts': 'Import receipts',
  'admin.sidebar.inventory': 'Inventory',
  'admin.sidebar.orders': 'Manage orders',
  'admin.sidebar.returnRequests': 'Return requests',
  'admin.sidebar.shipments': 'Shipment management',
  'admin.sidebar.reviews': 'Reviews',
  'admin.sidebar.notifications': 'Notifications',
  'admin.sidebar.chat': 'Support chat',
  'admin.sidebar.categories': 'Manage categories',
  'admin.sidebar.authors': 'Manage authors',
  'admin.sidebar.publishers': 'Manage publishers',
  'admin.sidebar.suppliers': 'Manage suppliers',
  'admin.sidebar.customers': 'Manage customers',
  'admin.sidebar.staff': 'Manage staff',
  'admin.sidebar.roles': 'Manage roles',
  'admin.sidebar.permissions': 'Manage permissions',
  'admin.sidebar.promotions': 'Manage promotions',
  'admin.sidebar.references': 'Reference data',
  'admin.sidebar.settings': 'Account settings',
  'admin.commandPalette.routeSubtitle': 'Open admin screen',
  'admin.commandPalette.recentRouteSubtitle': 'Reopen a recent admin screen',
  'admin.commandPalette.actions.switchToDark': 'Switch to dark theme',
  'admin.commandPalette.actions.switchToLight': 'Switch to light theme',
  'admin.commandPalette.actions.openChat': 'Open support chat',
  'admin.commandPalette.actions.goStorefront': 'Go to storefront',
  'admin.commandPalette.actions.logout': 'Log out',
  'admin.commandPalette.subtitles.toggleTheme':
    'Change the current admin workspace theme',
  'admin.commandPalette.subtitles.openChat':
    'Jump straight to the support chat workspace',
  'admin.commandPalette.subtitles.goStorefront':
    'Leave admin and return to the storefront',
  'admin.commandPalette.subtitles.logout':
    'End the current session and go back to login',
}

const t: TranslationFn = (key) => dictionary[key] ?? key

describe('admin-command-service', () => {
  beforeEach(() => {
    vi.stubGlobal('window', createWindowStorage())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('builds admin routes and shell actions for admin users', () => {
    const commands = buildAdminCommands(['ADMIN'], '/admin/orders')

    expect(commands.map((command) => command.id)).toEqual(
      expect.arrayContaining([
        'dashboard',
        'reports',
        'orders',
        'references',
        'settings',
        'TOGGLE_THEME',
        'OPEN_CHAT',
        'GO_STOREFRONT',
        'LOGOUT',
      ]),
    )
    expect(commands.find((command) => command.id === 'orders')?.isActive).toBe(true)
  })

  it('keeps only route-safe admin commands for staff users', () => {
    const commands = buildAdminCommands(['STAFF'], '/admin/chat')

    expect(
      commands.filter((command) => command.kind === 'route').map((command) => command.id),
    ).toEqual(['audit-logs', 'chat'])
    expect(commands.map((command) => command.id)).not.toContain('books')
  })

  it('prioritizes the closest navigation match for route queries', () => {
    const commands = buildAdminCommands(['ADMIN'], '/admin')
    const filteredCommands = filterAdminCommandItems(commands, 'ship')

    expect(filteredCommands[0]?.id).toBe('shipments')
  })

  it('finds the report center from its route keywords', () => {
    const commands = buildAdminCommands(['ADMIN'], '/admin')

    expect(filterAdminCommandItems(commands, 'reports')[0]?.id).toBe('reports')
  })

  it('matches localized action keywords when searching shell commands', () => {
    const commands = buildAdminCommands(['ADMIN'], '/admin')
    const filteredCommands = filterAdminCommandItems(commands, 'dang xuat')

    expect(filteredCommands[0]?.id).toBe('LOGOUT')
  })

  it('saves recent admin routes without duplicates and limits them to five', () => {
    const routeHrefs = [
      '/admin/books',
      '/admin/orders',
      '/admin/reviews',
      '/admin/audit-logs',
      '/admin/return-requests',
      '/admin/dashboard',
      '/admin/orders',
    ]
    routeHrefs.forEach(saveRecentAdminRoute)

    expect(getRecentAdminRouteHrefs()).toEqual([
      '/admin/orders',
      '/admin',
      '/admin/return-requests',
      '/admin/audit-logs',
      '/admin/reviews',
    ])
  })

  it('returns a safe empty list when browser storage throws', () => {
    vi.stubGlobal('window', {
      localStorage: {
        getItem: () => {
          throw new Error('storage unavailable')
        },
        setItem: () => {
          throw new Error('storage unavailable')
        },
      },
    })

    expect(getRecentAdminRouteHrefs()).toEqual([])
    expect(saveRecentAdminRoute('/admin/orders')).toEqual([])
  })

  it('keeps active and recent route commands ahead of regular navigation', () => {
    const commands = buildAdminCommands(['ADMIN'], '/admin/orders', 'light', [
      '/admin/audit-logs',
      '/admin/orders',
    ])

    expect(filterAdminCommandItems(commands, '').slice(0, 2).map((command) => command.id)).toEqual([
      'recent-orders',
      'recent-audit-logs',
    ])
  })
})

function buildAdminCommands(
  roles: UserRole[],
  pathname: string,
  theme: Theme = 'light',
  recentRouteHrefs: string[] = [],
) {
  return buildAdminCommandItems({
    pathname,
    recentRouteHrefs,
    roles,
    theme,
    t,
  })
}

function createWindowStorage() {
  const values = new Map<string, string>()

  return {
    localStorage: {
      getItem: (key: string) => values.get(key) ?? null,
      setItem: (key: string, value: string) => values.set(key, value),
    },
  }
}

import { describe, expect, it } from 'vitest'
import type { Theme } from '@/contexts/theme-context'
import type { TranslationFn } from '@/types/admin-command'
import type { UserRole } from '@/types/auth'
import {
  buildAdminCommandItems,
  filterAdminCommandItems,
} from './admin-command-service'

const dictionary: Record<string, string> = {
  'common.dashboard': 'Dashboard',
  'admin.sidebar.auditLogs': 'Audit logs',
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
  it('builds admin routes and shell actions for admin users', () => {
    const commands = buildAdminCommands(['ADMIN'], '/admin/orders')

    expect(commands.map((command) => command.id)).toEqual(
      expect.arrayContaining([
        'dashboard',
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

  it('matches localized action keywords when searching shell commands', () => {
    const commands = buildAdminCommands(['ADMIN'], '/admin')
    const filteredCommands = filterAdminCommandItems(commands, 'dang xuat')

    expect(filteredCommands[0]?.id).toBe('LOGOUT')
  })
})

function buildAdminCommands(roles: UserRole[], pathname: string, theme: Theme = 'light') {
  return buildAdminCommandItems({
    pathname,
    roles,
    theme,
    t,
  })
}

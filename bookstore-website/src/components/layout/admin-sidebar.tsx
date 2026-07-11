import { Link, useLocation } from 'react-router-dom'
import { BookOpen, ChevronRight, LogOut, Search, X } from 'lucide-react'
import { Button } from '@/components/common/button'
import { LanguageSwitcher } from '@/components/common/language-switcher'
import { ThemeSwitch } from '@/components/common/theme-switch'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { useTheme } from '@/contexts/theme-context'
import { cn } from '@/utils'
import { getAdminSidebarRoutes } from '@/utils/admin-route-registry'
import type { AdminRouteItem, TranslationFn } from '@/types/admin-command'

type AdminSidebarProps = {
  isOpen: boolean
  onClose: () => void
  onOpenCommandPalette: () => void
}

const SIDEBAR_GROUPS = [
  {
    id: 'overview',
    labelKey: 'admin.sidebar.groups.overview',
    routeIds: ['dashboard', 'reports', 'audit-logs'],
  },
  {
    id: 'catalog',
    labelKey: 'admin.sidebar.groups.catalog',
    routeIds: [
      'books',
      'digital-assets',
      'categories',
      'authors',
      'publishers',
      'suppliers',
    ],
  },
  {
    id: 'operations',
    labelKey: 'admin.sidebar.groups.operations',
    routeIds: [
      'orders',
      'payment-reconciliation',
      'refunds',
      'outbox',
      'return-requests',
      'shipments',
      'import-receipts',
      'inventory',
    ],
  },
  {
    id: 'engagement',
    labelKey: 'admin.sidebar.groups.engagement',
    routeIds: ['reviews', 'notifications', 'chat', 'promotions'],
  },
  {
    id: 'access',
    labelKey: 'admin.sidebar.groups.access',
    routeIds: ['customers', 'staff', 'roles', 'permissions'],
  },
  {
    id: 'system',
    labelKey: 'admin.sidebar.groups.system',
    routeIds: ['settings'],
  },
] as const

export function AdminSidebar({
  isOpen,
  onClose,
  onOpenCommandPalette,
}: AdminSidebarProps) {
  const location = useLocation()
  const { logout, user } = useAuth()
  const { t } = useLanguage()
  const { theme, toggleTheme } = useTheme()
  const menuItems = getAdminSidebarRoutes({
    pathname: location.pathname,
    roles: user?.roles ?? [],
    t,
  })
  const groupedItems = groupAdminMenuItems(menuItems, t)

  return (
    <aside
      className={cn(
        'motion-drawer fixed inset-y-0 left-0 z-40 flex w-[min(20rem,calc(100vw-1rem))] max-w-xs flex-col border-r border-border bg-card/96 backdrop-blur lg:sticky lg:top-0 lg:z-auto lg:h-screen lg:w-72 lg:max-w-none lg:translate-x-0',
        isOpen ? 'translate-x-0' : '-translate-x-full',
      )}
    >
      <div className="flex items-center justify-between gap-3 border-b border-border px-5 py-5">
        <div className="flex min-w-0 items-center gap-3">
          <BookOpen className="h-6 w-6 shrink-0 text-primary" />
          <h1 className="truncate font-heading text-xl font-bold">
            {t('admin.sidebar.title')}
          </h1>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          className="rounded-2xl lg:hidden"
          aria-label={t('admin.sidebar.mobileClose')}
          onClick={onClose}
        >
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div className="space-y-4 border-b border-border px-5 py-4">
        <div>
          <div className="mb-1 truncate text-sm font-semibold text-foreground">
            {user?.name}
          </div>
          <p className="truncate text-xs text-muted-foreground">{user?.email}</p>
        </div>

        <div className="flex items-center justify-between gap-3">
          <LanguageSwitcher />
          <ThemeSwitch
            checked={theme === 'dark'}
            onToggle={toggleTheme}
            label={
              theme === 'dark'
                ? t('header.switchToLight')
                : t('header.switchToDark')
            }
            className="animate-none"
          />
        </div>

        <Button
          type="button"
          variant="outline"
          size="sm"
          className="w-full justify-between rounded-2xl"
          onClick={() => {
            onClose()
            onOpenCommandPalette()
          }}
        >
          <span className="flex items-center gap-2">
            <Search className="h-4 w-4" />
            {t('admin.commandPalette.openButton')}
          </span>
          <span className="rounded-md border border-border/70 bg-background px-1.5 py-0.5 text-[11px] font-semibold text-muted-foreground">
            {t('admin.commandPalette.shortcutBadge')}
          </span>
        </Button>
      </div>

      <nav className="flex-1 overflow-y-auto px-3 py-4">
        {groupedItems.map((group) => (
          <section key={group.id} className="mb-5 last:mb-0">
            <p className="px-3 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">
              {group.label}
            </p>
            <div className="mt-2 space-y-1">
              {group.items.map((item) => {
                const Icon = item.icon

                return (
                  <Link
                    key={item.href}
                    to={item.href}
                    onClick={onClose}
                    className={cn(
                      'group flex items-center justify-between gap-3 rounded-2xl px-3 py-2.5 text-sm font-medium transition-colors',
                      item.isActive
                        ? 'bg-primary text-primary-foreground shadow-sm'
                        : 'text-muted-foreground hover:bg-muted/75 hover:text-foreground',
                    )}
                  >
                    <span className="flex min-w-0 items-center gap-3">
                      <Icon className="h-5 w-5 shrink-0" />
                      <span className="truncate">{item.label}</span>
                    </span>
                    <ChevronRight
                      className={cn(
                        'h-4 w-4 shrink-0 transition-transform',
                        item.isActive
                          ? 'text-primary-foreground/70'
                          : 'text-muted-foreground/55 group-hover:translate-x-0.5 group-hover:text-foreground/70',
                      )}
                    />
                  </Link>
                )
              })}
            </div>
          </section>
        ))}
      </nav>

      <div className="border-t border-border px-3 py-4">
        <Button
          onClick={logout}
          variant="outline"
          className="w-full rounded-2xl"
        >
          <LogOut className="mr-2 h-4 w-4" />
          {t('header.logout')}
        </Button>
      </div>
    </aside>
  )
}

function groupAdminMenuItems(menuItems: AdminRouteItem[], t: TranslationFn) {
  const groupLookup = new Map(
    SIDEBAR_GROUPS.flatMap((group) =>
      group.routeIds.map((routeId) => [routeId, group.id] as const),
    ),
  )
  const itemsByGroup = new Map<string, AdminRouteItem[]>()

  for (const item of menuItems) {
    const groupId = groupLookup.get(item.id) ?? 'system'
    const currentGroupItems = itemsByGroup.get(groupId) ?? []
    currentGroupItems.push(item)
    itemsByGroup.set(groupId, currentGroupItems)
  }

  return SIDEBAR_GROUPS.map((group) => ({
    id: group.id,
    label: t(group.labelKey),
    items: itemsByGroup.get(group.id) ?? [],
  })).filter((group) => group.items.length > 0)
}

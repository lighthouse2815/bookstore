import { Link, useLocation } from 'react-router-dom'
import {
  BarChart3,
  BellRing,
  BookOpen,
  FileText,
  Boxes,
  Building2,
  Key,
  LogOut,
  MessageSquareMore,
  PackagePlus,
  Percent,
  Settings2,
  Shield,
  ShoppingCart,
  Star,
  Tags,
  Truck,
  User,
  Users,
} from 'lucide-react'
import { Button } from '@/components/common/button'
import { LanguageSwitcher } from '@/components/common/language-switcher'
import { ThemeSwitch } from '@/components/common/theme-switch'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { useTheme } from '@/contexts/theme-context'

export function AdminSidebar() {
  const location = useLocation()
  const { logout, user } = useAuth()
  const { t } = useLanguage()
  const { theme, toggleTheme } = useTheme()
  const isAdmin = user?.roles.includes('ADMIN') ?? false

  const menuItems = isAdmin
    ? [
        {
          label: t('common.dashboard'),
          href: '/admin',
          icon: BarChart3,
        },
        {
          label: t('admin.sidebar.books'),
          href: '/admin/books',
          icon: BookOpen,
        },
        {
          label: t('admin.sidebar.digitalAssets'),
          href: '/admin/digital-assets',
          icon: FileText,
        },
        {
          label: t('admin.sidebar.importReceipts'),
          href: '/admin/import-receipts',
          icon: PackagePlus,
        },
        {
          label: t('admin.sidebar.inventory'),
          href: '/admin/inventory',
          icon: Boxes,
        },
        {
          label: t('admin.sidebar.orders'),
          href: '/admin/orders',
          icon: ShoppingCart,
        },
        {
          label: t('admin.sidebar.shipments'),
          href: '/admin/shipments',
          icon: Truck,
        },
        {
          label: t('admin.sidebar.reviews'),
          href: '/admin/reviews',
          icon: Star,
        },
        {
          label: t('admin.sidebar.notifications'),
          href: '/admin/notifications',
          icon: BellRing,
        },
        {
          label: t('admin.sidebar.chat'),
          href: '/admin/chat',
          icon: MessageSquareMore,
        },
        {
          label: t('admin.sidebar.categories'),
          href: '/admin/categories',
          icon: Tags,
        },
        {
          label: t('admin.sidebar.authors'),
          href: '/admin/authors',
          icon: User,
        },
        {
          label: t('admin.sidebar.publishers'),
          href: '/admin/publishers',
          icon: Building2,
        },
        {
          label: t('admin.sidebar.suppliers'),
          href: '/admin/suppliers',
          icon: Truck,
        },
        {
          label: t('admin.sidebar.customers'),
          href: '/admin/customers',
          icon: Users,
        },
        {
          label: t('admin.sidebar.staff'),
          href: '/admin/staff',
          icon: User,
        },
        {
          label: t('admin.sidebar.roles'),
          href: '/admin/roles',
          icon: Shield,
        },
        {
          label: t('admin.sidebar.permissions'),
          href: '/admin/permissions',
          icon: Key,
        },
        {
          label: t('admin.sidebar.promotions'),
          href: '/admin/promotions',
          icon: Percent,
        },
        {
          label: t('admin.sidebar.settings'),
          href: '/admin/settings',
          icon: Settings2,
        },
      ]
    : [
        {
          label: t('admin.sidebar.chat'),
          href: '/admin/chat',
          icon: MessageSquareMore,
        },
      ]

  return (
    <div className="sticky top-0 flex h-screen w-64 shrink-0 flex-col border-r border-border bg-card">
      <div className="flex items-center gap-2 border-b border-border px-6 py-6">
        <BookOpen className="h-6 w-6 text-primary" />
        <h1 className="font-heading text-xl font-bold">
          {t('admin.sidebar.title')}
        </h1>
      </div>

      <div className="space-y-4 border-b border-border px-6 py-4">
        <div>
          <div className="mb-1 text-sm font-semibold text-foreground">
            {user?.name}
          </div>
          <p className="text-xs text-muted-foreground">{user?.email}</p>
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
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
        {menuItems.map((item) => {
          const Icon = item.icon
          const isActive = location.pathname === item.href

          return (
            <Link
              key={item.href}
              to={item.href}
              className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:bg-muted'
              }`}
            >
              <Icon className="h-5 w-5" />
              {item.label}
            </Link>
          )
        })}
      </nav>

      <div className="border-t border-border px-3 py-4">
        {isAdmin ? (
          <Link to="/admin/settings" className="mb-3 block">
            <Button variant="outline" size="sm" className="w-full">
              <Settings2 className="mr-2 h-4 w-4" />
              {t('admin.sidebar.adminAccount')}
            </Button>
          </Link>
        ) : null}
        <Button
          onClick={logout}
          variant="outline"
          size="sm"
          className="w-full"
        >
          <LogOut className="mr-2 h-4 w-4" />
          {t('header.logout')}
        </Button>
      </div>
    </div>
  )
}

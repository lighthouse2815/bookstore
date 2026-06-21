import { Link, useLocation } from 'react-router-dom'
import {
  BarChart3,
  BellRing,
  BookOpen,
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
  const { language, t } = useLanguage()
  const { theme, toggleTheme } = useTheme()
  const isVietnamese = language === 'vi'
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
          label: isVietnamese ? 'Quan ly nhap kho' : 'Import receipts',
          href: '/admin/import-receipts',
          icon: PackagePlus,
        },
        {
          label: isVietnamese ? 'Quan ly ton kho' : 'Inventory',
          href: '/admin/inventory',
          icon: Boxes,
        },
        {
          label: t('admin.sidebar.orders'),
          href: '/admin/orders',
          icon: ShoppingCart,
        },
        {
          label: isVietnamese ? 'Quan ly danh gia' : 'Reviews',
          href: '/admin/reviews',
          icon: Star,
        },
        {
          label: isVietnamese ? 'Quan ly thong bao' : 'Notifications',
          href: '/admin/notifications',
          icon: BellRing,
        },
        {
          label: isVietnamese ? 'Chat ho tro' : 'Support chat',
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
          label: isVietnamese ? 'Quan ly nha cung cap' : 'Manage suppliers',
          href: '/admin/suppliers',
          icon: Truck,
        },
        {
          label: isVietnamese ? 'Quan ly khach hang' : 'Manage customers',
          href: '/admin/customers',
          icon: Users,
        },
        {
          label: isVietnamese ? 'Quan ly nhan vien' : 'Manage staff',
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
          label: isVietnamese ? 'Cai dat tai khoan' : 'Account settings',
          href: '/admin/settings',
          icon: Settings2,
        },
      ]
    : [
        {
          label: isVietnamese ? 'Chat ho tro' : 'Support chat',
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
              {isVietnamese ? 'Tai khoan quan tri' : 'Admin account'}
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

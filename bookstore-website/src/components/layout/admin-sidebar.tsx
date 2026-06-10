import { Link, useLocation } from 'react-router-dom'
import {
  BarChart3,
  Building2,
  BookOpen,
  Key,
  LogOut,
  ShoppingCart,
  Shield,
  Tags,
  User,
  Users,
} from 'lucide-react'
import { Button } from '@/components/common/button'
import { LanguageSwitcher } from '@/components/common/language-switcher'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'

export function AdminSidebar() {
  const location = useLocation()
  const { logout, user } = useAuth()
  const { t } = useLanguage()

  const menuItems = [
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
      label: t('admin.sidebar.orders'),
      href: '/admin/orders',
      icon: ShoppingCart,
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
      label: t('admin.sidebar.users'),
      href: '/admin/users',
      icon: Users,
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
  ]

  return (
    <div className="flex h-screen w-64 flex-col border-r border-border bg-card">
      <div className="flex items-center gap-2 border-b border-border px-6 py-6">
        <BookOpen className="h-6 w-6 text-primary" />
        <h1 className="font-heading text-xl font-bold">
          {t('admin.sidebar.title')}
        </h1>
      </div>

      <div className="space-y-3 border-b border-border px-6 py-4">
        <div>
          <div className="mb-1 text-sm font-semibold text-foreground">
            {user?.name}
          </div>
          <p className="text-xs text-muted-foreground">{user?.email}</p>
        </div>
        <LanguageSwitcher />
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

import { useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import {
  BookOpen,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  ShoppingCart,
  User,
  X,
} from 'lucide-react'
import { ThemeSwitch } from '@/components/common/theme-switch'
import { LanguageSwitcher } from '@/components/common/language-switcher'
import { useAuth } from '@/contexts/auth-context'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import { useTheme } from '@/contexts/theme-context'
import { cn } from '@/utils'

export function Header() {
  const { items } = useCart()
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { t } = useLanguage()
  const brand = t('common.brand')
  const brandPrefix = brand.endsWith('Vui') ? brand.slice(0, -3) : brand
  const location = useLocation()
  const totalItems = items.length
  const [open, setOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const profileRef = useRef<HTMLDivElement>(null)

  const navLinks = [
    { to: '/', label: t('header.nav.home') },
    { to: '/books', label: t('header.nav.books') },
    {
      to: '/books?category=__life-skills__',
      label: t('header.nav.lifeSkills'),
    },
    {
      to: '/books?category=__novel__',
      label: t('header.nav.novel'),
    },
  ]

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        profileRef.current &&
        !profileRef.current.contains(event.target as Node)
      ) {
        setProfileOpen(false)
      }
    }

    if (profileOpen) {
      document.addEventListener('mousedown', handleClickOutside)
      return () => document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [profileOpen])

  return (
    <header className="sticky top-0 z-50 border-b border-border bg-background/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-4 sm:px-6 lg:px-8">
        <Link to="/" className="flex items-center gap-2">
          <span className="flex size-9 items-center justify-center rounded-xl bg-primary text-primary-foreground">
            <BookOpen className="size-5" />
          </span>
          <span className="font-heading text-xl font-bold tracking-tight">
            {brandPrefix}
            {brand.endsWith('Vui') && <span className="text-primary">Vui</span>}
          </span>
        </Link>

        <nav className="hidden items-center gap-1 md:flex">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={cn(
                'rounded-full px-4 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground',
                location.pathname + location.search === link.to &&
                  'text-foreground',
              )}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="flex items-center gap-1">
          <LanguageSwitcher className="hidden sm:inline-flex" />

          <Link
            to="/books"
            className="flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted"
            aria-label={t('header.searchAria')}
          >
            <Search className="size-5" />
          </Link>

          <ThemeSwitch
            checked={theme === 'dark'}
            onToggle={toggleTheme}
            label={
              theme === 'dark'
                ? t('header.switchToLight')
                : t('header.switchToDark')
            }
          />

          {user && (
            <Link
              to="/cart"
              className="relative flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted"
              aria-label={t('header.cartAria')}
            >
              <ShoppingCart className="size-5" />
              {totalItems > 0 && (
                <span className="absolute -right-0.5 -top-0.5 flex min-w-5 items-center justify-center rounded-full bg-primary px-1 text-xs font-bold text-primary-foreground">
                  {totalItems}
                </span>
              )}
            </Link>
          )}

          {!user ? (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="hidden rounded-lg px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-muted sm:inline-block"
              >
                {t('header.login')}
              </Link>
              <Link
                to="/login"
                className="flex size-10 items-center justify-center rounded-full bg-primary text-primary-foreground transition-colors hover:opacity-90 sm:hidden"
                aria-label={t('header.login')}
              >
                <User className="size-5" />
              </Link>
            </div>
          ) : (
            <div className="relative" ref={profileRef}>
              <button
                onClick={() => setProfileOpen((current) => !current)}
                className="flex size-10 items-center justify-center rounded-full bg-primary/10 text-primary transition-colors hover:bg-primary/20"
                aria-label={t('header.profileMenu')}
              >
                <span className="text-lg">{user.avatar}</span>
              </button>

              {profileOpen && (
                <div className="absolute right-0 mt-2 w-48 rounded-lg border border-border bg-background shadow-lg">
                  <div className="border-b border-border px-4 py-3">
                    <p className="text-sm font-semibold text-foreground">
                      {user.name}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {user.email}
                    </p>
                  </div>
                  <nav className="flex flex-col">
                    <Link
                      to="/profile"
                      onClick={() => setProfileOpen(false)}
                      className="flex items-center gap-2 px-4 py-3 text-sm text-foreground transition-colors hover:bg-muted"
                    >
                      <User className="h-4 w-4" />
                      {t('header.myProfile')}
                    </Link>
                    {user.role === 'ADMIN' && (
                      <Link
                        to="/admin"
                        onClick={() => setProfileOpen(false)}
                        className="flex items-center gap-2 px-4 py-3 text-sm text-primary transition-colors hover:bg-muted"
                      >
                        <LayoutDashboard className="h-4 w-4" />
                        {t('header.adminDashboard')}
                      </Link>
                    )}
                    <button
                      onClick={() => {
                        void logout()
                        setProfileOpen(false)
                      }}
                      className="flex items-center gap-2 border-t border-border px-4 py-3 text-sm text-foreground transition-colors hover:bg-muted"
                    >
                      <LogOut className="h-4 w-4" />
                      {t('header.logout')}
                    </button>
                  </nav>
                </div>
              )}
            </div>
          )}

          <button
            type="button"
            onClick={() => setOpen((current) => !current)}
            className="flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted md:hidden"
            aria-label="Menu"
          >
            {open ? <X className="size-5" /> : <Menu className="size-5" />}
          </button>
        </div>
      </div>

      {open && (
        <nav className="border-t border-border bg-background md:hidden">
          <div className="flex flex-col gap-2 px-4 py-3">
            <LanguageSwitcher />
            {navLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setOpen(false)}
                className="rounded-lg px-3 py-3 text-sm font-medium text-foreground transition-colors hover:bg-muted"
              >
                {link.label}
              </Link>
            ))}
            {!user && (
              <Link
                to="/login"
                onClick={() => setOpen(false)}
                className="mt-2 rounded-lg bg-primary px-3 py-3 text-sm font-medium text-primary-foreground transition-colors hover:opacity-90"
              >
                {t('header.login')}
              </Link>
            )}
          </div>
        </nav>
      )}
    </header>
  )
}

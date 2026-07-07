import { Link } from 'react-router-dom'
import {
  BookOpen,
  LayoutDashboard,
  LogOut,
  Menu,
  Package,
  Search,
  ShoppingCart,
  User,
  X,
} from 'lucide-react'
import { ThemeSwitch } from '@/components/common/theme-switch'
import { LanguageSwitcher } from '@/components/common/language-switcher'
import { NotificationBell } from '@/components/layout/notification-bell'
import { useHeaderState } from '@/hooks/use-header-state'
import { cn } from '@/utils'

export function Header() {
  const {
    user,
    theme,
    t,
    brandPrefix,
    brandSuffix,
    totalItems,
    navLinks,
    searchTargetPath,
    open,
    profileOpen,
    searchQuery,
    profileRef,
    toggleTheme,
    isActiveLink,
    toggleMenu,
    closeMenu,
    toggleProfileMenu,
    closeProfileMenu,
    handleLogout,
    handleSearchQueryChange,
    submitSearch,
  } = useHeaderState()
  const canOpenAdminArea =
    Boolean(user) &&
    (user.roles.includes('ADMIN') || user.roles.includes('STAFF'))
  const adminLinkTarget = user?.roles.includes('ADMIN') ? '/admin' : '/admin/chat'

  return (
    <header className="sticky top-0 z-50 border-b border-border/70 bg-background/95 backdrop-blur">
      <div className="mx-auto flex min-h-[78px] max-w-[1380px] items-center gap-4 px-4 sm:px-6 lg:px-8">
        <Link to="/" className="shrink-0">
          <div className="flex items-center gap-3">
            <span className="flex size-10 items-center justify-center rounded-2xl bg-primary text-primary-foreground shadow-sm">
              <BookOpen className="size-5" />
            </span>
            <span className="font-heading text-2xl font-bold tracking-tight text-foreground">
              {brandPrefix}
              {brandSuffix && <span className="text-primary">{brandSuffix}</span>}
            </span>
          </div>
        </Link>

        <nav className="hidden items-center gap-1 lg:flex">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={cn(
                'rounded-full px-4 py-2 text-sm font-medium transition-colors',
                isActiveLink(link.to)
                  ? 'text-primary'
                  : 'text-muted-foreground hover:text-foreground',
              )}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="hidden min-w-0 flex-1 justify-end lg:flex">
          <form
            onSubmit={(event) => {
              event.preventDefault()
              submitSearch()
            }}
            className="relative w-full max-w-[380px] xl:max-w-[440px]"
          >
            <Search className="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <input
              type="search"
              value={searchQuery}
              onChange={handleSearchQueryChange}
              placeholder={t('book.listing.searchPlaceholder')}
              className="h-12 w-full rounded-full border border-border bg-card pl-11 pr-14 text-sm text-foreground outline-none transition-colors placeholder:text-muted-foreground focus:border-primary"
              aria-label={t('header.searchAria')}
            />
            <button
              type="submit"
              className="absolute right-2 top-1/2 flex size-9 -translate-y-1/2 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
              aria-label={t('header.searchAria')}
            >
              <Search className="size-4" />
            </button>
          </form>
        </div>

        <div className="ml-auto flex items-center gap-1.5">
          <LanguageSwitcher className="hidden sm:inline-flex" />

          <Link
            to={searchTargetPath}
            className="flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted lg:hidden"
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

          {user && <NotificationBell />}

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
                className="hidden rounded-full px-4 py-2 text-sm font-semibold text-foreground transition-colors hover:bg-muted sm:inline-flex"
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
                onClick={toggleProfileMenu}
                className="flex size-10 items-center justify-center rounded-full bg-primary/10 text-primary transition-colors hover:bg-primary/20"
                aria-label={t('header.profileMenu')}
              >
                <span className="text-lg">{user.avatar}</span>
              </button>

              {profileOpen && (
                <div className="absolute right-0 mt-2 w-52 overflow-hidden rounded-2xl border border-border bg-background shadow-lg">
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
                      onClick={closeProfileMenu}
                      className="flex items-center gap-2 px-4 py-3 text-sm text-foreground transition-colors hover:bg-muted"
                    >
                      <User className="h-4 w-4" />
                      {t('header.myProfile')}
                    </Link>
                    <Link
                      to="/orders"
                      onClick={closeProfileMenu}
                      className="flex items-center gap-2 px-4 py-3 text-sm text-foreground transition-colors hover:bg-muted"
                    >
                      <Package className="h-4 w-4" />
                      {t('orderDetail.orderHistory')}
                    </Link>
                    <Link
                      to="/library"
                      onClick={closeProfileMenu}
                      className="flex items-center gap-2 px-4 py-3 text-sm text-foreground transition-colors hover:bg-muted"
                    >
                      <BookOpen className="h-4 w-4" />
                      {t('header.myLibrary')}
                    </Link>
                    {canOpenAdminArea && (
                      <Link
                        to={adminLinkTarget}
                        onClick={closeProfileMenu}
                        className="flex items-center gap-2 px-4 py-3 text-sm text-primary transition-colors hover:bg-muted"
                      >
                        <LayoutDashboard className="h-4 w-4" />
                        {t('header.adminDashboard')}
                      </Link>
                    )}
                    <button
                      onClick={() => {
                        void handleLogout()
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
            onClick={toggleMenu}
            className="flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted lg:hidden"
            aria-label="Menu"
          >
            {open ? <X className="size-5" /> : <Menu className="size-5" />}
          </button>
        </div>
      </div>

      {open && (
        <div className="border-t border-border bg-background lg:hidden">
          <div className="mx-auto max-w-[1380px] px-4 py-4 sm:px-6">
            <form
              onSubmit={(event) => {
                event.preventDefault()
                submitSearch()
              }}
              className="relative mb-4"
            >
              <Search className="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <input
                type="search"
                value={searchQuery}
                onChange={handleSearchQueryChange}
                placeholder={t('book.listing.searchPlaceholder')}
                className="h-11 w-full rounded-full border border-border bg-card pl-11 pr-4 text-sm outline-none transition-colors focus:border-primary"
                aria-label={t('header.searchAria')}
              />
            </form>

            <div className="mb-4">
              <LanguageSwitcher />
            </div>

            <nav className="flex flex-col gap-2">
              {navLinks.map((link) => (
                <Link
                  key={link.to}
                  to={link.to}
                  onClick={closeMenu}
                  className={cn(
                    'rounded-2xl px-3 py-3 text-sm font-medium transition-colors',
                    isActiveLink(link.to)
                      ? 'bg-primary/10 text-primary'
                      : 'text-foreground hover:bg-muted',
                  )}
                >
                  {link.label}
                </Link>
              ))}
              {!user && (
                <Link
                  to="/login"
                  onClick={closeMenu}
                  className="mt-2 rounded-2xl bg-primary px-3 py-3 text-sm font-medium text-primary-foreground transition-colors hover:opacity-90"
                >
                  {t('header.login')}
                </Link>
              )}
            </nav>
          </div>
        </div>
      )}
    </header>
  )
}

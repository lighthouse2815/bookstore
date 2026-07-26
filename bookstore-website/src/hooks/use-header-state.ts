import { useEffect, useRef, useState, type ChangeEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/auth-context'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import { useBrandWordmark } from '@/hooks/use-brand-wordmark'
import { useTheme } from '@/contexts/theme-context'

export function useHeaderState() {
  const { items } = useCart()
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { t } = useLanguage()
  const { brandPrefix, brandSuffix } = useBrandWordmark()
  const location = useLocation()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const profileRef = useRef<HTMLDivElement>(null)

  const navLinks = [
    { to: '/', label: t('header.nav.home') },
    { to: '/books', label: t('header.nav.books') },
    { to: '/ebooks', label: t('header.nav.ebooks') },
    ...(user ? [{ to: '/library', label: t('header.nav.digitalLibrary') }] : []),
  ]
  const searchTargetPath = isEbooksPath(location.pathname) ? '/ebooks' : '/books'

  useEffect(() => {
    const query = new URLSearchParams(location.search).get('q')?.trim() ?? ''
    setSearchQuery(query)
    setOpen(false)
  }, [location.pathname, location.search])

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

  useEffect(() => {
    if (!open && !profileOpen) {
      return
    }

    function handleEscape(event: KeyboardEvent) {
      if (event.key !== 'Escape') {
        return
      }

      setOpen(false)
      setProfileOpen(false)
    }

    document.addEventListener('keydown', handleEscape)
    return () => document.removeEventListener('keydown', handleEscape)
  }, [open, profileOpen])

  function isActiveLink(to: string) {
    switch (to) {
      case '/':
        return location.pathname === '/'
      case '/books':
        return isBooksPath(location.pathname)
      case '/ebooks':
        return isEbooksPath(location.pathname)
      case '/library':
        return location.pathname.startsWith('/library')
      default:
        return (
          location.pathname === to || location.pathname.startsWith(`${to}/`)
        )
    }
  }

  function toggleMenu() {
    setOpen((current) => !current)
  }

  function closeMenu() {
    setOpen(false)
  }

  function toggleProfileMenu() {
    setProfileOpen((current) => !current)
  }

  function closeProfileMenu() {
    setProfileOpen(false)
  }

  async function handleLogout() {
    await logout()
    setProfileOpen(false)
    setOpen(false)
  }

  function handleSearchQueryChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchQuery(event.currentTarget.value)
  }

  function submitSearch() {
    const trimmedQuery = searchQuery.trim()
    const searchParams = new URLSearchParams()

    if (trimmedQuery) {
      searchParams.set('q', trimmedQuery)
    }

    const queryString = searchParams.toString()

    navigate(queryString ? `${searchTargetPath}?${queryString}` : searchTargetPath)
    setOpen(false)
  }

  return {
    user,
    theme,
    t,
    brandPrefix,
    brandSuffix,
    totalItems: items.length,
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
  }
}

function isBooksPath(pathname: string) {
  return pathname === '/books' || /^\/books\/[^/]+$/.test(pathname)
}

function isEbooksPath(pathname: string) {
  return pathname === '/ebooks' || /^\/books\/[^/]+\/ebook$/.test(pathname)
}

import { useEffect, useRef, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/auth-context'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import { useBrandWordmark } from '@/hooks/use-brand-wordmark'
import { useTheme } from '@/contexts/theme-context'

export function useHeaderState() {
  const { totalQuantity } = useCart()
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { t } = useLanguage()
  const { brand, brandPrefix, brandSuffix } = useBrandWordmark()
  const location = useLocation()
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

  function isActiveLink(to: string) {
    return location.pathname + location.search === to
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

  return {
    user,
    theme,
    t,
    brand,
    brandPrefix,
    brandSuffix,
    totalItems: totalQuantity,
    navLinks,
    open,
    profileOpen,
    profileRef,
    toggleTheme,
    isActiveLink,
    toggleMenu,
    closeMenu,
    toggleProfileMenu,
    closeProfileMenu,
    handleLogout,
  }
}

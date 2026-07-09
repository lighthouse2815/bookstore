import {
  startTransition,
  useDeferredValue,
  useEffect,
  useMemo,
  useState,
} from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import {
  buildAdminCommandItems,
  filterAdminCommandItems,
} from '@/services/admin-command-service'
import type { AdminCommandItem } from '@/types/admin-command'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { useTheme } from '@/contexts/theme-context'

export type UseAdminCommandPaletteResult = {
  isOpen: boolean
  query: string
  commands: AdminCommandItem[]
  highlightedIndex: number
  open: () => void
  close: () => void
  setQuery: (value: string) => void
  setHighlightedIndex: (index: number) => void
  moveHighlight: (direction: 1 | -1) => void
  executeCommand: (command: AdminCommandItem) => Promise<void>
  executeHighlightedCommand: () => Promise<void>
}

export function useAdminCommandPalette(): UseAdminCommandPaletteResult {
  const { logout, user } = useAuth()
  const { t } = useLanguage()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const location = useLocation()
  const [isOpen, setIsOpen] = useState(false)
  const [query, setQueryState] = useState('')
  const [highlightedIndex, setHighlightedIndex] = useState(0)
  const deferredQuery = useDeferredValue(query)
  const roles = user?.roles ?? []

  const commands = useMemo(
    () =>
      filterAdminCommandItems(
        buildAdminCommandItems({
          pathname: location.pathname,
          roles,
          t,
          theme,
        }),
        deferredQuery,
      ),
    [deferredQuery, location.pathname, roles, t, theme],
  )

  useEffect(() => {
    function handleWindowKeyDown(event: KeyboardEvent) {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        setIsOpen((currentIsOpen) => !currentIsOpen)
        return
      }

      if (event.key === 'Escape') {
        setIsOpen(false)
      }
    }

    window.addEventListener('keydown', handleWindowKeyDown)
    return () => {
      window.removeEventListener('keydown', handleWindowKeyDown)
    }
  }, [])

  useEffect(() => {
    setIsOpen(false)
    setQueryState('')
    setHighlightedIndex(0)
  }, [location.pathname])

  useEffect(() => {
    setHighlightedIndex((currentIndex) => {
      if (commands.length === 0) {
        return 0
      }

      return Math.min(currentIndex, commands.length - 1)
    })
  }, [commands.length])

  function open() {
    setIsOpen(true)
  }

  function close() {
    setIsOpen(false)
    setQueryState('')
    setHighlightedIndex(0)
  }

  function setQuery(value: string) {
    startTransition(() => {
      setQueryState(value)
      setHighlightedIndex(0)
    })
  }

  function moveHighlight(direction: 1 | -1) {
    if (commands.length === 0) {
      return
    }

    setHighlightedIndex((currentIndex) => {
      const nextIndex = currentIndex + direction

      if (nextIndex < 0) {
        return commands.length - 1
      }

      if (nextIndex >= commands.length) {
        return 0
      }

      return nextIndex
    })
  }

  async function executeCommand(command: AdminCommandItem) {
    if (command.kind === 'route' && command.href) {
      close()
      navigate(command.href)
      return
    }

    switch (command.id) {
      case 'TOGGLE_THEME':
        toggleTheme()
        close()
        return
      case 'OPEN_CHAT':
        close()
        navigate('/admin/chat')
        return
      case 'GO_STOREFRONT':
        close()
        navigate('/')
        return
      case 'LOGOUT':
        close()
        await logout()
        navigate('/login', { replace: true })
        return
      default:
        return
    }
  }

  async function executeHighlightedCommand() {
    const currentCommand = commands[highlightedIndex]
    if (!currentCommand) {
      return
    }

    await executeCommand(currentCommand)
  }

  return {
    isOpen,
    query,
    commands,
    highlightedIndex,
    open,
    close,
    setQuery,
    setHighlightedIndex,
    moveHighlight,
    executeCommand,
    executeHighlightedCommand,
  }
}

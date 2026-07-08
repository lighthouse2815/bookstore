import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  addWishlistBook,
  getMyWishlist,
  removeWishlistBook,
} from '@/services/wishlist-service'
import type { BookCardData } from '@/types/book'
import { getErrorMessage } from '@/utils'

type WishlistContextType = {
  items: BookCardData[]
  isLoading: boolean
  isWishlisted: (bookId: string) => boolean
  addBook: (book: BookCardData) => Promise<void>
  removeBook: (bookId: string) => Promise<void>
  toggleBook: (book: BookCardData) => Promise<boolean>
  refreshWishlist: () => Promise<void>
}

const WishlistContext = createContext<WishlistContextType | undefined>(undefined)

export function WishlistProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading: isAuthLoading } = useAuth()
  const { t } = useLanguage()
  const [items, setItems] = useState<BookCardData[]>([])
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    if (isAuthLoading) {
      return
    }

    if (!isAuthenticated) {
      setItems([])
      setIsLoading(false)
      return
    }

    let isCancelled = false

    async function loadWishlist() {
      setIsLoading(true)

      try {
        const nextItems = await getMyWishlist()

        if (!isCancelled) {
          setItems(nextItems)
        }
      } catch {
        if (!isCancelled) {
          setItems([])
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadWishlist()

    return () => {
      isCancelled = true
    }
  }, [isAuthenticated, isAuthLoading])

  const wishlistedBookIds = useMemo(
    () => new Set(items.map((item) => item.id)),
    [items],
  )

  async function refreshWishlist() {
    if (!isAuthenticated) {
      setItems([])
      return
    }

    setIsLoading(true)
    try {
      setItems(await getMyWishlist())
    } catch (error) {
      throw new Error(getErrorMessage(error, t('wishlist.fetchError')))
    } finally {
      setIsLoading(false)
    }
  }

  async function addBook(book: BookCardData) {
    if (!isAuthenticated) {
      throw new Error(t('wishlist.loginRequired'))
    }

    try {
      await addWishlistBook(book.id)
      setItems((currentItems) => [
        book,
        ...currentItems.filter((currentItem) => currentItem.id !== book.id),
      ])
    } catch (error) {
      throw new Error(getErrorMessage(error, t('wishlist.updateError')))
    }
  }

  async function removeBook(bookId: string) {
    if (!isAuthenticated) {
      throw new Error(t('wishlist.loginRequired'))
    }

    try {
      await removeWishlistBook(bookId)
      setItems((currentItems) =>
        currentItems.filter((currentItem) => currentItem.id !== bookId),
      )
    } catch (error) {
      throw new Error(getErrorMessage(error, t('wishlist.updateError')))
    }
  }

  async function toggleBook(book: BookCardData) {
    if (wishlistedBookIds.has(book.id)) {
      await removeBook(book.id)
      return false
    }

    await addBook(book)
    return true
  }

  return (
    <WishlistContext.Provider
      value={{
        items,
        isLoading,
        isWishlisted: (bookId: string) => wishlistedBookIds.has(bookId),
        addBook,
        removeBook,
        toggleBook,
        refreshWishlist,
      }}
    >
      {children}
    </WishlistContext.Provider>
  )
}

export function useWishlist() {
  const context = useContext(WishlistContext)

  if (!context) {
    throw new Error('useWishlist must be used within WishlistProvider')
  }

  return context
}

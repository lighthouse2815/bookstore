import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  addCartItem,
  addDigitalCartItem,
  clearMyCart,
  getMyCart,
  removeCartItem,
  updateCartItem,
} from '@/services/cart-service'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import type { CartItem, CartResponse } from '@/types/cart'
import { getErrorMessage } from '@/utils'

type CartContextType = {
  items: CartItem[]
  total: number
  totalQuantity: number
  isLoading: boolean
  addItem: (bookId: string, quantity?: number) => Promise<void>
  addDigitalItem: (digitalAssetId: string) => Promise<void>
  removeItem: (itemId: string) => Promise<void>
  removeItems: (itemIds: string[]) => Promise<void>
  updateQty: (itemId: string, quantity: number) => Promise<void>
  clearCart: () => Promise<void>
  refreshCart: () => Promise<void>
}

const CartContext = createContext<CartContextType | undefined>(undefined)

function mapCartItems(response: CartResponse): CartItem[] {
  return response.items.map((item) => ({
    id: item.id,
    itemType: item.itemType,
    bookId: item.bookId,
    digitalAssetId: item.digitalAssetId,
    title: item.bookTitle,
    assetTitle: item.assetTitle,
    format: item.format,
    cover: item.imageUrl,
    price: item.price,
    qty: item.quantity,
    lineTotal: item.lineTotal,
  }))
}

export function CartProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading: isAuthLoading } = useAuth()
  const { t } = useLanguage()
  const [items, setItems] = useState<CartItem[]>([])
  const [total, setTotal] = useState(0)
  const [totalQuantity, setTotalQuantity] = useState(0)
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    if (isAuthLoading) {
      return
    }

    if (!isAuthenticated) {
      setItems([])
      setTotal(0)
      setTotalQuantity(0)
      setIsLoading(false)
      return
    }

    void refreshCart().catch(() => undefined)
  }, [isAuthenticated, isAuthLoading])

  async function refreshCart() {
    if (!isAuthenticated) {
      setItems([])
      setTotal(0)
      setTotalQuantity(0)
      return
    }

    setIsLoading(true)

    try {
      const cart = await getMyCart()
      applyCartResponse(cart)
    } catch (error) {
      throw new Error(getErrorMessage(error, t('cart.fetchError')))
    } finally {
      setIsLoading(false)
    }
  }

  async function addItem(bookId: string, quantity = 1) {
    if (!isAuthenticated) {
      throw new Error(t('cart.loginRequired'))
    }

    try {
      const cart = await addCartItem({ bookId, quantity })
      applyCartResponse(cart)
    } catch (error) {
      throw new Error(getErrorMessage(error, t('cart.updateError')))
    }
  }

  async function addDigitalItem(digitalAssetId: string) {
    if (!isAuthenticated) {
      throw new Error(t('cart.loginRequired'))
    }

    try {
      const cart = await addDigitalCartItem({ digitalAssetId })
      applyCartResponse(cart)
    } catch (error) {
      throw new Error(getErrorMessage(error, t('cart.updateError')))
    }
  }

  async function removeItem(itemId: string) {
    if (!isAuthenticated) {
      throw new Error(t('cart.loginRequired'))
    }

    try {
      await removeCartItem(itemId)
      await refreshCart()
    } catch (error) {
      throw new Error(getErrorMessage(error, t('cart.updateError')))
    }
  }

  async function removeItems(itemIds: string[]) {
    if (!isAuthenticated) {
      throw new Error(t('cart.loginRequired'))
    }

    const uniqueItemIds = Array.from(new Set(itemIds))
    if (uniqueItemIds.length === 0) {
      return
    }

    try {
      await Promise.all(uniqueItemIds.map((itemId) => removeCartItem(itemId)))
      await refreshCart()
    } catch (error) {
      throw new Error(getErrorMessage(error, t('cart.updateError')))
    }
  }

  async function updateQty(itemId: string, quantity: number) {
    if (!isAuthenticated) {
      throw new Error(t('cart.loginRequired'))
    }

    if (quantity <= 0) {
      await removeItem(itemId)
      return
    }

    try {
      const cart = await updateCartItem(itemId, { quantity })
      applyCartResponse(cart)
    } catch (error) {
      throw new Error(getErrorMessage(error, t('cart.updateError')))
    }
  }

  async function clearCart() {
    if (!isAuthenticated) {
      setItems([])
      setTotal(0)
      setTotalQuantity(0)
      return
    }

    try {
      await clearMyCart()
      setItems([])
      setTotal(0)
      setTotalQuantity(0)
    } catch (error) {
      throw new Error(getErrorMessage(error, t('cart.updateError')))
    }
  }

  function applyCartResponse(response: CartResponse) {
    setItems(mapCartItems(response))
    setTotal(response.totalAmount)
    setTotalQuantity(response.totalQuantity)
  }

  const value = useMemo<CartContextType>(
    () => ({
      items,
      total,
      totalQuantity,
      isLoading,
      addItem,
      addDigitalItem,
      removeItem,
      removeItems,
      updateQty,
      clearCart,
      refreshCart,
    }),
    [items, total, totalQuantity, isLoading],
  )

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}

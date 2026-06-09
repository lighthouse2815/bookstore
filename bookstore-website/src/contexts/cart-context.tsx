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
  removeItem: (id: string) => Promise<void>
  updateQty: (id: string, quantity: number) => Promise<void>
  clearCart: () => Promise<void>
  refreshCart: () => Promise<void>
}

const CartContext = createContext<CartContextType | undefined>(undefined)

function mapCartItems(response: CartResponse): CartItem[] {
  return response.items.map((item) => ({
    id: item.bookId,
    title: item.bookTitle,
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

  async function removeItem(id: string) {
    if (!isAuthenticated) {
      throw new Error(t('cart.loginRequired'))
    }

    try {
      await removeCartItem(id)
      await refreshCart()
    } catch (error) {
      throw new Error(getErrorMessage(error, t('cart.updateError')))
    }
  }

  async function updateQty(id: string, quantity: number) {
    if (!isAuthenticated) {
      throw new Error(t('cart.loginRequired'))
    }

    if (quantity <= 0) {
      await removeItem(id)
      return
    }

    try {
      const cart = await updateCartItem(id, { quantity })
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
      removeItem,
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

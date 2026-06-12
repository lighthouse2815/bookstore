import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import type { Book } from '@/types/book'

export function useAddToCart(book: Book) {
  const { addItem } = useCart()
  const { isAuthenticated } = useAuth()
  const { t } = useLanguage()
  const navigate = useNavigate()
  const [qty, setQty] = useState(1)

  function decrementQty() {
    setQty((currentQty) => Math.max(1, currentQty - 1))
  }

  function incrementQty() {
    setQty((currentQty) => currentQty + 1)
  }

  async function handleAddToCart() {
    if (!isAuthenticated) {
      toast.error(t('cart.loginRequired'))
      navigate('/login')
      return
    }

    try {
      await addItem(book.id, qty)
      toast.success(t('book.addToCart.addedQtyToCart', { count: qty }))
    } catch (error) {
      toast.error(error instanceof Error ? error.message : t('cart.updateError'))
    }
  }

  async function handleBuyNow() {
    if (!isAuthenticated) {
      toast.error(t('cart.loginRequired'))
      navigate('/login')
      return
    }

    try {
      await addItem(book.id, qty)
      navigate('/checkout')
    } catch (error) {
      toast.error(error instanceof Error ? error.message : t('cart.updateError'))
    }
  }

  return {
    t,
    qty,
    decrementQty,
    incrementQty,
    handleAddToCart,
    handleBuyNow,
  }
}

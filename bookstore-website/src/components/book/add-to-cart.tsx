import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Minus, Plus, ShoppingCart, Zap } from 'lucide-react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import type { Book } from '@/types/book'

export function AddToCart({ book }: { book: Book }) {
  const { addItem } = useCart()
  const { isAuthenticated } = useAuth()
  const { t } = useLanguage()
  const navigate = useNavigate()
  const [qty, setQty] = useState(1)

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

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4">
        <span className="text-sm font-medium">
          {t('book.addToCart.quantity')}
        </span>
        <div className="flex items-center rounded-full border border-border">
          <button
            type="button"
            onClick={() => setQty((currentQty) => Math.max(1, currentQty - 1))}
            className="flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted"
            aria-label={t('book.addToCart.decrease')}
          >
            <Minus className="size-4" />
          </button>
          <span className="w-10 text-center font-semibold">{qty}</span>
          <button
            type="button"
            onClick={() => setQty((currentQty) => currentQty + 1)}
            className="flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted"
            aria-label={t('book.addToCart.increase')}
          >
            <Plus className="size-4" />
          </button>
        </div>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row">
        <button
          type="button"
          onClick={() => {
            void handleAddToCart()
          }}
          className="inline-flex flex-1 items-center justify-center gap-2 rounded-full border border-primary bg-background px-6 py-3 text-sm font-semibold text-primary transition-colors hover:bg-primary/5"
        >
          <ShoppingCart className="size-4" />
          {t('book.addToCart.addToCart')}
        </button>
        <button
          type="button"
          onClick={() => {
            void handleBuyNow()
          }}
          className="inline-flex flex-1 items-center justify-center gap-2 rounded-full bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
        >
          <Zap className="size-4" />
          {t('book.addToCart.buyNow')}
        </button>
      </div>
    </div>
  )
}

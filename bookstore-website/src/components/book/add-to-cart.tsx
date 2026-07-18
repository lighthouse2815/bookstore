import { Minus, Plus, ShoppingCart, Zap } from 'lucide-react'
import { useAddToCart } from '@/hooks/use-add-to-cart'
import type { Book } from '@/types/book'

export function AddToCart({ book }: { book: Book }) {
  const { t, qty, decrementQty, incrementQty, handleAddToCart, handleBuyNow } =
    useAddToCart(book)

  return (
    <div className="grid gap-3 md:grid-cols-3 md:items-center">
      <div className="flex min-h-14 items-center justify-between gap-4 rounded-2xl border border-border/80 bg-background/55 px-4 py-2.5">
        <span className="text-sm font-semibold text-foreground">
          {t('book.addToCart.quantity')}
        </span>
        <div className="flex items-center rounded-xl border border-border bg-background/70 p-0.5">
          <button
            type="button"
            onClick={decrementQty}
            className="flex size-9 items-center justify-center rounded-[0.625rem] text-muted-foreground transition-all hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:scale-95"
            aria-label={t('book.addToCart.decrease')}
          >
            <Minus className="size-4" />
          </button>
          <span className="w-9 text-center font-semibold tabular-nums">{qty}</span>
          <button
            type="button"
            onClick={incrementQty}
            className="flex size-9 items-center justify-center rounded-[0.625rem] text-muted-foreground transition-all hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:scale-95"
            aria-label={t('book.addToCart.increase')}
          >
            <Plus className="size-4" />
          </button>
        </div>
      </div>

      <button
        type="button"
        onClick={() => {
          void handleAddToCart()
        }}
        className="inline-flex min-h-14 w-full items-center justify-center gap-2 rounded-2xl border border-primary/60 bg-background/55 px-5 py-3 text-sm font-semibold text-primary transition-all duration-200 hover:-translate-y-0.5 hover:border-primary hover:bg-primary/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:translate-y-0"
      >
        <ShoppingCart className="size-4" />
        {t('book.addToCart.addToCart')}
      </button>

      <button
        type="button"
        onClick={() => {
          void handleBuyNow()
        }}
        className="inline-flex min-h-14 w-full items-center justify-center gap-2 rounded-2xl bg-primary px-5 py-3 text-sm font-semibold text-primary-foreground shadow-[0_16px_38px_-20px_hsl(var(--primary)/0.9)] transition-all duration-200 hover:-translate-y-0.5 hover:brightness-105 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 focus-visible:ring-offset-2 focus-visible:ring-offset-background active:translate-y-0"
      >
        <Zap className="size-4" />
        {t('book.addToCart.buyNow')}
      </button>
    </div>
  )
}

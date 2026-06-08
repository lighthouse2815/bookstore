import { Link } from 'react-router-dom'
import { ShoppingCart, Star } from 'lucide-react'
import { toast } from 'sonner'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import type { Book } from '@/types/book'
import { getCategoryLabel } from '@/utils/i18n'

export function BookCard({ book }: { book: Book }) {
  const { addItem } = useCart()
  const { t, formatCurrency } = useLanguage()
  const hasRating = typeof book.rating === 'number' && book.rating > 0

  const discount = book.oldPrice
    ? Math.round((1 - book.price / book.oldPrice) * 100)
    : 0

  return (
    <div className="group flex flex-col overflow-hidden rounded-2xl border border-border bg-card transition-all hover:-translate-y-1 hover:shadow-lg">
      <Link
        to={`/books/${book.id}`}
        className="relative block aspect-[3/4] overflow-hidden bg-muted"
      >
        <img
          src={book.cover || '/placeholder.svg'}
          alt={t('book.card.coverAlt', { title: book.title })}
          className="absolute inset-0 size-full object-cover transition-transform duration-300 group-hover:scale-105"
        />
        <div className="absolute left-2 top-2 flex flex-col gap-1">
          {book.bestseller && (
            <span className="rounded-full bg-accent px-2 py-1 text-xs font-bold text-accent-foreground">
              {t('book.card.bestseller')}
            </span>
          )}
          {discount > 0 && (
            <span className="rounded-full bg-primary px-2 py-1 text-xs font-bold text-primary-foreground">
              -{discount}%
            </span>
          )}
        </div>
      </Link>

      <div className="flex flex-1 flex-col p-4">
        <span className="mb-1 text-xs font-medium text-accent">
          {getCategoryLabel(book.category, t)}
        </span>
        <Link to={`/books/${book.id}`}>
          <h3 className="line-clamp-2 font-heading text-sm font-semibold leading-snug text-balance hover:text-primary">
            {book.title}
          </h3>
        </Link>
        <p className="mt-1 text-xs text-muted-foreground">
          {book.author || t('book.fallback.author')}
        </p>

        {hasRating && (
          <div className="mt-2 flex items-center gap-1 text-xs">
            <Star className="size-3.5 fill-chart-3 text-chart-3" />
            <span className="font-medium">{book.rating}</span>
            <span className="text-muted-foreground">({book.reviews ?? 0})</span>
          </div>
        )}

        <div className="mt-auto flex items-end justify-between gap-2 pt-3">
          <div className="flex flex-col">
            <span className="font-heading text-base font-bold text-primary">
              {formatCurrency(book.price)}
            </span>
            {book.oldPrice && (
              <span className="text-xs text-muted-foreground line-through">
                {formatCurrency(book.oldPrice)}
              </span>
            )}
          </div>
          <button
            type="button"
            onClick={() => {
              addItem(book)
              toast.success(t('book.card.addedToCart', { title: book.title }))
            }}
            className="flex size-10 shrink-0 items-center justify-center rounded-full bg-primary text-primary-foreground transition-opacity hover:opacity-90"
            aria-label={t('book.card.addToCartAria', { title: book.title })}
          >
            <ShoppingCart className="size-4" />
          </button>
        </div>
      </div>
    </div>
  )
}

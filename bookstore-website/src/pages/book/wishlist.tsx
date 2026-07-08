import { Link } from 'react-router-dom'
import { Heart } from 'lucide-react'
import { Button } from '@/components/common/button'
import { BookCard } from '@/components/book/book-card'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useWishlist } from '@/contexts/wishlist-context'

export default function WishlistPage() {
  const { t, formatNumber } = useLanguage()
  const { items, isLoading } = useWishlist()

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="flex-1 bg-gradient-to-b from-background via-background to-primary/5">
        <div className="mx-auto w-full max-w-[1280px] px-4 py-10 sm:px-6 lg:px-8">
          <div className="mb-8 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <span className="inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-1.5 text-sm font-semibold text-primary">
                <Heart className="size-4" />
                {t('wishlist.badge')}
              </span>
              <h1 className="mt-4 font-heading text-3xl font-bold tracking-tight">
                {t('wishlist.title')}
              </h1>
              <p className="mt-2 text-muted-foreground">
                {t('wishlist.description')}
              </p>
            </div>
            <p className="text-sm text-muted-foreground">
              {t('wishlist.count', { count: formatNumber(items.length) })}
            </p>
          </div>

          {isLoading ? (
            <div className="rounded-3xl border border-dashed border-border px-6 py-16 text-center">
              <p className="font-heading text-lg font-semibold">
                {t('common.loading')}
              </p>
            </div>
          ) : items.length === 0 ? (
            <div className="rounded-3xl border border-dashed border-border bg-card px-6 py-16 text-center shadow-sm">
              <h2 className="font-heading text-2xl font-bold">
                {t('wishlist.emptyTitle')}
              </h2>
              <p className="mx-auto mt-3 max-w-2xl text-muted-foreground">
                {t('wishlist.emptyDescription')}
              </p>
              <Link to="/books" className="mt-6 inline-flex">
                <Button>{t('wishlist.browseBooks')}</Button>
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-4">
              {items.map((book) => (
                <BookCard key={book.id} book={book} />
              ))}
            </div>
          )}
        </div>
      </main>
      <Footer />
    </div>
  )
}

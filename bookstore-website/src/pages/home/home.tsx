import { Link } from 'react-router-dom'
import {
  ArrowRight,
  BookMarked,
  Sparkles,
  Truck,
  Wallet,
} from 'lucide-react'
import { BookCard } from '@/components/book/book-card'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useBookCatalog } from '@/hooks/use-book-catalog'
import { getBookCoverUrl } from '@/utils/book-cover'
import { getCategoryLabel } from '@/utils/i18n'

export default function HomePage() {
  const { t } = useLanguage()
  const { books, categories, isLoading, error } = useBookCatalog()
  const hero = books[0]
  const highlightedBooks = books.filter((book) => book.id !== hero?.id).slice(0, 2)
  const featured = books.slice(0, 4)
  const bestsellers =
    books.length > 4 ? books.slice(4, 8) : books.slice(0, Math.min(4, books.length))

  const valueProps = [
    {
      icon: Truck,
      title: t('home.values.fastDeliveryTitle'),
      description: t('home.values.fastDeliveryDesc'),
    },
    {
      icon: Wallet,
      title: t('home.values.greatPriceTitle'),
      description: t('home.values.greatPriceDesc'),
    },
    {
      icon: BookMarked,
      title: t('home.values.authenticTitle'),
      description: t('home.values.authenticDesc'),
    },
  ]

  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col">
        <Header />
        <main className="mx-auto flex w-full max-w-7xl flex-1 items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
          <div className="rounded-2xl border border-dashed border-border px-8 py-12 text-center">
            <p className="font-heading text-lg font-semibold">
              {t('common.loading')}
            </p>
          </div>
        </main>
        <Footer />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-screen flex-col">
        <Header />
        <main className="mx-auto flex w-full max-w-7xl flex-1 items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
          <div className="rounded-2xl border border-dashed border-border px-8 py-12 text-center">
            <p className="font-heading text-lg font-semibold">
              {t('book.listing.errorTitle')}
            </p>
            <p className="mt-2 max-w-xl text-sm text-muted-foreground">
              {error}
            </p>
          </div>
        </main>
        <Footer />
      </div>
    )
  }

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        <section className="relative overflow-hidden border-b border-border bg-muted/40">
          <div className="mx-auto grid max-w-7xl items-center gap-10 px-4 py-12 sm:px-6 lg:grid-cols-2 lg:gap-6 lg:px-8 lg:py-20">
            <div className="flex flex-col items-start gap-6">
              <span className="inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-1.5 text-sm font-semibold text-primary">
                <Sparkles className="size-4" />
                {t('home.heroBadge')}
              </span>
              <h1 className="font-heading text-4xl font-bold leading-tight tracking-tight text-balance sm:text-5xl lg:text-6xl">
                {t('home.heroTitlePrefix')}{' '}
                <span className="text-primary">
                  {t('home.heroTitleAccent')}
                </span>
              </h1>
              <p className="max-w-md text-lg leading-relaxed text-muted-foreground text-pretty">
                {t('home.heroDescription')}
              </p>
              <div className="flex flex-wrap items-center gap-3">
                <Link
                  to="/books"
                  className="inline-flex items-center gap-2 rounded-full bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
                >
                  {t('home.shopNow')}
                  <ArrowRight className="size-4" />
                </Link>
                <Link
                  to="/books?category=__life-skills__"
                  className="inline-flex items-center gap-2 rounded-full border border-border bg-background px-6 py-3 text-sm font-semibold transition-colors hover:bg-muted"
                >
                  {t('home.lifeSkillsBooks')}
                </Link>
              </div>
              <div className="flex flex-wrap gap-6 pt-2">
                <div>
                  <p className="font-heading text-2xl font-bold">10K+</p>
                  <p className="text-sm text-muted-foreground">
                    {t('home.stats.books')}
                  </p>
                </div>
                <div>
                  <p className="font-heading text-2xl font-bold">50K+</p>
                  <p className="text-sm text-muted-foreground">
                    {t('home.stats.customers')}
                  </p>
                </div>
                <div>
                  <p className="font-heading text-2xl font-bold">4.9/5</p>
                  <p className="text-sm text-muted-foreground">
                    {t('home.stats.reviews')}
                  </p>
                </div>
              </div>
            </div>

            <div className="relative mx-auto w-full max-w-md">
              {hero ? (
                <>
                  <div className="absolute -right-4 -top-4 hidden size-24 rounded-full bg-accent/20 lg:block" />
                  <div className="absolute -bottom-6 -left-6 hidden size-32 rounded-full bg-primary/10 lg:block" />
                  <div className="relative grid grid-cols-2 gap-4">
                    <Link
                      to={`/books/${hero.id}`}
                      className="col-span-1 row-span-2 overflow-hidden rounded-2xl border border-border bg-card shadow-lg"
                    >
                      <div className="relative aspect-[3/4]">
                        <img
                          src={getBookCoverUrl(hero.cover)}
                          alt={hero.title}
                          className="absolute inset-0 size-full object-cover"
                        />
                      </div>
                    </Link>
                    {highlightedBooks.map((book) => (
                      <Link
                        key={book.id}
                        to={`/books/${book.id}`}
                        className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm"
                      >
                        <div className="relative aspect-[3/4]">
                          <img
                            src={getBookCoverUrl(book.cover)}
                            alt={book.title}
                            className="absolute inset-0 size-full object-cover"
                          />
                        </div>
                      </Link>
                    ))}
                  </div>
                </>
              ) : (
                <div className="rounded-2xl border border-dashed border-border bg-card px-6 py-12 text-center shadow-sm">
                  <p className="font-heading text-lg font-semibold">
                    {t('home.emptyTitle')}
                  </p>
                  <p className="mt-2 text-sm text-muted-foreground">
                    {t('home.emptyDescription')}
                  </p>
                </div>
              )}
            </div>
          </div>
        </section>

        <section className="border-b border-border">
          <div className="mx-auto grid max-w-7xl gap-4 px-4 py-8 sm:grid-cols-3 sm:px-6 lg:px-8">
            {valueProps.map((item) => (
              <div key={item.title} className="flex items-center gap-4">
                <span className="flex size-12 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
                  <item.icon className="size-6" />
                </span>
                <div>
                  <p className="font-heading font-semibold">{item.title}</p>
                  <p className="text-sm text-muted-foreground">
                    {item.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
          <h2 className="mb-6 font-heading text-2xl font-bold tracking-tight">
            {t('home.categoriesTitle')}
          </h2>
          {categories.length > 0 ? (
            <div className="flex flex-wrap gap-3">
              {categories.map((category) => (
                <Link
                  key={category}
                  to={`/books?category=${encodeURIComponent(category)}`}
                  className="rounded-full border border-border bg-card px-5 py-3 text-sm font-semibold transition-colors hover:border-primary hover:bg-primary hover:text-primary-foreground"
                >
                  {getCategoryLabel(category, t)}
                </Link>
              ))}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-border px-6 py-10 text-center">
              <p className="font-heading text-lg font-semibold">
                {t('home.emptyTitle')}
              </p>
              <p className="mt-2 text-sm text-muted-foreground">
                {t('home.emptyDescription')}
              </p>
            </div>
          )}
        </section>

        <section className="mx-auto max-w-7xl px-4 pb-4 sm:px-6 lg:px-8">
          <div className="mb-6 flex items-end justify-between">
            <h2 className="font-heading text-2xl font-bold tracking-tight">
              {t('home.featuredTitle')}
            </h2>
            <Link
              to="/books"
              className="inline-flex items-center gap-1 text-sm font-semibold text-primary hover:underline"
            >
              {t('common.viewAll')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            {featured.length > 0 ? (
              featured.map((book) => <BookCard key={book.id} book={book} />)
            ) : (
              <div className="col-span-full rounded-2xl border border-dashed border-border px-6 py-10 text-center">
                <p className="font-heading text-lg font-semibold">
                  {t('home.emptyTitle')}
                </p>
                <p className="mt-2 text-sm text-muted-foreground">
                  {t('home.emptyDescription')}
                </p>
              </div>
            )}
          </div>
        </section>

        <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
          <div className="flex flex-col items-center gap-4 rounded-3xl bg-primary px-6 py-12 text-center text-primary-foreground sm:px-12">
            <Sparkles className="size-8" />
            <h2 className="font-heading text-2xl font-bold text-balance sm:text-3xl">
              {t('home.promoTitle')}
            </h2>
            <p className="max-w-lg text-pretty opacity-90">
              {t('home.promoDescription')}
            </p>
            <Link
              to="/books"
              className="mt-2 inline-flex items-center gap-2 rounded-full bg-background px-6 py-3 text-sm font-semibold text-foreground transition-opacity hover:opacity-90"
            >
              {t('home.promoButton')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
        </section>

        <section className="mx-auto max-w-7xl px-4 pb-16 sm:px-6 lg:px-8">
          <div className="mb-6 flex items-end justify-between">
            <h2 className="font-heading text-2xl font-bold tracking-tight">
              {t('home.bestsellersTitle')}
            </h2>
            <Link
              to="/books"
              className="inline-flex items-center gap-1 text-sm font-semibold text-primary hover:underline"
            >
              {t('common.viewAll')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            {bestsellers.length > 0 ? (
              bestsellers.map((book) => <BookCard key={book.id} book={book} />)
            ) : (
              <div className="col-span-full rounded-2xl border border-dashed border-border px-6 py-10 text-center">
                <p className="font-heading text-lg font-semibold">
                  {t('home.emptyTitle')}
                </p>
                <p className="mt-2 text-sm text-muted-foreground">
                  {t('home.emptyDescription')}
                </p>
              </div>
            )}
          </div>
        </section>
      </main>
      <Footer />
    </div>
  )
}

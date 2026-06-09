import { Link } from 'react-router-dom'
import { ShoppingCart, Trash2 } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'

export default function CartPage() {
  const { items, removeItem, updateQty, total, totalQuantity, isLoading } =
    useCart()
  const { t, formatCurrency, formatNumber } = useLanguage()
  const subtotal = total
  const shipping = subtotal >= 200000 ? 0 : 30000
  const finalTotal = subtotal + shipping

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="container mx-auto flex-1 px-4 py-12">
        <div className="mb-8">
          <h1 className="mb-2 font-heading text-3xl font-bold">
            {t('cart.title')}
          </h1>
          <p className="text-muted-foreground">
            {t('cart.itemCount', { count: formatNumber(totalQuantity) })}
          </p>
        </div>

        {isLoading ? (
          <div className="py-12 text-center">
            <p className="text-lg text-muted-foreground">
              {t('common.loading')}
            </p>
          </div>
        ) : items.length === 0 ? (
          <div className="py-12 text-center">
            <ShoppingCart className="mx-auto mb-4 size-12 text-muted-foreground" />
            <p className="mb-4 text-lg text-muted-foreground">
              {t('cart.emptyTitle')}
            </p>
            <Link to="/books">
              <Button>{t('common.continueShopping')}</Button>
            </Link>
          </div>
        ) : (
          <div className="grid gap-8 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <div className="space-y-4">
                {items.map((item) => (
                  <div
                    key={item.id}
                    className="flex gap-4 rounded-lg border border-border bg-card p-4"
                  >
                    <img
                      src={item.cover || '/placeholder.svg'}
                      alt={item.title}
                      className="size-24 rounded object-cover"
                    />
                    <div className="flex-1">
                      <Link to={`/books/${item.id}`}>
                        <h3 className="font-semibold hover:text-primary">
                          {item.title}
                        </h3>
                      </Link>
                      <p className="mt-2 font-heading text-lg font-bold text-primary">
                        {formatCurrency(item.price)}
                      </p>
                    </div>
                    <div className="flex flex-col items-end justify-between">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => {
                            void updateQty(item.id, Math.max(1, item.qty - 1))
                          }}
                          className="rounded border px-2 py-1 hover:bg-muted"
                          aria-label="-"
                        >
                          -
                        </button>
                        <span className="w-8 text-center">{item.qty}</span>
                        <button
                          onClick={() => {
                            void updateQty(item.id, item.qty + 1)
                          }}
                          className="rounded border px-2 py-1 hover:bg-muted"
                          aria-label="+"
                        >
                          +
                        </button>
                      </div>
                      <button
                        onClick={() => {
                          void removeItem(item.id)
                        }}
                        className="mt-2 text-destructive hover:text-destructive/80"
                        aria-label={t('common.actions')}
                      >
                        <Trash2 className="size-5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="rounded-lg border border-border bg-card p-6">
              <h2 className="mb-4 font-heading text-xl font-bold">
                {t('cart.summaryTitle')}
              </h2>
              <div className="space-y-3 border-b border-border pb-4">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">
                    {t('common.subtotal')}:
                  </span>
                  <span className="font-semibold">
                    {formatCurrency(subtotal)}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">
                    {t('common.shipping')}:{' '}
                    {shipping === 0 && (
                      <span className="text-xs text-green-600">
                        ({t('common.free')})
                      </span>
                    )}
                  </span>
                  <span className="font-semibold">
                    {formatCurrency(shipping)}
                  </span>
                </div>
              </div>
              <div className="flex justify-between py-4 font-heading text-lg font-bold">
                <span>{t('common.total')}:</span>
                <span className="text-primary">
                  {formatCurrency(finalTotal)}
                </span>
              </div>
              <Link to="/checkout" className="w-full">
                <Button className="w-full">
                  {t('common.proceedToCheckout')}
                </Button>
              </Link>
            </div>
          </div>
        )}
      </main>
      <Footer />
    </div>
  )
}

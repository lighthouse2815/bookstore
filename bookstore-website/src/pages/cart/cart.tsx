import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
  type ReactNode,
} from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ArrowRight,
  CheckCircle2,
  Info,
  LockKeyhole,
  RefreshCcw,
  ShieldCheck,
  ShoppingBag,
  ShoppingCart,
  Trash2,
  Truck,
} from 'lucide-react'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import type { CartItem } from '@/types/cart'
import { cn } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'

const CART_SELECTED_ITEMS_STORAGE_KEY = 'bookstore-cart-selected-items'

export default function CartPage() {
  const { items, removeItem, removeItems, updateQty, isLoading } = useCart()
  const { t, formatCurrency, formatNumber } = useLanguage()
  const navigate = useNavigate()
  const [selectedItemIds, setSelectedItemIds] = useState<string[]>(
    readStoredSelectedItemIds,
  )
  const [isRemovingSelected, setIsRemovingSelected] = useState(false)

  useEffect(() => {
    if (items.length === 0) {
      return
    }

    setSelectedItemIds((currentIds) => {
      const itemIdSet = new Set(items.map((item) => item.id))
      return currentIds.filter((id) => itemIdSet.has(id))
    })
  }, [items])

  useEffect(() => {
    window.localStorage.setItem(
      CART_SELECTED_ITEMS_STORAGE_KEY,
      JSON.stringify(selectedItemIds),
    )
  }, [selectedItemIds])

  const selectedItemIdSet = useMemo(
    () => new Set(selectedItemIds),
    [selectedItemIds],
  )
  const selectedItems = useMemo(
    () => items.filter((item) => selectedItemIdSet.has(item.id)),
    [items, selectedItemIdSet],
  )
  const selectedSubtotal = selectedItems.reduce(
    (sum, item) => sum + item.lineTotal,
    0,
  )
  const selectedQuantity = selectedItems.reduce(
    (sum, item) => sum + item.qty,
    0,
  )
  const hasPhysicalSelectedItems = selectedItems.some(
    (item) => item.itemType === 'PHYSICAL_BOOK',
  )
  const shipping =
    hasPhysicalSelectedItems && selectedSubtotal < 200000 ? 30000 : 0
  const finalTotal = selectedSubtotal + shipping
  const allSelected = items.length > 0 && selectedItems.length === items.length
  const partiallySelected = selectedItems.length > 0 && !allSelected

  function toggleItem(itemId: string) {
    setSelectedItemIds((currentIds) =>
      currentIds.includes(itemId)
        ? currentIds.filter((id) => id !== itemId)
        : [...currentIds, itemId],
    )
  }

  function toggleAllItems() {
    setSelectedItemIds(allSelected ? [] : items.map((item) => item.id))
  }

  async function handleRemoveSelected() {
    if (selectedItems.length === 0 || isRemovingSelected) {
      return
    }

    setIsRemovingSelected(true)
    try {
      setSelectedItemIds((currentIds) =>
        currentIds.filter((id) => !selectedItemIdSet.has(id)),
      )
      await removeItems(selectedItems.map((item) => item.id))
    } finally {
      setIsRemovingSelected(false)
    }
  }

  function handleCheckoutSelected() {
    if (selectedItems.length === 0) {
      return
    }

    const searchParams = new URLSearchParams()
    searchParams.set('items', selectedItems.map((item) => item.id).join(','))
    navigate(`/checkout?${searchParams.toString()}`)
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="flex-1 bg-gradient-to-b from-background via-background to-primary/5">
        <div className="mx-auto w-full max-w-[1280px] px-4 py-10 sm:px-6 lg:px-8">
          <div className="mb-8">
            <h1 className="mb-2 font-heading text-3xl font-bold">
              {t('cart.title')}
            </h1>
            <p className="text-muted-foreground">
              {t('cart.itemCount', { count: formatNumber(items.length) })}
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
          <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_400px]">
            <section className="space-y-5">
              <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
                <div className="flex flex-col gap-3 border-b border-border px-4 py-4 sm:flex-row sm:items-center sm:justify-between">
                  <label className="flex cursor-pointer items-center gap-3 text-sm font-semibold">
                    <SelectionCheckbox
                      checked={allSelected}
                      indeterminate={partiallySelected}
                      onChange={toggleAllItems}
                      aria-label={t('cart.selectAllAria')}
                    />
                    <span>
                      {t('cart.selectAll', {
                        selected: formatNumber(selectedItems.length),
                        total: formatNumber(items.length),
                      })}
                    </span>
                  </label>

                  <button
                    type="button"
                    onClick={() => {
                      void handleRemoveSelected()
                    }}
                    disabled={selectedItems.length === 0 || isRemovingSelected}
                    className="inline-flex items-center gap-2 text-sm font-medium text-muted-foreground transition-colors hover:text-destructive disabled:pointer-events-none disabled:opacity-45"
                  >
                    {isRemovingSelected
                      ? t('common.processing')
                      : t('cart.removeSelected')}
                    <Trash2 className="size-4" />
                  </button>
                </div>

                <div className="max-h-[620px] space-y-3 overflow-y-auto overscroll-contain p-4 pr-2">
                  {items.map((item) => (
                    <CartLineItem
                      key={item.id}
                      item={item}
                      checked={selectedItemIdSet.has(item.id)}
                      onToggle={() => toggleItem(item.id)}
                      onDecrease={() => {
                        void updateQty(item.id, Math.max(1, item.qty - 1))
                      }}
                      onIncrease={() => {
                        void updateQty(item.id, item.qty + 1)
                      }}
                      onRemove={() => {
                        setSelectedItemIds((currentIds) =>
                          currentIds.filter((id) => id !== item.id),
                        )
                        void removeItem(item.id)
                      }}
                      formatCurrency={formatCurrency}
                      t={t}
                    />
                  ))}
                </div>
              </div>

              <div className="grid gap-4 rounded-2xl border border-border bg-card/80 p-4 shadow-sm sm:grid-cols-3">
                <CartBenefit
                  icon={<ShieldCheck className="size-6" />}
                  title={t('cart.qualityTitle')}
                  description={t('cart.qualityDescription')}
                />
                <CartBenefit
                  icon={<RefreshCcw className="size-6" />}
                  title={t('cart.returnTitle')}
                  description={t('cart.returnDescription')}
                />
                <CartBenefit
                  icon={<Truck className="size-6" />}
                  title={t('cart.deliveryTitle')}
                  description={t('cart.deliveryDescription')}
                />
              </div>
            </section>

            <aside className="h-fit rounded-2xl border border-border bg-card p-6 shadow-sm lg:sticky lg:top-24">
              <div className="mb-6 flex items-center gap-4">
                <span className="flex size-12 items-center justify-center rounded-full bg-primary/10 text-primary">
                  <ShoppingBag className="size-6" />
                </span>
                <div>
                  <h2 className="font-heading text-xl font-bold">
                    {t('cart.orderTitle')}
                  </h2>
                  <p className="text-sm text-muted-foreground">
                    {t('cart.selectedCount', {
                      selected: formatNumber(selectedItems.length),
                      total: formatNumber(items.length),
                    })}
                  </p>
                </div>
              </div>

              <div className="space-y-4 border-y border-border py-5">
                <div className="flex items-center justify-between gap-4 text-sm">
                  <span className="text-muted-foreground">
                    {t('cart.selectedSubtotal', {
                      count: formatNumber(selectedQuantity),
                    })}
                  </span>
                  <span className="font-semibold">
                    {formatCurrency(selectedSubtotal)}
                  </span>
                </div>
                <div className="flex items-center justify-between gap-4 text-sm">
                  <span className="inline-flex items-center gap-1 text-muted-foreground">
                    {t('common.shipping')}
                    <Info className="size-3.5" />
                  </span>
                  <span
                    className={cn(
                      'font-semibold',
                      shipping === 0 && selectedItems.length > 0
                        ? 'text-green-600'
                        : 'text-foreground',
                    )}
                  >
                    {shipping === 0 && selectedItems.length > 0
                      ? t('common.free')
                      : formatCurrency(shipping)}
                  </span>
                </div>
              </div>

              <div className="flex items-center justify-between gap-4 py-5">
                <span className="font-heading text-lg font-bold">
                  {t('cart.totalPayment')}
                </span>
                <span className="font-heading text-2xl font-bold text-primary">
                  {formatCurrency(finalTotal)}
                </span>
              </div>

              <Button
                type="button"
                onClick={handleCheckoutSelected}
                disabled={selectedItems.length === 0}
                className="h-12 w-full rounded-xl text-base font-bold"
              >
                {t('cart.checkoutSelected')}
                <ArrowRight className="ml-2 size-5" />
              </Button>

              <p className="mt-4 flex items-center justify-center gap-2 text-xs text-muted-foreground">
                <LockKeyhole className="size-4" />
                {t('cart.secureCheckout')}
              </p>
            </aside>
          </div>
        )}
        </div>
      </main>
      <Footer />
    </div>
  )
}

type SelectionCheckboxProps = {
  checked: boolean
  indeterminate?: boolean
  onChange: (event: ChangeEvent<HTMLInputElement>) => void
  'aria-label': string
}

function SelectionCheckbox({
  checked,
  indeterminate = false,
  onChange,
  'aria-label': ariaLabel,
}: SelectionCheckboxProps) {
  const checkboxRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (checkboxRef.current) {
      checkboxRef.current.indeterminate = indeterminate
    }
  }, [indeterminate])

  return (
    <input
      ref={checkboxRef}
      type="checkbox"
      checked={checked}
      onChange={onChange}
      aria-label={ariaLabel}
      className="size-5 shrink-0 cursor-pointer accent-primary"
    />
  )
}

type CartLineItemProps = {
  item: CartItem
  checked: boolean
  onToggle: () => void
  onDecrease: () => void
  onIncrease: () => void
  onRemove: () => void
  formatCurrency: (value: number) => string
  t: (key: string, values?: Record<string, string | number>) => string
}

function CartLineItem({
  item,
  checked,
  onToggle,
  onDecrease,
  onIncrease,
  onRemove,
  formatCurrency,
  t,
}: CartLineItemProps) {
  const itemTypeKey =
    item.itemType === 'DIGITAL_ASSET'
      ? 'cart.itemTypes.digitalAsset'
      : 'cart.itemTypes.physicalBook'

  return (
    <article
      className={cn(
        'grid gap-4 rounded-2xl border border-border bg-background p-4 shadow-sm transition-colors sm:grid-cols-[auto_104px_minmax(0,1fr)_auto] sm:items-center',
        checked && 'border-primary/40 bg-primary/5',
      )}
    >
      <SelectionCheckbox
        checked={checked}
        onChange={onToggle}
        aria-label={t('cart.selectItemAria', { title: item.title })}
      />

      <Link
        to={`/books/${item.bookId}`}
        className="block size-24 overflow-hidden rounded-xl bg-muted sm:size-28"
      >
        <img
          src={getBookCoverUrl(item.cover)}
          alt={item.title}
          className="size-full object-cover transition-transform hover:scale-105"
        />
      </Link>

      <div className="min-w-0 space-y-3">
        <div>
          <Link to={`/books/${item.bookId}`}>
            <h3 className="line-clamp-2 font-heading text-lg font-bold hover:text-primary">
              {item.title}
            </h3>
          </Link>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <span
              className={cn(
                'inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold',
                item.itemType === 'DIGITAL_ASSET'
                  ? 'bg-primary/10 text-primary'
                  : 'bg-emerald-500/10 text-emerald-600',
              )}
            >
              <CheckCircle2 className="size-4" />
              {t(`${itemTypeKey}.badge`)}
            </span>
            {item.assetTitle ? (
              <span className="text-sm text-muted-foreground">
                {item.assetTitle}
                {item.format ? ` • ${item.format}` : ''}
              </span>
            ) : null}
          </div>
        </div>
        <p className="font-heading text-lg font-bold text-primary sm:hidden">
          {formatCurrency(item.price)}
        </p>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 sm:flex-col sm:items-end">
        <p className="hidden font-heading text-lg font-bold text-primary sm:block">
          {formatCurrency(item.price)}
        </p>
        {item.itemType === 'DIGITAL_ASSET' ? (
          <div className="rounded-lg border border-primary/15 bg-primary/5 px-3 py-2 text-sm font-semibold text-primary">
            {t(`${itemTypeKey}.quantity`)}
          </div>
        ) : (
          <div className="grid h-10 w-32 grid-cols-3 overflow-hidden rounded-lg border border-border bg-background">
            <button
              type="button"
              onClick={onDecrease}
              className="flex items-center justify-center text-lg font-semibold text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
              aria-label={t('book.addToCart.decrease')}
            >
              -
            </button>
            <span className="flex items-center justify-center border-x border-border text-sm font-semibold">
              {item.qty}
            </span>
            <button
              type="button"
              onClick={onIncrease}
              className="flex items-center justify-center text-lg font-semibold text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
              aria-label={t('book.addToCart.increase')}
            >
              +
            </button>
          </div>
        )}
        <button
          type="button"
          onClick={onRemove}
          className="text-destructive transition-colors hover:text-destructive/80"
          aria-label={t('common.delete')}
        >
          <Trash2 className="size-5" />
        </button>
      </div>
    </article>
  )
}

function CartBenefit({
  icon,
  title,
  description,
}: {
  icon: ReactNode
  title: string
  description: string
}) {
  return (
    <div className="flex items-center gap-3">
      <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
        {icon}
      </span>
      <div>
        <p className="text-sm font-bold">{title}</p>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
    </div>
  )
}

function readStoredSelectedItemIds() {
  try {
    const storedValue = window.localStorage.getItem(
      CART_SELECTED_ITEMS_STORAGE_KEY,
    )
    if (!storedValue) {
      return []
    }

    const parsedValue: unknown = JSON.parse(storedValue)
    if (!Array.isArray(parsedValue)) {
      return []
    }

    return parsedValue.filter(
      (value): value is string =>
        typeof value === 'string' && value.trim() !== '',
    )
  } catch {
    return []
  }
}


import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { items, clearCart } = useCart()
  const { t, formatCurrency } = useLanguage()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    district: '',
    ward: '',
    paymentMethod: 'cod',
  })

  const subtotal = items.reduce((sum, item) => sum + item.price * item.qty, 0)
  const shipping = subtotal >= 200000 ? 0 : 30000
  const finalTotal = subtotal + shipping

  function handleChange(
    event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) {
    const { name, value } = event.currentTarget
    setFormData((previousValue) => ({ ...previousValue, [name]: value }))
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (
      !formData.fullName ||
      !formData.email ||
      !formData.phone ||
      !formData.address
    ) {
      toast.error(t('checkout.missingInfo'))
      return
    }

    setLoading(true)

    try {
      await new Promise((resolve) => setTimeout(resolve, 1500))
      toast.success(t('checkout.success'))
      clearCart()

      setTimeout(() => {
        navigate('/order-confirmation', { replace: true })
      }, 500)
    } catch {
      toast.error(t('checkout.error'))
    } finally {
      setLoading(false)
    }
  }

  if (items.length === 0) {
    return (
      <div className="flex min-h-screen flex-col bg-background">
        <Header />
        <main className="container mx-auto flex-1 px-4 py-12 text-center">
          <h1 className="mb-4 font-heading text-2xl font-bold">
            {t('checkout.emptyTitle')}
          </h1>
          <p className="mb-6 text-muted-foreground">
            {t('checkout.emptyDescription')}
          </p>
          <Link to="/books">
            <Button>{t('common.continueShopping')}</Button>
          </Link>
        </main>
        <Footer />
      </div>
    )
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="container mx-auto flex-1 px-4 py-12">
        <h1 className="mb-8 font-heading text-3xl font-bold">
          {t('checkout.title')}
        </h1>

        <div className="grid gap-8 lg:grid-cols-3">
          <form onSubmit={handleSubmit} className="lg:col-span-2">
            <div className="rounded-lg border border-border bg-card p-6">
              <div className="mb-8">
                <h2 className="mb-4 font-heading text-xl font-bold">
                  {t('checkout.shippingInfoTitle')}
                </h2>
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="fullName">{t('checkout.fullName')}</Label>
                    <Input
                      id="fullName"
                      name="fullName"
                      value={formData.fullName}
                      onChange={handleChange}
                      required
                      className="mt-2"
                    />
                  </div>
                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <Label htmlFor="email">{t('common.email')}</Label>
                      <Input
                        id="email"
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                        className="mt-2"
                      />
                    </div>
                    <div>
                      <Label htmlFor="phone">{t('common.phone')}</Label>
                      <Input
                        id="phone"
                        name="phone"
                        value={formData.phone}
                        onChange={handleChange}
                        required
                        className="mt-2"
                      />
                    </div>
                  </div>
                  <div>
                    <Label htmlFor="address">{t('checkout.address')}</Label>
                    <Input
                      id="address"
                      name="address"
                      value={formData.address}
                      onChange={handleChange}
                      required
                      className="mt-2"
                    />
                  </div>
                  <div className="grid gap-4 sm:grid-cols-3">
                    <div>
                      <Label htmlFor="city">{t('checkout.city')}</Label>
                      <Input
                        id="city"
                        name="city"
                        value={formData.city}
                        onChange={handleChange}
                        className="mt-2"
                      />
                    </div>
                    <div>
                      <Label htmlFor="district">{t('checkout.district')}</Label>
                      <Input
                        id="district"
                        name="district"
                        value={formData.district}
                        onChange={handleChange}
                        className="mt-2"
                      />
                    </div>
                    <div>
                      <Label htmlFor="ward">{t('checkout.ward')}</Label>
                      <Input
                        id="ward"
                        name="ward"
                        value={formData.ward}
                        onChange={handleChange}
                        className="mt-2"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <div className="border-t border-border pt-8">
                <h2 className="mb-4 font-heading text-xl font-bold">
                  {t('checkout.paymentMethodTitle')}
                </h2>
                <div className="space-y-3">
                  {(['cod', 'bank', 'card'] as const).map((method) => (
                    <label
                      key={method}
                      className="flex cursor-pointer items-center gap-3 rounded border border-border p-3 hover:bg-muted"
                    >
                      <input
                        type="radio"
                        name="paymentMethod"
                        value={method}
                        checked={formData.paymentMethod === method}
                        onChange={handleChange}
                        className="cursor-pointer"
                      />
                      <span className="font-medium">
                        {t(`checkout.paymentMethods.${method}`)}
                      </span>
                    </label>
                  ))}
                </div>
              </div>

              <Button type="submit" className="mt-8 w-full" disabled={loading}>
                {loading ? t('common.processing') : t('checkout.submit')}
              </Button>
            </div>
          </form>

          <div>
            <div className="sticky top-4 rounded-lg border border-border bg-card p-6">
              <h2 className="mb-4 font-heading text-xl font-bold">
                {t('checkout.orderSummary')}
              </h2>

              <div className="mb-6 max-h-96 space-y-3 overflow-y-auto">
                {items.map((item) => (
                  <div key={item.id} className="flex justify-between text-sm">
                    <div>
                      <p className="line-clamp-1 font-semibold">{item.title}</p>
                      <p className="text-muted-foreground">
                        {t('checkout.quantityShort', { count: item.qty })}
                      </p>
                    </div>
                    <p className="font-semibold">
                      {formatCurrency(item.price * item.qty)}
                    </p>
                  </div>
                ))}
              </div>

              <div className="space-y-3 border-t border-border pt-4">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">
                    {t('common.subtotal')}:
                  </span>
                  <span className="font-semibold">
                    {formatCurrency(subtotal)}
                  </span>
                </div>
                <div className="flex justify-between text-sm">
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

              <div className="mt-4 flex justify-between border-t border-border pt-4 font-heading text-lg font-bold">
                <span>{t('common.total')}:</span>
                <span className="text-primary">
                  {formatCurrency(finalTotal)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

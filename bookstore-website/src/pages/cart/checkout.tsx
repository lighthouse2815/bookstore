import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import { createAddress, getMyAddresses } from '@/services/address-service'
import { checkout } from '@/services/order-service'
import type { UserAddressResponse } from '@/types/address'
import { getErrorMessage } from '@/utils'

const NEW_ADDRESS_VALUE = '__new__'

const initialFormData = {
  fullName: '',
  phone: '',
  address: '',
  city: '',
  district: '',
  ward: '',
  couponCode: '',
}

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { items, total, clearCart, isLoading: isCartLoading } = useCart()
  const { t, formatCurrency } = useLanguage()
  const [loading, setLoading] = useState(false)
  const [isAddressLoading, setIsAddressLoading] = useState(true)
  const [savedAddresses, setSavedAddresses] = useState<UserAddressResponse[]>([])
  const [selectedAddressId, setSelectedAddressId] = useState(NEW_ADDRESS_VALUE)
  const [formData, setFormData] = useState(initialFormData)

  const subtotal = total
  const shipping = subtotal >= 200000 ? 0 : 30000
  const finalTotal = subtotal + shipping

  useEffect(() => {
    let isCancelled = false

    async function loadAddresses() {
      try {
        const addresses = await getMyAddresses()

        if (isCancelled) {
          return
        }

        setSavedAddresses(addresses)

        const defaultAddress =
          addresses.find((address) => address.defaultAddress) ?? addresses[0]

        if (defaultAddress) {
          setSelectedAddressId(defaultAddress.id)
          setFormData(prefillAddressForm(defaultAddress))
        }
      } catch (error) {
        if (!isCancelled) {
          toast.error(getErrorMessage(error, t('checkout.error')))
        }
      } finally {
        if (!isCancelled) {
          setIsAddressLoading(false)
        }
      }
    }

    void loadAddresses()

    return () => {
      isCancelled = true
    }
  }, [t])

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.currentTarget
    setFormData((previousValue) => ({ ...previousValue, [name]: value }))
  }

  function handleSelectAddressChange(nextValue: string) {
    setSelectedAddressId(nextValue)

    if (nextValue === NEW_ADDRESS_VALUE) {
      setFormData(initialFormData)
      return
    }

    const selectedAddress = savedAddresses.find((address) => address.id === nextValue)

    if (selectedAddress) {
      setFormData(prefillAddressForm(selectedAddress))
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const shouldCreateAddress = selectedAddressId === NEW_ADDRESS_VALUE
    const hasMissingAddressInfo =
      shouldCreateAddress &&
      (!formData.fullName ||
        !formData.phone ||
        !formData.address ||
        !formData.city ||
        !formData.district ||
        !formData.ward)

    if (hasMissingAddressInfo) {
      toast.error(t('checkout.missingInfo'))
      return
    }

    setLoading(true)

    try {
      const addressId = shouldCreateAddress
        ? (
            await createAddress({
              receiverName: formData.fullName,
              receiverPhone: formData.phone,
              receiverAddress: buildReceiverAddress(formData),
            })
          ).id
        : selectedAddressId

      const order = await checkout({
        addressId,
        couponCode: formData.couponCode.trim() || null,
      })

      await clearCart()
      toast.success(t('checkout.success'))
      navigate(`/order-confirmation?orderId=${order.orderId}`, {
        replace: true,
      })
    } catch (error) {
      toast.error(getErrorMessage(error, t('checkout.error')))
    } finally {
      setLoading(false)
    }
  }

  if (isCartLoading || isAddressLoading) {
    return (
      <div className="flex min-h-screen flex-col bg-background">
        <Header />
        <main className="container mx-auto flex-1 px-4 py-12 text-center">
          <p className="text-muted-foreground">{t('common.loading')}</p>
        </main>
        <Footer />
      </div>
    )
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

                {savedAddresses.length > 0 && (
                  <div className="mb-6 space-y-3">
                    {savedAddresses.map((address) => (
                      <label
                        key={address.id}
                        className="flex cursor-pointer items-start gap-3 rounded-xl border border-border p-4 hover:bg-muted/40"
                      >
                        <input
                          type="radio"
                          name="selectedAddress"
                          value={address.id}
                          checked={selectedAddressId === address.id}
                          onChange={(event) =>
                            handleSelectAddressChange(event.currentTarget.value)
                          }
                          className="mt-1"
                        />
                        <div>
                          <p className="font-semibold">{address.receiverName}</p>
                          <p className="text-sm text-muted-foreground">
                            {address.receiverPhone}
                          </p>
                          <p className="mt-1 text-sm text-muted-foreground">
                            {address.receiverAddress}
                          </p>
                        </div>
                      </label>
                    ))}

                    <label className="flex cursor-pointer items-start gap-3 rounded-xl border border-dashed border-border p-4 hover:bg-muted/40">
                      <input
                        type="radio"
                        name="selectedAddress"
                        value={NEW_ADDRESS_VALUE}
                        checked={selectedAddressId === NEW_ADDRESS_VALUE}
                        onChange={(event) =>
                          handleSelectAddressChange(event.currentTarget.value)
                        }
                        className="mt-1"
                      />
                      <div>
                        <p className="font-semibold">
                          {t('checkout.newAddressTitle')}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          {t('checkout.newAddressDescription')}
                        </p>
                      </div>
                    </label>
                  </div>
                )}

                {selectedAddressId === NEW_ADDRESS_VALUE && (
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
                )}
              </div>

              <div className="border-t border-border pt-8">
                <h2 className="mb-4 font-heading text-xl font-bold">
                  {t('checkout.paymentMethodTitle')}
                </h2>
                <p className="rounded-xl border border-dashed border-border bg-muted/40 p-4 text-sm text-muted-foreground">
                  {t('checkout.paymentMethodNotice')}
                </p>
              </div>

              <div className="mt-8 border-t border-border pt-8">
                <Label htmlFor="couponCode">{t('checkout.couponCode')}</Label>
                <Input
                  id="couponCode"
                  name="couponCode"
                  value={formData.couponCode}
                  onChange={handleChange}
                  placeholder={t('checkout.couponPlaceholder')}
                  className="mt-2"
                />
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
                      {formatCurrency(item.lineTotal)}
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

function buildReceiverAddress(formData: typeof initialFormData) {
  return [
    formData.address,
    formData.ward,
    formData.district,
    formData.city,
  ]
    .map((value) => value.trim())
    .filter((value) => value !== '')
    .join(', ')
}

function prefillAddressForm(address: UserAddressResponse) {
  return {
    fullName: address.receiverName,
    phone: address.receiverPhone,
    address: address.receiverAddress,
    city: '',
    district: '',
    ward: '',
    couponCode: '',
  }
}

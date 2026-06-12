import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import { createAddress, getMyAddresses } from '@/services/address-service'
import { checkout } from '@/services/order-service'
import type { UserAddressResponse } from '@/types/address'
import { getErrorMessage } from '@/utils'

export const NEW_ADDRESS_VALUE = '__new__'

type CheckoutFormState = {
  fullName: string
  phone: string
  address: string
  city: string
  district: string
  ward: string
  couponCode: string
}

const initialFormData: CheckoutFormState = {
  fullName: '',
  phone: '',
  address: '',
  city: '',
  district: '',
  ward: '',
  couponCode: '',
}

export function useCheckoutFlow() {
  const navigate = useNavigate()
  const { items, total, clearCart, isLoading: isCartLoading } = useCart()
  const { t } = useLanguage()
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

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
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

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
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

  return {
    items,
    total,
    subtotal,
    shipping,
    finalTotal,
    loading,
    isAddressLoading,
    isCartLoading,
    savedAddresses,
    selectedAddressId,
    formData,
    handleChange,
    handleSelectAddressChange,
    handleSubmit,
  }
}

function buildReceiverAddress(formData: CheckoutFormState) {
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

function prefillAddressForm(address: UserAddressResponse): CheckoutFormState {
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

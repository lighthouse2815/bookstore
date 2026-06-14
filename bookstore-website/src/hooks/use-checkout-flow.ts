import {
  useEffect,
  useMemo,
  useState,
  type ChangeEvent,
  type FormEvent,
} from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import { createAddress, getMyAddresses } from '@/services/address-service'
import { getActiveCoupons } from '@/services/coupon-service'
import { checkout } from '@/services/order-service'
import type { UserAddressResponse } from '@/types/address'
import type { CouponResponse } from '@/types/coupon'
import { getErrorMessage } from '@/utils'

export const NEW_ADDRESS_VALUE = '__new__'
const NO_ADDRESS_VALUE = ''
const STANDARD_SHIPPING_FEE = 30000
const FREE_SHIPPING_THRESHOLD = 200000

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
  const [searchParams] = useSearchParams()
  const { items: cartItems, refreshCart, isLoading: isCartLoading } = useCart()
  const { t } = useLanguage()
  const [loading, setLoading] = useState(false)
  const [isAddressLoading, setIsAddressLoading] = useState(true)
  const [isCouponLoading, setIsCouponLoading] = useState(true)
  const [savedAddresses, setSavedAddresses] = useState<UserAddressResponse[]>([])
  const [activeCoupons, setActiveCoupons] = useState<CouponResponse[]>([])
  const [selectedAddressId, setSelectedAddressId] = useState(NO_ADDRESS_VALUE)
  const [formData, setFormData] = useState(initialFormData)

  const selectedBookIds = useMemo(() => {
    const value = searchParams.get('items')
    if (!value) {
      return null
    }

    return Array.from(
      new Set(
        value
          .split(',')
          .map((id) => id.trim())
          .filter(Boolean),
      ),
    )
  }, [searchParams])

  const items = useMemo(() => {
    if (selectedBookIds === null) {
      return cartItems
    }

    const selectedBookIdSet = new Set(selectedBookIds)
    return cartItems.filter((item) => selectedBookIdSet.has(item.id))
  }, [cartItems, selectedBookIds])

  const subtotal = items.reduce((sum, item) => sum + item.lineTotal, 0)
  const shippingFee = items.length > 0 ? STANDARD_SHIPPING_FEE : 0
  const shippingDiscount =
    subtotal >= FREE_SHIPPING_THRESHOLD ? shippingFee : 0
  const shipping = shippingFee - shippingDiscount
  const selectedCoupon = useMemo(
    () =>
      activeCoupons.find(
        (coupon) =>
          coupon.code.toUpperCase() === formData.couponCode.trim().toUpperCase(),
      ) ?? null,
    [activeCoupons, formData.couponCode],
  )
  const couponDiscount = selectedCoupon
    ? calculateCouponDiscount(selectedCoupon, subtotal)
    : 0
  const finalTotal = Math.max(0, subtotal + shipping - couponDiscount)
  const selectedAddress = useMemo(
    () => savedAddresses.find((address) => address.id === selectedAddressId),
    [savedAddresses, selectedAddressId],
  )

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

  useEffect(() => {
    let isCancelled = false

    async function loadCoupons() {
      try {
        const coupons = await getActiveCoupons()

        if (isCancelled) {
          return
        }

        setActiveCoupons(coupons)
      } catch (error) {
        if (!isCancelled) {
          setActiveCoupons([])
          toast.error(getErrorMessage(error, t('checkout.error')))
        }
      } finally {
        if (!isCancelled) {
          setIsCouponLoading(false)
        }
      }
    }

    void loadCoupons()

    return () => {
      isCancelled = true
    }
  }, [t])

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.currentTarget
    setFormData((previousValue) => ({ ...previousValue, [name]: value }))
  }

  function handleCouponCodeChange(nextCouponCode: string) {
    setFormData((previousValue) => ({
      ...previousValue,
      couponCode: nextCouponCode,
    }))
  }

  function handleSelectAddressChange(nextValue: string) {
    setSelectedAddressId(nextValue)

    if (nextValue === NEW_ADDRESS_VALUE) {
      setFormData((previousValue) => ({
        ...initialFormData,
        couponCode: previousValue.couponCode,
      }))
      return
    }

    const selectedAddress = savedAddresses.find((address) => address.id === nextValue)

    if (selectedAddress) {
      setFormData((previousValue) => ({
        ...prefillAddressForm(selectedAddress),
        couponCode: previousValue.couponCode,
      }))
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const hasNoSelectedAddress = selectedAddressId === NO_ADDRESS_VALUE
    const shouldCreateAddress = selectedAddressId === NEW_ADDRESS_VALUE
    const hasMissingAddressInfo =
      shouldCreateAddress &&
      (!formData.fullName ||
        !formData.phone ||
        !formData.address ||
        !formData.city ||
        !formData.district ||
        !formData.ward)

    if (hasNoSelectedAddress || hasMissingAddressInfo) {
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
        bookIds:
          selectedBookIds === null ? null : items.map((item) => item.id),
      })

      await refreshCart()
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
    total: subtotal,
    subtotal,
    shipping,
    shippingFee,
    shippingDiscount,
    couponDiscount,
    finalTotal,
    loading,
    isAddressLoading,
    isCartLoading,
    isCouponLoading,
    savedAddresses,
    activeCoupons,
    selectedCoupon,
    selectedAddress,
    selectedAddressId,
    formData,
    handleChange,
    handleCouponCodeChange,
    handleSelectAddressChange,
    handleSubmit,
  }
}

function calculateCouponDiscount(coupon: CouponResponse, subtotal: number) {
  if (subtotal < coupon.minOrderAmount) {
    return 0
  }

  const rawDiscount =
    coupon.discountType === 'PERCENTAGE'
      ? (subtotal * coupon.discountValue) / 100
      : coupon.discountValue
  const cappedDiscount =
    coupon.maxDiscountAmount === null
      ? rawDiscount
      : Math.min(rawDiscount, coupon.maxDiscountAmount)

  return Math.min(Math.max(0, cappedDiscount), subtotal)
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

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
import { createOrder } from '@/services/order-service'
import type { UserAddressResponse } from '@/types/address'
import type { CouponResponse } from '@/types/coupon'
import type { PaymentMethod, ShippingMethod } from '@/types/order'
import { getErrorMessage } from '@/utils'

export const NEW_ADDRESS_VALUE = '__new__'
const NO_ADDRESS_VALUE = ''
const DEFAULT_SHIPPING_METHOD: ShippingMethod = 'DELIVERY'
const DEFAULT_PAYMENT_METHOD: PaymentMethod = 'BANK_TRANSFER_QR'

type CheckoutFormState = {
  fullName: string
  phone: string
  address: string
  city: string
  district: string
  ward: string
  couponCode: string
  note: string
}

const initialFormData: CheckoutFormState = {
  fullName: '',
  phone: '',
  address: '',
  city: '',
  district: '',
  ward: '',
  couponCode: '',
  note: '',
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
  const [shippingMethod, setShippingMethod] =
    useState<ShippingMethod>(DEFAULT_SHIPPING_METHOD)
  const [paymentMethod, setPaymentMethod] =
    useState<PaymentMethod>(DEFAULT_PAYMENT_METHOD)
  const [formData, setFormData] = useState(initialFormData)

  const selectedCartItemIds = useMemo(() => {
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
    if (selectedCartItemIds === null) {
      return cartItems
    }

    const selectedCartItemIdSet = new Set(selectedCartItemIds)
    return cartItems.filter((item) => selectedCartItemIdSet.has(item.id))
  }, [cartItems, selectedCartItemIds])

  const subtotal = items.reduce((sum, item) => sum + item.lineTotal, 0)
  const shippingFee = items.length > 0 ? getShippingFee(shippingMethod) : 0
  const selectedCoupon = useMemo(
    () =>
      activeCoupons.find(
        (coupon) =>
          coupon.code.toUpperCase() === formData.couponCode.trim().toUpperCase(),
      ) ?? null,
    [activeCoupons, formData.couponCode],
  )
  const shippingDiscount =
    selectedCoupon && isShippingCoupon(selectedCoupon)
      ? calculateCouponDiscount(selectedCoupon, subtotal, shippingFee)
      : 0
  const couponDiscount =
    selectedCoupon && !isShippingCoupon(selectedCoupon)
      ? calculateCouponDiscount(selectedCoupon, subtotal, subtotal)
      : 0
  const finalTotal = Math.max(
    0,
    subtotal + shippingFee - shippingDiscount - couponDiscount,
  )
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
          setFormData((previousValue) => ({
            ...prefillAddressForm(defaultAddress),
            couponCode: previousValue.couponCode,
            note: previousValue.note,
          }))
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

  function handleChange(
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) {
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
        note: previousValue.note,
      }))
      return
    }

    const nextAddress = savedAddresses.find((address) => address.id === nextValue)

    if (nextAddress) {
      setFormData((previousValue) => ({
        ...prefillAddressForm(nextAddress),
        couponCode: previousValue.couponCode,
        note: previousValue.note,
      }))
    }
  }

  function handleShippingMethodChange(nextShippingMethod: ShippingMethod) {
    setShippingMethod(nextShippingMethod)
  }

  function handlePaymentMethodChange(nextPaymentMethod: PaymentMethod) {
    setPaymentMethod(nextPaymentMethod)
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
      const normalizedCouponCode = formData.couponCode.trim().toUpperCase() || null
      const usesShippingCoupon =
        normalizedCouponCode !== null &&
        selectedCoupon !== null &&
        isShippingCoupon(selectedCoupon)
      const orderCartItemIds = items.map((item) => item.id)

      if (orderCartItemIds.length === 0) {
        toast.error(t('checkout.emptyDescription'))
        return
      }

      const order = await createOrder({
        cartItemIds: orderCartItemIds,
        addressId,
        shippingMethod,
        paymentMethod,
        bookCouponCode:
          normalizedCouponCode !== null && !usesShippingCoupon
            ? normalizedCouponCode
            : null,
        shippingCouponCode: usesShippingCoupon ? normalizedCouponCode : null,
        note: formData.note.trim() || null,
      })

      await refreshCart()
      toast.success(t('checkout.success'))

      const nextSearchParams = new URLSearchParams({
        orderId: order.orderId,
        orderCode: order.orderCode,
        transferContent: order.transferContent,
        totalAmount: String(order.totalAmount),
      })

      navigate(`/order-confirmation?${nextSearchParams.toString()}`, {
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
    subtotal,
    shippingMethod,
    paymentMethod,
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
    handleShippingMethodChange,
    handlePaymentMethodChange,
    handleSubmit,
  }
}

function calculateCouponDiscount(
  coupon: CouponResponse,
  orderSubtotal: number,
  applicableAmount: number,
) {
  if (orderSubtotal < coupon.minOrderAmount || applicableAmount <= 0) {
    return 0
  }

  const rawDiscount =
    coupon.discountType === 'PERCENTAGE'
      ? (applicableAmount * coupon.discountValue) / 100
      : coupon.discountValue
  const cappedDiscount =
    coupon.maxDiscountAmount === null
      ? rawDiscount
      : Math.min(rawDiscount, coupon.maxDiscountAmount)

  return Math.min(Math.max(0, cappedDiscount), applicableAmount)
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
    note: '',
  }
}

function isShippingCoupon(coupon: CouponResponse) {
  const text = normalizeCouponText(`${coupon.code} ${coupon.description ?? ''}`)
  return [
    'ship',
    'shipping',
    'freeship',
    'free ship',
    'giao hang',
    'van chuyen',
  ].some((keyword) => text.includes(keyword))
}

function normalizeCouponText(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}

function getShippingFee(shippingMethod: ShippingMethod) {
  switch (shippingMethod) {
    case 'DELIVERY':
    case 'PICKUP':
      return 0
  }
}

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
import { getBestCartCoupon } from '@/services/cart-service'
import { getActiveCoupons } from '@/services/coupon-service'
import { createOrder } from '@/services/order-service'
import type { UserAddressResponse } from '@/types/address'
import type { BestCouponSuggestion } from '@/types/cart'
import type { CouponResponse, CouponType } from '@/types/coupon'
import type { PaymentMethod, ShippingMethod } from '@/types/order'
import {
  calculateCouponDiscount,
  findCouponByCode,
  normalizeCouponCode,
} from '@/utils/checkout-coupon'
import { getErrorMessage } from '@/utils'

export const NEW_ADDRESS_VALUE = '__new__'
const NO_ADDRESS_VALUE = ''
const DEFAULT_SHIPPING_METHOD: ShippingMethod = 'DELIVERY'
const DEFAULT_PAYMENT_METHOD: PaymentMethod = 'BANK_TRANSFER_QR'
const DELIVERY_SHIPPING_FEE = 30_000
const FREE_SHIPPING_THRESHOLD = 200_000

type CheckoutFormState = {
  fullName: string
  phone: string
  address: string
  city: string
  district: string
  ward: string
  bookCouponCode: string
  shippingCouponCode: string
  note: string
}

const initialFormData: CheckoutFormState = {
  fullName: '',
  phone: '',
  address: '',
  city: '',
  district: '',
  ward: '',
  bookCouponCode: '',
  shippingCouponCode: '',
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
  const [bestCouponSuggestion, setBestCouponSuggestion] =
    useState<BestCouponSuggestion | null>(null)
  const [isBestCouponLoading, setIsBestCouponLoading] = useState(false)
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
  const selectedItemIdsKey = useMemo(
    () => items.map((item) => item.id).join(','),
    [items],
  )

  const hasPhysicalItems = useMemo(
    () => items.some((item) => item.itemType === 'PHYSICAL_BOOK'),
    [items],
  )
  const isDigitalOnly = items.length > 0 && !hasPhysicalItems
  const subtotal = items.reduce((sum, item) => sum + item.lineTotal, 0)
  const shippingFee =
    items.length > 0 ? getShippingFee(shippingMethod, subtotal, hasPhysicalItems) : 0
  const selectedBookCoupon = useMemo(
    () => findCouponByCode(activeCoupons, formData.bookCouponCode, 'BOOK'),
    [activeCoupons, formData.bookCouponCode],
  )
  const selectedShippingCoupon = useMemo(
    () =>
      hasPhysicalItems
        ? findCouponByCode(activeCoupons, formData.shippingCouponCode, 'SHIPPING')
        : null,
    [activeCoupons, formData.shippingCouponCode, hasPhysicalItems],
  )
  const shippingDiscount =
    selectedShippingCoupon
      ? calculateCouponDiscount(selectedShippingCoupon, subtotal, shippingFee)
      : 0
  const couponDiscount =
    selectedBookCoupon
      ? calculateCouponDiscount(selectedBookCoupon, subtotal, subtotal)
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
            bookCouponCode: previousValue.bookCouponCode,
            shippingCouponCode: previousValue.shippingCouponCode,
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

  useEffect(() => {
    if (!isDigitalOnly) {
      return
    }

    setShippingMethod('PICKUP')
    setPaymentMethod('BANK_TRANSFER_QR')
    setSelectedAddressId(NO_ADDRESS_VALUE)
  }, [isDigitalOnly])

  useEffect(() => {
    const nextBookCouponCode =
      normalizeCouponCode(searchParams.get('bookCoupon')) || null
    const nextShippingCouponCode =
      normalizeCouponCode(searchParams.get('shippingCoupon')) || null

    if (!nextBookCouponCode && !nextShippingCouponCode) {
      return
    }

    setFormData((previousValue) => ({
      ...previousValue,
      bookCouponCode: nextBookCouponCode ?? previousValue.bookCouponCode,
      shippingCouponCode:
        nextShippingCouponCode ?? previousValue.shippingCouponCode,
    }))
  }, [searchParams])

  useEffect(() => {
    if (items.length === 0) {
      setBestCouponSuggestion(null)
      setIsBestCouponLoading(false)
      return
    }

    let isCancelled = false

    async function loadBestCouponSuggestion() {
      setIsBestCouponLoading(true)

      try {
        const suggestion = await getBestCartCoupon({
          itemIds: items.map((item) => item.id),
          shippingMethod: hasPhysicalItems ? shippingMethod : 'PICKUP',
        })

        if (!isCancelled) {
          setBestCouponSuggestion(suggestion)
        }
      } catch {
        if (!isCancelled) {
          setBestCouponSuggestion(null)
        }
      } finally {
        if (!isCancelled) {
          setIsBestCouponLoading(false)
        }
      }
    }

    void loadBestCouponSuggestion()

    return () => {
      isCancelled = true
    }
  }, [items, shippingMethod, hasPhysicalItems, selectedItemIdsKey])

  function handleChange(
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) {
    const { name, value } = event.currentTarget
    setFormData((previousValue) => ({ ...previousValue, [name]: value }))
  }

  function handleCouponCodeChange(
    couponType: CouponType,
    nextCouponCode: string,
  ) {
    setFormData((previousValue) => ({
      ...previousValue,
      [couponType === 'BOOK' ? 'bookCouponCode' : 'shippingCouponCode']:
        nextCouponCode,
    }))
  }

  function handleSelectAddressChange(nextValue: string) {
    if (isDigitalOnly) {
      return
    }

    setSelectedAddressId(nextValue)

    if (nextValue === NEW_ADDRESS_VALUE) {
      setFormData((previousValue) => ({
        ...initialFormData,
        bookCouponCode: previousValue.bookCouponCode,
        shippingCouponCode: previousValue.shippingCouponCode,
        note: previousValue.note,
      }))
      return
    }

    const nextAddress = savedAddresses.find((address) => address.id === nextValue)

    if (nextAddress) {
      setFormData((previousValue) => ({
        ...prefillAddressForm(nextAddress),
        bookCouponCode: previousValue.bookCouponCode,
        shippingCouponCode: previousValue.shippingCouponCode,
        note: previousValue.note,
      }))
    }
  }

  function handleShippingMethodChange(nextShippingMethod: ShippingMethod) {
    if (isDigitalOnly) {
      setShippingMethod('PICKUP')
      return
    }

    setShippingMethod(nextShippingMethod)
  }

  function handlePaymentMethodChange(nextPaymentMethod: PaymentMethod) {
    if (isDigitalOnly && nextPaymentMethod === 'COD') {
      return
    }

    setPaymentMethod(nextPaymentMethod)
  }

  function applySuggestedCoupon() {
    if (
      !bestCouponSuggestion?.available ||
      !bestCouponSuggestion.couponCode ||
      !bestCouponSuggestion.couponType
    ) {
      return
    }

    handleCouponCodeChange(
      bestCouponSuggestion.couponType,
      bestCouponSuggestion.couponCode,
    )
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const requiresAddress = hasPhysicalItems
    const hasNoSelectedAddress =
      requiresAddress && selectedAddressId === NO_ADDRESS_VALUE
    const shouldCreateAddress =
      requiresAddress && selectedAddressId === NEW_ADDRESS_VALUE
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
      const addressId = !requiresAddress
        ? null
        : shouldCreateAddress
          ? (
              await createAddress({
                receiverName: formData.fullName,
                receiverPhone: formData.phone,
                receiverAddress: buildReceiverAddress(formData),
              })
            ).id
          : selectedAddressId
      const normalizedBookCouponCode =
        normalizeCouponCode(formData.bookCouponCode) || null
      const normalizedShippingCouponCode = hasPhysicalItems
        ? normalizeCouponCode(formData.shippingCouponCode) || null
        : null
      const orderCartItemIds = items.map((item) => item.id)

      if (orderCartItemIds.length === 0) {
        toast.error(t('checkout.emptyDescription'))
        return
      }

      const order = await createOrder({
        cartItemIds: orderCartItemIds,
        addressId,
        shippingMethod: hasPhysicalItems ? shippingMethod : 'PICKUP',
        paymentMethod: isDigitalOnly ? 'BANK_TRANSFER_QR' : paymentMethod,
        bookCouponCode: normalizedBookCouponCode,
        shippingCouponCode: normalizedShippingCouponCode,
        note: formData.note.trim() || null,
      })

      await refreshCart()
      toast.success(t('checkout.success'))

      const nextSearchParams = new URLSearchParams({
        orderId: order.orderId,
        orderCode: order.orderCode,
        paymentMethod: order.paymentMethod,
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
    hasPhysicalItems,
    isDigitalOnly,
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
    bestCouponSuggestion,
    isBestCouponLoading,
    savedAddresses,
    activeCoupons,
    selectedBookCoupon,
    selectedShippingCoupon,
    selectedAddress,
    selectedAddressId,
    formData,
    handleChange,
    handleCouponCodeChange,
    handleSelectAddressChange,
    handleShippingMethodChange,
    handlePaymentMethodChange,
    applySuggestedCoupon,
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
    bookCouponCode: '',
    shippingCouponCode: '',
    note: '',
  }
}

function getShippingFee(
  shippingMethod: ShippingMethod,
  subtotal: number,
  hasPhysicalItems: boolean,
) {
  if (!hasPhysicalItems) {
    return 0
  }

  switch (shippingMethod) {
    case 'DELIVERY':
      return subtotal < FREE_SHIPPING_THRESHOLD ? DELIVERY_SHIPPING_FEE : 0
    case 'PICKUP':
      return 0
  }
}

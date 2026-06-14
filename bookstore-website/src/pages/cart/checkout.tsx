import { useState, type ChangeEvent, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  CreditCard,
  Landmark,
  LockKeyhole,
  MapPin,
  PackageCheck,
  Pencil,
  Plus,
  Store,
  Tag,
  Truck,
  X,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import {
  NEW_ADDRESS_VALUE,
  useCheckoutFlow,
} from '@/hooks/use-checkout-flow'
import type { UserAddressResponse } from '@/types/address'
import type { CartItem } from '@/types/cart'
import type { CouponResponse } from '@/types/coupon'
import { cn } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'

type CheckoutAddressFormData = {
  fullName: string
  phone: string
  address: string
  city: string
  district: string
  ward: string
  couponCode: string
}

export default function CheckoutPage() {
  const { t, formatCurrency, language } = useLanguage()
  const {
    items,
    subtotal,
    shippingFee,
    shippingDiscount,
    couponDiscount,
    finalTotal,
    shippingMethod,
    paymentMethod,
    loading,
    isAddressLoading,
    isCartLoading,
    isCouponLoading,
    activeCoupons,
    selectedCoupon,
    savedAddresses,
    selectedAddress,
    selectedAddressId,
    formData,
    handleChange,
    handleCouponCodeChange,
    handleSelectAddressChange,
    handleShippingMethodChange,
    handlePaymentMethodChange,
    handleSubmit,
  } = useCheckoutFlow()
  const labels = buildCheckoutLabels(language)
  const [isAddressDialogOpen, setIsAddressDialogOpen] = useState(false)
  const [addressDialogValue, setAddressDialogValue] = useState('')
  const [isCouponDialogOpen, setIsCouponDialogOpen] = useState(false)
  const [couponDialogCode, setCouponDialogCode] = useState('')

  const isUsingNewAddress = selectedAddressId === NEW_ADDRESS_VALUE
  const hasSavedAddresses = savedAddresses.length > 0
  const hasCheckoutAddress = Boolean(selectedAddress) || isUsingNewAddress

  function openAddressDialog() {
    if (!hasSavedAddresses) {
      return
    }

    setAddressDialogValue(selectedAddress?.id ?? savedAddresses[0].id)
    setIsAddressDialogOpen(true)
  }

  function closeAddressDialog() {
    setIsAddressDialogOpen(false)
  }

  function handleConfirmAddressChange() {
    if (addressDialogValue) {
      handleSelectAddressChange(addressDialogValue)
    }

    closeAddressDialog()
  }

  function handleAddAddress() {
    handleSelectAddressChange(NEW_ADDRESS_VALUE)
  }

  function handleAddAddressFromDialog() {
    handleAddAddress()
    closeAddressDialog()
  }

  function openCouponDialog() {
    setCouponDialogCode(formData.couponCode)
    setIsCouponDialogOpen(true)
  }

  function closeCouponDialog() {
    setIsCouponDialogOpen(false)
  }

  function handleApplyCouponCode(nextCouponCode: string) {
    handleCouponCodeChange(nextCouponCode.trim().toUpperCase())
    closeCouponDialog()
  }

  const addressHeaderAction = hasSavedAddresses ? (
    isUsingNewAddress ? (
      <Button type="button" variant="outline" onClick={openAddressDialog}>
        <MapPin className="size-4" />
        {labels.chooseSavedAddress}
      </Button>
    ) : (
      <Button type="button" variant="ghost" onClick={handleAddAddress}>
        <Plus className="size-4" />
        {labels.addNewAddress}
      </Button>
    )
  ) : null

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
      <main className="flex-1 bg-gradient-to-b from-background via-background to-primary/5">
        <div className="mx-auto w-full max-w-[1280px] px-4 py-10 sm:px-6 lg:px-8">
          <h1 className="mb-8 font-heading text-3xl font-bold">
            {t('checkout.title')}
          </h1>

          <form
            onSubmit={handleSubmit}
            className="grid gap-8 lg:grid-cols-[minmax(0,1fr)_420px]"
          >
            <div className="space-y-5">
              <CheckoutSection
                step="1."
                title={labels.shippingAddressTitle}
                icon={<MapPin className="size-5" />}
                action={addressHeaderAction}
              >
                {selectedAddress ? (
                  <SelectedAddressCard
                    address={selectedAddress}
                    labels={labels}
                    onChange={openAddressDialog}
                  />
                ) : isUsingNewAddress ? (
                  <NewAddressIntro labels={labels} />
                ) : (
                  <NoAddressState labels={labels} onAdd={handleAddAddress} />
                )}

                {isUsingNewAddress && (
                  <AddressFormFields
                    formData={formData}
                    onChange={handleChange}
                    t={t}
                  />
                )}
              </CheckoutSection>

              <CheckoutSection
                step="2."
                title={labels.shippingMethodTitle}
                icon={<Truck className="size-5" />}
              >
                <div className="grid gap-4 sm:grid-cols-2">
                  <CheckoutOptionCard
                    selected={shippingMethod === 'DELIVERY'}
                    icon={<Truck className="size-5" />}
                    title={labels.homeDelivery}
                    description={labels.deliveryDescription}
                    onClick={() => handleShippingMethodChange('DELIVERY')}
                  />
                  <CheckoutOptionCard
                    selected={shippingMethod === 'PICKUP'}
                    icon={<Store className="size-5" />}
                    title={labels.storePickup}
                    description={labels.pickupDescription}
                    onClick={() => handleShippingMethodChange('PICKUP')}
                  />
                </div>
              </CheckoutSection>

              <CheckoutSection
                step="3."
                title={t('checkout.couponCode')}
                icon={<Tag className="size-5" />}
              >
                <div className="flex flex-col gap-3 sm:flex-row">
                  <Input
                    id="couponCode"
                    name="couponCode"
                    value={formData.couponCode}
                    onChange={handleChange}
                    placeholder={t('checkout.couponPlaceholder')}
                    className="h-11 flex-1"
                  />
                  <Button
                    type="button"
                    variant="outline"
                    onClick={openCouponDialog}
                    className="h-11 sm:w-32"
                  >
                    <Tag className="size-4" />
                    {labels.chooseCoupon}
                  </Button>
                </div>
                {selectedCoupon && (
                  <p className="mt-2 text-sm text-primary">
                    {labels.selectedCouponPrefix} {selectedCoupon.code}
                  </p>
                )}
              </CheckoutSection>

              <CheckoutSection
                step="4."
                title={labels.noteTitle}
                icon={<Pencil className="size-5" />}
              >
                <div>
                  <Label htmlFor="note">{labels.noteLabel}</Label>
                  <Textarea
                    id="note"
                    name="note"
                    value={formData.note}
                    onChange={handleChange}
                    placeholder={labels.notePlaceholder}
                    className="mt-2 min-h-[120px] resize-none"
                  />
                </div>
              </CheckoutSection>

              <CheckoutSection
                step="5."
                title={t('checkout.paymentMethodTitle')}
                icon={<CreditCard className="size-5" />}
              >
                <div className="grid gap-4">
                  <CheckoutOptionCard
                    selected={paymentMethod === 'BANK_TRANSFER_QR'}
                    icon={<Landmark className="size-5" />}
                    title={labels.bankTransferQr}
                    description={labels.bankTransferQrDescription}
                    onClick={() => handlePaymentMethodChange('BANK_TRANSFER_QR')}
                  />
                </div>
                <p className="mt-3 text-sm text-muted-foreground">
                  {labels.paymentMethodNotice}
                </p>
              </CheckoutSection>
            </div>

            <aside className="h-fit rounded-lg border border-border bg-card p-6 shadow-sm lg:sticky lg:top-24">
              <h2 className="mb-5 font-heading text-xl font-bold">
                {t('checkout.orderSummary')}
              </h2>

              <div className="mb-6 max-h-96 space-y-4 overflow-y-auto pr-1">
                {items.map((item) => (
                  <OrderSummaryItem
                    key={item.id}
                    item={item}
                    formatCurrency={formatCurrency}
                    t={t}
                  />
                ))}
              </div>

              <div className="space-y-3 border-t border-border pt-5">
                <SummaryLine
                  label={labels.productTotal}
                  value={formatCurrency(subtotal)}
                />
                <SummaryLine
                  label={labels.shippingFeeTotal}
                  value={formatCurrency(shippingFee)}
                />
                <SummaryLine
                  label={labels.shippingDiscount}
                  value={formatDiscountValue(shippingDiscount, formatCurrency)}
                  valueClassName="text-green-600"
                />
                <SummaryLine
                  label={labels.couponDiscount}
                  value={formatDiscountValue(couponDiscount, formatCurrency)}
                  valueClassName="text-green-600"
                />
              </div>

              <div className="mt-5 flex items-center justify-between gap-4 border-t border-border pt-5">
                <span className="font-heading text-lg font-bold">
                  {t('common.total')}
                </span>
                <span className="font-heading text-2xl font-bold text-primary">
                  {formatCurrency(finalTotal)}
                </span>
              </div>

              <Button
                type="submit"
                disabled={loading || !hasCheckoutAddress}
                className="mt-6 h-12 w-full rounded-lg text-base font-bold"
              >
                <PackageCheck className="size-5" />
                {loading ? t('common.processing') : t('checkout.submit')}
              </Button>

              <p className="mt-4 flex items-center justify-center gap-2 text-center text-xs text-muted-foreground">
                <LockKeyhole className="size-4" />
                {t('cart.secureCheckout')}
              </p>
            </aside>
          </form>
        </div>
      </main>

      {isAddressDialogOpen && (
        <AddressDialog
          addresses={savedAddresses}
          value={addressDialogValue}
          labels={labels}
          onValueChange={setAddressDialogValue}
          onConfirm={handleConfirmAddressChange}
          onAddNew={handleAddAddressFromDialog}
          onClose={closeAddressDialog}
        />
      )}

      {isCouponDialogOpen && (
        <CouponDialog
          coupons={activeCoupons}
          isLoading={isCouponLoading}
          value={couponDialogCode}
          labels={labels}
          subtotal={subtotal}
          selectedCode={formData.couponCode}
          formatCurrency={formatCurrency}
          onValueChange={setCouponDialogCode}
          onApply={handleApplyCouponCode}
          onClose={closeCouponDialog}
        />
      )}

      <Footer />
    </div>
  )
}

type CheckoutLabels = ReturnType<typeof buildCheckoutLabels>

function getCheckoutLabels(language: 'vi' | 'en') {
  if (language === 'en') {
    return {
      shippingAddressTitle: 'Shipping address',
      changeAddress: 'Change',
      addAddress: 'Add address',
      addNewAddress: 'Add new address',
      chooseSavedAddress: 'Choose saved address',
      chooseAddressTitle: 'Choose shipping address',
      chooseAddressDescription: 'Only the selected address will be used for this order.',
      useThisAddress: 'Use this address',
      cancel: 'Cancel',
      close: 'Close',
      defaultAddress: 'Default',
      noAddressTitle: 'No shipping address yet',
      noAddressDescription: 'Add a shipping address before placing this order.',
      newAddressHeading: 'New shipping address',
      shippingMethodTitle: 'Shipping method',
      homeDelivery: 'Home delivery',
      deliveryEstimate: 'Estimated delivery in 2-4 business days',
      storePickup: 'Store pickup',
      pickupUnavailable: 'Currently unavailable',
      codPayment: 'Cash on delivery (COD)',
      codDescription: 'Pay when the order is delivered',
      walletPayment: 'E-wallet',
      bankPayment: 'Bank transfer',
      paymentUnavailable: 'Coming soon',
      chooseCoupon: 'Choose code',
      selectedCouponPrefix: 'Selected code:',
      productTotal: 'Product total',
      shippingFeeTotal: 'Shipping fee total',
      shippingDiscount: 'Shipping fee discount',
      couponDiscount: 'Coupon discount',
      chooseCouponTitle: 'Choose coupon code',
      couponInputPlaceholder: 'Enter coupon code',
      applyCoupon: 'Apply code',
      useCoupon: 'Use code',
      shippingCoupons: 'Shipping codes',
      bookCoupons: 'Book discount codes',
      couponLoading: 'Loading coupon codes...',
      noCoupons: 'No coupon codes available',
      couponMinOrder: 'Min order {amount}',
      couponMaxDiscount: 'Max discount {amount}',
      couponUsage: 'Used {used}/{limit}',
      couponUsageNoLimit: 'Used {used}',
      couponUnavailable: 'Order has not reached the minimum value',
    }
  }

  return {
    shippingAddressTitle: 'Địa chỉ giao hàng',
    changeAddress: 'Thay đổi',
    addAddress: 'Thêm địa chỉ',
    addNewAddress: 'Thêm địa chỉ mới',
    chooseSavedAddress: 'Chọn địa chỉ đã lưu',
    chooseAddressTitle: 'Chọn địa chỉ giao hàng',
    chooseAddressDescription: 'Chỉ địa chỉ được chọn sẽ dùng cho đơn hàng này.',
    useThisAddress: 'Dùng địa chỉ này',
    cancel: 'Hủy',
    close: 'Đóng',
    defaultAddress: 'Mặc định',
    noAddressTitle: 'Chưa có địa chỉ giao hàng',
    noAddressDescription: 'Thêm địa chỉ giao hàng trước khi đặt đơn.',
    newAddressHeading: 'Địa chỉ giao hàng mới',
    shippingMethodTitle: 'Phương thức vận chuyển',
    homeDelivery: 'Giao hàng tận nơi',
    deliveryEstimate: 'Dự kiến giao trong 2-4 ngày làm việc',
    storePickup: 'Đến cửa hàng nhận',
    pickupUnavailable: 'Hiện chưa hỗ trợ',
    codPayment: 'Thanh toán khi nhận hàng (COD)',
    codDescription: 'Thanh toán khi nhận và kiểm tra hàng',
    walletPayment: 'Ví điện tử',
    bankPayment: 'Chuyển khoản ngân hàng',
    paymentUnavailable: 'Sắp hỗ trợ',
    chooseCoupon: 'Chọn mã',
    selectedCouponPrefix: 'Mã đã chọn:',
    productTotal: 'Tổng tiền sản phẩm',
    shippingFeeTotal: 'Tổng tiền phí vận chuyển',
    shippingDiscount: 'Giảm phí vận chuyển',
    couponDiscount: 'Giảm giá mã giảm giá',
    chooseCouponTitle: 'Chọn mã giảm giá',
    couponInputPlaceholder: 'Nhập mã giảm giá',
    applyCoupon: 'Áp dụng mã',
    useCoupon: 'Dùng mã',
    shippingCoupons: 'Mã ship',
    bookCoupons: 'Mã giảm tiền sách',
    couponLoading: 'Đang tải mã giảm giá...',
    noCoupons: 'Chưa có mã giảm giá phù hợp',
    couponMinOrder: 'Đơn tối thiểu {amount}',
    couponMaxDiscount: 'Giảm tối đa {amount}',
    couponUsage: 'Đã dùng {used}/{limit}',
    couponUsageNoLimit: 'Đã dùng {used}',
    couponUnavailable: 'Đơn hàng chưa đạt giá trị tối thiểu',
  }
}

function buildCheckoutLabels(language: 'vi' | 'en') {
  if (language === 'en') {
    return {
      shippingAddressTitle: 'Shipping address',
      changeAddress: 'Change',
      addAddress: 'Add address',
      addNewAddress: 'Add new address',
      chooseSavedAddress: 'Choose saved address',
      chooseAddressTitle: 'Choose shipping address',
      chooseAddressDescription:
        'Only the selected address will be used for this order.',
      useThisAddress: 'Use this address',
      cancel: 'Cancel',
      close: 'Close',
      defaultAddress: 'Default',
      noAddressTitle: 'No shipping address yet',
      noAddressDescription: 'Add a shipping address before placing this order.',
      newAddressHeading: 'New shipping address',
      shippingMethodTitle: 'Shipping method',
      homeDelivery: 'Home delivery',
      deliveryDescription:
        'Deliver to the saved shipping contact for this order',
      storePickup: 'Store pickup',
      pickupDescription: 'Pick up at the shop while keeping your contact details',
      noteTitle: 'Note for the shop',
      noteLabel: 'Message',
      notePlaceholder:
        'Optional note about delivery time or order instructions',
      bankTransferQr: 'Bank transfer via SePay',
      bankTransferQrDescription:
        'Place the order first, then transfer with the exact content from the waiting page',
      paymentMethodNotice:
        'Frontend only reads payment status from the backend and never marks the order as paid on its own.',
      chooseCoupon: 'Choose code',
      selectedCouponPrefix: 'Selected code:',
      productTotal: 'Product total',
      shippingFeeTotal: 'Shipping fee',
      shippingDiscount: 'Shipping discount',
      couponDiscount: 'Coupon discount',
      chooseCouponTitle: 'Choose coupon code',
      couponInputPlaceholder: 'Enter coupon code',
      applyCoupon: 'Apply code',
      useCoupon: 'Use code',
      shippingCoupons: 'Shipping codes',
      bookCoupons: 'Book discount codes',
      couponLoading: 'Loading coupon codes...',
      noCoupons: 'No coupon codes available',
      couponMinOrder: 'Min order {amount}',
      couponMaxDiscount: 'Max discount {amount}',
      couponUsage: 'Used {used}/{limit}',
      couponUsageNoLimit: 'Used {used}',
      couponUnavailable: 'Order has not reached the minimum value',
    }
  }

  return {
    shippingAddressTitle: 'Dia chi giao hang',
    changeAddress: 'Thay doi',
    addAddress: 'Them dia chi',
    addNewAddress: 'Them dia chi moi',
    chooseSavedAddress: 'Chon dia chi da luu',
    chooseAddressTitle: 'Chon dia chi giao hang',
    chooseAddressDescription:
      'Chi dia chi duoc chon se duoc dung cho don hang nay.',
    useThisAddress: 'Dung dia chi nay',
    cancel: 'Huy',
    close: 'Dong',
    defaultAddress: 'Mac dinh',
    noAddressTitle: 'Chua co dia chi giao hang',
    noAddressDescription: 'Them dia chi giao hang truoc khi dat don.',
    newAddressHeading: 'Dia chi giao hang moi',
    shippingMethodTitle: 'Phuong thuc van chuyen',
    homeDelivery: 'Giao hang tan noi',
    deliveryDescription: 'Giao theo thong tin nguoi nhan da luu cho don nay',
    storePickup: 'Nhan tai cua hang',
    pickupDescription: 'Nhan tai shop nhung van giu thong tin lien he cho don',
    noteTitle: 'Loi nhan cho shop',
    noteLabel: 'Ghi chu',
    notePlaceholder: 'Them ghi chu ve thoi gian nhan hoac yeu cau cho don',
    bankTransferQr: 'Chuyen khoan SePay',
    bankTransferQrDescription:
      'Dat don truoc, sau do chuyen khoan dung noi dung o man hinh cho thanh toan',
    paymentMethodNotice:
      'Frontend chi doc trang thai thanh toan tu backend va khong tu dong danh dau da thanh toan.',
    chooseCoupon: 'Chon ma',
    selectedCouponPrefix: 'Ma da chon:',
    productTotal: 'Tong tien san pham',
    shippingFeeTotal: 'Phi van chuyen',
    shippingDiscount: 'Giam phi van chuyen',
    couponDiscount: 'Giam gia ma giam gia',
    chooseCouponTitle: 'Chon ma giam gia',
    couponInputPlaceholder: 'Nhap ma giam gia',
    applyCoupon: 'Ap dung ma',
    useCoupon: 'Dung ma',
    shippingCoupons: 'Ma ship',
    bookCoupons: 'Ma giam tien sach',
    couponLoading: 'Dang tai ma giam gia...',
    noCoupons: 'Chua co ma giam gia phu hop',
    couponMinOrder: 'Don toi thieu {amount}',
    couponMaxDiscount: 'Giam toi da {amount}',
    couponUsage: 'Da dung {used}/{limit}',
    couponUsageNoLimit: 'Da dung {used}',
    couponUnavailable: 'Don hang chua dat gia tri toi thieu',
  }
}

function CheckoutSection({
  step,
  title,
  icon,
  action,
  children,
}: {
  step: string
  title: string
  icon: ReactNode
  action?: ReactNode
  children: ReactNode
}) {
  return (
    <section className="rounded-lg border border-border bg-card p-5 shadow-sm">
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h2 className="flex items-center gap-2 font-heading text-lg font-bold">
          <span className="text-primary">{icon}</span>
          <span>{step}</span>
          <span>{title}</span>
        </h2>
        {action}
      </div>
      {children}
    </section>
  )
}

function SelectedAddressCard({
  address,
  labels,
  onChange,
}: {
  address: UserAddressResponse
  labels: CheckoutLabels
  onChange: () => void
}) {
  return (
    <div className="rounded-lg border border-primary/45 bg-primary/5 p-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex min-w-0 gap-3">
          <span className="mt-1 flex size-9 shrink-0 items-center justify-center rounded-full bg-primary text-primary-foreground">
            <MapPin className="size-4" />
          </span>
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <p className="font-semibold">{address.receiverName}</p>
              {address.defaultAddress && (
                <Badge variant="secondary">{labels.defaultAddress}</Badge>
              )}
            </div>
            <p className="mt-1 text-sm text-muted-foreground">
              {address.receiverPhone}
            </p>
            <p className="mt-1 text-sm leading-6 text-muted-foreground">
              {address.receiverAddress}
            </p>
          </div>
        </div>

        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={onChange}
          className="self-start text-primary hover:text-primary"
        >
          <Pencil className="size-4" />
          {labels.changeAddress}
        </Button>
      </div>
    </div>
  )
}

function NoAddressState({
  labels,
  onAdd,
}: {
  labels: CheckoutLabels
  onAdd: () => void
}) {
  return (
    <div className="rounded-lg border border-dashed border-border bg-muted/30 p-5">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="font-semibold">{labels.noAddressTitle}</p>
          <p className="mt-1 text-sm text-muted-foreground">
            {labels.noAddressDescription}
          </p>
        </div>
        <Button type="button" onClick={onAdd}>
          <Plus className="size-4" />
          {labels.addAddress}
        </Button>
      </div>
    </div>
  )
}

function NewAddressIntro({ labels }: { labels: CheckoutLabels }) {
  return (
    <div className="mb-4 rounded-lg border border-primary/35 bg-primary/5 p-4">
      <p className="font-semibold">{labels.newAddressHeading}</p>
      <p className="mt-1 text-sm text-muted-foreground">
        {labels.noAddressDescription}
      </p>
    </div>
  )
}

function AddressFormFields({
  formData,
  onChange,
  t,
}: {
  formData: CheckoutAddressFormData
  onChange: (event: ChangeEvent<HTMLInputElement>) => void
  t: (key: string, values?: Record<string, string | number>) => string
}) {
  return (
    <div className="grid gap-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <Label htmlFor="fullName">{t('checkout.fullName')}</Label>
          <Input
            id="fullName"
            name="fullName"
            value={formData.fullName}
            onChange={onChange}
            required
            className="mt-2 h-11"
          />
        </div>
        <div>
          <Label htmlFor="phone">{t('common.phone')}</Label>
          <Input
            id="phone"
            name="phone"
            value={formData.phone}
            onChange={onChange}
            required
            className="mt-2 h-11"
          />
        </div>
      </div>

      <div>
        <Label htmlFor="address">{t('checkout.address')}</Label>
        <Input
          id="address"
          name="address"
          value={formData.address}
          onChange={onChange}
          required
          className="mt-2 h-11"
        />
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <div>
          <Label htmlFor="city">{t('checkout.city')}</Label>
          <Input
            id="city"
            name="city"
            value={formData.city}
            onChange={onChange}
            required
            className="mt-2 h-11"
          />
        </div>
        <div>
          <Label htmlFor="district">{t('checkout.district')}</Label>
          <Input
            id="district"
            name="district"
            value={formData.district}
            onChange={onChange}
            required
            className="mt-2 h-11"
          />
        </div>
        <div>
          <Label htmlFor="ward">{t('checkout.ward')}</Label>
          <Input
            id="ward"
            name="ward"
            value={formData.ward}
            onChange={onChange}
            required
            className="mt-2 h-11"
          />
        </div>
      </div>
    </div>
  )
}

function CheckoutOptionCard({
  title,
  description,
  icon,
  selected = false,
  disabled = false,
  onClick,
}: {
  title: string
  description: string
  icon: ReactNode
  selected?: boolean
  disabled?: boolean
  onClick?: () => void
}) {
  const className = cn(
    'flex min-h-[76px] items-center gap-3 rounded-lg border p-4 text-left transition-colors',
    selected ? 'border-primary/60 bg-primary/5' : 'border-border bg-background',
    disabled && 'cursor-not-allowed opacity-60',
    onClick && !disabled && 'cursor-pointer hover:border-primary/40',
  )

  const content = (
    <>
      <span
        className={cn(
          'flex size-5 shrink-0 items-center justify-center rounded-full border',
          selected ? 'border-primary' : 'border-border',
        )}
      >
        {selected && <span className="size-2.5 rounded-full bg-primary" />}
      </span>
      <div className="min-w-0 flex-1">
        <p className="text-sm font-semibold">{title}</p>
        <p className="mt-1 text-xs text-muted-foreground">{description}</p>
      </div>
      <span className="text-primary">{icon}</span>
    </>
  )

  if (onClick) {
    return (
      <button
        type="button"
        disabled={disabled}
        onClick={onClick}
        className={className}
      >
        {content}
      </button>
    )
  }

  return <div className={className}>{content}</div>
}

function OrderSummaryItem({
  item,
  formatCurrency,
  t,
}: {
  item: CartItem
  formatCurrency: (value: number) => string
  t: (key: string, values?: Record<string, string | number>) => string
}) {
  return (
    <div className="grid grid-cols-[64px_minmax(0,1fr)_auto] gap-3 text-sm">
      <Link
        to={`/books/${item.bookId}`}
        className="relative block size-16 overflow-hidden rounded-lg bg-muted"
      >
        <img
          src={getBookCoverUrl(item.cover)}
          alt={item.title}
          className="size-full object-cover"
        />
        <span className="absolute -right-1 -top-1 flex size-5 items-center justify-center rounded-full bg-primary text-xs font-bold text-primary-foreground">
          {item.qty}
        </span>
      </Link>

      <div className="min-w-0">
        <Link to={`/books/${item.bookId}`}>
          <p className="line-clamp-2 font-semibold hover:text-primary">
            {item.title}
          </p>
        </Link>
        <p className="mt-1 text-muted-foreground">
          {t('checkout.quantityShort', { count: item.qty })}
        </p>
      </div>

      <p className="whitespace-nowrap font-semibold">
        {formatCurrency(item.lineTotal)}
      </p>
    </div>
  )
}

function SummaryLine({
  label,
  value,
  valueClassName,
}: {
  label: string
  value: string
  valueClassName?: string
}) {
  return (
    <div className="flex items-center justify-between gap-4 text-sm">
      <span className="text-muted-foreground">{label}</span>
      <span className={cn('font-semibold', valueClassName)}>{value}</span>
    </div>
  )
}

function formatDiscountValue(
  amount: number,
  formatCurrency: (value: number) => string,
) {
  if (amount <= 0) {
    return formatCurrency(0)
  }

  return `- ${formatCurrency(amount)}`
}

function CouponDialog({
  coupons,
  isLoading,
  value,
  labels,
  subtotal,
  selectedCode,
  formatCurrency,
  onValueChange,
  onApply,
  onClose,
}: {
  coupons: CouponResponse[]
  isLoading: boolean
  value: string
  labels: CheckoutLabels
  subtotal: number
  selectedCode: string
  formatCurrency: (value: number) => string
  onValueChange: (value: string) => void
  onApply: (value: string) => void
  onClose: () => void
}) {
  const shippingCoupons = coupons.filter(isShippingCoupon)
  const bookCoupons = coupons.filter((coupon) => !isShippingCoupon(coupon))

  return (
    <div className="fixed inset-0 z-[120] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={labels.close}
        onClick={onClose}
        className="absolute inset-0 bg-slate-950/55 backdrop-blur-sm"
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="checkout-coupon-dialog-title"
        className="relative z-10 flex max-h-full w-full max-w-3xl flex-col overflow-hidden rounded-lg border border-border bg-background shadow-[0_30px_90px_rgba(15,23,42,0.28)]"
      >
        <div className="flex items-start justify-between gap-4 border-b border-border px-5 py-4">
          <div>
            <h3
              id="checkout-coupon-dialog-title"
              className="font-heading text-xl font-bold"
            >
              {labels.chooseCouponTitle}
            </h3>
          </div>
          <button
            type="button"
            aria-label={labels.close}
            onClick={onClose}
            className="flex size-9 shrink-0 items-center justify-center rounded-full border border-border bg-card text-foreground transition hover:border-primary/40 hover:text-primary"
          >
            <X className="size-4" />
          </button>
        </div>

        <div className="border-b border-border px-5 py-4">
          <div className="flex flex-col gap-3 sm:flex-row">
            <Input
              value={value}
              onChange={(event) => onValueChange(event.currentTarget.value)}
              placeholder={labels.couponInputPlaceholder}
              className="h-11 flex-1"
            />
            <Button
              type="button"
              onClick={() => onApply(value)}
              disabled={value.trim() === ''}
              className="h-11 sm:w-36"
            >
              <Tag className="size-4" />
              {labels.applyCoupon}
            </Button>
          </div>
        </div>

        <div className="space-y-6 overflow-y-auto px-5 py-5">
          {isLoading ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              {labels.couponLoading}
            </p>
          ) : (
            <>
              <CouponListSection
                title={labels.shippingCoupons}
                coupons={shippingCoupons}
                subtotal={subtotal}
                selectedCode={selectedCode}
                labels={labels}
                formatCurrency={formatCurrency}
                onApply={onApply}
              />
              <CouponListSection
                title={labels.bookCoupons}
                coupons={bookCoupons}
                subtotal={subtotal}
                selectedCode={selectedCode}
                labels={labels}
                formatCurrency={formatCurrency}
                onApply={onApply}
              />
            </>
          )}
        </div>
      </div>
    </div>
  )
}

function CouponListSection({
  title,
  coupons,
  subtotal,
  selectedCode,
  labels,
  formatCurrency,
  onApply,
}: {
  title: string
  coupons: CouponResponse[]
  subtotal: number
  selectedCode: string
  labels: CheckoutLabels
  formatCurrency: (value: number) => string
  onApply: (value: string) => void
}) {
  return (
    <section>
      <h4 className="mb-3 flex items-center gap-2 font-heading text-base font-bold">
        <Tag className="size-4 text-primary" />
        {title}
      </h4>

      {coupons.length === 0 ? (
        <div className="rounded-lg border border-dashed border-border bg-muted/30 px-4 py-5 text-center text-sm text-muted-foreground">
          {labels.noCoupons}
        </div>
      ) : (
        <div className="space-y-3">
          {coupons.map((coupon) => (
            <CouponCard
              key={coupon.id}
              coupon={coupon}
              disabled={subtotal < coupon.minOrderAmount}
              selected={
                selectedCode.trim().toUpperCase() === coupon.code.toUpperCase()
              }
              labels={labels}
              formatCurrency={formatCurrency}
              onApply={() => onApply(coupon.code)}
            />
          ))}
        </div>
      )}
    </section>
  )
}

function CouponCard({
  coupon,
  disabled,
  selected,
  labels,
  formatCurrency,
  onApply,
}: {
  coupon: CouponResponse
  disabled: boolean
  selected: boolean
  labels: CheckoutLabels
  formatCurrency: (value: number) => string
  onApply: () => void
}) {
  return (
    <div
      className={cn(
        'flex flex-col gap-4 rounded-lg border p-4 transition-colors sm:flex-row sm:items-center sm:justify-between',
        selected ? 'border-primary/60 bg-primary/5' : 'border-border bg-card',
        disabled && 'opacity-60',
      )}
    >
      <div className="min-w-0">
        <div className="flex flex-wrap items-center gap-2">
          <Badge variant={selected ? 'default' : 'outline'}>{coupon.code}</Badge>
          <span className="font-semibold text-primary">
            {formatCouponValue(coupon, formatCurrency)}
          </span>
        </div>
        {coupon.description && (
          <p className="mt-2 text-sm text-muted-foreground">
            {coupon.description}
          </p>
        )}
        <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
          <span>
            {labels.couponMinOrder.replace(
              '{amount}',
              formatCurrency(coupon.minOrderAmount),
            )}
          </span>
          {coupon.maxDiscountAmount !== null && (
            <span>
              {labels.couponMaxDiscount.replace(
                '{amount}',
                formatCurrency(coupon.maxDiscountAmount),
              )}
            </span>
          )}
          <span>
            {coupon.maxUsageCount === null
              ? labels.couponUsageNoLimit.replace(
                  '{used}',
                  String(coupon.usedCount),
                )
              : labels.couponUsage
                  .replace('{used}', String(coupon.usedCount))
                  .replace('{limit}', String(coupon.maxUsageCount))}
          </span>
        </div>
        {disabled && (
          <p className="mt-2 text-xs font-medium text-amber-600">
            {labels.couponUnavailable}
          </p>
        )}
      </div>

      <Button
        type="button"
        variant={selected ? 'default' : 'outline'}
        onClick={onApply}
        disabled={disabled}
        className="sm:w-28"
      >
        {labels.useCoupon}
      </Button>
    </div>
  )
}

function isShippingCoupon(coupon: CouponResponse) {
  const text = normalizeCouponText(`${coupon.code} ${coupon.description ?? ''}`)
  return ['ship', 'shipping', 'freeship', 'free ship', 'giao hang', 'van chuyen'].some(
    (keyword) => text.includes(keyword),
  )
}

function normalizeCouponText(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}

function formatCouponValue(
  coupon: CouponResponse,
  formatCurrency: (value: number) => string,
) {
  return coupon.discountType === 'PERCENTAGE'
    ? `${coupon.discountValue}%`
    : formatCurrency(coupon.discountValue)
}

function AddressDialog({
  addresses,
  value,
  labels,
  onValueChange,
  onConfirm,
  onAddNew,
  onClose,
}: {
  addresses: UserAddressResponse[]
  value: string
  labels: CheckoutLabels
  onValueChange: (value: string) => void
  onConfirm: () => void
  onAddNew: () => void
  onClose: () => void
}) {
  return (
    <div className="fixed inset-0 z-[120] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={labels.close}
        onClick={onClose}
        className="absolute inset-0 bg-slate-950/55 backdrop-blur-sm"
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="checkout-address-dialog-title"
        className="relative z-10 flex max-h-full w-full max-w-2xl flex-col overflow-hidden rounded-lg border border-border bg-background shadow-[0_30px_90px_rgba(15,23,42,0.28)]"
      >
        <div className="flex items-start justify-between gap-4 border-b border-border px-5 py-4">
          <div>
            <h3
              id="checkout-address-dialog-title"
              className="font-heading text-xl font-bold"
            >
              {labels.chooseAddressTitle}
            </h3>
            <p className="mt-1 text-sm text-muted-foreground">
              {labels.chooseAddressDescription}
            </p>
          </div>
          <button
            type="button"
            aria-label={labels.close}
            onClick={onClose}
            className="flex size-9 shrink-0 items-center justify-center rounded-full border border-border bg-card text-foreground transition hover:border-primary/40 hover:text-primary"
          >
            <X className="size-4" />
          </button>
        </div>

        <div className="space-y-3 overflow-y-auto px-5 py-5">
          {addresses.map((address) => (
            <label
              key={address.id}
              className={cn(
                'flex cursor-pointer items-start gap-3 rounded-lg border p-4 transition-colors hover:bg-muted/40',
                value === address.id
                  ? 'border-primary/60 bg-primary/5'
                  : 'border-border bg-background',
              )}
            >
              <input
                type="radio"
                name="checkoutAddress"
                value={address.id}
                checked={value === address.id}
                onChange={(event) =>
                  onValueChange(event.currentTarget.value)
                }
                className="mt-1 size-4 accent-primary"
              />
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <p className="font-semibold">{address.receiverName}</p>
                  {address.defaultAddress && (
                    <Badge variant="secondary">{labels.defaultAddress}</Badge>
                  )}
                </div>
                <p className="mt-1 text-sm text-muted-foreground">
                  {address.receiverPhone}
                </p>
                <p className="mt-1 text-sm leading-6 text-muted-foreground">
                  {address.receiverAddress}
                </p>
              </div>
            </label>
          ))}
        </div>

        <div className="flex flex-col-reverse gap-3 border-t border-border px-5 py-4 sm:flex-row sm:justify-between">
          <Button type="button" variant="outline" onClick={onAddNew}>
            <Plus className="size-4" />
            {labels.addNewAddress}
          </Button>
          <div className="flex gap-3">
            <Button type="button" variant="ghost" onClick={onClose}>
              {labels.cancel}
            </Button>
            <Button type="button" onClick={onConfirm} disabled={!value}>
              <MapPin className="size-4" />
              {labels.useThisAddress}
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}

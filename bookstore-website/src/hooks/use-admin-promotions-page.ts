import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminPromotion,
  deleteAdminPromotion,
  getAdminPromotions,
  updateAdminPromotion,
} from '@/services/admin-access-service'
import type {
  AdminPromotionMutationRequest,
  AdminPromotionResponse,
} from '@/types/admin-access'
import type { CouponDiscountType, CouponType } from '@/types/coupon'
import { getErrorMessage } from '@/utils'

type PromotionDialogMode = 'create' | 'view' | 'edit' | 'delete'

export type PromotionFormState = {
  code: string
  description: string
  couponType: CouponType
  discountType: CouponDiscountType
  discountValue: string
  minOrderAmount: string
  maxDiscountAmount: string
  maxUsageCount: string
  startsAt: string
  expiresAt: string
  active: boolean
}

type PromotionFieldName = keyof PromotionFormState

export type PromotionFormErrors = Partial<Record<PromotionFieldName, string>>

const promotionFieldNames: PromotionFieldName[] = [
  'code',
  'description',
  'couponType',
  'discountType',
  'discountValue',
  'minOrderAmount',
  'maxDiscountAmount',
  'maxUsageCount',
  'startsAt',
  'expiresAt',
  'active',
]

export function useAdminPromotionsPage() {
  const { language, t, formatCurrency, formatDate, formatNumber } =
    useLanguage()
  const isVietnamese = language === 'vi'
  const [promotions, setPromotions] = useState<AdminPromotionResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] = useState<PromotionDialogMode | null>(null)
  const [selectedPromotion, setSelectedPromotion] =
    useState<AdminPromotionResponse | null>(null)
  const [form, setForm] = useState<PromotionFormState>(createInitialFormState)
  const [touchedFields, setTouchedFields] = useState<
    Partial<Record<PromotionFieldName, boolean>>
  >({})
  const [hasAttemptedSubmit, setHasAttemptedSubmit] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  const labels = useMemo(
    () => ({
      addPromotion: isVietnamese ? 'Tao coupon' : 'Create coupon',
      detailTitle: isVietnamese ? 'Chi tiet coupon' : 'Coupon details',
      editTitle: isVietnamese ? 'Sua coupon' : 'Edit coupon',
      deleteTitle: isVietnamese
        ? 'Xac nhan xoa coupon'
        : 'Confirm coupon deletion',
      deleteDescription: isVietnamese
        ? 'Coupon nay se bi xoa khoi he thong va khong the hoan tac.'
        : 'This coupon will be removed from the system and cannot be undone.',
      createSuccess: isVietnamese ? 'Da tao coupon' : 'Coupon created successfully',
      updateSuccess: isVietnamese
        ? 'Da cap nhat coupon'
        : 'Coupon updated successfully',
      deleteSuccess: isVietnamese ? 'Da xoa coupon' : 'Coupon deleted successfully',
      loadError: isVietnamese
        ? 'Khong tai duoc danh sach coupon'
        : 'Unable to load coupon list',
      saveError: isVietnamese ? 'Khong luu duoc coupon' : 'Unable to save coupon',
      deleteError: isVietnamese
        ? 'Khong xoa duoc coupon'
        : 'Unable to delete coupon',
      showingCount: isVietnamese
        ? 'Hien thi {count} tren {total} coupon'
        : 'Showing {count} of {total} coupons',
      codeLabel: isVietnamese ? 'Ma coupon' : 'Coupon code',
      descriptionLabel: isVietnamese ? 'Mo ta' : 'Description',
      couponTypeLabel: isVietnamese ? 'Loai coupon' : 'Coupon type',
      discountTypeLabel: isVietnamese ? 'Kieu giam gia' : 'Discount type',
      discountValueLabel: isVietnamese ? 'Gia tri giam' : 'Discount value',
      minOrderAmountLabel: isVietnamese
        ? 'Don toi thieu'
        : 'Minimum order amount',
      maxDiscountAmountLabel: isVietnamese
        ? 'Giam toi da'
        : 'Maximum discount amount',
      maxUsageCountLabel: isVietnamese
        ? 'Luot dung toi da'
        : 'Maximum usage count',
      startsAtLabel: isVietnamese ? 'Bat dau luc' : 'Starts at',
      expiresAtLabel: isVietnamese ? 'Het han luc' : 'Expires at',
      activeLabel: isVietnamese ? 'Dang kich hoat' : 'Active',
      noDescription: isVietnamese ? 'Chua co mo ta' : 'No description',
      noLimit: isVietnamese ? 'Khong gioi han' : 'No limit',
      noMaxDiscount: isVietnamese ? 'Khong gioi han' : 'No cap',
      codeHint: isVietnamese
        ? 'Tu dong viet hoa va bo khoang trang.'
        : 'Automatically uppercased with spaces removed.',
      percentageHint: isVietnamese
        ? 'Coupon phan tram chi nhan gia tri tu 0 den 100.'
        : 'Percentage coupons only accept values from 0 to 100.',
      startsAtHint: isVietnamese
        ? 'Thoi gian tinh theo gio may hien tai.'
        : 'Time is based on the current device timezone.',
      expiresAtHint: isVietnamese
        ? 'Phai sau thoi diem bat dau.'
        : 'Must be later than the start time.',
      invalidForm: isVietnamese
        ? 'Vui long kiem tra lai thong tin coupon.'
        : 'Please review the coupon details.',
      codeRequired: isVietnamese
        ? 'Vui long nhap ma coupon.'
        : 'Coupon code is required.',
      discountValueRequired: isVietnamese
        ? 'Vui long nhap gia tri giam.'
        : 'Discount value is required.',
      discountValuePositive: isVietnamese
        ? 'Gia tri giam phai lon hon 0.'
        : 'Discount value must be greater than 0.',
      discountValuePercentageMax: isVietnamese
        ? 'Gia tri giam theo phan tram khong duoc vuot qua 100.'
        : 'Percentage discount cannot exceed 100.',
      minOrderAmountInvalid: isVietnamese
        ? 'Don toi thieu phai tu 0 tro len.'
        : 'Minimum order amount must be 0 or greater.',
      maxDiscountAmountInvalid: isVietnamese
        ? 'Giam toi da phai lon hon 0 neu duoc nhap.'
        : 'Maximum discount amount must be greater than 0 when provided.',
      maxUsageCountInvalid: isVietnamese
        ? 'Luot dung toi da phai la so nguyen duong.'
        : 'Maximum usage count must be a positive integer.',
      startsAtInvalid: isVietnamese
        ? 'Vui long chon thoi diem bat dau hop le.'
        : 'Please choose a valid start time.',
      expiresAtInvalid: isVietnamese
        ? 'Vui long chon thoi diem het han hop le.'
        : 'Please choose a valid expiration time.',
      expiresAtAfterStartsAt: isVietnamese
        ? 'Thoi diem het han phai sau thoi diem bat dau.'
        : 'Expiration time must be later than the start time.',
      deleteBlockedShort: isVietnamese
        ? 'Da phat sinh luot dung'
        : 'Already used',
      deleteBlockedReason: isVietnamese
        ? 'Coupon da co luot su dung, khong the xoa.'
        : 'This coupon already has usage history and cannot be deleted.',
      formDescription: isVietnamese
        ? 'Cap nhat thong tin ma giam gia va lich ap dung.'
        : 'Update the coupon information and active schedule.',
    }),
    [isVietnamese],
  )

  const formErrors = useMemo(
    () => validatePromotionForm(form, labels),
    [form, labels],
  )

  const visibleFormErrors = useMemo(() => {
    return promotionFieldNames.reduce<PromotionFormErrors>((errors, field) => {
      if ((hasAttemptedSubmit || touchedFields[field]) && formErrors[field]) {
        errors[field] = formErrors[field]
      }

      return errors
    }, {})
  }, [formErrors, hasAttemptedSubmit, touchedFields])

  const filteredPromotions = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return promotions
    }

    return promotions.filter((promotion) =>
      [
        promotion.code,
        promotion.description ?? '',
        promotion.couponType,
        promotion.discountType,
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [promotions, searchTerm])

  useEffect(() => {
    let isCancelled = false

    async function loadPromotions() {
      setIsLoading(true)

      try {
        const response = await getAdminPromotions()

        if (isCancelled) {
          return
        }

        setPromotions(response)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setPromotions([])
        setError(getErrorMessage(currentError, labels.loadError))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadPromotions()

    return () => {
      isCancelled = true
    }
  }, [labels.loadError])

  useEffect(() => {
    if (!dialogMode) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !(dialogMode === 'delete' && isDeleting)) {
        closeDialog()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [dialogMode, isDeleting])

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function resetDialog() {
    setDialogMode(null)
    setSelectedPromotion(null)
    setForm(createInitialFormState())
    setTouchedFields({})
    setHasAttemptedSubmit(false)
  }

  function closeDialog() {
    if (isSubmitting || isDeleting) {
      return
    }

    resetDialog()
  }

  function openCreateDialog() {
    setSelectedPromotion(null)
    setForm(createInitialFormState())
    setTouchedFields({})
    setHasAttemptedSubmit(false)
    setDialogMode('create')
  }

  function openViewDialog(promotion: AdminPromotionResponse) {
    setSelectedPromotion(promotion)
    setDialogMode('view')
  }

  function openEditDialog(promotion: AdminPromotionResponse) {
    setSelectedPromotion(promotion)
    setForm(mapPromotionToFormState(promotion))
    setTouchedFields({})
    setHasAttemptedSubmit(false)
    setDialogMode('edit')
  }

  function openEditFromView() {
    if (!selectedPromotion) {
      return
    }

    openEditDialog(selectedPromotion)
  }

  function openDeleteDialog(promotion: AdminPromotionResponse) {
    if (isPromotionDeletionBlocked(promotion)) {
      toast.error(labels.deleteBlockedReason)
      return
    }

    setSelectedPromotion(promotion)
    setDialogMode('delete')
  }

  function handleFormChange(
    field: PromotionFieldName,
    value: string | boolean,
  ) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: normalizeFormFieldValue(field, value),
    }))
  }

  function handleFieldBlur(field: PromotionFieldName) {
    setTouchedFields((currentTouchedFields) => ({
      ...currentTouchedFields,
      [field]: true,
    }))
  }

  async function reloadPromotions() {
    setIsLoading(true)

    try {
      const response = await getAdminPromotions()
      setPromotions(response)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, labels.loadError))
    } finally {
      setIsLoading(false)
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setHasAttemptedSubmit(true)
    setTouchedFields(createTouchedFieldState())

    if (Object.keys(validatePromotionForm(form, labels)).length > 0) {
      toast.error(labels.invalidForm)
      return
    }

    setIsSubmitting(true)

    try {
      const payload = buildPromotionPayload(form)

      if (dialogMode === 'edit' && selectedPromotion) {
        await updateAdminPromotion(selectedPromotion.id, payload)
        toast.success(labels.updateSuccess)
      } else {
        await createAdminPromotion(payload)
        toast.success(labels.createSuccess)
      }

      await reloadPromotions()
      resetDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.saveError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteConfirm() {
    if (!selectedPromotion) {
      return
    }

    if (isPromotionDeletionBlocked(selectedPromotion)) {
      toast.error(labels.deleteBlockedReason)
      return
    }

    setIsDeleting(true)

    try {
      await deleteAdminPromotion(selectedPromotion.id)
      await reloadPromotions()
      resetDialog()
      toast.success(labels.deleteSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.deleteError))
    } finally {
      setIsDeleting(false)
    }
  }

  return {
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    promotions,
    filteredPromotions,
    searchTerm,
    isLoading,
    error,
    dialogMode,
    selectedPromotion,
    form,
    formErrors: visibleFormErrors,
    isSubmitting,
    isDeleting,
    labels,
    isDialogLocked: dialogMode === 'delete' && isDeleting,
    handleSearchTermChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    openEditDialog,
    openEditFromView,
    openDeleteDialog,
    handleFormChange,
    handleFieldBlur,
    handleSubmit,
    handleDeleteConfirm,
    getDeleteBlockedReason: (
      promotion: AdminPromotionResponse,
    ): string | null => {
      return isPromotionDeletionBlocked(promotion)
        ? labels.deleteBlockedReason
        : null
    },
  }
}

function createInitialFormState(): PromotionFormState {
  const now = new Date()
  const nextWeek = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)

  return {
    code: '',
    description: '',
    couponType: 'BOOK',
    discountType: 'PERCENTAGE',
    discountValue: '',
    minOrderAmount: '0',
    maxDiscountAmount: '',
    maxUsageCount: '',
    startsAt: toDateTimeLocalValue(now.toISOString()),
    expiresAt: toDateTimeLocalValue(nextWeek.toISOString()),
    active: true,
  }
}

function mapPromotionToFormState(
  promotion: AdminPromotionResponse,
): PromotionFormState {
  return {
    code: promotion.code,
    description: promotion.description ?? '',
    couponType: promotion.couponType,
    discountType: promotion.discountType,
    discountValue: String(promotion.discountValue),
    minOrderAmount: String(promotion.minOrderAmount),
    maxDiscountAmount:
      promotion.maxDiscountAmount === null
        ? ''
        : String(promotion.maxDiscountAmount),
    maxUsageCount:
      promotion.maxUsageCount === null ? '' : String(promotion.maxUsageCount),
    startsAt: toDateTimeLocalValue(promotion.startsAt),
    expiresAt: toDateTimeLocalValue(promotion.expiresAt),
    active: promotion.active,
  }
}

function buildPromotionPayload(
  form: PromotionFormState,
): AdminPromotionMutationRequest {
  return {
    code: form.code.trim().toUpperCase(),
    description: normalizeOptionalText(form.description),
    couponType: form.couponType,
    discountType: form.discountType,
    discountValue: Number(form.discountValue),
    minOrderAmount: Number(form.minOrderAmount),
    maxDiscountAmount: normalizeOptionalNumber(form.maxDiscountAmount),
    maxUsageCount: normalizeOptionalInteger(form.maxUsageCount),
    startsAt: new Date(form.startsAt).toISOString(),
    expiresAt: new Date(form.expiresAt).toISOString(),
    active: form.active,
  }
}

function normalizeOptionalText(value: string) {
  const normalizedValue = value.trim()
  return normalizedValue === '' ? null : normalizedValue
}

function normalizeOptionalNumber(value: string) {
  const normalizedValue = value.trim()
  return normalizedValue === '' ? null : Number(normalizedValue)
}

function normalizeOptionalInteger(value: string) {
  const normalizedValue = value.trim()
  return normalizedValue === '' ? null : Number.parseInt(normalizedValue, 10)
}

function normalizeFormFieldValue(
  field: PromotionFieldName,
  value: string | boolean,
) {
  if (field === 'code' && typeof value === 'string') {
    return value.toUpperCase().replace(/\s+/g, '')
  }

  return value
}

function validatePromotionForm(
  form: PromotionFormState,
  labels: {
    codeRequired: string
    discountValuePercentageMax: string
    discountValuePositive: string
    discountValueRequired: string
    expiresAtAfterStartsAt: string
    expiresAtInvalid: string
    maxDiscountAmountInvalid: string
    maxUsageCountInvalid: string
    minOrderAmountInvalid: string
    startsAtInvalid: string
  },
): PromotionFormErrors {
  const errors: PromotionFormErrors = {}

  if (form.code.trim() === '') {
    errors.code = labels.codeRequired
  }

  const discountValue = parseRequiredNumber(form.discountValue)
  if (discountValue === null) {
    errors.discountValue = labels.discountValueRequired
  } else if (discountValue <= 0) {
    errors.discountValue = labels.discountValuePositive
  } else if (
    form.discountType === 'PERCENTAGE' &&
    discountValue > 100
  ) {
    errors.discountValue = labels.discountValuePercentageMax
  }

  const minOrderAmount = parseRequiredNumber(form.minOrderAmount)
  if (minOrderAmount === null || minOrderAmount < 0) {
    errors.minOrderAmount = labels.minOrderAmountInvalid
  }

  if (
    form.maxDiscountAmount.trim() !== '' &&
    !isPositiveNumber(form.maxDiscountAmount)
  ) {
    errors.maxDiscountAmount = labels.maxDiscountAmountInvalid
  }

  if (
    form.maxUsageCount.trim() !== '' &&
    !isPositiveInteger(form.maxUsageCount)
  ) {
    errors.maxUsageCount = labels.maxUsageCountInvalid
  }

  const startsAt = parseDateTimeValue(form.startsAt)
  const expiresAt = parseDateTimeValue(form.expiresAt)

  if (!startsAt) {
    errors.startsAt = labels.startsAtInvalid
  }

  if (!expiresAt) {
    errors.expiresAt = labels.expiresAtInvalid
  }

  if (
    startsAt &&
    expiresAt &&
    expiresAt.getTime() <= startsAt.getTime()
  ) {
    errors.expiresAt = labels.expiresAtAfterStartsAt
  }

  return errors
}

function createTouchedFieldState() {
  return promotionFieldNames.reduce<Record<PromotionFieldName, boolean>>(
    (currentState, field) => {
      currentState[field] = true
      return currentState
    },
    {} as Record<PromotionFieldName, boolean>,
  )
}

function parseRequiredNumber(value: string) {
  const normalizedValue = value.trim()
  if (normalizedValue === '') {
    return null
  }

  const parsedValue = Number(normalizedValue)
  return Number.isFinite(parsedValue) ? parsedValue : null
}

function isPositiveNumber(value: string) {
  const parsedValue = parseRequiredNumber(value)
  return parsedValue !== null && parsedValue > 0
}

function isPositiveInteger(value: string) {
  const normalizedValue = value.trim()
  if (normalizedValue === '') {
    return false
  }

  const parsedValue = Number.parseInt(normalizedValue, 10)
  return String(parsedValue) === normalizedValue && parsedValue > 0
}

function parseDateTimeValue(value: string) {
  const parsedValue = new Date(value)
  return Number.isNaN(parsedValue.getTime()) ? null : parsedValue
}

function isPromotionDeletionBlocked(promotion: AdminPromotionResponse) {
  return promotion.usedCount > 0
}

function toDateTimeLocalValue(value: string) {
  const date = new Date(value)
  const offset = date.getTimezoneOffset()
  const localDate = new Date(date.getTime() - offset * 60 * 1000)
  return localDate.toISOString().slice(0, 16)
}

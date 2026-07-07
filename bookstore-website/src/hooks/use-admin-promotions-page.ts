import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminPromotion,
  deleteAdminPromotion,
  getAdminPromotionsPage,
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

const PAGE_SIZE = 10

export function useAdminPromotionsPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [promotions, setPromotions] = useState<AdminPromotionResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
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
      addPromotion: t('admin.promotionsPage.addPromotion'),
      detailTitle: t('admin.promotionsPage.detailTitle'),
      editTitle: t('admin.promotionsPage.editTitle'),
      deleteTitle: t('admin.promotionsPage.deleteTitle'),
      deleteDescription: t('admin.promotionsPage.deleteDescription'),
      createSuccess: t('admin.promotionsPage.createSuccess'),
      updateSuccess: t('admin.promotionsPage.updateSuccess'),
      deleteSuccess: t('admin.promotionsPage.deleteSuccess'),
      loadError: t('admin.promotionsPage.loadError'),
      saveError: t('admin.promotionsPage.saveError'),
      deleteError: t('admin.promotionsPage.deleteError'),
      showingCount: t('admin.promotionsPage.showingCount'),
      codeLabel: t('admin.promotionsPage.codeLabel'),
      descriptionLabel: t('admin.promotionsPage.descriptionLabel'),
      couponTypeLabel: t('admin.promotionsPage.couponTypeLabel'),
      discountTypeLabel: t('admin.promotionsPage.discountTypeLabel'),
      discountValueLabel: t('admin.promotionsPage.discountValueLabel'),
      minOrderAmountLabel: t('admin.promotionsPage.minOrderAmountLabel'),
      maxDiscountAmountLabel: t('admin.promotionsPage.maxDiscountAmountLabel'),
      maxUsageCountLabel: t('admin.promotionsPage.maxUsageCountLabel'),
      startsAtLabel: t('admin.promotionsPage.startsAtLabel'),
      expiresAtLabel: t('admin.promotionsPage.expiresAtLabel'),
      activeLabel: t('admin.promotionsPage.activeLabel'),
      noDescription: t('admin.promotionsPage.noDescription'),
      noLimit: t('admin.promotionsPage.noLimit'),
      noMaxDiscount: t('admin.promotionsPage.noMaxDiscount'),
      codeHint: t('admin.promotionsPage.codeHint'),
      percentageHint: t('admin.promotionsPage.percentageHint'),
      startsAtHint: t('admin.promotionsPage.startsAtHint'),
      expiresAtHint: t('admin.promotionsPage.expiresAtHint'),
      invalidForm: t('admin.promotionsPage.invalidForm'),
      codeRequired: t('admin.promotionsPage.codeRequired'),
      discountValueRequired: t('admin.promotionsPage.discountValueRequired'),
      discountValuePositive: t('admin.promotionsPage.discountValuePositive'),
      discountValuePercentageMax: t(
        'admin.promotionsPage.discountValuePercentageMax',
      ),
      minOrderAmountInvalid: t('admin.promotionsPage.minOrderAmountInvalid'),
      maxDiscountAmountInvalid: t(
        'admin.promotionsPage.maxDiscountAmountInvalid',
      ),
      maxUsageCountInvalid: t('admin.promotionsPage.maxUsageCountInvalid'),
      startsAtInvalid: t('admin.promotionsPage.startsAtInvalid'),
      expiresAtInvalid: t('admin.promotionsPage.expiresAtInvalid'),
      expiresAtAfterStartsAt: t(
        'admin.promotionsPage.expiresAtAfterStartsAt',
      ),
      deleteBlockedShort: t('admin.promotionsPage.deleteBlockedShort'),
      deleteBlockedReason: t('admin.promotionsPage.deleteBlockedReason'),
      formDescription: t('admin.promotionsPage.formDescription'),
    }),
    [t],
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
        const response = await getAdminPromotionsPage({ page, size: PAGE_SIZE })

        if (isCancelled) {
          return
        }

        setPromotions(response.items)
        setTotalCount(response.totalCount)
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
  }, [labels.loadError, page])

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
    setPage(0)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
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
      const response = await getAdminPromotionsPage({ page, size: PAGE_SIZE })
      setPromotions(response.items)
      setTotalCount(response.totalCount)
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
    page,
    pageSize: PAGE_SIZE,
    totalCount,
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
    handlePageChange,
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

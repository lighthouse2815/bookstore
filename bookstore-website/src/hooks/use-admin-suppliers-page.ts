import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminSupplier,
  deleteAdminSupplier,
  getAdminSuppliersPage,
  updateAdminSupplier,
} from '@/services/admin-access-service'
import type {
  AdminSupplierMutationRequest,
  AdminSupplierResponse,
} from '@/types/admin-access'
import { getErrorMessage } from '@/utils'

type SupplierDialogMode = 'create' | 'view' | 'edit' | 'delete'

export type SupplierFormState = {
  name: string
  phone: string
  email: string
  address: string
  note: string
}

const initialFormState: SupplierFormState = {
  name: '',
  phone: '',
  email: '',
  address: '',
  note: '',
}

const PAGE_SIZE = 10

export function useAdminSuppliersPage() {
  const { t, formatDate, formatNumber } = useLanguage()
  const [suppliers, setSuppliers] = useState<AdminSupplierResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] = useState<SupplierDialogMode | null>(null)
  const [selectedSupplier, setSelectedSupplier] =
    useState<AdminSupplierResponse | null>(null)
  const [form, setForm] = useState<SupplierFormState>(initialFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  const labels = useMemo(
    () => ({
      pageTitle: t('admin.suppliersPage.pageTitle'),
      pageDescription: t('admin.suppliersPage.pageDescription'),
      totalSuppliers: t('admin.suppliersPage.totalSuppliers'),
      addSupplier: t('admin.suppliersPage.addSupplier'),
      searchPlaceholder: t('admin.suppliersPage.searchPlaceholder'),
      loadError: t('admin.suppliersPage.loadError'),
      empty: t('admin.suppliersPage.empty'),
      showingCount: t('admin.suppliersPage.showingCount'),
      nameColumn: t('admin.suppliersPage.nameColumn'),
      detailTitle: t('admin.suppliersPage.detailTitle'),
      editTitle: t('admin.suppliersPage.editTitle'),
      deleteTitle: t('admin.suppliersPage.deleteTitle'),
      deleteDescription: t('admin.suppliersPage.deleteDescription'),
      createSuccess: t('admin.suppliersPage.createSuccess'),
      updateSuccess: t('admin.suppliersPage.updateSuccess'),
      deleteSuccess: t('admin.suppliersPage.deleteSuccess'),
      saveError: t('admin.suppliersPage.saveError'),
      deleteError: t('admin.suppliersPage.deleteError'),
      noPhone: t('admin.suppliersPage.noPhone'),
      noEmail: t('admin.suppliersPage.noEmail'),
      noAddress: t('admin.suppliersPage.noAddress'),
      noNote: t('admin.suppliersPage.noNote'),
      phoneLabel: t('admin.suppliersPage.phoneLabel'),
      emailLabel: t('admin.suppliersPage.emailLabel'),
      addressLabel: t('admin.suppliersPage.addressLabel'),
      noteLabel: t('admin.suppliersPage.noteLabel'),
    }),
    [t],
  )

  const filteredSuppliers = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return suppliers
    }

    return suppliers.filter((supplier) =>
      [
        supplier.name,
        supplier.phone ?? '',
        supplier.email ?? '',
        supplier.address ?? '',
        supplier.note ?? '',
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [searchTerm, suppliers])

  useEffect(() => {
    let isCancelled = false

    async function loadSuppliers() {
      setIsLoading(true)

      try {
        const response = await getAdminSuppliersPage({ page, size: PAGE_SIZE })

        if (isCancelled) {
          return
        }

        setSuppliers(response.items)
        setTotalCount(response.totalCount)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, labels.loadError))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadSuppliers()

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
    setSelectedSupplier(null)
    setForm(initialFormState)
  }

  function closeDialog() {
    if (isSubmitting || isDeleting) {
      return
    }

    resetDialog()
  }

  function openCreateDialog() {
    setSelectedSupplier(null)
    setForm(initialFormState)
    setDialogMode('create')
  }

  function openViewDialog(supplier: AdminSupplierResponse) {
    setSelectedSupplier(supplier)
    setDialogMode('view')
  }

  function openEditDialog(supplier: AdminSupplierResponse) {
    setSelectedSupplier(supplier)
    setForm({
      name: supplier.name,
      phone: supplier.phone ?? '',
      email: supplier.email ?? '',
      address: supplier.address ?? '',
      note: supplier.note ?? '',
    })
    setDialogMode('edit')
  }

  function openEditFromView() {
    if (!selectedSupplier) {
      return
    }

    openEditDialog(selectedSupplier)
  }

  function openDeleteDialog(supplier: AdminSupplierResponse) {
    setSelectedSupplier(supplier)
    setDialogMode('delete')
  }

  function handleFormChange(field: keyof SupplierFormState, value: string) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  async function reloadSuppliers() {
    setIsLoading(true)

    try {
      const response = await getAdminSuppliersPage({ page, size: PAGE_SIZE })
      setSuppliers(response.items)
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

    setIsSubmitting(true)

    try {
      const payload: AdminSupplierMutationRequest = {
        name: form.name.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
        address: form.address.trim(),
        note: form.note.trim(),
      }

      if (dialogMode === 'edit' && selectedSupplier) {
        await updateAdminSupplier(selectedSupplier.id, payload)
        toast.success(labels.updateSuccess)
      } else {
        await createAdminSupplier(payload)
        toast.success(labels.createSuccess)
      }

      await reloadSuppliers()
      resetDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.saveError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteConfirm() {
    if (!selectedSupplier) {
      return
    }

    setIsDeleting(true)

    try {
      await deleteAdminSupplier(selectedSupplier.id)
      await reloadSuppliers()
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
    formatDate,
    formatNumber,
    suppliers,
    page,
    pageSize: PAGE_SIZE,
    totalCount,
    searchTerm,
    isLoading,
    error,
    dialogMode,
    selectedSupplier,
    form,
    isSubmitting,
    isDeleting,
    labels,
    filteredSuppliers,
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
    handleSubmit,
    handleDeleteConfirm,
  }
}

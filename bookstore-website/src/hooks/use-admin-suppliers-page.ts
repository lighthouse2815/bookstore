import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminSupplier,
  deleteAdminSupplier,
  getAdminSuppliers,
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

export function useAdminSuppliersPage() {
  const { language, t, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const [suppliers, setSuppliers] = useState<AdminSupplierResponse[]>([])
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
      pageTitle: isVietnamese ? 'Quản lý nhà cung cấp' : 'Manage suppliers',
      pageDescription: isVietnamese
        ? 'Theo dõi và cập nhật danh sách nhà cung cấp dùng cho nhập hàng.'
        : 'Review and update suppliers used for inventory imports.',
      totalSuppliers: isVietnamese
        ? '{count} nhà cung cấp'
        : '{count} suppliers',
      addSupplier: isVietnamese ? 'Thêm nhà cung cấp' : 'Add supplier',
      searchPlaceholder: isVietnamese
        ? 'Tìm theo tên, email hoặc số điện thoại...'
        : 'Search by name, email, or phone...',
      loadError: isVietnamese
        ? 'Không tải được danh sách nhà cung cấp'
        : 'Unable to load the supplier list',
      empty: isVietnamese ? 'Chưa có nhà cung cấp nào' : 'No suppliers found',
      showingCount: isVietnamese
        ? 'Hiển thị {count} trên {total} nhà cung cấp'
        : 'Showing {count} of {total} suppliers',
      nameColumn: isVietnamese ? 'Nhà cung cấp' : 'Supplier',
      detailTitle: isVietnamese
        ? 'Chi tiết nhà cung cấp'
        : 'Supplier details',
      editTitle: isVietnamese ? 'Sửa nhà cung cấp' : 'Edit supplier',
      deleteTitle: isVietnamese
        ? 'Xác nhận xóa nhà cung cấp'
        : 'Confirm supplier deletion',
      deleteDescription: isVietnamese
        ? 'Hành động này sẽ xóa nhà cung cấp khỏi hệ thống và không thể hoàn tác.'
        : 'This action removes the supplier from the system and cannot be undone.',
      createSuccess: isVietnamese
        ? 'Đã tạo nhà cung cấp'
        : 'Supplier created successfully',
      updateSuccess: isVietnamese
        ? 'Đã cập nhật nhà cung cấp'
        : 'Supplier updated successfully',
      deleteSuccess: isVietnamese
        ? 'Đã xóa nhà cung cấp'
        : 'Supplier deleted successfully',
      saveError: isVietnamese
        ? 'Không lưu được nhà cung cấp'
        : 'Unable to save supplier',
      deleteError: isVietnamese
        ? 'Không xóa được nhà cung cấp'
        : 'Unable to delete supplier',
      noPhone: isVietnamese ? 'Chưa có số điện thoại' : 'No phone number',
      noEmail: isVietnamese ? 'Chưa có email' : 'No email',
      noAddress: isVietnamese ? 'Chưa có địa chỉ' : 'No address',
      noNote: isVietnamese ? 'Chưa có ghi chú' : 'No note',
      phoneLabel: isVietnamese ? 'Số điện thoại' : 'Phone number',
      emailLabel: isVietnamese ? 'Email' : 'Email',
      addressLabel: isVietnamese ? 'Địa chỉ' : 'Address',
      noteLabel: isVietnamese ? 'Ghi chú' : 'Note',
    }),
    [isVietnamese],
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
        const response = await getAdminSuppliers()

        if (isCancelled) {
          return
        }

        setSuppliers(response)
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
      const response = await getAdminSuppliers()
      setSuppliers(response)
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

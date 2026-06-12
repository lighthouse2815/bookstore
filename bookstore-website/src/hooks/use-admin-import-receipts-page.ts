import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminImportReceipt,
  getAdminImportReceipts,
  getAdminSuppliers,
} from '@/services/admin-access-service'
import { getBookCatalog } from '@/services/book-service'
import type {
  AdminCreateImportReceiptRequest,
  AdminImportReceiptResponse,
  AdminSupplierResponse,
} from '@/types/admin-access'
import type { Book } from '@/types/book'
import { getErrorMessage } from '@/utils'

type DialogMode = 'create' | 'view'

type ReceiptItemForm = {
  bookId: string
  quantity: string
  unitCost: string
}

export type ReceiptFormState = {
  supplierId: string
  note: string
  items: ReceiptItemForm[]
}

const initialFormState: ReceiptFormState = {
  supplierId: '',
  note: '',
  items: [{ bookId: '', quantity: '1', unitCost: '0' }],
}

export function useAdminImportReceiptsPage() {
  const { language, t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const [receipts, setReceipts] = useState<AdminImportReceiptResponse[]>([])
  const [suppliers, setSuppliers] = useState<AdminSupplierResponse[]>([])
  const [books, setBooks] = useState<Book[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] = useState<DialogMode | null>(null)
  const [selectedReceipt, setSelectedReceipt] =
    useState<AdminImportReceiptResponse | null>(null)
  const [form, setForm] = useState<ReceiptFormState>(initialFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const labels = useMemo(
    () => ({
      title: isVietnamese ? 'Quản lý nhập kho' : 'Import receipts',
      description: isVietnamese
        ? 'Tạo và theo dõi các phiếu nhập sách vào kho.'
        : 'Create and review book inventory import receipts.',
      total: isVietnamese ? '{count} phiếu nhập' : '{count} receipts',
      add: isVietnamese ? 'Tạo phiếu nhập' : 'Create receipt',
      search: isVietnamese
        ? 'Tìm theo nhà cung cấp, mã phiếu hoặc tên sách...'
        : 'Search by supplier, receipt id, or book title...',
      empty: isVietnamese ? 'Chưa có phiếu nhập nào' : 'No import receipts found',
      receipt: isVietnamese ? 'Phiếu nhập' : 'Receipt',
      supplier: isVietnamese ? 'Nhà cung cấp' : 'Supplier',
      totalAmount: isVietnamese ? 'Tổng tiền' : 'Total',
      items: isVietnamese ? 'Số dòng' : 'Items',
      createdAt: isVietnamese ? 'Ngày nhập' : 'Created',
      detailTitle: isVietnamese ? 'Chi tiết phiếu nhập' : 'Receipt details',
      note: isVietnamese ? 'Ghi chú' : 'Note',
      noNote: isVietnamese ? 'Không có ghi chú' : 'No note',
      loadError: isVietnamese
        ? 'Không tải được danh sách phiếu nhập'
        : 'Unable to load import receipts',
      saveError: isVietnamese
        ? 'Không tạo được phiếu nhập'
        : 'Unable to create receipt',
      saveSuccess: isVietnamese ? 'Đã tạo phiếu nhập' : 'Import receipt created',
      book: isVietnamese ? 'Sách' : 'Book',
      quantity: isVietnamese ? 'Số lượng' : 'Quantity',
      unitCost: isVietnamese ? 'Giá nhập' : 'Unit cost',
      addLine: isVietnamese ? 'Thêm dòng sách' : 'Add book line',
      removeLine: isVietnamese ? 'Xóa dòng' : 'Remove line',
      chooseSupplier: isVietnamese ? 'Chọn nhà cung cấp' : 'Choose supplier',
      chooseBook: isVietnamese ? 'Chọn sách' : 'Choose book',
    }),
    [isVietnamese],
  )

  const supplierMap = useMemo(
    () => new Map(suppliers.map((supplier) => [supplier.id, supplier.name])),
    [suppliers],
  )

  const filteredReceipts = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return receipts
    }

    return receipts.filter((receipt) =>
      [
        receipt.id,
        supplierMap.get(receipt.supplierId) ?? '',
        ...receipt.items.map((item) => item.bookTitle),
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [receipts, searchTerm, supplierMap])

  useEffect(() => {
    let isCancelled = false

    async function loadData() {
      setIsLoading(true)

      try {
        const [receiptResponse, supplierResponse, bookResponse] = await Promise.all([
          getAdminImportReceipts(),
          getAdminSuppliers(),
          getBookCatalog(),
        ])

        if (isCancelled) {
          return
        }

        setReceipts(receiptResponse)
        setSuppliers(supplierResponse)
        setBooks(bookResponse.books)
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

    void loadData()

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
      if (event.key === 'Escape' && !isSubmitting) {
        closeDialog()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [dialogMode, isSubmitting])

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function closeDialog() {
    if (isSubmitting) {
      return
    }

    setDialogMode(null)
    setSelectedReceipt(null)
    setForm(initialFormState)
  }

  function openCreateDialog() {
    setSelectedReceipt(null)
    setForm({
      ...initialFormState,
      supplierId: suppliers[0]?.id ?? '',
      items: [
        {
          bookId: books[0]?.id ?? '',
          quantity: '1',
          unitCost: '0',
        },
      ],
    })
    setDialogMode('create')
  }

  function openViewDialog(receipt: AdminImportReceiptResponse) {
    setSelectedReceipt(receipt)
    setDialogMode('view')
  }

  function handleFormChange<K extends keyof Omit<ReceiptFormState, 'items'>>(
    field: K,
    value: ReceiptFormState[K],
  ) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  function updateItem(index: number, patch: Partial<ReceiptItemForm>) {
    setForm((currentForm) => ({
      ...currentForm,
      items: currentForm.items.map((item, itemIndex) =>
        itemIndex === index ? { ...item, ...patch } : item,
      ),
    }))
  }

  function addItem() {
    setForm((currentForm) => ({
      ...currentForm,
      items: [
        ...currentForm.items,
        { bookId: books[0]?.id ?? '', quantity: '1', unitCost: '0' },
      ],
    }))
  }

  function removeItem(index: number) {
    setForm((currentForm) => ({
      ...currentForm,
      items: currentForm.items.filter((_, itemIndex) => itemIndex !== index),
    }))
  }

  async function reloadData() {
    setIsLoading(true)

    try {
      const [receiptResponse, supplierResponse, bookResponse] = await Promise.all([
        getAdminImportReceipts(),
        getAdminSuppliers(),
        getBookCatalog(),
      ])

      setReceipts(receiptResponse)
      setSuppliers(supplierResponse)
      setBooks(bookResponse.books)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, labels.loadError))
    } finally {
      setIsLoading(false)
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const payload: AdminCreateImportReceiptRequest = {
      supplierId: form.supplierId,
      note: form.note.trim(),
      items: form.items.map((item) => ({
        bookId: item.bookId,
        quantity: Number(item.quantity),
        unitCost: Number(item.unitCost),
      })),
    }

    setIsSubmitting(true)

    try {
      await createAdminImportReceipt(payload)
      toast.success(labels.saveSuccess)
      await reloadData()
      closeDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.saveError))
    } finally {
      setIsSubmitting(false)
    }
  }

  return {
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    labels,
    receipts,
    suppliers,
    books,
    searchTerm,
    isLoading,
    error,
    dialogMode,
    selectedReceipt,
    form,
    isSubmitting,
    supplierMap,
    filteredReceipts,
    handleSearchTermChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    handleFormChange,
    updateItem,
    addItem,
    removeItem,
    handleSubmit,
  }
}

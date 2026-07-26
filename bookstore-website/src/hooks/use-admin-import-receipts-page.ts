import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminImportReceipt,
  getAdminImportReceiptsPage,
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

const PAGE_SIZE = 10

export function useAdminImportReceiptsPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [receipts, setReceipts] = useState<AdminImportReceiptResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
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
      title: t('admin.importReceiptsPage.title'),
      description: t('admin.importReceiptsPage.description'),
      total: t('admin.importReceiptsPage.total'),
      add: t('admin.importReceiptsPage.add'),
      search: t('admin.importReceiptsPage.search'),
      empty: t('admin.importReceiptsPage.empty'),
      receipt: t('admin.importReceiptsPage.receipt'),
      supplier: t('admin.importReceiptsPage.supplier'),
      totalAmount: t('admin.importReceiptsPage.totalAmount'),
      items: t('admin.importReceiptsPage.items'),
      createdAt: t('admin.importReceiptsPage.createdAt'),
      detailTitle: t('admin.importReceiptsPage.detailTitle'),
      note: t('admin.importReceiptsPage.note'),
      noNote: t('admin.importReceiptsPage.noNote'),
      loadError: t('admin.importReceiptsPage.loadError'),
      saveError: t('admin.importReceiptsPage.saveError'),
      saveSuccess: t('admin.importReceiptsPage.saveSuccess'),
      book: t('admin.importReceiptsPage.book'),
      quantity: t('admin.importReceiptsPage.quantity'),
      unitCost: t('admin.importReceiptsPage.unitCost'),
      addLine: t('admin.importReceiptsPage.addLine'),
      removeLine: t('admin.importReceiptsPage.removeLine'),
      chooseSupplier: t('admin.importReceiptsPage.chooseSupplier'),
      chooseBook: t('admin.importReceiptsPage.chooseBook'),
    }),
    [t],
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
          getAdminImportReceiptsPage({ page, size: PAGE_SIZE }),
          getAdminSuppliers(),
          getBookCatalog(),
        ])

        if (isCancelled) {
          return
        }

        setReceipts(receiptResponse.items)
        setTotalCount(receiptResponse.totalCount)
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
  }, [labels.loadError, page])

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
    setPage(0)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
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
        getAdminImportReceiptsPage({ page, size: PAGE_SIZE }),
        getAdminSuppliers(),
        getBookCatalog(),
      ])

      setReceipts(receiptResponse.items)
      setTotalCount(receiptResponse.totalCount)
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
    page,
    pageSize: PAGE_SIZE,
    totalCount,
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
    handlePageChange,
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

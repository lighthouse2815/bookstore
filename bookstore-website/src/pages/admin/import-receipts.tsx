import { useEffect, useMemo, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  CalendarDays,
  Eye,
  PackagePlus,
  Plus,
  ReceiptText,
  Search,
  Trash2,
  Truck,
  X,
} from 'lucide-react'
import { toast } from 'sonner'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { AdminLayout } from '@/components/layout/admin-layout'
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

type ReceiptFormState = {
  supplierId: string
  note: string
  items: ReceiptItemForm[]
}

const initialFormState: ReceiptFormState = {
  supplierId: '',
  note: '',
  items: [{ bookId: '', quantity: '1', unitCost: '0' }],
}

export default function AdminImportReceiptsPage() {
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
      saveError: isVietnamese ? 'Không tạo được phiếu nhập' : 'Unable to create receipt',
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
    void loadData()
  }, [])

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

  async function loadData() {
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

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
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
      await loadData()
      closeDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.saveError))
    } finally {
      setIsSubmitting(false)
    }
  }

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={closeDialog}
        disabled={isSubmitting}
      />
      <div className="relative z-10 w-full max-w-4xl">
        {dialogMode === 'create' ? (
          <DialogShell
            title={labels.add}
            onClose={closeDialog}
            canClose={!isSubmitting}
          >
            <form className="space-y-5" onSubmit={(event) => void handleSubmit(event)}>
              <div className="grid gap-5 md:grid-cols-2">
                <div className="space-y-2">
                  <Label>{labels.supplier}</Label>
                  <select
                    value={form.supplierId}
                    onChange={(event) =>
                      setForm((currentForm) => ({
                        ...currentForm,
                        supplierId: event.currentTarget.value,
                      }))
                    }
                    className="h-11 w-full rounded-2xl border border-input bg-background px-3 text-sm"
                    required
                  >
                    <option value="" disabled>
                      {labels.chooseSupplier}
                    </option>
                    {suppliers.map((supplier) => (
                      <option key={supplier.id} value={supplier.id}>
                        {supplier.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="space-y-2">
                  <Label>{labels.note}</Label>
                  <Input
                    value={form.note}
                    onChange={(event) =>
                      setForm((currentForm) => ({
                        ...currentForm,
                        note: event.currentTarget.value,
                      }))
                    }
                    className="h-11 rounded-2xl"
                  />
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-center justify-between gap-3">
                  <Label>{labels.items}</Label>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={addItem}
                    className="rounded-2xl"
                  >
                    <Plus className="mr-2 h-4 w-4" />
                    {labels.addLine}
                  </Button>
                </div>

                <div className="space-y-3">
                  {form.items.map((item, index) => (
                    <div
                      key={`${index}-${item.bookId}`}
                      className="grid gap-3 rounded-[20px] border border-border/60 bg-background/55 p-4 md:grid-cols-[minmax(0,1fr)_8rem_10rem_auto]"
                    >
                      <select
                        value={item.bookId}
                        onChange={(event) =>
                          updateItem(index, { bookId: event.currentTarget.value })
                        }
                        className="h-11 min-w-0 rounded-2xl border border-input bg-background px-3 text-sm"
                        required
                      >
                        <option value="" disabled>
                          {labels.chooseBook}
                        </option>
                        {books.map((book) => (
                          <option key={book.id} value={book.id}>
                            {book.title}
                          </option>
                        ))}
                      </select>
                      <Input
                        type="number"
                        min="1"
                        value={item.quantity}
                        onChange={(event) =>
                          updateItem(index, { quantity: event.currentTarget.value })
                        }
                        className="h-11 rounded-2xl"
                        aria-label={labels.quantity}
                        required
                      />
                      <Input
                        type="number"
                        min="0"
                        step="1000"
                        value={item.unitCost}
                        onChange={(event) =>
                          updateItem(index, { unitCost: event.currentTarget.value })
                        }
                        className="h-11 rounded-2xl"
                        aria-label={labels.unitCost}
                        required
                      />
                      <Button
                        type="button"
                        variant="destructive"
                        size="icon"
                        onClick={() => removeItem(index)}
                        disabled={form.items.length === 1}
                        aria-label={labels.removeLine}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex justify-end gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={closeDialog}
                  disabled={isSubmitting}
                >
                  {t('common.cancel')}
                </Button>
                <Button
                  type="submit"
                  disabled={isSubmitting || !form.supplierId || form.items.length === 0}
                >
                  {isSubmitting ? t('common.processing') : t('common.save')}
                </Button>
              </div>
            </form>
          </DialogShell>
        ) : null}

        {dialogMode === 'view' && selectedReceipt ? (
          <DialogShell title={labels.detailTitle} onClose={closeDialog}>
            <ReceiptDetail
              formatCurrency={formatCurrency}
              formatDate={formatDate}
              labels={labels}
              receipt={selectedReceipt}
              supplierName={supplierMap.get(selectedReceipt.supplierId) ?? selectedReceipt.supplierId}
            />
          </DialogShell>
        ) : null}
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(129,140,248,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(59,130,246,0.12),transparent_32%)]" />

          <div className="relative">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                    {labels.title}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <ReceiptText className="mr-2 h-4 w-4" />
                    {interpolateLabel(labels.total, {
                      count: formatNumber(receipts.length),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {labels.description}
                </p>
              </div>

              <Button
                size="lg"
                onClick={openCreateDialog}
                className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
              >
                <PackagePlus className="mr-2 h-5 w-5" />
                {labels.add}
              </Button>
            </div>

            <div className="mt-8 max-w-xl">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={searchTerm}
                  onChange={(event) => setSearchTerm(event.currentTarget.value)}
                  placeholder={labels.search}
                  className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base"
                />
              </div>
            </div>

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.4fr_1fr_1fr_12rem]">
                  <div className="px-8 py-6">{labels.receipt}</div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.supplier}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.totalAmount}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.items}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {t('common.actions')}
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredReceipts.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center text-muted-foreground">
                    {labels.empty}
                  </div>
                ) : (
                  filteredReceipts.map((receipt) => (
                    <article
                      key={receipt.id}
                      className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.4fr_1fr_1fr_12rem] xl:gap-0 xl:p-0"
                    >
                      <div className="min-w-0 xl:px-8 xl:py-6">
                        <p className="truncate text-lg font-semibold text-foreground">
                          {receipt.id}
                        </p>
                        <p className="mt-2 flex items-center gap-2 text-sm text-muted-foreground">
                          <CalendarDays className="h-4 w-4" />
                          {formatDate(receipt.createdAt)}
                        </p>
                      </div>
                      <div className="flex items-center justify-start border-border/40 text-sm font-medium text-foreground xl:justify-center xl:border-l">
                        <Truck className="mr-2 h-4 w-4 text-primary" />
                        {supplierMap.get(receipt.supplierId) ?? receipt.supplierId}
                      </div>
                      <div className="flex items-center justify-start border-border/40 text-lg font-semibold text-foreground xl:justify-center xl:border-l">
                        {formatCurrency(receipt.totalAmount)}
                      </div>
                      <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                        <Badge variant="outline" className="rounded-2xl px-3 py-1.5">
                          {formatNumber(receipt.items.length)}
                        </Badge>
                      </div>
                      <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                        <Button
                          type="button"
                          variant="outline"
                          onClick={() => openViewDialog(receipt)}
                          className="min-w-[96px] justify-center rounded-2xl bg-background/60"
                        >
                          <Eye className="mr-2 h-4 w-4" />
                          {t('common.view')}
                        </Button>
                      </div>
                    </article>
                  ))
                )}
              </div>
            </section>
          </div>
        </div>
      </AdminLayout>

      {dialogMarkup && typeof document !== 'undefined'
        ? createPortal(dialogMarkup, document.body)
        : null}
    </>
  )
}

function ReceiptDetail({
  formatCurrency,
  formatDate,
  labels,
  receipt,
  supplierName,
}: {
  formatCurrency: (value: number) => string
  formatDate: (value: string | number | Date) => string
  labels: {
    createdAt: string
    note: string
    noNote: string
    supplier: string
    totalAmount: string
  }
  receipt: AdminImportReceiptResponse
  supplierName: string
}) {
  return (
    <div className="space-y-6">
      <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
        <p className="text-sm text-muted-foreground">{labels.supplier}</p>
        <p className="mt-2 text-2xl font-semibold text-foreground">{supplierName}</p>
        <p className="mt-3 text-sm text-muted-foreground">
          {labels.createdAt}: {formatDate(receipt.createdAt)}
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="rounded-[20px] border border-border/60 bg-background/55 p-4">
          <p className="text-sm text-muted-foreground">{labels.totalAmount}</p>
          <p className="mt-2 text-2xl font-bold text-foreground">
            {formatCurrency(receipt.totalAmount)}
          </p>
        </div>
        <div className="rounded-[20px] border border-border/60 bg-background/55 p-4">
          <p className="text-sm text-muted-foreground">{labels.note}</p>
          <p className="mt-2 text-base font-semibold text-foreground">
            {receipt.note || labels.noNote}
          </p>
        </div>
      </div>

      <div className="space-y-3">
        {receipt.items.map((item) => (
          <div
            key={item.id}
            className="grid gap-3 rounded-[20px] border border-border/60 bg-background/55 p-4 md:grid-cols-[minmax(0,1fr)_7rem_10rem_10rem]"
          >
            <p className="min-w-0 truncate font-semibold text-foreground">
              {item.bookTitle}
            </p>
            <p className="text-muted-foreground">x{item.quantity}</p>
            <p className="font-medium text-foreground">
              {formatCurrency(item.unitCost)}
            </p>
            <p className="font-semibold text-primary">
              {formatCurrency(item.lineTotal)}
            </p>
          </div>
        ))}
      </div>
    </div>
  )
}

function DialogShell({
  canClose = true,
  children,
  onClose,
  title,
}: {
  canClose?: boolean
  children: React.ReactNode
  onClose: () => void
  title: string
}) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          onClick={onClose}
          className="rounded-2xl"
          disabled={!canClose}
        >
          <X className="h-4 w-4" />
        </Button>
      </div>
      <div className="max-h-[78vh] overflow-y-auto px-6 py-6">{children}</div>
    </div>
  )
}

function interpolateLabel(
  template: string,
  params: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    String(params[key] ?? `{${key}}`),
  )
}

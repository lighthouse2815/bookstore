import { useEffect, useMemo, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  BookOpen,
  Building2,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Edit2,
  Eye,
  Package2,
  Plus,
  RefreshCw,
  Search,
  Tag,
  Trash2,
  User2,
  Wallet,
  X,
  type LucideIcon,
} from 'lucide-react'
import { toast } from 'sonner'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { Textarea } from '@/components/common/textarea'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import {
  createBook,
  deleteBook,
  getBookCatalog,
  getBookReferences,
  updateBook,
} from '@/services/book-service'
import type { Book, BookReferenceData } from '@/types/book'
import { getBookCoverUrl } from '@/utils/book-cover'
import { cn, getErrorMessage } from '@/utils'
import { getCategoryLabel } from '@/utils/i18n'

type BookDialogMode = 'create' | 'view' | 'edit' | 'delete'

type BookFormState = {
  title: string
  description: string
  price: string
  stockQuantity: string
  imageUrl: string
  categoryId: string
  authorId: string
  publisherId: string
}

const initialFormState: BookFormState = {
  title: '',
  description: '',
  price: '',
  stockQuantity: '',
  imageUrl: '',
  categoryId: '',
  authorId: '',
  publisherId: '',
}

const initialReferences: BookReferenceData = {
  categories: [],
  authors: [],
  publishers: [],
}

const tableGridClassName =
  'xl:grid xl:grid-cols-[minmax(0,2.8fr)_1.15fr_1fr_1fr_auto]'

export default function AdminBooksPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [books, setBooks] = useState<Book[]>([])
  const [references, setReferences] = useState<BookReferenceData>(initialReferences)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategoryId, setSelectedCategoryId] = useState('all')
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [dialogMode, setDialogMode] = useState<BookDialogMode | null>(null)
  const [selectedBook, setSelectedBook] = useState<Book | null>(null)
  const [form, setForm] = useState<BookFormState>(initialFormState)

  const hasReferenceData =
    references.categories.length > 0 &&
    references.authors.length > 0 &&
    references.publishers.length > 0

  const isDialogLocked = dialogMode === 'delete' && isDeleting

  const filteredBooks = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    return books.filter((book) => {
      const matchesSearch =
        keyword === '' ||
        [book.title, book.author, book.category, book.publisher, book.description ?? '']
          .join(' ')
          .toLowerCase()
          .includes(keyword)

      const matchesCategory =
        selectedCategoryId === 'all' || book.categoryId === selectedCategoryId

      return matchesSearch && matchesCategory
    })
  }, [books, searchTerm, selectedCategoryId])

  useEffect(() => {
    void loadAdminBooksData()
  }, [])

  useEffect(() => {
    if (!dialogMode) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !isDialogLocked) {
        closeDialog()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [dialogMode, isDialogLocked])

  async function loadAdminBooksData() {
    setIsLoading(true)

    try {
      const [catalog, referenceData] = await Promise.all([
        getBookCatalog(),
        getBookReferences(),
      ])

      setBooks(catalog.books)
      setReferences(referenceData)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, t('book.listing.errorTitle')))
    } finally {
      setIsLoading(false)
    }
  }

  function createFormState(book: Book): BookFormState {
    return {
      title: book.title,
      description: book.description ?? '',
      price: String(book.price),
      stockQuantity: String(book.stockQuantity),
      imageUrl: book.cover ?? '',
      categoryId: book.categoryId,
      authorId: book.authorId,
      publisherId: book.publisherId,
    }
  }

  function openCreateDialog() {
    setSelectedBook(null)
    setForm(initialFormState)
    setDialogMode('create')
  }

  function openViewDialog(book: Book) {
    setSelectedBook(book)
    setDialogMode('view')
  }

  function openEditDialog(book: Book) {
    setSelectedBook(book)
    setForm(createFormState(book))
    setDialogMode('edit')
  }

  function openEditFromDetail() {
    if (!selectedBook) {
      return
    }

    openEditDialog(selectedBook)
  }

  function openDeleteDialog(book: Book) {
    setSelectedBook(book)
    setDialogMode('delete')
  }

  function closeDialog() {
    setDialogMode(null)
    setSelectedBook(null)
    setForm(initialFormState)
  }

  function handleFormChange(field: keyof BookFormState, value: string) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!hasReferenceData) {
      toast.error(t('admin.books.referencesMissing'))
      return
    }

    setIsSubmitting(true)

    try {
      const payload = {
        title: form.title.trim(),
        description: form.description.trim() || null,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
        imageUrl: form.imageUrl.trim() || null,
        categoryId: form.categoryId,
        authorId: form.authorId,
        publisherId: form.publisherId,
      }

      if (dialogMode === 'edit' && selectedBook) {
        await updateBook(selectedBook.id, payload)
        toast.success(t('admin.books.updateSuccess'))
      } else {
        await createBook(payload)
        toast.success(t('admin.books.createSuccess'))
      }

      await loadAdminBooksData()
      closeDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function confirmDelete() {
    if (!selectedBook) {
      return
    }

    setIsDeleting(true)

    try {
      await deleteBook(selectedBook.id)
      toast.success(t('admin.books.deleteSuccess'))
      await loadAdminBooksData()
      closeDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsDeleting(false)
    }
  }

  const dialogSizeClassName =
    dialogMode === 'delete'
      ? 'max-w-xl'
      : dialogMode === 'view'
        ? 'max-w-[1180px]'
        : 'max-w-5xl'

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/80 backdrop-blur-md"
        onClick={isDialogLocked ? undefined : closeDialog}
        disabled={isDialogLocked}
      />

      <div
        className={cn(
          'relative z-10 w-full rounded-[32px] border border-border/70 bg-card/95 p-6 shadow-[0_40px_120px_rgba(2,6,23,0.55)] backdrop-blur xl:p-7',
          dialogSizeClassName,
        )}
      >
        <button
          type="button"
          onClick={isDialogLocked ? undefined : closeDialog}
          disabled={isDialogLocked}
          className="absolute right-5 top-5 inline-flex size-10 items-center justify-center rounded-full border border-border/70 bg-background/50 text-muted-foreground transition-colors hover:bg-background hover:text-foreground"
        >
          <X className="h-4 w-4" />
        </button>

        {dialogMode === 'view' && selectedBook ? (
          <BookDetailDialogContent
            book={selectedBook}
            formatCurrency={formatCurrency}
            formatDate={formatDate}
            formatNumber={formatNumber}
            onClose={closeDialog}
            onEdit={openEditFromDetail}
            t={t}
          />
        ) : dialogMode === 'delete' && selectedBook ? (
          <BookDeleteDialogContent
            book={selectedBook}
            isDeleting={isDeleting}
            onClose={closeDialog}
            onConfirm={confirmDelete}
            t={t}
          />
        ) : (
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="pr-12">
              <div className="flex items-center gap-4">
                <div className="flex size-16 items-center justify-center rounded-[20px] border border-primary/20 bg-linear-to-br from-primary/20 via-primary/10 to-transparent shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]">
                  <BookOpen className="h-7 w-7 text-primary" />
                </div>
                <div>
                  <p className="text-sm font-medium text-primary">
                    {t('admin.books.sectionLabel')}
                  </p>
                  <h2 className="mt-1 font-heading text-2xl font-bold text-foreground">
                    {dialogMode === 'create'
                      ? t('admin.books.addBook')
                      : t('admin.books.editBook')}
                  </h2>
                </div>
              </div>

              {dialogMode === 'create' ? (
                <p className="mt-4 text-sm text-muted-foreground">
                  {t('admin.books.formDescription')}
                </p>
              ) : null}
            </div>

            {!hasReferenceData ? (
              <div className="rounded-2xl border border-amber-400/30 bg-amber-400/10 p-4 text-sm text-amber-950 dark:text-amber-200">
                <p className="font-semibold">{t('admin.books.referencesMissing')}</p>
                <p className="mt-2">{t('admin.books.referencesSplitDescription')}</p>
              </div>
            ) : null}

            <div className="grid gap-6 lg:grid-cols-[minmax(0,1.8fr)_minmax(280px,.9fr)]">
              <div className="space-y-5">
                <div>
                  <Label htmlFor="bookTitle">{t('admin.books.fields.title')}</Label>
                  <Input
                    id="bookTitle"
                    value={form.title}
                    onChange={(event) =>
                      handleFormChange('title', event.currentTarget.value)
                    }
                    className="mt-2 h-12 rounded-2xl bg-background/60"
                    required
                  />
                </div>

                <div>
                  <Label htmlFor="bookDescription">
                    {t('admin.books.fields.description')}
                  </Label>
                  <Textarea
                    id="bookDescription"
                    value={form.description}
                    onChange={(event) =>
                      handleFormChange('description', event.currentTarget.value)
                    }
                    className="mt-2 min-h-32 rounded-2xl bg-background/60"
                    rows={6}
                  />
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <Label htmlFor="bookPrice">{t('admin.books.fields.price')}</Label>
                    <Input
                      id="bookPrice"
                      type="number"
                      min="0"
                      step="1000"
                      value={form.price}
                      onChange={(event) =>
                        handleFormChange('price', event.currentTarget.value)
                      }
                      className="mt-2 h-12 rounded-2xl bg-background/60"
                      required
                    />
                  </div>
                  <div>
                    <Label htmlFor="bookStock">
                      {t('admin.books.fields.stockQuantity')}
                    </Label>
                    <Input
                      id="bookStock"
                      type="number"
                      min="0"
                      step="1"
                      value={form.stockQuantity}
                      onChange={(event) =>
                        handleFormChange('stockQuantity', event.currentTarget.value)
                      }
                      className="mt-2 h-12 rounded-2xl bg-background/60"
                      required
                    />
                  </div>
                </div>

                <div>
                  <Label htmlFor="bookImageUrl">
                    {t('admin.books.fields.imageUrl')}
                  </Label>
                  <Input
                    id="bookImageUrl"
                    value={form.imageUrl}
                    onChange={(event) =>
                      handleFormChange('imageUrl', event.currentTarget.value)
                    }
                    className="mt-2 h-12 rounded-2xl bg-background/60"
                  />
                </div>

                <div className="grid gap-4 sm:grid-cols-3">
                  <ReferenceSelectField
                    id="bookCategory"
                    label={t('admin.books.fields.category')}
                    value={form.categoryId}
                    onValueChange={(value) => handleFormChange('categoryId', value)}
                    placeholder={t('admin.books.fields.category')}
                    options={references.categories.map((category) => ({
                      value: category.id,
                      label: category.name,
                    }))}
                  />
                  <ReferenceSelectField
                    id="bookAuthor"
                    label={t('admin.books.fields.author')}
                    value={form.authorId}
                    onValueChange={(value) => handleFormChange('authorId', value)}
                    placeholder={t('admin.books.fields.author')}
                    options={references.authors.map((author) => ({
                      value: author.id,
                      label: author.name,
                    }))}
                  />
                  <ReferenceSelectField
                    id="bookPublisher"
                    label={t('admin.books.fields.publisher')}
                    value={form.publisherId}
                    onValueChange={(value) =>
                      handleFormChange('publisherId', value)
                    }
                    placeholder={t('admin.books.fields.publisher')}
                    options={references.publishers.map((publisher) => ({
                      value: publisher.id,
                      label: publisher.name,
                    }))}
                  />
                </div>
              </div>

              <div className="rounded-[24px] border border-dashed border-border/60 bg-background/40 p-5">
                <p className="text-sm font-semibold text-foreground">
                  {t('admin.books.previewTitle')}
                </p>
                <div className="mt-4 flex flex-col gap-4">
                  <div className="overflow-hidden rounded-[20px] border border-border/60 bg-background/70">
                    <img
                      src={getBookCoverUrl(form.imageUrl)}
                      alt={form.title || t('admin.books.fields.title')}
                      className="aspect-[3/4] w-full object-cover"
                    />
                  </div>
                  <div>
                    <p className="text-xl font-semibold text-foreground">
                      {form.title || t('admin.books.emptyPreviewTitle')}
                    </p>
                    <p className="mt-2 text-sm leading-7 text-muted-foreground">
                      {form.description || t('admin.books.emptyPreviewDescription')}
                    </p>
                    <p className="mt-4 text-2xl font-bold text-primary">
                      {formatCurrency(Number(form.price || 0))}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="flex flex-wrap justify-end gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={closeDialog}
                className="rounded-2xl"
              >
                {t('common.cancel')}
              </Button>
              <Button
                type="submit"
                disabled={isSubmitting || !hasReferenceData}
                className="rounded-2xl"
              >
                {isSubmitting ? t('common.processing') : t('common.save')}
              </Button>
            </div>
          </form>
        )}
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
                    {t('admin.books.title')}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <BookOpen className="mr-2 h-4 w-4" />
                    {t('admin.books.countLabel', {
                      count: formatNumber(books.length),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {t('admin.books.description')}
                </p>
              </div>

              <Button
                size="lg"
                onClick={openCreateDialog}
                className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
              >
                <Plus className="mr-2 h-5 w-5" />
                {t('admin.books.addBook')}
              </Button>
            </div>

            <div className="mt-8 flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
              <div className="w-full max-w-xl">
                <div className="relative">
                  <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    placeholder={t('admin.books.searchPlaceholder')}
                    value={searchTerm}
                    onChange={(event) => setSearchTerm(event.currentTarget.value)}
                    className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base shadow-[inset_0_1px_0_rgba(255,255,255,0.04)]"
                  />
                </div>
              </div>

              <div className="w-full max-w-sm xl:ml-auto">
                <Select
                  value={selectedCategoryId}
                  onValueChange={(nextValue) =>
                    setSelectedCategoryId(nextValue ?? 'all')
                  }
                >
                  <SelectTrigger className="h-14 w-full rounded-2xl border-border/70 bg-background/55 px-4 text-base shadow-[inset_0_1px_0_rgba(255,255,255,0.04)]">
                    <SelectValue placeholder={t('admin.books.filterPlaceholder')}>
                      {selectedCategoryId === 'all'
                        ? t('admin.books.allCategories')
                        : references.categories.find(
                            (category) => category.id === selectedCategoryId,
                          )?.name}
                    </SelectValue>
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">
                      {t('admin.books.allCategories')}
                    </SelectItem>
                    {references.categories.map((category) => (
                      <SelectItem key={category.id} value={category.id}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {!hasReferenceData && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-amber-400/30 bg-amber-400/10 p-5 text-sm text-amber-950 dark:text-amber-200">
                <p className="font-semibold">{t('admin.books.referencesMissing')}</p>
                <p className="mt-2">{t('admin.books.referencesSplitDescription')}</p>
              </div>
            ) : null}

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden xl:block">
                <div
                  className={cn(
                    'overflow-hidden rounded-[24px] border border-border/60 bg-background/55 shadow-[0_18px_40px_rgba(2,6,23,0.16)] text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground',
                    tableGridClassName,
                  )}
                >
                    <div className="flex items-center gap-5 px-8 py-6">
                      <div
                        aria-hidden="true"
                        className="w-24 shrink-0"
                      />
                      <p>{t('admin.books.columns.book')}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{t('admin.books.columns.author')}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{t('admin.books.columns.price')}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{t('admin.books.columns.stock')}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{t('common.actions')}</p>
                    </div>
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredBooks.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center">
                    <p className="text-base font-medium text-foreground">
                      {t('admin.books.empty')}
                    </p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {filteredBooks.map((book) => {
                      const stockAvailable = book.stockQuantity > 0

                      return (
                        <article
                          key={book.id}
                          className={cn(
                            'flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)]',
                            tableGridClassName,
                            'xl:gap-0 xl:p-0',
                          )}
                        >
                          <div className="flex min-w-0 items-center gap-5 xl:px-8 xl:py-6">
                            <div className="overflow-hidden rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
                              <img
                                src={getBookCoverUrl(book.cover)}
                                alt={book.title}
                                className="h-36 w-24 object-cover"
                              />
                            </div>

                            <div className="min-w-0">
                              <p className="truncate text-2xl font-semibold text-foreground">
                                {book.title}
                              </p>
                              <div className="mt-4">
                                <Badge
                                  variant="outline"
                                  className={cn(
                                    'rounded-2xl px-3 py-1.5 text-sm font-semibold',
                                    stockAvailable
                                      ? 'border-emerald-400/30 bg-emerald-400/10 text-emerald-300'
                                      : 'border-destructive/30 bg-destructive/10 text-destructive',
                                  )}
                                >
                                  <Package2 className="mr-2 h-4 w-4" />
                                  {stockAvailable
                                    ? t('admin.books.inStock')
                                    : t('admin.books.outOfStock')}
                                </Badge>
                              </div>
                            </div>
                          </div>

                          <div className="xl:flex xl:min-h-[192px] xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6 xl:text-center">
                            <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground xl:hidden">
                              {t('admin.books.columns.author')}
                            </p>
                            <p className="mt-2 text-lg font-medium text-foreground xl:mt-0">
                              {book.author || t('book.fallback.author')}
                            </p>
                          </div>

                          <div className="xl:flex xl:min-h-[192px] xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6 xl:text-center">
                            <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground xl:hidden">
                              {t('admin.books.columns.price')}
                            </p>
                            <p className="mt-2 text-2xl font-bold text-foreground xl:mt-0">
                              {formatCurrency(book.price)}
                            </p>
                          </div>

                          <div className="xl:flex xl:min-h-[192px] xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6 xl:text-center">
                            <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground xl:hidden">
                              {t('admin.books.columns.stock')}
                            </p>
                            <div className="mt-2 inline-flex min-w-[96px] flex-col items-center rounded-[20px] border border-border/60 bg-background/60 px-4 py-3 xl:mt-0">
                              <span className="text-2xl font-bold text-foreground">
                                {formatNumber(book.stockQuantity)}
                              </span>
                              <span className="text-xs uppercase tracking-[0.18em] text-muted-foreground">
                                {t('admin.books.stockUnit')}
                              </span>
                            </div>
                          </div>

                          <div className="flex flex-wrap gap-3 xl:min-h-[192px] xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6">
                            <Button
                              type="button"
                              variant="outline"
                              onClick={() => openViewDialog(book)}
                              className="min-w-[110px] justify-center rounded-2xl bg-background/60"
                            >
                              <Eye className="mr-2 h-4 w-4" />
                              {t('common.view')}
                            </Button>
                            <Button
                              type="button"
                              variant="outline"
                              onClick={() => openEditDialog(book)}
                              className="min-w-[110px] justify-center rounded-2xl bg-background/60"
                            >
                              <Edit2 className="mr-2 h-4 w-4" />
                              {t('common.edit')}
                            </Button>
                            <Button
                              type="button"
                              variant="destructive"
                              onClick={() => openDeleteDialog(book)}
                              className="min-w-[110px] justify-center rounded-2xl"
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              {t('common.delete')}
                            </Button>
                          </div>
                        </article>
                      )
                    })}
                  </div>
                )}
              </div>

              {!isLoading && !error && filteredBooks.length > 0 ? (
                <div className="grid gap-4 border-t border-border/60 px-6 py-5 text-sm text-muted-foreground xl:grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] xl:items-center">
                  <p className="min-w-0 xl:self-center">
                    {t('admin.books.showingCount', {
                      count: formatNumber(filteredBooks.length),
                      total: formatNumber(books.length),
                    })}
                  </p>
                  <div className="flex items-center justify-center gap-3 xl:justify-self-center">
                    <Button
                      type="button"
                      variant="outline"
                      disabled
                      className="size-12 rounded-2xl border-border/60 bg-background/40 p-0 text-muted-foreground opacity-60"
                    >
                      <ChevronLeft className="h-4 w-4" />
                    </Button>
                    <div className="flex h-12 min-w-[52px] items-center justify-center rounded-2xl bg-primary px-4 text-sm font-semibold text-primary-foreground shadow-[0_18px_40px_rgba(99,102,241,0.35)]">
                      1
                    </div>
                    <Button
                      type="button"
                      variant="outline"
                      disabled
                      className="size-12 rounded-2xl border-border/60 bg-background/40 p-0 text-muted-foreground opacity-60"
                    >
                      <ChevronRight className="h-4 w-4" />
                    </Button>
                  </div>
                  <div className="hidden xl:block" />
                </div>
              ) : null}
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

type BookDetailDialogContentProps = {
  book: Book
  formatCurrency: (value: number) => string
  formatDate: (value: string | number | Date) => string
  formatNumber: (value: number) => string
  onClose: () => void
  onEdit: () => void
  t: (key: string, params?: Record<string, number | string>) => string
}

function BookDetailDialogContent({
  book,
  formatCurrency,
  formatDate,
  formatNumber,
  onClose,
  onEdit,
  t,
}: BookDetailDialogContentProps) {
  const stockAvailable = book.stockQuantity > 0

  return (
    <div className="space-y-6">
      <div className="px-12">
        <h2 className="text-center font-heading text-2xl font-bold text-foreground">
          {t('admin.books.detailTitle')}
        </h2>
      </div>

      <div className="grid gap-6 lg:grid-cols-[240px_minmax(0,1fr)]">
        <div className="overflow-hidden rounded-[28px] border border-border/60 bg-background/60 shadow-[0_20px_50px_rgba(2,6,23,0.22)]">
          <img
            src={getBookCoverUrl(book.cover)}
            alt={book.title}
            className="aspect-[3/4] w-full object-cover"
          />
        </div>

        <div className="space-y-5">
          <div>
            <p className="text-sm font-medium text-primary">
              {t('admin.books.sectionLabel')}
            </p>
            <h3 className="mt-2 font-heading text-3xl font-bold text-foreground">
              {book.title}
            </h3>
          </div>

          <div className="flex flex-wrap gap-3">
            <Badge
              variant="outline"
              className="rounded-2xl border-primary/20 bg-primary/12 px-3 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
            >
              <Tag className="mr-2 h-4 w-4" />
              {getCategoryLabel(book.category, t)}
            </Badge>
            <Badge
              variant="outline"
              className={cn(
                'rounded-2xl px-3 py-1.5 text-sm font-semibold',
                stockAvailable
                  ? 'border-emerald-400/30 bg-emerald-400/10 text-emerald-300'
                  : 'border-destructive/30 bg-destructive/10 text-destructive',
              )}
            >
              <Package2 className="mr-2 h-4 w-4" />
              {stockAvailable
                ? t('admin.books.inStock')
                : t('admin.books.outOfStock')}
            </Badge>
          </div>

          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            <BookMetaCard
              icon={User2}
              label={t('admin.books.fields.author')}
              value={book.author || t('book.fallback.author')}
              className="sm:col-span-2 xl:col-span-2"
              valueClassName="truncate whitespace-nowrap"
            />
            <BookMetaCard
              icon={Building2}
              label={t('admin.books.fields.publisher')}
              value={book.publisher || t('book.fallback.publisher')}
            />
            <BookMetaCard
              icon={Tag}
              label={t('admin.books.fields.category')}
              value={getCategoryLabel(book.category, t)}
            />
            <BookMetaCard
              icon={Wallet}
              label={t('admin.books.fields.price')}
              value={formatCurrency(book.price)}
            />
            <BookMetaCard
              icon={Package2}
              label={t('admin.books.fields.stockQuantity')}
              value={`${formatNumber(book.stockQuantity)} ${t('admin.books.stockUnit')}`}
            />
          </div>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <BookMetaCard
          icon={CalendarDays}
          label={t('common.createdAt')}
          value={formatDate(book.createdAt)}
        />
        <BookMetaCard
          icon={RefreshCw}
          label={t('common.updatedAt')}
          value={formatDate(book.updatedAt)}
        />
      </div>

      <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
        <p className="text-sm font-semibold text-foreground">
          {t('admin.books.fields.description')}
        </p>
        <p className="mt-3 whitespace-pre-wrap text-sm leading-7 text-muted-foreground">
          {book.description || t('admin.books.emptyDescription')}
        </p>
      </div>

      <div className="flex flex-wrap justify-end gap-3">
        <Button type="button" variant="outline" onClick={onClose} className="rounded-2xl">
          {t('common.close')}
        </Button>
        <Button type="button" onClick={onEdit} className="rounded-2xl">
          <Edit2 className="mr-2 h-4 w-4" />
          {t('common.edit')}
        </Button>
      </div>
    </div>
  )
}

type BookDeleteDialogContentProps = {
  book: Book
  isDeleting: boolean
  onClose: () => void
  onConfirm: () => void
  t: (key: string, params?: Record<string, number | string>) => string
}

function BookDeleteDialogContent({
  book,
  isDeleting,
  onClose,
  onConfirm,
  t,
}: BookDeleteDialogContentProps) {
  return (
    <div className="space-y-6">
      <div className="px-12 text-center">
        <div className="mx-auto flex size-18 items-center justify-center rounded-[22px] border border-destructive/30 bg-destructive/10 text-destructive shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]">
          <AlertTriangle className="h-8 w-8" />
        </div>
        <h2 className="mt-5 font-heading text-2xl font-bold text-foreground">
          {t('admin.books.deleteTitle')}
        </h2>
        <p className="mt-3 text-sm leading-7 text-muted-foreground">
          {t('admin.books.confirmDelete', { title: book.title })}
        </p>
      </div>

      <div className="flex items-center gap-4 rounded-[24px] border border-border/60 bg-background/55 p-5">
        <div className="overflow-hidden rounded-[18px] border border-border/60 bg-background/70">
          <img
            src={getBookCoverUrl(book.cover)}
            alt={book.title}
            className="h-24 w-16 object-cover"
          />
        </div>
        <div className="min-w-0">
          <p className="text-sm font-medium text-primary">
            {t('admin.books.sectionLabel')}
          </p>
          <p className="mt-1 truncate text-xl font-semibold text-foreground">
            {book.title}
          </p>
          <p className="mt-2 text-sm text-muted-foreground">
            {book.author || t('book.fallback.author')}
          </p>
        </div>
      </div>

      <p className="text-sm text-muted-foreground">
        {t('admin.books.deleteDescription')}
      </p>

      <div className="flex flex-wrap justify-end gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={onClose}
          disabled={isDeleting}
          className="rounded-2xl"
        >
          {t('common.cancel')}
        </Button>
        <Button
          type="button"
          variant="destructive"
          onClick={onConfirm}
          disabled={isDeleting}
          className="rounded-2xl"
        >
          <Trash2 className="mr-2 h-4 w-4" />
          {isDeleting ? t('common.processing') : t('common.delete')}
        </Button>
      </div>
    </div>
  )
}

function BookMetaCard({
  icon: Icon,
  label,
  value,
  className,
  valueClassName,
}: {
  icon: LucideIcon
  label: string
  value: string
  className?: string
  valueClassName?: string
}) {
  return (
    <div
      className={cn(
        'rounded-[24px] border border-border/60 bg-background/55 p-5',
        className,
      )}
    >
      <div className="flex items-center gap-3">
        <div className="flex size-11 items-center justify-center rounded-2xl border border-border/60 bg-background/70 text-muted-foreground">
          <Icon className="h-4 w-4" />
        </div>
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
            {label}
          </p>
          <p
            className={cn(
              'mt-2 text-base font-semibold text-foreground',
              valueClassName,
            )}
          >
            {value}
          </p>
        </div>
      </div>
    </div>
  )
}

type ReferenceSelectFieldProps = {
  id: string
  label: string
  value: string
  onValueChange: (value: string) => void
  placeholder: string
  options: Array<{ value: string; label: string }>
}

function ReferenceSelectField({
  id,
  label,
  value,
  onValueChange,
  placeholder,
  options,
}: ReferenceSelectFieldProps) {
  const selectedOption = options.find((option) => option.value === value)

  return (
    <div>
      <Label htmlFor={id}>{label}</Label>
      <Select value={value} onValueChange={(nextValue) => onValueChange(nextValue ?? '')}>
        <SelectTrigger id={id} className="mt-2 h-12 w-full rounded-2xl bg-background/60">
          <SelectValue placeholder={placeholder}>
            {selectedOption?.label}
          </SelectValue>
        </SelectTrigger>
        <SelectContent>
          {options.map((option) => (
            <SelectItem key={option.value} value={option.value}>
              {option.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}

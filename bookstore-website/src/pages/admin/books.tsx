import { useEffect, useMemo, useState } from 'react'
import { Edit2, Plus, Search, Trash2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
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
import { getErrorMessage } from '@/utils'
import { getCategoryLabel } from '@/utils/i18n'

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

export default function AdminBooksPage() {
  const { t, formatCurrency, formatNumber } = useLanguage()
  const [books, setBooks] = useState<Book[]>([])
  const [references, setReferences] = useState<BookReferenceData>(initialReferences)
  const [searchTerm, setSearchTerm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [editingBookId, setEditingBookId] = useState<string | null>(null)
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [form, setForm] = useState<BookFormState>(initialFormState)

  const hasReferenceData =
    references.categories.length > 0 &&
    references.authors.length > 0 &&
    references.publishers.length > 0

  const filteredBooks = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return books
    }

    return books.filter((book) =>
      [book.title, book.author, book.category, book.publisher]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [books, searchTerm])

  useEffect(() => {
    void loadAdminBooksData()
  }, [])

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

  function resetForm() {
    setEditingBookId(null)
    setForm(initialFormState)
    setIsFormOpen(false)
  }

  function handleCreateMode() {
    setEditingBookId(null)
    setForm(initialFormState)
    setIsFormOpen(true)
  }

  function handleEditBook(book: Book) {
    setEditingBookId(book.id)
    setForm({
      title: book.title,
      description: book.description ?? '',
      price: String(book.price),
      stockQuantity: String(book.stockQuantity),
      imageUrl: book.cover ?? '',
      categoryId: book.categoryId,
      authorId: book.authorId,
      publisherId: book.publisherId,
    })
    setIsFormOpen(true)
  }

  function handleFormChange(
    field: keyof BookFormState,
    value: string,
  ) {
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

      if (editingBookId) {
        await updateBook(editingBookId, payload)
        toast.success(t('admin.books.updateSuccess'))
      } else {
        await createBook(payload)
        toast.success(t('admin.books.createSuccess'))
      }

      await loadAdminBooksData()
      resetForm()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteBook(book: Book) {
    const confirmed = window.confirm(
      t('admin.books.confirmDelete', { title: book.title }),
    )

    if (!confirmed) {
      return
    }

    try {
      await deleteBook(book.id)
      toast.success(t('admin.books.deleteSuccess'))
      await loadAdminBooksData()

      if (editingBookId === book.id) {
        resetForm()
      }
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    }
  }

  return (
    <AdminLayout>
      <div>
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <h1 className="font-heading text-3xl font-bold text-foreground">
              {t('admin.books.title')}
            </h1>
            <p className="mt-2 text-muted-foreground">
              {t('admin.books.totalBooks', {
                count: formatNumber(books.length),
              })}
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <Link to="/admin/references">
              <Button variant="outline" size="lg">
                {t('admin.books.manageReferences')}
              </Button>
            </Link>
            <Button size="lg" onClick={handleCreateMode}>
              <Plus className="mr-2 h-4 w-4" />
              {editingBookId ? t('admin.books.addBook') : t('admin.books.addBook')}
            </Button>
          </div>
        </div>

        {!hasReferenceData && !isLoading && (
          <div className="mt-8 rounded-2xl border border-dashed border-amber-400/50 bg-amber-50/60 p-5 text-sm text-amber-900 dark:bg-amber-950/20 dark:text-amber-200">
            <p className="font-semibold">{t('admin.books.referencesMissing')}</p>
            <p className="mt-2">{t('admin.books.referencesMissingDescription')}</p>
          </div>
        )}

        {isFormOpen && (
          <form
            onSubmit={handleSubmit}
            className="mt-8 rounded-2xl border border-border bg-card p-6"
          >
            <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <h2 className="font-heading text-2xl font-bold text-foreground">
                  {editingBookId
                    ? t('admin.books.editBook')
                    : t('admin.books.addBook')}
                </h2>
                <p className="mt-2 text-sm text-muted-foreground">
                  {t('admin.books.formDescription')}
                </p>
              </div>

              <div className="flex gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={resetForm}
                  disabled={isSubmitting}
                >
                  {t('common.cancel')}
                </Button>
                <Button type="submit" disabled={isSubmitting || !hasReferenceData}>
                  {isSubmitting ? t('common.processing') : t('common.save')}
                </Button>
              </div>
            </div>

            <div className="mt-6 grid gap-5 lg:grid-cols-3">
              <div className="lg:col-span-2 space-y-5">
                <div>
                  <Label htmlFor="bookTitle">{t('admin.books.fields.title')}</Label>
                  <Input
                    id="bookTitle"
                    value={form.title}
                    onChange={(event) =>
                      handleFormChange('title', event.currentTarget.value)
                    }
                    className="mt-2"
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
                    className="mt-2"
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
                      className="mt-2"
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
                      className="mt-2"
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
                    className="mt-2"
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

              <div className="rounded-2xl border border-dashed border-border bg-muted/30 p-4">
                <p className="text-sm font-semibold text-foreground">
                  {t('admin.books.previewTitle')}
                </p>
                <div className="mt-4 flex flex-col gap-4">
                  <div className="overflow-hidden rounded-xl border border-border bg-background">
                    <img
                      src={form.imageUrl.trim() || '/placeholder.svg'}
                      alt={form.title || t('admin.books.fields.title')}
                      className="aspect-[3/4] w-full object-cover"
                    />
                  </div>
                  <div>
                    <p className="font-semibold text-foreground">
                      {form.title || t('admin.books.emptyPreviewTitle')}
                    </p>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {form.description || t('admin.books.emptyPreviewDescription')}
                    </p>
                    <p className="mt-3 text-lg font-bold text-primary">
                      {formatCurrency(Number(form.price || 0))}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </form>
        )}

        <div className="mt-8">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder={t('admin.books.searchPlaceholder')}
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        <div className="mt-8 rounded-lg border border-border bg-card">
          {isLoading ? (
            <div className="px-6 py-8 text-center">
              <p className="text-muted-foreground">{t('common.loading')}</p>
            </div>
          ) : error ? (
            <div className="px-6 py-8 text-center">
              <p className="font-semibold text-foreground">
                {t('book.listing.errorTitle')}
              </p>
              <p className="mt-2 text-sm text-muted-foreground">{error}</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.book')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.author')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.category')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.price')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.stock')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.actions')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredBooks.map((book) => (
                    <tr key={book.id} className="border-b border-border">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <img
                            src={book.cover || '/placeholder.svg'}
                            alt={book.title}
                            className="h-14 w-10 rounded object-cover"
                          />
                          <div>
                            <p className="text-sm font-medium text-foreground">
                              {book.title}
                            </p>
                            <p className="mt-1 text-xs text-muted-foreground">
                              {book.publisher || t('book.fallback.publisher')}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {book.author || t('book.fallback.author')}
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {getCategoryLabel(book.category, t)}
                      </td>
                      <td className="px-6 py-4 text-sm font-medium text-foreground">
                        {formatCurrency(book.price)}
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {formatNumber(book.stockQuantity)}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <div className="flex gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleEditBook(book)}
                          >
                            <Edit2 className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => void handleDeleteBook(book)}
                          >
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {!isLoading && !error && filteredBooks.length === 0 && (
            <div className="px-6 py-8 text-center">
              <p className="text-muted-foreground">{t('admin.books.empty')}</p>
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
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
  return (
    <div>
      <Label htmlFor={id}>{label}</Label>
      <Select value={value} onValueChange={(nextValue) => onValueChange(nextValue ?? '')}>
        <SelectTrigger id={id} className="mt-2 w-full">
          <SelectValue placeholder={placeholder} />
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

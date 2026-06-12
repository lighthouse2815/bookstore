import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
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

export function useAdminBooksPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [books, setBooks] = useState<Book[]>([])
  const [references, setReferences] = useState<BookReferenceData>(
    initialReferences,
  )
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
        [
          book.title,
          book.author,
          book.category,
          book.publisher,
          book.description ?? '',
        ]
          .join(' ')
          .toLowerCase()
          .includes(keyword)

      const matchesCategory =
        selectedCategoryId === 'all' || book.categoryId === selectedCategoryId

      return matchesSearch && matchesCategory
    })
  }, [books, searchTerm, selectedCategoryId])

  const dialogSizeClassName =
    dialogMode === 'delete'
      ? 'max-w-xl'
      : dialogMode === 'view'
        ? 'max-w-[1180px]'
        : 'max-w-5xl'

  useEffect(() => {
    let isCancelled = false

    async function loadAdminBooksData() {
      setIsLoading(true)

      try {
        const [catalog, referenceData] = await Promise.all([
          getBookCatalog(),
          getBookReferences(),
        ])

        if (isCancelled) {
          return
        }

        setBooks(catalog.books)
        setReferences(referenceData)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, t('book.listing.errorTitle')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadAdminBooksData()

    return () => {
      isCancelled = true
    }
  }, [t])

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

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function handleCategoryChange(nextValue: string | null) {
    setSelectedCategoryId(nextValue ?? 'all')
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

  async function reloadBooks() {
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

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
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

      await reloadBooks()
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
      await reloadBooks()
      closeDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsDeleting(false)
    }
  }

  return {
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    books,
    references,
    searchTerm,
    selectedCategoryId,
    error,
    isLoading,
    isSubmitting,
    isDeleting,
    dialogMode,
    selectedBook,
    form,
    hasReferenceData,
    isDialogLocked,
    filteredBooks,
    dialogSizeClassName,
    handleSearchTermChange,
    handleCategoryChange,
    openCreateDialog,
    openViewDialog,
    openEditDialog,
    openEditFromDetail,
    openDeleteDialog,
    closeDialog,
    handleFormChange,
    handleSubmit,
    confirmDelete,
  }
}

import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { uploadManagedFile } from '@/services/file-service'
import {
  createBook,
  deleteBook,
  getBookCatalogPage,
  getBookReferences,
  updateBook,
} from '@/services/book-service'
import type { Book, BookReferenceData } from '@/types/book'
import { getErrorMessage } from '@/utils'

type BookDialogMode = 'create' | 'view' | 'edit' | 'delete'

type BookFormImage = {
  id?: string
  fileAssetId: string
  previewUrl: string
  altText: string
  primaryImage: boolean
}

type BookFormState = {
  title: string
  description: string
  price: string
  stockQuantity: string
  images: BookFormImage[]
  categoryId: string
  authorId: string
  publisherId: string
}

type BookFormTextField = Exclude<keyof BookFormState, 'images'>

const initialFormState: BookFormState = {
  title: '',
  description: '',
  price: '',
  stockQuantity: '',
  images: [],
  categoryId: '',
  authorId: '',
  publisherId: '',
}

const initialReferences: BookReferenceData = {
  categories: [],
  authors: [],
  publishers: [],
}

const PAGE_SIZE = 10

export function useAdminBooksPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [books, setBooks] = useState<Book[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [references, setReferences] = useState<BookReferenceData>(
    initialReferences,
  )
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategoryId, setSelectedCategoryId] = useState('all')
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isUploadingImage, setIsUploadingImage] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [dialogMode, setDialogMode] = useState<BookDialogMode | null>(null)
  const [selectedBook, setSelectedBook] = useState<Book | null>(null)
  const [form, setForm] = useState<BookFormState>(initialFormState)

  const hasReferenceData =
    references.categories.length > 0 &&
    references.authors.length > 0 &&
    references.publishers.length > 0

  const isDialogLocked =
    (dialogMode === 'delete' && isDeleting) || isUploadingImage || isSubmitting

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
          getBookCatalogPage({ page, size: PAGE_SIZE }),
          getBookReferences(),
        ])

        if (isCancelled) {
          return
        }

        setBooks(catalog.books)
        setTotalCount(catalog.totalCount)
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
  }, [page, t])

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
    setPage(0)
  }

  function handleCategoryChange(nextValue: string | null) {
    setSelectedCategoryId(nextValue ?? 'all')
    setPage(0)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
  }

  function createFormState(book: Book): BookFormState {
    const hasPrimaryImage = book.images.some((image) => image.primaryImage)

    return {
      title: book.title,
      description: book.description ?? '',
      price: String(book.price),
      stockQuantity: String(book.stockQuantity),
      images: book.images.map((image, index) => ({
        id: image.id,
        fileAssetId: image.fileAssetId,
        previewUrl: image.imageUrl,
        altText: image.altText ?? '',
        primaryImage: hasPrimaryImage ? image.primaryImage : index === 0,
      })),
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

  function handleFormChange(field: BookFormTextField, value: string) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  async function handleImageFilesChange(files: File[]) {
    if (files.length === 0) {
      return
    }

    setIsUploadingImage(true)

    try {
      const uploadedImages: BookFormImage[] = []
      let failedUploadCount = 0

      for (const file of files) {
        try {
          const uploadedFile = await uploadManagedFile(file, {
            purpose: 'BOOK_IMAGE',
            visibility: 'PUBLIC',
            bookId: dialogMode === 'edit' ? selectedBook?.id : undefined,
          })

          uploadedImages.push({
            fileAssetId: uploadedFile.id,
            previewUrl: uploadedFile.publicUrl ?? URL.createObjectURL(file),
            altText: '',
            primaryImage: false,
          })
        } catch {
          failedUploadCount += 1
        }
      }

      if (uploadedImages.length > 0) {
        setForm((currentForm) => {
          const shouldAssignPrimaryImage = currentForm.images.length === 0
          const normalizedUploadedImages = uploadedImages.map((image, index) => ({
            ...image,
            primaryImage: shouldAssignPrimaryImage && index === 0,
          }))

          return {
            ...currentForm,
            images: [...currentForm.images, ...normalizedUploadedImages],
          }
        })
      }

      if (failedUploadCount > 0) {
        toast.error(
          t('admin.books.imageUploadPartial', {
            failed: failedUploadCount,
            total: files.length,
          }),
        )
      }
    } finally {
      setIsUploadingImage(false)
    }
  }

  function handleBookImageAltTextChange(imageIndex: number, value: string) {
    setForm((currentForm) => ({
      ...currentForm,
      images: currentForm.images.map((image, index) =>
        index === imageIndex ? { ...image, altText: value } : image,
      ),
    }))
  }

  function setPrimaryBookImage(imageIndex: number) {
    setForm((currentForm) => ({
      ...currentForm,
      images: currentForm.images.map((image, index) => ({
        ...image,
        primaryImage: index === imageIndex,
      })),
    }))
  }

  function moveBookImage(imageIndex: number, direction: -1 | 1) {
    setForm((currentForm) => {
      const nextIndex = imageIndex + direction

      if (nextIndex < 0 || nextIndex >= currentForm.images.length) {
        return currentForm
      }

      const images = [...currentForm.images]
      ;[images[imageIndex], images[nextIndex]] = [images[nextIndex], images[imageIndex]]

      return { ...currentForm, images }
    })
  }

  function removeBookImage(imageIndex: number) {
    setForm((currentForm) => {
      const removedImageWasPrimary = currentForm.images[imageIndex]?.primaryImage === true
      const images = currentForm.images.filter((_, index) => index !== imageIndex)

      if (removedImageWasPrimary && images.length > 0) {
        images[0] = { ...images[0], primaryImage: true }
      }

      return { ...currentForm, images }
    })
  }

  async function reloadBooks() {
    setIsLoading(true)

    try {
      const [catalog, referenceData] = await Promise.all([
        getBookCatalogPage({ page, size: PAGE_SIZE }),
        getBookReferences(),
      ])

      setBooks(catalog.books)
      setTotalCount(catalog.totalCount)
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
        images: form.images.map((image, index) => ({
          id: image.id,
          fileAssetId: image.fileAssetId,
          primaryImage: image.primaryImage,
          sortOrder: index,
          altText: image.altText.trim() || null,
        })),
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
    page,
    pageSize: PAGE_SIZE,
    totalCount,
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
    isUploadingImage,
    filteredBooks,
    dialogSizeClassName,
    handleSearchTermChange,
    handleCategoryChange,
    handlePageChange,
    openCreateDialog,
    openViewDialog,
    openEditDialog,
    openEditFromDetail,
    openDeleteDialog,
    closeDialog,
    handleFormChange,
    handleImageFilesChange,
    handleBookImageAltTextChange,
    setPrimaryBookImage,
    moveBookImage,
    removeBookImage,
    handleSubmit,
    confirmDelete,
  }
}

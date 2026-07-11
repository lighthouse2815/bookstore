import {
  useEffect,
  useMemo,
  useState,
  type ChangeEvent,
  type FormEvent,
} from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { uploadManagedFile } from '@/services/file-service'
import { getBookReferences } from '@/services/book-service'
import {
  createAuthor,
  createCategory,
  createPublisher,
  deleteAuthor,
  deleteCategory,
  deletePublisher,
  getAuthorsPage,
  getCategoriesPage,
  getPublishersPage,
  updateAuthor,
  updateCategory,
  updatePublisher,
} from '@/services/reference-service'
import type {
  AuthorResponse,
  BookReferenceData,
  CategoryResponse,
  PublisherResponse,
} from '@/types/book'
import { getErrorMessage } from '@/utils'

export type ReferenceSectionKey = 'categories' | 'authors' | 'publishers'
export type ReferenceItem = CategoryResponse | AuthorResponse | PublisherResponse

type ReferenceDialogMode = 'create' | 'view' | 'edit' | 'delete'

export type ReferenceFormState = {
  id: string | null
  name: string
  description: string
  avatarFileAssetId: string
  avatarPreviewUrl: string
  referenceImageFileAssetId: string
  referenceImagePreviewUrl: string
  birthYear: string
  deathYear: string
}

const initialFormState: ReferenceFormState = {
  id: null,
  name: '',
  description: '',
  avatarFileAssetId: '',
  avatarPreviewUrl: '',
  referenceImageFileAssetId: '',
  referenceImagePreviewUrl: '',
  birthYear: '',
  deathYear: '',
}

const PAGE_SIZE = 10

export function useAdminReferenceManagementPage(
  sectionKey: ReferenceSectionKey,
) {
  const { t, formatDate, formatNumber } = useLanguage()
  const [items, setItems] = useState<ReferenceItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState<ReferenceFormState>(initialFormState)
  const [searchTerm, setSearchTerm] = useState('')
  const [dialogMode, setDialogMode] = useState<ReferenceDialogMode | null>(null)
  const [selectedItem, setSelectedItem] = useState<ReferenceItem | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [isUploadingReferenceImage, setIsUploadingReferenceImage] = useState(false)
  const [page, setPage] = useState(0)
  const [serverTotalCount, setServerTotalCount] = useState(0)

  const filteredItems = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return items
    }

    return items.filter((item) =>
      [item.name, getReferenceDescription(sectionKey, item)]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [items, searchTerm, sectionKey])

  const paginatedItems = useMemo(
    () =>
      searchTerm.trim()
        ? filteredItems.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)
        : filteredItems,
    [filteredItems, page, searchTerm],
  )

  const totalCount = searchTerm.trim()
    ? filteredItems.length
    : serverTotalCount

  useEffect(() => {
    const lastPage = Math.max(0, Math.ceil(totalCount / PAGE_SIZE) - 1)
    if (page > lastPage) {
      setPage(lastPage)
    }
  }, [page, totalCount])

  useEffect(() => {
    let isCancelled = false

    async function loadItems() {
      setIsLoading(true)

      try {
        if (searchTerm.trim()) {
          const response = await getBookReferences()
          const sectionItems = getSectionItems(sectionKey, response)
          if (isCancelled) {
            return
          }
          setItems(sectionItems)
          setServerTotalCount(sectionItems.length)
        } else {
          const response = await getSectionPage(sectionKey, page, PAGE_SIZE)
          if (isCancelled) {
            return
          }
          setItems(response.items)
          setServerTotalCount(response.totalCount)
        }
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, t('checkout.error')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadItems()

    return () => {
      isCancelled = true
    }
  }, [page, searchTerm, sectionKey, t])

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

  function handleFormChange(field: keyof ReferenceFormState, value: string) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  async function handleReferenceImageFileChange(file: File | null) {
    if (!file) {
      return
    }

    setIsUploadingReferenceImage(true)

    try {
      const purpose = getReferenceImagePurpose(sectionKey)
      const uploadedFile = await uploadManagedFile(file, {
        purpose,
        visibility: 'PUBLIC',
        authorId: sectionKey === 'authors' ? form.id ?? undefined : undefined,
      })

      setForm((currentForm) => ({
        ...currentForm,
        ...(sectionKey === 'authors'
          ? {
              avatarFileAssetId: uploadedFile.id,
              avatarPreviewUrl: uploadedFile.publicUrl ?? URL.createObjectURL(file),
            }
          : {
              referenceImageFileAssetId: uploadedFile.id,
              referenceImagePreviewUrl:
                uploadedFile.publicUrl ?? URL.createObjectURL(file),
            }),
      }))
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsUploadingReferenceImage(false)
    }
  }

  function resetDialog() {
    setDialogMode(null)
    setSelectedItem(null)
    setForm(initialFormState)
  }

  function closeDialog() {
    if (isSubmitting || isDeleting) {
      return
    }

    resetDialog()
  }

  function openCreateDialog() {
    setSelectedItem(null)
    setForm(initialFormState)
    setDialogMode('create')
  }

  function openViewDialog(item: ReferenceItem) {
    setSelectedItem(item)
    setDialogMode('view')
  }

  function openEditDialog(item: ReferenceItem) {
    setSelectedItem(item)
    setForm({
      id: item.id,
      name: item.name,
      description: getReferenceDescription(sectionKey, item),
      avatarFileAssetId:
        sectionKey === 'authors' && 'avatarFileAssetId' in item
          ? item.avatarFileAssetId ?? ''
          : '',
      avatarPreviewUrl:
        sectionKey === 'authors' && 'avatarUrl' in item
          ? item.avatarUrl ?? ''
          : '',
      referenceImageFileAssetId: getReferenceImageFileAssetId(sectionKey, item),
      referenceImagePreviewUrl: getReferenceImageUrl(sectionKey, item),
      birthYear:
        sectionKey === 'authors' && 'birthYear' in item && item.birthYear
          ? String(item.birthYear)
          : '',
      deathYear:
        sectionKey === 'authors' && 'deathYear' in item && item.deathYear
          ? String(item.deathYear)
          : '',
    })
    setDialogMode('edit')
  }

  function openEditFromDetail() {
    if (!selectedItem) {
      return
    }

    openEditDialog(selectedItem)
  }

  function openDeleteDialog(item: ReferenceItem) {
    setSelectedItem(item)
    setDialogMode('delete')
  }

  async function reloadItems() {
    setIsLoading(true)

    try {
      if (searchTerm.trim()) {
        const response = await getBookReferences()
        const sectionItems = getSectionItems(sectionKey, response)
        setItems(sectionItems)
        setServerTotalCount(sectionItems.length)
      } else {
        const response = await getSectionPage(sectionKey, page, PAGE_SIZE)
        setItems(response.items)
        setServerTotalCount(response.totalCount)
      }
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsLoading(false)
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    setIsSubmitting(true)

    try {
      switch (sectionKey) {
        case 'categories':
          if (form.id) {
            await updateCategory(form.id, {
              name: form.name.trim(),
              description: form.description.trim() || null,
              imageFileAssetId: toNullableString(form.referenceImageFileAssetId),
            })
          } else {
            await createCategory({
              name: form.name.trim(),
              description: form.description.trim() || null,
              imageFileAssetId: toNullableString(form.referenceImageFileAssetId),
            })
          }
          break
        case 'authors':
          if (form.id) {
            await updateAuthor(form.id, {
              name: form.name.trim(),
              biography: form.description.trim() || null,
              avatarFileAssetId: toNullableString(form.avatarFileAssetId),
              birthYear: toNullableNumber(form.birthYear),
              deathYear: toNullableNumber(form.deathYear),
            })
          } else {
            await createAuthor({
              name: form.name.trim(),
              biography: form.description.trim() || null,
              avatarFileAssetId: toNullableString(form.avatarFileAssetId),
              birthYear: toNullableNumber(form.birthYear),
              deathYear: toNullableNumber(form.deathYear),
            })
          }
          break
        case 'publishers':
          if (form.id) {
            await updatePublisher(form.id, {
              name: form.name.trim(),
              description: form.description.trim() || null,
              logoFileAssetId: toNullableString(form.referenceImageFileAssetId),
            })
          } else {
            await createPublisher({
              name: form.name.trim(),
              description: form.description.trim() || null,
              logoFileAssetId: toNullableString(form.referenceImageFileAssetId),
            })
          }
          break
      }

      toast.success(t('admin.references.saveSuccess'))
      await reloadItems()
      resetDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteConfirm() {
    if (!selectedItem) {
      return
    }

    setIsDeleting(true)

    try {
      switch (sectionKey) {
        case 'categories':
          await deleteCategory(selectedItem.id)
          break
        case 'authors':
          await deleteAuthor(selectedItem.id)
          break
        case 'publishers':
          await deletePublisher(selectedItem.id)
          break
      }

      toast.success(t('admin.references.deleteSuccess'))
      await reloadItems()
      resetDialog()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsDeleting(false)
    }
  }

  return {
    t,
    formatDate,
    formatNumber,
    items,
    isLoading,
    error,
    form,
    searchTerm,
    dialogMode,
    selectedItem,
    isSubmitting,
    isDeleting,
    filteredItems,
    paginatedItems,
    totalCount,
    page,
    pageSize: PAGE_SIZE,
    isDialogLocked: dialogMode === 'delete' && isDeleting,
    isUploadingReferenceImage,
    handleSearchTermChange,
    handleFormChange,
    handleReferenceImageFileChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    openEditDialog,
    openEditFromDetail,
    openDeleteDialog,
    handleSubmit,
    handleDeleteConfirm,
    handlePageChange: setPage,
  }
}

function getReferenceImagePurpose(sectionKey: ReferenceSectionKey) {
  switch (sectionKey) {
    case 'categories':
      return 'CATEGORY_IMAGE' as const
    case 'authors':
      return 'AUTHOR_AVATAR' as const
    case 'publishers':
      return 'PUBLISHER_LOGO' as const
  }
}

export function getReferenceImageFileAssetId(
  sectionKey: ReferenceSectionKey,
  item: ReferenceItem,
) {
  if (sectionKey === 'authors' && 'avatarFileAssetId' in item) {
    return item.avatarFileAssetId ?? ''
  }
  if (sectionKey === 'categories' && 'imageFileAssetId' in item) {
    return item.imageFileAssetId ?? ''
  }
  if (sectionKey === 'publishers' && 'logoFileAssetId' in item) {
    return item.logoFileAssetId ?? ''
  }
  return ''
}

export function getReferenceImageUrl(
  sectionKey: ReferenceSectionKey,
  item: ReferenceItem,
) {
  if (sectionKey === 'authors' && 'avatarUrl' in item) {
    return item.avatarUrl ?? ''
  }
  if (sectionKey === 'categories' && 'imageUrl' in item) {
    return item.imageUrl ?? ''
  }
  if (sectionKey === 'publishers' && 'logoUrl' in item) {
    return item.logoUrl ?? ''
  }
  return ''
}

function getSectionPage(sectionKey: ReferenceSectionKey, page: number, size: number) {
  switch (sectionKey) {
    case 'categories':
      return getCategoriesPage({ page, size })
    case 'authors':
      return getAuthorsPage({ page, size })
    case 'publishers':
      return getPublishersPage({ page, size })
  }
}

function getSectionItems(
  sectionKey: ReferenceSectionKey,
  referenceData: BookReferenceData,
): ReferenceItem[] {
  switch (sectionKey) {
    case 'categories':
      return referenceData.categories
    case 'authors':
      return referenceData.authors
    case 'publishers':
      return referenceData.publishers
  }
}

export function getReferenceDescription(
  sectionKey: ReferenceSectionKey,
  item: ReferenceItem,
) {
  if (sectionKey === 'authors') {
    return ('biography' in item ? item.biography : null) ?? ''
  }

  return ('description' in item ? item.description : null) ?? ''
}

function toNullableString(value: string) {
  const trimmedValue = value.trim()
  return trimmedValue === '' ? null : trimmedValue
}

function toNullableNumber(value: string) {
  const trimmedValue = value.trim()
  if (trimmedValue === '') {
    return null
  }

  const parsedValue = Number(trimmedValue)
  return Number.isFinite(parsedValue) ? parsedValue : null
}

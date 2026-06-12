import {
  useEffect,
  useMemo,
  useState,
  type ChangeEvent,
  type FormEvent,
} from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { getBookReferences } from '@/services/book-service'
import {
  createAuthor,
  createCategory,
  createPublisher,
  deleteAuthor,
  deleteCategory,
  deletePublisher,
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
}

const initialFormState: ReferenceFormState = {
  id: null,
  name: '',
  description: '',
}

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

  useEffect(() => {
    let isCancelled = false

    async function loadItems() {
      setIsLoading(true)

      try {
        const response = await getBookReferences()

        if (isCancelled) {
          return
        }

        setItems(getSectionItems(sectionKey, response))
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
  }, [sectionKey, t])

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

  function handleFormChange(field: keyof ReferenceFormState, value: string) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
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
      const response = await getBookReferences()
      setItems(getSectionItems(sectionKey, response))
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
            })
          } else {
            await createCategory({
              name: form.name.trim(),
              description: form.description.trim() || null,
            })
          }
          break
        case 'authors':
          if (form.id) {
            await updateAuthor(form.id, {
              name: form.name.trim(),
              biography: form.description.trim() || null,
            })
          } else {
            await createAuthor({
              name: form.name.trim(),
              biography: form.description.trim() || null,
            })
          }
          break
        case 'publishers':
          if (form.id) {
            await updatePublisher(form.id, {
              name: form.name.trim(),
              description: form.description.trim() || null,
            })
          } else {
            await createPublisher({
              name: form.name.trim(),
              description: form.description.trim() || null,
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
    isDialogLocked: dialogMode === 'delete' && isDeleting,
    handleSearchTermChange,
    handleFormChange,
    closeDialog,
    openCreateDialog,
    openViewDialog,
    openEditDialog,
    openEditFromDetail,
    openDeleteDialog,
    handleSubmit,
    handleDeleteConfirm,
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

import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  createAdminDigitalAsset,
  deleteAdminDigitalAsset,
  getAdminDigitalAssetsByBookId,
  updateAdminDigitalAsset,
} from '@/services/digital-library-service'
import type {
  DigitalAssetFormat,
  DigitalAssetResponse,
  UpsertDigitalAssetRequest,
} from '@/types/digital-library'
import { getErrorMessage } from '@/utils'

type DigitalAssetFormState = {
  format: DigitalAssetFormat
  title: string
  fileName: string
  storageKey: string
  mimeType: string
  fileSize: string
  checksum: string
  sampleStorageKey: string
  price: string
  downloadAllowed: boolean
  published: boolean
}

type DigitalAssetActionMode = 'create' | 'edit' | 'delete' | null

const initialFormState: DigitalAssetFormState = {
  format: 'PDF',
  title: '',
  fileName: '',
  storageKey: '',
  mimeType: '',
  fileSize: '',
  checksum: '',
  sampleStorageKey: '',
  price: '',
  downloadAllowed: false,
  published: false,
}

export function useAdminBookDigitalAssets(bookId?: string) {
  const { language } = useLanguage()
  const [assets, setAssets] = useState<DigitalAssetResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionMode, setActionMode] = useState<DigitalAssetActionMode>(null)
  const [selectedAsset, setSelectedAsset] = useState<DigitalAssetResponse | null>(
    null,
  )
  const [form, setForm] = useState<DigitalAssetFormState>(initialFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  useEffect(() => {
    if (!bookId) {
      setAssets([])
      setError(null)
      setIsLoading(false)
      return
    }

    let isCancelled = false

    async function loadAssets() {
      setIsLoading(true)

      try {
        const response = await getAdminDigitalAssetsByBookId(bookId)

        if (isCancelled) {
          return
        }

        setAssets(response)
        setError(null)
      } catch (currentError) {
        if (!isCancelled) {
          setError(getErrorMessage(currentError))
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadAssets()

    return () => {
      isCancelled = true
    }
  }, [bookId])

  function openCreateForm() {
    setSelectedAsset(null)
    setForm(initialFormState)
    setActionMode('create')
  }

  function openEditForm(asset: DigitalAssetResponse) {
    setSelectedAsset(asset)
    setForm(createFormState(asset))
    setActionMode('edit')
  }

  function openDeleteDialog(asset: DigitalAssetResponse) {
    setSelectedAsset(asset)
    setActionMode('delete')
  }

  function closeAction() {
    setSelectedAsset(null)
    setActionMode(null)
    setForm(initialFormState)
  }

  function handleFormChange<K extends keyof DigitalAssetFormState>(
    field: K,
    value: DigitalAssetFormState[K],
  ) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  async function reloadAssets() {
    if (!bookId) {
      return
    }

    setIsLoading(true)

    try {
      const response = await getAdminDigitalAssetsByBookId(bookId)
      setAssets(response)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError))
    } finally {
      setIsLoading(false)
    }
  }

  async function submitForm() {
    if (!bookId) {
      return
    }

    const payload = buildRequestPayload(form)

    if (!payload) {
      toast.error(
        language === 'vi'
          ? 'Vui lòng kiểm tra lại các trường bắt buộc, giá và dung lượng tệp.'
          : 'Please check the required fields, price, and file size.',
      )
      return
    }

    setIsSubmitting(true)

    try {
      if (actionMode === 'edit' && selectedAsset) {
        await updateAdminDigitalAsset(bookId, selectedAsset.id, payload)
        toast.success(
          language === 'vi'
            ? 'Đã cập nhật digital asset.'
            : 'Digital asset updated.',
        )
      } else {
        await createAdminDigitalAsset(bookId, payload)
        toast.success(
          language === 'vi'
            ? 'Đã tạo digital asset.'
            : 'Digital asset created.',
        )
      }

      await reloadAssets()
      closeAction()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function confirmDelete() {
    if (!bookId || !selectedAsset) {
      return
    }

    setIsDeleting(true)

    try {
      await deleteAdminDigitalAsset(bookId, selectedAsset.id)
      toast.success(
        language === 'vi' ? 'Đã xóa digital asset.' : 'Digital asset deleted.',
      )
      await reloadAssets()
      closeAction()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsDeleting(false)
    }
  }

  return {
    assets,
    isLoading,
    error,
    actionMode,
    selectedAsset,
    form,
    isSubmitting,
    isDeleting,
    openCreateForm,
    openEditForm,
    openDeleteDialog,
    closeAction,
    handleFormChange,
    submitForm,
    confirmDelete,
  }
}

function createFormState(asset: DigitalAssetResponse): DigitalAssetFormState {
  return {
    format: asset.format,
    title: asset.title,
    fileName: asset.fileName,
    storageKey: asset.storageKey,
    mimeType: asset.mimeType,
    fileSize: String(asset.fileSize),
    checksum: asset.checksum ?? '',
    sampleStorageKey: asset.sampleStorageKey ?? '',
    price: String(asset.price),
    downloadAllowed: asset.downloadAllowed,
    published: asset.published,
  }
}

function buildRequestPayload(
  form: DigitalAssetFormState,
): UpsertDigitalAssetRequest | null {
  if (
    form.title.trim() === '' ||
    form.fileName.trim() === '' ||
    form.storageKey.trim() === '' ||
    form.mimeType.trim() === ''
  ) {
    return null
  }

  const fileSize = Number(form.fileSize)
  const price = Number(form.price)

  if (
    Number.isNaN(fileSize) ||
    fileSize < 0 ||
    Number.isNaN(price) ||
    price < 0
  ) {
    return null
  }

  return {
    format: form.format,
    title: form.title,
    fileName: form.fileName,
    storageKey: form.storageKey,
    mimeType: form.mimeType,
    fileSize,
    checksum: form.checksum,
    sampleStorageKey: form.sampleStorageKey,
    price,
    downloadAllowed: form.downloadAllowed,
    published: form.published,
  }
}

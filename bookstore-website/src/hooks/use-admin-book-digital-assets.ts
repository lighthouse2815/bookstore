import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { uploadManagedFile } from '@/services/file-service'
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
  fileAssetId: string
  fileName: string
  mimeType: string
  fileSize: string
  checksum: string
  sampleFileAssetId: string
  price: string
  downloadAllowed: boolean
  purchaseAllowed: boolean
  published: boolean
}

type DigitalAssetActionMode = 'create' | 'edit' | 'delete' | null

const initialFormState: DigitalAssetFormState = {
  format: 'PDF',
  title: '',
  fileAssetId: '',
  fileName: '',
  mimeType: '',
  fileSize: '',
  checksum: '',
  sampleFileAssetId: '',
  price: '',
  downloadAllowed: false,
  purchaseAllowed: true,
  published: false,
}

export function useAdminBookDigitalAssets(bookId?: string) {
  const { t } = useLanguage()
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
  const [isUploadingMainFile, setIsUploadingMainFile] = useState(false)
  const [isUploadingSampleFile, setIsUploadingSampleFile] = useState(false)

  useEffect(() => {
    if (!bookId) {
      setAssets([])
      setError(null)
      setIsLoading(false)
      return
    }
    const resolvedBookId = bookId

    let isCancelled = false

    async function loadAssets() {
      setIsLoading(true)

      try {
        const response = await getAdminDigitalAssetsByBookId(resolvedBookId)

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

  async function handleMainFileChange(file: File | null) {
    if (!file) {
      return
    }

    setIsUploadingMainFile(true)

    try {
      const uploadedFile = await uploadManagedFile(file, {
        purpose: 'EBOOK_FILE',
        visibility: 'PRIVATE',
        digitalAssetId: actionMode === 'edit' ? selectedAsset?.id : undefined,
      })

      setForm((currentForm) => ({
        ...currentForm,
        fileAssetId: uploadedFile.id,
        fileName: uploadedFile.originalName ?? file.name,
        mimeType: uploadedFile.contentType ?? file.type,
        fileSize: String(uploadedFile.sizeBytes ?? file.size),
        checksum: uploadedFile.checksumSha256 ?? '',
      }))
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsUploadingMainFile(false)
    }
  }

  async function handleSampleFileChange(file: File | null) {
    if (!file) {
      return
    }

    setIsUploadingSampleFile(true)

    try {
      const uploadedFile = await uploadManagedFile(file, {
        purpose: 'SAMPLE_FILE',
        visibility: 'PRIVATE',
        digitalAssetId: actionMode === 'edit' ? selectedAsset?.id : undefined,
      })

      setForm((currentForm) => ({
        ...currentForm,
        sampleFileAssetId: uploadedFile.id,
      }))
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsUploadingSampleFile(false)
    }
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
      toast.error(t('admin.digitalAssets.validationError'))
      return
    }

    setIsSubmitting(true)

    try {
      if (actionMode === 'edit' && selectedAsset) {
        await updateAdminDigitalAsset(bookId, selectedAsset.id, payload)
        toast.success(t('admin.digitalAssets.updatedSuccess'))
      } else {
        await createAdminDigitalAsset(bookId, payload)
        toast.success(t('admin.digitalAssets.createdSuccess'))
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
      toast.success(t('admin.digitalAssets.deletedSuccess'))
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
    isUploadingMainFile,
    isUploadingSampleFile,
    openCreateForm,
    openEditForm,
    openDeleteDialog,
    closeAction,
    handleFormChange,
    handleMainFileChange,
    handleSampleFileChange,
    submitForm,
    confirmDelete,
  }
}

function createFormState(asset: DigitalAssetResponse): DigitalAssetFormState {
  return {
    format: asset.format,
    title: asset.title,
    fileAssetId: asset.fileAssetId,
    fileName: asset.fileName,
    mimeType: asset.mimeType,
    fileSize: String(asset.fileSize),
    checksum: asset.checksum ?? '',
    sampleFileAssetId: asset.sampleFileAssetId ?? '',
    price: String(asset.price),
    downloadAllowed: asset.downloadAllowed,
    purchaseAllowed: asset.purchaseAllowed,
    published: asset.published,
  }
}

function buildRequestPayload(
  form: DigitalAssetFormState,
): UpsertDigitalAssetRequest | null {
  if (form.title.trim() === '' || form.fileAssetId.trim() === '') {
    return null
  }

  const price = Number(form.price)

  if (Number.isNaN(price) || price < 0) {
    return null
  }

  return {
    format: form.format,
    title: form.title,
    fileAssetId: form.fileAssetId,
    sampleFileAssetId: toNullableString(form.sampleFileAssetId),
    price,
    downloadAllowed: form.downloadAllowed,
    purchaseAllowed: form.purchaseAllowed,
    published: form.published,
  }
}

function toNullableString(value: string) {
  const trimmedValue = value.trim()
  return trimmedValue === '' ? null : trimmedValue
}

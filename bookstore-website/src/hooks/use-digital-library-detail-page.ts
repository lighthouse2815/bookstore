import axios from 'axios'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import {
  getPublishedDigitalAssetSampleUrl,
  getMyDigitalAssetDownloadUrl,
  getMyDigitalLibraryAsset,
  updateMyReadingProgress,
} from '@/services/digital-library-service'
import type { DigitalLibraryAssetResponse } from '@/types/digital-library'
import { getErrorMessage } from '@/utils'

type ProgressFormState = {
  currentPage: string
  progressPercent: string
  positionData: string
}

const initialFormState: ProgressFormState = {
  currentPage: '',
  progressPercent: '0',
  positionData: '',
}

export function useDigitalLibraryDetailPage(digitalAssetId?: string) {
  const { t } = useLanguage()
  const [asset, setAsset] = useState<DigitalLibraryAssetResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [isSavingProgress, setIsSavingProgress] = useState(false)
  const [isResolvingDownloadUrl, setIsResolvingDownloadUrl] = useState(false)
  const [isResolvingSampleUrl, setIsResolvingSampleUrl] = useState(false)
  const [progressForm, setProgressForm] = useState<ProgressFormState>(
    initialFormState,
  )

  useEffect(() => {
    if (!digitalAssetId) {
      setAsset(null)
      setIsLoading(false)
      setError(null)
      setNotFound(true)
      return
    }

    let isCancelled = false

    async function loadAsset() {
      setIsLoading(true)

      try {
        const response = await getMyDigitalLibraryAsset(digitalAssetId)

        if (isCancelled) {
          return
        }

        setAsset(response)
        setProgressForm(createProgressFormState(response))
        setNotFound(false)
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        const assetNotFound =
          axios.isAxiosError(currentError) && currentError.response?.status === 404

        setAsset(null)
        setNotFound(assetNotFound)
        setError(assetNotFound ? null : getErrorMessage(currentError))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadAsset()

    return () => {
      isCancelled = true
    }
  }, [digitalAssetId])

  function handleProgressFieldChange(
    field: keyof ProgressFormState,
    value: string,
  ) {
    setProgressForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  async function submitProgress() {
    if (!asset) {
      return
    }

    const progressPercent = Number(progressForm.progressPercent)
    const currentPage =
      progressForm.currentPage.trim() === ''
        ? null
        : Number(progressForm.currentPage)

    if (
      Number.isNaN(progressPercent) ||
      progressPercent < 0 ||
      progressPercent > 100
    ) {
      toast.error(t('library.progress.validation.progressPercentRange'))
      return
    }

    if (
      currentPage !== null &&
      (Number.isNaN(currentPage) || currentPage < 0 || !Number.isInteger(currentPage))
    ) {
      toast.error(t('library.progress.validation.currentPageNonNegativeInteger'))
      return
    }

    setIsSavingProgress(true)

    try {
      const progress = await updateMyReadingProgress(asset.digitalAssetId, {
        currentPage,
        progressPercent,
        positionData: progressForm.positionData,
      })

      setAsset((currentAsset) =>
        currentAsset
          ? {
              ...currentAsset,
              progress,
            }
          : currentAsset,
      )
      setProgressForm(createProgressFormStateFromProgress(progress))
      toast.success(t('library.progress.updateSuccess'))
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsSavingProgress(false)
    }
  }

  async function openSampleAsset() {
    await openSignedAssetUrl({
      asset,
      requestUrl: () =>
        getPublishedDigitalAssetSampleUrl(asset.bookId, asset.digitalAssetId),
      setLoading: setIsResolvingSampleUrl,
    })
  }

  async function downloadAsset() {
    await openSignedAssetUrl({
      asset,
      requestUrl: () => getMyDigitalAssetDownloadUrl(asset.digitalAssetId),
      setLoading: setIsResolvingDownloadUrl,
    })
  }

  return {
    asset,
    isLoading,
    error,
    notFound,
    isSavingProgress,
    isResolvingDownloadUrl,
    isResolvingSampleUrl,
    progressForm,
    handleProgressFieldChange,
    submitProgress,
    openSampleAsset,
    downloadAsset,
  }
}

function createProgressFormState(asset: DigitalLibraryAssetResponse): ProgressFormState {
  return createProgressFormStateFromProgress(asset.progress)
}

function createProgressFormStateFromProgress(
  progress: DigitalLibraryAssetResponse['progress'],
): ProgressFormState {
  return {
    currentPage:
      typeof progress?.currentPage === 'number' ? String(progress.currentPage) : '',
    progressPercent:
      typeof progress?.progressPercent === 'number'
        ? String(progress.progressPercent)
        : '0',
    positionData: progress?.positionData ?? '',
  }
}

async function openSignedAssetUrl({
  asset,
  requestUrl,
  setLoading,
}: {
  asset: DigitalLibraryAssetResponse | null
  requestUrl: () => Promise<{ url: string }>
  setLoading: (value: boolean) => void
}) {
  if (!asset || typeof window === 'undefined') {
    return
  }

  const pendingWindow = window.open('', '_blank')
  setLoading(true)

  try {
    const signedUrl = await requestUrl()

    if (pendingWindow) {
      pendingWindow.location.href = signedUrl.url
    } else {
      window.location.assign(signedUrl.url)
    }
  } catch (currentError) {
    pendingWindow?.close()
    toast.error(getErrorMessage(currentError))
  } finally {
    setLoading(false)
  }
}

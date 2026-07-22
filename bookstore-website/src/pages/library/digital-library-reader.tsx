import axios from 'axios'
import { useEffect, useState, type ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, ExternalLink, Headphones } from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import {
  getMyDigitalAssetReadUrl,
  getMyDigitalLibraryAsset,
} from '@/services/digital-library-service'
import type { DigitalLibraryAssetResponse } from '@/types/digital-library'
import { cn, getErrorMessage } from '@/utils'
import {
  getDigitalAccessTypeLabel,
  getDigitalAssetFormatLabel,
} from '@/utils/i18n'

type ReaderState = {
  asset: DigitalLibraryAssetResponse | null
  readUrl: string | null
  isLoading: boolean
  error: string | null
  notFound: boolean
}

const initialState: ReaderState = {
  asset: null,
  readUrl: null,
  isLoading: true,
  error: null,
  notFound: false,
}

export default function DigitalLibraryReaderPage() {
  const { digitalAssetId } = useParams<{ digitalAssetId: string }>()
  const { formatDate, t } = useLanguage()
  const [state, setState] = useState<ReaderState>(initialState)

  useEffect(() => {
    if (!digitalAssetId) {
      setState({
        ...initialState,
        isLoading: false,
        notFound: true,
      })
      return
    }
    const resolvedDigitalAssetId = digitalAssetId

    let isCancelled = false

    async function loadReader() {
      setState((currentState) => ({
        ...currentState,
        isLoading: true,
        error: null,
        notFound: false,
      }))

      try {
        const [asset, signedUrl] = await Promise.all([
          getMyDigitalLibraryAsset(resolvedDigitalAssetId),
          getMyDigitalAssetReadUrl(resolvedDigitalAssetId),
        ])

        if (isCancelled) {
          return
        }

        setState({
          asset,
          readUrl: signedUrl.url,
          isLoading: false,
          error: null,
          notFound: false,
        })
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        const notFound =
          axios.isAxiosError(currentError) && currentError.response?.status === 404

        setState({
          asset: null,
          readUrl: null,
          isLoading: false,
          error: notFound ? null : getErrorMessage(currentError),
          notFound,
        })
      }
    }

    void loadReader()

    return () => {
      isCancelled = true
    }
  }, [digitalAssetId])

  if (state.isLoading) {
    return (
      <PageShell>
        <StatePanel>{t('library.reader.loading')}</StatePanel>
      </PageShell>
    )
  }

  if (state.notFound || !state.asset || !state.readUrl) {
    return (
      <PageShell>
        <StatePanel tone="error">{t('library.reader.notFound')}</StatePanel>
      </PageShell>
    )
  }

  if (state.error) {
    return (
      <PageShell>
        <StatePanel tone="error">{state.error}</StatePanel>
      </PageShell>
    )
  }

  const isAudio = state.asset.format === 'AUDIO'

  return (
    <PageShell>
      <div className="mx-auto flex w-full max-w-[1380px] flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <Link
            to={`/library/${state.asset.digitalAssetId}`}
            className="inline-flex items-center gap-2 text-sm font-semibold text-primary"
          >
            <ArrowLeft className="h-4 w-4" />
            {t('library.reader.backLabel')}
          </Link>

          <a href={state.readUrl} target="_blank" rel="noreferrer">
            <Button type="button" variant="outline" className="rounded-2xl">
              <ExternalLink className="mr-2 h-4 w-4" />
              {t('library.reader.openNewTab')}
            </Button>
          </a>
        </div>

        <section className="rounded-[28px] border border-primary/10 bg-card/92 p-6 text-card-foreground shadow-[0_18px_50px_rgba(109,76,255,0.08)] dark:border-white/10 dark:bg-card/88 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                {t('library.reader.bookLabel')}
              </p>
              <h1 className="mt-2 font-heading text-3xl font-bold text-foreground">
                {state.asset.bookTitle}
              </h1>
              <p className="mt-2 text-lg font-medium text-muted-foreground">
                {state.asset.assetTitle}
              </p>
            </div>

            <div className="flex flex-wrap gap-2">
              <Badge variant="outline" className="rounded-full px-3 py-1 text-xs">
                {getDigitalAssetFormatLabel(state.asset.format, t)}
              </Badge>
              <Badge variant="outline" className="rounded-full px-3 py-1 text-xs">
                {getDigitalAccessTypeLabel(state.asset.accessType, t)}
              </Badge>
            </div>
          </div>

          <div className="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <MetaCard label={t('library.reader.acquiredAtLabel')} value={formatDate(state.asset.acquiredAt)} />
            <MetaCard
              label={t('library.reader.updatedAtLabel')}
              value={formatDate(state.asset.assetUpdatedAt)}
            />
            <MetaCard
              label={t('library.reader.expiresAtLabel')}
              value={
                state.asset.expiresAt
                  ? formatDate(state.asset.expiresAt)
                  : t('library.reader.noExpiry')
              }
            />
            <MetaCard label={t('library.reader.fileNameLabel')} value={state.asset.fileName} />
          </div>
        </section>

        <section className="overflow-hidden rounded-[30px] border border-primary/10 bg-card/92 shadow-[0_18px_50px_rgba(109,76,255,0.08)] dark:border-white/10 dark:bg-card/88 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]">
          {isAudio ? (
            <div className="p-6">
              <div className="mb-4 flex items-center gap-3">
                <span className="flex size-12 items-center justify-center rounded-full bg-primary/10 text-primary">
                  <Headphones className="h-5 w-5" />
                </span>
                <div>
                  <h2 className="font-heading text-2xl font-bold text-foreground">
                    {t('library.reader.audioTitle')}
                  </h2>
                  <p className="mt-1 text-sm text-muted-foreground">{t('library.reader.audioDescription')}</p>
                </div>
              </div>

              <audio controls className="w-full" src={state.readUrl}>
                {t('library.reader.audioFallback')}
              </audio>
            </div>
          ) : (
            <iframe
              title={state.asset.assetTitle}
              src={state.readUrl}
              className="min-h-[75vh] w-full bg-background"
            />
          )}
        </section>
      </div>
    </PageShell>
  )
}

function PageShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(249,247,255,1)_0%,rgba(244,239,255,0.94)_46%,rgba(255,255,255,1)_100%)] dark:bg-[linear-gradient(180deg,rgba(24,20,38,1)_0%,rgba(18,16,29,0.98)_46%,rgba(13,12,21,1)_100%)]">
      <Header />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  )
}

function StatePanel({
  children,
  tone = 'default',
}: {
  children: ReactNode
  tone?: 'default' | 'error'
}) {
  return (
    <div className="mx-auto w-full max-w-[1380px] px-4 py-6 sm:px-6 lg:px-8">
      <section
        className={cn(
          'rounded-[30px] border p-8 text-center shadow-[0_18px_50px_rgba(109,76,255,0.08)]',
          tone === 'default'
            ? 'border-primary/10 bg-card/90 text-muted-foreground dark:border-white/10 dark:bg-card/88'
            : 'border-destructive/20 bg-destructive/5 text-destructive',
        )}
      >
        {children}
      </section>
    </div>
  )
}

function MetaCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[20px] border border-primary/8 bg-primary/4 px-4 py-3">
      <dt className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
        {label}
      </dt>
      <dd className="mt-2 break-words text-sm font-semibold text-foreground">
        {value}
      </dd>
    </div>
  )
}

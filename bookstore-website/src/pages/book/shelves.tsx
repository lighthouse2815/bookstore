import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import {
  BookPlus,
  LibraryBig,
  PencilLine,
  Plus,
  Sparkles,
  Trash2,
} from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import {
  StatePanel,
  destructiveOutlineButtonClassName,
  primaryButtonClassName,
  secondaryButtonClassName,
} from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useBookshelves } from '@/hooks/use-bookshelves'
import { addBookToShelf } from '@/services/bookshelf-service'
import type { BookshelfSummary } from '@/types/bookshelf'
import { getErrorMessage } from '@/utils'

export default function ShelvesPage() {
  const { t, formatDate, formatNumber } = useLanguage()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const addBookId = searchParams.get('addBook')
  const {
    shelves,
    isLoading,
    isSaving,
    error,
    refresh,
    createShelf,
    renameShelf,
    removeShelf,
  } = useBookshelves()
  const [draftName, setDraftName] = useState('')
  const [editingShelfId, setEditingShelfId] = useState<string | null>(null)
  const [editingName, setEditingName] = useState('')
  const [busyShelfId, setBusyShelfId] = useState<string | null>(null)

  async function handleCreateShelf(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const nextName = draftName.trim()
    if (!nextName) {
      return
    }

    try {
      const createdShelf = await createShelf(nextName)
      setDraftName('')

      if (addBookId) {
        setBusyShelfId(createdShelf.id)
        const updatedShelf = await addBookToShelf(createdShelf.id, addBookId)
        toast.success(
          t('shelves.addedToShelfSuccess', { name: updatedShelf.name }),
        )
        navigate(`/shelves/${updatedShelf.id}`)
        return
      }

      toast.success(t('shelves.createSuccess', { name: createdShelf.name }))
      navigate(`/shelves/${createdShelf.id}`)
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('shelves.createError')))
    } finally {
      setBusyShelfId(null)
    }
  }

  async function handleRenameShelf(shelfId: string) {
    const nextName = editingName.trim()
    if (!nextName) {
      return
    }

    try {
      const updatedShelf = await renameShelf(shelfId, nextName)
      toast.success(t('shelves.renameSuccess', { name: updatedShelf.name }))
      setEditingShelfId(null)
      setEditingName('')
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('shelves.renameError')))
    }
  }

  async function handleDeleteShelf(shelf: BookshelfSummary) {
    if (!window.confirm(t('shelves.deleteConfirm', { name: shelf.name }))) {
      return
    }

    try {
      await removeShelf(shelf.id)
      toast.success(t('shelves.deleteSuccess', { name: shelf.name }))
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('shelves.deleteError')))
    }
  }

  async function handleOpenShelf(shelf: BookshelfSummary) {
    if (!addBookId) {
      navigate(`/shelves/${shelf.id}`)
      return
    }

    try {
      setBusyShelfId(shelf.id)
      const updatedShelf = await addBookToShelf(shelf.id, addBookId)
      toast.success(
        t('shelves.addedToShelfSuccess', { name: updatedShelf.name }),
      )
      navigate(`/shelves/${updatedShelf.id}`)
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('shelves.addToShelfError')))
    } finally {
      setBusyShelfId(null)
    }
  }

  function startEditingShelf(shelf: BookshelfSummary) {
    setEditingShelfId(shelf.id)
    setEditingName(shelf.name)
  }

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(249,245,255,1)_0%,rgba(255,255,255,1)_46%,rgba(246,249,255,1)_100%)] dark:bg-[linear-gradient(180deg,rgba(18,15,29,1)_0%,rgba(23,20,34,1)_46%,rgba(18,15,29,1)_100%)]">
      <Header />
      <main className="flex-1 pb-16 pt-8">
        <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <section className="overflow-hidden rounded-[32px] border border-primary/12 bg-white/88 p-6 shadow-[0_24px_80px_rgba(137,92,255,0.12)] backdrop-blur dark:border-white/10 dark:bg-card/90 dark:shadow-[0_24px_80px_rgba(0,0,0,0.3)]">
            <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
              <div className="space-y-4">
                <span className="inline-flex items-center gap-2 rounded-full bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:bg-primary/18">
                  <LibraryBig className="size-4" />
                  {t('shelves.badge')}
                </span>
                <div>
                  <h1 className="font-heading text-3xl font-bold tracking-tight text-slate-950 dark:text-foreground">
                    {t('shelves.title')}
                  </h1>
                  <p className="mt-2 max-w-3xl text-sm text-slate-600 dark:text-muted-foreground sm:text-base">
                    {t('shelves.description')}
                  </p>
                </div>
              </div>

              <div className="rounded-3xl border border-primary/12 bg-primary/6 px-5 py-4 text-sm text-primary dark:border-primary/20 dark:bg-primary/10">
                <p className="font-semibold">
                  {t('shelves.countLabel', { count: formatNumber(shelves.length) })}
                </p>
                <p className="mt-1 text-primary/80 dark:text-primary/75">
                  {t('shelves.hint')}
                </p>
              </div>
            </div>
          </section>

          {addBookId ? (
            <section className="rounded-[28px] border border-emerald-200/70 bg-emerald-50/90 p-5 text-emerald-900 shadow-sm dark:border-emerald-400/20 dark:bg-emerald-400/10 dark:text-emerald-100">
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div className="space-y-1">
                  <p className="inline-flex items-center gap-2 text-sm font-semibold">
                    <Sparkles className="size-4" />
                    {t('shelves.addBookBannerTitle')}
                  </p>
                  <p className="text-sm text-emerald-800/90 dark:text-emerald-100/80">
                    {t('shelves.addBookBannerDescription')}
                  </p>
                </div>
                <Link
                  to="/books"
                  className="text-sm font-semibold text-emerald-700 underline-offset-4 hover:underline dark:text-emerald-200"
                >
                  {t('shelves.browseMoreBooks')}
                </Link>
              </div>
            </section>
          ) : null}

          <section className="rounded-[28px] border border-primary/10 bg-white/88 p-5 shadow-[0_16px_50px_rgba(137,92,255,0.08)] backdrop-blur dark:border-white/10 dark:bg-card/90 dark:shadow-[0_16px_50px_rgba(0,0,0,0.25)]">
            <form
              className="flex flex-col gap-3 md:flex-row"
              onSubmit={(event) => {
                void handleCreateShelf(event)
              }}
            >
              <Input
                value={draftName}
                onChange={(event) => setDraftName(event.currentTarget.value)}
                placeholder={t('shelves.createPlaceholder')}
                disabled={isSaving}
                className="h-12 rounded-2xl"
              />
              <Button
                type="submit"
                disabled={isSaving || !draftName.trim()}
                className={`${primaryButtonClassName} h-12 px-5`}
              >
                <Plus className="mr-2 size-4" />
                {addBookId
                  ? t('shelves.createAndSave')
                  : t('shelves.createAction')}
              </Button>
            </form>

            {error && shelves.length > 0 ? (
              <p className="mt-3 text-sm text-rose-500">{error}</p>
            ) : null}
          </section>

          {isLoading ? (
            <StatePanel
              title={t('common.loading')}
              description={t('shelves.hint')}
              minHeightClassName="min-h-[260px]"
            />
          ) : error && shelves.length === 0 ? (
            <StatePanel
              title={t('shelves.emptyTitle')}
              description={error}
              tone="error"
              minHeightClassName="min-h-[260px]"
              action={
                <Button
                  type="button"
                  variant="outline"
                  className={secondaryButtonClassName}
                  onClick={() => {
                    void refresh()
                  }}
                >
                  {t('common.retry')}
                </Button>
              }
            />
          ) : shelves.length === 0 ? (
            <StatePanel
              icon={<LibraryBig className="mx-auto size-12 text-primary/70" />}
              title={t('shelves.emptyTitle')}
              description={
                addBookId
                  ? t('shelves.emptyWithBookDescription')
                  : t('shelves.emptyDescription')
              }
              minHeightClassName="min-h-[260px]"
            />
          ) : (
            <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {shelves.map((shelf) => {
                const isEditing = editingShelfId === shelf.id
                const isBusy = isSaving || busyShelfId === shelf.id

                return (
                  <article
                    key={shelf.id}
                    className="motion-card rounded-[28px] border border-primary/10 bg-white/92 p-5 shadow-[0_16px_48px_rgba(137,92,255,0.08)] dark:border-white/10 dark:bg-card/92 dark:shadow-[0_16px_48px_rgba(0,0,0,0.24)]"
                  >
                    {isEditing ? (
                      <div className="space-y-3">
                        <Input
                          value={editingName}
                          onChange={(event) =>
                            setEditingName(event.currentTarget.value)
                          }
                          disabled={isSaving}
                          className="h-11 rounded-2xl"
                        />
                        <div className="flex gap-2">
                          <Button
                            type="button"
                            size="sm"
                            disabled={isSaving || !editingName.trim()}
                            className={primaryButtonClassName}
                            onClick={() => {
                              void handleRenameShelf(shelf.id)
                            }}
                          >
                            {t('common.save')}
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            disabled={isSaving}
                            className={secondaryButtonClassName}
                            onClick={() => {
                              setEditingShelfId(null)
                              setEditingName('')
                            }}
                          >
                            {t('common.cancel')}
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <>
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <h2 className="font-heading text-xl font-bold text-slate-950 dark:text-foreground">
                              {shelf.name}
                            </h2>
                            <p className="mt-2 text-sm text-muted-foreground">
                              {t('shelves.cardMeta', {
                                count: formatNumber(shelf.bookCount),
                                date: formatDate(shelf.updatedAt),
                              })}
                            </p>
                          </div>
                          <span className="rounded-full bg-primary/12 px-3 py-1 text-xs font-semibold text-primary dark:bg-primary/18">
                            {t('shelves.booksCountBadge', {
                              count: formatNumber(shelf.bookCount),
                            })}
                          </span>
                        </div>

                        <div className="mt-5 flex flex-wrap gap-2">
                          <Button
                            type="button"
                            disabled={isBusy}
                            className={primaryButtonClassName}
                            onClick={() => {
                              void handleOpenShelf(shelf)
                            }}
                          >
                            {addBookId ? (
                              <>
                                <BookPlus className="mr-2 size-4" />
                                {t('shelves.saveSelectedBook')}
                              </>
                            ) : (
                              t('shelves.openAction')
                            )}
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            className={secondaryButtonClassName}
                            disabled={isBusy}
                            onClick={() => startEditingShelf(shelf)}
                          >
                            <PencilLine className="mr-2 size-4" />
                            {t('shelves.renameAction')}
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            className={destructiveOutlineButtonClassName}
                            disabled={isBusy}
                            onClick={() => {
                              void handleDeleteShelf(shelf)
                            }}
                          >
                            <Trash2 className="mr-2 size-4" />
                            {t('common.delete')}
                          </Button>
                        </div>
                      </>
                    )}
                  </article>
                )
              })}
            </section>
          )}
        </div>
      </main>
      <Footer />
    </div>
  )
}

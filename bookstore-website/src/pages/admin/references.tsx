import { useEffect, useState } from 'react'
import { Edit2, Plus, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { AdminLayout } from '@/components/layout/admin-layout'
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

type ReferenceSectionKey = 'categories' | 'authors' | 'publishers'

type ReferenceFormState = {
  id: string | null
  name: string
  description: string
}

const initialReferences: BookReferenceData = {
  categories: [],
  authors: [],
  publishers: [],
}

const initialFormState: ReferenceFormState = {
  id: null,
  name: '',
  description: '',
}

export default function AdminReferencesPage() {
  const { t, formatNumber, formatDate } = useLanguage()
  const [references, setReferences] = useState<BookReferenceData>(initialReferences)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeSection, setActiveSection] = useState<ReferenceSectionKey | null>(
    null,
  )
  const [form, setForm] = useState<ReferenceFormState>(initialFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    void loadReferences()
  }, [])

  async function loadReferences() {
    setIsLoading(true)

    try {
      const response = await getBookReferences()
      setReferences(response)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsLoading(false)
    }
  }

  function handleCreate(section: ReferenceSectionKey) {
    setActiveSection(section)
    setForm(initialFormState)
  }

  function handleEdit(
    section: ReferenceSectionKey,
    item: CategoryResponse | AuthorResponse | PublisherResponse,
  ) {
    setActiveSection(section)
    setForm({
      id: item.id,
      name: item.name,
      description: getReferenceDescription(section, item),
    })
  }

  function handleCancel() {
    setActiveSection(null)
    setForm(initialFormState)
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!activeSection) {
      return
    }

    setIsSubmitting(true)

    try {
      switch (activeSection) {
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
      await loadReferences()
      handleCancel()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDelete(
    section: ReferenceSectionKey,
    item: CategoryResponse | AuthorResponse | PublisherResponse,
  ) {
    const confirmed = window.confirm(
      t('admin.references.confirmDelete', { name: item.name }),
    )

    if (!confirmed) {
      return
    }

    try {
      switch (section) {
        case 'categories':
          await deleteCategory(item.id)
          break
        case 'authors':
          await deleteAuthor(item.id)
          break
        case 'publishers':
          await deletePublisher(item.id)
          break
      }

      toast.success(t('admin.references.deleteSuccess'))
      await loadReferences()

      if (activeSection === section && form.id === item.id) {
        handleCancel()
      }
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    }
  }

  return (
    <AdminLayout>
      <div>
        <div>
          <h1 className="font-heading text-3xl font-bold text-foreground">
            {t('admin.references.title')}
          </h1>
          <p className="mt-2 text-muted-foreground">
            {t('admin.references.description')}
          </p>
        </div>

        {error && !isLoading && (
          <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            {error}
          </div>
        )}

        <div className="mt-8 grid gap-6 xl:grid-cols-3">
          <ReferenceSection
            sectionKey="categories"
            title={t('admin.references.sections.categories')}
            addLabel={t('admin.references.addCategory')}
            emptyLabel={t('admin.references.emptyCategories')}
            nameLabel={t('common.name')}
            descriptionLabel={t('common.description')}
            cancelLabel={t('common.cancel')}
            saveLabel={t('common.save')}
            loadingLabel={t('common.loading')}
            processingLabel={t('common.processing')}
            items={references.categories}
            formatDate={formatDate}
            formatNumber={formatNumber}
            isLoading={isLoading}
            activeSection={activeSection}
            form={form}
            isSubmitting={isSubmitting}
            onCreate={handleCreate}
            onEdit={handleEdit}
            onDelete={handleDelete}
            onCancel={handleCancel}
            onFormChange={setForm}
            onSubmit={handleSubmit}
          />

          <ReferenceSection
            sectionKey="authors"
            title={t('admin.references.sections.authors')}
            addLabel={t('admin.references.addAuthor')}
            emptyLabel={t('admin.references.emptyAuthors')}
            nameLabel={t('common.name')}
            descriptionLabel={t('admin.references.biography')}
            cancelLabel={t('common.cancel')}
            saveLabel={t('common.save')}
            loadingLabel={t('common.loading')}
            processingLabel={t('common.processing')}
            items={references.authors}
            formatDate={formatDate}
            formatNumber={formatNumber}
            isLoading={isLoading}
            activeSection={activeSection}
            form={form}
            isSubmitting={isSubmitting}
            onCreate={handleCreate}
            onEdit={handleEdit}
            onDelete={handleDelete}
            onCancel={handleCancel}
            onFormChange={setForm}
            onSubmit={handleSubmit}
          />

          <ReferenceSection
            sectionKey="publishers"
            title={t('admin.references.sections.publishers')}
            addLabel={t('admin.references.addPublisher')}
            emptyLabel={t('admin.references.emptyPublishers')}
            nameLabel={t('common.name')}
            descriptionLabel={t('common.description')}
            cancelLabel={t('common.cancel')}
            saveLabel={t('common.save')}
            loadingLabel={t('common.loading')}
            processingLabel={t('common.processing')}
            items={references.publishers}
            formatDate={formatDate}
            formatNumber={formatNumber}
            isLoading={isLoading}
            activeSection={activeSection}
            form={form}
            isSubmitting={isSubmitting}
            onCreate={handleCreate}
            onEdit={handleEdit}
            onDelete={handleDelete}
            onCancel={handleCancel}
            onFormChange={setForm}
            onSubmit={handleSubmit}
          />
        </div>
      </div>
    </AdminLayout>
  )
}

type ReferenceSectionProps = {
  sectionKey: ReferenceSectionKey
  title: string
  addLabel: string
  emptyLabel: string
  nameLabel: string
  descriptionLabel: string
  cancelLabel: string
  saveLabel: string
  loadingLabel: string
  processingLabel: string
  items: Array<CategoryResponse | AuthorResponse | PublisherResponse>
  formatDate: (value: string | number | Date) => string
  formatNumber: (value: number) => string
  isLoading: boolean
  activeSection: ReferenceSectionKey | null
  form: ReferenceFormState
  isSubmitting: boolean
  onCreate: (section: ReferenceSectionKey) => void
  onEdit: (
    section: ReferenceSectionKey,
    item: CategoryResponse | AuthorResponse | PublisherResponse,
  ) => void
  onDelete: (
    section: ReferenceSectionKey,
    item: CategoryResponse | AuthorResponse | PublisherResponse,
  ) => Promise<void>
  onCancel: () => void
  onFormChange: React.Dispatch<React.SetStateAction<ReferenceFormState>>
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => Promise<void>
}

function ReferenceSection({
  sectionKey,
  title,
  addLabel,
  emptyLabel,
  nameLabel,
  descriptionLabel,
  cancelLabel,
  saveLabel,
  loadingLabel,
  processingLabel,
  items,
  formatDate,
  formatNumber,
  isLoading,
  activeSection,
  form,
  isSubmitting,
  onCreate,
  onEdit,
  onDelete,
  onCancel,
  onFormChange,
  onSubmit,
}: ReferenceSectionProps) {
  const isActive = activeSection === sectionKey

  return (
    <section className="rounded-2xl border border-border bg-card p-5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="font-heading text-xl font-bold text-foreground">
            {title}
          </h2>
          <p className="mt-1 text-sm text-muted-foreground">
            {formatNumber(items.length)}
          </p>
        </div>
        <Button size="sm" onClick={() => onCreate(sectionKey)}>
          <Plus className="mr-2 h-4 w-4" />
          {addLabel}
        </Button>
      </div>

      {isActive && (
        <form onSubmit={onSubmit} className="mt-5 rounded-xl border border-border p-4">
          <div className="space-y-4">
            <div>
              <Label htmlFor={`${sectionKey}-name`}>{nameLabel}</Label>
              <Input
                id={`${sectionKey}-name`}
                value={form.name}
                onChange={(event) =>
                  onFormChange((currentForm) => ({
                    ...currentForm,
                    name: event.currentTarget.value,
                  }))
                }
                className="mt-2"
                required
              />
            </div>

            <div>
              <Label htmlFor={`${sectionKey}-description`}>
                {descriptionLabel}
              </Label>
              <Textarea
                id={`${sectionKey}-description`}
                value={form.description}
                onChange={(event) =>
                  onFormChange((currentForm) => ({
                    ...currentForm,
                    description: event.currentTarget.value,
                  }))
                }
                className="mt-2"
                rows={4}
              />
            </div>
          </div>

          <div className="mt-4 flex gap-3">
            <Button type="button" variant="outline" onClick={onCancel}>
              {cancelLabel}
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? processingLabel : saveLabel}
            </Button>
          </div>
        </form>
      )}

      <div className="mt-5 space-y-3">
        {isLoading ? (
          <p className="text-sm text-muted-foreground">{loadingLabel}</p>
        ) : items.length === 0 ? (
          <p className="text-sm text-muted-foreground">{emptyLabel}</p>
        ) : (
          items.map((item) => {
            const description = getReferenceDescription(sectionKey, item)

            return (
              <div
                key={item.id}
                className="rounded-xl border border-border bg-background p-4"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-semibold text-foreground">{item.name}</p>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {description}
                    </p>
                    <p className="mt-3 text-xs text-muted-foreground">
                      {formatDate(item.updatedAt)}
                    </p>
                  </div>

                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onEdit(sectionKey, item)}
                    >
                      <Edit2 className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => void onDelete(sectionKey, item)}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              </div>
            )
          })
        )}
      </div>
    </section>
  )
}

function getReferenceDescription(
  section: ReferenceSectionKey,
  item: CategoryResponse | AuthorResponse | PublisherResponse,
) {
  if (section === 'authors') {
    return ('biography' in item ? item.biography : null) ?? ''
  }

  return ('description' in item ? item.description : null) ?? ''
}

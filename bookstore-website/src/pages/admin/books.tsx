import { useMemo, useState } from 'react'
import { Edit2, Plus, Search, Trash2 } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import { useBookCatalog } from '@/hooks/use-book-catalog'
import { getCategoryLabel } from '@/utils/i18n'

export default function AdminBooksPage() {
  const [searchTerm, setSearchTerm] = useState('')
  const { t, formatCurrency, formatNumber } = useLanguage()
  const { books, isLoading, error } = useBookCatalog()

  const filteredBooks = useMemo(
    () =>
      books.filter(
        (book) =>
          book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
          book.author.toLowerCase().includes(searchTerm.toLowerCase()),
      ),
    [books, searchTerm],
  )

  return (
    <AdminLayout>
      <div>
        <div className="flex items-center justify-between">
          <div>
            <h1 className="font-heading text-3xl font-bold text-foreground">
              {t('admin.books.title')}
            </h1>
            <p className="mt-2 text-muted-foreground">
              {t('admin.books.totalBooks', {
                count: formatNumber(books.length),
              })}
            </p>
          </div>
          <Button size="lg">
            <Plus className="mr-2 h-4 w-4" />
            {t('admin.books.addBook')}
          </Button>
        </div>

        <div className="mt-8">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder={t('admin.books.searchPlaceholder')}
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        <div className="mt-8 rounded-lg border border-border bg-card">
          {isLoading ? (
            <div className="px-6 py-8 text-center">
              <p className="text-muted-foreground">{t('common.loading')}</p>
            </div>
          ) : error ? (
            <div className="px-6 py-8 text-center">
              <p className="font-semibold text-foreground">
                {t('book.listing.errorTitle')}
              </p>
              <p className="mt-2 text-sm text-muted-foreground">{error}</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.book')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.author')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.category')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.price')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.stock')}
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-foreground">
                      {t('admin.books.columns.actions')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredBooks.map((book) => (
                    <tr key={book.id} className="border-b border-border">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <img
                            src={book.cover || '/placeholder.svg'}
                            alt={book.title}
                            className="h-10 w-8 rounded object-cover"
                          />
                          <div>
                            <p className="text-sm font-medium text-foreground">
                              {book.title}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {book.author || t('book.fallback.author')}
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {getCategoryLabel(book.category, t)}
                      </td>
                      <td className="px-6 py-4 text-sm font-medium text-foreground">
                        {formatCurrency(book.price)}
                      </td>
                      <td className="px-6 py-4 text-sm text-foreground">
                        {formatNumber(book.stockQuantity)}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <div className="flex gap-2">
                          <Button variant="ghost" size="sm">
                            <Edit2 className="h-4 w-4" />
                          </Button>
                          <Button variant="ghost" size="sm">
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {!isLoading && !error && filteredBooks.length === 0 && (
            <div className="px-6 py-8 text-center">
              <p className="text-muted-foreground">{t('admin.books.empty')}</p>
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  )
}

import { BookCard } from '@/components/book/book-card'
import { useLanguage } from '@/contexts/language-context'
import { usePersonalizedRecommendations } from '@/hooks/use-personalized-recommendations'
import { getDisplayReasonCodes, getRecommendationReasonKey } from '@/utils/recommendation'

type PersonalizedRecommendationsProps = {
  enabled: boolean
}

export function PersonalizedRecommendations({ enabled }: PersonalizedRecommendationsProps) {
  const { t } = useLanguage()
  const { data, isLoading } = usePersonalizedRecommendations(enabled)

  if (!enabled || (!isLoading && (!data || data.items.length === 0))) {
    return null
  }

  return (
    <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <div className="mb-6 max-w-2xl">
        <h2 className="font-heading text-2xl font-bold tracking-tight">
          {t('recommendations.title')}
        </h2>
        <p className="mt-1 text-sm text-muted-foreground">
          {t('recommendations.subtitle')}
        </p>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-4">
          {Array.from({ length: 6 }, (_, index) => (
            <div
              key={index}
              className="aspect-[3/5] animate-pulse rounded-2xl border border-border bg-muted/60 motion-reduce:animate-none"
              aria-label={t('recommendations.loading')}
            />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-4">
          {data?.items.map((item) => (
            <div key={item.book.id} className="min-w-0">
              <BookCard book={item.book} />
              {item.reasonCodes.length > 0 && (
                <div className="mt-2 flex flex-wrap gap-1.5 px-1">
                  {getDisplayReasonCodes(item.reasonCodes).map((reasonCode) => (
                    <span
                      key={reasonCode}
                      className="max-w-full rounded-full bg-primary/10 px-2 py-1 text-xs font-medium text-primary"
                    >
                      {t(getRecommendationReasonKey(reasonCode))}
                    </span>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

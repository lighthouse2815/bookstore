import { Link } from 'react-router-dom'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'

export default function NotFoundPage() {
  const { t } = useLanguage()

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="container mx-auto flex flex-1 items-center justify-center px-4 py-12">
        <div className="text-center">
          <h1 className="mb-2 font-heading text-6xl font-bold text-primary">
            404
          </h1>
          <p className="mb-4 font-heading text-2xl font-bold">
            {t('notFound.title')}
          </p>
          <p className="mb-8 text-muted-foreground">
            {t('notFound.description')}
          </p>
          <Link to="/">
            <Button>{t('common.backHome')}</Button>
          </Link>
        </div>
      </main>
      <Footer />
    </div>
  )
}

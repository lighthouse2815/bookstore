import { Link } from 'react-router-dom'
import { CheckCircle } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'

export default function OrderConfirmationPage() {
  const { t } = useLanguage()

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="container mx-auto flex-1 px-4 py-12">
        <div className="mx-auto max-w-md text-center">
          <div className="mb-6 flex justify-center">
            <CheckCircle className="size-20 text-green-600" />
          </div>
          <h1 className="mb-2 font-heading text-3xl font-bold">
            {t('orderConfirmation.title')}
          </h1>
          <p className="mb-6 text-muted-foreground">
            {t('orderConfirmation.description')}
          </p>
          <p className="mb-8 text-sm text-muted-foreground">
            {t('orderConfirmation.emailNotice')}
          </p>
          <div className="space-y-3">
            <Link to="/books" className="block">
              <Button className="w-full">
                {t('common.continueShopping')}
              </Button>
            </Link>
            <Link to="/" className="block">
              <Button variant="outline" className="w-full">
                {t('common.backHome')}
              </Button>
            </Link>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

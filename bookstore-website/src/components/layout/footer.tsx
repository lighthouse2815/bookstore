import { Link } from 'react-router-dom'
import { BookOpen, Globe, Mail, MessageCircle } from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'

export function Footer() {
  const { t } = useLanguage()
  const brand = t('common.brand')
  const brandPrefix = brand.endsWith('Vui') ? brand.slice(0, -3) : brand

  return (
    <footer className="border-t border-border bg-muted/40">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-4">
          <div className="space-y-3">
            <Link to="/" className="flex items-center gap-2">
              <span className="flex size-9 items-center justify-center rounded-xl bg-primary text-primary-foreground">
                <BookOpen className="size-5" />
              </span>
              <span className="font-heading text-xl font-bold tracking-tight">
                {brandPrefix}
                {brand.endsWith('Vui') && (
                  <span className="text-primary">Vui</span>
                )}
              </span>
            </Link>
            <p className="text-sm leading-relaxed text-muted-foreground">
              {t('footer.description')}
            </p>
            <div className="flex gap-2">
              {[Globe, MessageCircle, Mail].map((Icon, index) => (
                <a
                  key={index}
                  href="#"
                  className="flex size-9 items-center justify-center rounded-full bg-background text-foreground transition-colors hover:bg-primary hover:text-primary-foreground"
                  aria-label={t('footer.socialAria')}
                >
                  <Icon className="size-4" />
                </a>
              ))}
            </div>
          </div>

          <div>
            <h3 className="mb-3 font-heading text-sm font-semibold">
              {t('footer.explore')}
            </h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li>
                <Link to="/books" className="hover:text-primary">
                  {t('footer.links.allBooks')}
                </Link>
              </li>
              <li>
                <Link to="/books" className="hover:text-primary">
                  {t('footer.links.bestsellers')}
                </Link>
              </li>
              <li>
                <Link to="/books" className="hover:text-primary">
                  {t('footer.links.newBooks')}
                </Link>
              </li>
              <li>
                <Link to="/books" className="hover:text-primary">
                  {t('footer.links.promotions')}
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="mb-3 font-heading text-sm font-semibold">
              {t('footer.support')}
            </h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li>
                <Link to="/shipping-policy" className="hover:text-primary">
                  {t('footer.links.shippingPolicy')}
                </Link>
              </li>
              <li>
                <Link to="/returns-refunds" className="hover:text-primary">
                  {t('footer.links.returns')}
                </Link>
              </li>
              <li>
                <Link to="/faq" className="hover:text-primary">
                  {t('footer.links.faq')}
                </Link>
              </li>
              <li>
                <Link to="/contact" className="hover:text-primary">
                  {t('footer.links.contact')}
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="mb-3 font-heading text-sm font-semibold">
              {t('footer.newsletterTitle')}
            </h3>
            <p className="mb-3 text-sm text-muted-foreground">
              {t('footer.newsletterDescription')}
            </p>
            <form className="flex gap-2">
              <input
                type="email"
                required
                placeholder={t('footer.newsletterPlaceholder')}
                className="h-10 w-full rounded-full border border-border bg-background px-4 text-sm outline-none focus:border-primary"
              />
              <button
                type="submit"
                className="h-10 shrink-0 rounded-full bg-primary px-4 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
              >
                {t('common.send')}
              </button>
            </form>
          </div>
        </div>

        <div className="mt-10 flex flex-col items-center justify-between gap-2 border-t border-border pt-6 text-sm text-muted-foreground sm:flex-row">
          <p>{t('footer.copyright')}</p>
          <p>{t('footer.tagline')}</p>
        </div>
      </div>
    </footer>
  )
}

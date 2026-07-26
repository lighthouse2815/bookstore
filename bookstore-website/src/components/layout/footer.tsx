import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  BookOpen,
  Camera,
  LoaderCircle,
  MessageCircle,
  Users,
  type LucideIcon,
} from 'lucide-react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { useBrandWordmark } from '@/hooks/use-brand-wordmark'
import { subscribeNewsletter } from '@/services/newsletter-service'
import { getErrorMessage } from '@/utils'

export function Footer() {
  const { t } = useLanguage()
  const { brandPrefix, brandSuffix } = useBrandWordmark()
  const [newsletterEmail, setNewsletterEmail] = useState('')
  const [isNewsletterSubmitting, setIsNewsletterSubmitting] = useState(false)
  const socialLinks = (
    [
      {
        href: getPublicSocialUrl(import.meta.env.VITE_FACEBOOK_URL),
        label: t('footer.facebookAria'),
        icon: Users,
      },
      {
        href: getPublicSocialUrl(import.meta.env.VITE_INSTAGRAM_URL),
        label: t('footer.instagramAria'),
        icon: Camera,
      },
      {
        href: getPublicSocialUrl(import.meta.env.VITE_ZALO_URL),
        label: t('footer.zaloAria'),
        icon: MessageCircle,
      },
    ] satisfies Array<{
      href: string | null
      label: string
      icon: LucideIcon
    }>
  ).filter(
    (link): link is { href: string; label: string; icon: LucideIcon } =>
      Boolean(link.href),
  )

  async function handleNewsletterSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsNewsletterSubmitting(true)

    try {
      await subscribeNewsletter(newsletterEmail.trim())
      setNewsletterEmail('')
      toast.success(t('footer.newsletterSuccess'))
    } catch (error) {
      toast.error(getErrorMessage(error, t('footer.newsletterError')))
    } finally {
      setIsNewsletterSubmitting(false)
    }
  }

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
                {brandSuffix && <span className="text-primary">{brandSuffix}</span>}
              </span>
            </Link>
            <p className="text-sm leading-relaxed text-muted-foreground">
              {t('footer.description')}
            </p>
            {socialLinks.length > 0 ? (
              <div className="flex gap-2">
                {socialLinks.map(({ href, icon: Icon, label }) => (
                <a
                  key={href}
                  href={href}
                  target="_blank"
                  rel="noreferrer"
                  className="flex size-11 items-center justify-center rounded-full bg-background text-foreground transition-colors hover:bg-primary hover:text-primary-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50"
                  aria-label={label}
                >
                  <Icon className="size-4" />
                </a>
                ))}
              </div>
            ) : null}
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
            <form className="flex gap-2" onSubmit={handleNewsletterSubmit}>
              <input
                type="email"
                name="newsletterEmail"
                required
                autoComplete="email"
                value={newsletterEmail}
                disabled={isNewsletterSubmitting}
                onChange={(event) => setNewsletterEmail(event.target.value)}
                placeholder={t('footer.newsletterPlaceholder')}
                aria-label={t('footer.newsletterPlaceholder')}
                className="h-10 w-full rounded-full border border-border bg-background px-4 text-sm outline-none focus:border-primary"
              />
              <button
                type="submit"
                disabled={isNewsletterSubmitting}
                className="flex h-11 min-w-16 shrink-0 items-center justify-center rounded-full bg-primary px-4 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isNewsletterSubmitting ? (
                  <>
                    <LoaderCircle className="mr-2 size-4 animate-spin" />
                    <span className="sr-only">
                      {t('footer.newsletterSubmitting')}
                    </span>
                  </>
                ) : (
                  t('common.send')
                )}
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

function getPublicSocialUrl(value: string | undefined) {
  const candidate = value?.trim()

  if (!candidate) {
    return null
  }

  try {
    const url = new URL(candidate)
    return url.protocol === 'https:' || url.protocol === 'http:'
      ? url.toString()
      : null
  } catch {
    return null
  }
}

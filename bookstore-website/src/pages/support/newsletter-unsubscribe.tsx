import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { CheckCircle2, LoaderCircle, MailX } from 'lucide-react'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { unsubscribeNewsletter } from '@/services/newsletter-service'
import { getErrorMessage } from '@/utils'

type UnsubscribeState = 'idle' | 'submitting' | 'success' | 'error'

export default function NewsletterUnsubscribePage() {
  const { language } = useLanguage()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')?.trim() ?? ''
  const [state, setState] = useState<UnsubscribeState>('idle')
  const [errorMessage, setErrorMessage] = useState('')

  const content =
    language === 'en'
      ? {
          badge: 'Email preferences',
          title: 'Stop receiving the newsletter?',
          description:
            'Confirm below and we will stop sending weekly deals and book recommendations to this email address.',
          confirm: 'Unsubscribe',
          submitting: 'Unsubscribing',
          successTitle: 'You have unsubscribed',
          successDescription:
            'You will no longer receive the Sach Vui newsletter. You can subscribe again from the website footer at any time.',
          invalidToken: 'This unsubscribe link is invalid or incomplete.',
          error: 'Unable to unsubscribe. Please try again.',
          home: 'Return to homepage',
        }
      : {
          badge: 'Tùy chọn email',
          title: 'Bạn muốn ngừng nhận bản tin?',
          description:
            'Xác nhận bên dưới và chúng tôi sẽ ngừng gửi ưu đãi cùng gợi ý sách hằng tuần đến email này.',
          confirm: 'Hủy đăng ký',
          submitting: 'Đang hủy đăng ký',
          successTitle: 'Đã hủy đăng ký nhận tin',
          successDescription:
            'Bạn sẽ không còn nhận bản tin từ Sách Vui. Bạn luôn có thể đăng ký lại tại phần cuối trang web.',
          invalidToken: 'Liên kết hủy đăng ký không hợp lệ hoặc chưa đầy đủ.',
          error: 'Không thể hủy đăng ký. Vui lòng thử lại.',
          home: 'Về trang chủ',
        }

  async function handleUnsubscribe() {
    if (!token) {
      setErrorMessage(content.invalidToken)
      setState('error')
      return
    }

    setState('submitting')
    setErrorMessage('')

    try {
      await unsubscribeNewsletter(token)
      setState('success')
    } catch (error) {
      setErrorMessage(getErrorMessage(error, content.error))
      setState('error')
    }
  }

  const isSuccess = state === 'success'
  const isSubmitting = state === 'submitting'

  return (
    <div className="min-h-screen bg-background text-foreground">
      <Header />
      <main className="mx-auto flex min-h-[62vh] max-w-3xl items-center px-4 py-16 sm:px-6 lg:px-8">
        <section className="w-full border-y border-border py-12 text-center sm:py-16">
          <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-primary/10 text-primary">
            {isSuccess ? (
              <CheckCircle2 className="size-7" />
            ) : (
              <MailX className="size-7" />
            )}
          </div>
          <p className="mt-6 text-xs font-semibold uppercase tracking-[0.24em] text-primary">
            {content.badge}
          </p>
          <h1 className="mx-auto mt-3 max-w-2xl font-heading text-3xl font-bold tracking-tight sm:text-4xl">
            {isSuccess ? content.successTitle : content.title}
          </h1>
          <p className="mx-auto mt-4 max-w-xl text-sm leading-7 text-muted-foreground sm:text-base">
            {isSuccess ? content.successDescription : content.description}
          </p>

          {state === 'error' && (
            <p role="alert" className="mt-5 text-sm font-medium text-destructive">
              {errorMessage}
            </p>
          )}

          <div className="mt-8 flex justify-center">
            {isSuccess ? (
              <Link
                to="/"
                className="inline-flex h-11 items-center rounded-full bg-primary px-6 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
              >
                {content.home}
              </Link>
            ) : (
              <button
                type="button"
                onClick={() => void handleUnsubscribe()}
                disabled={isSubmitting || !token}
                className="inline-flex h-11 min-w-36 items-center justify-center rounded-full bg-primary px-6 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isSubmitting && (
                  <LoaderCircle className="mr-2 size-4 animate-spin" />
                )}
                {isSubmitting ? content.submitting : content.confirm}
              </button>
            )}
          </div>
        </section>
      </main>
      <Footer />
    </div>
  )
}

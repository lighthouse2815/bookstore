import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { BookOpen, Check } from 'lucide-react'
import { toast } from 'sonner'
import { AuthFlipCard } from '@/components/common/auth-flip-card'
import { RegisterTermsDialog } from '@/components/common/register-terms-dialog'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import type { RegisterRequest } from '@/types/auth'
import { getRegisterTermsCopy } from '@/utils/register-terms'

const initialRegisterFormData: RegisterRequest & { confirmPassword: string } = {
  email: '',
  password: '',
  confirmPassword: '',
}

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login, register } = useAuth()
  const { language, t } = useLanguage()
  const [isLoginLoading, setIsLoginLoading] = useState(false)
  const [isRegisterLoading, setIsRegisterLoading] = useState(false)
  const [isRegisterFace, setIsRegisterFace] = useState(false)
  const [hasAcceptedTerms, setHasAcceptedTerms] = useState(false)
  const [hasReadTermsDialog, setHasReadTermsDialog] = useState(false)
  const [isTermsOpen, setIsTermsOpen] = useState(false)
  const [shouldAcceptTermsOnClose, setShouldAcceptTermsOnClose] =
    useState(false)
  const [loginFormData, setLoginFormData] = useState({
    username: '',
    password: '',
  })
  const [registerFormData, setRegisterFormData] = useState<
    RegisterRequest & { confirmPassword: string }
  >(initialRegisterFormData)

  const brand = t('common.brand')
  const brandPrefix = brand.endsWith('Vui') ? brand.slice(0, -3) : brand
  const termsCopy = getRegisterTermsCopy(language)

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search)
    const username = searchParams.get('username')?.trim()

    if (!username) {
      return
    }

    setLoginFormData((currentFormData) => ({
      ...currentFormData,
      username,
      password: '',
    }))
    setIsRegisterFace(false)
  }, [location.search])

  function handleLoginChange(event: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target
    setLoginFormData((previousValue) => ({ ...previousValue, [name]: value }))
  }

  function handleRegisterChange(event: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target
    setRegisterFormData((previousValue) => ({
      ...previousValue,
      [name]: value,
    }))
  }

  async function handleLoginSubmit(event: React.FormEvent) {
    event.preventDefault()
    setIsLoginLoading(true)

    try {
      await login(loginFormData.username, loginFormData.password)
      toast.success(t('auth.login.success'))
      navigate('/')
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : t('auth.login.errorFallback'),
      )
    } finally {
      setIsLoginLoading(false)
    }
  }

  async function handleRegisterSubmit(event: React.FormEvent) {
    event.preventDefault()

    if (registerFormData.password !== registerFormData.confirmPassword) {
      toast.error(t('auth.register.passwordMismatch'))
      return
    }

    if (registerFormData.password.length < 8) {
      toast.error(t('auth.register.passwordTooShort'))
      return
    }

    if (!hasAcceptedTerms) {
      toast.error(termsCopy.requiredMessage)
      return
    }

    setIsRegisterLoading(true)

    try {
      const email = registerFormData.email.trim()

      await register({
        email,
        password: registerFormData.password,
      })
      toast.success(t('auth.register.success'))
      setLoginFormData({
        username: email,
        password: '',
      })
      setRegisterFormData(initialRegisterFormData)
      setHasAcceptedTerms(false)
      setIsRegisterFace(false)
      navigate(`/login?username=${encodeURIComponent(email)}`, { replace: true })
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : t('auth.register.errorFallback'),
      )
    } finally {
      setIsRegisterLoading(false)
    }
  }

  function openTermsDialog(acceptTermsOnClose: boolean) {
    setShouldAcceptTermsOnClose(acceptTermsOnClose)
    setIsTermsOpen(true)
  }

  function handleTermsCheckboxChange(
    event: React.ChangeEvent<HTMLInputElement>,
  ) {
    if (event.currentTarget.checked) {
      if (hasReadTermsDialog) {
        setHasAcceptedTerms(true)
        return
      }

      setHasAcceptedTerms(false)
      openTermsDialog(true)
      return
    }

    setShouldAcceptTermsOnClose(false)
    setHasAcceptedTerms(false)
  }

  function handleTermsLinkClick(event: React.MouseEvent<HTMLButtonElement>) {
    event.preventDefault()
    event.stopPropagation()
    openTermsDialog(false)
  }

  function handleTermsDialogClose() {
    setIsTermsOpen(false)
    setHasReadTermsDialog(true)
    setShouldAcceptTermsOnClose(false)

    if (shouldAcceptTermsOnClose) {
      setHasAcceptedTerms(true)
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="flex flex-1 items-center justify-center px-4 py-12">
        <div className="w-full max-w-lg">
          <div className="absolute inset-0 -z-10 bg-gradient-to-br from-primary/5 via-transparent to-transparent opacity-50" />

          <div className="mb-8 flex flex-col items-center">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-2 transition-all hover:bg-primary/15">
              <BookOpen className="h-7 w-7 text-primary" />
              <span className="font-heading text-xl font-bold text-primary">
                {brandPrefix}
                {brand.endsWith('Vui') && 'Vui'}
              </span>
            </div>
            <h2 className="text-center font-heading text-3xl font-bold text-foreground">
              {t('auth.login.title')}
            </h2>
            <p className="mt-2 text-center text-sm text-muted-foreground">
              {t('auth.login.description')}
            </p>
          </div>

          <AuthFlipCard
            checked={isRegisterFace}
            onCheckedChange={setIsRegisterFace}
            frontTitle={t('auth.login.cardTitle')}
            backTitle={t('auth.register.title')}
            frontContent={
              <form onSubmit={handleLoginSubmit} className="face_content">
                <input
                  name="username"
                  type="text"
                  placeholder={t('auth.login.username')}
                  className="input"
                  value={loginFormData.username}
                  onChange={handleLoginChange}
                  autoComplete="username"
                  required
                />
                <input
                  name="password"
                  type="password"
                  placeholder={t('auth.login.password')}
                  className="input"
                  value={loginFormData.password}
                  onChange={handleLoginChange}
                  autoComplete="current-password"
                  required
                />

                <button type="submit" className="btn" disabled={isLoginLoading}>
                  {isLoginLoading ? (
                    <span className="loading_inline">
                      <span className="loading_dot" />
                      <span>{t('common.processing')}</span>
                    </span>
                  ) : (
                    t('auth.login.submit')
                  )}
                </button>
              </form>
            }
            backContent={
              <form onSubmit={handleRegisterSubmit} className="face_content">
                <input
                  name="email"
                  placeholder={t('common.email')}
                  className="input"
                  type="email"
                  value={registerFormData.email}
                  onChange={handleRegisterChange}
                  autoComplete="email"
                  required
                />
                <input
                  name="password"
                  placeholder={t('auth.register.password')}
                  className="input"
                  type="password"
                  value={registerFormData.password}
                  onChange={handleRegisterChange}
                  autoComplete="new-password"
                  required
                />
                <input
                  name="confirmPassword"
                  placeholder={t('auth.register.confirmPassword')}
                  className="input"
                  type="password"
                  value={registerFormData.confirmPassword}
                  onChange={handleRegisterChange}
                  autoComplete="new-password"
                  required
                />
                <label className="flex items-start gap-3 rounded-2xl border border-white/12 bg-white/8 px-3 py-3 text-left text-sm leading-6 text-[color:var(--auth-text)]">
                  <input
                    type="checkbox"
                    checked={hasAcceptedTerms}
                    onChange={handleTermsCheckboxChange}
                    className="mt-1 h-4 w-4 rounded accent-primary"
                  />
                  <span>
                    {termsCopy.agreementLabel}{' '}
                    <button
                      type="button"
                      onClick={handleTermsLinkClick}
                      className="font-semibold text-[color:var(--auth-accent)] underline decoration-current/40 underline-offset-4"
                    >
                      {termsCopy.linkLabel}
                    </button>
                  </span>
                </label>
                <button
                  type="submit"
                  className="btn"
                  disabled={isRegisterLoading}
                >
                  {isRegisterLoading ? (
                    <span className="loading_inline">
                      <span className="loading_dot" />
                      <span>{t('common.processing')}</span>
                    </span>
                  ) : (
                    t('auth.register.submit')
                  )}
                </button>
              </form>
            }
            frontSwitchText={t('auth.login.noAccount')}
            frontSwitchAction={t('auth.login.registerNow')}
            backSwitchText={t('auth.register.haveAccount')}
            backSwitchAction={t('auth.login.cardTitle')}
          />

          <div className="mt-6 rounded-2xl border border-primary/20 bg-gradient-to-br from-primary/5 to-primary/10 p-4 backdrop-blur-sm">
            <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-primary">
              {t('auth.login.seedAccountTitle')}
            </p>
            <div className="space-y-2">
              <div className="flex items-start gap-2">
                <Check className="mt-0.5 h-4 w-4 flex-shrink-0 text-primary" />
                <p className="text-xs text-muted-foreground">
                  <strong>{t('auth.login.username')}:</strong>{' '}
                  <code className="rounded bg-muted px-1.5 py-0.5 font-mono">
                    giamdocdang
                  </code>
                </p>
              </div>
              <div className="flex items-start gap-2">
                <Check className="mt-0.5 h-4 w-4 flex-shrink-0 text-primary" />
                <p className="text-xs text-muted-foreground">
                  <strong>{t('auth.login.password')}:</strong>{' '}
                  <code className="rounded bg-muted px-1.5 py-0.5 font-mono">
                    123123aa
                  </code>
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
      <Footer />
      <RegisterTermsDialog
        open={isTermsOpen}
        onClose={handleTermsDialogClose}
      />
    </div>
  )
}

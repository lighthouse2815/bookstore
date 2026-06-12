import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  AlertCircle,
  ArrowRight,
  BookOpen,
  CheckCircle,
  Eye,
  EyeOff,
} from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { RegisterTermsDialog } from '@/components/common/register-terms-dialog'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import type { RegisterRequest } from '@/types/auth'
import { getRegisterTermsCopy } from '@/utils/register-terms'

type RegisterFormState = RegisterRequest & {
  confirmPassword: string
}

const initialFormData: RegisterFormState = {
  email: '',
  password: '',
  confirmPassword: '',
}

export default function RegisterPage() {
  const navigate = useNavigate()
  const { register } = useAuth()
  const { language, t } = useLanguage()
  const [isLoading, setIsLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [formData, setFormData] = useState(initialFormData)
  const [passwordStrength, setPasswordStrength] = useState(0)
  const [hasAcceptedTerms, setHasAcceptedTerms] = useState(false)
  const [hasReadTermsDialog, setHasReadTermsDialog] = useState(false)
  const [isTermsOpen, setIsTermsOpen] = useState(false)
  const [shouldAcceptTermsOnClose, setShouldAcceptTermsOnClose] =
    useState(false)

  const brand = t('common.brand')
  const brandPrefix = brand.endsWith('Vui') ? brand.slice(0, -3) : brand
  const termsCopy = getRegisterTermsCopy(language)

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target
    setFormData((previousValue) => ({ ...previousValue, [name]: value }))

    if (name === 'password') {
      let strength = 0
      if (value.length >= 8) strength++
      if (/[A-Z]/.test(value)) strength++
      if (/[0-9]/.test(value)) strength++
      if (/[^A-Za-z0-9]/.test(value)) strength++
      setPasswordStrength(strength)
    }
  }

  function getPasswordStrengthLabel() {
    if (passwordStrength <= 1) {
      return t('auth.register.passwordWeak')
    }
    if (passwordStrength === 2) {
      return t('auth.register.passwordMedium')
    }
    if (passwordStrength === 3) {
      return t('auth.register.passwordStrong')
    }
    return t('auth.register.passwordVeryStrong')
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()

    if (formData.password !== formData.confirmPassword) {
      toast.error(t('auth.register.passwordMismatch'))
      return
    }

    if (formData.password.length < 8) {
      toast.error(t('auth.register.passwordTooShort'))
      return
    }

    if (!hasAcceptedTerms) {
      toast.error(termsCopy.requiredMessage)
      return
    }

    setIsLoading(true)

    try {
      const email = formData.email.trim()

      await register({
        email,
        password: formData.password,
      })
      toast.success(t('auth.register.success'))
      setHasAcceptedTerms(false)
      navigate(`/login?username=${encodeURIComponent(email)}`)
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : t('auth.register.errorFallback'),
      )
    } finally {
      setIsLoading(false)
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
        <div className="w-full max-w-xl">
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
              {t('auth.register.title')}
            </h2>
            <p className="mt-2 text-center text-sm text-muted-foreground">
              {t('auth.register.description')}
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-5 rounded-2xl border border-border bg-card p-6">
              <div className="space-y-2">
                <Label htmlFor="email" className="font-semibold">
                  {t('common.email')}
                </Label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  autoComplete="email"
                  required
                  className="h-11 rounded-lg border-primary/30 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="font-semibold">
                  {t('auth.register.password')}
                </Label>
                <div className="relative">
                  <Input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder={t('auth.register.passwordPlaceholder')}
                    value={formData.password}
                    onChange={handleChange}
                    autoComplete="new-password"
                    required
                    className="h-11 rounded-lg border-primary/30 pr-12 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((current) => !current)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground"
                  >
                    {showPassword ? (
                      <EyeOff className="h-5 w-5" />
                    ) : (
                      <Eye className="h-5 w-5" />
                    )}
                  </button>
                </div>

                {formData.password && (
                  <div className="space-y-1">
                    <div className="flex gap-1">
                      {[...Array(4)].map((_, index) => (
                        <div
                          key={index}
                          className={`h-1 flex-1 rounded-full transition-colors ${
                            index < passwordStrength
                              ? passwordStrength === 4
                                ? 'bg-green-500'
                                : passwordStrength === 3
                                  ? 'bg-blue-500'
                                  : 'bg-yellow-500'
                              : 'bg-muted'
                          }`}
                        />
                      ))}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      {getPasswordStrengthLabel()}
                    </p>
                  </div>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="font-semibold">
                  {t('auth.register.confirmPassword')}
                </Label>
                <div className="relative">
                  <Input
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    autoComplete="new-password"
                    required
                    className="h-11 rounded-lg border-primary/30 pr-12 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                  />
                  <button
                    type="button"
                    onClick={() =>
                      setShowConfirmPassword((current) => !current)
                    }
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground"
                  >
                    {showConfirmPassword ? (
                      <EyeOff className="h-5 w-5" />
                    ) : (
                      <Eye className="h-5 w-5" />
                    )}
                  </button>
                </div>

                {formData.password && formData.confirmPassword && (
                  <div
                    className={`flex items-center gap-2 text-xs ${
                      formData.password === formData.confirmPassword
                        ? 'text-green-600'
                        : 'text-red-600'
                    }`}
                  >
                    {formData.password === formData.confirmPassword ? (
                      <>
                        <CheckCircle className="h-4 w-4" />
                        {t('auth.register.passwordMatched')}
                      </>
                    ) : (
                      <>
                        <AlertCircle className="h-4 w-4" />
                        {t('auth.register.passwordNotMatched')}
                      </>
                    )}
                  </div>
                )}
              </div>

              <div className="mt-2 rounded-2xl border border-primary/15 bg-primary/5 px-4 py-3">
                <label className="flex items-start gap-3 text-sm leading-6 text-foreground">
                  <input
                    type="checkbox"
                    checked={hasAcceptedTerms}
                    onChange={handleTermsCheckboxChange}
                    className="mt-1 h-4 w-4 rounded border-border accent-primary"
                  />
                  <span>
                    {termsCopy.agreementLabel}{' '}
                    <button
                      type="button"
                      onClick={handleTermsLinkClick}
                      className="font-semibold text-primary underline decoration-primary/40 underline-offset-4 transition hover:decoration-primary"
                    >
                      {termsCopy.linkLabel}
                    </button>
                  </span>
                </label>
              </div>
            </div>

            <Button
              type="submit"
              disabled={isLoading}
              className="flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary font-semibold text-primary-foreground transition-all hover:bg-primary/90 active:scale-95 disabled:opacity-50"
            >
              {isLoading ? (
                <>
                  <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                  {t('common.processing')}
                </>
              ) : (
                <>
                  {t('auth.register.submit')}
                  <ArrowRight className="h-4 w-4" />
                </>
              )}
            </Button>
          </form>

          <div className="my-6 flex items-center gap-3">
            <div className="h-px flex-1 bg-border" />
            <span className="text-xs font-medium text-muted-foreground">
              {t('common.or')}
            </span>
            <div className="h-px flex-1 bg-border" />
          </div>

          <p className="text-center text-sm text-muted-foreground">
            {t('auth.register.haveAccount')}{' '}
            <Link
              to="/login"
              className="font-semibold text-primary hover:underline"
            >
              {t('auth.register.loginNow')}
            </Link>
          </p>
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

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { BookOpen, Check } from 'lucide-react'
import { toast } from 'sonner'
import { AuthFlipCard } from '@/components/common/auth-flip-card'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import type { Gender, RegisterRequest } from '@/types/auth'
import { getGenderLabel } from '@/utils/i18n'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login, register } = useAuth()
  const { t } = useLanguage()
  const [isLoginLoading, setIsLoginLoading] = useState(false)
  const [isRegisterLoading, setIsRegisterLoading] = useState(false)
  const [loginFormData, setLoginFormData] = useState({
    username: '',
    password: '',
  })
  const [registerFormData, setRegisterFormData] = useState<
    RegisterRequest & { confirmPassword: string }
  >({
    username: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
    email: '',
    firstName: '',
    lastName: '',
    avatarUrl: '',
    gender: 'MALE',
    dateOfBirth: '',
  })

  const brand = t('common.brand')
  const brandPrefix = brand.endsWith('Vui') ? brand.slice(0, -3) : brand

  function handleLoginChange(event: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target
    setLoginFormData((previousValue) => ({ ...previousValue, [name]: value }))
  }

  function handleRegisterChange(
    event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) {
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

    setIsRegisterLoading(true)

    try {
      await register({
        username: registerFormData.username,
        password: registerFormData.password,
        phoneNumber: registerFormData.phoneNumber,
        email: registerFormData.email,
        firstName: registerFormData.firstName,
        lastName: registerFormData.lastName,
        avatarUrl: registerFormData.avatarUrl || null,
        gender: registerFormData.gender as Gender,
        dateOfBirth: registerFormData.dateOfBirth,
      })
      toast.success(t('auth.register.success'))
      navigate('/')
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

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="flex flex-1 items-center justify-center px-4 py-12">
        <div className="w-full max-w-md">
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
              <form
                onSubmit={handleRegisterSubmit}
                className="face_content face_content--wide"
              >
                <div className="field_grid">
                  <input
                    name="firstName"
                    placeholder={t('auth.register.firstName')}
                    className="input"
                    type="text"
                    value={registerFormData.firstName}
                    onChange={handleRegisterChange}
                    required
                  />
                  <input
                    name="lastName"
                    placeholder={t('auth.register.lastName')}
                    className="input"
                    type="text"
                    value={registerFormData.lastName}
                    onChange={handleRegisterChange}
                    required
                  />
                  <input
                    name="username"
                    placeholder={t('auth.login.username')}
                    className="input"
                    type="text"
                    value={registerFormData.username}
                    onChange={handleRegisterChange}
                    autoComplete="username"
                    required
                  />
                  <input
                    name="phoneNumber"
                    placeholder={t('auth.register.phoneNumber')}
                    className="input"
                    type="tel"
                    value={registerFormData.phoneNumber}
                    onChange={handleRegisterChange}
                    required
                  />
                  <input
                    name="email"
                    placeholder={t('common.email')}
                    className="input field_full"
                    type="email"
                    value={registerFormData.email}
                    onChange={handleRegisterChange}
                    autoComplete="email"
                    required
                  />
                  <select
                    name="gender"
                    className="input"
                    value={registerFormData.gender}
                    onChange={handleRegisterChange}
                    required
                  >
                    {(['MALE', 'FEMALE', 'OTHER'] as const).map((gender) => (
                      <option key={gender} value={gender}>
                        {getGenderLabel(gender, t)}
                      </option>
                    ))}
                  </select>
                  <input
                    name="dateOfBirth"
                    className="input"
                    type="date"
                    value={registerFormData.dateOfBirth}
                    onChange={handleRegisterChange}
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
                </div>

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
    </div>
  )
}

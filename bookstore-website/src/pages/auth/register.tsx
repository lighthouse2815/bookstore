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
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import type { Gender, RegisterRequest } from '@/types/auth'
import { getGenderLabel } from '@/utils/i18n'

type RegisterFormState = RegisterRequest & {
  confirmPassword: string
}

const initialFormData: RegisterFormState = {
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
}

export default function RegisterPage() {
  const navigate = useNavigate()
  const { register } = useAuth()
  const { t } = useLanguage()
  const [isLoading, setIsLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [formData, setFormData] = useState(initialFormData)
  const [passwordStrength, setPasswordStrength] = useState(0)

  const brand = t('common.brand')
  const brandPrefix = brand.endsWith('Vui') ? brand.slice(0, -3) : brand

  function handleChange(
    event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) {
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

    setIsLoading(true)

    try {
      await register({
        username: formData.username,
        password: formData.password,
        phoneNumber: formData.phoneNumber,
        email: formData.email,
        firstName: formData.firstName,
        lastName: formData.lastName,
        avatarUrl: formData.avatarUrl || null,
        gender: formData.gender as Gender,
        dateOfBirth: formData.dateOfBirth,
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
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="flex flex-1 items-center justify-center px-4 py-12">
        <div className="w-full max-w-3xl">
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
            <div className="grid gap-5 rounded-2xl border border-border bg-card p-6 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="username" className="font-semibold">
                  {t('auth.login.username')}
                </Label>
                <Input
                  id="username"
                  name="username"
                  type="text"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  className="h-11 rounded-lg border-primary/30 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="phoneNumber" className="font-semibold">
                  {t('auth.register.phoneNumber')}
                </Label>
                <Input
                  id="phoneNumber"
                  name="phoneNumber"
                  type="tel"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  required
                  className="h-11 rounded-lg border-primary/30 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="firstName" className="font-semibold">
                  {t('auth.register.firstName')}
                </Label>
                <Input
                  id="firstName"
                  name="firstName"
                  type="text"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  className="h-11 rounded-lg border-primary/30 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="lastName" className="font-semibold">
                  {t('auth.register.lastName')}
                </Label>
                <Input
                  id="lastName"
                  name="lastName"
                  type="text"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                  className="h-11 rounded-lg border-primary/30 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                />
              </div>

              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="email" className="font-semibold">
                  {t('common.email')}
                </Label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  className="h-11 rounded-lg border-primary/30 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="gender" className="font-semibold">
                  {t('auth.register.gender')}
                </Label>
                <select
                  id="gender"
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  className="h-11 w-full rounded-lg border border-primary/30 bg-background px-3 text-sm transition-all outline-none focus:border-primary focus:ring-2 focus:ring-primary/30"
                >
                  {(['MALE', 'FEMALE', 'OTHER'] as const).map((gender) => (
                    <option key={gender} value={gender}>
                      {getGenderLabel(gender, t)}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="dateOfBirth" className="font-semibold">
                  {t('auth.register.dateOfBirth')}
                </Label>
                <Input
                  id="dateOfBirth"
                  name="dateOfBirth"
                  type="date"
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                  required
                  className="h-11 rounded-lg border-primary/30 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                />
              </div>

              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="avatarUrl" className="font-semibold">
                  {t('auth.register.avatarUrl')}
                </Label>
                <Input
                  id="avatarUrl"
                  name="avatarUrl"
                  type="url"
                  value={formData.avatarUrl ?? ''}
                  onChange={handleChange}
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
    </div>
  )
}

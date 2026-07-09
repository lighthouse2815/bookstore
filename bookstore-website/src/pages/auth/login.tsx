import { Link } from 'react-router-dom'
import { ArrowRight, BookOpen, Check, Mail, ShieldCheck } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { AuthFlipCard } from '@/components/common/auth-flip-card'
import { GoogleAuthButton } from '@/components/common/google-auth-button'
import { RegisterTermsDialog } from '@/components/common/register-terms-dialog'
import { useLoginPage } from '@/hooks/use-login-page'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import lockImage from '@/assets/lock_2.png'

export default function LoginPage() {
  const {
    language,
    t,
    loginForm,
    registerForm,
    termsConsent,
    brandPrefix,
    brandSuffix,
    isRegisterFace,
    handleRegisterFaceChange,
    handleActivationSubmit,
    handleLoginWithGoogle,
    handleGoogleRegister,
    handleLoginSubmit,
    handleRegisterSubmit,
  } = useLoginPage()

  const showLockedScreen = loginForm.isLockedRestriction
  const showInactiveScreen = loginForm.isInactiveRestriction
  const showSpecialScreen = showLockedScreen || showInactiveScreen

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="flex flex-1 items-center justify-center px-4 py-12">
        <div className={`w-full ${showSpecialScreen ? 'max-w-xl' : 'max-w-lg'}`}>
          <div className="absolute inset-0 -z-10 bg-gradient-to-br from-primary/5 via-transparent to-transparent opacity-50" />

          <div className="mb-8 flex flex-col items-center">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-2 transition-all hover:bg-primary/15">
              <BookOpen className="h-7 w-7 text-primary" />
              <span className="font-heading text-xl font-bold text-primary">
                {brandPrefix}
                {brandSuffix}
              </span>
            </div>
            <h2 className="text-center font-heading text-3xl font-bold text-foreground">
              {t('auth.login.title')}
            </h2>
            <p className="mt-2 text-center text-sm text-muted-foreground">
              {t('auth.login.description')}
            </p>
          </div>

          {showLockedScreen ? (
            <div className="overflow-hidden rounded-[36px] border border-amber-500/20 bg-gradient-to-br from-amber-500/10 via-background to-red-500/5 p-6 shadow-[0_30px_90px_rgba(15,23,42,0.18)] backdrop-blur sm:p-8">
              <div className="mx-auto flex max-w-lg flex-col items-center text-center">
                <img
                  src={lockImage}
                  alt={loginForm.restrictionCopy?.title ?? 'Locked account'}
                  className="mb-6 h-80 w-80 max-w-full object-contain drop-shadow-[0_24px_60px_rgba(15,23,42,0.24)] sm:h-[28rem] sm:w-[28rem] lg:h-[32rem] lg:w-[32rem]"
                />
                <p className="rounded-full bg-amber-500/12 px-4 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-amber-700 dark:text-amber-300">
                  {t('auth.login.cardTitle')}
                </p>
                <h3 className="mt-5 text-3xl font-bold text-foreground sm:text-4xl">
                  {loginForm.restrictionCopy?.title}
                </h3>
                <p className="mt-4 max-w-md text-sm leading-7 text-muted-foreground sm:text-base">
                  {loginForm.restrictionCopy?.description}
                </p>
                <Button
                  type="button"
                  variant="outline"
                  size="lg"
                  onClick={loginForm.clearLoginRestriction}
                  className="mt-8 rounded-2xl border-amber-500/30 bg-background/70 px-6"
                >
                  {loginForm.flowCopy.lockedActionLabel}
                </Button>
              </div>
            </div>
          ) : showInactiveScreen ? (
            <div className="overflow-hidden rounded-[32px] border border-border/70 bg-card/95 p-6 shadow-[0_28px_90px_rgba(15,23,42,0.16)] backdrop-blur sm:p-8">
              <div className="mx-auto flex max-w-md flex-col gap-5">
                <div className="flex items-start gap-4 rounded-3xl border border-sky-500/20 bg-sky-500/10 p-5">
                  <ShieldCheck className="mt-1 h-6 w-6 flex-shrink-0 text-sky-600 dark:text-sky-400" />
                  <div className="space-y-2">
                    <h3 className="text-xl font-bold text-foreground">
                      {loginForm.restrictionCopy?.title}
                    </h3>
                    <p className="text-sm leading-6 text-muted-foreground">
                      {loginForm.restrictionCopy?.description}
                    </p>
                  </div>
                </div>

                <div className="rounded-3xl border border-border/70 bg-background/70 p-4">
                  <p className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                    {t('common.email')}
                  </p>
                  <p className="mt-2 break-all text-sm font-semibold text-foreground">
                    {loginForm.activationEmail}
                  </p>
                </div>

                <p className="text-sm leading-6 text-muted-foreground">
                  {loginForm.flowCopy.inactiveOtpLead}
                </p>

                <form onSubmit={handleActivationSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <label
                      htmlFor="activationOtpCode"
                      className="text-sm font-semibold text-foreground"
                    >
                      {t('auth.register.otpCode')}
                    </label>
                    <Input
                      id="activationOtpCode"
                      name="activationOtpCode"
                      type="text"
                      inputMode="numeric"
                      autoComplete="one-time-code"
                      placeholder={t('auth.register.otpPlaceholder')}
                      value={loginForm.activationOtpCode}
                      onChange={loginForm.handleActivationOtpChange}
                      required
                      className="h-12 rounded-2xl text-center text-lg font-semibold tracking-[0.35em]"
                    />
                    <div className="flex flex-col items-start gap-2 sm:flex-row sm:items-center sm:justify-between">
                      <p className="flex-1 text-xs leading-5 text-muted-foreground">
                        {loginForm.flowCopy.inactiveOtpReadyHint}
                      </p>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => void loginForm.requestActivationOtp()}
                        disabled={
                          loginForm.isActivationRequestLoading ||
                          loginForm.isActivationLoading
                        }
                        className="h-auto rounded-xl px-0 text-sky-700 hover:bg-transparent hover:text-sky-800 dark:text-sky-300 dark:hover:text-sky-200"
                      >
                        {loginForm.isActivationRequestLoading ? (
                          <>
                            <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" />
                            {t('common.processing')}
                          </>
                        ) : (
                          <>
                            <Mail className="h-3.5 w-3.5" />
                            {loginForm.flowCopy.inactiveActionLabel}
                          </>
                        )}
                      </Button>
                    </div>
                  </div>

                  <div className="flex flex-col gap-3 sm:flex-row">
                    <Button
                      type="button"
                      variant="ghost"
                      size="lg"
                      onClick={loginForm.clearLoginRestriction}
                      disabled={
                        loginForm.isActivationRequestLoading ||
                        loginForm.isActivationLoading
                      }
                      className="rounded-2xl"
                    >
                      {loginForm.flowCopy.inactiveBackLabel}
                    </Button>
                    <Button
                      type="submit"
                      size="lg"
                      disabled={
                        loginForm.isActivationLoading ||
                        loginForm.isActivationRequestLoading
                      }
                      className="rounded-2xl sm:flex-1"
                    >
                      {loginForm.isActivationLoading ? (
                        <>
                          <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                          {t('common.processing')}
                        </>
                      ) : (
                        <>
                          {loginForm.flowCopy.inactiveVerifyLabel}
                          <ArrowRight className="h-4 w-4" />
                        </>
                      )}
                    </Button>
                  </div>
                </form>
              </div>
            </div>
          ) : (
            <AuthFlipCard
              checked={isRegisterFace}
              onCheckedChange={handleRegisterFaceChange}
              frontTitle={t('auth.login.cardTitle')}
              backTitle={t('auth.register.title')}
              frontContent={
                <form onSubmit={handleLoginSubmit} className="face_content">
                  <input
                    name="username"
                    type="text"
                    placeholder={t('auth.login.username')}
                    className="input"
                    value={loginForm.formData.username}
                    onChange={loginForm.handleChange}
                    autoComplete="username"
                    required
                  />
                  <input
                    name="password"
                    type="password"
                    placeholder={t('auth.login.password')}
                    className="input"
                    value={loginForm.formData.password}
                    onChange={loginForm.handleChange}
                    autoComplete="current-password"
                    required
                  />
                  <Link
                    to="/forgot-password"
                    className="ml-auto text-xs font-semibold text-[color:var(--auth-accent)] underline decoration-current/40 underline-offset-4"
                  >
                    {t('auth.login.forgotPassword')}
                  </Link>

                  <button
                    type="submit"
                    className="btn"
                    disabled={loginForm.isLoading || loginForm.isGoogleLoading}
                  >
                    {loginForm.isLoading ? (
                      <span className="loading_inline">
                        <span className="loading_dot" />
                        <span>{t('common.processing')}</span>
                      </span>
                    ) : (
                      t('auth.login.submit')
                    )}
                  </button>

                  <div className="my-1 flex items-center gap-3 text-[0.7rem] uppercase tracking-[0.16em] text-[color:var(--auth-text)]/70">
                    <span className="h-px flex-1 bg-white/12" />
                    <span>{t('common.or')}</span>
                    <span className="h-px flex-1 bg-white/12" />
                  </div>

                  {!isRegisterFace ? (
                    <GoogleAuthButton
                      locale={language}
                      text="signin_with"
                      isLoading={loginForm.isGoogleLoading}
                      onCredential={handleLoginWithGoogle}
                    />
                  ) : null}
                </form>
              }
              backContent={
                <form onSubmit={handleRegisterSubmit} className="face_content">
                  <input
                    name="email"
                    placeholder={t('common.email')}
                    className="input"
                    type="email"
                    value={registerForm.formData.email}
                    onChange={registerForm.handleChange}
                    autoComplete="email"
                    required
                  />
                  <input
                    name="password"
                    placeholder={t('auth.register.password')}
                    className="input"
                    type="password"
                    value={registerForm.formData.password}
                    onChange={registerForm.handleChange}
                    autoComplete="new-password"
                    required
                  />
                  <input
                    name="confirmPassword"
                    placeholder={t('auth.register.confirmPassword')}
                    className="input"
                    type="password"
                    value={registerForm.formData.confirmPassword}
                    onChange={registerForm.handleChange}
                    autoComplete="new-password"
                    required
                  />
                  <label className="flex items-start gap-3 rounded-2xl border border-white/12 bg-white/8 px-3 py-3 text-left text-sm leading-6 text-[color:var(--auth-text)]">
                    <input
                      type="checkbox"
                      checked={termsConsent.hasAcceptedTerms}
                      onChange={termsConsent.handleTermsCheckboxChange}
                      className="mt-1 h-4 w-4 rounded accent-primary"
                    />
                    <span>
                      {termsConsent.termsCopy.agreementLabel}{' '}
                      <button
                        type="button"
                        onClick={termsConsent.handleTermsLinkClick}
                        className="font-semibold text-[color:var(--auth-accent)] underline decoration-current/40 underline-offset-4"
                      >
                        {termsConsent.termsCopy.linkLabel}
                      </button>
                    </span>
                  </label>
                  <button
                    type="submit"
                    className="btn"
                    disabled={registerForm.isLoading || registerForm.isGoogleLoading}
                  >
                    {registerForm.isLoading ? (
                      <span className="loading_inline">
                        <span className="loading_dot" />
                        <span>{t('common.processing')}</span>
                      </span>
                    ) : (
                      t('auth.register.submit')
                    )}
                  </button>

                  <div className="my-1 flex items-center gap-3 text-[0.7rem] uppercase tracking-[0.16em] text-[color:var(--auth-text)]/70">
                    <span className="h-px flex-1 bg-white/12" />
                    <span>{t('common.or')}</span>
                    <span className="h-px flex-1 bg-white/12" />
                  </div>

                  {isRegisterFace ? (
                    <GoogleAuthButton
                      locale={language}
                      text="signup_with"
                      isLoading={registerForm.isGoogleLoading}
                      disabled={!termsConsent.hasAcceptedTerms}
                      disabledMessage={termsConsent.termsCopy.requiredMessage}
                      onCredential={handleGoogleRegister}
                    />
                  ) : null}
                </form>
              }
              frontSwitchText={t('auth.login.noAccount')}
              frontSwitchAction={t('auth.login.registerNow')}
              backSwitchText={t('auth.register.haveAccount')}
              backSwitchAction={t('auth.login.cardTitle')}
            />
          )}

          {!showSpecialScreen ? (
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
          ) : null}
        </div>
      </main>
      <Footer />
      <RegisterTermsDialog
        open={termsConsent.isTermsOpen}
        onClose={termsConsent.handleTermsDialogClose}
      />
    </div>
  )
}

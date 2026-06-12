import { Link } from 'react-router-dom'
import {
  AlertCircle,
  ArrowRight,
  BookOpen,
  CheckCircle,
  Eye,
  EyeOff,
  ShieldCheck,
} from 'lucide-react'
import styled from 'styled-components'
import { Button } from '@/components/common/button'
import { GoogleAuthButton } from '@/components/common/google-auth-button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { RegisterTermsDialog } from '@/components/common/register-terms-dialog'
import { useRegisterPage } from '@/hooks/use-register-page'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'

export default function RegisterPage() {
  const {
    language,
    t,
    registerForm,
    termsConsent,
    verification,
    brandPrefix,
    brandSuffix,
    showPassword,
    showConfirmPassword,
    pageTitle,
    pageDescription,
    handleGoogleRegister,
    handleSubmit,
    handleVerifySubmit,
    togglePasswordVisibility,
    toggleConfirmPasswordVisibility,
  } = useRegisterPage()

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
                {brandSuffix}
              </span>
            </div>
            <h2 className="text-center font-heading text-3xl font-bold text-foreground">
              {pageTitle}
            </h2>
            <p className="mt-2 text-center text-sm text-muted-foreground">
              {pageDescription}
            </p>
          </div>

          <StyledRegisterFlowCard>
            {verification.currentStep === 'verify' ? (
              <div className="otp_shell">
                <div className="otp_frame">
                  <div className="otp_card">
                    <div className="otp_notice">
                      <ShieldCheck className="otp_notice_icon" />
                      <div className="otp_notice_copy">
                        <p className="otp_notice_title">
                          {t('auth.register.otpSent')}
                        </p>
                        <p className="otp_notice_text">
                          {t('auth.register.verifyHint')}
                        </p>
                      </div>
                    </div>

                    <form onSubmit={handleVerifySubmit} className="otp_content">
                      <div className="otp_field">
                        <label
                          htmlFor="verificationEmail"
                          className="otp_field_label"
                        >
                          {t('common.email')}
                        </label>
                        <input
                          id="verificationEmail"
                          value={verification.verificationEmail}
                          readOnly
                          className="otp_input otp_input--readonly"
                        />
                      </div>

                      <div className="otp_field">
                        <label htmlFor="otpCode" className="otp_field_label">
                          {t('auth.register.otpCode')}
                        </label>
                        <input
                          id="otpCode"
                          name="otpCode"
                          type="text"
                          inputMode="numeric"
                          autoComplete="one-time-code"
                          placeholder={t('auth.register.otpPlaceholder')}
                          value={verification.otpCode}
                          onChange={verification.handleOtpChange}
                          required
                          className="otp_input otp_input--otp"
                        />
                        <p className="otp_hint">{t('auth.register.otpHint')}</p>
                      </div>

                      <div className="otp_text_actions">
                        <button
                          type="button"
                          onClick={() => void verification.resendOtp()}
                          disabled={
                            verification.isRequestingOtp || verification.isLoading
                          }
                          className="otp_text_btn otp_text_btn--start"
                        >
                          {verification.isRequestingOtp
                            ? t('common.processing')
                            : verification.copy.resendOtpLabel}
                        </button>

                        <button
                          type="button"
                          onClick={verification.goBackToRegister}
                          disabled={
                            verification.isRequestingOtp || verification.isLoading
                          }
                          className="otp_text_btn"
                        >
                          {t('auth.register.backToRegister')}
                        </button>
                      </div>

                      <button
                        type="submit"
                        disabled={
                          verification.isLoading || verification.isRequestingOtp
                        }
                        className="otp_btn"
                      >
                        {verification.isLoading ? (
                          <span className="loading_inline">
                            <span className="loading_dot" />
                            <span>{t('common.processing')}</span>
                          </span>
                        ) : (
                          <span className="otp_btn_content">
                            <span>{t('auth.register.verifySubmit')}</span>
                            <ArrowRight className="h-4 w-4" />
                          </span>
                        )}
                      </button>
                    </form>
                  </div>
                </div>
              </div>
            ) : (
              <div className="otp_shell">
                <div className="otp_frame otp_frame--wide">
                  <div className="otp_card">
                    <form onSubmit={handleSubmit} className="register_flow">
                      <div className="register_panel">
                        <div className="space-y-2">
                          <Label htmlFor="email" className="font-semibold">
                            {t('common.email')}
                          </Label>
                          <Input
                            id="email"
                            name="email"
                            type="email"
                            value={registerForm.formData.email}
                            onChange={registerForm.handleChange}
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
                              value={registerForm.formData.password}
                              onChange={registerForm.handleChange}
                              autoComplete="new-password"
                              required
                              className="h-11 rounded-lg border-primary/30 pr-12 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                            />
                            <button
                              type="button"
                              onClick={togglePasswordVisibility}
                              className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground"
                            >
                              {showPassword ? (
                                <EyeOff className="h-5 w-5" />
                              ) : (
                                <Eye className="h-5 w-5" />
                              )}
                            </button>
                          </div>

                          {registerForm.formData.password && (
                            <div className="space-y-1">
                              <div className="flex gap-1">
                                {[...Array(4)].map((_, index) => (
                                  <div
                                    key={index}
                                    className={`h-1 flex-1 rounded-full transition-colors ${
                                      index < registerForm.passwordStrength
                                        ? registerForm.passwordStrength === 4
                                          ? 'bg-green-500'
                                          : registerForm.passwordStrength === 3
                                            ? 'bg-blue-500'
                                            : 'bg-yellow-500'
                                        : 'bg-muted'
                                    }`}
                                  />
                                ))}
                              </div>
                              <p className="text-xs text-muted-foreground">
                                {registerForm.passwordStrengthLabel}
                              </p>
                            </div>
                          )}
                        </div>

                        <div className="space-y-2">
                          <Label
                            htmlFor="confirmPassword"
                            className="font-semibold"
                          >
                            {t('auth.register.confirmPassword')}
                          </Label>
                          <div className="relative">
                            <Input
                              id="confirmPassword"
                              name="confirmPassword"
                              type={showConfirmPassword ? 'text' : 'password'}
                              value={registerForm.formData.confirmPassword}
                              onChange={registerForm.handleChange}
                              autoComplete="new-password"
                              required
                              className="h-11 rounded-lg border-primary/30 pr-12 transition-all focus:border-primary focus:ring-2 focus:ring-primary/30"
                            />
                            <button
                              type="button"
                              onClick={toggleConfirmPasswordVisibility}
                              className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground"
                            >
                              {showConfirmPassword ? (
                                <EyeOff className="h-5 w-5" />
                              ) : (
                                <Eye className="h-5 w-5" />
                              )}
                            </button>
                          </div>

                          {registerForm.formData.password &&
                          registerForm.formData.confirmPassword && (
                            <div
                              className={`flex items-center gap-2 text-xs ${
                                registerForm.passwordsMatch
                                  ? 'text-green-600'
                                  : 'text-red-600'
                              }`}
                            >
                              {registerForm.passwordsMatch ? (
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

                        <div className="register_terms">
                          <label className="flex items-start gap-3 text-sm leading-6 text-foreground">
                            <input
                              type="checkbox"
                              checked={termsConsent.hasAcceptedTerms}
                              onChange={termsConsent.handleTermsCheckboxChange}
                              className="mt-1 h-4 w-4 rounded border-border accent-primary"
                            />
                            <span>
                              {termsConsent.termsCopy.agreementLabel}{' '}
                              <button
                                type="button"
                                onClick={termsConsent.handleTermsLinkClick}
                                className="font-semibold text-primary underline decoration-primary/40 underline-offset-4 transition hover:decoration-primary"
                              >
                                {termsConsent.termsCopy.linkLabel}
                              </button>
                            </span>
                          </label>
                        </div>
                      </div>

                      <Button
                        type="submit"
                        disabled={
                          registerForm.isLoading || registerForm.isGoogleLoading
                        }
                        className="flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary font-semibold text-primary-foreground transition-all hover:bg-primary/90 active:scale-95 disabled:opacity-50"
                      >
                        {registerForm.isLoading ? (
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

                      <div className="register_divider">
                        <div className="h-px flex-1 bg-border" />
                        <span className="text-xs font-medium text-muted-foreground">
                          {t('common.or')}
                        </span>
                        <div className="h-px flex-1 bg-border" />
                      </div>

                      <GoogleAuthButton
                        locale={language}
                        text="signup_with"
                        isLoading={registerForm.isGoogleLoading}
                        disabled={!termsConsent.hasAcceptedTerms}
                        disabledMessage={termsConsent.termsCopy.requiredMessage}
                        onCredential={handleGoogleRegister}
                      />
                    </form>
                  </div>
                </div>
              </div>
            )}
          </StyledRegisterFlowCard>

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
        open={termsConsent.isTermsOpen}
        onClose={termsConsent.handleTermsDialogClose}
      />
    </div>
  )
}

const StyledRegisterFlowCard = styled.div`
  --auth-shell:
    radial-gradient(circle at top, rgba(99, 102, 241, 0.14), transparent 42%),
    radial-gradient(
      circle at bottom right,
      rgba(14, 165, 233, 0.1),
      transparent 34%
    ),
    linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.94),
      rgba(248, 250, 252, 0.98)
    );
  --auth-surface:
    linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.92),
      rgba(244, 247, 255, 0.96)
    );
  --auth-surface-border: rgba(71, 85, 105, 0.14);
  --auth-surface-shadow:
    0 30px 90px rgba(15, 23, 42, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.72);
  --auth-title: var(--foreground);
  --auth-text: var(--muted-foreground);
  --auth-accent: var(--primary);
  --auth-input-bg: rgba(255, 255, 255, 0.82);
  --auth-input-border: rgba(99, 102, 241, 0.18);
  --auth-input-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.75),
    0 10px 24px rgba(99, 102, 241, 0.08);
  --auth-input-shadow-focus:
    0 0 0 4px rgba(99, 102, 241, 0.14),
    0 16px 32px rgba(99, 102, 241, 0.12);
  --auth-btn-bg:
    linear-gradient(135deg, rgba(79, 70, 229, 0.98), rgba(37, 99, 235, 0.95));
  --auth-btn-shadow:
    0 18px 40px rgba(79, 70, 229, 0.24),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  --auth-btn-shadow-hover:
    0 22px 44px rgba(79, 70, 229, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);

  .dark & {
    --auth-shell:
      radial-gradient(circle at top, rgba(129, 140, 248, 0.2), transparent 42%),
      radial-gradient(
        circle at bottom right,
        rgba(56, 189, 248, 0.14),
        transparent 34%
      ),
      linear-gradient(180deg, rgba(20, 24, 39, 0.95), rgba(10, 14, 24, 0.98));
    --auth-surface:
      linear-gradient(180deg, rgba(24, 28, 45, 0.95), rgba(16, 20, 34, 0.96));
    --auth-surface-border: rgba(148, 163, 184, 0.18);
    --auth-surface-shadow:
      0 36px 100px rgba(2, 6, 23, 0.52),
      inset 0 1px 0 rgba(255, 255, 255, 0.05);
    --auth-title: rgba(248, 250, 252, 0.98);
    --auth-text: rgba(203, 213, 225, 0.8);
    --auth-input-bg: rgba(15, 23, 42, 0.72);
    --auth-input-border: rgba(129, 140, 248, 0.18);
    --auth-input-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.04),
      0 14px 34px rgba(2, 6, 23, 0.34);
    --auth-input-shadow-focus:
      0 0 0 4px rgba(129, 140, 248, 0.16),
      0 18px 36px rgba(15, 23, 42, 0.34);
    --auth-btn-shadow:
      0 18px 44px rgba(79, 70, 229, 0.35),
      inset 0 1px 0 rgba(255, 255, 255, 0.08);
    --auth-btn-shadow-hover:
      0 22px 48px rgba(79, 70, 229, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.1);
  }

  .otp_shell {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    min-height: 440px;
    padding: 12px 0;
  }

  .otp_frame {
    width: min(100%, 460px);
    border-radius: 28px;
    background: var(--auth-shell);
    padding: 10px;
    box-shadow: 0 18px 70px rgba(15, 23, 42, 0.12);
  }

  .otp_frame--wide {
    width: min(100%, 580px);
  }

  .otp_card {
    display: flex;
    flex-direction: column;
    gap: 18px;
    border-radius: 22px;
    padding: 34px 30px 26px;
    background: var(--auth-surface);
    border: 1px solid var(--auth-surface-border);
    box-shadow: var(--auth-surface-shadow);
  }

  .otp_content {
    width: 100%;
    max-width: 310px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .register_flow {
    width: 100%;
    max-width: 100%;
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .register_panel {
    display: flex;
    flex-direction: column;
    gap: 20px;
    border-radius: 22px;
    border: 1px solid rgba(99, 102, 241, 0.1);
    background: rgba(255, 255, 255, 0.52);
    padding: 24px;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.65),
      0 14px 28px rgba(99, 102, 241, 0.06);
  }

  .dark & .register_panel {
    background: rgba(15, 23, 42, 0.42);
    border-color: rgba(148, 163, 184, 0.14);
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.04),
      0 16px 30px rgba(2, 6, 23, 0.24);
  }

  .register_terms {
    margin-top: 4px;
    border-radius: 18px;
    border: 1px solid rgba(99, 102, 241, 0.12);
    background: rgba(99, 102, 241, 0.05);
    padding: 14px 16px;
  }

  .dark & .register_terms {
    border-color: rgba(129, 140, 248, 0.18);
    background: rgba(79, 70, 229, 0.08);
  }

  .register_divider {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .otp_notice {
    display: flex;
    align-items: flex-start;
    gap: 14px;
    margin: 0 auto;
    width: 100%;
    max-width: 360px;
    border-radius: 18px;
    border: 1px solid rgba(99, 102, 241, 0.16);
    background: rgba(255, 255, 255, 0.46);
    padding: 15px 16px;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.7),
      0 12px 26px rgba(99, 102, 241, 0.08);
  }

  .dark & .otp_notice {
    background: rgba(15, 23, 42, 0.52);
  }

  .otp_notice_icon {
    width: 18px;
    height: 18px;
    margin-top: 1px;
    flex-shrink: 0;
    color: var(--auth-accent);
  }

  .otp_notice_copy {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  .otp_notice_title {
    color: var(--auth-title);
    font-size: 0.95rem;
    font-weight: 700;
    line-height: 1.4;
  }

  .otp_notice_text {
    color: var(--auth-text);
    font-size: 0.82rem;
    line-height: 1.55;
  }

  .otp_field {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .otp_field_label {
    color: var(--auth-title);
    font-size: 0.95rem;
    font-weight: 600;
  }

  .otp_input {
    width: 100%;
    min-height: 48px;
    color: var(--auth-title);
    outline: none;
    transition:
      border-color 0.25s ease,
      box-shadow 0.25s ease,
      background-color 0.25s ease,
      transform 0.25s ease;
    padding: 0 14px;
    background: var(--auth-input-bg);
    border-radius: 14px;
    border: 1px solid var(--auth-input-border);
    box-shadow: var(--auth-input-shadow);
    appearance: none;
  }

  .otp_input::placeholder {
    color: var(--auth-text);
  }

  .otp_input:focus {
    border-color: rgba(99, 102, 241, 0.45);
    box-shadow: var(--auth-input-shadow-focus);
    transform: translateY(-1px);
  }

  .otp_input--readonly {
    color: var(--auth-text);
    cursor: default;
  }

  .otp_input--otp {
    text-align: center;
    font-size: 1.15rem;
    font-weight: 700;
    letter-spacing: 0.35em;
  }

  .otp_hint {
    color: var(--auth-text);
    font-size: 0.78rem;
    line-height: 1.5;
  }

  .otp_btn {
    width: 100%;
    min-height: 50px;
    margin-top: 6px;
    padding: 12px 18px;
    cursor: pointer;
    background: var(--auth-btn-bg);
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.12);
    box-shadow: var(--auth-btn-shadow);
    color: rgba(255, 255, 255, 0.98);
    font-size: 15px;
    font-weight: 700;
    transition:
      transform 0.2s ease,
      box-shadow 0.2s ease,
      opacity 0.2s ease;
  }

  .otp_btn:hover,
  .otp_btn:focus {
    transform: translateY(-1px);
    box-shadow: var(--auth-btn-shadow-hover);
  }

  .otp_btn:disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }

  .otp_btn_content,
  .loading_inline {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .otp_text_btn {
    width: fit-content;
    color: var(--auth-accent);
    font-size: 0.85rem;
    font-weight: 600;
    background: transparent;
    border: 0;
    padding: 0;
    cursor: pointer;
    text-decoration: underline;
    text-decoration-color: rgba(99, 102, 241, 0.35);
    text-underline-offset: 4px;
  }

  .otp_text_btn:hover {
    text-decoration-color: currentColor;
  }

  .otp_text_btn:disabled {
    cursor: not-allowed;
    opacity: 0.65;
  }

  .otp_text_actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .otp_text_btn--start {
    align-self: flex-start;
  }

  .loading_dot {
    width: 14px;
    height: 14px;
    border-radius: 9999px;
    border: 2px solid currentColor;
    border-top-color: transparent;
    animation: spin 0.8s linear infinite;
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  @media (max-width: 480px) {
    .otp_shell {
      min-height: 460px;
    }

    .otp_frame {
      width: min(100%, 352px);
      padding: 8px;
    }

    .otp_frame--wide {
      width: min(100%, 352px);
    }

    .otp_card {
      padding: 28px 18px 22px;
    }

    .otp_content {
      max-width: 100%;
    }

    .register_panel {
      padding: 20px 16px;
    }

    .otp_notice {
      max-width: 100%;
    }

    .otp_text_actions {
      flex-direction: column;
      align-items: flex-start;
    }
  }
`

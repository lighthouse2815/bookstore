import { Link } from 'react-router-dom'
import {
  ArrowRight,
  BookOpen,
  Eye,
  EyeOff,
} from 'lucide-react'
import styled from 'styled-components'
import { Footer } from '@/components/layout/footer'
import { useForgotPasswordFlow } from '@/hooks/use-forgot-password-flow'
import { useBrandWordmark } from '@/hooks/use-brand-wordmark'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'

export default function ForgotPasswordPage() {
  const { t } = useLanguage()
  const forgotPasswordFlow = useForgotPasswordFlow()
  const { brandPrefix, brandSuffix } = useBrandWordmark()
  const NoticeIcon = forgotPasswordFlow.NoticeIcon

  async function handleRequestSubmit(event: React.FormEvent) {
    event.preventDefault()
    await forgotPasswordFlow.submitRequest()
  }

  async function handleVerifySubmit(event: React.FormEvent) {
    event.preventDefault()
    await forgotPasswordFlow.submitVerification()
  }

  async function handleResetSubmit(event: React.FormEvent) {
    event.preventDefault()
    await forgotPasswordFlow.submitReset()
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
                {brandSuffix}
              </span>
            </div>
            <h2 className="text-center font-heading text-3xl font-bold text-foreground">
              {forgotPasswordFlow.pageTitle}
            </h2>
            <p className="mt-2 text-center text-sm text-muted-foreground">
              {forgotPasswordFlow.pageDescription}
            </p>
          </div>

          <StyledForgotPasswordCard>
            <div className="flow_shell">
              <div className="flow_frame">
                <div className="flow_card">
                  <div className="flow_notice">
                    <NoticeIcon className="flow_notice_icon" />
                    <div className="flow_notice_copy">
                      <p className="flow_notice_title">
                        {forgotPasswordFlow.noticeTitle}
                      </p>
                      <p className="flow_notice_text">
                        {forgotPasswordFlow.noticeText}
                      </p>
                    </div>
                  </div>

                  {forgotPasswordFlow.currentStep === 'request' ? (
                    <form
                      onSubmit={handleRequestSubmit}
                      className="flow_content"
                    >
                      <div className="flow_field">
                        <label htmlFor="email" className="flow_field_label">
                          {t('common.email')}
                        </label>
                        <input
                          id="email"
                          type="email"
                          autoComplete="email"
                          value={forgotPasswordFlow.email}
                          onChange={forgotPasswordFlow.handleEmailChange}
                          className="flow_input"
                          required
                        />
                      </div>

                      <button
                        type="submit"
                        disabled={forgotPasswordFlow.isRequestLoading}
                        className="flow_btn"
                      >
                        {forgotPasswordFlow.isRequestLoading ? (
                          <span className="loading_inline">
                            <span className="loading_dot" />
                            <span>{t('common.processing')}</span>
                          </span>
                        ) : (
                          <span className="flow_btn_content">
                            <span>{t('auth.forgotPassword.requestSubmit')}</span>
                            <ArrowRight className="h-4 w-4" />
                          </span>
                        )}
                      </button>
                    </form>
                  ) : null}

                  {forgotPasswordFlow.currentStep === 'verify' ? (
                    <form onSubmit={handleVerifySubmit} className="flow_content">
                      <div className="flow_field">
                        <label
                          htmlFor="verificationEmail"
                          className="flow_field_label"
                        >
                          {t('common.email')}
                        </label>
                        <input
                          id="verificationEmail"
                          value={forgotPasswordFlow.email}
                          readOnly
                          className="flow_input flow_input--readonly"
                        />
                      </div>

                      <div className="flow_field">
                        <label htmlFor="otpCode" className="flow_field_label">
                          {t('auth.forgotPassword.otpCode')}
                        </label>
                        <input
                          id="otpCode"
                          name="otpCode"
                          type="text"
                          inputMode="numeric"
                          autoComplete="one-time-code"
                          placeholder={t('auth.forgotPassword.otpPlaceholder')}
                          value={forgotPasswordFlow.otpCode}
                          onChange={forgotPasswordFlow.handleOtpChange}
                          required
                          className="flow_input flow_input--otp"
                        />
                      </div>

                      <button
                        type="button"
                        onClick={forgotPasswordFlow.goBackToRequest}
                        className="flow_text_btn"
                      >
                        {t('auth.forgotPassword.backStep')}
                      </button>

                      <button
                        type="submit"
                        disabled={forgotPasswordFlow.isVerifyLoading}
                        className="flow_btn"
                      >
                        {forgotPasswordFlow.isVerifyLoading ? (
                          <span className="loading_inline">
                            <span className="loading_dot" />
                            <span>{t('common.processing')}</span>
                          </span>
                        ) : (
                          <span className="flow_btn_content">
                            <span>{t('auth.forgotPassword.verifySubmit')}</span>
                            <ArrowRight className="h-4 w-4" />
                          </span>
                        )}
                      </button>
                    </form>
                  ) : null}

                  {forgotPasswordFlow.currentStep === 'reset' ? (
                    <form onSubmit={handleResetSubmit} className="flow_content">
                      <div className="flow_field">
                        <label
                          htmlFor="resetEmail"
                          className="flow_field_label"
                        >
                          {t('common.email')}
                        </label>
                        <input
                          id="resetEmail"
                          value={forgotPasswordFlow.email}
                          readOnly
                          className="flow_input flow_input--readonly"
                        />
                      </div>

                      <div className="flow_field">
                        <label
                          htmlFor="newPassword"
                          className="flow_field_label"
                        >
                          {t('auth.forgotPassword.newPassword')}
                        </label>
                        <div className="flow_password_wrap">
                          <input
                            id="newPassword"
                            type={
                              forgotPasswordFlow.showNewPassword
                                ? 'text'
                                : 'password'
                            }
                            autoComplete="new-password"
                            placeholder={t(
                              'auth.forgotPassword.newPasswordPlaceholder',
                            )}
                            value={forgotPasswordFlow.newPassword}
                            onChange={forgotPasswordFlow.handleNewPasswordChange}
                            className="flow_input flow_input--with_icon"
                            required
                          />
                          <button
                            type="button"
                            onClick={
                              forgotPasswordFlow.toggleNewPasswordVisibility
                            }
                            className="flow_icon_btn"
                          >
                            {forgotPasswordFlow.showNewPassword ? (
                              <EyeOff className="h-5 w-5" />
                            ) : (
                              <Eye className="h-5 w-5" />
                            )}
                          </button>
                        </div>
                      </div>

                      <div className="flow_field">
                        <label
                          htmlFor="confirmPassword"
                          className="flow_field_label"
                        >
                          {t('auth.forgotPassword.confirmPassword')}
                        </label>
                        <div className="flow_password_wrap">
                          <input
                            id="confirmPassword"
                            type={
                              forgotPasswordFlow.showConfirmPassword
                                ? 'text'
                                : 'password'
                            }
                            autoComplete="new-password"
                            value={forgotPasswordFlow.confirmPassword}
                            onChange={
                              forgotPasswordFlow.handleConfirmPasswordChange
                            }
                            className="flow_input flow_input--with_icon"
                            required
                          />
                          <button
                            type="button"
                            onClick={
                              forgotPasswordFlow.toggleConfirmPasswordVisibility
                            }
                            className="flow_icon_btn"
                          >
                            {forgotPasswordFlow.showConfirmPassword ? (
                              <EyeOff className="h-5 w-5" />
                            ) : (
                              <Eye className="h-5 w-5" />
                            )}
                          </button>
                        </div>
                      </div>

                      <button
                        type="button"
                        onClick={forgotPasswordFlow.goBackToVerify}
                        className="flow_text_btn"
                      >
                        {t('auth.forgotPassword.backStep')}
                      </button>

                      <button
                        type="submit"
                        disabled={forgotPasswordFlow.isResetLoading}
                        className="flow_btn"
                      >
                        {forgotPasswordFlow.isResetLoading ? (
                          <span className="loading_inline">
                            <span className="loading_dot" />
                            <span>{t('common.processing')}</span>
                          </span>
                        ) : (
                          <span className="flow_btn_content">
                            <span>{t('auth.forgotPassword.resetSubmit')}</span>
                            <ArrowRight className="h-4 w-4" />
                          </span>
                        )}
                      </button>
                    </form>
                  ) : null}
                </div>
              </div>
            </div>
          </StyledForgotPasswordCard>

          <div className="mt-6 text-center text-sm text-muted-foreground">
            <Link
              to="/login"
              className="font-semibold text-primary hover:underline"
            >
              {t('auth.forgotPassword.backToLogin')}
            </Link>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

const StyledForgotPasswordCard = styled.div`
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

  .flow_shell {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    min-height: 470px;
    padding: 12px 0;
  }

  .flow_frame {
    width: min(100%, 460px);
    border-radius: 28px;
    background: var(--auth-shell);
    padding: 10px;
    box-shadow: 0 18px 70px rgba(15, 23, 42, 0.12);
  }

  .flow_card {
    display: flex;
    flex-direction: column;
    gap: 18px;
    border-radius: 22px;
    padding: 34px 30px 26px;
    background: var(--auth-surface);
    border: 1px solid var(--auth-surface-border);
    box-shadow: var(--auth-surface-shadow);
  }

  .flow_notice {
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

  .dark & .flow_notice {
    background: rgba(15, 23, 42, 0.52);
  }

  .flow_notice_icon {
    width: 18px;
    height: 18px;
    margin-top: 1px;
    flex-shrink: 0;
    color: var(--auth-accent);
  }

  .flow_notice_copy {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  .flow_notice_title {
    color: var(--auth-title);
    font-size: 0.95rem;
    font-weight: 700;
    line-height: 1.4;
  }

  .flow_notice_text {
    color: var(--auth-text);
    font-size: 0.82rem;
    line-height: 1.55;
  }

  .flow_content {
    width: 100%;
    max-width: 310px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .flow_field {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .flow_field_label {
    color: var(--auth-title);
    font-size: 0.95rem;
    font-weight: 600;
  }

  .flow_password_wrap {
    position: relative;
  }

  .flow_input {
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

  .flow_input::placeholder {
    color: var(--auth-text);
  }

  .flow_input:focus {
    border-color: rgba(99, 102, 241, 0.45);
    box-shadow: var(--auth-input-shadow-focus);
    transform: translateY(-1px);
  }

  .flow_input--readonly {
    color: var(--auth-text);
    cursor: default;
  }

  .flow_input--otp {
    text-align: center;
    font-size: 1.15rem;
    font-weight: 700;
    letter-spacing: 0.35em;
  }

  .flow_input--with_icon {
    padding-right: 46px;
  }

  .flow_icon_btn {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: var(--auth-text);
    background: transparent;
    border: 0;
    padding: 0;
    cursor: pointer;
  }

  .flow_text_btn {
    width: fit-content;
    align-self: flex-end;
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

  .flow_text_btn:hover {
    text-decoration-color: currentColor;
  }

  .flow_btn {
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

  .flow_btn:hover,
  .flow_btn:focus {
    transform: translateY(-1px);
    box-shadow: var(--auth-btn-shadow-hover);
  }

  .flow_btn:disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }

  .flow_btn_content,
  .loading_inline {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
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
    .flow_shell {
      min-height: 500px;
    }

    .flow_frame {
      width: min(100%, 352px);
      padding: 8px;
    }

    .flow_card {
      padding: 28px 18px 22px;
    }

    .flow_content {
      max-width: 100%;
    }

    .flow_notice {
      max-width: 100%;
    }
  }
`

import { useId, useState, type ReactNode } from 'react'
import styled from 'styled-components'

type AuthFlipCardProps = {
  frontTitle: string
  backTitle: string
  frontContent: ReactNode
  backContent: ReactNode
  frontSwitchText: string
  frontSwitchAction: string
  backSwitchText: string
  backSwitchAction: string
  checked?: boolean
  onCheckedChange?: (checked: boolean) => void
}

export function AuthFlipCard({
  frontTitle,
  backTitle,
  frontContent,
  backContent,
  frontSwitchText,
  frontSwitchAction,
  backSwitchText,
  backSwitchAction,
  checked,
  onCheckedChange,
}: AuthFlipCardProps) {
  const toggleId = useId().replace(/:/g, '')
  const [internalChecked, setInternalChecked] = useState(false)
  const isControlled = checked !== undefined
  const isChecked = isControlled ? checked : internalChecked

  function handleCheckedChange(nextChecked: boolean) {
    if (!isControlled) {
      setInternalChecked(nextChecked)
    }

    onCheckedChange?.(nextChecked)
  }

  return (
    <StyledWrapper>
      <div className="container">
        <input
          type="checkbox"
          id={toggleId}
          className="toggle"
          checked={isChecked}
          onChange={(event) => handleCheckedChange(event.target.checked)}
        />
        <div className="form">
          <div className="form_front">
            <div className="form_details">{frontTitle}</div>
            {frontContent}
            <span className="switch">
              {frontSwitchText}{' '}
              <label className="signup_tog" htmlFor={toggleId}>
                {frontSwitchAction}
              </label>
            </span>
          </div>
          <div className="form_back">
            <div className="form_details">{backTitle}</div>
            {backContent}
            <span className="switch">
              {backSwitchText}{' '}
              <label className="signup_tog" htmlFor={toggleId}>
                {backSwitchAction}
              </label>
            </span>
          </div>
        </div>
      </div>
    </StyledWrapper>
  )
}

const StyledWrapper = styled.div`
  --auth-shell:
    radial-gradient(circle at top, rgba(99, 102, 241, 0.14), transparent 42%),
    radial-gradient(circle at bottom right, rgba(14, 165, 233, 0.1), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 252, 0.98));
  --auth-surface:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(244, 247, 255, 0.96));
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
      radial-gradient(circle at bottom right, rgba(56, 189, 248, 0.14), transparent 34%),
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

  .container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    min-height: 560px;
    padding: 12px 0;
  }

  .toggle {
    display: none;
  }

  .form {
    position: relative;
    width: min(100%, 460px);
    min-height: 530px;
    display: flex;
    justify-content: center;
    align-items: center;
    transform-style: preserve-3d;
    transition: all 1s ease;
    border-radius: 28px;
    background: var(--auth-shell);
    padding: 10px;
    box-shadow: 0 18px 70px rgba(15, 23, 42, 0.12);
  }

  .toggle:checked + .form {
    transform: rotateY(-180deg);
  }

  .form .form_front,
  .form .form_back {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: stretch;
    gap: 18px;
    backface-visibility: hidden;
    padding: 34px 30px 26px;
    border-radius: 22px;
    background: var(--auth-surface);
    border: 1px solid var(--auth-surface-border);
    box-shadow: var(--auth-surface-shadow);
  }

  .form .form_back {
    transform: rotateY(-180deg);
  }

  .form_details {
    font-size: 1.65rem;
    line-height: 1.15;
    font-weight: 700;
    margin-bottom: 4px;
    color: var(--auth-title);
    text-align: center;
  }

  .face_content {
    width: 100%;
    max-width: 310px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    gap: 14px;
  }

  .input {
    width: 100%;
    min-height: 48px;
    color: var(--auth-title);
    outline: none;
    transition:
      border-color 0.25s ease,
      box-shadow 0.25s ease,
      background-color 0.25s ease;
    padding: 0 14px;
    background: var(--auth-input-bg);
    border-radius: 14px;
    border: 1px solid var(--auth-input-border);
    box-shadow: var(--auth-input-shadow);
    appearance: none;
  }

  .input::placeholder {
    color: var(--auth-text);
  }

  .input:focus::placeholder {
    transition: 0.3s;
    opacity: 0.6;
  }

  .input:focus {
    border-color: rgba(99, 102, 241, 0.45);
    box-shadow: var(--auth-input-shadow-focus);
  }

  .btn {
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
    font-weight: bold;
    transition:
      transform 0.2s ease,
      box-shadow 0.2s ease,
      opacity 0.2s ease;
  }

  .btn:hover,
  .btn:focus {
    transform: translateY(-1px);
    box-shadow: var(--auth-btn-shadow-hover);
  }

  .btn:disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }

  .switch {
    margin-top: auto;
    padding-top: 8px;
    font-size: 13px;
    color: var(--auth-text);
    text-align: center;
    line-height: 1.6;
  }

  .signup_tog {
    font-weight: 700;
    cursor: pointer;
    text-decoration: underline;
    text-decoration-color: rgba(99, 102, 241, 0.35);
    color: var(--auth-accent);
  }

  .signup_tog:hover {
    text-decoration-color: currentColor;
  }

  .loading_inline {
    display: inline-flex;
    align-items: center;
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
    .container {
      min-height: 580px;
    }

    .form {
      width: min(100%, 352px);
      min-height: 550px;
      padding: 8px;
    }

    .form .form_front,
    .form .form_back {
      padding: 28px 18px 22px;
    }

    .face_content {
      max-width: 100%;
    }

    .face_content--wide {
      max-width: 100%;
    }

    .field_grid {
      grid-template-columns: 1fr;
      gap: 10px;
    }

    .field_full {
      grid-column: auto;
    }
  }
`

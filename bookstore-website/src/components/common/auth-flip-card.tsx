import { useId, type ReactNode } from 'react'
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
}: AuthFlipCardProps) {
  const toggleId = useId().replace(/:/g, '')

  return (
    <StyledWrapper>
      <div className="container">
        <input type="checkbox" id={toggleId} className="toggle" />
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
  .container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    min-height: 590px;
  }

  .toggle {
    display: none;
  }

  .form {
    position: relative;
    width: min(100%, 430px);
    min-height: 560px;
    display: flex;
    justify-content: center;
    align-items: center;
    transform-style: preserve-3d;
    transition: all 1s ease;
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
    justify-content: center;
    align-items: center;
    gap: 20px;
    backface-visibility: hidden;
    padding: 42px 32px;
    border-radius: 15px;
    background-color: #212121;
    box-shadow:
      inset 2px 2px 10px rgba(0, 0, 0, 1),
      inset -1px -1px 5px rgba(255, 255, 255, 0.28);
  }

  .form .form_back {
    transform: rotateY(-180deg);
  }

  .form_details {
    font-size: 25px;
    font-weight: 600;
    padding-bottom: 10px;
    color: white;
  }

  .face_content {
    width: 245px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }

  .face_content--wide {
    width: 100%;
    max-width: 360px;
  }

  .field_grid {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
  }

  .field_full {
    grid-column: 1 / -1;
  }

  .input {
    width: 100%;
    min-height: 45px;
    color: #fff;
    outline: none;
    transition: 0.35s;
    padding: 0 7px;
    background-color: #212121;
    border-radius: 6px;
    border: 2px solid #212121;
    box-shadow:
      6px 6px 10px rgba(0, 0, 0, 1),
      1px 1px 10px rgba(255, 255, 255, 0.6);
  }

  .input::placeholder {
    color: #999;
  }

  .input:focus::placeholder {
    transition: 0.3s;
    opacity: 0;
  }

  .input:focus {
    transform: scale(1.05);
    box-shadow:
      6px 6px 10px rgba(0, 0, 0, 1),
      1px 1px 10px rgba(255, 255, 255, 0.6),
      inset 2px 2px 10px rgba(0, 0, 0, 1),
      inset -1px -1px 5px rgba(255, 255, 255, 0.6);
  }

  .btn {
    padding: 10px 35px;
    cursor: pointer;
    background-color: #212121;
    border-radius: 6px;
    border: 2px solid #212121;
    box-shadow:
      6px 6px 10px rgba(0, 0, 0, 1),
      1px 1px 10px rgba(255, 255, 255, 0.6);
    color: #fff;
    font-size: 15px;
    font-weight: bold;
    transition: 0.35s;
  }

  .btn:hover,
  .btn:focus {
    transform: scale(1.05);
    box-shadow:
      6px 6px 10px rgba(0, 0, 0, 1),
      1px 1px 10px rgba(255, 255, 255, 0.6),
      inset 2px 2px 10px rgba(0, 0, 0, 1),
      inset -1px -1px 5px rgba(255, 255, 255, 0.6);
  }

  .btn:disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }

  .switch {
    margin-top: auto;
    font-size: 13px;
    color: white;
    text-align: center;
  }

  .signup_tog {
    font-weight: 700;
    cursor: pointer;
    text-decoration: underline;
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
      min-height: 620px;
    }

    .form {
      width: min(100%, 320px);
      min-height: 600px;
    }

    .form .form_front,
    .form .form_back {
      padding: 36px 20px;
    }

    .face_content {
      width: 240px;
    }

    .face_content--wide {
      max-width: 100%;
    }

    .field_grid {
      grid-template-columns: 1fr;
    }

    .field_full {
      grid-column: auto;
    }
  }
`

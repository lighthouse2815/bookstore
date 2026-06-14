/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_GOOGLE_CLIENT_ID?: string
  readonly VITE_BANK_TRANSFER_BANK_NAME?: string
  readonly VITE_BANK_TRANSFER_ACCOUNT_NUMBER?: string
  readonly VITE_BANK_TRANSFER_ACCOUNT_NAME?: string
  readonly VITE_BANK_TRANSFER_QR_URL?: string
}

type GoogleCredentialResponse = {
  credential: string
  select_by: string
}

type GoogleIdConfiguration = {
  callback: (response: GoogleCredentialResponse) => void
  client_id: string
}

type GoogleIdButtonConfiguration = {
  locale?: string
  logo_alignment?: 'center' | 'left'
  shape?: 'circle' | 'pill' | 'rectangular' | 'square'
  size?: 'large' | 'medium' | 'small'
  text?: 'continue_with' | 'signin_with' | 'signup_with'
  theme?: 'filled_black' | 'filled_blue' | 'outline'
  width?: number
}

interface Window {
  google?: {
    accounts: {
      id: {
        initialize: (config: GoogleIdConfiguration) => void
        renderButton: (
          parent: HTMLElement,
          options: GoogleIdButtonConfiguration,
        ) => void
      }
    }
  }
}

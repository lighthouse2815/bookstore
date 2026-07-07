import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { messages, type AppLanguage } from '@/locales/messages'

type TranslationParams = Record<string, number | string>

type LanguageContextValue = {
  language: AppLanguage
  locale: string
  setLanguage: (language: AppLanguage) => void
  toggleLanguage: () => void
  getMessage: <T = unknown>(key: string) => T | undefined
  t: (key: string, params?: TranslationParams) => string
  formatCurrency: (value: number) => string
  formatDate: (value: Date | number | string) => string
  formatNumber: (value: number) => string
}

const LANGUAGE_STORAGE_KEY = 'bookstore-language'
const LOCALE_BY_LANGUAGE: Record<AppLanguage, string> = {
  vi: 'vi-VN',
  en: 'en-US',
}

const LanguageContext = createContext<LanguageContextValue | undefined>(
  undefined,
)

function getStoredLanguage(): AppLanguage {
  if (typeof window === 'undefined') {
    return 'vi'
  }

  const storedLanguage = window.localStorage.getItem(LANGUAGE_STORAGE_KEY)
  return storedLanguage === 'en' ? 'en' : 'vi'
}

function getMessageValue(language: AppLanguage, key: string) {
  return key
    .split('.')
    .reduce<unknown>((currentValue, currentKey) => {
      if (
        currentValue &&
        typeof currentValue === 'object' &&
        currentKey in currentValue
      ) {
        return (currentValue as Record<string, unknown>)[currentKey]
      }

      return undefined
    }, messages[language])
}

function interpolate(template: string, params?: TranslationParams) {
  if (!params) {
    return template
  }

  return template.replace(/\{(\w+)\}/g, (_, key: string) => {
    const value = params[key]
    return value === undefined ? `{${key}}` : String(value)
  })
}

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguage] = useState<AppLanguage>(getStoredLanguage)

  useEffect(() => {
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, language)
    document.documentElement.lang = language
  }, [language])

  const locale = LOCALE_BY_LANGUAGE[language]

  const value = useMemo<LanguageContextValue>(() => {
    const getMessage = <T = unknown>(key: string) => {
      const currentValue = getMessageValue(language, key)
      const fallbackValue = getMessageValue('vi', key)
      return (currentValue ?? fallbackValue) as T | undefined
    }

    const t = (key: string, params?: TranslationParams) => {
      const template = getMessage<string>(key)
      const nextValue = typeof template === 'string' ? template : key

      return interpolate(nextValue, params)
    }

    return {
      language,
      locale,
      setLanguage,
      toggleLanguage: () =>
        setLanguage((currentLanguage) =>
          currentLanguage === 'vi' ? 'en' : 'vi',
        ),
      getMessage,
      t,
      formatCurrency: (value: number) =>
        new Intl.NumberFormat(locale, {
          style: 'currency',
          currency: 'VND',
          maximumFractionDigits: 0,
        }).format(value),
      formatDate: (value: Date | number | string) =>
        new Intl.DateTimeFormat(locale, { dateStyle: 'medium' }).format(
          new Date(value),
        ),
      formatNumber: (value: number) => new Intl.NumberFormat(locale).format(value),
    }
  }, [language, locale])

  return (
    <LanguageContext.Provider value={value}>
      {children}
    </LanguageContext.Provider>
  )
}

export function useLanguage() {
  const context = useContext(LanguageContext)

  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider')
  }

  return context
}

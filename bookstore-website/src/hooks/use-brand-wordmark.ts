import { useLanguage } from '@/contexts/language-context'

export function useBrandWordmark() {
  const { t } = useLanguage()
  const brand = t('common.brand')
  const brandSuffix = brand.endsWith('Vui') ? 'Vui' : ''
  const brandPrefix = brandSuffix ? brand.slice(0, -brandSuffix.length) : brand

  return {
    brand,
    brandPrefix,
    brandSuffix,
  }
}

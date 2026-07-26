export function formatYearValue(value: number, locale: string) {
  return new Intl.NumberFormat(locale, {
    useGrouping: false,
    maximumFractionDigits: 0,
  }).format(value)
}

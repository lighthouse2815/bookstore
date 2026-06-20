const DIRECT_RESOURCE_PREFIXES = ['http://', 'https://', '/', 'blob:', 'data:']

export function resolveDigitalAssetUrl(storageKey?: string | null) {
  const normalizedStorageKey = storageKey?.trim() ?? ''

  if (normalizedStorageKey === '') {
    return null
  }

  // Backend often returns storage metadata keys here, not browser-openable URLs.
  // Only render direct links when the key already looks like a concrete URL/path.
  return DIRECT_RESOURCE_PREFIXES.some((prefix) =>
    normalizedStorageKey.startsWith(prefix),
  )
    ? normalizedStorageKey
    : null
}

export function formatDigitalFileSize(fileSize?: number | null) {
  if (typeof fileSize !== 'number' || Number.isNaN(fileSize) || fileSize < 0) {
    return '--'
  }

  if (fileSize < 1024) {
    return `${fileSize} B`
  }

  if (fileSize < 1024 ** 2) {
    return `${(fileSize / 1024).toFixed(1)} KB`
  }

  if (fileSize < 1024 ** 3) {
    return `${(fileSize / 1024 ** 2).toFixed(1)} MB`
  }

  return `${(fileSize / 1024 ** 3).toFixed(1)} GB`
}

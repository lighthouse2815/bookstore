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

const AVATAR_MAX_DATA_URL_LENGTH = 500

const AVATAR_MIME_TYPES = ['image/webp', 'image/jpeg'] as const
const AVATAR_EDGE_SIZES = [128, 96, 80, 72, 64, 56, 48, 40, 32, 24, 20, 16] as const
const AVATAR_QUALITIES = [0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.15, 0.1, 0.05] as const

export type AvatarFileErrorCode = 'invalid_type' | 'decode_failed' | 'too_large'

export class AvatarFileError extends Error {
  readonly code: AvatarFileErrorCode

  constructor(code: AvatarFileErrorCode) {
    super(code)
    this.name = 'AvatarFileError'
    this.code = code
    Object.setPrototypeOf(this, AvatarFileError.prototype)
  }
}

export async function compressAvatarFile(file: File): Promise<string> {
  if (!file.type.startsWith('image/')) {
    throw new AvatarFileError('invalid_type')
  }

  const image = await loadImage(file)

  for (const mimeType of AVATAR_MIME_TYPES) {
    for (const maxEdge of AVATAR_EDGE_SIZES) {
      const { width, height } = fitImageSize(
        image.naturalWidth,
        image.naturalHeight,
        maxEdge,
      )
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height

      const context = canvas.getContext('2d')
      if (!context) {
        throw new AvatarFileError('decode_failed')
      }

      context.imageSmoothingEnabled = true
      context.imageSmoothingQuality = 'high'
      context.drawImage(image, 0, 0, width, height)

      for (const quality of AVATAR_QUALITIES) {
        try {
          const dataUrl = canvas.toDataURL(mimeType, quality)

          if (dataUrl.length <= AVATAR_MAX_DATA_URL_LENGTH) {
            return dataUrl
          }
        } catch {
          // Try the next encoding option.
        }
      }
    }
  }

  throw new AvatarFileError('too_large')
}

export function getAvatarFileErrorMessage(
  error: unknown,
  isVietnamese: boolean,
  fallbackMessage: string,
) {
  if (!(error instanceof AvatarFileError)) {
    return fallbackMessage
  }

  switch (error.code) {
    case 'invalid_type':
      return isVietnamese
        ? 'Vui lòng chọn một file ảnh hợp lệ.'
        : 'Please choose a valid image file.'
    case 'decode_failed':
      return isVietnamese
        ? 'Không đọc được file ảnh đã chọn.'
        : 'Could not read the selected image file.'
    case 'too_large':
      return isVietnamese
        ? 'Ảnh này quá lớn để lưu. Hãy chọn ảnh nhỏ hơn.'
        : 'This image is too large to save. Please choose a smaller one.'
  }

  return fallbackMessage
}

function fitImageSize(
  sourceWidth: number,
  sourceHeight: number,
  maxEdge: number,
) {
  if (sourceWidth <= 0 || sourceHeight <= 0) {
    throw new AvatarFileError('decode_failed')
  }

  const scale = Math.min(1, maxEdge / sourceWidth, maxEdge / sourceHeight)

  return {
    width: Math.max(1, Math.round(sourceWidth * scale)),
    height: Math.max(1, Math.round(sourceHeight * scale)),
  }
}

function loadImage(file: File) {
  return new Promise<HTMLImageElement>((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file)
    const image = new Image()

    image.decoding = 'async'

    image.onload = () => {
      URL.revokeObjectURL(objectUrl)
      resolve(image)
    }

    image.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new AvatarFileError('decode_failed'))
    }

    image.src = objectUrl
  })
}

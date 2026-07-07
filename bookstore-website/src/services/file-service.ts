import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  CompleteFileUploadRequest,
  FileAssetResponse,
  PresignedUploadResponse,
  PresignUploadRequest,
} from '@/types/file'
import { unwrapResponse } from '@/utils'

export async function createPresignedUpload(
  payload: PresignUploadRequest,
): Promise<PresignedUploadResponse> {
  const response = await api.post<ApiResponse<PresignedUploadResponse>>(
    '/files/presign-upload',
    payload,
  )

  return unwrapResponse(response)
}

export async function completeFileUpload(
  payload: CompleteFileUploadRequest,
): Promise<FileAssetResponse> {
  const response = await api.post<ApiResponse<FileAssetResponse>>(
    '/files/complete-upload',
    payload,
  )

  return unwrapResponse(response)
}

export async function getFileAssetById(
  fileAssetId: string,
): Promise<FileAssetResponse> {
  const response = await api.get<ApiResponse<FileAssetResponse>>(
    `/files/${fileAssetId}`,
  )

  return unwrapResponse(response)
}

export async function deleteFileAsset(fileAssetId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/files/${fileAssetId}`)
}

export async function uploadManagedFile(
  file: File,
  payload: Omit<PresignUploadRequest, 'fileName' | 'contentType' | 'sizeBytes'>,
): Promise<FileAssetResponse> {
  const presignResponse = await createPresignedUpload({
    ...payload,
    fileName: file.name,
    contentType: file.type,
    sizeBytes: file.size,
  })

  const uploadResponse = await fetch(presignResponse.uploadUrl, {
    method: presignResponse.method,
    headers: presignResponse.headers,
    body: file,
  })

  if (!uploadResponse.ok) {
    throw new Error(`Upload failed with status ${uploadResponse.status}`)
  }

  return completeFileUpload({
    fileAssetId: presignResponse.fileAssetId,
  })
}

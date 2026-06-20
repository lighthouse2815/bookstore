import { apiBaseUrl } from '@/src/config';
import type { ApiResult, RequestOptions } from '@/src/types/api';

function buildHeaders(options: RequestOptions, accessToken?: string) {
  const headers = new Headers(options.headers);

  headers.set('Accept', 'application/json');

  if (options.json !== undefined) {
    headers.set('Content-Type', 'application/json');
  }

  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }

  return headers;
}

export async function apiFetch<T>(
  path: string,
  options: RequestOptions = {},
  accessToken?: string,
): Promise<ApiResult<T>> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    headers: buildHeaders(options, accessToken),
    body: options.json !== undefined ? JSON.stringify(options.json) : undefined,
  });

  const rawBody = await response.text();
  const body = rawBody ? JSON.parse(rawBody) : null;

  return {
    ok: response.ok,
    status: response.status,
    body,
  };
}

export function unwrapApiResult<T>(result: ApiResult<T>) {
  const body = result.body;

  if (result.ok && body && body.success !== false && body.data !== null) {
    return body.data as T;
  }

  if (result.ok && body && body.success !== false && body.data === null) {
    return null as T;
  }

  throw new Error(body?.message ?? 'Khong the ket noi den backend');
}

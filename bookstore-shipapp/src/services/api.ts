import { apiBaseUrl, apiConfigurationError } from '@/src/config';
import type { ApiResult, RequestOptions } from '@/src/types/api';

const requestTimeoutMs = 15_000;

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
  if (!apiBaseUrl) {
    throw new Error(apiConfigurationError ?? 'Cau hinh API khong hop le.');
  }

  const controller = new AbortController();
  const abortFromCaller = () => controller.abort();
  options.signal?.addEventListener('abort', abortFromCaller, { once: true });
  const timeout = setTimeout(() => controller.abort(), requestTimeoutMs);

  try {
    const response = await fetch(`${apiBaseUrl}${path}`, {
      ...options,
      headers: buildHeaders(options, accessToken),
      body: options.json !== undefined ? JSON.stringify(options.json) : undefined,
      signal: controller.signal,
    });

    const rawBody = await response.text();
    let body = null;

    if (rawBody) {
      try {
        body = JSON.parse(rawBody);
      } catch {
        // Keep an unparseable response as a normal failed API result.
      }
    }

    return {
      ok: response.ok,
      status: response.status,
      body,
    };
  } catch (error) {
    if (error instanceof Error && error.name === 'AbortError') {
      throw new Error('Yeu cau mat qua lau. Kiem tra ket noi mang va thu lai.');
    }

    throw new Error('Khong the ket noi den backend. Kiem tra mang va API base URL, sau do thu lai.');
  } finally {
    clearTimeout(timeout);
    options.signal?.removeEventListener('abort', abortFromCaller);
  }
}

export function unwrapApiResult<T>(result: ApiResult<T>) {
  const body = result.body;

  if (result.ok && body && body.success !== false && body.data !== null) {
    return body.data as T;
  }

  if (result.ok && body && body.success !== false && body.data === null) {
    return null as T;
  }

  if (body?.message) {
    throw new Error(body.message);
  }

  if (result.status >= 500) {
    throw new Error('Backend dang gap su co. Vui long thu lai sau.');
  }

  throw new Error('Yeu cau khong thanh cong. Vui long thu lai.');
}

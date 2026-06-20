import { apiFetch, unwrapApiResult } from '@/src/services/api';
import { clearStoredSession, loadStoredSession, storeSession } from '@/src/services/storage';
import type { LoginPayload, Session } from '@/src/types/auth';

interface LoginResponse {
  userId: string;
  status: string;
  roles: string[];
  accessToken: string;
  refreshToken: string;
}

function toSession(payload: LoginResponse): Session {
  return {
    userId: payload.userId,
    status: payload.status,
    roles: payload.roles,
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
  };
}

export async function bootstrapSession() {
  return loadStoredSession();
}

export async function signIn(payload: LoginPayload) {
  const result = await apiFetch<LoginResponse>('/auth/login', {
    method: 'POST',
    json: payload,
  });
  const session = toSession(unwrapApiResult(result));

  await storeSession(session);
  return session;
}

export async function refreshCurrentSession(refreshToken: string) {
  const result = await apiFetch<LoginResponse>('/auth/refresh', {
    method: 'POST',
    json: { refreshToken },
  });
  const session = toSession(unwrapApiResult(result));

  await storeSession(session);
  return session;
}

export async function signOut(refreshToken?: string | null) {
  try {
    if (refreshToken) {
      const result = await apiFetch<void>('/auth/logout', {
        method: 'POST',
        json: { refreshToken },
      });
      unwrapApiResult(result);
    }
  } catch {
    // Local logout still wins even if backend revoke fails.
  } finally {
    await clearStoredSession();
  }
}

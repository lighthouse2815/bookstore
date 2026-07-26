import type { ApiResult } from '@/src/types/api';

export function hasShipperRole(roles: readonly string[]): boolean {
  return roles.includes('SHIPPER');
}

export function createSingleFlight<TArgs extends readonly unknown[], TResult>(
  operation: (...args: TArgs) => Promise<TResult>,
) {
  let inFlight: Promise<TResult> | null = null;

  return (...args: TArgs): Promise<TResult> => {
    if (!inFlight) {
      inFlight = operation(...args).finally(() => {
        inFlight = null;
      });
    }

    return inFlight;
  };
}

export async function retryOnceAfterUnauthorized<T, TSession extends { accessToken: string }>(
  initialResult: ApiResult<T>,
  session: TSession,
  send: (accessToken: string) => Promise<ApiResult<T>>,
  refresh: (session: TSession) => Promise<TSession>,
): Promise<ApiResult<T>> {
  if (initialResult.status !== 401) {
    return initialResult;
  }

  const refreshedSession = await refresh(session);
  return send(refreshedSession.accessToken);
}

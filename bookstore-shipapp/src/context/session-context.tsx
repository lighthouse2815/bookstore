import { apiFetch, unwrapApiResult } from '@/src/services/api';
import { createSingleFlight, retryOnceAfterUnauthorized } from '@/src/lib/session-policy';
import {
  bootstrapSession,
  refreshCurrentSession,
  signIn as authSignIn,
  signOut as authSignOut,
} from '@/src/services/auth';
import type { LoginPayload, Session } from '@/src/types/auth';
import type { RequestOptions } from '@/src/types/api';
import { useRouter } from 'expo-router';
import {
  createContext,
  type PropsWithChildren,
  useContext,
  useEffect,
  useRef,
  useState,
} from 'react';

interface SessionContextValue {
  hydrated: boolean;
  session: Session | null;
  signIn: (payload: LoginPayload) => Promise<Session>;
  signOut: () => Promise<void>;
  request: <T>(path: string, options?: RequestOptions) => Promise<T>;
}

const SessionContext = createContext<SessionContextValue | null>(null);

export function SessionProvider({ children }: PropsWithChildren) {
  const router = useRouter();
  const [hydrated, setHydrated] = useState(false);
  const [session, setSession] = useState<Session | null>(null);
  const sessionRef = useRef<Session | null>(null);
  const signOutInFlightRef = useRef<Promise<void> | null>(null);
  const refreshSession = useRef(
    createSingleFlight(async (currentSession: Session) => {
      const refreshedSession = await refreshCurrentSession(currentSession.refreshToken);
      sessionRef.current = refreshedSession;
      setSession(refreshedSession);
      return refreshedSession;
    }),
  ).current;

  useEffect(() => {
    sessionRef.current = session;
  }, [session]);

  useEffect(() => {
    let mounted = true;

    async function hydrate() {
      try {
        const storedSession = await bootstrapSession();

        if (mounted) {
          setSession(storedSession);
        }
      } finally {
        if (mounted) {
          setHydrated(true);
        }
      }
    }

    void hydrate();

    return () => {
      mounted = false;
    };
  }, []);

  async function handleSignIn(payload: LoginPayload) {
    const nextSession = await authSignIn(payload);

    sessionRef.current = nextSession;
    setSession(nextSession);
    return nextSession;
  }

  async function handleSignOut() {
    if (signOutInFlightRef.current) {
      return signOutInFlightRef.current;
    }

    const refreshToken = sessionRef.current?.refreshToken;

    sessionRef.current = null;
    setSession(null);
    const signOutPromise = authSignOut(refreshToken).then(() => {
      router.replace('/login');
    });
    signOutInFlightRef.current = signOutPromise;

    try {
      await signOutPromise;
    } finally {
      signOutInFlightRef.current = null;
    }
  }

  async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const activeSession = sessionRef.current;
    const send = (accessToken: string) => apiFetch<T>(path, options, accessToken);
    let result = await apiFetch<T>(path, options, activeSession?.accessToken);

    if (result.status === 401) {
      if (!activeSession?.refreshToken) {
        await handleSignOut();
        throw new Error('Phien dang nhap da het han. Vui long dang nhap lai.');
      }

      try {
        result = await retryOnceAfterUnauthorized(
          result,
          activeSession,
          send,
          async (session) => {
            const currentSession = sessionRef.current;
            if (currentSession && currentSession.accessToken !== session.accessToken) {
              return currentSession;
            }
            return refreshSession(session);
          },
        );
      } catch {
        await handleSignOut();
        throw new Error('Phien dang nhap da het han');
      }

      if (result.status === 401) {
        await handleSignOut();
        throw new Error('Phien dang nhap da het han. Vui long dang nhap lai.');
      }
    }

    return unwrapApiResult(result);
  }

  return (
    <SessionContext.Provider
      value={{
        hydrated,
        session,
        signIn: handleSignIn,
        signOut: handleSignOut,
        request,
      }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const context = useContext(SessionContext);

  if (!context) {
    throw new Error('useSession phai duoc dung ben trong SessionProvider');
  }

  return context;
}

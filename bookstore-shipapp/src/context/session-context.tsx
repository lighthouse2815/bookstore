import { apiFetch, unwrapApiResult } from '@/src/services/api';
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

    if (!nextSession.roles.includes('SHIPPER')) {
      await authSignOut(nextSession.refreshToken);
      throw new Error('Tai khoan nay khong co quyen SHIPPER');
    }

    sessionRef.current = nextSession;
    setSession(nextSession);
    return nextSession;
  }

  async function handleSignOut() {
    const refreshToken = sessionRef.current?.refreshToken;

    sessionRef.current = null;
    setSession(null);
    await authSignOut(refreshToken);
    router.replace('/login');
  }

  async function request<T>(path: string, options: RequestOptions = {}, allowRetry = true): Promise<T> {
    const activeSession = sessionRef.current;
    const result = await apiFetch<T>(path, options, activeSession?.accessToken);

    if (result.status === 401 && allowRetry && activeSession?.refreshToken) {
      try {
        const refreshedSession = await refreshCurrentSession(activeSession.refreshToken);
        sessionRef.current = refreshedSession;
        setSession(refreshedSession);
        return request<T>(path, options, false);
      } catch {
        await handleSignOut();
        throw new Error('Phien dang nhap da het han');
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

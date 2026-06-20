import type { Session } from '@/src/types/auth';
import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

const sessionKey = 'bookstore_shipapp_session';

function hasBrowserStorage() {
  return Platform.OS === 'web' && typeof window !== 'undefined' && !!window.localStorage;
}

async function readRawValue() {
  if (hasBrowserStorage()) {
    return window.localStorage.getItem(sessionKey);
  }

  return SecureStore.getItemAsync(sessionKey);
}

async function writeRawValue(value: string) {
  if (hasBrowserStorage()) {
    window.localStorage.setItem(sessionKey, value);
    return;
  }

  await SecureStore.setItemAsync(sessionKey, value);
}

async function clearRawValue() {
  if (hasBrowserStorage()) {
    window.localStorage.removeItem(sessionKey);
    return;
  }

  await SecureStore.deleteItemAsync(sessionKey);
}

export async function loadStoredSession() {
  const rawValue = await readRawValue();

  if (!rawValue) {
    return null;
  }

  try {
    return JSON.parse(rawValue) as Session;
  } catch {
    await clearRawValue();
    return null;
  }
}

export async function storeSession(session: Session) {
  await writeRawValue(JSON.stringify(session));
}

export async function clearStoredSession() {
  await clearRawValue();
}

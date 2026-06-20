import 'react-native-reanimated';

import { SessionProvider } from '@/src/context/session-context';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';

export {
  ErrorBoundary,
} from 'expo-router';

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <SessionProvider>
        <StatusBar style="dark" />
        <Stack
          screenOptions={{
            contentStyle: { backgroundColor: '#F3F7FB' },
            headerShadowVisible: false,
            headerStyle: { backgroundColor: '#F3F7FB' },
            headerTitleStyle: { fontWeight: '800' },
          }}>
          <Stack.Screen name="index" options={{ headerShown: false }} />
          <Stack.Screen name="login" options={{ headerShown: false }} />
          <Stack.Screen name="shipments/index" options={{ title: 'Chuyen giao' }} />
          <Stack.Screen name="shipments/[id]" options={{ title: 'Chi tiet chuyen' }} />
        </Stack>
      </SessionProvider>
    </SafeAreaProvider>
  );
}

import 'react-native-reanimated';

import { apiConfigurationError, palette } from '@/src/config';
import { SessionProvider } from '@/src/context/session-context';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { StyleSheet, Text, View } from 'react-native';

export {
  ErrorBoundary,
} from 'expo-router';

export default function RootLayout() {
  if (apiConfigurationError) {
    return (
      <SafeAreaProvider>
        <StatusBar style="dark" />
        <View style={styles.configurationScreen}>
          <View style={styles.configurationCard}>
            <Text style={styles.configurationEyebrow}>CAN CAU HINH TRUOC KHI DI GIAO</Text>
            <Text style={styles.configurationTitle}>Khong the ket noi API</Text>
            <Text style={styles.configurationText}>{apiConfigurationError}</Text>
            <Text style={styles.configurationHint}>
              Sau khi sua .env, hay full reload Expo Go hoac dev build de ap dung URL moi.
            </Text>
          </View>
        </View>
      </SafeAreaProvider>
    );
  }

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

const styles = StyleSheet.create({
  configurationScreen: {
    alignItems: 'center',
    backgroundColor: palette.background,
    flex: 1,
    justifyContent: 'center',
    padding: 24,
  },
  configurationCard: {
    backgroundColor: palette.surface,
    borderColor: palette.danger,
    borderRadius: 20,
    borderWidth: 1,
    gap: 14,
    maxWidth: 520,
    padding: 22,
    width: '100%',
  },
  configurationEyebrow: {
    color: palette.danger,
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  configurationTitle: {
    color: palette.text,
    fontSize: 24,
    fontWeight: '900',
  },
  configurationText: {
    color: palette.text,
    fontSize: 15,
    lineHeight: 23,
  },
  configurationHint: {
    color: palette.textMuted,
    fontSize: 14,
    lineHeight: 21,
  },
});

import { palette } from '@/src/config';
import { ActionButton, Panel } from '@/src/components/ui';
import { useSession } from '@/src/context/session-context';
import { Redirect, router } from 'expo-router';
import { useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

export default function LoginScreen() {
  const { hydrated, session, signIn } = useSession();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (hydrated && session) {
    return <Redirect href="/shipments" />;
  }

  async function handleSubmit() {
    if (!username.trim() || !password) {
      setError('Nhap day du ten dang nhap va mat khau');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      await signIn({
        username: username.trim(),
        password,
      });
      router.replace('/shipments');
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Dang nhap that bai');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={styles.keyboardView}>
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <View style={styles.hero}>
          <View style={styles.pill}>
            <Text style={styles.pillText}>Bookstore ship app</Text>
          </View>
          <Text style={styles.title}>Dang nhap shipper</Text>
          <Text style={styles.subtitle}>
            App nay dung rieng cho tai khoan co role SHIPPER va chi lam viec voi API giao hang.
          </Text>
        </View>

        <Panel style={styles.panel}>
          <View style={styles.fieldGroup}>
            <Text style={styles.label}>Ten dang nhap</Text>
            <TextInput
              autoCapitalize="none"
              autoCorrect={false}
              onChangeText={setUsername}
              placeholder="Nhap username shipper"
              placeholderTextColor={palette.textMuted}
              style={styles.input}
              value={username}
            />
          </View>

          <View style={styles.fieldGroup}>
            <Text style={styles.label}>Mat khau</Text>
            <TextInput
              onChangeText={setPassword}
              placeholder="Nhap mat khau"
              placeholderTextColor={palette.textMuted}
              secureTextEntry
              style={styles.input}
              value={password}
            />
          </View>

          {error ? <Text style={styles.error}>{error}</Text> : null}

          <ActionButton
            icon="login"
            label={submitting ? 'Dang dang nhap...' : 'Vao ca giao'}
            loading={submitting}
            onPress={() => {
              void handleSubmit();
            }}
          />
        </Panel>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  keyboardView: {
    backgroundColor: '#DCE9F5',
    flex: 1,
  },
  content: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 24,
  },
  hero: {
    gap: 12,
    marginBottom: 24,
  },
  pill: {
    alignSelf: 'flex-start',
    backgroundColor: '#D9F99D',
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  pillText: {
    color: '#365314',
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  title: {
    color: palette.text,
    fontSize: 34,
    fontWeight: '900',
    lineHeight: 40,
  },
  subtitle: {
    color: palette.textMuted,
    fontSize: 15,
    lineHeight: 24,
    maxWidth: 420,
  },
  panel: {
    gap: 18,
  },
  fieldGroup: {
    gap: 8,
  },
  label: {
    color: palette.text,
    fontSize: 14,
    fontWeight: '700',
  },
  input: {
    backgroundColor: palette.surfaceMuted,
    borderColor: palette.border,
    borderRadius: 14,
    borderWidth: 1,
    color: palette.text,
    fontSize: 16,
    minHeight: 52,
    paddingHorizontal: 16,
  },
  error: {
    color: palette.danger,
    fontSize: 14,
    fontWeight: '600',
    lineHeight: 20,
  },
});

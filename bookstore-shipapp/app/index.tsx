import { palette } from '@/src/config';
import { useSession } from '@/src/context/session-context';
import { Redirect } from 'expo-router';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

export default function IndexScreen() {
  const { hydrated, session } = useSession();

  if (!hydrated) {
    return (
      <View style={styles.container}>
        <ActivityIndicator color={palette.primary} size="large" />
        <Text style={styles.caption}>Dang tai phien shipper...</Text>
      </View>
    );
  }

  return <Redirect href={session ? '/shipments' : '/login'} />;
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    backgroundColor: palette.background,
    flex: 1,
    gap: 14,
    justifyContent: 'center',
  },
  caption: {
    color: palette.textMuted,
    fontSize: 14,
    fontWeight: '600',
  },
});

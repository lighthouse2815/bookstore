import { palette } from '@/src/config';
import { Link, Stack } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';

export default function NotFoundScreen() {
  return (
    <>
      <Stack.Screen options={{ title: 'Oops!' }} />
      <View style={styles.container}>
        <Text style={styles.title}>Man hinh nay khong ton tai.</Text>

        <Link href="/" style={styles.link}>
          <Text style={styles.linkText}>Quay ve trang chinh</Text>
        </Link>
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: palette.background,
    justifyContent: 'center',
    padding: 20,
  },
  title: {
    color: palette.text,
    fontSize: 20,
    fontWeight: 'bold',
  },
  link: {
    marginTop: 15,
    paddingVertical: 15,
  },
  linkText: {
    fontSize: 14,
    color: palette.primary,
  },
});

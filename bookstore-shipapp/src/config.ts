const defaultBaseUrl = 'http://localhost:8080/api';

export const apiBaseUrl = (process.env.EXPO_PUBLIC_API_BASE_URL ?? defaultBaseUrl).replace(/\/+$/, '');

export const palette = {
  background: '#F3F7FB',
  surface: '#FFFFFF',
  surfaceMuted: '#E6EEF5',
  text: '#102033',
  textMuted: '#607184',
  border: '#D7E0E8',
  primary: '#0F766E',
  primaryDark: '#115E59',
  accent: '#D97706',
  success: '#15803D',
  danger: '#B91C1C',
  shadow: 'rgba(15, 23, 42, 0.08)',
};

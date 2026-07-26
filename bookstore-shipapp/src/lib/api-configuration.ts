export function getApiConfigurationError(value: string | undefined, platform: string) {
  if (!value) {
    return 'Thieu EXPO_PUBLIC_API_BASE_URL. Tao file .env tu .env.example, dat URL backend, roi tai lai ung dung.';
  }

  let parsedUrl: URL;

  try {
    parsedUrl = new URL(value);
  } catch {
    return 'EXPO_PUBLIC_API_BASE_URL phai la URL day du, vi du http://<LAN_IP>:<port>/api.';
  }

  if (parsedUrl.protocol !== 'http:' && parsedUrl.protocol !== 'https:') {
    return 'EXPO_PUBLIC_API_BASE_URL chi ho tro http hoac https.';
  }

  if (parsedUrl.search || parsedUrl.hash || parsedUrl.pathname.replace(/\/+$/, '') !== '/api') {
    return 'EXPO_PUBLIC_API_BASE_URL phai tro den duong dan /api cua backend.';
  }

  const host = parsedUrl.hostname.toLowerCase();
  const localOnlyHost = host === 'localhost' || host === '127.0.0.1' || host === '::1';

  if (platform !== 'web' && localOnlyHost) {
    return 'Khong dung localhost tren thiet bi. Android Emulator dung 10.0.2.2; dien thoai that dung LAN IP cua may backend.';
  }

  return null;
}

import { AuthProvider } from '@/contexts/auth-context'
import { CartProvider } from '@/contexts/cart-context'
import { LanguageProvider } from '@/contexts/language-context'
import { ThemeProvider } from '@/contexts/theme-context'
import AppRoutes from '@/routes/AppRoutes'
import { Toaster } from '@/components/common/sonner'

export function App() {
  return (
    <LanguageProvider>
      <ThemeProvider>
        <AuthProvider>
          <CartProvider>
            <AppRoutes />
            <Toaster position="bottom-right" />
          </CartProvider>
        </AuthProvider>
      </ThemeProvider>
    </LanguageProvider>
  )
}

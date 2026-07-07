import { AuthProvider } from '@/contexts/auth-context'
import { CartProvider } from '@/contexts/cart-context'
import { ChatProvider } from '@/contexts/chat-context'
import { LanguageProvider } from '@/contexts/language-context'
import { NotificationProvider } from '@/contexts/notification-context'
import { ThemeProvider } from '@/contexts/theme-context'
import { DeployStartupGate } from '@/components/common/deploy-startup-gate'
import AppRoutes from '@/routes/AppRoutes'
import { Toaster } from '@/components/common/sonner'

export function App() {
  return (
    <LanguageProvider>
      <ThemeProvider>
        <DeployStartupGate>
          <AuthProvider>
            <NotificationProvider>
              <ChatProvider>
                <CartProvider>
                  <AppRoutes />
                  <Toaster position="bottom-right" />
                </CartProvider>
              </ChatProvider>
            </NotificationProvider>
          </AuthProvider>
        </DeployStartupGate>
      </ThemeProvider>
    </LanguageProvider>
  )
}

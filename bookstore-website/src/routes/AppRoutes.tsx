import { lazy, Suspense, useEffect, type ReactNode } from 'react'
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
  useLocation,
} from 'react-router-dom'
import { CustomerChatWidget } from '@/components/chat/customer-chat-widget'
import { useLanguage } from '@/contexts/language-context'
import { ProtectedRoute } from './protected-route'

const HomePage = lazy(() => import('@/pages/home/home'))
const BooksPage = lazy(() => import('@/pages/book/books'))
const BookMatchPage = lazy(() => import('@/pages/book/book-match'))
const GiftFinderPage = lazy(() => import('@/pages/book/gift-finder'))
const ShelvesPage = lazy(() => import('@/pages/book/shelves'))
const ShelfDetailPage = lazy(() => import('@/pages/book/shelf-detail'))
const ReadingJournalPage = lazy(() => import('@/pages/book/reading-journal'))
const CouponGamePage = lazy(() => import('@/pages/home/coupon-game'))
const ReadingChallengePage = lazy(
  () => import('@/pages/home/reading-challenge'),
)
const EbooksPage = lazy(() => import('@/pages/book/ebooks'))
const BookDetailPage = lazy(() => import('@/pages/book/book-detail'))
const BookEbookPage = lazy(() => import('@/pages/book/book-ebook'))
const CartPage = lazy(() => import('@/pages/cart/cart'))
const CheckoutPage = lazy(() => import('@/pages/cart/checkout'))
const NotFoundPage = lazy(() => import('@/pages/home/not-found'))
const ShippingPolicyPage = lazy(() => import('@/pages/support/shipping-policy'))
const ReturnsRefundsPage = lazy(() => import('@/pages/support/returns-refunds'))
const FaqPage = lazy(() => import('@/pages/support/faq'))
const ContactPage = lazy(() => import('@/pages/support/contact'))
const NewsletterUnsubscribePage = lazy(
  () => import('@/pages/support/newsletter-unsubscribe'),
)
const OrderConfirmationPage = lazy(
  () => import('@/pages/order/order-confirmation'),
)
const MyOrdersPage = lazy(() => import('@/pages/order/my-orders'))
const OrderDetailPage = lazy(() => import('@/pages/order/order-detail'))
const ReturnRequestsPage = lazy(() => import('@/pages/order/return-requests'))
const WishlistPage = lazy(() => import('@/pages/book/wishlist'))
const NotificationsPage = lazy(() => import('@/pages/notifications/notifications'))
const DigitalLibraryPage = lazy(() => import('@/pages/library/digital-library'))
const DigitalLibraryDetailPage = lazy(
  () => import('@/pages/library/digital-library-detail'),
)
const DigitalLibraryReaderPage = lazy(
  () => import('@/pages/library/digital-library-reader'),
)
const LoginPage = lazy(() => import('@/pages/auth/login'))
const ForgotPasswordPage = lazy(() => import('@/pages/auth/forgot-password'))
const RegisterPage = lazy(() => import('@/pages/auth/register'))
const ProfilePage = lazy(() => import('@/pages/auth/profile'))

const AdminDashboard = lazy(() => import('@/pages/admin/dashboard'))
const AdminAuditLogsPage = lazy(() => import('@/pages/admin/audit-logs'))
const AdminReportsPage = lazy(() => import('@/pages/admin/reports'))
const AdminBooksPage = lazy(() => import('@/pages/admin/books'))
const AdminDigitalAssetsPage = lazy(() => import('@/pages/admin/digital-assets'))
const AdminOrdersPage = lazy(() => import('@/pages/admin/orders'))
const AdminPaymentReconciliationPage = lazy(
  () => import('@/pages/admin/payment-reconciliation'),
)
const AdminRefundsPage = lazy(() => import('@/pages/admin/refunds'))
const AdminOutboxPage = lazy(() => import('@/pages/admin/outbox'))
const AdminReturnRequestsPage = lazy(
  () => import('@/pages/admin/return-requests'),
)
const AdminShipmentsPage = lazy(() => import('@/pages/admin/shipments'))
const AdminCategoriesPage = lazy(() => import('@/pages/admin/categories'))
const AdminAuthorsPage = lazy(() => import('@/pages/admin/authors'))
const AdminPublishersPage = lazy(() => import('@/pages/admin/publishers'))
const AdminSuppliersPage = lazy(() => import('@/pages/admin/suppliers'))
const AdminImportReceiptsPage = lazy(
  () => import('@/pages/admin/import-receipts'),
)
const AdminInventoryPage = lazy(() => import('@/pages/admin/inventory'))
const AdminReviewsPage = lazy(() => import('@/pages/admin/reviews'))
const AdminNotificationsPage = lazy(
  () => import('@/pages/admin/notifications'),
)
const AdminCustomersPage = lazy(() => import('@/pages/admin/customers'))
const AdminStaffPage = lazy(() => import('@/pages/admin/staff'))
const AdminRolesPage = lazy(() => import('@/pages/admin/roles'))
const AdminPermissionsPage = lazy(() => import('@/pages/admin/permissions'))
const AdminPromotionsPage = lazy(() => import('@/pages/admin/promotions'))
const AdminReferencesPage = lazy(() => import('@/pages/admin/references'))
const AdminSettingsPage = lazy(() => import('@/pages/admin/settings'))
const AdminChatPage = lazy(() => import('@/pages/admin/admin-chat-page'))

function ScrollToTop() {
  const { pathname } = useLocation()

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])

  return null
}

function RouteLoading() {
  const { t } = useLanguage()

  return (
    <div className="flex min-h-[40vh] items-center justify-center px-4 py-12">
      <p className="text-sm text-muted-foreground">{t('common.loading')}</p>
    </div>
  )
}

function LazyPage({ children }: { children: ReactNode }) {
  return <Suspense fallback={<RouteLoading />}>{children}</Suspense>
}

function AppRouteContent() {
  return (
    <>
      <ScrollToTop />
      <Routes>
        <Route
          path="/"
          element={
            <LazyPage>
              <HomePage />
            </LazyPage>
          }
        />
        <Route
          path="/login"
          element={
            <LazyPage>
              <LoginPage />
            </LazyPage>
          }
        />
        <Route
          path="/register"
          element={
            <LazyPage>
              <RegisterPage />
            </LazyPage>
          }
        />
        <Route
          path="/forgot-password"
          element={
            <LazyPage>
              <ForgotPasswordPage />
            </LazyPage>
          }
        />
        <Route
          path="/books"
          element={
            <LazyPage>
              <BooksPage />
            </LazyPage>
          }
        />
        <Route
          path="/book-match"
          element={
            <LazyPage>
              <BookMatchPage />
            </LazyPage>
          }
        />
        <Route
          path="/gift-finder"
          element={
            <LazyPage>
              <GiftFinderPage />
            </LazyPage>
          }
        />
        <Route
          path="/coupon-game"
          element={
            <LazyPage>
              <CouponGamePage />
            </LazyPage>
          }
        />
        <Route
          path="/reading-challenge"
          element={
            <LazyPage>
              <ReadingChallengePage />
            </LazyPage>
          }
        />
        <Route
          path="/ebooks"
          element={
            <LazyPage>
              <EbooksPage />
            </LazyPage>
          }
        />
        <Route
          path="/books/:id"
          element={
            <LazyPage>
              <BookDetailPage />
            </LazyPage>
          }
        />
        <Route
          path="/books/:id/ebook"
          element={
            <LazyPage>
              <BookEbookPage />
            </LazyPage>
          }
        />
        <Route
          path="/shipping-policy"
          element={
            <LazyPage>
              <ShippingPolicyPage />
            </LazyPage>
          }
        />
        <Route
          path="/returns-refunds"
          element={
            <LazyPage>
              <ReturnsRefundsPage />
            </LazyPage>
          }
        />
        <Route
          path="/faq"
          element={
            <LazyPage>
              <FaqPage />
            </LazyPage>
          }
        />
        <Route
          path="/contact"
          element={
            <LazyPage>
              <ContactPage />
            </LazyPage>
          }
        />

        <Route
          path="/cart"
          element={
            <ProtectedRoute>
              <LazyPage>
                <CartPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/checkout"
          element={
            <ProtectedRoute>
              <LazyPage>
                <CheckoutPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <LazyPage>
                <MyOrdersPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders/:id"
          element={
            <ProtectedRoute>
              <LazyPage>
                <OrderDetailPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/return-requests"
          element={
            <ProtectedRoute>
              <LazyPage>
                <ReturnRequestsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/wishlist"
          element={
            <ProtectedRoute>
              <LazyPage>
                <WishlistPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/newsletter/unsubscribe"
          element={
            <LazyPage>
              <NewsletterUnsubscribePage />
            </LazyPage>
          }
        />
        <Route
          path="/shelves"
          element={
            <ProtectedRoute>
              <LazyPage>
                <ShelvesPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/shelves/:shelfId"
          element={
            <ProtectedRoute>
              <LazyPage>
                <ShelfDetailPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/reading-journal"
          element={
            <ProtectedRoute>
              <LazyPage>
                <ReadingJournalPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/order-confirmation"
          element={
            <ProtectedRoute>
              <LazyPage>
                <OrderConfirmationPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/notifications"
          element={
            <ProtectedRoute>
              <LazyPage>
                <NotificationsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <LazyPage>
                <ProfilePage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/library"
          element={
            <ProtectedRoute>
              <LazyPage>
                <DigitalLibraryPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/library/:digitalAssetId"
          element={
            <ProtectedRoute>
              <LazyPage>
                <DigitalLibraryDetailPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/library/:digitalAssetId/read"
          element={
            <ProtectedRoute>
              <LazyPage>
                <DigitalLibraryReaderPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminDashboard />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <Navigate to="/admin" replace />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/audit-logs"
          element={
            <ProtectedRoute requiredRoles={['ADMIN', 'STAFF']}>
              <LazyPage>
                <AdminAuditLogsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/reports"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminReportsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/books"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminBooksPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/digital-assets"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminDigitalAssetsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/orders"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminOrdersPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/payment-reconciliation"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminPaymentReconciliationPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/refunds"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminRefundsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/outbox"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminOutboxPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/return-requests"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminReturnRequestsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/shipments"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminShipmentsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/categories"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminCategoriesPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/authors"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminAuthorsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/publishers"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminPublishersPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/suppliers"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminSuppliersPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/import-receipts"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminImportReceiptsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/inventory"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminInventoryPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/reviews"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminReviewsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/chat"
          element={
            <ProtectedRoute requiredRoles={['ADMIN', 'STAFF']}>
              <LazyPage>
                <AdminChatPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/notifications"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminNotificationsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/customers"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminCustomersPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/staff"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminStaffPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <Navigate to="/admin/customers" replace />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/roles"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminRolesPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/permissions"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminPermissionsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/promotions"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminPromotionsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/references"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminReferencesPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/settings"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <LazyPage>
                <AdminSettingsPage />
              </LazyPage>
            </ProtectedRoute>
          }
        />
        <Route
          path="*"
          element={
            <LazyPage>
              <NotFoundPage />
            </LazyPage>
          }
        />
      </Routes>
      <CustomerChatWidget />
    </>
  )
}

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <AppRouteContent />
    </BrowserRouter>
  )
}

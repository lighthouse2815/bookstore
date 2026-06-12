import { lazy, Suspense, useEffect, type ReactNode } from 'react'
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
  useLocation,
} from 'react-router-dom'
import { useLanguage } from '@/contexts/language-context'
import { ProtectedRoute } from './protected-route'

const HomePage = lazy(() => import('@/pages/home/home'))
const BooksPage = lazy(() => import('@/pages/book/books'))
const BookDetailPage = lazy(() => import('@/pages/book/book-detail'))
const CartPage = lazy(() => import('@/pages/cart/cart'))
const CheckoutPage = lazy(() => import('@/pages/cart/checkout'))
const NotFoundPage = lazy(() => import('@/pages/home/not-found'))
const ShippingPolicyPage = lazy(() => import('@/pages/support/shipping-policy'))
const ReturnsRefundsPage = lazy(() => import('@/pages/support/returns-refunds'))
const FaqPage = lazy(() => import('@/pages/support/faq'))
const ContactPage = lazy(() => import('@/pages/support/contact'))
const OrderConfirmationPage = lazy(
  () => import('@/pages/order/order-confirmation'),
)
const MyOrdersPage = lazy(() => import('@/pages/order/my-orders'))
const OrderDetailPage = lazy(() => import('@/pages/order/order-detail'))
const LoginPage = lazy(() => import('@/pages/auth/login'))
const ForgotPasswordPage = lazy(() => import('@/pages/auth/forgot-password'))
const RegisterPage = lazy(() => import('@/pages/auth/register'))
const ProfilePage = lazy(() => import('@/pages/auth/profile'))

const AdminDashboard = lazy(() => import('@/pages/admin/dashboard'))
const AdminBooksPage = lazy(() => import('@/pages/admin/books'))
const AdminOrdersPage = lazy(() => import('@/pages/admin/orders'))
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
          path="/books/:id"
          element={
            <LazyPage>
              <BookDetailPage />
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
